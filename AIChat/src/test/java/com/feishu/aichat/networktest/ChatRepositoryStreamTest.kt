package com.feishu.aichat.networktest
import org.junit.Test
import kotlinx.coroutines.test.runTest
import com.feishu.aichat.data.network.ApiService
import com.feishu.aichat.data.network.ChatRequest
import com.feishu.aichat.data.ChatRepository
import org.junit.Assert.*
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest


import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before

import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ChatRepositoryStreamTest {

    private lateinit var server: MockWebServer
    private lateinit var api: ApiService
    private lateinit var repo: ChatRepository

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()

        val client = OkHttpClient.Builder().build()
        api = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
            .create(ApiService::class.java)

        repo = ChatRepository(apiService = api, database = null)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `sendMessageStream connects and parses SSE chunks`() = runTest {
        val ssePayload = """
            data: {"choices":[{"delta":{"content":"Hi"},"index":0}]}
            
            data: {"choices":[{"delta":{"content":"!"},"index":0}]}
            
            data: [DONE]
        """.trimIndent()

        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "text/event-stream")
                .setChunkedBody(ssePayload, 8)
        )

        val results = repo.sendMessageStream(
            userMessage = "hello",
            chatHistory = emptyList()
        ).toList()

        assertTrue(results.all { it.isSuccess })
        val contents = results.mapNotNull { it.getOrNull() }
        assertEquals(listOf("Hi", "!"), contents)

        val req: RecordedRequest = server.takeRequest()
        assertEquals("/api/v3/chat/completions", req.path)
        assertEquals("text/event-stream", req.getHeader("Accept"))
    }
}