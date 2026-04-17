package com.example.meetingmemo.ui.importaudio

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.meetingmemo.data.importaudio.AudioImportDraftStore
import com.example.meetingmemo.data.repository.SummaryRepository
import com.example.meetingmemo.domain.model.ImportedAudioDraft
import com.example.meetingmemo.ui.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AudioImportUiState(
    val selectedFileName: String = "",
    val selectedFileSizeLabel: String = "",
    val selectedFileMimeType: String = "",
    val isReadyToProcess: Boolean = false,
    val importState: UiState<Unit> = UiState.Idle,
    val importedDraft: ImportedAudioDraft? = null,
)

@HiltViewModel
class AudioImportViewModel @Inject constructor(
    private val summaryRepository: SummaryRepository,
    private val audioImportDraftStore: AudioImportDraftStore,
    @ApplicationContext private val appContext: Context,
) : ViewModel() {
    private val _uiState = MutableStateFlow(AudioImportUiState())
    val uiState: StateFlow<AudioImportUiState> = _uiState.asStateFlow()

    private var selectedFileUri: Uri? = null
    private var selectedFileSizeBytes: Long = 0L

    fun onAudioFileSelected(uri: Uri) {
        val metadata = readMetadata(uri)
        if (metadata == null) {
            _uiState.update {
                it.copy(
                    importState = UiState.Error("선택한 파일 정보를 읽을 수 없습니다."),
                    importedDraft = null,
                    isReadyToProcess = false,
                )
            }
            return
        }

        val extension = metadata.name.substringAfterLast('.', "").lowercase()
        if (extension !in SupportedExtensions) {
            _uiState.update {
                it.copy(
                    importState = UiState.Error("지원 형식은 m4a, mp3, wav, aac, ogg 입니다."),
                    importedDraft = null,
                    isReadyToProcess = false,
                )
            }
            return
        }

        if (metadata.sizeBytes > MaxAudioFileSizeBytes) {
            _uiState.update {
                it.copy(
                    importState = UiState.Error("오디오 파일은 최대 25MB까지 업로드할 수 있습니다."),
                    importedDraft = null,
                    isReadyToProcess = false,
                )
            }
            return
        }

        selectedFileUri = uri
        selectedFileSizeBytes = metadata.sizeBytes
        _uiState.update {
            it.copy(
                selectedFileName = metadata.name,
                selectedFileSizeLabel = formatFileSize(metadata.sizeBytes),
                selectedFileMimeType = metadata.mimeType,
                isReadyToProcess = true,
                importedDraft = null,
                importState = UiState.Idle,
            )
        }
    }

    fun processSelectedAudio() {
        val uri = selectedFileUri
        if (uri == null) {
            _uiState.update {
                it.copy(importState = UiState.Error("먼저 음성 파일을 선택해주세요."))
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(importState = UiState.Loading, importedDraft = null) }
            runCatching {
                val bytes = appContext.contentResolver.openInputStream(uri)?.use { input ->
                    input.readBytes()
                } ?: error("선택한 음성 파일을 읽을 수 없습니다.")

                if (bytes.size > MaxAudioFileSizeBytes.toInt()) {
                    error("오디오 파일은 최대 25MB까지 업로드할 수 있습니다.")
                }

                val response = summaryRepository.processAudio(
                    fileName = uiState.value.selectedFileName,
                    mimeType = uiState.value.selectedFileMimeType.ifBlank { "audio/*" },
                    bytes = bytes,
                )

                ImportedAudioDraft(
                    fileName = uiState.value.selectedFileName,
                    title = response.title,
                    rawText = response.transcript,
                    summaryText = response.summary,
                    actionItems = response.actionItems,
                )
            }.onSuccess { draft ->
                _uiState.update {
                    it.copy(
                        importedDraft = draft,
                        importState = UiState.Success(Unit),
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        importState = UiState.Error(
                            throwable.message ?: "오디오 파일 처리에 실패했습니다.",
                        ),
                    )
                }
            }
        }
    }

    suspend fun cacheDraftForRecord(): Boolean {
        val draft = uiState.value.importedDraft ?: return false
        audioImportDraftStore.save(draft)
        return true
    }

    fun clearImportState() {
        _uiState.update { it.copy(importState = UiState.Idle) }
    }

    private fun readMetadata(uri: Uri): AudioFileMetadata? {
        val mimeType = appContext.contentResolver.getType(uri).orEmpty()
        appContext.contentResolver.query(
            uri,
            arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE),
            null,
            null,
            null,
        )?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
            if (!cursor.moveToFirst()) return null

            val name = if (nameIndex >= 0) cursor.getString(nameIndex).orEmpty() else ""
            val size = if (sizeIndex >= 0) cursor.getLong(sizeIndex) else 0L
            return AudioFileMetadata(
                name = name.ifBlank { "voice-note.m4a" },
                sizeBytes = size.coerceAtLeast(0L),
                mimeType = mimeType,
            )
        }
        return null
    }

    private fun formatFileSize(sizeBytes: Long): String {
        val mb = sizeBytes / (1024f * 1024f)
        return if (mb >= 1f) {
            String.format("%.1f MB", mb)
        } else {
            String.format("%.1f KB", sizeBytes / 1024f)
        }
    }

    private data class AudioFileMetadata(
        val name: String,
        val sizeBytes: Long,
        val mimeType: String,
    )

    private companion object {
        const val MaxAudioFileSizeBytes: Long = 25L * 1024L * 1024L
        val SupportedExtensions = setOf("m4a", "mp3", "wav", "aac", "ogg")
    }
}
