package com.feishu.aichat.viewmodel

import com.feishu.aichat.data.ChatRepository
import com.feishu.aichat.intent.ChatIntent
import com.feishu.aichat.state.ChatState
import com.feishu.aichat.state.isIdle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ChatViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    @Mock
    private lateinit var chatRepository: ChatRepository
    private lateinit var chatViewModel: ChatViewModel
    private lateinit var mockitoAnnotations: AutoCloseable

    @Before
    fun setup() {
        // 设置测试调度器
        Dispatchers.setMain(testDispatcher)
        
        // 初始化Mockito注解
        mockitoAnnotations = MockitoAnnotations.openMocks(this)
        
        // 创建ChatViewModel实例
        chatViewModel = ChatViewModel(chatRepository)
    }

    @After
    fun tearDown() {
        // 关闭Mockito注解
        mockitoAnnotations.close()
        
        // 重置调度器
        Dispatchers.resetMain()
    }

    @Test
    fun `test SendMessage`() = runTest {
        val userMessageText = "Hello AI"
        chatViewModel.handleIntent(ChatIntent.SendMessage(userMessageText))
        //测试环境下真实网络请求的 Flow 无法被正常收集，所以需要Mock真实网络请求
        `when`(chatRepository.sendMessageStream(any(), any())).thenReturn(flowOf())

        // 验证开始状态
        var state = chatViewModel.state.value
        assertTrue(state.messages[0].isUser)
        assertEquals(userMessageText, state.messages[0].text)
        assertEquals("", state.currentInput)
        assertTrue(state.isWaitingForResponse)
        assertNull(state.errorMessage)

        // 开始执行（等待执行完成）
        advanceUntilIdle()
        
        // 验证结束状态
        state = chatViewModel.state.value
        assertEquals(2, state.messages.size)
        assertTrue(state.messages[0].isUser)
        assertEquals(userMessageText, state.messages[0].text)
        assertFalse(state.messages[1].isUser)
        assertTrue(state.messages[1].isLoading)
        assertEquals("", state.currentInput)
        assertFalse(state.isWaitingForResponse)
        assertNull(state.errorMessage)
    }

    @Test
    fun `test ClearChat`() = runTest {
        // 准备初始状态
        chatViewModel.updateInput("Hello")
        chatViewModel.handleIntent(ChatIntent.SendMessage("Hello"))
        advanceUntilIdle()
        
        // 处理Intent
        chatViewModel.handleIntent(ChatIntent.ClearChat)
        advanceUntilIdle()
        
        // 验证状态
        val state = chatViewModel.state.value
        assertEquals(emptyList(), state.messages)
        assertEquals("", state.currentInput)
        assertFalse(state.isWaitingForResponse)
        assertNull(state.errorMessage)
        
        // 验证仓库调用
        verify(chatRepository).clearAllMessages()
    }

    @Test
    fun `test StopRequesting`() = runTest {
        val userMessageText = "Hello"

        chatViewModel.handleIntent(ChatIntent.InitializeChat)
        chatViewModel.handleIntent(ChatIntent.SendMessage(userMessageText))
        
        // 验证初始状态
        var state = chatViewModel.state.value
        assertTrue(state.isWaitingForResponse)

        chatViewModel.handleIntent(ChatIntent.StopRequesting)
        
        // 验证最终中状态
        state = chatViewModel.state.value
        assertFalse(state.isWaitingForResponse)
    }

    @Test
    fun `test updateInput`() {
        val newInput = "New message"
        chatViewModel.updateInput(newInput)
        
        // 验证状态
        val state = chatViewModel.state.value
        assertEquals(newInput, state.currentInput)
    }

    @Test
    fun `test ChatState isIdle extension function`() {
        // 测试空闲状态
        val idleState = ChatState(isLoading = false, isWaitingForResponse = false)
        assertTrue(idleState.isIdle())
        
        // 测试加载状态
        val loadingState = ChatState(isLoading = true, isWaitingForResponse = false)
        assertFalse(loadingState.isIdle())
        
        // 测试等待响应状态
        val waitingState = ChatState(isLoading = false, isWaitingForResponse = true)
        assertFalse(waitingState.isIdle())
        
        // 测试同时处于加载和等待响应状态
        val busyState = ChatState(isLoading = true, isWaitingForResponse = true)
        assertFalse(busyState.isIdle())
    }
}
