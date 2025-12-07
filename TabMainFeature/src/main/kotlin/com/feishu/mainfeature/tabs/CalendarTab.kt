package com.feishu.mainfeature.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Button
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.feishu.tabinterface.TabDescriptor
import com.feishu.tabinterface.TabRegister
import java.util.UUID
import kotlin.math.max

/**
 * 精简版日历：包含时间轴、事件卡片、简易创建入口，使用自定义布局避免重叠。
 */
class CalendarTab : TabRegister {
    override val descriptor: TabDescriptor = TabDescriptor(
        id = "calendar",
        title = "日历",
        icon = Icons.Outlined.CalendarToday,
        route = "tab/calendar"
    )

    @Composable
    override fun TopBar(navController: NavHostController) {
        // 简洁顶部栏由内容区自行处理
    }

    @Composable
    override fun Content(navController: NavHostController) {
        CalendarScreen()
    }
}

// ------------------------------------------------------------
// UI 主体
// ------------------------------------------------------------

private object CalendarStore {
    val schedules by lazy { buildInitialSchedules() }
}

@Composable
private fun CalendarScreen() {
    val dayKeys = (1..30).map { "day$it" }
    var selectedDay by rememberSaveable { mutableStateOf(dayKeys.first()) }
    val schedules = CalendarStore.schedules
    val dayEvents = schedules.getOrPut(selectedDay) { mutableStateListOf() }
    var showDayPicker by remember { mutableStateOf(false) }

    var titleInput by rememberSaveable { mutableStateOf("") }
    var timeInput by rememberSaveable { mutableStateOf("09:00 - 10:00") }
    var showCreateDialog by remember { mutableStateOf(false) }

    fun addEvent(title: String, timeText: String) {
        val parsed = parseTimeRange(timeText)
        val range = parsed ?: DEFAULT_EVENT_RANGE
        dayEvents.add(
            CalendarEvent(
                id = UUID.randomUUID().toString(),
                title = title.ifBlank { "未命名日程" },
                startMinutes = range.first,
                endMinutes = range.second,
                color = sampleColors[dayEvents.size % sampleColors.size],
                status = "会议",
                attendees = 3,
                location = null,
                organizer = "我"
            )
        )
    }

    fun resizeEvent(event: CalendarEvent, newStart: Int, newEnd: Int) {
        val idx = dayEvents.indexOfFirst { it.id == event.id }
        if (idx >= 0) {
            val safeStart = newStart.coerceAtLeast(DAY_START_MINUTES)
            val safeEnd = newEnd
                .coerceAtLeast(safeStart + MIN_EVENT_DURATION_MINUTES)
                .coerceAtMost(DAY_END_MINUTES)
            dayEvents[idx] = dayEvents[idx].copy(
                startMinutes = safeStart,
                endMinutes = safeEnd
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "日历",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold
                )
                DaySelector(
                    selectedLabel = "第 ${dayKeys.indexOf(selectedDay) + 1} 天",
                    onOpenPicker = { showDayPicker = true }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            TimeLineBoard(
                events = dayEvents.sortedBy { it.startMinutes },
                onDelete = { event ->
                    dayEvents.removeAll { it.id == event.id }
                },
                onResize = { event, start, end -> resizeEvent(event, start, end) }
            )
            Spacer(modifier = Modifier.height(80.dp))
        }

        FloatingActionButton(
            onClick = { showCreateDialog = true },
            containerColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .align(Alignment.BottomStart)
        ) {
            Icon(
                imageVector = Icons.Outlined.Add,
                contentDescription = "添加日程"
            )
        }
    }

    if (showDayPicker) {
        DayPickerDialog(
            days = dayKeys,
            selected = selectedDay,
            onSelect = {
                selectedDay = it
                showDayPicker = false
            },
            onDismiss = { showDayPicker = false }
        )
    }

    if (showCreateDialog) {
        AddEventDialog(
            title = titleInput,
            timeText = timeInput,
            onTitleChange = { titleInput = it },
            onTimeChange = { timeInput = it },
            onDismiss = { showCreateDialog = false },
            onConfirm = {
                addEvent(titleInput, timeInput)
                titleInput = ""
                showCreateDialog = false
            }
        )
    }
}

// ------------------------------------------------------------
// 组件
// ------------------------------------------------------------

@Composable
private fun DaySelector(selectedLabel: String, onOpenPicker: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
        modifier = Modifier
            .clickable { onOpenPicker() }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(
                text = selectedLabel,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Outlined.CalendarToday,
                contentDescription = "选择日期",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun TimeLineBoard(
    events: List<CalendarEvent>,
    onDelete: (CalendarEvent) -> Unit,
    onResize: (CalendarEvent, Int, Int) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        tonalElevation = 2.dp,
        shape = RoundedCornerShape(16.dp)
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(600.dp)
                .padding(12.dp)
        ) {
            val constraint = constraints
            val containerHeightPx = constraint.maxHeight
            val containerWidthPx = constraint.maxWidth
            val totalMinutes = (DAY_END_MINUTES - DAY_START_MINUTES).coerceAtLeast(1)
            val labels = generateTimelineHours()
            val density = LocalDensity.current
            val labelWidthDp = 64.dp
            val spacerDp = 8.dp
            val segmentHeight = maxHeight / labels.size
            val paddingPx = with(density) { (labelWidthDp + spacerDp).toPx().toInt() }

            Box(Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .width(labelWidthDp)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    labels.forEachIndexed { index, minute ->
                        Column(
                            modifier = Modifier
                                .height(segmentHeight)
                                .fillMaxWidth()
                        ) {
                            Text(
                                text = formatMinutes(minute),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (index < labels.lastIndex) {
                                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                            }
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth()
                        .padding(start = labelWidthDp + spacerDp)
                ) {
                    ScheduleLayout(
                        events = events,
                        dayStart = DAY_START_MINUTES,
                        dayEnd = DAY_END_MINUTES,
                        totalMinutes = totalMinutes,
                        containerHeight = containerHeightPx,
                        containerWidth = containerWidthPx - paddingPx,
                        onDelete = onDelete,
                        onResize = onResize
                    )
                }
            }
        }
    }
}

@Composable
private fun DayPickerDialog(
    days: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        title = { Text(text = "选择日期") },
        text = {
            LazyVerticalGrid(
                columns = GridCells.Fixed(5),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(days) { day ->
                    val index = days.indexOf(day) + 1
                    val isSelected = day == selected
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
                        else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSelect(day)
                            }
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "$index",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium,
                                color = if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "日",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    )
}

@Composable
private fun AddEventDialog(
    title: String,
    timeText: String,
    onTitleChange: (String) -> Unit,
    onTimeChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "添加日程") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = onTitleChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("主题") },
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = timeText,
                    onValueChange = onTimeChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("时间段，例如 09:00 - 10:00") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions.Default
                )
            }
        },
        confirmButton = {
            Button(onClick = onConfirm) { Text("保存") }
        },
        dismissButton = {
            Button(onClick = onDismiss) { Text("取消") }
        }
    )
}

