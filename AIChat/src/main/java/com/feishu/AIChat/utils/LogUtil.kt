package com.feishu.AIChat.utils

import android.util.Log

object LogUtil {

    private const val SEGMENT_SIZE = 3 * 1024

    fun d(tag: String?, msg: String?) {
        printLongLog(Log.DEBUG, tag, msg)
    }

    fun i(tag: String?, msg: String?) {
        printLongLog(Log.INFO, tag, msg)
    }

    fun w(tag: String?, msg: String?) {
        printLongLog(Log.WARN, tag, msg)
    }

    fun e(tag: String?, msg: String?) {
        printLongLog(Log.ERROR, tag, msg)
    }

    private fun printLongLog(level: Int, tag: String?, msg: String?) {
        if (tag.isNullOrEmpty() || msg.isNullOrEmpty()) return

        var start = 0
        val length = msg.length

        while (start < length) {
            val end = (start + SEGMENT_SIZE).coerceAtMost(length)
            val part = msg.substring(start, end)

            when (level) {
                Log.DEBUG -> Log.d(tag, part)
                Log.INFO -> Log.i(tag, part)
                Log.WARN -> Log.w(tag, part)
                Log.ERROR -> Log.e(tag, part)
            }

            start = end
        }
    }
}
