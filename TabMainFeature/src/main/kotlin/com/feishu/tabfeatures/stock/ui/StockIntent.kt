package com.feishu.tabfeatures.stock.ui

import com.feishu.tabfeatures.stock.data.api.StockListItem


/**
 * 股票 Tab 的用户意图/事件
 * 采用密封类设计，便于扩展和类型安全
 */
sealed class StockIntent {
    /**
     * 输入股票代码
     */
    data class InputStockCode(val code: String) : StockIntent()

    /**
     * 选择联想候选
     */
    data class SelectSuggestion(val item: StockListItem) : StockIntent()

    /**
     * 清除联想列表
     */
    object ClearSuggestions : StockIntent()

    /**
     * 查询公司信息
     */
    object QueryCompanyInfo : StockIntent()

    /**
     * 查询实时数据
     */
    object QueryRealTimeData : StockIntent()

    /**
     * 查询历史数据
     */
    object QueryHistoryData : StockIntent()

    /**
     * 切换历史数据周期
     */
    data class SwitchHistoryPeriod(val period: HistoryPeriod) : StockIntent()

    /**
     * 切换数据视图（实时/历史）
     */
    data class SwitchViewMode(val mode: ViewMode) : StockIntent()

    /**
     * 清除错误
     */
    object ClearError : StockIntent()

    /**
     * 停止自动刷新
     */
    object StopAutoRefresh : StockIntent()
}

/**
 * 历史数据周期
 */
enum class HistoryPeriod(val displayName: String, val apiValue: String) {
    DAY("日线", "d"),
    MONTH("月线", "m"),
    YEAR("年线", "y")
}

/**
 * 视图模式
 */
enum class ViewMode {
    REAL_TIME,  // 实时数据
    HISTORY     // 历史数据
}

