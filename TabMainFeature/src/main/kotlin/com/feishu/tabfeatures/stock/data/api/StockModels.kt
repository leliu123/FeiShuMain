package com.feishu.tabfeatures.stock.data.api

/**
 * 股票列表条目
 * @param dm 股票代码
 * @param mc 股票名称
 * @param jys 交易所 "sh"/"sz"
 */
data class StockListItem(
    val dm: String?,
    val mc: String?,
    val jys: String?
)

/**
 * 公司简介核心字段（常用）
 */
data class CompanyInfo(
    val name: String? = null,
    val ename: String? = null,
    val market: String? = null,
    val idea: String? = null,
    val ldate: String? = null,
    val sprice: String? = null,
    val organ: String? = null,
    val phone: String? = null,
    val site: String? = null,
    val addr: String? = null,
    val oaddr: String? = null,
    val desc: String? = null
)

/**
 * 业绩预告
 */
data class PerformanceForecast(
    val pdate: String? = null, // 公告日期
    val rdate: String? = null, // 报告期
    val type: String? = null,  // 类型
    val abs: String? = null,   // 摘要
    val old: String? = null    // 上年同期每股收益
)

/**
 * 财务指标（近四个季度）
 */
data class FinancialIndicator(
    val date: String? = null,   // 报告日期
    val tbmg: String? = null,   // 摊薄每股收益
    val jqmg: String? = null,   // 加权每股收益
    val mgsy: String? = null,   // 每股收益_调整后
    val mgjz: String? = null,   // 每股净资产_调整前
    val mgjy: String? = null,   // 每股经营性现金流
    val mgwly: String? = null,  // 每股未分配利润
    val zylr: String? = null,   // 主营业务利润率
    val zzzzl: String? = null,  // 总资产周转率
    val ldbl: String? = null,   // 流动比率
    val sdbl: String? = null,   // 速动比率
    val xjbl: String? = null    // 现金比率
)

