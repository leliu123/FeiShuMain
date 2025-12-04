/**
 * 这是 Tab 的“注册中心”或“户籍系统”。
 * 它使用一个静态的 HashMap 来统一存储和管理 App 中所有可用的 Tab 实例。
 * 通过这种中心化的方式，UI 展示层（TabContainerScreen）可以方便地获取到所有已注册的 Tab，
 * 而无需关心这些 Tab 是在何处、何时被注册的。
 */
package com.feishu.mainfeature

import com.feishu.tabinterface.TabRegister

// 注册中心：用 HashMap 存所有 Tab
object TabRegistry {

    // key = route（字符串），value = TabRegister 实现类
    private val tabMap = LinkedHashMap<String, TabRegister>()

    // 注册一个 Tab
    fun register(tab: TabRegister) {
        tabMap[tab.descriptor.route] = tab
    }

    // 一次性注册多个
    fun registerAll(tabs: List<TabRegister>) {
        tabs.forEach { register(it) }
    }

    // 拿出所有 Tab
    fun getAll(): List<TabRegister> = tabMap.values.toList()

    // 通过 route 拿某个 Tab
    fun getByRoute(route: String): TabRegister? = tabMap[route]
}
