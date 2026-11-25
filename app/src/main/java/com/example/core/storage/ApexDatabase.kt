package com.example.core.storage

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        CalcHistoryEntity::class,
        NotebookEntryEntity::class,
        LessonProgressEntity::class,
        PracticeHistoryEntity::class,
        SavedGraphEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class ApexDatabase : RoomDatabase() {
    abstract fun calcHistoryDao(): CalcHistoryDao
    abstract fun notebookDao(): NotebookDao
    abstract fun lessonProgressDao(): LessonProgressDao
    abstract fun practiceHistoryDao(): PracticeHistoryDao
    abstract fun savedGraphDao(): SavedGraphDao

    companion object {
        @Volatile
        private var INSTANCE: ApexDatabase? = null

        fun getInstance(context: Context): ApexDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    ApexDatabase::class.java,
                    "apexmath_offline.db"
                )
                    .fallbackToDestructiveMigration(dropAllTables = false)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
