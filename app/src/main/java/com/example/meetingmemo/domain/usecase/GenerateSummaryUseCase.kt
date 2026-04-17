package com.example.meetingmemo.domain.usecase

import com.example.meetingmemo.data.remote.dto.SummaryResponse
import com.example.meetingmemo.data.repository.SummaryRepository
import com.example.meetingmemo.domain.validation.SummaryValidator
import javax.inject.Inject

class GenerateSummaryUseCase @Inject constructor(
    private val summaryRepository: SummaryRepository,
) {
    suspend operator fun invoke(rawText: String): SummaryResponse {
        require(SummaryValidator.hasEnoughSourceText(rawText)) {
            "요약을 생성하려면 최소 20자 이상의 원문이 필요합니다."
        }
        return summaryRepository.generateSummary(rawText)
    }
}
