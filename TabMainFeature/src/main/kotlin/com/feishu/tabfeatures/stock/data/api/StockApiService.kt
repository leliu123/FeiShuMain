package com.feishu.tabfeatures.stock.data.api


import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * 股票数据 API 服务接口
 */
interface StockApiService {

    /**
     * 获取实时交易数据（网络数据源）
     * API: https://api.mairuiapi.com/hsrl/ssjy/{code}/{licence}
     */
    @GET("hsrl/ssjy/{code}/{licence}")
    suspend fun getRealTimeData(
        @Path("code") code: String,
        @Path("licence") licence: String
    ): Response<List<RealTimeStockData>>

    /**
     * 获取实时交易数据（券商数据源）
     * API: https://api.mairuiapi.com/hsstock/real/time/{code}/{licence}
     * 注意：此 API 返回单个对象，不是数组
     */
    @GET("hsstock/real/time/{code}/{licence}")
    suspend fun getBrokerRealTimeData(
        @Path("code") code: String,
        @Path("licence") licence: String
    ): Response<BrokerRealTimeData>

    /**
     * 获取当天逐笔交易数据
     * API: https://api.mairuiapi.com/hsrl/zbjy/{code}/{licence}
     */
    @GET("hsrl/zbjy/{code}/{licence}")
    suspend fun getTickData(
        @Path("code") code: String,
        @Path("licence") licence: String
    ): Response<List<TickData>>

    /**
     * 获取最新分时交易数据
     * API: https://api.mairuiapi.com/hsstock/latest/{code.market}/{period}/{adjust}/{licence}?lt={limit}
     */
    @GET("hsstock/latest/{codeMarket}/{period}/{adjust}/{licence}")
    suspend fun getLatestTimeSeries(
        @Path("codeMarket") codeMarket: String,  // 如 000001.SZ
        @Path("period") period: String,          // 分时级别: 5, 15, 30, 60, d, w, m, y
        @Path("adjust") adjust: String,           // 除权方式: n, f, b, fr, br
        @Path("licence") licence: String,
        @Query("lt") limit: Int? = null          // 最新条数
    ): Response<List<TimeSeriesData>>

    /**
     * 获取历史分时交易数据
     * API: https://api.mairuiapi.com/hsstock/history/{code.market}/{period}/{adjust}/{licence}?st={start}&et={end}&lt={limit}
     */
    @GET("hsstock/history/{codeMarket}/{period}/{adjust}/{licence}")
    suspend fun getHistoryTimeSeries(
        @Path("codeMarket") codeMarket: String,   // 如 000001.SZ
        @Path("period") period: String,           // 分时级别: 5, 15, 30, 60, d, w, m, y
        @Path("adjust") adjust: String,           // 除权方式: n, f, b, fr, br
        @Path("licence") licence: String,
        @Query("st") startTime: String? = null,  // 开始时间 YYYYMMDD 或 YYYYMMDDhhmmss
        @Query("et") endTime: String? = null,   // 结束时间 YYYYMMDD 或 YYYYMMDDhhmmss
        @Query("lt") limit: Int? = null          // 最新条数
    ): Response<List<TimeSeriesData>>

    /**
     * 获取股票列表（代码+名称）
     * API: https://api.mairuiapi.com/hslt/list/{licence}
     */
    @GET("hslt/list/{licence}")
    suspend fun getStockList(
        @Path("licence") licence: String
    ): Response<List<StockListItem>>

    /**
     * 获取公司简介
     * API: https://api.mairuiapi.com/hscp/gsjj/{code}/{licence}
     * 返回数组，取第一条
     */
    @GET("hscp/gsjj/{code}/{licence}")
    suspend fun getCompanyInfo(
        @Path("code") code: String,
        @Path("licence") licence: String
    ): Response<CompanyInfo>

    /**
     * 业绩预告
     * API: https://api.mairuiapi.com/hscp/yjyg/{code}/{licence}
     */
    @GET("hscp/yjyg/{code}/{licence}")
    suspend fun getPerformanceForecast(
        @Path("code") code: String,
        @Path("licence") licence: String
    ): Response<List<PerformanceForecast>>

    /**
     * 财务指标
     * API: https://api.mairuiapi.com/hscp/cwzb/{code}/{licence}
     */
    @GET("hscp/cwzb/{code}/{licence}")
    suspend fun getFinancialIndicators(
        @Path("code") code: String,
        @Path("licence") licence: String
    ): Response<List<FinancialIndicator>>
}

