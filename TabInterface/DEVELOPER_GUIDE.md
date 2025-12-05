# TabInterface 模块开发文档



## 模块概述

`TabInterface` 模块定义了模块化 Tab 架构的契约，用于将不同功能模块集成到主界面。该模块只定义标准，不包含具体实现，实现解耦。

### 核心特性

- **契约式设计**：通过接口定义标准，实现模块间解耦
- **可扩展性**：轻松添加新的 Tab 模块，无需修改现有代码
- **统一管理**：通过 `TabDescriptor` 统一管理 Tab 元数据
- **UI 复用**：提供 `FeiShuTitleBar` 组件，保持 UI 一致性

---

## 架构设计

### 架构图

```
┌─────────────────────────────────────────────────────────┐
│                    MainActivity                         │
│  ┌───────────────────────────────────────────────────┐  │
│  │          TabContainerScreen (UI 容器)              │  │
│  │  ┌──────────────┐  ┌──────────────┐             │  │
│  │  │   TopBar     │  │   Content    │             │  │
│  │  │  (动态渲染)   │  │  (动态渲染)   │             │  │
│  │  └──────────────┘  └──────────────┘             │  │
│  │  ┌─────────────────────────────────────────────┐  │  │
│  │  │         Bottom Navigation Bar               │  │  │
│  │  └─────────────────────────────────────────────┘  │  │
│  └───────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────┐
│              TabRegistry (注册中心)                      │
│  ┌───────────────────────────────────────────────────┐  │
│  │  HashMap<String, TabRegister>                     │  │
│  │  - register(tab: TabRegister)                    │  │
│  │  - getAll(): List<TabRegister>                   │  │
│  │  - getByRoute(route: String): TabRegister?       │  │
│  └───────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────┐
│            TabRegister 实现类                            │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐ │
│  │ MessageTab   │  │ CalendarTab  │  │ ProfileTab   │ │
│  │              │  │              │  │              │ │
│  │ descriptor   │  │ descriptor   │  │ descriptor   │ │
│  │ TopBar()     │  │ TopBar()     │  │ TopBar()     │ │
│  │ Content()    │  │ Content()    │  │ Content()    │ │
│  └──────────────┘  └──────────────┘  └──────────────┘ │
└─────────────────────────────────────────────────────────┘
```

### 工作流程

1. **初始化阶段**：应用启动时，调用 `initTabs()` 注册所有 Tab
2. **注册阶段**：每个 Tab 通过 `TabRegistry.register()` 注册到中心
3. **展示阶段**：`TabContainerScreen` 从注册中心获取所有 Tab，动态渲染 UI
4. **切换阶段**：用户点击底部导航栏，切换当前显示的 Tab

### 关键组件

| 组件 | 位置 | 职责 |
|------|------|------|
| `TabInterface` | 本模块 | 定义契约（接口和数据类） |
| `TabRegistry` | TabMainFeature | Tab 注册中心，管理所有 Tab 实例 |
| `TabContainerScreen` | TabMainFeature | UI 容器，负责展示和切换 Tab |
| `TabsInitializer` | TabMainFeature | 初始化函数，注册所有 Tab |

---

## TabContract.kt - 契约定义

### 1. TabDescriptor（数据类）

Tab 的静态信息描述，类似于 Tab 的"身份证"。

#### 变量定义

| 变量名 | 类型 | 说明 | 示例值 |
|--------|------|------|--------|
| `id` | `String` | Tab 的唯一标识，用于排序和识别 | `"message"`, `"contacts"`, `"settings"` |
| `title` | `String` | 底部导航栏显示的文字 | `"消息"`, `"联系人"`, `"设置"` |
| `icon` | `ImageVector` | 底部导航栏显示的图标 | Material Icons 中的图标向量 |
| `route` | `String` | 导航路径，作为 HashMap 的 key，必须唯一 | `"tab/message"`, `"tab/contacts"` |

#### 作用

- **统一元数据格式**：所有 Tab 使用相同的数据结构描述
- **支持导航路由**：`route` 用于导航和注册中心查找
- **底部导航栏展示**：`title` 和 `icon` 用于底部导航栏显示
- **便于排序和管理**：`id` 可用于排序和标识

