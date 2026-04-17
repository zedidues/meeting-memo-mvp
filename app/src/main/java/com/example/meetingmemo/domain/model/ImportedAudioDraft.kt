package com.example.meetingmemo.domain.model

data class ImportedAudioDraft(
    val fileName: String,
    val title: String,
    val rawText: String,
    val summaryText: String,
    val actionItems: List<String> = emptyList(),
)
