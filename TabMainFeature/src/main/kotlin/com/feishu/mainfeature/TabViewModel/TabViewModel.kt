package com.feishu.mainfeature.TabViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.feishu.mainfeature.di.TabRegistry
import com.feishu.mainfeature.TabIntent.TabIntent
import com.feishu.mainfeature.state.TabState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TabViewModel : ViewModel() {
    private val _state = MutableStateFlow(TabState())
    val state: StateFlow<TabState> = _state.asStateFlow()

    fun dispatch(intent: TabIntent) {
        when (intent) {
            is TabIntent.InitializeTabs,
            is TabIntent.RefreshTabs -> {
                // 完全在ViewModel中处理业务逻辑
                viewModelScope.launch {
                    _state.value = _state.value.copy(isLoading = true, error = null)
                    try {
                        com.feishu.mainfeature.di.initTabs()
                        val tabs = TabRegistry.getAll()
                        val firstId = tabs.firstOrNull()?.descriptor?.id
                        _state.value = _state.value.copy(
                            tabs = tabs,
                            selectedTabId = _state.value.selectedTabId ?: firstId,
                            isLoading = false,
                            error = null
                        )
                    } catch (e: Exception) {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            error = e.message ?: "unknown error"
                        )
                    }
                }
            }

            is TabIntent.SelectTab -> {
                viewModelScope.launch {
                    // 只更新状态，UI根据状态自动更新
                    _state.value = _state.value.copy(selectedTabId = intent.tabId)
                }
            }

            is TabIntent.ClearError -> {
                viewModelScope.launch {
                    _state.value = _state.value.copy(error = null)
                }
            }

            is TabIntent.ToAIChat,
            is TabIntent.ExitAIChat -> {
                // 这里不处理导航
            }
        }
    }
}
