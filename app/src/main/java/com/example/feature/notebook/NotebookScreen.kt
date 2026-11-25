package com.example.feature.notebook

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.ShowChart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.MainViewModel
import com.example.core.storage.NotebookEntryEntity
import com.example.core.ui.ApexActionChip
import com.example.core.ui.ApexCard
import com.example.core.ui.ApexPillBadge
import com.example.core.ui.MonoFormulaBox
import com.example.feature.calculator.apexFieldColors
import com.example.ui.theme.ApexAmber
import com.example.ui.theme.ApexBackground
import com.example.ui.theme.ApexCyan
import com.example.ui.theme.ApexCyanContainer
import com.example.ui.theme.ApexSurface
import com.example.ui.theme.ApexTextMuted
import com.example.ui.theme.ApexTextPrimary
import com.example.ui.theme.ApexTextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotebookScreen(
    mainViewModel: MainViewModel,
    notebookViewModel: NotebookViewModel
) {
    val entries by notebookViewModel.entries.collectAsStateWithLifecycle()
    val query by notebookViewModel.searchQuery.collectAsStateWithLifecycle()
    val category by notebookViewModel.selectedCategory.collectAsStateWithLifecycle()
    val editing by notebookViewModel.editingEntry.collectAsStateWithLifecycle()

    val categories = listOf("All", "Algebra", "Geometry", "Calculus", "Graph", "General")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ApexBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text("Mathematics Notebook", style = MaterialTheme.typography.titleLarge, color = ApexTextPrimary)
            Text("Offline Room database • Formulas, derivations & graphs", style = MaterialTheme.typography.bodySmall, color = ApexTextSecondary)
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = query,
                onValueChange = { notebookViewModel.setSearchQuery(it) },
                placeholder = { Text("Search notes, equations, expressions...") },
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("notebook_search_input"),
                colors = apexFieldColors()
            )
            Spacer(Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                categories.forEach { cat ->
                    FilterChip(
                        selected = category == cat,
                        onClick = { notebookViewModel.setCategory(cat) },
                        label = { Text(cat) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ApexCyanContainer,
                            selectedLabelColor = ApexCyan
                        )
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            if (entries.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                        Icon(Icons.Outlined.MenuBook, contentDescription = null, tint = ApexTextMuted, modifier = Modifier.size(48.dp))
                        Spacer(Modifier.height(10.dp))
                        Text("Your notebook is empty", style = MaterialTheme.typography.titleMedium, color = ApexTextPrimary)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Tap + to add a formula note, or save calculations, graphs, and lesson summaries directly.",
                            style = MaterialTheme.typography.bodySmall,
                            color = ApexTextSecondary
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(entries, key = { it.id }) { entry ->
                        NotebookCard(
                            entry = entry,
                            onEdit = { notebookViewModel.startEditNote(entry) },
                            onDelete = { notebookViewModel.deleteEntry(entry.id) },
                            onReopen = { mainViewModel.reopenFromNotebook(entry) }
                        )
                    }
                    item { Spacer(Modifier.height(72.dp)) }
                }
            }
        }

        FloatingActionButton(
            onClick = { notebookViewModel.startNewNote() },
            containerColor = ApexCyan,
            contentColor = ApexBackground,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .testTag("notebook_add_btn")
        ) {
            Icon(Icons.Outlined.Add, contentDescription = "New note")
        }
    }

    editing?.let { draft ->
        BackHandler { notebookViewModel.dismissEditor() }
        ModalBottomSheet(
            onDismissRequest = { notebookViewModel.dismissEditor() },
            containerColor = ApexSurface
        ) {
            NotebookEditorSheet(
                draft = draft,
                onDismiss = { notebookViewModel.dismissEditor() },
                onSave = { title, content, cat, expr, graphExprs ->
                    notebookViewModel.saveEntry(draft.id, title, content, cat, expr, graphExprs, draft.lessonId)
                }
            )
        }
    }
}

