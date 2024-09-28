package com.tomsky.hitv

import RetrofitClient
import android.content.Context
import android.text.TextUtils
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tomsky.hitv.data.CheckType
import com.tomsky.hitv.data.TVBean
import com.tomsky.hitv.data.TVCategoryBean
import com.tomsky.hitv.request.ApiService
import com.tomsky.hitv.util.FileUtils
import com.tomsky.hitv.util.JSONUtils
import com.tomsky.hitv.util.SP
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader

class TVViewModel:ViewModel() {

    private val TAG = "tv-model"

    private val fileName = "IPTV.m3u"

    companion object {
        const val INVALID_INDEX = -1
        const val VERSION = 5
    }

    private val tvCategoryMap = HashMap<String, TVCategoryBean>()
    private val tvCategoryList = ArrayList<TVCategoryBean>()

    val checkResult = MutableLiveData<CheckType>()

    private var cateSize = 0
    private var cateIndex = INVALID_INDEX
    private var chanelIndex = INVALID_INDEX

    private val apiService = RetrofitClient.createService(ApiService::class.java)

    fun getVideoUrlCurrent(): String {
        return "http://[2409:8087:1a01:df::7005]:80/ottrrs.hl.chinamobile.com/PLTV/88888888/224/3221226559/index.m3u8" // cctv 1
//        return "http://[2409:8087:1a01:df::4077]:80/ottrrs.hl.chinamobile.com/PLTV/88888888/224/3221226008/index.m3u8"// cctv 8
//        return "http://[2409:8087:1a01:df::4077]:80/ottrrs.hl.chinamobile.com/PLTV/88888888/224/3221226010/index.m3u8"// cctv 6
    }

    fun checkVersion(context: Context) {
        viewModelScope.launch {
            var result = CheckType.FAILED
            try {
                val tVersion = apiService.getVersion()
                val version = tVersion.version
                val localVersion = SP.tvVersion
                Log.i(TAG, "sever version is:$version, localVersion:$localVersion")
                if (version > localVersion) {
                    val tvBody = apiService.getIPTV()
                    var destPath = saveFile(tvBody, FileUtils.getRootPath(context)+"temp.m3u", FileUtils.getRootPath(context) + fileName, tVersion.md5)
                    if (destPath != null) {
                        parseFromFile(destPath)
                        SP.tvVersion = version
                        if (cateSize > 0) {
                            result = CheckType.SUCCESS
                        }
                    }
                } else {
                    result = CheckType.NONE
                }
            } catch (e: Exception) {
                Log.e(TAG,"version", e)
            }
            Log.i(TAG, "check result: $result")
            checkResult.value = result
        }
    }

    fun getData(): List<TVCategoryBean> {
        return tvCategoryList
    }

    private suspend fun readFileContent(responseBody: ResponseBody): String {
        return withContext(Dispatchers.IO) {
            try {
                // 使用 ResponseBody.string() 获取文件内容
                responseBody.string()
            } catch (e: Exception) {
                "0" // 处理错误情况
            }
        }
    }

    private suspend fun saveFile(responseBody: ResponseBody, tempPath: String, destPath: String, md5: String): String? {
        return withContext(Dispatchers.IO) {
            try {
                var finalPath: String? = null
                FileUtils.saveFile(responseBody, tempPath)
                val fileMd5 = FileUtils.calculateMD5(File(tempPath))
                if (md5 == fileMd5) {
                    if (File(tempPath).renameTo(File(destPath))) {
                        finalPath = destPath
                    }
                }
                // 使用 ResponseBody.string() 获取文件内容
                finalPath
            } catch (e: Exception) {
                null // 处理错误情况
            }
        }
    }

    fun getVolume():Float {
        return 1f
    }

    fun parseData(context: Context): List<TVCategoryBean> {
        cateSize = 0
        cateIndex = INVALID_INDEX
        chanelIndex = INVALID_INDEX

        SP.tvVersion
        val cacheIPTV = SP.iptv
        if (!TextUtils.isEmpty(cacheIPTV) && SP.tvVersion >= VERSION) {
            val categoryList = JSONUtils.fromJsonArray(Array<TVCategoryBean>::class.java, cacheIPTV)
            if (categoryList != null && categoryList.size > 0) {
                categoryList.forEachIndexed { _, tvCategoryBean ->
                    tvCategoryMap[tvCategoryBean.group] = tvCategoryBean
                }
            }
            if (tvCategoryMap.size > 0) {
                tvCategoryList.clear()
                tvCategoryList.addAll(categoryList)
                cateSize = tvCategoryList.size
                Log.i(TAG, "from cache,size:${cateSize}, json:$cacheIPTV")
                return tvCategoryList
            }
        }
        var destPath = FileUtils.getRootPath(context) + fileName
        if (FileUtils.copyAssetToFile(context, fileName, destPath)) {
            parseFromFile(destPath)
        }
        return tvCategoryList
    }

