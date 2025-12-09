package com.feishu.tabfeatures.stock.data.repository

import android.util.Log
import com.feishu.tabfeatures.stock.data.api.BrokerRealTimeData
import com.feishu.tabfeatures.stock.data.api.CompanyInfo
import com.feishu.tabfeatures.stock.data.api.FinancialIndicator
import com.feishu.tabfeatures.stock.data.api.PerformanceForecast
import com.feishu.tabfeatures.stock.data.api.StockListItem
import com.feishu.tabfeatures.stock.data.api.StockNetworkModule
import com.feishu.tabfeatures.stock.data.api.TimeSeriesData
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * 股票数据仓库
 * 负责数据获取和转换
 */
class StockRepository {
    private val apiService = StockNetworkModule.stockApiService
    private val licence = StockNetworkModule.getLicence()

    private val TAG = "StockRepository"
    private var cachedStockList: List<StockListItem>? = null

    /**
     * 获取实时交易数据（使用券商数据源）
     */
    suspend fun getRealTimeData(code: String): Result<BrokerRealTimeData?> {
        return try {
            // 构建完整请求 URL
            val requestUrl = "${StockNetworkModule.BASE_URL}hsstock/real/time/$code/$licence"
            Log.d(TAG, "Fetching real-time data from broker data source")
            Log.d(TAG, "Request URL: $requestUrl")
            Log.d(TAG, "Stock code: $code, Licence: $licence")

            val response = apiService.getBrokerRealTimeData(code, licence)

            // 打印响应信息
            Log.d(TAG, "Response code: ${response.code()}")
            Log.d(TAG, "Response message: ${response.message()}")

            if (response.isSuccessful && response.body() != null) {
                val data = response.body()!!
                Log.d(TAG, "Broker real-time data fetched successfully: price=${data.price}, changePercent=${data.changePercent}%")
                Result.success(data)
            } else {
                val errorMsg = "Failed to fetch broker real-time data: ${response.code()}"
                Log.e(TAG, errorMsg)
                Log.e(TAG, "Request URL was: $requestUrl")
                // 读取错误响应体
                val errorBody = response.errorBody()?.string()
                if (errorBody != null) {
                    Log.e(TAG, "Error response body: $errorBody")
                }
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching broker real-time data", e)
            Result.failure(e)
        }
    }

    /**
     * 获取历史分时数据
     * @param code 股票代码（如 000001）
     * @param market 市场（如 SZ 或 SH）
     * @param period 周期：d(日), w(周), m(月), y(年)
     * @param limit 限制条数
     */
    suspend fun getHistoryData(
        code: String,
        period: String,
        limit: Int? = null
    ): Result<List<TimeSeriesData>> {
        return try {
            val adjust = "n" // 不复权

            // 计算时间范围
            val (startTime, endTime) = when (period) {
                "d" -> {
                    // 日线：最近30天
                    val calendar = Calendar.getInstance()
                    val end = formatDate(calendar.time)
                    calendar.add(Calendar.DAY_OF_YEAR, -30)
                    val start = formatDate(calendar.time)
                    Log.d(TAG, "Day period: limiting to last 30 days")
                    Pair(start, end)
                }
                "m" -> {
                    // 月线：最近一年
                    val calendar = Calendar.getInstance()
                    val end = formatDate(calendar.time)
                    calendar.add(Calendar.YEAR, -1)
                    val start = formatDate(calendar.time)
                    Pair(start, end)
                }
                "y" -> {
                    // 年线：全部历史
                    Pair(null, null)
                }
                else -> Pair(null, null)
            }

            // 构建完整请求 URL
            val urlBuilder = StringBuilder("${StockNetworkModule.BASE_URL}hsstock/history/$code/$period/$adjust/$licence")
            val queryParams = mutableListOf<String>()
            startTime?.let { queryParams.add("st=$it") }
            endTime?.let { queryParams.add("et=$it") }
            limit?.let { queryParams.add("lt=$it") }
            if (queryParams.isNotEmpty()) {
                urlBuilder.append("?").append(queryParams.joinToString("&"))
            }
            val requestUrl = urlBuilder.toString()

            Log.d(TAG, "Fetching history data: code=$code, period=$period, start=$startTime, end=$endTime")
            Log.d(TAG, "Request URL: $requestUrl")

            val response = apiService.getHistoryTimeSeries(
                codeMarket = code,
                period = period,
                adjust = adjust,
                licence = licence,
                startTime = startTime,
                endTime = endTime,
                limit = limit
            )

            // 打印响应信息
            Log.d(TAG, "Response code: ${response.code()}")
            Log.d(TAG, "Response message: ${response.message()}")

            if (response.isSuccessful && response.body() != null) {
                val data = response.body()!!
                Log.d(TAG, "History data fetched successfully: ${data.size} records")
                Result.success(data)
            } else {
                val errorMsg = "Failed to fetch history data: ${response.code()}"
                Log.e(TAG, errorMsg)
                Log.e(TAG, "Request URL was: $requestUrl")
                if (response.errorBody() != null) {
                    Log.e(TAG, "Error body: ${response.errorBody()?.string()}")
                }
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching history data", e)
            Result.failure(e)
        }
    }

    /**
     * 获取/缓存股票列表
     */
    suspend fun getStockList(): Result<List<StockListItem>> {
        cachedStockList?.let { return Result.success(it) }
        return try {
            val requestUrl = "${StockNetworkModule.BASE_URL}hslt/list/$licence"
            Log.d(TAG, "Fetching stock list. Request URL: $requestUrl")
            val response = apiService.getStockList(licence)
            if (response.isSuccessful && response.body() != null) {
                val list = response.body()!!.filter { !it.dm.isNullOrBlank() && !it.mc.isNullOrBlank() }
                cachedStockList = list
                Log.d(TAG, "Stock list fetched: ${list.size} items")
                Result.success(list)
            } else {
                val errorMsg = "Failed to fetch stock list: ${response.code()}"
                Log.e(TAG, errorMsg)
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching stock list", e)
            Result.failure(e)
        }
    }

    /**
     * 根据关键词搜索股票代码/名称
     */
    suspend fun searchStocks(keyword: String): Result<List<StockListItem>> {
        val key = keyword.lowercase()
        return getStockList().mapCatching { list ->
            list.filter { item ->
                val code = item.dm.orEmpty().lowercase()
                val name = item.mc.orEmpty().lowercase()
                code.contains(key) || name.contains(key)
            }.take(20) // 限制返回数量，避免 UI 卡顿
        }
    }

    /**
     * 获取公司简介（取第一条）
     */
    suspend fun getCompanyInfo(code: String): Result<CompanyInfo?> {
        return try {
            val requestUrl = "${StockNetworkModule.BASE_URL}hscp/gsjj/$code/$licence"
            Log.d(TAG, "Fetching company info, url: $requestUrl")
            val response = apiService.getCompanyInfo(code, licence)
            if (response.isSuccessful && response.body() != null) {
                val info = response.body()!!
                Result.success(info)
            } else {
                val errorMsg = "Failed to fetch company info: ${response.code()}"
                Log.e(TAG, errorMsg)
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching company info", e)
            Result.failure(e)
        }
    }

    /**
     * 获取业绩预告
     */
    suspend fun getPerformanceForecast(code: String): Result<List<PerformanceForecast>> {
        return try {
            val requestUrl = "${StockNetworkModule.BASE_URL}hscp/yjyg/$code/$licence"
            Log.d(TAG, "Fetching performance forecast, url: $requestUrl")
            val response = apiService.getPerformanceForecast(code, licence)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorMsg = "Failed to fetch performance forecast: ${response.code()}"
                Log.e(TAG, errorMsg)
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching performance forecast", e)
            Result.failure(e)
        }
    }

    /**
     * 获取财务指标
     */
    suspend fun getFinancialIndicators(code: String): Result<List<FinancialIndicator>> {
        return try {
            val requestUrl = "${StockNetworkModule.BASE_URL}hscp/cwzb/$code/$licence"
            Log.d(TAG, "Fetching financial indicators, url: $requestUrl")
            val response = apiService.getFinancialIndicators(code, licence)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorMsg = "Failed to fetch financial indicators: ${response.code()}"
                Log.e(TAG, errorMsg)
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching financial indicators", e)
            Result.failure(e)
        }
    }

    /**
     * 格式化日期为 YYYYMMDD
     */
    private fun formatDate(date: java.util.Date): String {
        val format = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        return format.format(date)
    }
}
