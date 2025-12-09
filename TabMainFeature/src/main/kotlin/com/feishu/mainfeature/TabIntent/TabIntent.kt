package com.feishu.mainfeature.TabIntent

import androidx.compose.ui.geometry.Offset

sealed class TabIntent {
    // 初始化 Tab 列表（启动时调用）
    object InitializeTabs : TabIntent()

    // 选择 Tab
    data class SelectTab(val tabId: String) : TabIntent()

    // 刷新 Tab 列表
    object RefreshTabs : TabIntent()

    // 进入/退出 AIChat（这里 ViewModel 暂不做导航，仅作为意图占位）
    object ToAIChat : TabIntent()
    object ExitAIChat : TabIntent()

    // 清除错误
    object ClearError : TabIntent()

    // 更新 AI 按钮位置
    data class UpdateFabPosition(val position: Offset) : TabIntent()
}