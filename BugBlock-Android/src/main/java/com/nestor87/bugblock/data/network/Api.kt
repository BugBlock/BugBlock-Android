package com.nestor87.bugblock.data.network

import com.nestor87.bugblock.data.dto.*
import okhttp3.MultipartBody
import retrofit2.http.*

interface Api {
    @POST("reporter/crash")
    suspend fun reportCrash(@Header("AppId") appId: String, @Body crashData: CrashData): CrashResponse

    @POST("reporter/issue")
    suspend fun reportIssue(@Header("AppId") appId: String, @Body issue: Issue): IssueResponse

    @Multipart
    @PUT("reporter/issue/{id}/image")
    suspend fun addImageToIssue(@Header("AppId") appId: String, @Path("id") issueId: Int, @Part image: MultipartBody.Part): AddImageToIssueResponse

    @POST("reporter/metadata")
    suspend fun sendMetadata(@Header("AppId") appId: String, @Body metadata: Metadata)
}