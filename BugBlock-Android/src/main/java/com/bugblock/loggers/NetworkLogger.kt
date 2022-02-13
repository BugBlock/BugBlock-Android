package com.bugblock.loggers

import com.bugblock.data.dto.NetworkData
import com.bugblock.data.dto.NetworkRequest
import com.bugblock.data.dto.NetworkResponse
import okhttp3.Interceptor
import okhttp3.Request
import okio.Buffer
import java.io.IOException

class NetworkLogger {
    companion object {
        private val _networkLogs = mutableListOf<NetworkData>()
        val networkLogs: List<NetworkData> = _networkLogs

        val loggingInterceptor = Interceptor {
                val request = it.request()
                val response = it.proceed(it.request())
                _networkLogs.add(
                    NetworkData(
                        method = request.method,
                        url = request.url.toUrl(),
                        statusCode = response.code,
                        request = NetworkRequest(
                            headers = request.headers.toMap(),
                            body = requestBodyToString(request) ?: ""
                        ),
                        response = NetworkResponse(
                            headers = response.headers.toMap(),
                            body = response.peekBody(2048).string()
                        )
                    )
                )
                return@Interceptor response
        }

        val emptyInterceptor = Interceptor {
            return@Interceptor it.proceed(it.request())
        }

        private fun requestBodyToString(request: Request): String? {
            return try {
                val copy = request.newBuilder().build()
                val buffer = Buffer()
                copy.body?.writeTo(buffer)
                buffer.readUtf8()
            } catch (e: IOException) {
                "did not work"
            }

        }

    }
}