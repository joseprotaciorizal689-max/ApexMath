package com.example.feature.notebook

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.storage.NotebookEntryEntity
import com.example.core.storage.StorageRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NotebookViewModel(private val repository: StorageRepository) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _editingEntry = MutableStateFlow<NotebookEntryEntity?>(null)
    val editingEntry: StateFlow<NotebookEntryEntity?> = _editingEntry.asStateFlow()

    val entries: StateFlow<List<NotebookEntryEntity>> = combine(
        repository.notebookEntries,
        _searchQuery,
        _selectedCategory
    ) { all, query, cat ->
        all.filter { entry ->
            val catMatch = cat == "All" || entry.category.equals(cat, ignoreCase = true)
            val q = query.trim()
            val textMatch = q.isEmpty() ||
                entry.title.contains(q, ignoreCase = true) ||
                entry.content.contains(q, ignoreCase = true) ||
                entry.expression.contains(q, ignoreCase = true)
            catMatch && textMatch
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun setSearchQuery(q: String) {
        _searchQuery.value = q
    }

    fun setCategory(cat: String) {
        _selectedCategory.value = cat
    }

    fun startNewNote() {
        _editingEntry.value = NotebookEntryEntity(title = "", content = "", category = "General")
    }

    fun startEditNote(entry: NotebookEntryEntity) {
        _editingEntry.value = entry
    }

    fun dismissEditor() {
        _editingEntry.value = null
    }

    fun saveEntry(
        id: Long,
        title: String,
        content: String,
        category: String,
        expression: String,
        graphExpressions: String,
        lessonId: String
    ) {
        val cleanTitle = title.trim().ifEmpty { "Untitled Math Note" }
        viewModelScope.launch {
            repository.saveNotebookEntry(
                NotebookEntryEntity(
                    id = id,
                    title = cleanTitle,
                    content = content.trim(),
                    category = category,
                    expression = expression.trim(),
                    graphExpressions = graphExpressions.trim(),
                    lessonId = lessonId
                )
            )
            _editingEntry.value = null
        }
    }

    fun deleteEntry(id: Long) {
        viewModelScope.launch { repository.deleteNotebookEntry(id) }
    }
}
