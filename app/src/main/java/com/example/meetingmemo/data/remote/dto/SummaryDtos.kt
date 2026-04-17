package com.example.meetingmemo.data.remote.dto

data class SummaryRequest(
    val rawText: String,
)

data class SummaryResponse(
    val title: String,
    val summary: String,
    val actionItems: List<String> = emptyList(),
)

data class AudioProcessResponse(
    val title: String,
    val transcript: String,
    val summary: String,
    val actionItems: List<String> = emptyList(),
)
