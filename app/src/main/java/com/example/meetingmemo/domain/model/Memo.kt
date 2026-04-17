package com.example.meetingmemo.domain.model

data class Memo(
    val id: Long = 0L,
    val title: String = "",
    val rawText: String = "",
    val summaryText: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)
