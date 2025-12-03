package com.feishu

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

const val ROUTE_AI_ONCALL: String = "ai/oncall"

// --- State Holder Class ---
// Manages the state and logic of the screen
class AiOnCallState {
    val messages: SnapshotStateList<ChatMessage> = mutableStateListOf(*MockData.MOCK_MESSAGES.toTypedArray())
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    fun sendMessage(text: String) {
        if (text.isBlank()) return

        // Add user message
        messages.add(ChatMessage(id = System.currentTimeMillis(), sender = "me", content = text))

        // Simulate AI response
        coroutineScope.launch {
            delay(1500)
            val aiResponse = ChatMessage(
                id = System.currentTimeMillis(),
                sender = "ai",
                content = "收到关于“${text}”的问题。这是一个常见问题，建议您先检查相关模块的依赖和配置。"
            )
            messages.add(aiResponse)
        }
    }
}

@Composable
fun rememberAiOnCallState(): AiOnCallState {
    return remember { AiOnCallState() }
}

// --- UI ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIOnCallScreen(
    navController: NavHostController,
) {
    var text by remember { mutableStateOf("") }
    val state = rememberAiOnCallState() // Use the state holder
    val listState = rememberLazyListState()

    // Use LaunchedEffect for a more robust way to scroll when the list size changes
    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.lastIndex)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI OnCall") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        },
        bottomBar = {
            BottomInputBar(value = text, onValueChange = { text = it }) {
                state.sendMessage(text) // Delegate logic to the state holder
                text = ""
            }
        }
    ) { innerPadding ->
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(state.messages, key = { it.id }) { message ->
                MessageBubble(message)
            }
        }
    }
}

@Composable
fun BottomInputBar(value: String, onValueChange: (String) -> Unit, onSend: () -> Unit) {
    Surface(modifier = Modifier.fillMaxWidth(), shadowElevation = 8.dp) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("输入您的问题...") },
                shape = RoundedCornerShape(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = onSend, enabled = value.isNotBlank()) {
                Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = "发送")
            }
        }
    }
}

@Composable
fun MessageBubble(message: ChatMessage) {
    val isFromMe = message.sender == "me"
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isFromMe) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp).copy(
                bottomStart = if (!isFromMe) CornerSize(0.dp) else CornerSize(16.dp),
                bottomEnd = if (isFromMe) CornerSize(0.dp) else CornerSize(16.dp)
            ),
            color = if (isFromMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (isFromMe) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f, fill = false)
        ) {
            Text(text = message.content, modifier = Modifier.padding(12.dp))
        }
    }
}
