package com.example.meetingmemo.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.meetingmemo.data.preference.UserPreferencesRepository
import com.example.meetingmemo.data.repository.MemoRepository
import com.example.meetingmemo.domain.model.Memo
import com.example.meetingmemo.domain.usecase.SendMemoEmailUseCase
import com.example.meetingmemo.ui.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MemoDetailUiState(
    val memo: Memo? = null,
    val defaultEmail: String = "",
    val emailInput: String = "",
)

@HiltViewModel
class MemoDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    memoRepository: MemoRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val sendMemoEmailUseCase: SendMemoEmailUseCase,
) : ViewModel() {
    private val memoId: Long = checkNotNull(savedStateHandle.get<Long>("memoId"))
    private val emailInput = MutableStateFlow("")

    val uiState: StateFlow<MemoDetailUiState> = combine(
        memoRepository.observeMemo(memoId),
        userPreferencesRepository.defaultEmail,
        emailInput,
    ) { memo, defaultEmail, email ->
        MemoDetailUiState(
            memo = memo,
            defaultEmail = defaultEmail,
            emailInput = if (email.isBlank()) defaultEmail else email,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = MemoDetailUiState(),
    )

    private val _emailState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val emailSendState: StateFlow<UiState<String>> = _emailState.asStateFlow()

    fun updateEmailInput(value: String) {
        emailInput.update { value }
    }

    fun saveDefaultEmail() {
        viewModelScope.launch {
            userPreferencesRepository.setDefaultEmail(uiState.value.emailInput.trim())
        }
    }

    fun sendEmail() {
        val currentState = uiState.value
        val memo = currentState.memo ?: return
        viewModelScope.launch {
            _emailState.value = UiState.Loading
            runCatching {
                sendMemoEmailUseCase(
                    toEmail = currentState.emailInput.trim(),
                    title = memo.title,
                    rawText = memo.rawText,
                    summaryText = memo.summaryText,
                )
            }.onSuccess { response ->
                _emailState.value = if (response.success) {
                    UiState.Success(response.message)
                } else {
                    UiState.Error(response.message)
                }
            }.onFailure { throwable ->
                _emailState.value = UiState.Error(
                    throwable.message ?: "이메일 발송에 실패했습니다.",
                )
            }
        }
    }

    fun clearEmailState() {
        _emailState.value = UiState.Idle
    }
}
