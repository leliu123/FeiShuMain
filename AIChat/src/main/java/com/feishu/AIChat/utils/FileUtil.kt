package com.feishu.AIChat.utils

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import java.io.BufferedOutputStream
import java.io.BufferedReader
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException
import java.nio.file.Files
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object FileUtil {
    val TAG = "FileUtils"
    /**
     * 将字符串写入指定文件
     *
     * @param filePath 文件路径
     * @param content  要写入的字符串内容
     * @param append   是否追加写入
     * @return 写入成功返回 true，失败返回 false
     */
    @JvmStatic
    fun writeStringToFile(filePath: String, content: String, append: Boolean): Boolean {
        return try {
            val file = File(filePath)
            // 如果文件所在目录不存在，则创建目录
            file.parentFile?.mkdirs()
            FileWriter(file, append).use { writer ->
                writer.write(content)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    @JvmStatic
    fun getStringFromFile(fileName: String): String? {
        val file = File(fileName)

        if (!file.exists()) {
            Log.e(TAG, "File $fileName not found in the files directory.")
            return null
        }

        val stringBuilder = StringBuilder()

        try {
            BufferedReader(FileReader(file)).use { bufferedReader ->
                var line: String? = bufferedReader.readLine()
                while (line != null) {
                    stringBuilder.append(line)
                    line = bufferedReader.readLine()
                }
            }
        } catch (e: FileNotFoundException) {
            Log.e(TAG, "File not found: ${e.message}")
            return null
        } catch (e: IOException) {
            Log.e(TAG, "Error reading file: ${e.message}")
            return null
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error: ${e.message}")
            return null
        }

        return stringBuilder.toString()
    }

    /**
     * 删除指定文件
     *
     * @param filePath 文件路径
     * @return 删除成功返回 true，失败返回 false
     */
    @JvmStatic
    fun deleteFile(filePath: String): Boolean {
        val file = File(filePath)

        return try {
            if (file.exists()) {
                file.delete()
            } else {
                Log.e(TAG, "File not found: $filePath")
                false
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "Permission denied: ${e.message}")
            false
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error while deleting file: ${e.message}")
            false
        }
    }

    /**
     * 压缩一个文件夹
     */
    @RequiresApi(Build.VERSION_CODES.O)
    @JvmStatic
    fun zipFolder(folderPath: String): String {
        val folder = File(folderPath)
        if (!folder.exists()) {
            return ""
        }
        val zipfilePath = "${folder.parentFile.path}/${folder.name}.zip"
        ZipOutputStream(BufferedOutputStream(FileOutputStream(zipfilePath))).use { out ->
            Files.walk(folder.toPath())
                .filter { path -> Files.isRegularFile(path) }
                .forEach { path ->
                    val zipEntry = ZipEntry(folder.toPath().relativize(path).toString().replace("\\", "/"))
                    out.putNextEntry(zipEntry)

                    Files.copy(path, out)
                    out.closeEntry()

                }
        }
        deleteDirectoryRecursively(folder)
        folder.delete()
        return zipfilePath
    }

    @JvmStatic
    fun deleteDirectoryRecursively(file: File) {
        if (file.isDirectory) {
            file.listFiles()?.forEach { deleteDirectoryRecursively(it) }
        } else {
            file.delete()
        }
    }


}