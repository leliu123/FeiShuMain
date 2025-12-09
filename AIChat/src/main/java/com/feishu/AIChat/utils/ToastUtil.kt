package com.feishu.AIChat.utils

import android.content.Context
import android.graphics.Bitmap
import android.os.Looper
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object MyToastUtil {

    @JvmStatic
    fun show(
        @NonNull mContext: Context,
        @NonNull c: CharSequence,
        @Nullable img: Bitmap?
    ) {
        if (c.isBlank()) return
        if (Looper.myLooper() != Looper.getMainLooper()) {
            android.os.Handler(Looper.getMainLooper()).post {
                createComposeToast(mContext, c.toString(), img)
            }
        } else {
            createComposeToast(mContext, c.toString(), img)
        }
    }

    private fun createComposeToast(context: Context, text: String, img: Bitmap?) {
        val toast = Toast(context)
        toast.setGravity(Gravity.CENTER_HORIZONTAL or Gravity.BOTTOM, 0, 60)
        val composeView = androidx.compose.ui.platform.ComposeView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setContent {
                // 复刻原 XML 的布局和样式
                ToastContent(text = text, bitmap = img)
            }
        }

        toast.view = composeView
        toast.show()
    }

    @Composable
    private fun ToastContent(text: String, bitmap: Bitmap?) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = Color(0xFF000000).copy(alpha = 0.8f),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                )
                .padding(
                    top = 16.dp,
                    bottom = 16.dp,
                    start = 16.dp,
                    end = 20.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier
                        .size(32.dp)
                        .padding(top = 2.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            } else {
                Spacer(modifier = Modifier.width(16.dp))
            }

            Text(
                text = text,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    @Composable
    fun showInCompose(
        @NonNull text: CharSequence,
        @Nullable img: Bitmap? = null
    ) {
        val context = LocalContext.current
        LaunchedEffect(text, img) {
            show(context, text, img)
        }
    }

    @Composable
    fun rememberToast(
        show: Boolean,
        text: CharSequence,
        img: Bitmap? = null
    ) {
        val context = LocalContext.current
        val toast = remember { Toast(context) }

        LaunchedEffect(show, text, img) {
            if (show && text.isNotBlank()) {
                // 重建 Compose 布局
                val composeView = androidx.compose.ui.platform.ComposeView(context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    setContent {
                        ToastContent(text = text.toString(), bitmap = img)
                    }
                }
                toast.view = composeView
                toast.setGravity(Gravity.CENTER_HORIZONTAL or Gravity.BOTTOM, 0, 60)
                toast.show()
            }
        }
    }
}