package com.example.feature.calculator

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BookmarkAdd
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Functions
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Percent
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.ShowChart
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalTextInputService
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.math.AngleUnit
import com.example.core.math.Complex
import com.example.core.math.ConstantEntry
import com.example.core.math.ConstantsLibrary
import com.example.core.math.MathFormatter
import com.example.core.storage.CalcHistoryEntity
import com.example.core.storage.UserSettings
import com.example.core.ui.ApexActionChip
import com.example.core.ui.ApexCard
import com.example.core.ui.ApexPillBadge
import com.example.ui.theme.ApexAmber
import com.example.ui.theme.ApexAmberContainer
import com.example.ui.theme.ApexBackground
import com.example.ui.theme.ApexBorder
import com.example.ui.theme.ApexCyan
import com.example.ui.theme.ApexCyanContainer
import com.example.ui.theme.ApexError
import com.example.ui.theme.ApexSurface
import com.example.ui.theme.ApexSurfaceElevated
import com.example.ui.theme.ApexTextMuted
import com.example.ui.theme.ApexTextPrimary
import com.example.ui.theme.ApexTextSecondary
import com.example.ui.theme.JetBrainsMonoFontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Tier1StandardPanel(
    state: StandardCalcUiState,
    settings: UserSettings,
    history: List<CalcHistoryEntity>,
    viewModel: CalculatorViewModel,
    onToggleAngleUnit: () -> Unit,
    onToggleFractionDisplay: () -> Unit,
    onSendToGraph: (String) -> Unit,
    onSaveToNotebook: (expr: String, result: String) -> Unit
) {
    androidx.compose.foundation.layout.BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val sizeTier = when {
            maxHeight < 420.dp -> KeypadSizeTier.ULTRA
            maxHeight < 540.dp -> KeypadSizeTier.COMPACT
            else -> KeypadSizeTier.NORMAL
        }
        val ultraCompact = sizeTier == KeypadSizeTier.ULTRA
        Column(modifier = Modifier.fillMaxSize()) {
            // Upper display + expandable utility zone
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = if (ultraCompact) 2.dp else 4.dp),
                verticalArrangement = Arrangement.spacedBy(if (ultraCompact) 4.dp else 6.dp)
            ) {
                // Expression + Result Display Card
                ApexCard(
                    containerColor = ApexSurface,
                    borderColor = ApexBorder,
                    contentPadding = if (ultraCompact) 8.dp else 14.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                            ApexPillBadge(
                                text = settings.angleUnit.name,
                                accentColor = ApexCyan,
                                containerColor = ApexCyanContainer,
                                modifier = Modifier.clickable(onClick = onToggleAngleUnit)
                            )
                            ApexPillBadge(
                                text = if (settings.fractionDisplay) "FRAC" else "DEC",
                                accentColor = ApexAmber,
                                containerColor = ApexAmberContainer,
                                modifier = Modifier.clickable(onClick = onToggleFractionDisplay)
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            IconButton(
                                onClick = { viewModel.setHistoryVisible(true) },
                                modifier = Modifier.size(if (ultraCompact) 26.dp else 36.dp).testTag("calc_history_btn")
                            ) {
                                Icon(Icons.Outlined.History, contentDescription = "Calculation History", tint = ApexTextSecondary)
                            }
                            IconButton(
                                onClick = { viewModel.setConstantsVisible(true) },
                                modifier = Modifier.size(if (ultraCompact) 26.dp else 36.dp).testTag("calc_constants_btn")
                            ) {
                                Icon(Icons.Outlined.Functions, contentDescription = "Scientific Constants", tint = ApexCyan)
                            }
                        }
                    }

                    Spacer(Modifier.height(if (ultraCompact) 2.dp else 4.dp))

                    // Suppress Android soft keyboard on standard calculator display while keeping cursor placement
                    @Suppress("DEPRECATION")
                    CompositionLocalProvider(LocalTextInputService provides null) {
                        BasicTextField(
                            value = state.input,
                            onValueChange = { viewModel.updateTextFieldValue(it) },
                            textStyle = TextStyle(
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = if (ultraCompact) 16.sp else 22.sp,
                                lineHeight = if (ultraCompact) 20.sp else 30.sp,
                                color = ApexTextPrimary,
                                textAlign = TextAlign.End
                            ),
                            cursorBrush = SolidColor(ApexCyan),
                            maxLines = if (ultraCompact) 1 else 3,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = if (ultraCompact) 22.dp else 52.dp)
                                .testTag("calc_expression_input"),
                            decorationBox = { innerTextField ->
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    if (state.input.text.isEmpty()) {
                                        Text(
                                            text = "0",
                                            fontFamily = JetBrainsMonoFontFamily,
                                            fontSize = if (ultraCompact) 17.sp else 24.sp,
                                            color = ApexTextMuted
                                        )
                                    }
                                    innerTextField()
                                }
                            }
                        )
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = if (ultraCompact) 3.dp else 8.dp),
                        color = ApexBorder.copy(alpha = 0.5f)
                    )

                    // Output line
                    val displayResult = when {
                        settings.fractionDisplay && state.exactFraction != null -> state.exactFraction
                        state.liveResult.isNotBlank() -> state.liveResult
                        else -> ""
                    }
                    if (state.errorMessage != null && state.isEvaluated) {
                        Text(
                            text = state.errorMessage,
                            style = MaterialTheme.typography.bodySmall.copy(color = ApexError),
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.End
                        )
                    } else {
                        Text(
                            text = if (displayResult.isNotBlank()) "= $displayResult" else "Ready",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = if (ultraCompact) {
                                17.sp
                            } else if (state.isEvaluated) {
                                24.sp
                            } else {
                                19.sp
                            },
                            color = if (state.isEvaluated) ApexCyan else ApexTextSecondary,
                            modifier = Modifier.fillMaxWidth().testTag("calc_result_text"),
                            textAlign = TextAlign.End,
                            maxLines = if (ultraCompact) 1 else 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (state.exactFraction != null || state.complexPolar != null) {
                            Spacer(Modifier.height(if (ultraCompact) 2.dp else 4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                state.exactFraction?.let { frac ->
                                    val mixed = state.mixedFraction?.let { " ($it)" } ?: ""
                                    Text(
                                        text = "Exact: $frac$mixed",
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontSize = 11.sp,
                                        color = ApexAmber
                                    )
                                }
                                state.complexPolar?.let { polar ->
                                    Spacer(Modifier.width(10.dp))
                                    Text(
                                        text = "Polar: $polar",
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontSize = 11.sp,
                                        color = ApexCyan
                                    )
                                }
                            }
                        }
                    }
                }

                // Quick utility bar: Hyperbolics, Percent/Complex tools, Graph, Notebook
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ApexActionChip(
                        label = if (state.showQuickTools) "Hide Tools" else "% & Complex",
                        icon = Icons.Outlined.Percent,
                        accentColor = ApexAmber,
                        onClick = { viewModel.toggleQuickTools() }
                    )
                    if (state.input.text.contains("x", ignoreCase = true)) {
                        ApexActionChip(
                            label = "Plot Graph",
                            icon = Icons.Outlined.ShowChart,
                            onClick = { onSendToGraph(state.input.text) }
                        )
                    }
                    if (state.input.text.isNotBlank() && state.liveResult.isNotBlank()) {
                        ApexActionChip(
                            label = "Save Note",
                            icon = Icons.Outlined.BookmarkAdd,
                            onClick = { onSaveToNotebook(state.input.text, state.liveResult) }
                        )
                    }
                    listOf("sinh", "cosh", "tanh", "abs", "gcd", "lcm").forEach { fn ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = ApexSurfaceElevated,
                            border = BorderStroke(1.dp, ApexBorder),
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { viewModel.insertFunction(fn) }
                        ) {
                            Text(
                                text = "$fn()",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 12.sp,
                                color = ApexTextPrimary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp)
                            )
                        }
                    }
                }

                // Expandable Percentage & Complex Number Converter
                AnimatedVisibility(visible = state.showQuickTools) {
                    QuickUtilitiesCard(angleUnit = settings.angleUnit, viewModel = viewModel)
                }
            }

            // Custom Scientific Keypad
            ScientificKeypad(
                secondMode = state.secondMode,
                angleUnit = settings.angleUnit,
                hapticEnabled = settings.hapticEnabled,
                sizeTier = sizeTier,
                onToggleSecond = { viewModel.toggleSecondMode() },
                onToggleAngleUnit = onToggleAngleUnit,
                onInsertToken = { viewModel.insertAtCursor(it) },
                onInsertFunction = { viewModel.insertFunction(it) },
                onMoveCursor = { viewModel.moveCursor(it) },
                onBackspace = { viewModel.backspace() },
                onClearAll = { viewModel.clearAll() },
                onEquals = { viewModel.evaluateCommit() }
            )
        }
    }

    if (state.showConstantsSheet) {
        ConstantsModalSheet(
            onDismiss = { viewModel.setConstantsVisible(false) },
            onInsert = { entry ->
                viewModel.insertAtCursor(entry.insertText)
                viewModel.setConstantsVisible(false)
            }
        )
    }

    if (state.showHistorySheet) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.setHistoryVisible(false) },
            containerColor = ApexSurface
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Calculation History", style = MaterialTheme.typography.titleLarge, color = ApexTextPrimary)
                    if (history.isNotEmpty()) {
                        TextButton(onClick = { viewModel.clearHistory() }) {
                            Icon(Icons.Outlined.DeleteOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Clear")
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                if (history.isEmpty()) {
                    Text(
                        "No saved calculations yet. Press '=' to evaluate and log expressions.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ApexTextSecondary,
                        modifier = Modifier.padding(vertical = 24.dp)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().heightIn(max = 380.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(history, key = { it.id }) { item ->
                            ApexCard(
                                containerColor = ApexSurfaceElevated,
                                onClick = { viewModel.reuseHistoryItem(item) }
                            ) {
                                Text(
                                    text = item.expression,
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 13.sp,
                                    color = ApexTextSecondary
                                )
                                Text(
                                    text = "= ${item.result}",
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = ApexCyan
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun QuickUtilitiesCard(angleUnit: AngleUnit, viewModel: CalculatorViewModel) {
    var valA by remember { mutableStateOf("25") }
    var valB by remember { mutableStateOf("200") }
    var pctMode by remember { mutableStateOf("of") }
    var cRe by remember { mutableStateOf("3") }
    var cIm by remember { mutableStateOf("4") }

    ApexCard(containerColor = ApexSurfaceElevated) {
        Text("Percentage & Complex Converter", style = MaterialTheme.typography.titleSmall, color = ApexAmber)
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf(
                "of" to "% of",
                "inc" to "+% Inc",
                "dec" to "-% Dec",
                "chg" to "% Change",
                "rev" to "Reverse %"
            ).forEach { (id, lbl) ->
                FilterChip(
                    selected = pctMode == id,
                    onClick = { pctMode = id },
                    label = { Text(lbl, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ApexCyanContainer,
                        selectedLabelColor = ApexCyan
                    )
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = valA,
                onValueChange = { valA = it },
                label = { Text(if (pctMode == "chg") "Old" else if (pctMode == "rev") "Final" else "Percent %", fontSize = 11.sp) },
                singleLine = true,
                modifier = Modifier.weight(1f),
                colors = apexFieldColors()
            )
            OutlinedTextField(
                value = valB,
                onValueChange = { valB = it },
                label = { Text(if (pctMode == "chg") "New" else if (pctMode == "rev") "Change %" else "Value", fontSize = 11.sp) },
                singleLine = true,
                modifier = Modifier.weight(1f),
                colors = apexFieldColors()
            )
        }
        val aNum = valA.toDoubleOrNull()
        val bNum = valB.toDoubleOrNull()
        val pctResult = if (aNum != null && bNum != null) {
            if (pctMode == "inc" || pctMode == "dec") viewModel.computePercentTool(pctMode, bNum, aNum)
            else viewModel.computePercentTool(pctMode, aNum, bNum)
        } else "Enter numbers"
        Spacer(Modifier.height(6.dp))
        Text(
            text = "Result: $pctResult",
            fontFamily = JetBrainsMonoFontFamily,
            fontSize = 13.sp,
            color = ApexCyan
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = ApexBorder)
        Text("Complex Rectangular (a + bi) ↔ Polar", style = MaterialTheme.typography.labelLarge, color = ApexTextPrimary)
        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(value = cRe, onValueChange = { cRe = it }, label = { Text("Re (a)") }, singleLine = true, modifier = Modifier.weight(1f), colors = apexFieldColors())
            OutlinedTextField(value = cIm, onValueChange = { cIm = it }, label = { Text("Im (b)") }, singleLine = true, modifier = Modifier.weight(1f), colors = apexFieldColors())
        }
        val z = Complex(cRe.toDoubleOrNull() ?: 0.0, cIm.toDoubleOrNull() ?: 0.0)
        Spacer(Modifier.height(6.dp))
        Text(
            text = "Polar: ${z.toPolarString(angleUnit)}   |z| = ${MathFormatter.formatNumber(z.magnitude, 6)}   conj = ${z.conjugate().format(5)}",
            fontFamily = JetBrainsMonoFontFamily,
            fontSize = 12.sp,
            color = ApexTextSecondary
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConstantsModalSheet(
    onDismiss: () -> Unit,
    onInsert: (ConstantEntry) -> Unit
) {
    var query by remember { mutableStateOf("") }
    val filtered = remember(query) {
        if (query.isBlank()) ConstantsLibrary.all
        else ConstantsLibrary.all.filter {
            it.name.contains(query, ignoreCase = true) ||
                it.symbol.contains(query, ignoreCase = true) ||
                it.category.contains(query, ignoreCase = true)
        }
    }
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = ApexSurface) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
            Text("Scientific Constants Library", style = MaterialTheme.typography.titleLarge, color = ApexTextPrimary)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Search name, symbol, or category...") },
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = apexFieldColors()
            )
            Spacer(Modifier.height(10.dp))
            LazyColumn(
                modifier = Modifier.fillMaxWidth().heightIn(max = 420.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filtered, key = { it.name }) { c ->
                    ApexCard(
                        containerColor = ApexSurfaceElevated,
                        onClick = { onInsert(c) }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(ApexCyanContainer)
                                        .border(1.dp, ApexCyan.copy(alpha = 0.4f), RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(c.symbol, fontFamily = JetBrainsMonoFontFamily, fontWeight = FontWeight.Bold, color = ApexCyan)
                                }
                                Column {
                                    Text(c.name, style = MaterialTheme.typography.titleSmall, color = ApexTextPrimary)
                                    Text(
                                        "${c.valueDisplay} ${c.unit}",
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontSize = 12.sp,
                                        color = ApexAmber
                                    )
                                }
                            }
                            ApexPillBadge(text = c.category)
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(c.description, style = MaterialTheme.typography.bodySmall, color = ApexTextSecondary)
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
fun apexFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = ApexCyan,
    unfocusedBorderColor = ApexBorder,
    focusedLabelColor = ApexCyan,
    unfocusedLabelColor = ApexTextSecondary,
    focusedTextColor = ApexTextPrimary,
    unfocusedTextColor = ApexTextPrimary,
    cursorColor = ApexCyan,
    focusedContainerColor = ApexBackground.copy(alpha = 0.4f),
    unfocusedContainerColor = ApexBackground.copy(alpha = 0.25f)
)
