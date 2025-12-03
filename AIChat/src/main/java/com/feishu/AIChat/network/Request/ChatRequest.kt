package com.feishu.AIChat.network.Request

import com.google.gson.annotations.SerializedName

data class ChatRequest(
    @SerializedName("model")
    val model: String,
    @SerializedName("messages")
    val messages: List<Message>,
    @SerializedName("stream")
    val stream: Boolean = true
)

data class Message(
    @SerializedName("role")
    val role: String, // system, user, assistant, tool
    @SerializedName("content")
    val content: Any // 可以是String或List<ContentItem>
)

sealed class ContentItem {
    @SerializedName("type")
    open val type: String = ""
}

data class TextContent(
    @SerializedName("type")
    override val type: String = "text",
    @SerializedName("text")
    val text: String
) : ContentItem()

data class ImageContent(
    @SerializedName("type")
    override val type: String = "image_url",
    @SerializedName("image_url")
    val imageUrl: ImageUrl
) : ContentItem()

data class ImageUrl(
    @SerializedName("url")
    val url: String, // 图片链接或Base64编码
    @SerializedName("detail")
    val detail: String = "low" // high或low
)

data class VideoContent(
    @SerializedName("type")
    override val type: String = "video_url",
    @SerializedName("video_url")
    val videoUrl: VideoUrl
) : ContentItem()

data class VideoUrl(
    @SerializedName("url")
    val url: String, // 视频链接或Base64编码
    @SerializedName("fps")
    val fps: Float = 1.0f // 抽帧频率，范围[0.2, 5]
)