@Composable
private fun NotebookCard(
    entry: NotebookEntryEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onReopen: () -> Unit
) {
    val dateStr = remember(entry.updatedAt) {
        SimpleDateFormat("MMM d, HH:mm", Locale.US).format(Date(entry.updatedAt))
    }
    ApexCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                ApexPillBadge(text = entry.category, accentColor = ApexAmber)
                Text(dateStr, style = MaterialTheme.typography.labelMedium, color = ApexTextMuted)
            }
            Row {
                IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Outlined.Edit, contentDescription = "Edit note", tint = ApexTextSecondary, modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Outlined.DeleteOutline, contentDescription = "Delete note", tint = ApexTextSecondary, modifier = Modifier.size(18.dp))
                }
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(entry.title, style = MaterialTheme.typography.titleMedium, color = ApexTextPrimary)
        if (entry.content.isNotBlank()) {
            Spacer(Modifier.height(4.dp))
            Text(entry.content, style = MaterialTheme.typography.bodySmall, color = ApexTextSecondary)
        }
        if (entry.expression.isNotBlank()) {
            Spacer(Modifier.height(6.dp))
            MonoFormulaBox(entry.expression)
        }
        if (entry.expression.isNotBlank() || entry.graphExpressions.isNotBlank()) {
            Spacer(Modifier.height(8.dp))
            val isGraph = entry.graphExpressions.isNotBlank()
            ApexActionChip(
                label = if (isGraph) "Reopen Graph" else "Reopen in Calc",
                icon = if (isGraph) Icons.Outlined.ShowChart else Icons.Outlined.Calculate,
                onClick = onReopen
            )
        }
    }
}

@Composable
private fun NotebookEditorSheet(
    draft: NotebookEntryEntity,
    onDismiss: () -> Unit,
    onSave: (title: String, content: String, cat: String, expr: String, graphExprs: String) -> Unit
) {
    var title by remember(draft) { mutableStateOf(draft.title) }
    var content by remember(draft) { mutableStateOf(draft.content) }
    var category by remember(draft) { mutableStateOf(draft.category) }
    var expression by remember(draft) { mutableStateOf(draft.expression) }
    var graphExprs by remember(draft) { mutableStateOf(draft.graphExpressions) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(if (draft.id == 0L) "New Notebook Entry" else "Edit Notebook Entry", style = MaterialTheme.typography.titleLarge, color = ApexTextPrimary)
        Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("General", "Algebra", "Geometry", "Calculus", "Graph").forEach { cat ->
                FilterChip(
                    selected = category == cat,
                    onClick = { category = cat },
                    label = { Text(cat) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = ApexCyanContainer, selectedLabelColor = ApexCyan)
                )
            }
        }
        OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, singleLine = true, modifier = Modifier.fillMaxWidth(), colors = apexFieldColors())
        OutlinedTextField(value = expression, onValueChange = { expression = it }, label = { Text("Formula / Expression (optional)") }, singleLine = true, modifier = Modifier.fillMaxWidth(), colors = apexFieldColors())
        OutlinedTextField(value = graphExprs, onValueChange = { graphExprs = it }, label = { Text("Graph equations (; separated, optional)") }, singleLine = true, modifier = Modifier.fillMaxWidth(), colors = apexFieldColors())
        OutlinedTextField(value = content, onValueChange = { content = it }, label = { Text("Notes & Derivation") }, minLines = 3, maxLines = 7, modifier = Modifier.fillMaxWidth(), colors = apexFieldColors())

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = onDismiss) { Text("Cancel") }
            Spacer(Modifier.width(8.dp))
            Button(
                onClick = { onSave(title, content, category, expression, graphExprs) },
                colors = ButtonDefaults.buttonColors(containerColor = ApexCyan, contentColor = ApexBackground)
            ) {
                Text("Save Note", fontWeight = FontWeight.SemiBold)
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}
