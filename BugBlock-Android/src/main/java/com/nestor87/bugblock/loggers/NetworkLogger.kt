package com.nestor87.bugblock.loggers

import com.nestor87.bugblock.data.dto.NetworkData
import com.nestor87.bugblock.data.dto.NetworkRequest
import com.nestor87.bugblock.data.dto.NetworkResponse
import okhttp3.Interceptor
import okhttp3.RequestBody
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
                            body = requestBodyToString(request.body) ?: ""
                        ),
                        response = NetworkResponse(
                            headers = response.headers.toMap(),
                            body = response.body?.string() ?: ""
                        )
                    )
                )
                return@Interceptor response
        }

        val emptyInterceptor = Interceptor {
            return@Interceptor it.proceed(it.request())
        }

        private fun requestBodyToString(request: RequestBody?): String? {
            return try {
                val copy: RequestBody? = request
                val buffer = okio.Buffer()
                if (copy != null) copy.writeTo(buffer) else return ""
                buffer.readUtf8()
            } catch (e: IOException) {
                "did not work"
            }
        }

    }
}