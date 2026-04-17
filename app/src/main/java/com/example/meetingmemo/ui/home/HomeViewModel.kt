package com.example.meetingmemo.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.meetingmemo.data.repository.MemoRepository
import com.example.meetingmemo.domain.model.Memo
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class HomeUiState(
    val memos: List<Memo> = emptyList(),
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    memoRepository: MemoRepository,
) : ViewModel() {
    val uiState: StateFlow<HomeUiState> = memoRepository.observeMemos()
        .map { memos ->
            val seen = mutableSetOf<String>()
            HomeUiState(memos = memos.filter { memo -> seen.add(memo.rawText.trim()) })
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeUiState(),
        )
}
