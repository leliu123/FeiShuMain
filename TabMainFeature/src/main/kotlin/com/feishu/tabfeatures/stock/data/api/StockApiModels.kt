package com.feishu.tabfeatures.stock.data.api
import com.google.gson.annotations.SerializedName
/**
 * 实时交易数据（网络数据源）响应模型
 */
data class RealTimeStockData(
    @SerializedName("fm") val fiveMinChange: Double? = null,      // 五分钟涨跌幅（%）
    @SerializedName("h") val high: Double? = null,                 // 最高价（元）
    @SerializedName("hs") val turnover: Double? = null,           // 换手（%）
    @SerializedName("lb") val volumeRatio: Double? = null,         // 量比（%）
    @SerializedName("l") val low: Double? = null,                  // 最低价（元）
    @SerializedName("lt") val circulatingMarketValue: Double? = null, // 流通市值（元）
    @SerializedName("o") val open: Double? = null,                 // 开盘价（元）
    @SerializedName("pe") val peRatio: Double? = null,             // 市盈率
    @SerializedName("pc") val changePercent: Double? = null,       // 涨跌幅（%）
    @SerializedName("p") val price: Double? = null,               // 当前价格（元）
    @SerializedName("sz") val totalMarketValue: Double? = null,     // 总市值（元）
    @SerializedName("cje") val turnoverAmount: Double? = null,     // 成交额（元）
    @SerializedName("ud") val changeAmount: Double? = null,         // 涨跌额（元）
    @SerializedName("v") val volume: Double? = null,                // 成交量（手）
    @SerializedName("yc") val previousClose: Double? = null,       // 昨日收盘价（元）
    @SerializedName("zf") val amplitude: Double? = null,           // 振幅（%）
    @SerializedName("zs") val changeSpeed: Double? = null,          // 涨速（%）
    @SerializedName("sjl") val pbRatio: Double? = null,             // 市净率
    @SerializedName("zdf60") val changePercent60: Double? = null,   // 60日涨跌幅（%）
    @SerializedName("zdfnc") val changePercentYear: Double? = null, // 年初至今涨跌幅（%）
    @SerializedName("t") val updateTime: String? = null             // 更新时间
)

/**
 * 实时交易数据（券商数据源）响应模型
 */
data class BrokerRealTimeData(
    @SerializedName("p") val price: Double? = null,               // 最新价
    @SerializedName("o") val open: Double? = null,                 // 开盘价
    @SerializedName("h") val high: Double? = null,                 // 最高价
    @SerializedName("l") val low: Double? = null,                 // 最低价
    @SerializedName("yc") val previousClose: Double? = null,       // 前收盘价
    @SerializedName("cje") val turnoverAmount: Double? = null,    // 成交总额
    @SerializedName("v") val volume: Double? = null,               // 成交总量
    @SerializedName("pv") val originalVolume: Double? = null,      // 原始成交总量
    @SerializedName("ud") val changeAmount: Double? = null,        // 涨跌额
    @SerializedName("pc") val changePercent: Double? = null,       // 涨跌幅
    @SerializedName("zf") val amplitude: Double? = null,           // 振幅
    @SerializedName("t") val updateTime: String? = null,            // 更新时间
    @SerializedName("pe") val peRatio: Double? = null,             // 市盈率
    @SerializedName("tr") val turnoverRate: Double? = null,        // 换手率
    @SerializedName("pb_ratio") val pbRatio: Double? = null,       // 市净率
    @SerializedName("tv") val tradingVolume: Double? = null         // 成交量
)

/**
 * 当天逐笔交易数据响应模型
 */
data class TickData(
    @SerializedName("d") val date: String? = null,                 // 数据归属日期（yyyy-MM-dd）
    @SerializedName("t") val time: String? = null,                 // 时间（HH:mm:dd）
    @SerializedName("v") val volume: Double? = null,               // 成交量（股）
    @SerializedName("p") val price: Double? = null,                // 成交价
    @SerializedName("ts") val tradeSide: Int? = null              // 交易方向（0：中性盘，1：买入，2：卖出）
)

/**
 * 分时交易数据响应模型（最新/历史）
 */
data class TimeSeriesData(
    @SerializedName("t") val time: String? = null,                 // 交易时间
    @SerializedName("o") val open: Double? = null,                 // 开盘价
    @SerializedName("h") val high: Double? = null,                 // 最高价
    @SerializedName("l") val low: Double? = null,                   // 最低价
    @SerializedName("c") val close: Double? = null,                // 收盘价
    @SerializedName("v") val volume: Double? = null,               // 成交量
    @SerializedName("a") val amount: Double? = null,               // 成交额
    @SerializedName("pc") val previousClose: Double? = null,       // 前收盘价
    @SerializedName("sf") val suspended: Int? = null              // 停牌 1停牌，0 不停牌
)