package com.feishu.tabinterface


import androidx.compose.material3.*
import androidx.compose.runtime.Composable

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

data class TitleBarAction(
    val icon: ImageVector,
    val label: String,
    val onClick: () -> Unit
)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeiShuTitleBar(
    modifier: Modifier = Modifier,
    title: String,
    actions: List<TitleBarAction> = emptyList(),


) {
    TopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge
            )
        },
        actions = {


            // 自定义操作按钮
            actions.forEach { action ->
                IconButton(onClick = action.onClick) {
                    Icon(
                        imageVector = action.icon,
                        contentDescription = action.label
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            actionIconContentColor = MaterialTheme.colorScheme.onSurface
        ),
        modifier = modifier
    )
}

