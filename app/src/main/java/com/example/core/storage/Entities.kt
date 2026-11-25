package com.example.core.storage

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class MasteryLevel(val label: String, val rank: Int) {
    NOT_STARTED("Not Started", 0),
    STARTED("Started", 1),
    PRACTICING("Practicing", 2),
    PROFICIENT("Proficient", 3),
    MASTERED("Mastered", 4)
}

@Entity(tableName = "calc_history")
data class CalcHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val expression: String,
    val result: String,
    val tier: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "notebook_entries")
data class NotebookEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val content: String,
    val category: String, // "Algebra", "Geometry", "Calculus", "Graph", "General"
    val expression: String = "",
    val graphExpressions: String = "", // semicolon-separated expressions if graph
    val lessonId: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "lesson_progress")
data class LessonProgressEntity(
    @PrimaryKey val lessonId: String,
    val track: String,
    val masteryName: String = MasteryLevel.NOT_STARTED.name,
    val score: Int = 0, // 0..100
    val attempts: Int = 0,
    val quickCheckMask: Int = 0, // bitmask of solved quick checks (bits 0..2)
    val streak: Int = 0,
    val updatedAt: Long = System.currentTimeMillis()
) {
    val masteryLevel: MasteryLevel
        get() = runCatching { MasteryLevel.valueOf(masteryName) }.getOrDefault(MasteryLevel.NOT_STARTED)
}

@Entity(tableName = "practice_history")
data class PracticeHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val topicId: String,
    val track: String,
    val difficulty: Int,
    val prompt: String,
    val userAnswer: String,
    val expectedAnswer: String,
    val isCorrect: Boolean,
    val errorCategory: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "saved_graphs")
data class SavedGraphEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val expressionsSerialized: String, // newline-separated equations
    val xMin: Double = -10.0,
    val xMax: Double = 10.0,
    val yMin: Double = -10.0,
    val yMax: Double = 10.0,
    val createdAt: Long = System.currentTimeMillis()
)
