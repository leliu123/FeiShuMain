package com.feishu.aichat.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.feishu.aichat.data.ChatMessage
import dev.jeziellago.compose.markdowntext.MarkdownText
@Composable
fun ChatBubble(message: ChatMessage) {
    val screenWidthDp = LocalConfiguration.current.screenWidthDp.dp
    val maxBubbleWidth = screenWidthDp * 0.75f
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = if (message.isUser) Alignment.End else Alignment.Start
    ) {
        Box(
            modifier = Modifier
                .background(
                    color = if (message.isUser) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    },
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(12.dp)
                .widthIn(
                    min = 40.dp,
                    max = maxBubbleWidth
                )
                .wrapContentWidth()
        ) {if(message.isUser){
            Text(
                text = message.text,
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.bodyMedium
            )
        }
            else{
                MarkdownText(
                    markdown =message.text,
                    style = MaterialTheme.typography.bodyMedium
                )
        }
        }
    }
}