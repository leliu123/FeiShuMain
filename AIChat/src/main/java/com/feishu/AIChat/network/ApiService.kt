package com.feishu.AIChat.network

import com.feishu.AIChat.network.Request.ChatRequest
import com.feishu.AIChat.network.Response.ChatResponse
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    /**
     * 聊天完成接口
     * @param request 聊天请求参数
     * @return 流式响应的ResponseBody
     */
    @POST("/api/v3/chat/completions")
    fun chatCompletions(@Body request: ChatRequest): Call<ResponseBody>
    
    /**
     * 聊天完成接口（非流式）
     * @param request 聊天请求参数
     * @return 聊天响应
     */
    @POST("/api/v3/chat/completions")
    fun chatCompletionsNonStream(@Body request: ChatRequest): Call<ChatResponse>
}