#### 使用示例

```kotlin
val descriptor = TabDescriptor(
    id = "message",
    title = "消息",
    icon = Icons.Outlined.Message,
    route = "tab/message"
)
```

#### 注意事项

- `route` 必须唯一，建议使用 `"tab/{功能名}"` 格式
- `id` 建议使用小写字母和下划线，如 `"message"`, `"work_place"`
- `icon` 可以使用 Material Icons 的 `Icons.Default.*` 或 `Icons.Outlined.*`

---

### 2. TabRegister（接口）

所有需要集成到 Tab 容器的页面必须实现此接口。这是 Tab 模块的"契约"。

#### 接口定义

```kotlin
interface TabRegister {
    val descriptor: TabDescriptor
    
    @Composable
    fun TopBar(navController: NavHostController)
    
    @Composable
    fun Content(navController: NavHostController)
}
```

#### 成员说明

| 成员 | 类型 | 说明 | 是否必需 |
|------|------|------|---------|
| `descriptor` | `TabDescriptor` | Tab 的描述信息（只读属性） | ✅ 必需 |
| `TopBar()` | `@Composable` | 提供顶部栏 UI，接收 `NavHostController` 用于导航 | ✅ 必需（可为空实现） |
| `Content()` | `@Composable` | 提供核心内容区 UI，接收 `NavHostController` 用于导航 | ✅ 必需 |

#### 作用

- **统一能力标准**：所有 Tab 必须提供相同的能力
- **强制实现**：确保每个 Tab 都有顶部栏和内容区
- **支持导航**：通过 `NavHostController` 实现页面间导航
- **解耦设计**：Tab 模块不依赖主应用，只依赖接口

#### 实现要求

- `descriptor` 必须是不可变的，建议使用 `val` 定义
- `TopBar()` 可以为空实现（不显示顶部栏），但必须实现
- `Content()` 必须实现，这是 Tab 的核心内容

---

## FeiShuTitleBar.kt - 标题栏组件

### 1. TitleBarAction（数据类）

定义标题栏操作按钮的数据结构。

#### 变量定义

| 变量名 | 类型 | 说明 | 示例 |
|--------|------|------|------|
| `icon` | `ImageVector` | 按钮图标，使用 Material Icons | `Icons.Default.Search` |
| `label` | `String` | 按钮描述，用于无障碍访问 | `"搜索"`, `"设置"` |
| `onClick` | `() -> Unit` | 点击回调函数 | `{ /* 执行操作 */ }` |

#### 作用

- **统一操作按钮结构**：所有操作按钮使用相同的数据结构
- **支持多个按钮**：可以传递多个 `TitleBarAction` 到 `actions` 列表
- **无障碍支持**：`label` 用于屏幕阅读器

#### 使用示例

```kotlin
val searchAction = TitleBarAction(
    icon = Icons.Default.Search,
    label = "搜索",
    onClick = { 
        // 执行搜索逻辑
        performSearch()
    }
)
```

---

### 2. FeiShuTitleBar（Composable 函数）

可复用的标题栏组件，基于 Material3 的 `TopAppBar`。

#### 函数签名

```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeiShuTitleBar(
    modifier: Modifier = Modifier,
    title: String,
    actions: List<TitleBarAction> = emptyList(),
    
)
```

#### 参数说明

| 参数名 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| `modifier` | `Modifier` | `Modifier` | 布局修饰符，用于自定义样式 |
| `title` | `String` | - | 标题文字，显示在标题栏中央 |
| `actions` | `List<TitleBarAction>` | `emptyList()` | 操作按钮列表，显示在标题栏右侧 |


#### 功能特性

- **Material3 设计**：使用 `TopAppBar` 组件，遵循 Material Design 规范
- **主题适配**：自动适配 Material Theme 的颜色方案
- **自定义标题**：支持任意字符串标题
- **多个操作按钮**：支持在右侧添加多个操作按钮
- **无障碍支持**：所有按钮都有 `contentDescription`

#### 样式说明

