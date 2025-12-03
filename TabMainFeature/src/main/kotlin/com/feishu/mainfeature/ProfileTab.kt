package com.feishu.mainfeature

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.feishu.tabinterface.TabDescriptor
import com.feishu.tabinterface.TabRegister

class ProfileTab : TabRegister {
    override val descriptor: TabDescriptor = TabDescriptor(
        id = "profile",
        title = "我的",
        icon = Icons.Outlined.Person,
        route = "tab/profile"
    )

    @Composable
    override fun TopBar(navController: NavHostController) {
        // No top bar for this placeholder tab
    }

    @Composable
    override fun Content(navController: NavHostController) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "我的界面")
        }
    }
}
