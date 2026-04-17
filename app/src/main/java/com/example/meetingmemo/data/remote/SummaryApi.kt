package com.example.meetingmemo.data.remote

import com.example.meetingmemo.data.remote.dto.AudioProcessResponse
import com.example.meetingmemo.data.remote.dto.SummaryRequest
import com.example.meetingmemo.data.remote.dto.SummaryResponse
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface SummaryApi {
    @POST("summary")
    suspend fun generateSummary(@Body request: SummaryRequest): SummaryResponse

    @Multipart
    @POST("processAudio")
    suspend fun processAudio(
        @Part file: MultipartBody.Part,
    ): AudioProcessResponse
}