// ------------------------------------------------------------
// 数据与布局
// ------------------------------------------------------------

private data class CalendarEvent(
    val id: String,
    val title: String,
    val startMinutes: Int,
    val endMinutes: Int,
    val color: Color,
    val status: String,
    val attendees: Int,
    val location: String?,
    val organizer: String
) {
    val durationMinutes: Int get() = (endMinutes - startMinutes).coerceAtLeast(MIN_EVENT_DURATION_MINUTES)
    val timeRangeLabel: String get() = "${formatMinutes(startMinutes)} - ${formatMinutes(endMinutes)}"
}

private data class EventSlot(val col: Int, val cols: Int)
private data class Placed(val event: CalendarEvent, val x: Int, val y: Int, val width: Int, val height: Int)

@Composable
private fun ScheduleLayout(
    events: List<CalendarEvent>,
    dayStart: Int,
    dayEnd: Int,
    totalMinutes: Int,
    containerHeight: Int,
    containerWidth: Int,
    onDelete: (CalendarEvent) -> Unit,
    onResize: (CalendarEvent, Int, Int) -> Unit
) {
    val density = LocalDensity.current
    val ordered = events.sortedBy { it.startMinutes }
    val slots = rememberSlots(ordered)
    val minutesPerPixel = totalMinutes.toFloat() / containerHeight.toFloat()
    Layout(
        content = {
            ordered.forEach { event ->
                EventBlock(
                    event = event,
                    minutesPerPixel = minutesPerPixel,
                    onDelete = { onDelete(event) },
                    onResize = { newEnd ->
                        onResize(event, event.startMinutes, newEnd)
                    }
                )
            }
        }
    ) { measurables, _ ->
        val minHeight = with(density) { 32.dp.toPx().toInt() }
        val minWidth = with(density) { 56.dp.toPx().toInt() }

        val placed = ordered.mapIndexed { index, event ->
            val slot = slots[event.id] ?: EventSlot(0, 1)
            val top = ((event.startMinutes - dayStart).coerceAtLeast(0) * containerHeight / totalMinutes)
                .coerceIn(0, containerHeight)
            val height = (event.durationMinutes * containerHeight / totalMinutes).coerceAtLeast(minHeight)
            val width = (containerWidth / slot.cols).coerceAtLeast(minWidth)
            val left = width * slot.col
            index to Placed(event, left, top, width, height)
        }.associate { it }

        val placeables = measurables.mapIndexed { index, measurable ->
            val p = placed[index] ?: return@mapIndexed measurable.measure(Constraints())
            measurable.measure(
                Constraints.fixed(width = p.width, height = p.height)
            )
        }

        layout(containerWidth, containerHeight) {
            placeables.forEachIndexed { index, placeable ->
                placed[index]?.let { p ->
                    placeable.placeRelative(p.x, p.y)
                }
            }
        }
    }
}