标题栏使用以下 Material Theme 颜色：
- `containerColor`: `MaterialTheme.colorScheme.surface`
- `titleContentColor`: `MaterialTheme.colorScheme.onSurface`
- `actionIconContentColor`: `MaterialTheme.colorScheme.onSurface`

#### 使用示例

```kotlin
FeiShuTitleBar(
    title = "消息",
    actions = listOf(
        TitleBarAction(
            icon = Icons.Default.Search,
            label = "搜索",
            onClick = { /* 搜索逻辑 */ }
        ),
        TitleBarAction(
            icon = Icons.Default.MoreVert,
            label = "更多",
            onClick = { /* 更多选项 */ }
        )
    ),
    modifier = Modifier.fillMaxWidth()
)
```

---

## 开发示例

### 示例 1：创建一个简单的 Tab

创建一个最简单的 Tab，不显示顶部栏：

```kotlin

class SimpleTab : TabRegister {
    override val descriptor = TabDescriptor(
        id = "simple",
        title = "简单",
        icon = Icons.Outlined.Message,
        route = "tab/simple"
    )

    @Composable
    override fun TopBar(navController: NavHostController) {
        // 不显示顶部栏，空实现即可
    }

    @Composable
    override fun Content(navController: NavHostController) {
        Text("这是一个简单的 Tab")
    }
}
```

---

### 示例 2：创建一个带标题栏的 Tab

使用 `FeiShuTitleBar` 创建带标题栏的 Tab：

```kotlin


class MessageTab : TabRegister {
    override val descriptor = TabDescriptor(
        id = "message",
        title = "消息",
        icon = Icons.Outlined.Message,
        route = "tab/message"
    )

    @Composable
    override fun TopBar(navController: NavHostController) {
        FeiShuTitleBar(
            title = "消息",
            actions = listOf(
                TitleBarAction(
                    icon = Icons.Default.Search,
                    label = "搜索",
                    onClick = { 
                        // 导航到搜索页面
                        navController.navigate("search")
                    }
                ),
                TitleBarAction(
                    icon = Icons.Default.Add,
                    label = "新建",
                    onClick = { 
                        // 显示新建消息对话框
                        showNewMessageDialog()
                    }
                )
            )
        )
    }

    @Composable
    override fun Content(navController: NavHostController) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "消息列表",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(16.dp))
            MessageList(navController)
        }
    }
}

@Composable
fun MessageList(navController: NavHostController) {
    LazyColumn {
        items(10) { index ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                onClick = {
                    // 点击消息项，导航到详情页
                    navController.navigate("message/$index")
                }
            ) {
                Text(
                    text = "消息 $index",
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}
```

---





### 示例 3：使用自定义标题栏

如果 `FeiShuTitleBar` 不满足需求，可以实现自定义标题栏：

```kotlin
@Composable
override fun TopBar(navController: NavHostController) {
    TopAppBar(
        title = { Text("自定义标题") },
        navigationIcon = {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ArrowBack, "返回")
            }
        },
        actions = {
            IconButton(onClick = { /* 操作 */ }) {
                Icon(Icons.Default.MoreVert, "更多")
            }
        }
    )
}
```

---

### 示例 4：在 Tab 中使用状态管理

在 Tab 中使用 Compose 状态管理：

```kotlin
class DataTab : TabRegister {
    override val descriptor = TabDescriptor(
        id = "data",
        title = "数据",
        icon = Icons.Outlined.BarChart,
        route = "tab/data"
    )

    @Composable
    override fun TopBar(navController: NavHostController) {
        FeiShuTitleBar(title = "数据")
    }

    @Composable
    override fun Content(navController: NavHostController) {
        var count by remember { mutableStateOf(0) }
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "计数: $count",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { count++ }) {
                Text("增加")
            }
        }
    }
}
```

---

## 最佳实践

### 1. Tab 模块化设计

- **独立模块**：每个 Tab 应该作为独立的功能模块
- **实现接口**：必须实现 `TabRegister` 接口
- **定义描述符**：使用 `TabDescriptor` 定义 Tab 的元数据
- **单一职责**：每个 Tab 只负责一个功能领域

