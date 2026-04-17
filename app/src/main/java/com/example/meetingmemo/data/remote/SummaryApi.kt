package com.example.meetingmemo.data.remote

import com.example.meetingmemo.data.remote.dto.JobCreatedResponse
import com.example.meetingmemo.data.remote.dto.JobStatusResponse
import com.example.meetingmemo.data.remote.dto.SummaryRequest
import com.example.meetingmemo.data.remote.dto.SummaryResponse
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface SummaryApi {
    @POST("summary")
    suspend fun generateSummary(@Body request: SummaryRequest): SummaryResponse

    @Multipart
    @POST("processAudio")
    suspend fun submitAudio(
        @Part file: MultipartBody.Part,
    ): JobCreatedResponse

    @GET("jobs/{jobId}")
    suspend fun getJobStatus(
        @Path("jobId") jobId: String,
    ): JobStatusResponse
}
