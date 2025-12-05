package com.feishu.AIChat.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.feishu.AIChat.network.ApiService

class ChatViewModelFactory(private val apiService: ApiService) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            return ChatViewModel(apiService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}