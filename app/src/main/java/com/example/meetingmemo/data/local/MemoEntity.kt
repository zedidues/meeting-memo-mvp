package com.example.meetingmemo.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.meetingmemo.domain.model.Memo

@Entity(tableName = "memos")
data class MemoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val rawText: String,
    val summaryText: String,
    val createdAt: Long,
    val updatedAt: Long,
)

fun MemoEntity.toDomain(): Memo = Memo(
    id = id,
    title = title,
    rawText = rawText,
    summaryText = summaryText,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

fun Memo.toEntity(): MemoEntity = MemoEntity(
    id = id,
    title = title,
    rawText = rawText,
    summaryText = summaryText,
    createdAt = createdAt,
    updatedAt = updatedAt,
)
