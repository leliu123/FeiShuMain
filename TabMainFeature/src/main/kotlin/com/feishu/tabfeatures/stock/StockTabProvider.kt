package com.feishu.tabfeatures.stock
import androidx.compose.material.icons.outlined.AreaChart
import androidx.compose.material.icons.Icons

import androidx.compose.runtime.Composable

import androidx.navigation.NavHostController
import com.feishu.tabfeatures.stock.ui.StockScreen
import com.feishu.tabinterface.FeiShuTitleBar
import com.feishu.tabinterface.TabDescriptor
import com.feishu.tabinterface.TabRegister


class StockTabProvider : TabRegister {
    override val descriptor = TabDescriptor(
        id = "com.lea.feishutab.stock",
        title = "股票",
        icon = Icons.Outlined.AreaChart,
        route = "stock"
    )

    @Composable
    override fun TopBar(navController: NavHostController) {
        FeiShuTitleBar(
            title = descriptor.title
        )
    }

    @Composable
    override fun Content(navController: NavHostController) {
        StockScreen()
    }
}
