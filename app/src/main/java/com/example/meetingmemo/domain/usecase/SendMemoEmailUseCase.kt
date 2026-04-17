package com.example.meetingmemo.domain.usecase

import com.example.meetingmemo.data.remote.dto.EmailResponse
import com.example.meetingmemo.data.repository.EmailRepository
import com.example.meetingmemo.domain.validation.EmailValidator
import javax.inject.Inject

class SendMemoEmailUseCase @Inject constructor(
    private val emailRepository: EmailRepository,
) {
    suspend operator fun invoke(
        toEmail: String,
        title: String,
        rawText: String,
        summaryText: String,
    ): EmailResponse {
        require(EmailValidator.isValid(toEmail)) {
            "유효한 이메일 주소를 입력해주세요."
        }
        return emailRepository.sendEmail(
            toEmail = toEmail,
            title = title,
            rawText = rawText,
            summaryText = summaryText,
        )
    }
}
