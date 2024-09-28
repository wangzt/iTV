package com.tomsky.hitv.util

import android.content.Context
import okhttp3.ResponseBody
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.security.MessageDigest

object FileUtils {
    private var BASE_ROOT_PATH: String? = null

    private const val ROOT_DIR_NAME = "hitv"


    private val SEPARATOR: String = File.separator //路径分隔符

    fun getRootPath(context: Context):String {
        if (BASE_ROOT_PATH == null) {
            initRootPath(context)
        }
        return BASE_ROOT_PATH!!
    }

    private fun initRootPath(context: Context) {
        if (BASE_ROOT_PATH == null) {
            var file: File? = null
            try {
                file = context.getExternalFilesDir(ROOT_DIR_NAME)
                file!!.mkdirs()
                if (file.exists() && file.canRead() && file.canWrite()) {
                    //如果可读写，则使用此目录
                    val path = file.absolutePath
                    if (path.endsWith("/")) {
                        BASE_ROOT_PATH = file.absolutePath
                    } else {
                        BASE_ROOT_PATH = file.absolutePath + "/"
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            if (BASE_ROOT_PATH == null) {
                //如果走到这里，说明外置sd卡不可用
                if (context != null) {
                    file = context.filesDir
                    val path = file.absolutePath
                    if (path.endsWith("/")) {
                        BASE_ROOT_PATH =
                            file.absolutePath + ROOT_DIR_NAME + "/"
                    } else {
                        BASE_ROOT_PATH =
                            file.absolutePath + "/" + ROOT_DIR_NAME + "/"
                    }
                } else {
                    BASE_ROOT_PATH =
                        "/sdcard/$ROOT_DIR_NAME/"
                }
            }
        }
        val file = File(BASE_ROOT_PATH)
        if (!file.exists()) {
            file.mkdirs()
        } else if (!file.isDirectory) {
            deleteFile(file)
            file.mkdirs()
        }
    }

    /**
     * 删除文件或者目录
     *
     * @param file 要删除的文件
     */
    fun deleteFile(file: File?) {
        if (file != null && file.exists()) {
            if (file.isDirectory) {
                val filelist = file.listFiles()
                if (filelist != null && filelist.size > 0) {
                    for (delFile in filelist) {
                        if (delFile.exists()) deleteFile(delFile)
                    }
                }
            }
            file.delete()
        }
    }

    /**
     * @param context
     * @param assetFileName
     * @param dstPath
     */
    fun copyAssetToFile(context: Context, assetFileName: String?, dstPath: String?): Boolean {
        var myInput: InputStream? = null
        var myOutput: OutputStream? = null
        var isOk = false
        try {
            val file = File(dstPath)
            if (!file.exists()) {
                file.createNewFile()
            }
            myOutput = FileOutputStream(file.absolutePath)
            myInput = context.assets.open(assetFileName!!)
            if (myInput.available().toLong() == file.length()) // 如果大小一致，就认为是同一个文件
                return true
            val buffer = ByteArray(1024)
            var length = myInput.read(buffer)
            while (length > 0) {
                myOutput.write(buffer, 0, length)
                length = myInput.read(buffer)
            }
            myOutput.flush()
            isOk = true
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        } finally {
            if (myInput != null) {
                try {
                    myInput.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            if (myOutput != null) {
                try {
                    myOutput.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        return isOk
    }

    // Function to save the downloaded file
    fun saveFile(responseBody: ResponseBody, filePath: String): String? {
        return try {

            var inputStream: InputStream? = null
            var outputStream: FileOutputStream? = null

            try {
                inputStream = responseBody.byteStream()
                outputStream = FileOutputStream(filePath)

                val buffer = ByteArray(4096)
                var bytesRead: Int

                // 逐块读取文件流
                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                }

                outputStream.flush()
                filePath // 文件保存成功，返回文件路径
            } catch (e: Exception) {
                null // 处理失败情况
            } finally {
                inputStream?.close()
                outputStream?.close()
            }
        } catch (e: Exception) {
            null
        }
    }

    fun calculateMD5(file: File): String {
        return file.inputStream().use { inputStream ->
            val digest = MessageDigest.getInstance("MD5")
            val bytes = ByteArray(1024)
            var length: Int
            while (inputStream.read(bytes).also { length = it } != -1) {
                digest.update(bytes, 0, length)
            }
            digest.digest().let { md ->
                md.fold("") { str, it ->
                    str + "%02x".format(it)
                }
            }
        }
    }
}