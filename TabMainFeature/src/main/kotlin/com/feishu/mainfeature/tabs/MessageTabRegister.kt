package com.feishu.mainfeature.tabs

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Message
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.feishu.tabinterface.TabDescriptor
import com.feishu.tabinterface.TabRegister

@OptIn(ExperimentalMaterial3Api::class)
class MessageTabRegister : TabRegister {
    override val descriptor: TabDescriptor = TabDescriptor(
        id = "message",
        title = "消息",
        icon = Icons.Outlined.Message,
        route = "tab/message",
    )

    @Composable
    override fun TopBar(navController: NavHostController) {
        // No top bar for this tab
    }

    @Composable
    override fun Content(navController: NavHostController) {
        Text("这里是消息 Tab，可以在这里做消息列表。")
    }
}
