package com.feishu.mainfeature.ui
import android.widget.Toast
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavHostController
import com.feishu.mainfeature.TabIntent.TabIntent
import com.feishu.mainfeature.TabViewModel.TabViewModel
import com.feishu.mainfeature.navigation.ROUTE_AI_CHAT
import kotlinx.coroutines.flow.collectLatest
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.filled.AllInclusive
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.delay



@OptIn(ExperimentalAnimationApi::class)
@Composable
fun TabContainerScreen(
    navController: NavHostController,
    viewModel: TabViewModel = viewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val state by viewModel.state.collectAsState()

    // 监听错误消息
    LaunchedEffect(lifecycleOwner.lifecycle) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.state.collectLatest { tabState ->
                tabState.error?.let { errorMessage ->
                    Toast.makeText(
                        context,
                        "错误: $errorMessage",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    // 初始化Tab
    LaunchedEffect(Unit) {
        viewModel.dispatch(TabIntent.InitializeTabs)
    }

    Scaffold(
        topBar = {
            // 顶部栏
            state.selectedTab?.TopBar(navController = navController)
        },
        bottomBar = {
            // 底部导航栏 - 使用动画版本
            AnimatedBottomNavigationBar(
                tabs = state.tabs,
                selectedTabId = state.selectedTabId,
                onTabSelected = { tabId ->
                    viewModel.dispatch(TabIntent.SelectTab(tabId))
                }
            )
        },
        floatingActionButton = {
            // AI助手按钮 - 带动画
            AnimatedFloatingActionButton(
                onClick = {
                    navController.navigate(ROUTE_AI_CHAT)
                }
            )
        }
    ) { innerPadding ->
        // 使用动画化的Tab内容
        AnimatedTabContent(
            state = state,
            navController = navController,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

/**
 * 动画底部导航栏
 */
@Composable
private fun AnimatedBottomNavigationBar(
    tabs: List<com.feishu.tabinterface.TabRegister>,
    selectedTabId: String?,
    onTabSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(modifier = modifier) {
        tabs.forEach { tab ->
            val isSelected = tab.descriptor.id == selectedTabId

            // 动画颜色
            val iconColor by animateColorAsState(
                targetValue = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                animationSpec = tween(durationMillis = 300)
            )

            val textColor by animateColorAsState(
                targetValue = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                animationSpec = tween(durationMillis = 300)
            )

            // 缩放动画
            val scale by animateFloatAsState(
                targetValue = if (isSelected) 1.2f else 1f,
                animationSpec = tween(durationMillis = 300)
            )

            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(tab.descriptor.id) },
                icon = {
                    Icon(
                        imageVector = tab.descriptor.icon,
                        contentDescription = tab.descriptor.title,
                        modifier = Modifier.graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                        },
                        tint = iconColor
                    )
                },
                label = {
                    Text(
                        text = tab.descriptor.title,
                        color = textColor
                    )
                }
            )
        }
    }
}
/**
 * 动画Tab内容切换 - 根据切换方向动态改变动画方向
 */
@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun AnimatedTabContent(
    state: com.feishu.mainfeature.state.TabState,
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val currentTabId = state.selectedTabId ?: state.tabs.firstOrNull()?.descriptor?.id

    // 记录上一次的Tab ID用于判断方向
    var previousTabId by remember { mutableStateOf(currentTabId) }

    // 计算切换方向：true表示向前（向右滑出），false表示向后（向左滑出）
    val isForward = remember(currentTabId) {
        if (previousTabId == null || currentTabId == null) {
            true
        } else {
            // 根据Tab索引判断方向
            val currentIndex = state.tabs.indexOfFirst { it.descriptor.id == currentTabId }
            val previousIndex = state.tabs.indexOfFirst { it.descriptor.id == previousTabId }
            currentIndex > previousIndex
        }.also {
            // 更新前一个Tab ID
            previousTabId = currentTabId
        }
    }

    AnimatedContent(
        targetState = currentTabId,
        transitionSpec = {
            if (isForward) {
                // 向前切换：新内容从右滑入，旧内容向左滑出
                slideInHorizontally(
                    animationSpec = tween(300),
                    initialOffsetX = { fullWidth -> fullWidth }
                ) togetherWith slideOutHorizontally(
                    animationSpec = tween(300),
                    targetOffsetX = { fullWidth -> -fullWidth }
                )
            } else {
                // 向后切换：新内容从左滑入，旧内容向右滑出
                slideInHorizontally(
                    animationSpec = tween(300),
                    initialOffsetX = { fullWidth -> -fullWidth }
                ) togetherWith slideOutHorizontally(
                    animationSpec = tween(300),
                    targetOffsetX = { fullWidth -> fullWidth }
                )
            }
        },
        label = "Tab切换动画"
    ) { tabId ->
        Box(modifier = modifier.fillMaxSize()) {
            when {
                state.isLoading -> {
                    LoadingWithAnimation(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                state.error != null -> {
                    Text(
                        text = "错误: ${state.error}",
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.error
                    )
                }
                else -> {
                    val tab = state.tabs.firstOrNull { it.descriptor.id == tabId }
                    tab?.Content(navController)
                }
            }
        }
    }
}
/**
 * 动画悬浮按钮
 */
@Composable
private fun AnimatedFloatingActionButton(
    onClick: () -> Unit
) {
    var isRotating by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(
        targetValue = if (isRotating) 360f else 0f,
        animationSpec = tween(durationMillis = 600)
    )

    FloatingActionButton(
        onClick = {
            isRotating = !isRotating
            onClick()
        },
        modifier = Modifier.graphicsLayer {
            rotationZ = rotation
        },
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary
    ) {
        Icon(
            imageVector = Icons.Filled.AllInclusive    ,
            contentDescription = "AI助手"
        )
    }
}

/**
 * 带动画的加载指示器
 */
@Composable
private fun LoadingWithAnimation(modifier: Modifier = Modifier) {
    // 旋转动画
    var rotation by remember { mutableStateOf(0f) }

    LaunchedEffect(Unit) {
        while (true) {
            rotation += 360f
            delay(1000)
        }
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 旋转的进度条
        CircularProgressIndicator(
            modifier = Modifier
                .size(48.dp)
                .graphicsLayer {
                    rotationZ = rotation
                }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 脉冲动画的文字
        var alpha by remember { mutableStateOf(0.3f) }

        LaunchedEffect(Unit) {
            while (true) {
                alpha = 0.8f
                delay(600)
                alpha = 0.3f
                delay(600)
            }
        }

        Text(
            text = "加载中...",
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha)
        )
    }
}
