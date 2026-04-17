package com.example.meetingmemo.data.remote.dto

data class EmailRequest(
    val toEmail: String,
    val title: String,
    val rawText: String,
    val summaryText: String,
)

data class EmailResponse(
    val success: Boolean,
    val message: String,
)
