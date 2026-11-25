package com.example.feature.lessons

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.curriculum.AdaptivePracticeEngine
import com.example.core.curriculum.CurriculumRepository
import com.example.core.curriculum.LessonModule
import com.example.core.curriculum.MathTrack
import com.example.core.curriculum.PracticeEvaluation
import com.example.core.curriculum.PracticeProblem
import com.example.core.storage.LessonProgressEntity
import com.example.core.storage.MasteryLevel
import com.example.core.storage.PracticeHistoryEntity
import com.example.core.storage.StorageRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AdaptivePracticeUiState(
    val isOpen: Boolean = false,
    val track: MathTrack = MathTrack.ALGEBRA,
    val lessonId: String? = null,
    val difficulty: Int = 1,
    val streak: Int = 0,
    val sessionSolved: Int = 0,
    val sessionCorrect: Int = 0,
    val ruleNote: String = "Deterministic adaptive difficulty starts at Level 1.",
    val currentProblem: PracticeProblem? = null,
    val userAnswer: String = "",
    val showHint: Boolean = false,
    val evaluation: PracticeEvaluation? = null
)

class LessonsViewModel(private val repository: StorageRepository) : ViewModel() {

    private val _selectedTrack = MutableStateFlow(MathTrack.ALGEBRA)
    val selectedTrack: StateFlow<MathTrack> = _selectedTrack.asStateFlow()

    private val _activeLesson = MutableStateFlow<LessonModule?>(null)
    val activeLesson: StateFlow<LessonModule?> = _activeLesson.asStateFlow()

    private val _practiceState = MutableStateFlow(AdaptivePracticeUiState())
    val practiceState: StateFlow<AdaptivePracticeUiState> = _practiceState.asStateFlow()

    val lessonProgressList: StateFlow<List<LessonProgressEntity>> = repository.lessonProgress.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val practiceHistory: StateFlow<List<PracticeHistoryEntity>> = repository.practiceHistory.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun selectTrack(track: MathTrack) {
        _selectedTrack.value = track
    }

    fun openLesson(module: LessonModule) {
        _activeLesson.value = module
        viewModelScope.launch {
            repository.markLessonOpened(module.id, module.track.name)
        }
    }

    fun closeLesson() {
        _activeLesson.value = null
    }

    fun submitQuickCheck(module: LessonModule, questionIndex: Int, chosenOption: Int): Boolean {
        val q = module.quickChecks.getOrNull(questionIndex) ?: return false
        val isCorrect = chosenOption == q.correctIndex
        viewModelScope.launch {
            repository.recordQuickCheckResult(module.id, module.track.name, questionIndex, isCorrect)
        }
        return isCorrect
    }

    fun startAdaptivePractice(track: MathTrack, lessonId: String? = null) {
        val progress = lessonProgressList.value.find { it.lessonId == lessonId }
        val initDifficulty = when (progress?.masteryLevel) {
            MasteryLevel.MASTERED -> 3
            MasteryLevel.PROFICIENT -> 2
            else -> 1
        }
        val problem = AdaptivePracticeEngine.generateProblem(track, lessonId, initDifficulty)
        _practiceState.value = AdaptivePracticeUiState(
            isOpen = true,
            track = track,
            lessonId = lessonId,
            difficulty = initDifficulty,
            streak = progress?.streak ?: 0,
            ruleNote = "Initial Level $initDifficulty chosen from local mastery record.",
            currentProblem = problem
        )
    }

    fun closeAdaptivePractice() {
        _practiceState.update { it.copy(isOpen = false) }
    }

    fun updatePracticeAnswer(ans: String) {
        _practiceState.update { it.copy(userAnswer = ans) }
    }

    fun toggleHint() {
        _practiceState.update { it.copy(showHint = !it.showHint) }
    }

    fun submitPracticeAnswer() {
        val state = _practiceState.value
        val problem = state.currentProblem ?: return
        if (state.evaluation != null) return
        val eval = AdaptivePracticeEngine.evaluateAnswer(problem, state.userAnswer)
        val newStreak = if (eval.isCorrect) state.streak + 1 else 0
        val nextDiff = AdaptivePracticeEngine.chooseNextDifficulty(state.difficulty, newStreak, !eval.isCorrect)
        val note = AdaptivePracticeEngine.adaptationRuleNote(nextDiff, newStreak, !eval.isCorrect)

        _practiceState.update {
            it.copy(
                streak = newStreak,
                difficulty = nextDiff,
                sessionSolved = it.sessionSolved + 1,
                sessionCorrect = it.sessionCorrect + if (eval.isCorrect) 1 else 0,
                ruleNote = note,
                evaluation = eval
            )
        }
        viewModelScope.launch {
            repository.recordAdaptivePractice(
                lessonId = problem.lessonId,
                track = problem.track.name,
                difficulty = problem.difficulty,
                prompt = problem.prompt,
                userAnswer = state.userAnswer,
                expectedAnswer = problem.expectedNumeric.toString(),
                isCorrect = eval.isCorrect,
                errorCategory = eval.errorCategory
            )
        }
    }

    fun nextPracticeProblem() {
        val s = _practiceState.value
        val next = AdaptivePracticeEngine.generateProblem(s.track, s.lessonId, s.difficulty)
        _practiceState.update {
            it.copy(
                currentProblem = next,
                userAnswer = "",
                showHint = false,
                evaluation = null
            )
        }
    }

    fun recommendedReviewModule(track: MathTrack, progress: List<LessonProgressEntity>): LessonModule? {
        val map = progress.associateBy { it.lessonId }
        val mods = CurriculumRepository.modulesForTrack(track)
        return mods.firstOrNull { m ->
            val p = map[m.id]
            p == null || p.masteryLevel.rank < MasteryLevel.PROFICIENT.rank
        } ?: mods.minByOrNull { map[it.id]?.score ?: 0 }
    }
}
