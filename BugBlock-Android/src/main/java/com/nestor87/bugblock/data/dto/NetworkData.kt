package com.nestor87.bugblock.data.dto

import com.google.gson.annotations.SerializedName
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

data class NetworkData (
    val timestamp: String? = SimpleDateFormat(
        "yyyy-MM-dd HH:mm:ss Z",
        Locale.getDefault()
    ).format(
        Date()
    ),
    var url: URL,
    var method: String,
    @field:SerializedName("status_code")
    var statusCode: Int,
    var request: NetworkRequest,
    var response: NetworkResponse
)

data class NetworkRequest(
    var headers: Map<String, String>,
    var body: String
)

data class NetworkResponse(
    var headers: Map<String, String>,
    var body: String
)