package com.feishu.tabfeatures.stock.ui.chart



import android.graphics.Paint
import android.util.Log
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.android.InternalPlatformTextApi
import androidx.compose.ui.unit.dp
import com.feishu.tabfeatures.stock.data.api.TimeSeriesData
import com.feishu.tabfeatures.stock.ui.HistoryPeriod
import java.text.SimpleDateFormat
import java.util.Locale

private const val TAG = "KLineChart"

/**
 * K线图组件
 * @param data 历史数据列表
 * @param period 数据周期（用于格式化时间显示）
 * @param modifier 修饰符
 */
@Composable
fun KLineChart(
    data: List<TimeSeriesData>,
    period: HistoryPeriod,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) {
        Log.w(TAG, "Data is empty, cannot render chart")
        return
    }

    Log.d(TAG, "Rendering K-line chart with ${data.size} data points, period: ${period.displayName}")

    // 计算整体涨跌趋势（用于决定线条颜色）
    val firstPrice = data.firstOrNull()?.close ?: 0.0
    val lastPrice = data.lastOrNull()?.close ?: 0.0
    val isRising = lastPrice >= firstPrice

    // 定义颜色主题
    val lineColor = if (isRising) Color(0xFF4CAF50) else Color(0xFFF44336) // 绿色上涨，红色下跌
    val gradientStartColor = if (isRising)
        Color(0xFF4CAF50).copy(alpha = 0.2f)
    else
        Color(0xFFF44336).copy(alpha = 0.2f)
    val gradientEndColor = if (isRising)
        Color(0xFF4CAF50).copy(alpha = 0.0f)
    else
        Color(0xFFF44336).copy(alpha = 0.0f)

    // 淡入动画 - 数据变化时重新触发动画
    val dataKey = remember(data) { data.hashCode() }
    var targetAlpha by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(dataKey) {
        targetAlpha = 0f
        kotlinx.coroutines.delay(50)
        targetAlpha = 1f
    }
    val alpha by animateFloatAsState(
        targetValue = targetAlpha,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "chart_fade_in"
    )

    // 根据周期选择不同的日期格式化方式
    val dateFormatter = remember(period) {
        when (period) {
            HistoryPeriod.DAY -> SimpleDateFormat("dd", Locale.getDefault())
            HistoryPeriod.MONTH -> SimpleDateFormat("MM", Locale.getDefault())
            HistoryPeriod.YEAR -> SimpleDateFormat("yy", Locale.getDefault())
        }
    }

    // 计算价格范围 - 使用 Double 进行计算
    val prices = data.mapNotNull { it.close }
    val minPrice = prices.minOrNull() ?: 0.0
    val maxPrice = prices.maxOrNull() ?: 0.0
    val priceRange = (maxPrice - minPrice).coerceAtLeast(0.01)
    val padding = priceRange * 0.1
    val chartMin = minPrice - padding
    val chartMax = maxPrice + padding

    // 文本画笔
    val density = LocalDensity.current
    val textPaint = remember {
        Paint().apply {
            isAntiAlias = true
            textSize = with(density) { 12.dp.toPx() }
            color = android.graphics.Color.parseColor("#707070")
        }
    }

    // 预计算日期标签
    val dateLabels = remember(dataKey) {
        data.map { ts ->
            ts.time?.let { raw ->
                try {
                    val date = when {
                        raw.contains(" ") -> SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(raw)
                        raw.contains("-") -> SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(raw)
                        else -> null
                    }
                    date?.let { dateFormatter.format(it) } ?: raw.takeLast(5)
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing date: $raw", e)
                    raw.takeLast(5)
                }
            } ?: ""
        }
    }

    // 预计算点位
    val points = remember(dataKey) {
        data.map { it.close ?: 0.0 }
    }

    // 选择显示的横轴标签数量，避免过多文本导致卡顿
    val maxLabels = 6
    val labelStep = ((data.size - 1).coerceAtLeast(1) / (maxLabels - 1)).coerceAtLeast(1)

    Box(
        modifier = modifier
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(12.dp),
                spotColor = lineColor.copy(alpha = 0.2f)
            )
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                ),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 12.dp, vertical = 20.dp)
            .alpha(alpha)
    ) {
        // 渐变背景层（在图表下方）
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(gradientStartColor, gradientEndColor),
                        startY = 0f,
                        endY = Float.POSITIVE_INFINITY
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
        )

        // 图表绘制层 - 使用 Canvas，避免第三方库带来的切换卡顿
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawKLineChart(
                prices = points,
                labels = dateLabels,
                chartMin = chartMin,
                chartMax = chartMax,
                lineColor = lineColor,
                gradientStartColor = gradientStartColor,
                gradientEndColor = gradientEndColor,
                labelStep = labelStep,
                textPaint = textPaint
            )
        }
    }
}

