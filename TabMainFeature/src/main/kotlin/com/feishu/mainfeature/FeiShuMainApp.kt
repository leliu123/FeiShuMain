package com.feishu.mainfeature

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.feishu.aichat.ui.ChatScreen
import com.feishu.mainfeature.navigation.ROUTE_AI_CHAT
import com.feishu.mainfeature.navigation.ROUTE_MAIN
import com.feishu.mainfeature.ui.TabContainerScreen

/**
 * Standalone entry point for the TabMainFeature module. This mirrors the host application's
 * navigation setup so previews/tests can exercise the tab container and the AI chat surface.
 */
@Composable
fun FeiShuMainApp() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = ROUTE_MAIN
    ) {
        composable(ROUTE_MAIN) {
            TabContainerScreen(navController = navController)
        }
        composable(ROUTE_AI_CHAT) {
            ChatScreen()
        }
    }
}
