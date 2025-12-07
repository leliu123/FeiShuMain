package com.feishu.mainfeature.tabs

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChatBubble
import androidx.compose.material.icons.outlined.FilterAlt
import androidx.compose.material.icons.outlined.GroupAdd
import androidx.compose.material.icons.automirrored.outlined.Message
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.feishu.mainfeature.navigation.ROUTE_AI_CHAT
import com.feishu.tabinterface.TabDescriptor
import com.feishu.tabinterface.TabRegister
import java.util.UUID

class MessageTabRegister : TabRegister {
    override val descriptor: TabDescriptor = TabDescriptor(
        id = "message",
        title = "消息",
        icon = Icons.AutoMirrored.Outlined.Message,
        route = "tab/message",
    )

    @Composable
    override fun TopBar(navController: NavHostController) {
        // 使用系统的沉浸式标题栏，消息页本身在内容区域提供搜索与筛选
    }

    @Composable
    override fun Content(navController: NavHostController) {
        MessageHome(
            onEnterChat = { _ -> navController.navigate(ROUTE_AI_CHAT) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageHome(
    onEnterChat: (Conversation?) -> Unit
) {
    val filters = remember { listOf("全部", "未读", "公告", "机器人") }
    var selectedFilter by rememberSaveable { mutableStateOf(filters.first()) }
    var query by rememberSaveable { mutableStateOf("") }
    val conversations = remember {
        mutableStateListOf<Conversation>().apply { addAll(mockConversations()) }
    }
    val palette = remember {
        listOf(
            Color(0xFF5C6BC0),
            Color(0xFFFFB74D),
            Color(0xFF26A69A),
            Color(0xFFEF5350),
            Color(0xFFAB47BC)
        )
    }
    var activeConversation by remember { mutableStateOf<Conversation?>(null) }
    var pendingAction by remember { mutableStateOf<QuickActionType?>(null) }
    var actionPrimary by remember { mutableStateOf("") }
    var actionSecondary by remember { mutableStateOf("") }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current
    val addConversation: (String, String, String?, Boolean) -> Unit = remember(conversations, palette) {
        { title, snippet, tag, pinned ->
            val nextColor = palette[conversations.size % palette.size]
            val newConversation = Conversation(
                id = UUID.randomUUID().toString(),
                title = title.ifBlank { "新的会话" },
                snippet = snippet.ifBlank { "暂无内容" },
                time = "刚刚",
                unreadCount = 1,
                isPinned = pinned,
                hasMention = false,
                tag = tag,
                avatarColor = nextColor
            )
            conversations.add(0, newConversation)
        }
    }
    val filteredConversations by remember {
        derivedStateOf {
            val keyword = query.trim()
            conversations.filter { conversation ->
                val filterPass = when (selectedFilter) {
                    "全部" -> true
                    "未读" -> conversation.unreadCount > 0
                    "公告" -> conversation.tag == "公告"
                    "机器人" -> conversation.tag == "机器人"
                    else -> true
                }
                val keywordPass = keyword.isBlank() ||
                    conversation.title.contains(keyword, ignoreCase = true) ||
                    conversation.snippet.contains(keyword, ignoreCase = true)
                filterPass && keywordPass
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))
        MessageSearchBar(
            query = query,
            onQueryChange = { query = it },
            onEnterChat = { onEnterChat(null) }
        )
        Spacer(modifier = Modifier.height(12.dp))
        MessageQuickActions(
            onActionClick = { action ->
                when (action.type) {
                    QuickActionType.StartChat -> onEnterChat(null)
                    QuickActionType.CreateGroup,
                    QuickActionType.InviteMember,
                    QuickActionType.Announcement -> {
                        pendingAction = action.type
                        actionPrimary = ""
                        actionSecondary = ""
                    }
                }
            }
        )
        Spacer(modifier = Modifier.height(12.dp))
        MessageFilterSection(
            filters = filters,
            selectedFilter = selectedFilter,
            onFilterSelected = { selectedFilter = it }
        )
        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(8.dp))
    if (filteredConversations.isEmpty()) {
        EmptyConversationState(query, selectedFilter)
    } else {
        ConversationList(
            conversations = filteredConversations,
            onConversationClick = { conversation ->
                activeConversation = conversation
            }
        )
    }

    activeConversation?.let { conversation ->
        ModalBottomSheet(
            onDismissRequest = { activeConversation = null },
            sheetState = sheetState
        ) {
            ConversationDetailSheet(
                conversation = conversation,
                onMarkRead = { updated ->
                    val newUnread = if (updated.unreadCount > 0) 0 else 1
                    updateConversation(conversations, updated.copy(unreadCount = newUnread))
                },
                onTogglePin = { updated ->
                    updateConversation(conversations, updated.copy(isPinned = !updated.isPinned))
                },
                onDelete = { updated ->
                    conversations.remove(updated)
                },
                onEnterChat = {
                    activeConversation = null
                    onEnterChat(conversation)
                },
                onDismiss = { activeConversation = null }
            )
        }
    }

    pendingAction?.let { action ->
        when (action) {
            QuickActionType.CreateGroup -> QuickActionDialog(
                title = "创建群聊",
                primaryLabel = "群聊名称",
                secondaryLabel = "群介绍（可选）",
                primaryValue = actionPrimary,
                secondaryValue = actionSecondary,
                onPrimaryChange = { actionPrimary = it },
                onSecondaryChange = { actionSecondary = it },
                onDismiss = { pendingAction = null },
                onConfirm = {
                    val name = actionPrimary.ifBlank { "新群聊" }
                    val desc = actionSecondary.ifBlank { "群聊已创建" }
                    addConversation(name, desc, "群聊", true)
                    Toast.makeText(context, "已创建群聊「$name」", Toast.LENGTH_SHORT).show()
                    pendingAction = null
                }
            )

            QuickActionType.InviteMember -> QuickActionDialog(
                title = "加人入群",
                primaryLabel = "同事姓名",
                secondaryLabel = "目标群聊",
                primaryValue = actionPrimary,
                secondaryValue = actionSecondary,
                onPrimaryChange = { actionPrimary = it },
                onSecondaryChange = { actionSecondary = it },
                onDismiss = { pendingAction = null },
                onConfirm = {
                    val member = actionPrimary.ifBlank { "同事" }
                    val group = actionSecondary.ifBlank { "当前群聊" }
                    Toast.makeText(context, "已邀请 $member 加入 $group", Toast.LENGTH_SHORT).show()
                    pendingAction = null
                }
            )

            QuickActionType.Announcement -> QuickActionDialog(
                title = "公告通知",
                primaryLabel = "公告标题",
                secondaryLabel = "公告内容",
                primaryValue = actionPrimary,
                secondaryValue = actionSecondary,
                onPrimaryChange = { actionPrimary = it },
                onSecondaryChange = { actionSecondary = it },
                onDismiss = { pendingAction = null },
                onConfirm = {
                    val annTitle = actionPrimary.ifBlank { "新公告" }
                    val annContent = actionSecondary.ifBlank { "请及时关注最新公告" }
                    addConversation(annTitle, annContent, "公告", true)
                    Toast.makeText(context, "公告已发送", Toast.LENGTH_SHORT).show()
                    pendingAction = null
                }
            )

            QuickActionType.StartChat -> {
                // handled earlier
                pendingAction = null
            }
        }
    }

    // End of main column
    }
}

@Composable
private fun MessageSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onEnterChat: () -> Unit
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth(),
        leadingIcon = {
            Icon(imageVector = Icons.Outlined.Search, contentDescription = "搜索")
        },
        trailingIcon = {
            TextButton(onClick = { if (query.isNotEmpty()) onQueryChange("") else onEnterChat() }) {
                Text(text = if (query.isNotEmpty()) "清空" else "发起会话")
            }
        },
        placeholder = { Text(text = "搜索会话、群聊或文档") },
        singleLine = true
    )
}

@Composable
private fun MessageQuickActions(onActionClick: (QuickAction) -> Unit) {
    val quickActions = remember {
        listOf(
            QuickAction("发起会话", Icons.Outlined.ChatBubble, QuickActionType.StartChat),
            QuickAction("加人入群", Icons.Outlined.PersonAdd, QuickActionType.InviteMember),
            QuickAction("创建群聊", Icons.Outlined.GroupAdd, QuickActionType.CreateGroup),
            QuickAction("公告通知", Icons.Outlined.Notifications, QuickActionType.Announcement)
        )
    }

    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        items(quickActions) { action ->
            AssistChip(
                onClick = { onActionClick(action) },
                label = { Text(action.label) },
                leadingIcon = {
                    Icon(
                        imageVector = action.icon,
                        contentDescription = action.label,
                        modifier = Modifier.size(18.dp)
                    )
                }
            )
        }
    }
}

