package com.feishu.AIChat.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.feishu.AIChat.intent.ChatIntent
import com.feishu.AIChat.state.ChatMessage
import com.feishu.AIChat.state.ChatState
import com.feishu.AIChat.network.ApiService
import com.feishu.AIChat.network.ChatRequest
import com.feishu.AIChat.network.StreamResponseParser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class ChatViewModel(private val apiService: ApiService) : ViewModel() {

    // UI状态
    private val _state = MutableStateFlow(ChatState())
    val state: StateFlow<ChatState> = _state.asStateFlow()

    init {
        // 加载聊天记录
        handleIntent(ChatIntent.InitializeChat)
    }

    fun handleIntent(intent: ChatIntent) {
        when (intent) {
            ChatIntent.InitializeChat -> initializeChat()
            is ChatIntent.SendMessage -> sendMessage(intent.message)
            ChatIntent.ClearChat -> clearChat()
            ChatIntent.Reload -> reload()
            ChatIntent.StopRequesting -> stopRequesting()
        }
    }

    private fun initializeChat() {
        _state.value = _state.value.copy(isLoading = true, isInitialized = false)

        viewModelScope.launch {
            try {

                _state.value = _state.value.copy(
                    isInitialized = true,
                    isLoading = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    errorMessage = "加载失败: ${e.message}",
                    isLoading = false
                )
            }
        }
    }

    private fun sendMessage(message: String) {
        // 检查消息是否为空
        if (message.isBlank()) return

        // 添加用户消息
        val userMessage = ChatMessage(
            id = UUID.randomUUID().toString(),
            content = message,
            isUserMessage = true
        )

        val currentMessages = _state.value.messages.toMutableList()
        currentMessages.add(userMessage)

        _state.value = _state.value.copy(
            messages = currentMessages,
            currentInput = "",
            isWaitingForResponse = true,
            errorMessage = null
        )

        // 异步请求AI回复
        viewModelScope.launch {
            try {
                // 准备请求体
                val request = ChatRequest(
                    messages = buildMessages(currentMessages)
                )

                // 调用API获取响应
                val response = apiService.chatStream(request)

                // 处理响应
                val aiReplyBuilder = StringBuilder()
                response.body()?.byteStream()?.use { stream ->
                    val parser = StreamResponseParser()
                    parser.parse(stream) { chunk ->
                        aiReplyBuilder.append(chunk)
                        updateAIMessage(aiReplyBuilder.toString())
                    }
                }

                // 响应完成
                _state.value = _state.value.copy(
                    isWaitingForResponse = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    errorMessage = "请求失败: ${e.message}",
                    isWaitingForResponse = false
                )
            }
        }
    }

    private fun updateAIMessage(content: String) {
        val messages = _state.value.messages.toMutableList()

        // 最后一条是AI消息，则更新
        if (messages.isNotEmpty() && !messages.last().isUserMessage) {
            messages[messages.size - 1] = messages.last().copy(content = content)
        } else {
            messages.add(ChatMessage(
                id = UUID.randomUUID().toString(),
                content = content,
                isUserMessage = false
            ))
        }

        _state.value = _state.value.copy(messages = messages)
    }

    private fun clearChat() {
        _state.value = _state.value.copy(
            messages = emptyList(),
            currentInput = "",
            errorMessage = null
        )
        viewModelScope.launch {
            try {
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    errorMessage = "清空失败: ${e.message}"
                )
            }
        }
    }

    private fun reload() {
        _state.value = _state.value.copy(
            isInitialized = false,
            errorMessage = null
        )
        initializeChat()
    }

    private fun stopRequesting() {
        _state.value = _state.value.copy(isWaitingForResponse = false)
    }

    fun updateInput(newInput: String) {
        _state.value = _state.value.copy(currentInput = newInput)
    }

    private fun buildMessages(messages: List<ChatMessage>): List<Map<String, String>> {
        return messages.map { message ->
            mapOf(
                "role" to if (message.isUserMessage) "user" else "assistant",
                "content" to message.content
            )
        }
    }
}