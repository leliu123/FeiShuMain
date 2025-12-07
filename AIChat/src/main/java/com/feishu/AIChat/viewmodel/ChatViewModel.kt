package com.feishu.aichat.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.feishu.aichat.intent.ChatIntent
import com.feishu.aichat.data.ChatMessage
import com.feishu.aichat.state.ChatState
import com.feishu.aichat.data.ChatRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException

class ChatViewModel(private val chatRepository: ChatRepository= ChatRepository()) : ViewModel() {

    // UI状态
    private val _state = MutableStateFlow(ChatState())
    val state: StateFlow<ChatState> = _state.asStateFlow()
    private var messageIdCounter = 0L
    private var currentAiRequestJob: Job? = null

    init {
        // 加载聊天记录
        handleIntent(ChatIntent.InitializeChat)
    }

    fun handleIntent(intent: ChatIntent) {
        when (intent) {
            is ChatIntent.InitializeChat -> initializeChat()
            is ChatIntent.SendMessage -> sendMessage(intent.message)
            is ChatIntent.ClearChat -> clearChat()
            is ChatIntent.StopRequesting -> stopRequesting()
        }
    }

    private fun initializeChat() {
        _state.value = _state.value.copy(isLoading = true, isInitialized = false)

        viewModelScope.launch {
            try {
                val messages = chatRepository.getAllMessages().first()
                if (messages.isNotEmpty()) {
                    messageIdCounter = (messages.maxOfOrNull { it.id } ?: -1L) + 1
                    _state.update { currentState ->
                        currentState.copy(
                            messages = messages,
                            isLoading = false,
                            isInitialized = true
                        )
                    }
                    Log.d("AIChatViewModel", "Loaded ${messages.size} messages from database")
                } else {
                    _state.update { currentState ->
                        currentState.copy(
                            isLoading = false,
                            isInitialized = true
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("AIChatViewModel", "Error loading messages", e)
                _state.value = _state.value.copy(
                    errorMessage = "加载失败: ${e.message}",
                    isLoading = false,
                    isInitialized = true
                )
            }
        }
    }

    private fun sendMessage(message: String) {
        // 检查消息是否为空
        if (message.isBlank()) return

        // 获取当前消息列表作为历史记录
        val currentMessages = _state.value.messages.toMutableList()

        // 创建用户消息
        val userMessageId = messageIdCounter++
        val userMessage = ChatMessage(
            id = userMessageId,
            text = message,
            isUser = true
        )

        // 保存用户消息到数据库
        viewModelScope.launch {
            chatRepository.saveMessage(userMessage)
            Log.d("AIChatViewModel", "Saved user message with ID: $userMessageId")
        }

        // 添加用户消息到列表
        currentMessages.add(userMessage)

        // 创建 AI 消息占位符
        val assistantMessageId = messageIdCounter++
        val assistantMessage = ChatMessage(
            id = assistantMessageId,
            text = "",
            isUser = false,
            isLoading = true
        )

        // 更新状态：添加用户消息和空的 AI 消息
        _state.value = _state.value.copy(
            messages = currentMessages + assistantMessage,
            currentInput = "",
            isWaitingForResponse = true,
            errorMessage = null
        )

        // 异步请求AI回复（流式）
        currentAiRequestJob = viewModelScope.launch {
            try {
                val aiReplyBuilder = StringBuilder()
                
                // 使用 Repository 的流式方法
                chatRepository.sendMessageStream(message, currentMessages)
                    .catch { e ->
                        Log.e("AIChatViewModel", "Stream error", e)
                        _state.update { currentState ->
                            currentState.copy(
                                errorMessage = "请求失败: ${e.message}",
                                isWaitingForResponse = false
                            )
                        }
                        // 移除加载中的 AI 消息
                        removeLoadingAIMessage()
                    }
                    .collect { result ->
                        result.fold(
                            onSuccess = { chunk ->
                                // 累积流式响应内容
                                aiReplyBuilder.append(chunk)
                                // 更新 AI 消息内容
                                updateAIMessage(assistantMessageId, aiReplyBuilder.toString())
                            },
                            onFailure = { error ->
                                Log.e("AIChatViewModel", "API error", error)
                                _state.update { currentState ->
                                    currentState.copy(
                                        errorMessage = "请求失败: ${error.message}",
                                        isWaitingForResponse = false
                                    )
                                }
                                // 移除加载中的 AI 消息
                                removeLoadingAIMessage()
                            }
                        )
                    }

                // 流式响应完成，保存最终的 AI 消息到数据库
                val finalAIMessage = ChatMessage(
                    id = assistantMessageId,
                    text = aiReplyBuilder.toString(),
                    isUser = false,
                    isLoading = false
                )
                chatRepository.saveMessage(finalAIMessage)
                Log.d("AIChatViewModel", "Saved AI message with ID: $assistantMessageId")

                // 更新状态：完成响应
                _state.update { currentState ->
                    currentState.copy(
                        isWaitingForResponse = false
                    )
                }
                currentAiRequestJob = null
            } catch (e: Exception) {
                Log.e("AIChatViewModel", "Error sending message", e)
                if (e !is CancellationException){
                    _state.value = _state.value.copy(
                        errorMessage = "请求失败: ${e.message}",
                        isWaitingForResponse = false
                    )
                }
                // 移除加载中的 AI 消息
                removeLoadingAIMessage()
                currentAiRequestJob = null
            }
        }
    }

    private fun updateAIMessage(messageId: Long, content: String) {
        _state.update { currentState ->
            val messages = currentState.messages.toMutableList()
            // 找到对应的 AI 消息并更新
            val index = messages.indexOfFirst { it.id == messageId && !it.isUser }
            if (index != -1) {
                messages[index] = messages[index].copy(
                    text = content,
                    isLoading = false
                )
            }
            currentState.copy(messages = messages)
        }
    }

    private fun removeLoadingAIMessage() {
        _state.update { currentState ->
            val messages = currentState.messages.filterNot { !it.isUser && it.isLoading }
            currentState.copy(messages = messages)
        }
    }

    private fun clearChat() {
        _state.value = _state.value.copy(
            messages = emptyList(),
            currentInput = "",
            errorMessage = null,
            isWaitingForResponse = false
        )
        viewModelScope.launch {
            try {
                chatRepository.clearAllMessages()
                messageIdCounter = 0L
                Log.d("AIChatViewModel", "Cleared all messages")
            } catch (e: Exception) {
                Log.e("AIChatViewModel", "Error clearing messages", e)
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
        currentAiRequestJob?.cancel()
        currentAiRequestJob = null
        _state.value = _state.value.copy(isWaitingForResponse = false)
        removeLoadingAIMessage()
    }

    fun updateInput(newInput: String) {
        _state.value = _state.value.copy(currentInput = newInput)
    }
}