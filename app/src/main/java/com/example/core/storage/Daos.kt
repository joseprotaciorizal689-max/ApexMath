package com.example.core.storage

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CalcHistoryDao {
    @Query("SELECT * FROM calc_history ORDER BY timestamp DESC LIMIT 100")
    fun observeHistory(): Flow<List<CalcHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: CalcHistoryEntity)

    @Query("DELETE FROM calc_history WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM calc_history")
    suspend fun clearAll()
}

@Dao
interface NotebookDao {
    @Query("SELECT * FROM notebook_entries ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<NotebookEntryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: NotebookEntryEntity): Long

    @Update
    suspend fun update(entry: NotebookEntryEntity)

    @Query("DELETE FROM notebook_entries WHERE id = :id")
    suspend fun deleteById(id: Long)
}

@Dao
interface LessonProgressDao {
    @Query("SELECT * FROM lesson_progress")
    fun observeAll(): Flow<List<LessonProgressEntity>>

    @Query("SELECT * FROM lesson_progress WHERE lessonId = :lessonId LIMIT 1")
    suspend fun getById(lessonId: String): LessonProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(progress: LessonProgressEntity)
}

@Dao
interface PracticeHistoryDao {
    @Query("SELECT * FROM practice_history ORDER BY timestamp DESC LIMIT 200")
    fun observeRecent(): Flow<List<PracticeHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: PracticeHistoryEntity)
}

@Dao
interface SavedGraphDao {
    @Query("SELECT * FROM saved_graphs ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<SavedGraphEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(graph: SavedGraphEntity): Long

    @Query("DELETE FROM saved_graphs WHERE id = :id")
    suspend fun deleteById(id: Long)
}
