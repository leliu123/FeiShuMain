package com.feishu.tabfeatures.message
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.runtime.Composable

import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.feishu.tabinterface.FeiShuTitleBar
import com.feishu.tabinterface.TabDescriptor
import com.feishu.tabinterface.TabRegister
import com.feishu.tabfeatures.message.screen.MessageScreen
class MessageTabProvider : TabRegister{
    override val descriptor = TabDescriptor(
        id = "com.lea.feishutab.message",
        title = "消息",
        icon = Icons.Filled.Call,
        route = "message"
    )

    @Composable
    override fun TopBar(navController: NavHostController) {
        FeiShuTitleBar(
            title = descriptor.title
        )
    }

    @Composable
    override fun Content(navController: NavHostController) {


        MessageScreen()
    }
}