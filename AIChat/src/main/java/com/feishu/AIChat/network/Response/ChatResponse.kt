package com.feishu.AIChat.network.Response

import com.feishu.AIChat.network.Request.Message
import com.google.gson.annotations.SerializedName

data class ChatResponse(
    @SerializedName("id")
    val id: String,
    @SerializedName("choices")
    val choices: List<Choice>,
    @SerializedName("created")
    val created: Long,
    @SerializedName("model")
    val model: String,
    @SerializedName("system_fingerprint")
    val systemFingerprint: String?,
    @SerializedName("object")
    val `object`: String,
    @SerializedName("usage")
    val usage: Usage?
)

data class Choice(
    @SerializedName("delta")
    val delta: Delta, // 流式响应时使用delta
    @SerializedName("message")
    val message: Message? = null, // 非流式响应时使用message
    @SerializedName("index")
    val index: Int,
    @SerializedName("finish_reason")
    val finishReason: String? // null表示未完成，stream结束时会有值
)

data class Delta(
    @SerializedName("role")
    val role: String? = null,
    @SerializedName("content")
    val content: String? = null
)

data class Usage(
    @SerializedName("prompt_tokens")
    val promptTokens: Int,
    @SerializedName("completion_tokens")
    val completionTokens: Int,
    @SerializedName("total_tokens")
    val totalTokens: Int
)

// 流式响应的包装类，用于处理SSE格式
data class StreamResponse(
    @SerializedName("data")
    val data: ChatResponse?,
    @SerializedName("error")
    val error: ErrorResponse?
)

data class ErrorResponse(
    @SerializedName("code")
    val code: String,
    @SerializedName("message")
    val message: String,
    @SerializedName("details")
    val details: List<String>?
)
