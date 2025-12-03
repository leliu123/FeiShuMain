package com.feishu.feishumain

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.feishu.AIOnCallScreen
import com.feishu.ROUTE_AI_ONCALL
import com.feishu.feishumain.ui.theme.FeiShuMainTheme
import com.feishu.mainfeature.TabContainerScreen
import com.feishu.mainfeature.initTabs

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Initialize and register all our Tabs before setting the content
        initTabs()

        enableEdgeToEdge()
        setContent {
            FeiShuMainTheme {
                // 2. Set up the app's navigation
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "main_screen") {
        // The main screen of our app is the Tab Container
        composable("main_screen") {
            TabContainerScreen(navController = navController)
        }
        // The destination for the AI chat screen
        composable(ROUTE_AI_ONCALL) {
            AIOnCallScreen(navController = navController)
        }
    }
}
