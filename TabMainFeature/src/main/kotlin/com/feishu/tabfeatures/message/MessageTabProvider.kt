package com.feishu.tabfeatures.message
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import com.feishu.tabinterface.FeiShuTitleBar
import com.feishu.tabinterface.TabDescriptor
import com.feishu.tabinterface.TabRegister
import com.feishu.tabfeatures.message.state.ChatMessage
import com.feishu.tabfeatures.message.screen.MessageScreen
import com.feishu.mainfeature.tabs.MessageHome
import com.feishu.mainfeature.tabs.Conversation
import com.feishu.tabfeatures.message.screen.ChatDetailScreen
import com.feishu.tabfeatures.message.state.MessageItem
import java.util.UUID
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
class MessageTabProvider : TabRegister{
    override val descriptor = TabDescriptor(
        id = "com.lea.feishutab.message",
        title = "消息",
        icon = Icons.Filled.Call,
        route = "message"
    )

    @Composable
    override fun TopBar(navController: NavHostController) {
        FeiShuTitleBar(
            title = descriptor.title
        )
    }

    @Composable
    override fun Content(navController: NavHostController) {
        var activeConversation by remember { mutableStateOf<Conversation?>(null) }
        var chatHistory by remember { mutableStateOf<List<ChatMessage>>(emptyList()) }
        val scope = rememberCoroutineScope()

        if (activeConversation == null) {
            MessageHome(
                onEnterChat = { conversation ->
                    val target = conversation ?: createDefaultConversation()
                    activeConversation = target
                    chatHistory = initialHistory(target)
                }
            )
        } else {
            BackHandler {
                activeConversation = null
                chatHistory = emptyList()
            }
            ChatDetailScreen(
                chat = activeConversation!!.toMessageItem(),
                history = chatHistory,
                onBack = {
                    activeConversation = null
                    chatHistory = emptyList()
                },
                onSendMessage = { content ->
                    val trimmed = content.trim()
                    if (trimmed.isEmpty()) return@ChatDetailScreen
                    val conversation = activeConversation ?: return@ChatDetailScreen
                    val outgoing = ChatMessage(
                        id = UUID.randomUUID().toString(),
                        sender = "我",
                        content = trimmed,
                        timestamp = System.currentTimeMillis(),
                        isMe = true
                    )
                    chatHistory = chatHistory + outgoing
                    scope.launch {
                        delay(400)
                        val reply = ChatMessage(
                            id = UUID.randomUUID().toString(),
                            sender = conversation.title,
                            content = "收到：$trimmed",
                            timestamp = System.currentTimeMillis(),
                            isMe = false
                        )
                        chatHistory = chatHistory + reply
                    }
                }
            )
        }
    }
}

private fun Conversation.toMessageItem(): MessageItem = MessageItem(
    id = id,
    title = title,
    content = snippet,
    sender = title,
    timestamp = System.currentTimeMillis(),
    isRead = false
)

private fun initialHistory(conversation: Conversation): List<ChatMessage> = listOf(
    ChatMessage(
        id = UUID.randomUUID().toString(),
        sender = conversation.title,
        content = conversation.snippet.ifBlank { "嗨，有什么可以帮忙的？" },
        timestamp = System.currentTimeMillis() - 2 * 60 * 1000,
        isMe = false
    )
)

private fun createDefaultConversation(): Conversation = Conversation(
    id = UUID.randomUUID().toString(),
    title = "新的会话",
    snippet = "开始聊天吧",
    time = "刚刚",
    unreadCount = 0,
    isPinned = false,
    hasMention = false,
    tag = null,
    avatarColor = androidx.compose.ui.graphics.Color(0xFF5C6BC0)
)
