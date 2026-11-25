package com.example.core.storage

import android.content.Context
import com.example.core.math.AngleUnit
import kotlinx.coroutines.flow.Flow

class StorageRepository(context: Context) {
    private val db = ApexDatabase.getInstance(context)
    private val prefs = AppPreferences(context)

    val settings: Flow<UserSettings> = prefs.settingsFlow
    val calcHistory: Flow<List<CalcHistoryEntity>> = db.calcHistoryDao().observeHistory()
    val notebookEntries: Flow<List<NotebookEntryEntity>> = db.notebookDao().observeAll()
    val lessonProgress: Flow<List<LessonProgressEntity>> = db.lessonProgressDao().observeAll()
    val practiceHistory: Flow<List<PracticeHistoryEntity>> = db.practiceHistoryDao().observeRecent()
    val savedGraphs: Flow<List<SavedGraphEntity>> = db.savedGraphDao().observeAll()

    suspend fun setAngleUnit(unit: AngleUnit) = prefs.setAngleUnit(unit)
    suspend fun setFractionDisplay(enabled: Boolean) = prefs.setFractionDisplay(enabled)
    suspend fun setPrecision(precision: Int) = prefs.setPrecision(precision)
    suspend fun setHapticEnabled(enabled: Boolean) = prefs.setHapticEnabled(enabled)

    suspend fun addCalcHistory(expression: String, result: String, tier: String) {
        db.calcHistoryDao().insert(CalcHistoryEntity(expression = expression, result = result, tier = tier))
    }

    suspend fun clearCalcHistory() = db.calcHistoryDao().clearAll()

    suspend fun saveNotebookEntry(entry: NotebookEntryEntity): Long {
        return if (entry.id == 0L) {
            db.notebookDao().insert(entry)
        } else {
            db.notebookDao().update(entry.copy(updatedAt = System.currentTimeMillis()))
            entry.id
        }
    }

    suspend fun deleteNotebookEntry(id: Long) = db.notebookDao().deleteById(id)

    suspend fun recordQuickCheckResult(
        lessonId: String,
        track: String,
        questionIndex: Int,
        isCorrect: Boolean
    ) {
        val current = db.lessonProgressDao().getById(lessonId)
            ?: LessonProgressEntity(lessonId = lessonId, track = track)
        val newMask = if (isCorrect) (current.quickCheckMask or (1 shl questionIndex)) else current.quickCheckMask
        val solvedCount = Integer.bitCount(newMask)
        val newAttempts = current.attempts + 1
        val newStreak = if (isCorrect) current.streak + 1 else 0
        val newScore = ((solvedCount / 3.0) * 80.0 + minOf(20.0, newStreak * 5.0)).toInt().coerceIn(0, 100)
        val newMastery = when {
            solvedCount == 3 && newScore >= 85 -> MasteryLevel.MASTERED
            solvedCount >= 2 || newScore >= 60 -> MasteryLevel.PROFICIENT
            newAttempts >= 1 -> MasteryLevel.PRACTICING
            else -> MasteryLevel.STARTED
        }
        db.lessonProgressDao().upsert(
            current.copy(
                masteryName = newMastery.name,
                score = newScore,
                attempts = newAttempts,
                quickCheckMask = newMask,
                streak = newStreak,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun markLessonOpened(lessonId: String, track: String) {
        val existing = db.lessonProgressDao().getById(lessonId)
        if (existing == null) {
            db.lessonProgressDao().upsert(
                LessonProgressEntity(
                    lessonId = lessonId,
                    track = track,
                    masteryName = MasteryLevel.STARTED.name
                )
            )
        }
    }

    suspend fun recordAdaptivePractice(
        lessonId: String,
        track: String,
        difficulty: Int,
        prompt: String,
        userAnswer: String,
        expectedAnswer: String,
        isCorrect: Boolean,
        errorCategory: String
    ) {
        db.practiceHistoryDao().insert(
            PracticeHistoryEntity(
                topicId = lessonId,
                track = track,
                difficulty = difficulty,
                prompt = prompt,
                userAnswer = userAnswer,
                expectedAnswer = expectedAnswer,
                isCorrect = isCorrect,
                errorCategory = errorCategory
            )
        )
        val current = db.lessonProgressDao().getById(lessonId)
            ?: LessonProgressEntity(lessonId = lessonId, track = track)
        val newAttempts = current.attempts + 1
        val newStreak = if (isCorrect) current.streak + 1 else 0
        val delta = if (isCorrect) 8 * difficulty else -5
        val newScore = (current.score + delta).coerceIn(10, 100)
        val solvedCount = Integer.bitCount(current.quickCheckMask)
        val newMastery = when {
            newScore >= 88 && (newStreak >= 3 || solvedCount == 3) -> MasteryLevel.MASTERED
            newScore >= 65 -> MasteryLevel.PROFICIENT
            newAttempts >= 1 -> MasteryLevel.PRACTICING
            else -> MasteryLevel.STARTED
        }
        db.lessonProgressDao().upsert(
            current.copy(
                masteryName = newMastery.name,
                score = newScore,
                attempts = newAttempts,
                streak = newStreak,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun saveGraph(
        name: String,
        expressions: List<String>,
        xMin: Double,
        xMax: Double,
        yMin: Double,
        yMax: Double
    ): Long {
        return db.savedGraphDao().insert(
            SavedGraphEntity(
                name = name,
                expressionsSerialized = expressions.joinToString("\n"),
                xMin = xMin,
                xMax = xMax,
                yMin = yMin,
                yMax = yMax
            )
        )
    }

    suspend fun deleteSavedGraph(id: Long) = db.savedGraphDao().deleteById(id)
}
