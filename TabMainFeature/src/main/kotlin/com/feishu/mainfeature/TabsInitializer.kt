/**
 * 这是 App 的“初始化程序”。
 * 它的职责非常单一：在 App 启动时，将所有需要展示的 Tab 实例，
 * 调用 TabRegistry.register() 方法，将它们统一注册到“户籍系统”中。
 * 每当需要新增或移除一个主界面的 Tab 时，主要修改的就是这个文件。
 */
package com.feishu.mainfeature

import com.feishu.MessageTabRegister

// 整个 App 启动时调用一次，把所有 Tab 注册进去
fun initTabs() {
    TabRegistry.register(MessageTabRegister()) // The original Message Tab
    TabRegistry.register(CalendarTab())      // The new Calendar Tab
    TabRegistry.register(WorkplaceTab())     // The new Workplace Tab
    TabRegistry.register(ProfileTab())       // The new Profile Tab
}
