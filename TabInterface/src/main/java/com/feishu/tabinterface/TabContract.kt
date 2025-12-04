/**
 * 这个文件定义了 Tab 模块化架构的“协议”或“契约”。
 * 它包含了所有想被集成到主界面的 Tab 必须遵守的数据结构和接口。
 * 这个模块本身不包含任何具体实现，只定义标准，以实现最大程度的解耦。
 */
package com.feishu.tabinterface

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController

// 一张 Tab 的“名片”，定义了 Tab 的所有静态信息
data class TabDescriptor(
    val id: String,          // 用来排序、标识，比如 "message"
    val title: String,       // 底部导航显示的文字，比如 "消息"
    val icon: ImageVector,   // 底部导航的图标
    val route: String,       // 作为 HashMap 的 key 和导航路径，比如 "tab/message"
)

// 所有想被集成到 Tab 容器里的页面，都必须实现这个接口
// 它定义了一个 Tab 必须具备的动态能力（提供UI界面）
interface TabRegister {
    // Tab 必须提供自己的“名片”信息
    val descriptor: TabDescriptor

    // Tab 必须能自己决定顶部栏（TopBar）长啥样
    @Composable
    fun TopBar(navController: NavHostController)

    // Tab 必须能自己决定核心内容区（Content）长啥样
    @Composable
    fun Content(navController: NavHostController)
}
