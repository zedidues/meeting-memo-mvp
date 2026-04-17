package com.example.meetingmemo.data.repository

import com.example.meetingmemo.data.remote.EmailApi
import com.example.meetingmemo.data.remote.dto.EmailRequest
import com.example.meetingmemo.data.remote.dto.EmailResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EmailRepository @Inject constructor(
    private val emailApi: EmailApi,
) {
    suspend fun sendEmail(
        toEmail: String,
        title: String,
        rawText: String,
        summaryText: String,
    ): EmailResponse {
        return emailApi.sendEmail(
            EmailRequest(
                toEmail = toEmail,
                title = title,
                rawText = rawText,
                summaryText = summaryText,
            ),
        )
    }
}
