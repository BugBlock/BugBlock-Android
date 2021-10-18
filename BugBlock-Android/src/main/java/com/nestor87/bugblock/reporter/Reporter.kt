package com.nestor87.bugblock.reporter

import android.util.Log
import com.nestor87.bugblock.BBLog
import com.nestor87.bugblock.data.dto.CrashData
import com.nestor87.bugblock.data.dto.Issue
import com.nestor87.bugblock.data.network.Network
import com.nestor87.bugblock.loggers.ConsoleLogger
import com.nestor87.bugblock.loggers.NetworkLogger
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

internal class Reporter {
    companion object {

        suspend fun reportIssue(email: String, description: String, image: File? = null): Boolean {
            val issueResponse = Network.retrofit.reportIssue(
                BBLog.appId,
                Issue(
                    email = email,
                    description = description,
                    metadata = BBLog.metadata,
                    consoleLogs = ConsoleLogger.consoleLogs,
                    networkLogs = NetworkLogger.networkLogs
                )
            )

            if (issueResponse.id != null) {
                if (image == null) {
                    return true
                } else {
                    val requestFile = image.asRequestBody("image/*".toMediaTypeOrNull())
                    val body = MultipartBody.Part.createFormData("image", "image.png", requestFile)
                    val screenshotResponse = Network.retrofit.addImageToIssue(
                        BBLog.appId,
                        issueResponse.id,
                        body
                    )
                    return if (screenshotResponse.error == null) {
                        true
                    } else {
                        Log.e("BBScreenshotSending", screenshotResponse.error)
                        false
                    }
                }
            } else {
                Log.e("BBReportSending", issueResponse.error ?: "Unknown error")
                return false
            }
        }

        suspend fun reportCrash(crashLogs: String) {
            val crashResponse = Network.retrofit.reportCrash(
                BBLog.appId,
                CrashData(
                    metadata = BBLog.metadata,
                    log = crashLogs
                ),
            )
            Log.e("ReportCrash", crashResponse.error ?: "")
        }

        suspend fun sendMetadata() {
            Network.retrofit.sendMetadata(
                BBLog.appId,
                BBLog.metadata
            )
        }
    }
}