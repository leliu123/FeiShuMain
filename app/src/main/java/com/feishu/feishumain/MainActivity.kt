package com.feishu.feishumain

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.feishu.mainfeature.FeiShuMainApp
import com.feishu.feishumain.ui.theme.FeiShuMainTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FeiShuMainTheme {
                FeiShuMainApp()
            }
        }
    }
}