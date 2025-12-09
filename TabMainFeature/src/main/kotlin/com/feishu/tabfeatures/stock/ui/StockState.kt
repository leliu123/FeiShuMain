package com.feishu.tabfeatures.stock.ui
import com.feishu.tabfeatures.stock.data.api.BrokerRealTimeData
import com.feishu.tabfeatures.stock.data.api.StockListItem
import com.feishu.tabfeatures.stock.data.api.TimeSeriesData
import com.feishu.tabfeatures.stock.data.api.CompanyInfo
import com.feishu.tabfeatures.stock.data.api.FinancialIndicator
import com.feishu.tabfeatures.stock.data.api.PerformanceForecast

data class StockState(
    /**
     * 当前输入的股票代码
     */
    val stockCode: String = "",

    /**
     * 联想搜索列表
     */
    val suggestions: List<StockListItem> = emptyList(),

    /**
     * 是否在加载联想
     */
    val isLoadingSuggestions: Boolean = false,

    /**
     * 实时数据
     */
    val realTimeData: BrokerRealTimeData? = null,

    /**
     * 历史数据
     */
    val historyData: List<TimeSeriesData> = emptyList(),

    /**
     * 当前历史数据周期
     */
    val currentPeriod: HistoryPeriod = HistoryPeriod.DAY,

    /**
     * 当前视图模式
     */
    val viewMode: ViewMode = ViewMode.REAL_TIME,

    /**
     * 是否正在加载实时数据
     */
    val isLoadingRealTime: Boolean = false,

    /**
     * 是否正在加载历史数据
     */
    val isLoadingHistory: Boolean = false,

    /**
     * 是否正在自动刷新
     */
    val isAutoRefreshing: Boolean = false,

    /**
     * 错误信息
     */
    val error: String? = null,

    /**
     * 公司信息
     */
    val companyInfo: CompanyInfo? = null,

    /**
     * 业绩预告
     */
    val performanceForecasts: List<PerformanceForecast> = emptyList(),

    /**
     * 财务指标
     */
    val financialIndicators: List<FinancialIndicator> = emptyList(),

    /**
     * 公司信息加载中
     */
    val isLoadingCompany: Boolean = false,

    /**
     * 最后更新时间
     */
    val lastUpdateTime: String? = null
) {
    /**
     * 是否有错误
     */
    val hasError: Boolean
        get() = error != null

    /**
     * 是否正在加载
     */
    val isLoading: Boolean
        get() = isLoadingRealTime || isLoadingHistory
}

