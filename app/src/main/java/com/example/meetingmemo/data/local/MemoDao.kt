package com.example.meetingmemo.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MemoDao {
    @Query("SELECT * FROM memos ORDER BY updatedAt DESC")
    fun observeMemos(): Flow<List<MemoEntity>>

    @Query("SELECT * FROM memos WHERE id = :memoId LIMIT 1")
    fun observeMemo(memoId: Long): Flow<MemoEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(memo: MemoEntity): Long

    @Update
    suspend fun update(memo: MemoEntity)
}
