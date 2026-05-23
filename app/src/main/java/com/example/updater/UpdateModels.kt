package com.example.updater

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UpdateInfo(
    @Json(name = "versionCode") val versionCode: Int,
    @Json(name = "versionName") val versionName: String,
    @Json(name = "apkUrl") val apkUrl: String,
    @Json(name = "releaseNotes") val releaseNotes: String? = null
)
