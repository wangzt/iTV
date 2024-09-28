package com.tomsky.hitv.request

import com.tomsky.hitv.data.TVersion
import okhttp3.ResponseBody
import retrofit2.http.GET

interface ApiService {

    @GET("admin/version")
    suspend fun getVersion(): TVersion

    @GET("admin/IPTV.m3u")
    suspend fun getIPTV(): ResponseBody
}