package com.feishu.tabinterface

import androidx.compose.runtime.Composable
import androidx.navigation.NavController

interface TabRegister {
    val descriptor: TabDescriptor
    @Composable
    fun TopBar(navController: NavController)
    @Composable
    fun Content(navController: NavController)
}