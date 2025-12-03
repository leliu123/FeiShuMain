package com.feishu

data class ChatMessage(
    val id: Long,
    val sender: String, // "me" or "ai"
    val content: String,
    val type: String = "text"
)