@OptIn(InternalPlatformTextApi::class)
private fun DrawScope.drawKLineChart(
    prices: List<Double>,
    labels: List<String>,
    chartMin: Double,
    chartMax: Double,
    lineColor: Color,
    gradientStartColor: Color,
    gradientEndColor: Color,
    labelStep: Int,
    textPaint: Paint
) {
    if (prices.isEmpty() || labels.isEmpty()) return

    val paddingLeft = 56.dp.toPx()
    val paddingRight = 16.dp.toPx()
    val paddingTop = 16.dp.toPx()
    val paddingBottom = 32.dp.toPx()

    val chartWidth = size.width - paddingLeft - paddingRight
    val chartHeight = size.height - paddingTop - paddingBottom
    if (chartWidth <= 0f || chartHeight <= 0f) return

    val priceRange = (chartMax - chartMin).coerceAtLeast(0.01)
    val stepX = if (prices.size > 1) chartWidth / (prices.size - 1) else 0f

    val path = Path()
    val fillPath = Path()

    prices.forEachIndexed { index, price ->
        val normalized = ((price - chartMin) / priceRange).coerceIn(0.0, 1.0)
        val x = paddingLeft + stepX * index
        val y = paddingTop + chartHeight * (1f - normalized.toFloat())

        if (index == 0) {
            path.moveTo(x, y)
            fillPath.moveTo(x, chartHeight + paddingTop)
            fillPath.lineTo(x, y)
        } else {
            path.lineTo(x, y)
            fillPath.lineTo(x, y)
        }

        if (index == prices.lastIndex) {
            fillPath.lineTo(x, chartHeight + paddingTop)
            fillPath.close()
        }
    }

    // 渐变填充
    drawPath(
        path = fillPath,
        brush = Brush.verticalGradient(
            colors = listOf(gradientStartColor, gradientEndColor),
            startY = paddingTop,
            endY = paddingTop + chartHeight
        )
    )

    // 主折线
    drawPath(
        path = path,
        color = lineColor,
        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
    )

    // 网格与 Y 轴刻度
    val gridLines = 4
    repeat(gridLines + 1) { i ->
        val ratio = i / gridLines.toFloat()
        val y = paddingTop + chartHeight * ratio
        // 网格线
        drawLine(
            color = Color(0x1A000000),
            start = Offset(paddingLeft, y),
            end = Offset(paddingLeft + chartWidth, y),
            strokeWidth = 1.dp.toPx()
        )
        // 价格标签
        val priceLabel = chartMax - priceRange * ratio
        val text = String.format(Locale.getDefault(), "%.2f", priceLabel)
        drawContext.canvas.nativeCanvas.drawText(
            text,
            paddingLeft - 8.dp.toPx() - textPaint.measureText(text),
            y + textPaint.textSize / 3,
            textPaint
        )
    }

    // X 轴
    drawLine(
        color = Color(0x33000000),
        start = Offset(paddingLeft, paddingTop + chartHeight),
        end = Offset(paddingLeft + chartWidth, paddingTop + chartHeight),
        strokeWidth = 1.dp.toPx()
    )

    // 底部时间标签（限量，避免切换卡顿）
    for (i in labels.indices step labelStep) {
        val x = paddingLeft + stepX * i
        val text = labels[i]
        drawContext.canvas.nativeCanvas.drawText(
            text,
            x - textPaint.measureText(text) / 2,
            paddingTop + chartHeight + textPaint.textSize + 6.dp.toPx(),
            textPaint
        )
    }
}
