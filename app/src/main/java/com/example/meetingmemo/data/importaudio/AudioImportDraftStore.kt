package com.example.meetingmemo.data.importaudio

import com.example.meetingmemo.domain.model.ImportedAudioDraft
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@Singleton
class AudioImportDraftStore @Inject constructor() {
    private val mutex = Mutex()
    private var pendingDraft: ImportedAudioDraft? = null

    suspend fun save(draft: ImportedAudioDraft) {
        mutex.withLock {
            pendingDraft = draft
        }
    }

    suspend fun consume(): ImportedAudioDraft? {
        return mutex.withLock {
            pendingDraft.also { pendingDraft = null }
        }
    }
}
