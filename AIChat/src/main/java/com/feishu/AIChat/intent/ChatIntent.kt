package com.feishu.AIChat.intent

sealed class ChatIntent {
    // 初始化聊天室（获取历史记录）
    data object InitializeChat : ChatIntent()

    // 发送消息
    data class SendMessage(val message: String) : ChatIntent()

    // 清空聊天记录
    data object ClearChat : ChatIntent()

    // 重新加载
    data object Reload : ChatIntent()

    // 停止当前请求
    data object StopRequesting : ChatIntent()
}