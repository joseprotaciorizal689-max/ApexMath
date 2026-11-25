package com.example

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.curriculum.CalcTargetTier
import com.example.core.math.AngleUnit
import com.example.core.storage.NotebookEntryEntity
import com.example.core.storage.StorageRepository
import com.example.core.storage.UserSettings
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class PrimaryTab(val label: String) {
    CALCULATOR("Calculator"),
    LEARN("Learn"),
    GRAPH("Graph"),
    NOTEBOOK("Notebook")
}

data class PendingCalculatorPayload(
    val tier: CalcTargetTier,
    val expression: String,
    val sourceLabel: String
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    val repository = StorageRepository(application)

    val settings: StateFlow<UserSettings> = repository.settings.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UserSettings()
    )

    private val _selectedTab = MutableStateFlow(PrimaryTab.CALCULATOR)
    val selectedTab: StateFlow<PrimaryTab> = _selectedTab.asStateFlow()

    private val _showReleaseNotes = MutableStateFlow(false)
    val showReleaseNotes: StateFlow<Boolean> = _showReleaseNotes.asStateFlow()

    private val _pendingOverwritePrompt = MutableStateFlow<PendingCalculatorPayload?>(null)
    val pendingOverwritePrompt: StateFlow<PendingCalculatorPayload?> = _pendingOverwritePrompt.asStateFlow()

    private val _calculatorInjectEvents = MutableSharedFlow<Pair<PendingCalculatorPayload, Boolean>>(extraBufferCapacity = 8)
    val calculatorInjectEvents = _calculatorInjectEvents.asSharedFlow()

    private val _graphInjectEvents = MutableSharedFlow<List<String>>(extraBufferCapacity = 8)
    val graphInjectEvents = _graphInjectEvents.asSharedFlow()

    private val _snackbarMessages = MutableSharedFlow<String>(extraBufferCapacity = 8)
    val snackbarMessages = _snackbarMessages.asSharedFlow()

    var currentCalculatorExprNonEmpty: () -> Boolean = { false }

    fun selectTab(tab: PrimaryTab) {
        _selectedTab.value = tab
    }

    fun openReleaseNotes() {
        _showReleaseNotes.value = true
    }

    fun closeReleaseNotes() {
        _showReleaseNotes.value = false
    }

    fun toggleAngleUnit() {
        viewModelScope.launch {
            val next = if (settings.value.angleUnit == AngleUnit.DEG) AngleUnit.RAD else AngleUnit.DEG
            repository.setAngleUnit(next)
        }
    }

    fun toggleFractionDisplay() {
        viewModelScope.launch {
            repository.setFractionDisplay(!settings.value.fractionDisplay)
        }
    }

    fun setHapticEnabled(enabled: Boolean) {
        viewModelScope.launch { repository.setHapticEnabled(enabled) }
    }

    fun requestSendToCalculator(
        tier: CalcTargetTier,
        expression: String,
        sourceLabel: String = "ApexMath"
    ) {
        val payload = PendingCalculatorPayload(tier, expression, sourceLabel)
        _selectedTab.value = PrimaryTab.CALCULATOR
        if (currentCalculatorExprNonEmpty() && tier == CalcTargetTier.STANDARD) {
            _pendingOverwritePrompt.value = payload
        } else {
            _calculatorInjectEvents.tryEmit(payload to true)
        }
    }

    fun confirmCalculatorInjection(replace: Boolean) {
        val payload = _pendingOverwritePrompt.value ?: return
        _pendingOverwritePrompt.value = null
        _calculatorInjectEvents.tryEmit(payload to replace)
    }

    fun dismissCalculatorInjection() {
        _pendingOverwritePrompt.value = null
    }

    fun sendToGraph(expressions: List<String>) {
        _selectedTab.value = PrimaryTab.GRAPH
        _graphInjectEvents.tryEmit(expressions.filter { it.isNotBlank() })
    }

    fun saveToNotebook(
        title: String,
        content: String,
        category: String,
        expression: String = "",
        graphExpressions: String = "",
        lessonId: String = ""
    ) {
        viewModelScope.launch {
            repository.saveNotebookEntry(
                NotebookEntryEntity(
                    title = title,
                    content = content,
                    category = category,
                    expression = expression,
                    graphExpressions = graphExpressions,
                    lessonId = lessonId
                )
            )
            _snackbarMessages.tryEmit("Saved to Notebook")
        }
    }

    fun reopenFromNotebook(entry: NotebookEntryEntity) {
        if (entry.graphExpressions.isNotBlank()) {
            val exprs = entry.graphExpressions.split(";").map { it.trim() }.filter { it.isNotBlank() }
            if (exprs.isNotEmpty()) {
                sendToGraph(exprs)
                return
            }
        }
        if (entry.expression.isNotBlank()) {
            requestSendToCalculator(CalcTargetTier.STANDARD, entry.expression, entry.title)
        }
    }

    fun notifyMessage(msg: String) {
        _snackbarMessages.tryEmit(msg)
    }
}
