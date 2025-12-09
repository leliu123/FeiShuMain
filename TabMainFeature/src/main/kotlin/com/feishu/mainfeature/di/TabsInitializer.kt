package com.feishu.mainfeature.di

import com.feishu.mainfeature.tabs.CalendarTab
import com.feishu.mainfeature.tabs.ProfileTab

import com.feishu.tabfeatures.message.MessageTabProvider
import com.feishu.tabfeatures.stock.StockTabProvider

/**
 * 这是 App 的“初始化程序”。
 * 它的职责非常单一：在 App 启动时，将所有需要展示的 Tab 实例，
 * 调用 TabRegistry.register() 方法，将它们统一注册到“户籍系统”中。
 * 每当需要新增或移除一个主界面的 Tab 时，主要修改的就是这个文件。
// */
fun initTabs() {
    TabRegistry.register(MessageTabProvider()) // Message Tab (with enhanced UI)
    TabRegistry.register(CalendarTab())      // The new Calendar Tab
    TabRegistry.register(StockTabProvider()) // The new Stock Tab
    TabRegistry.register(ProfileTab())       // The new Profile Tab


}
