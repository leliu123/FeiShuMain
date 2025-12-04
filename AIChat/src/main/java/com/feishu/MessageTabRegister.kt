package com.feishu

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Message
import androidx.compose.material.icons.outlined.SmartToy
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
        CenterAlignedTopAppBar(
            title = { Text("消息") },
            actions = {
                IconButton(onClick = {
                    navController.navigate(ROUTE_AI_ONCALL)
                }) {
                    Icon(
                        imageVector = Icons.Outlined.SmartToy,
                        contentDescription = "AI OnCall"
                    )
                }
            }
        )
    }

    @Composable
    override fun Content(navController: NavHostController) {
        Text("这里是消息 Tab，可以在这里做消息列表。")
    }
}
