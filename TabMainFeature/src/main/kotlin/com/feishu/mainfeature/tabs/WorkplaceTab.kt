package com.feishu.mainfeature.tabs

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.feishu.tabinterface.TabDescriptor
import com.feishu.tabinterface.TabRegister

/**
 * "工作台" Tab 的占位符实现。
 *
 * 这是一个最简单的 Tab 示例，它不包含任何复杂的业务逻辑，
 * 仅用于在主导航栏中占据一个位置，并显示一个简单的文本界面。
 * 这种方式非常适合在应用开发的早期阶段，快速搭建起整体的UI框架。
 */
class WorkplaceTab : TabRegister {
    // 定义此 Tab 的“名片”，包括标题、图标和路由路径
    override val descriptor: TabDescriptor = TabDescriptor(
        id = "workplace",
        title = "工作台",
        icon = Icons.Outlined.GridView,
        route = "tab/workplace"
    )

    // 这个占位符 Tab 不需要复杂的顶部导航栏
    @Composable
    override fun TopBar(navController: NavHostController) {
        // No top bar for this placeholder tab
    }

    // 定义此 Tab 的主内容，这里只是一个居中的文本
    @Composable
    override fun Content(navController: NavHostController) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "工作台界面")
        }
    }
}
