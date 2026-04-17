package com.example.meetingmemo.data.repository

import com.example.meetingmemo.data.remote.SummaryApi
import com.example.meetingmemo.data.remote.dto.AudioProcessResponse
import com.example.meetingmemo.data.remote.dto.SummaryRequest
import com.example.meetingmemo.data.remote.dto.SummaryResponse
import com.example.meetingmemo.ui.importaudio.AudioProcessStep
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.delay
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
        onProgress: (AudioProcessStep) -> Unit,
    ): AudioProcessResponse {
        val requestBody = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
        val filePart = MultipartBody.Part.createFormData(
            name = "file",
            filename = fileName,
            body = requestBody,
        )

        val jobCreated = summaryApi.submitAudio(filePart)
        onProgress(AudioProcessStep.TRANSCRIBING)

        repeat(120) {
            delay(3_000L)
            val jobStatus = summaryApi.getJobStatus(jobCreated.jobId)
            when (jobStatus.status) {
                "summarizing" -> onProgress(AudioProcessStep.SUMMARIZING)
                "completed" -> return jobStatus.result
                    ?: error("서버에서 결과를 받지 못했습니다.")
                "failed" -> error(jobStatus.error ?: "오디오 처리에 실패했습니다.")
            }
        }
        error("처리 시간이 초과되었습니다. (6분 초과)")
    }
}
