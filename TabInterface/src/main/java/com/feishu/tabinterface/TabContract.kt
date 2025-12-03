package com.feishu.tabinterface

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.compose.ui.graphics.vector.ImageVector

// 一张 Tab 的“名片”
data class TabDescriptor(
    val id: String,          // 用来排序、标识，比如 "message"
    val title: String,       // 底部导航显示的文字，比如 "消息"
    val icon: ImageVector,   // 底部导航的图标
    val route: String,       // 作为 HashMap 的 key，比如 "tab/message"
)

// 所有想接到 Tab 容器里的页面，都要实现这个接口
interface TabRegister {r
    val descriptor: TabDescriptor

    @Composable
    fun TopBar(navController: NavHostController)

    @Composable
    fun Content(navController: NavHostController)
}
