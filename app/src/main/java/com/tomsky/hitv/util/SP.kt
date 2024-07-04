package com.tomsky.hitv.util

import android.content.Context
import android.content.SharedPreferences

object SP {
    private const val SP_FILE_NAME = "hitv_sp"

    private const val KEY_IPTV_CACHE = "key_iptv_cache"
    private const val KEY_IPTV_INDEX = "key_iptv_index"

    private lateinit var sp: SharedPreferences

    /**
     * The method must be invoked as early as possible(At least before using the keys)
     */
    fun init(context: Context) {
        sp = context.getSharedPreferences(SP_FILE_NAME, Context.MODE_PRIVATE)
    }

    /**
     * iptv cache, json format
     */
    var iptv: String
        get() = sp.getString(KEY_IPTV_CACHE, "") ?: ""
        set(value) = sp.edit().putString(KEY_IPTV_CACHE, value).apply()

    /**
     * tv location in iptv list, eg: 0-0 (The first category and first chanel)
     */
    var tvIndex: String
        get() = sp.getString(KEY_IPTV_INDEX, "") ?: ""
        set(value) = sp.edit().putString(KEY_IPTV_INDEX, value).apply()
}