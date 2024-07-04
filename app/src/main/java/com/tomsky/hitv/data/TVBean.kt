package com.tomsky.hitv.data

class TVCategoryBean (
    var group: String,
    var tvList: ArrayList<TVBean>
)

class TVBean() {
    var id: String? = null // tvg-id
    var name: String? = null // tvg-name
    var logo: String? = null // tvg-logo
    var group: String? = null // group-title
    var display: String? = null // 逗号后面的名称，用于显示
    var url: String?  = null // 播放地址，这里只收录ipv6
}