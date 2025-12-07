package com.feishu.tabfeatures.message.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.feishu.tabfeatures.message.intent.MessageIntent
import java.util.UUID
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.feishu.tabfeatures.message.state.MessageState
import com.feishu.tabfeatures.message.state.ChatMessage
import com.feishu.tabfeatures.message.state.MessageItem
/**
 * 消息Tab的ViewModel - 采用MVI架构
 */
class MessageViewModel : ViewModel() {

    private val _state = MutableStateFlow(MessageState())
    val state: StateFlow<MessageState> = _state.asStateFlow()

    init {
        // 初始化时加载消息
        handleIntent(MessageIntent.LoadMessages)
    }

    /**
     * 处理用户意图
     */
    fun handleIntent(intent: MessageIntent) {
        viewModelScope.launch {
            when (intent) {
                is MessageIntent.LoadMessages -> {
                    loadMessages()
                }
                is MessageIntent.RefreshMessages -> {
                    refreshMessages()
                }
                is MessageIntent.MarkAsRead -> {
                    markAsRead(intent.messageId)
                }
                is MessageIntent.DeleteMessage -> {
                    deleteMessage(intent.messageId)
                }
                is MessageIntent.OpenChat -> {
                    openChat(intent.messageId)
                }
                is MessageIntent.CloseChat -> {
                    closeChat()
                }
                is MessageIntent.SendChatMessage -> {
                    sendChatMessage(intent.content)
                }
                is MessageIntent.ClearError -> {
                    clearError()
                }
            }
        }
    }

    /**
     * 加载消息列表
     */
    private suspend fun loadMessages() {
        _state.update { it.copy(isLoading = true, error = null) }

        try {
            // 模拟网络请求延迟
            delay(1000)

            // 模拟消息数据
            val messages = generateMockMessages()

            _state.update {
                it.copy(
                    messages = messages,
                    isLoading = false,
                    unreadCount = messages.count { !it.isRead }
                )
            }
        } catch (e: Exception) {
            _state.update {
                it.copy(
                    isLoading = false,
                    error = e.message ?: "加载消息失败"
                )
            }
        }
    }

    /**
     * 刷新消息列表
     */
    private suspend fun refreshMessages() {
        _state.update { it.copy(isRefreshing = true, error = null) }

        try {
            // 模拟网络请求延迟
            delay(800)

            // 模拟消息数据
            val messages = generateMockMessages()

            _state.update {
                it.copy(
                    messages = messages,
                    isRefreshing = false,
                    unreadCount = messages.count { !it.isRead }
                )
            }
        } catch (e: Exception) {
            _state.update {
                it.copy(
                    isRefreshing = false,
                    error = e.message ?: "刷新消息失败"
                )
            }
        }
    }

    /**
     * 标记消息为已读
     */
    private fun markAsRead(messageId: String) {
        _state.update { currentState ->
            val updatedMessages = currentState.messages.map { message ->
                if (message.id == messageId && !message.isRead) {
                    message.copy(isRead = true)
                } else {
                    message
                }
            }

            currentState.copy(
                messages = updatedMessages,
                unreadCount = updatedMessages.count { !it.isRead }
            )
        }
    }

    /**
     * 删除消息
     */
    private fun deleteMessage(messageId: String) {
        _state.update { currentState ->
            val updatedMessages = currentState.messages.filter { it.id != messageId }

            currentState.copy(
                messages = updatedMessages,
                unreadCount = updatedMessages.count { !it.isRead },
                // 若删除的是当前聊天，自动关闭聊天
                selectedChat = currentState.selectedChat?.takeIf { it.id != messageId },
                chatHistory = if (currentState.selectedChat?.id == messageId) emptyList() else currentState.chatHistory
            )
        }
    }

