package com.feishu.tabfeatures.stock.ui

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.feishu.tabfeatures.stock.ui.chart.KLineChart
import com.feishu.tabfeatures.stock.data.api.StockListItem
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalConfiguration
import com.feishu.tabfeatures.stock.data.api.BrokerRealTimeData
import com.feishu.tabfeatures.stock.data.api.CompanyInfo
import com.feishu.tabfeatures.stock.data.api.FinancialIndicator
import com.feishu.tabfeatures.stock.data.api.PerformanceForecast
import com.feishu.tabfeatures.stock.data.api.TimeSeriesData
import java.text.DecimalFormat

private const val TAG = "StockScreen"

/**
 * 股票 Tab 的主界面
 */
@Composable
fun StockScreen(
    viewModel: StockViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val focusManager = LocalFocusManager.current
    val suggestionsVisible = state.suggestions.isNotEmpty()

    // 启动自动刷新
    LaunchedEffect(state.stockCode, state.viewMode) {
        if (state.stockCode.isNotEmpty() && state.viewMode == ViewMode.REAL_TIME) {
            Log.d(TAG, "Starting auto refresh for stock: ${state.stockCode}")
            viewModel.startAutoRefresh()
        } else {
            Log.d(TAG, "Stopping auto refresh")
            viewModel.handleIntent(StockIntent.StopAutoRefresh)
        }
    }

    // 处理错误显示
    LaunchedEffect(state.error) {
        state.error?.let { error ->
            Log.e(TAG, "Error occurred: $error")
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(suggestionsVisible) {
                detectTapGestures {
                    focusManager.clearFocus()
                    viewModel.handleIntent(StockIntent.ClearSuggestions)
                }
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // 股票代码输入框
            StockCodeInput(
                code = state.stockCode,
                suggestions = state.suggestions,
                isLoadingSuggestions = state.isLoadingSuggestions,
                onCodeChange = { code ->
                    viewModel.handleIntent(StockIntent.InputStockCode(code))
                },
                onSuggestionClick = { item ->
                    viewModel.handleIntent(StockIntent.SelectSuggestion(item))
                    focusManager.clearFocus()
                },
                onSearch = {
                    if (state.viewMode == ViewMode.REAL_TIME) {
                        viewModel.handleIntent(StockIntent.QueryRealTimeData)
                    } else {
                        viewModel.handleIntent(StockIntent.QueryHistoryData)
                    }
                    viewModel.handleIntent(StockIntent.QueryCompanyInfo)
                    focusManager.clearFocus()
                    viewModel.handleIntent(StockIntent.ClearSuggestions)
                }
            )

            if (state.companyInfo != null || state.isLoadingCompany) {
                Spacer(modifier = Modifier.height(8.dp))
                CompanyButton(
                    info = state.companyInfo,
                    performance = state.performanceForecasts,
                    indicators = state.financialIndicators,
                    isLoading = state.isLoadingCompany
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 视图模式切换
            ViewModeSelector(
                currentMode = state.viewMode,
                onModeChange = { mode ->
                    viewModel.handleIntent(StockIntent.SwitchViewMode(mode))
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 根据视图模式显示不同内容
            when (state.viewMode) {
                ViewMode.REAL_TIME -> {
                    RealTimeDataView(
                        data = state.realTimeData,
                        isLoading = state.isLoadingRealTime,
                        isAutoRefreshing = state.isAutoRefreshing,
                        lastUpdateTime = state.lastUpdateTime,
                        onRefresh = {
                            viewModel.handleIntent(StockIntent.QueryRealTimeData)
                        }
                    )
                }
                ViewMode.HISTORY -> {
                    HistoryDataView(
                        data = state.historyData,
                        currentPeriod = state.currentPeriod,
                        isLoading = state.isLoadingHistory,
                        onPeriodChange = { period ->
                            viewModel.handleIntent(StockIntent.SwitchHistoryPeriod(period))
                        },
                        onRefresh = {
                            viewModel.handleIntent(StockIntent.QueryHistoryData)
                        }
                    )
                }
            }
        }
    }
}

/**
 * 股票代码输入框
 */
@Composable
private fun StockCodeInput(
    code: String,
    suggestions: List<StockListItem>,
    isLoadingSuggestions: Boolean,
    onCodeChange: (String) -> Unit,
    onSuggestionClick: (StockListItem) -> Unit,
    onSearch: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        val screenHeightHalf = LocalConfiguration.current.screenHeightDp.dp / 2
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = code,
                onValueChange = onCodeChange,
                modifier = Modifier.weight(1f),
                label = { Text("股票代码/公司名称") },
                placeholder = { Text("如：000001 或 平安银行") },
                singleLine = true
            )

            Button(
                onClick = onSearch,
                modifier = Modifier.height(56.dp)
            ) {
                Icon(Icons.Default.Search, contentDescription = "查询")
                Spacer(modifier = Modifier.width(4.dp))
                Text("查询")
            }
        }

        if (isLoadingSuggestions) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
            )
        }

        AnimatedVisibility(
            visible = suggestions.isNotEmpty(),
            enter = expandVertically(
                expandFrom = Alignment.Top,
                animationSpec = tween(durationMillis = 450)
            ) + fadeIn(tween(450)),
            exit = shrinkVertically(
                shrinkTowards = Alignment.Top,
                animationSpec = tween(durationMillis = 350)
            ) + fadeOut(tween(350))
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = screenHeightHalf)
                    .animateContentSize(),

                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    itemsIndexed(suggestions) { idx, item ->
                        val codeText = item.dm.orEmpty().substringBefore(".")
                        val nameText = item.mc.orEmpty()
                        TextButton(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            onClick = { onSuggestionClick(item) }
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = "$nameText ($codeText)",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                        if (idx < suggestions.lastIndex) {
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 8.dp),
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 视图模式选择器
 */
@Composable
private fun ViewModeSelector(
    currentMode: ViewMode,
    onModeChange: (ViewMode) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = currentMode == ViewMode.REAL_TIME,
            onClick = { onModeChange(ViewMode.REAL_TIME) },
            label = { Text("实时数据") }
        )

        FilterChip(
            selected = currentMode == ViewMode.HISTORY,
            onClick = { onModeChange(ViewMode.HISTORY) },
            label = { Text("历史数据") }
        )
    }
}

/**
 * 公司信息按钮与弹窗
 */
@Composable
private fun CompanyButton(
    info: CompanyInfo?,
    performance: List<PerformanceForecast>,
    indicators: List<FinancialIndicator>,
    isLoading: Boolean
) {
    var showDialog by remember { mutableStateOf(false) }
    if (info == null) return
    Button(
        onClick = { showDialog = true },
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Text(text = info.name ?: "公司信息", color = MaterialTheme.colorScheme.onSecondaryContainer)
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("关闭")
                }
            },
            title = {
                Text(text = info.name ?: "公司信息")
            },
            text = {
                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                    ) {
                        SectionHeader("基础信息")
                        InfoRow("上市市场", info.market)
                        InfoRow("英文名", info.ename)
                        InfoRow("上市日期", info.ldate)
                        InfoRow("发行价", info.sprice)
                        InfoRow("概念", info.idea)
                        InfoRow("机构类型", info.organ)
                        InfoRow("电话", info.phone)
                        InfoRow("网站", info.site)
                        InfoRow("注册地址", info.addr)
                        InfoRow("办公地址", info.oaddr)
                        InfoRow("简介", info.desc)

                        Spacer(modifier = Modifier.height(12.dp))
                        SectionHeader("近年业绩预告")
                        if (performance.isEmpty()) {
                            InfoRow("提示", "暂无数据")
                        } else {
                            performance.take(10).forEach { pf ->
                                InfoRow("公告日期", pf.pdate)
                                InfoRow("报告期", pf.rdate)
                                InfoRow("类型", pf.type)
                                InfoRow("摘要", pf.abs)
                                InfoRow("上年同期每股收益", pf.old)
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        SectionHeader("财务指标（近四个季度）")
                        if (indicators.isEmpty()) {
                            InfoRow("提示", "暂无数据")
                        } else {
                            indicators.take(10).forEach { fi ->
                                InfoRow("报告日期", fi.date)
                                InfoRow("摊薄每股收益", fi.tbmg)
                                InfoRow("加权每股收益", fi.jqmg)
                                InfoRow("每股收益_调整后", fi.mgsy)
                                InfoRow("每股净资产_调整前", fi.mgjz)
                                InfoRow("每股经营性现金流", fi.mgjy)
                                InfoRow("每股未分配利润", fi.mgwly)
                                InfoRow("主营业务利润率", fi.zylr)
                                InfoRow("总资产周转率", fi.zzzzl)
                                InfoRow("流动比率", fi.ldbl)
                                InfoRow("速动比率", fi.sdbl)
                                InfoRow("现金比率", fi.xjbl)
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }
        )
    }
}

@Composable
private fun InfoRow(label: String, value: String?) {
    if (value.isNullOrBlank()) return
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun SectionHeader(title: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
    }
}

/**
 * 实时数据视图
 */
@Composable
private fun RealTimeDataView(
    data: BrokerRealTimeData?,
    isLoading: Boolean,
    isAutoRefreshing: Boolean,
    lastUpdateTime: String?,
    onRefresh: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // 标题栏
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "实时行情",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (isAutoRefreshing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    }
                    IconButton(onClick = onRefresh) {
                        Icon(Icons.Default.Refresh, contentDescription = "刷新")
                    }
                }
            }

            if (lastUpdateTime != null) {
                Text(
                    text = "更新时间: $lastUpdateTime",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading && data == null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (data == null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "暂无数据，请输入股票代码查询",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                RealTimeDataContent(data = data)
            }
        }
    }
}

/**
 * 实时数据内容
 */
@Composable
private fun RealTimeDataContent(
    data: BrokerRealTimeData
) {
    val df = DecimalFormat("#.##")
    val price = data.price ?: 0.0
    val changePercent = data.changePercent ?: 0.0
    val changeAmount = data.changeAmount ?: 0.0
    val isRising = changeAmount >= 0

    val accent = if (isRising) Color(0xFFE53E3E) else Color(0xFF38A169)

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 价格与涨跌
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = df.format(price),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = accent
                )
                Text(
                    text = "昨收 ${data.previousClose?.let { df.format(it) } ?: "--"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    color = accent.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "${if (isRising) "▲" else "▼"} ${df.format(changeAmount)} | ${df.format(changePercent)}%",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = accent,
                        fontWeight = FontWeight.Medium
                    )
                }
                Text(
                    text = "成交额 ${data.turnoverAmount?.let { "${df.format(it / 10000)}万" } ?: "--"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // 关键指标精简展示
        Spacer(modifier = Modifier.height(4.dp))
        val metrics = listOf(
            "开盘" to (data.open?.let { df.format(it) } ?: "--"),
            "最高" to (data.high?.let { df.format(it) } ?: "--"),
            "最低" to (data.low?.let { df.format(it) } ?: "--"),
            "换手率" to (data.turnoverRate?.let { "${df.format(it)}%" } ?: "--"),
            "成交量" to (data.tradingVolume?.let { "${df.format(it / 100)}手" } ?: data.volume?.let { "${df.format(it / 100)}手" } ?: "--"),
            "市盈率" to (data.peRatio?.let { df.format(it) } ?: "--")
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            metrics.chunked(2).forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    row.forEach { (k, v) ->
                        MetricChip(label = k, value = v, modifier = Modifier.weight(1f))
                    }
                    if (row.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

/**
 * 数据项
 */
@Composable
private fun MetricChip(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
        shape = RoundedCornerShape(10.dp),
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/**
 * 历史数据视图
 */
@Composable
private fun HistoryDataView(
    data: List<TimeSeriesData>,
    currentPeriod: HistoryPeriod,
    isLoading: Boolean,
    onPeriodChange: (HistoryPeriod) -> Unit,
    onRefresh: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // 标题栏
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "历史数据",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                IconButton(onClick = onRefresh) {
                    Icon(Icons.Default.Refresh, contentDescription = "刷新")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 周期选择器
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HistoryPeriod.entries.forEach { period ->
                    FilterChip(
                        selected = currentPeriod == period,
                        onClick = { onPeriodChange(period) },
                        label = { Text(period.displayName) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (isLoading && data.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (data.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "暂无数据，请输入股票代码查询",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                // K线图
                KLineChart(
                    data = data,
                    period = currentPeriod,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(340.dp)
                )
            }
        }
    }
}