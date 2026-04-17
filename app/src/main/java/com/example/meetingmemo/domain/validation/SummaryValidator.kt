package com.example.meetingmemo.domain.validation

object SummaryValidator {
    private const val MinSummarySourceLength = 20

    fun hasEnoughSourceText(rawText: String): Boolean {
        return rawText.trim().length >= MinSummarySourceLength
    }
}