    private fun parseFromFile(destPath: String) {
        try {
            tvCategoryList.clear()
            tvCategoryMap.clear()

            val inputStreamReader = InputStreamReader(FileInputStream(destPath))
            val bufferedReader = BufferedReader(inputStreamReader)
            var line: String? = ""
            var tvBean: TVBean? = null
            while ((bufferedReader.readLine().also { line = it }) != null) {
                line?.let {
                    if (it.startsWith("#EXTINF:")) {
                        tvBean = TVBean()
                        val dotList = it.split(",")
                        val dotSize = dotList.size
                        if (dotSize > 1) {
                            tvBean?.display = dotList[1].trim()
                        }
                        if (dotSize > 0) {
                            val blankList = dotList[0].split(" ")
                            blankList.forEachIndexed { index, s ->
                                val bStr = s.trim()
                                val eIndex = bStr.indexOf("=")
                                if (eIndex > 0) {
                                    val key = bStr.substring(0, eIndex)
                                    val value = bStr.substring(eIndex+1, bStr.length).replace("\"","")
                                    if ("tvg-id" == key) {
                                        tvBean?.id = value
                                    } else if ("tvg-name" == key) {
                                        tvBean?.name = value
                                    } else if ("tvg-logo" == key) {
                                        tvBean?.logo = value
                                    } else if ("group-title" == key) {
                                        tvBean?.group = value
                                    }
                                }
                            }
                        }
                    } else {
                        if (tvBean != null && tvBean?.group != null) {
                            val group = tvBean?.group!!
                            val url = it.trim()
                            if (!TextUtils.isEmpty(url) && isIPV6(url)) {
                                tvBean?.url = url
                                var tvCategory = tvCategoryMap[group]
                                if (tvCategory == null) {
                                    var tvList = ArrayList<TVBean>()
                                    tvList.add(tvBean!!)
                                    tvCategory = TVCategoryBean(group, tvList)
                                    tvCategoryMap[group] = tvCategory
                                    tvCategoryList.add(tvCategory)
                                } else {
                                    tvCategory.tvList.add(tvBean!!)
                                }
                            }
                        }
                    }
                }

            }
            cateSize = tvCategoryList.size
            if (cateSize > 0) {
                val jsonStr = JSONUtils.toJson(tvCategoryList)
                SP.iptv = jsonStr
                SP.tvVersion = VERSION
                Log.i(TAG, "from origin,size:${tvCategoryList.size}, json:$jsonStr")
            }
            bufferedReader.close()
            inputStreamReader.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun saveIndex(category: Int, chanel: Int) {
        cateIndex = category
        chanelIndex = chanel
        SP.tvIndex = "${category}-${chanel}"
    }

    fun getIndex():List<Int> {
        val indexStr = SP.tvIndex
        val indexArray = indexStr.split("-")
        if (indexArray.size == 2) {
            return listOf(indexArray[0].toIntOrNull()?:0, indexArray[1].toIntOrNull()?:0)
        } else {
            return listOf(0,0)
        }
    }

    fun getPrevious():List<Int> {
        if (cateIndex == -1 || chanelIndex == -1) return listOf(INVALID_INDEX, INVALID_INDEX)

        if (cateIndex < cateSize) {
            if (chanelIndex == 0) { // left edge
                if (cateIndex == 0) { // first category
                    return listOf(cateSize-1, tvCategoryList[cateSize-1].tvList.size-1)
                } else { // middle category
                    return listOf(cateIndex - 1, tvCategoryList[cateIndex-1].tvList.size-1)
                }
            } else if (chanelIndex > 0) { // current category
                return listOf(cateIndex, chanelIndex-1)
            }
        }
        return listOf(INVALID_INDEX, INVALID_INDEX)
    }

    fun getNext():List<Int> {
        if (cateIndex == -1 || chanelIndex == -1) return listOf(INVALID_INDEX, INVALID_INDEX)

        if (cateIndex < cateSize) {
            val curChanelSize = tvCategoryList[cateIndex].tvList.size
            if (chanelIndex == curChanelSize -1) { // current category, last chanel
                if (cateIndex == cateSize - 1) { // current category is last category
                    return listOf(0, 0)
                } else {
                    return listOf(cateIndex+1, 0)
                }
            } else {
                return listOf(cateIndex, chanelIndex+1)
            }
        }

        return listOf(INVALID_INDEX, INVALID_INDEX)
    }

    fun getTVBean(cateIndex: Int, chanelIndex: Int):TVBean {
        return tvCategoryList[cateIndex].tvList[chanelIndex]
    }

    private fun isIPV6(url: String): Boolean {
        return url.startsWith("http://[") || url.startsWith("https://[")
    }
}