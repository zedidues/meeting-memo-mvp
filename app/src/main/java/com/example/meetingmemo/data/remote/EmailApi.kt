package com.example.meetingmemo.data.remote

import com.example.meetingmemo.data.remote.dto.EmailRequest
import com.example.meetingmemo.data.remote.dto.EmailResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface EmailApi {
    @POST("sendEmail")
    suspend fun sendEmail(@Body request: EmailRequest): EmailResponse
}
