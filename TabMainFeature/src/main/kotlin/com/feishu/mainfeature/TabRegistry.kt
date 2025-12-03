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
