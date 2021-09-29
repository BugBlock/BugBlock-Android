package com.nestor87.bugblock.data.dto

import com.google.gson.annotations.SerializedName

data class Issue (
    var email: String = "",
    val type: String = "bug",
    var description: String = "",
    var metadata: Metadata?,
    @field:SerializedName("network_logs")
    var networkLogs: List<NetworkData>?,
    @field:SerializedName("console_logs")
    var consoleLogs: List<ConsoleData>?
)


