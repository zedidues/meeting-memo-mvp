package com.example.meetingmemo.data.repository

import com.example.meetingmemo.data.remote.dto.AudioProcessResponse
import com.example.meetingmemo.data.remote.SummaryApi
import com.example.meetingmemo.data.remote.dto.SummaryRequest
import com.example.meetingmemo.data.remote.dto.SummaryResponse
import javax.inject.Inject
import javax.inject.Singleton
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

@Singleton
class SummaryRepository @Inject constructor(
    private val summaryApi: SummaryApi,
) {
    suspend fun generateSummary(rawText: String): SummaryResponse {
        return summaryApi.generateSummary(SummaryRequest(rawText = rawText))
    }

    suspend fun processAudio(
        fileName: String,
        mimeType: String,
        bytes: ByteArray,
    ): AudioProcessResponse {
        val requestBody = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
        val filePart = MultipartBody.Part.createFormData(
            name = "file",
            filename = fileName,
            body = requestBody,
        )
        return summaryApi.processAudio(filePart)
    }
}
