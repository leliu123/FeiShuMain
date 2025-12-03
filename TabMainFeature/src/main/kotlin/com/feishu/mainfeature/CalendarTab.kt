package com.feishu.mainfeature

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.feishu.tabinterface.TabDescriptor
import com.feishu.tabinterface.TabRegister

class CalendarTab : TabRegister {
    override val descriptor: TabDescriptor = TabDescriptor(
        id = "calendar",
        title = "日历",
        icon = Icons.Outlined.CalendarToday,
        route = "tab/calendar"
    )

    @Composable
    override fun TopBar(navController: NavHostController) {
        // No top bar for this placeholder tab
    }

    @Composable
    override fun Content(navController: NavHostController) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "日历界面")
        }
    }
}
