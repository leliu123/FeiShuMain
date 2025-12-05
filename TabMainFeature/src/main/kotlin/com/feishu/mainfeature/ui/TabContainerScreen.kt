package com.feishu.mainfeature.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.feishu.mainfeature.TabIntent.TabIntent
import com.feishu.mainfeature.TabViewModel.TabViewModel
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import android.widget.Toast
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat


@Composable
fun TabContainerScreen(
    navController: NavHostController,
    viewModel: TabViewModel = viewModel()
) {
    val context = LocalContext.current

    // 只发送Intent，不执行业务逻辑
    LaunchedEffect(Unit) {
        viewModel.dispatch(TabIntent.InitializeTabs)
    }

    val state by viewModel.state.collectAsState()

    //根据状态显示错误消息
    LaunchedEffect(state.error) {
        state.error?.let { errorMessage ->
            Toast.makeText(context, "错误: $errorMessage", Toast.LENGTH_SHORT).show()
        }
    }

    if (state.isLoading && state.tabs.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize() ,
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            Text("加载中...")
        }
        return
    }

    if (state.tabs.isEmpty()) {
        Box(
            modifier =  Modifier.fillMaxSize() , 
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            Text("没有注册的Tab")
        }
        return
    }

    val selectedId = state.selectedTabId ?: state.tabs.first().descriptor.id
    val currentTab = state.tabs.firstOrNull { it.descriptor.id == selectedId }

    Scaffold(
        topBar = {
            currentTab?.TopBar(navController)
        },
        bottomBar = {
            NavigationBar {
                state.tabs.forEach { tab ->
                    val selected = tab.descriptor.id == selectedId
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            //只发送Intent，不直接操作状态
                            viewModel.dispatch(TabIntent.SelectTab(tab.descriptor.id))
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
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    // 跳转到AI对话页面
                    navController.navigate("ai_chat")
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Chat,
                    contentDescription = "AI助手"
                )
            }
        }
    ) { innerPadding ->
        Box(Modifier.padding(innerPadding)) {
            if (state.error != null) {
                Text("错误: ${state.error}")
            } else {
                currentTab?.Content(navController)
            }
        }
    }
}





