package com.tomsky.hitv.data

data class TVersion (val version: Int, val md5: String)

enum class CheckType {
    NONE, // 已经是最新版本了，不需要更新
    SUCCESS, // 更新成功
    FAILED, // 更新失败
}

data class TVMenuItem(val display: String, val action: Int)

enum class TVMenuAction {
    NONE, UPDATE_DATA,CLOSE
}