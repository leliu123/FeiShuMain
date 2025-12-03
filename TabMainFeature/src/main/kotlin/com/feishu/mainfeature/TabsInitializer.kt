package com.feishu.mainfeature

import com.feishu.MessageTabRegister

// 整个 App 启动时调用一次，把所有 Tab 注册进去
fun initTabs() {
    TabRegistry.register(MessageTabRegister()) // The original Message Tab
    TabRegistry.register(CalendarTab())      // The new Calendar Tab
    TabRegistry.register(WorkplaceTab())     // The new Workplace Tab
    TabRegistry.register(ProfileTab())       // The new Profile Tab
}
