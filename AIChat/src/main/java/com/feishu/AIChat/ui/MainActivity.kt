package com.feishu.AIChat.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.lifecycle.ViewModelProvider
import com.feishu.AIChat.network.RetrofitClient
import com.feishu.AIChat.viewmodel.ChatViewModel
import com.feishu.AIChat.viewmodel.ChatViewModelFactory

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: ChatViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 初始化
        val factory = ChatViewModelFactory(RetrofitClient.apiService)
        viewModel = ViewModelProvider(this, factory).get(ChatViewModel::class.java)

        setContent {
            MaterialTheme {
                Surface {
                    ChatScreen(viewModel)
                }
            }
        }
    }
}