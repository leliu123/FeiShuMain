package com.feishu.mainfeature.ui
import android.widget.Toast
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext

import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
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
        }
    ) { innerPadding ->
        // 使用动画化的Tab内容
        Box(modifier = Modifier.fillMaxSize()) {
            AnimatedTabContent(
                state = state,
                navController = navController,
                modifier = Modifier.padding(innerPadding)
            )
            
            // 可拖拽的 AI 按钮（在内容区域内）
            Box(modifier = Modifier.padding(innerPadding)) {
                DraggableFloatingActionButton(
                    position = state.fabPosition,
                    onPositionChange = { newPosition ->
                        viewModel.dispatch(TabIntent.UpdateFabPosition(newPosition))
                    },
                    onClick = {
                        navController.navigate(ROUTE_AI_CHAT)
                    }
                )
            }
        }
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
 * 可拖拽的悬浮按钮
 */
@Composable
private fun DraggableFloatingActionButton(
    position: Offset?,
    onPositionChange: (Offset) -> Unit,
    onClick: () -> Unit
) {
    val density = LocalDensity.current
    
    // FAB 默认大小（56.dp）
    val fabSize = with(density) { 56.dp.toPx() }
    val fabRadius = fabSize / 2f
    
    var currentPosition by remember { mutableStateOf<Offset?>(null) }
    var isDragging by remember { mutableStateOf(false) }
    var containerSize by remember { mutableStateOf(IntSize.Zero) }
    
    // 限制位置在容器范围内（边缘对齐，不是中心对齐）
    fun constrainPosition(offset: Offset): Offset {
        // 按钮中心点的约束范围
        // 左边缘不能超出：pos.x - fabRadius >= 0，所以 pos.x >= fabRadius
        // 右边缘不能超出：pos.x + fabRadius <= containerSize.width，所以 pos.x <= containerSize.width - fabRadius
        // 上边缘不能超出：pos.y - fabRadius >= 0，所以 pos.y >= fabRadius
        // 下边缘不能超出：pos.y + fabRadius <= containerSize.height，所以 pos.y <= containerSize.height - fabRadius
        val minX = fabRadius
        val maxX = containerSize.width - fabRadius
        val minY = fabRadius
        val maxY = containerSize.height - fabRadius
        
        return Offset(
            offset.x.coerceIn(minX, maxX),
            offset.y.coerceIn(minY, maxY)
        )
    }
    
    // 计算默认位置（右下角，底部和右边缘贴着容器边缘）
    fun getDefaultPosition(): Offset {
        val horizontalPadding = with(density) { 16.dp.toPx() }
        val verticalPadding = with(density) { 80.dp.toPx() } // 增加底部距离，让按钮更靠上
        // 右边缘距离右边界 padding：pos.x + fabRadius = containerSize.width - padding
        // 所以 pos.x = containerSize.width - fabRadius - padding
        val defaultX = containerSize.width - fabRadius - horizontalPadding
        // 底边缘距离底边界 padding：pos.y + fabRadius = containerSize.height - padding
        // 所以 pos.y = containerSize.height - fabRadius - padding
        val defaultY = containerSize.height - fabRadius - verticalPadding
        return constrainPosition(Offset(defaultX, defaultY))
    }
    
    // 初始化或更新位置
    LaunchedEffect(containerSize, position) {
        if (containerSize.width > 0 && containerSize.height > 0) {
            val targetPosition = position ?: getDefaultPosition()
            if (currentPosition == null || position != null) {
                currentPosition = constrainPosition(targetPosition)
            }
        }
    }
    
    var rotation by remember { mutableStateOf(0f) }
    val animatedRotation by animateFloatAsState(
        targetValue = if (isDragging) 360f else rotation,
        animationSpec = tween(durationMillis = 600)
    )
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned { coordinates ->
                containerSize = coordinates.size
            }
    ) {
        currentPosition?.let { pos ->
            FloatingActionButton(
                onClick = {
                    if (!isDragging) {
                        rotation += 360f
                        onClick()
                    }
                },
                modifier = Modifier
                    .offset(
                        x = with(density) { (pos.x - fabRadius).toDp() },
                        y = with(density) { (pos.y - fabRadius).toDp() }
                    )
                    .pointerInput(Unit) {
                        detectDragGesturesAfterLongPress(
                            onDragStart = {
                                isDragging = true
                            },
                            onDragEnd = {
                                isDragging = false
                                // 保存最终位置
                                onPositionChange(currentPosition!!)
                            }
                        ) { change, dragAmount ->
                            val newPosition = constrainPosition(currentPosition!! + dragAmount)
                            currentPosition = newPosition
                        }
                    }
                    .graphicsLayer {
                        rotationZ = animatedRotation
                    },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = Icons.Filled.AllInclusive,
                    contentDescription = "AI助手"
                )
            }
        }
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
