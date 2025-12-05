package com.feishu.mainfeature.state



import com.feishu.tabinterface.TabRegister

data class TabState(
    val tabs: List<TabRegister> = emptyList(),    // 所有已注册的 Tab
    val selectedTabId: String? = null,            // 当前选中的 Tab ID
    val isLoading: Boolean = false,               // 加载状态
    val error: String? = null                      // 错误信息
) {
    val selectedTab: TabRegister?                 // 当前选中的 Tab（只读）
        get() = selectedTabId?.let { tabId ->
            tabs.firstOrNull { it.descriptor.id == tabId }
        }
}