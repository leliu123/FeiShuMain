package com.feishu.tabfeatures.stock.ui
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.feishu.tabfeatures.stock.data.api.StockListItem
import com.feishu.tabfeatures.stock.data.repository.StockRepository
import java.util.Locale
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


/**
 * 股票 Tab 的 ViewModel - 采用 MVI 架构
 */
class StockViewModel(
    private val repository: StockRepository = StockRepository()
) : ViewModel() {

    private val TAG = "StockViewModel"

    private val _state = MutableStateFlow(StockState())
    val state: StateFlow<StockState> = _state.asStateFlow()

    private var autoRefreshJob: Job? = null
    private var suggestJob: Job? = null

    /**
     * 处理用户意图
     */
    fun handleIntent(intent: StockIntent) {
        viewModelScope.launch {
            when (intent) {
                is StockIntent.InputStockCode -> {
                    inputStockCode(intent.code)
                }
                is StockIntent.SelectSuggestion -> {
                    selectSuggestion(intent.item)
                }
                is StockIntent.ClearSuggestions -> {
                    clearSuggestions()
                }
                is StockIntent.QueryRealTimeData -> {
                    queryRealTimeData()
                }
                is StockIntent.QueryHistoryData -> {
                    queryHistoryData()
                }
                is StockIntent.QueryCompanyInfo -> {
                    queryCompanyInfo()
                }
                is StockIntent.SwitchHistoryPeriod -> {
                    switchHistoryPeriod(intent.period)
                }
                is StockIntent.SwitchViewMode -> {
                    switchViewMode(intent.mode)
                }
                is StockIntent.ClearError -> {
                    clearError()
                }
                is StockIntent.StopAutoRefresh -> {
                    stopAutoRefresh()
                }
            }
        }
    }

    /**
     * 输入股票代码
     */
    private fun inputStockCode(code: String) {
        Log.d(TAG, "Input stock code: $code")
        _state.update { it.copy(stockCode = code, isLoadingSuggestions = true) }
        // 防抖获取联想
        suggestJob?.cancel()
        if (code.isBlank()) {
            _state.update { it.copy(suggestions = emptyList(), isLoadingSuggestions = false) }
            return
        }
        suggestJob = viewModelScope.launch {
            delay(250)
            repository.searchStocks(code).fold(
                onSuccess = { list ->
                    _state.update { it.copy(suggestions = list, isLoadingSuggestions = false) }
                },
                onFailure = { e ->
                    Log.e(TAG, "search suggestions failed", e)
                    _state.update { it.copy(suggestions = emptyList(), isLoadingSuggestions = false) }
                }
            )
        }
    }

    /**
     * 选择联想项：填充代码与市场，并触发查询
     */
    private fun selectSuggestion(item: StockListItem) {
        val code = sanitizeCode(item.dm.orEmpty())
        Log.d(TAG, "Select suggestion: $code")
        _state.update {
            it.copy(
                stockCode = code,
                suggestions = emptyList(),
                isLoadingSuggestions = false
            )
        }
        handleIntent(StockIntent.QueryRealTimeData)
        handleIntent(StockIntent.QueryHistoryData)
        handleIntent(StockIntent.QueryCompanyInfo)
    }

    private fun clearSuggestions() {
        _state.update { it.copy(suggestions = emptyList(), isLoadingSuggestions = false) }
    }

    /**
     * 查询实时数据
     */
    private suspend fun queryRealTimeData() {
        val code = _state.value.stockCode
        val cleanCode = sanitizeCode(code)
        if (code.isEmpty()) {
            Log.w(TAG, "Stock code is empty, cannot query real-time data")
            _state.update { it.copy(error = "请输入股票代码") }
            return
        }

        Log.d(TAG, "Querying real-time data for code: $cleanCode")
        _state.update { it.copy(isLoadingRealTime = true, error = null) }

        repository.getRealTimeData(cleanCode).fold(
            onSuccess = { data ->
                Log.d(TAG, "Real-time data received: ${data?.price}")
                _state.update {
                    it.copy(
                        realTimeData = data,
                        isLoadingRealTime = false,
                        lastUpdateTime = data?.updateTime
                    )
                }
                // 如果当前在实时数据视图，启动自动刷新
                if (_state.value.viewMode == ViewMode.REAL_TIME) {
                    startAutoRefresh()
                }
                // 自动补充公司信息
                handleIntent(StockIntent.QueryCompanyInfo)
            },
            onFailure = { error ->
                Log.e(TAG, "Failed to fetch real-time data", error)
                _state.update {
                    it.copy(
                        isLoadingRealTime = false,
                        error = error.message ?: "获取实时数据失败"
                    )
                }
            }
        )
    }

    /**
     * 查询历史数据
     */
    private suspend fun queryHistoryData() {
        val code = _state.value.stockCode
        val cleanCode = sanitizeCode(code)
        if (code.isEmpty()) {
            Log.w(TAG, "Stock code is empty, cannot query history data")
            _state.update { it.copy(error = "请输入股票代码") }
            return
        }

        val period = _state.value.currentPeriod
        Log.d(TAG, "Querying history data for code: $cleanCode, period: ${period.apiValue}")
        _state.update { it.copy(isLoadingHistory = true, error = null) }

        repository.getHistoryData(cleanCode, period = period.apiValue).fold(
            onSuccess = { data ->
                Log.d(TAG, "History data received: ${data.size} records")
                _state.update {
                    it.copy(
                        historyData = data,
                        isLoadingHistory = false
                    )
                }
            },
            onFailure = { error ->
                Log.e(TAG, "Failed to fetch history data", error)
                _state.update {
                    it.copy(
                        isLoadingHistory = false,
                        error = error.message ?: "获取历史数据失败"
                    )
                }
            }
        )
    }

    /**
     * 切换历史数据周期
     */
    private fun switchHistoryPeriod(period: HistoryPeriod) {
        Log.d(TAG, "Switching history period to: ${period.displayName}")
        _state.update { it.copy(currentPeriod = period) }
        // 自动重新查询历史数据
        handleIntent(StockIntent.QueryHistoryData)
    }

    /**
     * 切换视图模式
     */
    private fun switchViewMode(mode: ViewMode) {
        Log.d(TAG, "Switching view mode to: $mode")
        _state.update { it.copy(viewMode = mode) }

        // 切换到历史数据视图时，自动查询历史数据
        if (mode == ViewMode.HISTORY && _state.value.historyData.isEmpty()) {
            handleIntent(StockIntent.QueryHistoryData)
        }
    }

    /**
     * 查询公司信息
     */
    private suspend fun queryCompanyInfo() {
        val code = _state.value.stockCode
        val cleanCode = sanitizeCode(code)
        if (cleanCode.isEmpty()) return
        _state.update { it.copy(isLoadingCompany = true) }
        repository.getCompanyInfo(cleanCode).fold(
            onSuccess = { info ->
                _state.update { it.copy(companyInfo = info, isLoadingCompany = false) }
            },
            onFailure = { e ->
                Log.e(TAG, "Failed to fetch company info", e)
                _state.update { it.copy(isLoadingCompany = false) }
            }
        )
        // 并行拉取业绩预告和财务指标
        viewModelScope.launch {
            repository.getPerformanceForecast(cleanCode).onSuccess { list ->
                _state.update { it.copy(performanceForecasts = list) }
            }.onFailure { e -> Log.e(TAG, "Failed to fetch performance forecast", e) }
        }
        viewModelScope.launch {
            repository.getFinancialIndicators(cleanCode).onSuccess { list ->
                _state.update { it.copy(financialIndicators = list) }
            }.onFailure { e -> Log.e(TAG, "Failed to fetch financial indicators", e) }
        }
    }

    private fun sanitizeCode(raw: String): String {
        return raw.trim().uppercase(Locale.getDefault()).substringBefore(".")
    }

    /**
     * 清除错误
     */
    private fun clearError() {
        _state.update { it.copy(error = null) }
    }

    /**
     * 开始自动刷新实时数据
     */
    fun startAutoRefresh() {
        if (autoRefreshJob?.isActive == true) {
            Log.d(TAG, "Auto refresh already running")
            return
        }

        val code = _state.value.stockCode
        if (code.isEmpty()) {
            Log.w(TAG, "Cannot start auto refresh: stock code is empty")
            return
        }

        Log.d(TAG, "Starting auto refresh for code: $code")
        _state.update { it.copy(isAutoRefreshing = true) }

        autoRefreshJob = viewModelScope.launch {
            while (true) {
                delay(60_000) // 每分钟刷新一次
                if (_state.value.viewMode == ViewMode.REAL_TIME) {
                    Log.d(TAG, "Auto refreshing real-time data")
                    queryRealTimeData()
                }
            }
        }
    }

    /**
     * 停止自动刷新
     */
    private fun stopAutoRefresh() {
        Log.d(TAG, "Stopping auto refresh")
        autoRefreshJob?.cancel()
        autoRefreshJob = null
        _state.update { it.copy(isAutoRefreshing = false) }
    }

    override fun onCleared() {
        super.onCleared()
        stopAutoRefresh()
        Log.d(TAG, "ViewModel cleared")
    }
}

