package com.feishu.aichat.state

import com.feishu.aichat.data.ChatMessage


data class ChatState(
    // 聊天消息列表
    val messages: List<ChatMessage> = emptyList(),

    // 正在加载
    val isLoading: Boolean = false,

    // 正在等待AI响应
    val isWaitingForResponse: Boolean = false,

    // 当前输入框内容
    val currentInput: String = "",

    // 错误信息
    val errorMessage: String? = null,

    // 初始化完成
    val isInitialized: Boolean = false
)

fun ChatState.isIdle(): Boolean = !isLoading && !isWaitingForResponse