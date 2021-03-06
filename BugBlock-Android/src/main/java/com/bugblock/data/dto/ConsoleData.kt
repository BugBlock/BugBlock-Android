package com.bugblock.data.dto

import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat
import java.util.*

data class ConsoleData (
    val timestamp: String? = SimpleDateFormat(
        "yyyy-MM-dd HH:mm:ss",
        Locale.getDefault()
    ).format(
        Date()
    ),
    var message: String?,
    @field:SerializedName("log_level")
    var logLevel: ConsoleLogLevel = ConsoleLogLevel.INFO
)

enum class ConsoleLogLevel  {
    DEBUG, INFO, WARNING, ERROR
}
