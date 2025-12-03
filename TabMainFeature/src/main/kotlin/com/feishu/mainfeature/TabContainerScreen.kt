package com.feishu.mainfeature

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.feishu.tabinterface.TabRegister

// 整个 Tab 容器页面
@Composable
fun TabContainerScreen(
    navController: NavHostController,
) {
    // 从注册中心拿所有 Tab
    val tabs: List<TabRegister> = remember {
        TabRegistry.getAll()
    }

    if (tabs.isEmpty()) {
        Text("No tabs registered. Did you call initTabs() ?")
        return
    }

    // 当前选中的 Tab 的 route
    var currentRoute by rememberSaveable {
        mutableStateOf(tabs.first().descriptor.route)
    }

    val currentTab = tabs.firstOrNull { it.descriptor.route == currentRoute }

    Scaffold(
        topBar = {
            // 当前 Tab 画自己的 TopBar
            currentTab?.TopBar(navController)
        },
        bottomBar = {
            // 底部导航栏
            NavigationBar {
                tabs.forEach { tab ->
                    val selected = tab.descriptor.route == currentRoute
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            // 切换当前 Tab
                            currentRoute = tab.descriptor.route
                        },
                        icon = {
                            Icon(
                                imageVector = tab.descriptor.icon,
                                contentDescription = tab.descriptor.title
                            )
                        },
                        label = {
                            Text(tab.descriptor.title)
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(Modifier.padding(innerPadding)) {
            // 当前 Tab 画自己的 Content
            currentTab?.Content(navController)
        }
    }
}
