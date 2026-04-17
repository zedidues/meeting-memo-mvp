package com.example.meetingmemo.data.repository

import com.example.meetingmemo.data.local.MemoDao
import com.example.meetingmemo.data.local.toDomain
import com.example.meetingmemo.data.local.toEntity
import com.example.meetingmemo.domain.model.Memo
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class MemoRepository @Inject constructor(
    private val memoDao: MemoDao,
) {
    fun observeMemos(): Flow<List<Memo>> = memoDao.observeMemos()
        .map { items -> items.map { it.toDomain() } }

    fun observeMemo(memoId: Long): Flow<Memo?> = memoDao.observeMemo(memoId)
        .map { it?.toDomain() }

    suspend fun saveMemo(memo: Memo): Long {
        val now = System.currentTimeMillis()
        val upsertMemo = memo.copy(
            createdAt = if (memo.id == 0L) now else memo.createdAt,
            updatedAt = now,
        )
        return memoDao.insert(upsertMemo.toEntity())
    }
}