```kotlin
// ✅ 好的做法：单一职责
class MessageTab : TabRegister { /* 只处理消息相关功能 */ }
class CalendarTab : TabRegister { /* 只处理日历相关功能 */ }

// ❌ 不好的做法：混合职责
class MixedTab : TabRegister { /* 同时处理消息和日历 */ }
```

---

### 2. 标题栏使用规范

- **统一组件**：优先使用 `FeiShuTitleBar` 保持 UI 一致性
- **操作按钮**：通过 `actions` 参数添加操作按钮
- **无障碍支持**：确保 `label` 参数有意义
- **Material Design**：遵循 Material Design 设计规范

```kotlin
// ✅ 好的做法：使用 FeiShuTitleBar
FeiShuTitleBar(
    title = "消息",
    actions = listOf(
        TitleBarAction(
            icon = Icons.Default.Search,
            label = "搜索",  // 有意义的标签
            onClick = { /* 操作 */ }
        )
    )
)

// ❌ 不好的做法：硬编码 UI
TopAppBar(title = { Text("消息") }) // 不一致的样式
```

---




### 4. 解耦设计原则

- **接口依赖**：Tab 模块只依赖 `TabInterface` 模块
- **不依赖主应用**：Tab 模块不应该依赖主应用的代码
- **独立测试**：每个 Tab 应该可以独立测试
- **可插拔**：Tab 应该可以轻松添加或移除



### 5. 性能优化建议

- **懒加载**：Tab 内容应该按需加载
- **状态保存**：使用 `rememberSaveable` 保存状态
- **避免重复创建**：Tab 实例应该复用，不要每次都创建新实例

```kotlin
// ✅ 好的做法：复用 Tab 实例
object MessageTab : TabRegister {
    override val descriptor = TabDescriptor()
    // ...
}

// ❌ 不好的做法：每次都创建新实例
fun createMessageTab() = MessageTab() // 不应该这样做
```

---



## API 参考

### TabDescriptor

```kotlin
data class TabDescriptor(
    val id: String,
    val title: String,
    val icon: ImageVector,
    val route: String
)
```

**构造函数参数：**
- `id: String` - Tab 的唯一标识
- `title: String` - 显示在底部导航栏的标题
- `icon: ImageVector` - 显示在底部导航栏的图标
- `route: String` - 导航路由，必须唯一

---

### TabRegister

```kotlin
interface TabRegister {
    val descriptor: TabDescriptor
    
    @Composable
    fun TopBar(navController: NavHostController)
    
    @Composable
    fun Content(navController: NavHostController)
}
```

**成员：**
- `descriptor: TabDescriptor` - Tab 的描述信息
- `TopBar(navController: NavHostController): Unit` - 渲染顶部栏
- `Content(navController: NavHostController): Unit` - 渲染内容区

---

### TitleBarAction

```kotlin
data class TitleBarAction(
    val icon: ImageVector,
    val label: String,
    val onClick: () -> Unit
)
```

**构造函数参数：**
- `icon: ImageVector` - 按钮图标
- `label: String` - 按钮标签（用于无障碍）
- `onClick: () -> Unit` - 点击回调

---

### FeiShuTitleBar

```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeiShuTitleBar(
    modifier: Modifier = Modifier,
    title: String,
    actions: List<TitleBarAction> = emptyList(),

)
```

**参数：**
- `modifier: Modifier` - 布局修饰符
- `title: String` - 标题文字
- `actions: List<TitleBarAction>` - 操作按钮列表


---

## 总结

`TabInterface` 模块提供了：

- **统一的 Tab 契约**（`TabRegister`）：定义所有 Tab 必须实现的标准
- **标准化的 Tab 描述**（`TabDescriptor`）：统一管理 Tab 的元数据
- **可复用的标题栏组件**（`FeiShuTitleBar`）：保持 UI 一致性

遵循这些契约和最佳实践，可以快速创建新的 Tab 模块并集成到主应用中，实现模块化和可扩展的架构。

---

## 相关资源

- [Material Design 3 文档](https://m3.material.io/)
- [Jetpack Compose 导航文档](https://developer.android.com/jetpack/compose/navigation)
- [Kotlin 数据类文档](https://kotlinlang.org/docs/data-classes.html)

---

