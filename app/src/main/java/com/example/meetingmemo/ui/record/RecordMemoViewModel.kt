package com.example.meetingmemo.ui.record

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.meetingmemo.data.importaudio.AudioImportDraftStore
import com.example.meetingmemo.data.repository.MemoRepository
import com.example.meetingmemo.domain.model.Memo
import com.example.meetingmemo.domain.usecase.GenerateSummaryUseCase
import com.example.meetingmemo.ui.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RecordMemoUiState(
    val title: String = "",
    val rawText: String = "",
    val summaryText: String = "",
    val actionItems: List<String> = emptyList(),
    val summaryState: UiState<Unit> = UiState.Idle,
    val saveState: UiState<Long> = UiState.Idle,
)

@HiltViewModel
class RecordMemoViewModel @Inject constructor(
    private val memoRepository: MemoRepository,
    private val generateSummaryUseCase: GenerateSummaryUseCase,
    private val audioImportDraftStore: AudioImportDraftStore,
) : ViewModel() {
    private val _uiState = MutableStateFlow(RecordMemoUiState())
    val uiState: StateFlow<RecordMemoUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            audioImportDraftStore.consume()?.let { draft ->
                _uiState.update {
                    it.copy(
                        title = draft.title,
                        rawText = draft.rawText,
                        summaryText = draft.summaryText,
                        actionItems = draft.actionItems,
                    )
                }
            }
        }
    }

    fun updateTitle(title: String) {
        _uiState.update { it.copy(title = title) }
    }

    fun updateRawText(rawText: String) {
        _uiState.update { it.copy(rawText = rawText) }
    }

    fun updateSummaryText(summaryText: String) {
        _uiState.update { it.copy(summaryText = summaryText) }
    }

    fun appendSpeechResult(text: String) {
        _uiState.update { state ->
            val mergedText = listOf(state.rawText, text.trim())
                .filter { it.isNotBlank() }
                .joinToString(separator = "\n")
            state.copy(rawText = mergedText)
        }
    }

    fun generateSummary() {
        viewModelScope.launch {
            _uiState.update { it.copy(summaryState = UiState.Loading) }
            runCatching {
                generateSummaryUseCase(_uiState.value.rawText)
            }.onSuccess { response ->
                _uiState.update {
                    it.copy(
                        title = if (it.title.isBlank()) response.title else it.title,
                        summaryText = response.summary,
                        actionItems = response.actionItems,
                        summaryState = UiState.Success(Unit),
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        summaryState = UiState.Error(
                            throwable.message ?: "요약 생성에 실패했습니다.",
                        ),
                    )
                }
            }
        }
    }

    fun saveMemo() {
        viewModelScope.launch {
            _uiState.update { it.copy(saveState = UiState.Loading) }
            val state = _uiState.value
            runCatching {
                memoRepository.saveMemo(
                    Memo(
                        title = state.title.ifBlank { buildFallbackTitle(state.rawText) },
                        rawText = state.rawText,
                        summaryText = state.summaryText,
                    ),
                )
            }.onSuccess { memoId ->
                _uiState.update { it.copy(saveState = UiState.Success(memoId)) }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        saveState = UiState.Error(
                            throwable.message ?: "메모 저장에 실패했습니다.",
                        ),
                    )
                }
            }
        }
    }

    fun clearSaveState() {
        _uiState.update { it.copy(saveState = UiState.Idle) }
    }

    fun clearSummaryState() {
        _uiState.update { it.copy(summaryState = UiState.Idle) }
    }

    private fun buildFallbackTitle(rawText: String): String {
        return rawText.lineSequence()
            .map { it.trim() }
            .firstOrNull { it.isNotBlank() }
            ?.take(30)
            ?: "새 회의 메모"
    }
}