@Composable
private fun EventBlock(
    event: CalendarEvent,
    minutesPerPixel: Float,
    onDelete: () -> Unit,
    onResize: (Int) -> Unit
) {
    var dragOffset by remember { mutableStateOf(0f) }
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = event.color.copy(alpha = 0.14f),
        border = androidx.compose.foundation.BorderStroke(1.dp, event.color.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 6.dp)
                )
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "删除",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = event.timeRangeLabel,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .pointerInput(event.id) {
                        detectDragGestures(
                            onDragEnd = {
                                val deltaMinutes = (dragOffset * minutesPerPixel).toInt()
                                if (deltaMinutes != 0) {
                                    val newEnd = (event.endMinutes + deltaMinutes)
                                        .coerceAtLeast(event.startMinutes + MIN_EVENT_DURATION_MINUTES)
                                        .coerceAtMost(DAY_END_MINUTES)
                                    onResize(newEnd)
                                }
                                dragOffset = 0f
                            },
                            onDragCancel = { dragOffset = 0f },
                            onDrag = { change, dragAmount ->
                                change.consumeAllChanges()
                                dragOffset += dragAmount.y
                            }
                        )
                    }
                    .background(
                        color = event.color.copy(alpha = 0.35f),
                        shape = RoundedCornerShape(6.dp)
                    )
            )
        }
    }
}