@Composable
private fun MessageFilterSection(
    filters: List<String>,
    selectedFilter: String,
    onFilterSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(filters) { filter ->
                FilterChip(
                    selected = filter == selectedFilter,
                    onClick = { onFilterSelected(filter) },
                    label = { Text(filter) }
                )
            }
        }
        TextButton(onClick = { /* placeholder */ }) {
            Icon(
                imageVector = Icons.Outlined.FilterAlt,
                contentDescription = "更多筛选",
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = "筛选")
        }
    }
}

@Composable
private fun ConversationList(
    conversations: List<Conversation>,
    onConversationClick: (Conversation) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Top
    ) {
        items(conversations, key = { it.id }) { conversation ->
            ConversationRow(
                conversation = conversation,
                onConversationClick = { onConversationClick(conversation) }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ConversationRow(
    conversation: Conversation,
    onConversationClick: () -> Unit
) {
    val context = LocalContext.current
    Column(modifier = Modifier
        .fillMaxWidth()
        .combinedClickable(
            onClick = { onConversationClick() },
            onLongClick = {
                Toast.makeText(context, "稍后可在这里展示会话操作", Toast.LENGTH_SHORT).show()
            }
        )
        .padding(vertical = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            ConversationAvatar(conversation)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = conversation.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (conversation.isPinned) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Outlined.PushPin,
                            contentDescription = "已置顶",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = conversation.snippet,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = conversation.time,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                if (conversation.unreadCount > 0) {
                    BadgedBox(badge = {
                        Badge(containerColor = MaterialTheme.colorScheme.error) {
                            Text(text = conversation.unreadCount.toString())
                        }
                    }) {
                        Spacer(modifier = Modifier.size(1.dp))
                    }
                }
            }
        }
        if (conversation.tag != null || conversation.hasMention) {
            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                conversation.tag?.let { tag ->
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = tag,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
                if (conversation.hasMention) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = "@我",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
    }
}

@Composable
private fun ConversationAvatar(conversation: Conversation) {
    Surface(
        color = conversation.avatarColor.copy(alpha = 0.15f),
        shape = MaterialTheme.shapes.large
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(MaterialTheme.shapes.large),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = conversation.initial,
                style = MaterialTheme.typography.titleMedium,
                color = conversation.avatarColor,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun EmptyConversationState(query: String, filter: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "没有符合条件的会话",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "尝试调整筛选条件或重新输入关键词",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        val summary = buildString {
            append("当前筛选：$filter")
            if (query.isNotBlank()) append(" · $query")
        }
        Text(
            text = summary,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private data class QuickAction(
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val type: QuickActionType
)

private enum class QuickActionType {
    StartChat,
    InviteMember,
    CreateGroup,
    Announcement
}

@Composable
private fun QuickActionDialog(
    title: String,
    primaryLabel: String,
    secondaryLabel: String,
    primaryValue: String,
    secondaryValue: String,
    onPrimaryChange: (String) -> Unit,
    onSecondaryChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = primaryValue,
                    onValueChange = onPrimaryChange,
                    label = { Text(primaryLabel) },
                    singleLine = true
                )
                OutlinedTextField(
                    value = secondaryValue,
                    onValueChange = onSecondaryChange,
                    label = { Text(secondaryLabel) },
                    singleLine = false,
                    minLines = 2
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("确认")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

data class Conversation(
    val id: String,
    val title: String,
    val snippet: String,
    val time: String,
    val unreadCount: Int,
    val isPinned: Boolean,
    val hasMention: Boolean,
    val tag: String?,
    val avatarColor: Color
) {
    val initial: String = title.firstOrNull()?.toString() ?: "?"
}

private fun updateConversation(
    conversations: MutableList<Conversation>,
    updated: Conversation
) {
    val index = conversations.indexOfFirst { it.id == updated.id }
    if (index >= 0) {
        conversations[index] = updated
    }
}

@Composable
private fun ConversationDetailSheet(
    conversation: Conversation,
    onMarkRead: (Conversation) -> Unit,
    onTogglePin: (Conversation) -> Unit,
    onDelete: (Conversation) -> Unit,
    onEnterChat: () -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            ConversationAvatar(conversation)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = conversation.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "最近消息 · ${conversation.time}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = conversation.snippet,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(
                onClick = { onTogglePin(conversation) },
                modifier = Modifier.weight(1f)
            ) {
                Text(text = if (conversation.isPinned) "取消置顶" else "置顶会话")
            }
            OutlinedButton(
                onClick = { onMarkRead(conversation) },
                modifier = Modifier.weight(1f)
            ) {
                Text(text = if (conversation.unreadCount > 0) "标记已读" else "标记未读")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onEnterChat,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "进入会话")
        }
        TextButton(
            onClick = {
                onDelete(conversation)
                onDismiss()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "删除会话", color = MaterialTheme.colorScheme.error)
        }
    }
}

private fun mockConversations(): List<Conversation> = listOf(
    Conversation(
        id = "1",
        title = "研发群",
        snippet = "@你 下午4点记得发布",
        time = "09:12",
        unreadCount = 8,
        isPinned = true,
        hasMention = true,
        tag = "公告",
        avatarColor = Color(0xFF5C6BC0)
    ),
    Conversation(
        id = "2",
        title = "与王小二",
        snippet = "明天的评审资料我发你邮箱了",
        time = "昨天",
        unreadCount = 0,
        isPinned = false,
        hasMention = false,
        tag = null,
        avatarColor = Color(0xFFFFB74D)
    ),
    Conversation(
        id = "3",
        title = "营销日报机器人",
        snippet = "今日线索新增 35 条，转化 3 单",
        time = "09:00",
        unreadCount = 2,
        isPinned = false,
        hasMention = false,
        tag = "机器人",
        avatarColor = Color(0xFF26A69A)
    ),
    Conversation(
        id = "4",
        title = "人力 - Offer 跟进",
        snippet = "李雷签约流程已完成",
        time = "周二",
        unreadCount = 1,
        isPinned = false,
        hasMention = false,
        tag = null,
        avatarColor = Color(0xFFAB47BC)
    ),
    Conversation(
        id = "5",
        title = "通知助手",
        snippet = "[公告] 周五 18:00 机房断电维护",
        time = "周一",
        unreadCount = 0,
        isPinned = true,
        hasMention = false,
        tag = "公告",
        avatarColor = Color(0xFFEF5350)
    )
)
