package com.feishu.tabinterface

import androidx.compose.ui.graphics.vector.ImageVector

data class TabDescriptor(
    val id: String,
    val title: String,
    val icon: ImageVector,
    val route: String = "",
)
