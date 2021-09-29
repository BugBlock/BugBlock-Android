package com.nestor87.bugblock.data.dto

import com.google.gson.annotations.SerializedName

data class Metadata (
    @field:SerializedName("os_type")
    val osType: String = "android",
    @field:SerializedName("os_version")
    var osVersion: Int?,
    @field:SerializedName("app_version")
    var appVersion: String?,
    @field:SerializedName("app_build")
    var appBuild: Int?,
    @field:SerializedName("app_package_name")
    var appPackageName: String?,
    @field:SerializedName("device_name")
    var deviceName: String?,
    @field:SerializedName("network_type")
    var networkType: String?,
    @field:SerializedName("user_uuid")
    var userUUID: String?,
    @field:SerializedName("user_email")
    var userEmail: String?,
    @field:SerializedName("user_name")
    var userName: String?
)
