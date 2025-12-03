package com.feishu

object MockData {
    val MOCK_MESSAGES = mutableListOf(
        ChatMessage(id = 1, sender = "ai", content = "您好，我是 AI OnCall 助手。我可以帮您排查代码错误、生成技术方案或解释业务协议。"),
        ChatMessage(id = 2, sender = "me", content = "我遇到了一个 Compose 编译报错，能帮我看看吗？"),
        ChatMessage(id = 3, sender = "ai", content = "没问题，请贴出具体的错误堆栈或代码片段。")
    )
}