@Composable
private fun rememberSlots(events: List<CalendarEvent>): Map<String, EventSlot> {
    // 两遍：先分组，再分列，再补足 group 列数
    val sorted = events.sortedBy { it.startMinutes }
    val result = mutableMapOf<String, EventSlot>()
    var i = 0
    while (i < sorted.size) {
        val group = mutableListOf(sorted[i])
        var end = sorted[i].endMinutes
        i++
        while (i < sorted.size && sorted[i].startMinutes < end) {
            group += sorted[i]
            end = max(end, sorted[i].endMinutes)
            i++
        }
        val columnEnd = mutableListOf<Int>()
        val temp = mutableListOf<Pair<String, Int>>()
        group.sortedBy { it.startMinutes }.forEach { e ->
            var col = 0
            while (col < columnEnd.size && e.startMinutes < columnEnd[col]) col++
            if (col == columnEnd.size) columnEnd += e.endMinutes else columnEnd[col] = max(columnEnd[col], e.endMinutes)
            temp += e.id to col
        }
        val cols = columnEnd.size.coerceAtLeast(1)
        temp.forEach { (id, col) -> result[id] = EventSlot(col, cols) }
    }
    return result
}

// ------------------------------------------------------------
// 辅助与样例
// ------------------------------------------------------------

private const val DAY_START_MINUTES = 8 * 60
private const val DAY_END_MINUTES = 20 * 60
private const val MIN_EVENT_DURATION_MINUTES = 30
private val DEFAULT_EVENT_RANGE = 9 * 60 to 10 * 60

private val sampleColors = listOf(
    Color(0xFF5C6BC0),
    Color(0xFF26A69A),
    Color(0xFFFFB74D),
    Color(0xFFEF5350),
    Color(0xFF8D6E63)
)

private fun buildInitialSchedules(): MutableMap<String, MutableList<CalendarEvent>> {
    val map = mutableStateMapOf<String, MutableList<CalendarEvent>>()
    map["day1"] = mutableStateListOf(
        CalendarEvent(
            id = "1",
            title = "项目例会",
            startMinutes = 9 * 60 + 30,
            endMinutes = 10 * 60 + 30,
            color = sampleColors[0],
            status = "会议",
            attendees = 10,
            location = "12F-会议室",
            organizer = "产品组"
        ),
        CalendarEvent(
            id = "2",
            title = "需求评审",
            startMinutes = 14 * 60,
            endMinutes = 15 * 60,
            color = sampleColors[1],
            status = "评审",
            attendees = 6,
            location = "线上",
            organizer = "研发"
        )
    )
    map["day2"] = mutableStateListOf(
        CalendarEvent(
            id = "3",
            title = "OKR 对齐",
            startMinutes = 10 * 60,
            endMinutes = 11 * 60 + 30,
            color = sampleColors[2],
            status = "会议",
            attendees = 20,
            location = "线上",
            organizer = "HR"
        )
    )
    (3..30).forEach { index ->
        map["day$index"] = mutableStateListOf()
    }
    return map
}

// 容错时间解析：支持中英文冒号与常见连接符
private val DEFAULT_TIME_REGEXP = Regex(
    pattern = """\s*(\d{1,2})[:：](\d{2})\s*[-–—~～]\s*(\d{1,2})[:：](\d{2})\s*"""
)

private fun parseTimeRange(text: String): Pair<Int, Int>? {
    val match = DEFAULT_TIME_REGEXP.find(text) ?: return null
    val (sh, sm, eh, em) = match.destructured
    val start = sh.toIntOrNull()?.times(60)?.plus(sm.toIntOrNull() ?: 0) ?: return null
    val end = eh.toIntOrNull()?.times(60)?.plus(em.toIntOrNull() ?: 0) ?: return null
    if (end <= start) return null
    return start to end
}

private fun formatMinutes(value: Int): String {
    val h = (value / 60).coerceIn(0, 23)
    val m = (value % 60).coerceIn(0, 59)
    return "%02d:%02d".format(h, m)
}

private fun generateTimelineHours(): List<Int> {
    val list = mutableListOf<Int>()
    var cur = DAY_START_MINUTES
    while (cur <= DAY_END_MINUTES) {
        list += cur
        cur += 60
    }
    return list
}
