package com.feishu.AIChat.network

import com.feishu.AIChat.network.Response.ChatResponse
import com.google.gson.Gson
import okhttp3.ResponseBody
import java.io.IOException

/**
 * 流式响应解析器
 * 用于处理服务器发送的事件(SSE)格式的响应
 */
class StreamResponseParser(private val listener: StreamEventListener) {
    private val gson = Gson()

    /**
     * 解析ResponseBody为流式响应
     * @param responseBody 响应体
     */
    fun parse(responseBody: ResponseBody) {
        try {
            val source = responseBody.source().buffer()
            while (!source.exhausted()) {
                val line = source.readUtf8Line() ?: break
                if (line.isNotEmpty() && line.startsWith("data: ")) {
                    val data = line.substring(6)
                    if (data == "[DONE]") {
                        listener.onComplete()
                        break
                    }
                    try {
                        val chatResponse = gson.fromJson(data, ChatResponse::class.java)
                        listener.onResponse(chatResponse)
                    } catch (e: Exception) {
                        listener.onError(e)
                    }
                }
            }
        } catch (e: IOException) {
            listener.onError(e)
        } finally {
            responseBody.close()
        }
    }

    /**
     * 流式事件监听器
     */
    interface StreamEventListener {
        /**
         * 接收到响应
         * @param response 聊天响应
         */
        fun onResponse(response: ChatResponse)

        /**
         * 完成
         */
        fun onComplete()

        /**
         * 发生错误
         * @param error 错误信息
         */
        fun onError(error: Throwable)
    }
}