    /**
     * 打开聊天详情
     */
    private fun openChat(messageId: String) {
        val currentMessages = _state.value.messages
        val selected = currentMessages.find { it.id == messageId } ?: return
        val updatedMessages = currentMessages.map { message ->
            if (message.id == messageId && !message.isRead) {
                message.copy(isRead = true)
            } else {
                message
            }
        }

        _state.update {
            it.copy(
                messages = updatedMessages,
                unreadCount = updatedMessages.count { !it.isRead },
                selectedChat = selected,
                chatHistory = generateMockChatHistory(selected)
            )
        }
    }

    /**
     * 关闭聊天详情
     */
    private fun closeChat() {
        _state.update {
            it.copy(
                selectedChat = null,
                chatHistory = emptyList()
            )
        }
    }

    /**
     * 发送聊天消息
     */
    private fun sendChatMessage(content: String) {
        val trimmed = content.trim()
        if (trimmed.isEmpty()) return

        val currentChat = _state.value.selectedChat ?: return
        val outgoingMessage = ChatMessage(
            id = UUID.randomUUID().toString(),
            sender = "我",
            content = trimmed,
            timestamp = System.currentTimeMillis(),
            isMe = true
        )

        _state.update {
            it.copy(
                chatHistory = it.chatHistory + outgoingMessage
            )
        }

        // 模拟对方回复
        viewModelScope.launch {
            delay(600)
            val reply = ChatMessage(
                id = UUID.randomUUID().toString(),
                sender = currentChat.sender,
                content = "收到：$trimmed",
                timestamp = System.currentTimeMillis(),
                isMe = false
            )
            _state.update {
                // 若期间关闭了聊天，则不追加
                if (it.selectedChat?.id == currentChat.id) {
                    it.copy(chatHistory = it.chatHistory + reply)
                } else {
                    it
                }
            }
        }
    }

    /**
     * 清除错误
     */
    private fun clearError() {
        _state.update { it.copy(error = null) }
    }

    /**
     * 生成模拟消息数据
     */
    private fun generateMockMessages(): List<MessageItem> {
        return listOf(
            MessageItem(
                id = "1",
                title = "系统通知",
                content = "您有一条新的系统消息，请及时查看",
                sender = "系统",
                timestamp = System.currentTimeMillis() - 1000 * 60 * 5, // 5分钟前
                isRead = false
            ),
            MessageItem(
                id = "2",
                title = "团队协作",
                content = "张三邀请您加入项目讨论",
                sender = "张三",
                timestamp = System.currentTimeMillis() - 1000 * 60 * 30, // 30分钟前
                isRead = false
            ),
            MessageItem(
                id = "3",
                title = "会议提醒",
                content = "下午3点有团队会议，请准时参加",
                sender = "李四",
                timestamp = System.currentTimeMillis() - 1000 * 60 * 60 * 2, // 2小时前
                isRead = true
            ),
            MessageItem(
                id = "4",
                title = "任务更新",
                content = "您的任务状态已更新为已完成",
                sender = "系统",
                timestamp = System.currentTimeMillis() - 1000 * 60 * 60 * 5, // 5小时前
                isRead = true
            ),
            MessageItem(
                id = "5",
                title = "文档分享",
                content = "王五分享了文档《项目计划书》",
                sender = "王五",
                timestamp = System.currentTimeMillis() - 1000 * 60 * 60 * 24, // 1天前
                isRead = true
            )
        )
    }

    /**
     * 生成模拟聊天记录
     */
    private fun generateMockChatHistory(message: MessageItem): List<ChatMessage> {
        return listOf(
            ChatMessage(
                id = "${message.id}-1",
                sender = message.sender,
                content = message.content,
                timestamp = message.timestamp,
                isMe = false
            ),
            ChatMessage(
                id = "${message.id}-2",
                sender = "我",
                content = "收到，会尽快跟进。",
                timestamp = message.timestamp + 2 * 60 * 1000,
                isMe = true
            ),
            ChatMessage(
                id = "${message.id}-3",
                sender = message.sender,
                content = "辛苦啦，有进展及时同步。",
                timestamp = message.timestamp + 5 * 60 * 1000,
                isMe = false
            )
        )
    }
}
