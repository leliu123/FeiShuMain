package com.feishu.AIChat.utils

import android.content.Context

/**
 * SharedPreferences 工具类（Kotlin 版）
 */
object SPUtil {

    private const val FILE_NAME = "user_prefs"

    fun putString(context: Context, key: String, value: String?) {
        getEditor(context)
            .putString(key, value)
            .apply()
    }

    fun getString(context: Context, key: String, defValue: String = ""): String {
        return getSharedPreferences(context).getString(key, defValue) ?: defValue
    }

    fun putBoolean(context: Context, key: String, value: Boolean) {
        getEditor(context)
            .putBoolean(key, value)
            .apply()
    }

    fun getBoolean(context: Context, key: String, defValue: Boolean = false): Boolean {
        return getSharedPreferences(context).getBoolean(key, defValue)
    }

    fun clear(context: Context) {
        getEditor(context)
            .clear()
            .apply()
    }

    private fun getSharedPreferences(context: Context) =
        context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)

    private fun getEditor(context: Context) =
        getSharedPreferences(context).edit()
}