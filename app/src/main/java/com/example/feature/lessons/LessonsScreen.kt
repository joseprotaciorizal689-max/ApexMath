package com.example.feature.lessons

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.MainViewModel
import com.example.core.curriculum.CurriculumRepository
import com.example.core.curriculum.MathTrack
import com.example.core.storage.MasteryLevel
import com.example.core.ui.ApexActionChip
import com.example.core.ui.ApexCard
import com.example.core.ui.ApexPillBadge
import com.example.core.ui.StatusBanner
import com.example.feature.calculator.apexFieldColors
import com.example.ui.theme.ApexAmber
import com.example.ui.theme.ApexAmberContainer
import com.example.ui.theme.ApexBackground
import com.example.ui.theme.ApexCyan
import com.example.ui.theme.ApexCyanContainer
import com.example.ui.theme.ApexSuccess
import com.example.ui.theme.ApexSuccessContainer
import com.example.ui.theme.ApexSurface
import com.example.ui.theme.ApexSurfaceElevated
import com.example.ui.theme.ApexTextPrimary
import com.example.ui.theme.ApexTextSecondary
import com.example.ui.theme.JetBrainsMonoFontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessonsScreen(
    mainViewModel: MainViewModel,
    lessonsViewModel: LessonsViewModel
) {
    val selectedTrack by lessonsViewModel.selectedTrack.collectAsStateWithLifecycle()
    val activeLesson by lessonsViewModel.activeLesson.collectAsStateWithLifecycle()
    val progressList by lessonsViewModel.lessonProgressList.collectAsStateWithLifecycle()
    val practiceState by lessonsViewModel.practiceState.collectAsStateWithLifecycle()
    val practiceHistory by lessonsViewModel.practiceHistory.collectAsStateWithLifecycle()

    val progressMap = progressList.associateBy { it.lessonId }
    val trackModules = CurriculumRepository.modulesForTrack(selectedTrack)
    val masteredCount = trackModules.count { progressMap[it.id]?.masteryLevel == MasteryLevel.MASTERED }
    val avgScore = if (trackModules.isEmpty()) 0 else {
        trackModules.sumOf { progressMap[it.id]?.score ?: 0 } / trackModules.size
    }

    if (activeLesson != null) {
        val mod = activeLesson!!
        LessonDetailScreen(
            module = mod,
            progress = progressMap[mod.id],
            onBack = { lessonsViewModel.closeLesson() },
            onQuickCheckAnswer = { qIdx, chosen ->
                lessonsViewModel.submitQuickCheck(mod, qIdx, chosen)
            },
            onSendToCalculator = { expr ->
                mainViewModel.requestSendToCalculator(mod.calculatorTier, expr, mod.title)
            },
            onSendToGraph = { expr ->
                mainViewModel.sendToGraph(listOf(expr))
            },
            onSaveToNotebook = {
                mainViewModel.saveToNotebook(
                    title = "${mod.track.title}: ${mod.title}",
                    content = "${mod.conceptExplanation}\n\nFormulas:\n${mod.coreFormulas.joinToString("\n")}\n\nWorked Answer: ${mod.workedExampleAnswer}",
                    category = mod.track.title,
                    expression = mod.sendToCalculatorExpr,
                    graphExpressions = mod.graphExpr,
                    lessonId = mod.id
                )
            },
            onStartPractice = {
                lessonsViewModel.startAdaptivePractice(mod.track, mod.id)
            }
        )
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(ApexBackground)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Offline Curriculum", style = MaterialTheme.typography.titleLarge, color = ApexTextPrimary)
                    Text("24 structured modules • Zero internet required", style = MaterialTheme.typography.bodySmall, color = ApexTextSecondary)
                }
                ApexActionChip(
                    label = "Practice",
                    icon = Icons.Outlined.FitnessCenter,
                    accentColor = ApexAmber,
                    onClick = { lessonsViewModel.startAdaptivePractice(selectedTrack, null) }
                )
            }

            Spacer(Modifier.height(10.dp))

            // Track Tabs
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MathTrack.entries.forEach { track ->
                    FilterChip(
                        selected = selectedTrack == track,
                        onClick = { lessonsViewModel.selectTrack(track) },
                        label = { Text("${track.title} (8)") },
                        modifier = Modifier.testTag("track_${track.name.lowercase()}"),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ApexCyanContainer,
                            selectedLabelColor = ApexCyan
                        )
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Track Mastery Summary + Review Recommendation
            val recommended = lessonsViewModel.recommendedReviewModule(selectedTrack, progressList)
            val totalPractice = practiceHistory.count { it.track == selectedTrack.name }
            val correctPractice = practiceHistory.count { it.track == selectedTrack.name && it.isCorrect }
            ApexCard(containerColor = ApexSurfaceElevated) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("${selectedTrack.title} Mastery", style = MaterialTheme.typography.titleSmall, color = ApexCyan)
                    Text("$masteredCount / ${trackModules.size} Mastered • $avgScore%", fontFamily = JetBrainsMonoFontFamily, fontSize = 11.sp, color = ApexAmber)
                }
                Spacer(Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { avgScore / 100f },
                    modifier = Modifier.fillMaxWidth().height(5.dp),
                    color = ApexCyan,
                    trackColor = ApexBackground
                )
                if (totalPractice > 0) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Practice accuracy: $correctPractice / $totalPractice (${(correctPractice * 100 / totalPractice)}%)",
                        style = MaterialTheme.typography.bodySmall,
                        color = ApexTextSecondary
                    )
                }
                if (recommended != null) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Recommended next: Module ${recommended.order} — ${recommended.title}",
                        style = MaterialTheme.typography.bodySmall,
                        color = ApexAmber
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(trackModules, key = { it.id }) { mod ->
                    val prog = progressMap[mod.id]
                    val mastery = prog?.masteryLevel ?: MasteryLevel.NOT_STARTED
                    val checksDone = Integer.bitCount(prog?.quickCheckMask ?: 0)
                    ApexCard(
                        onClick = { lessonsViewModel.openLesson(mod) },
                        modifier = Modifier.testTag("lesson_card_${mod.id}")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Module ${mod.order}",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 11.sp,
                                color = ApexCyan
                            )
                            ApexPillBadge(
                                text = mastery.label,
                                accentColor = when (mastery) {
                                    MasteryLevel.MASTERED -> ApexSuccess
                                    MasteryLevel.PROFICIENT -> ApexCyan
                                    MasteryLevel.PRACTICING, MasteryLevel.STARTED -> ApexAmber
                                    MasteryLevel.NOT_STARTED -> ApexTextSecondary
                                },
                                containerColor = when (mastery) {
                                    MasteryLevel.MASTERED -> ApexSuccessContainer
                                    MasteryLevel.PROFICIENT -> ApexCyanContainer
                                    else -> ApexSurfaceElevated
                                }
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(mod.title, style = MaterialTheme.typography.titleMedium, color = ApexTextPrimary)
                        Spacer(Modifier.height(2.dp))
                        Text(mod.summary, style = MaterialTheme.typography.bodySmall, color = ApexTextSecondary)
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Quick Checks: $checksDone / 3  •  Score: ${prog?.score ?: 0}%",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 11.sp,
                            color = ApexTextSecondary
                        )
                    }
                }
            }
        }
    }

    if (practiceState.isOpen) {
        BackHandler { lessonsViewModel.closeAdaptivePractice() }
        ModalBottomSheet(
            onDismissRequest = { lessonsViewModel.closeAdaptivePractice() },
            containerColor = ApexSurface
        ) {
            AdaptivePracticeSheetContent(
                state = practiceState,
                onAnswerChange = { lessonsViewModel.updatePracticeAnswer(it) },
                onToggleHint = { lessonsViewModel.toggleHint() },
                onSubmit = { lessonsViewModel.submitPracticeAnswer() },
                onNext = { lessonsViewModel.nextPracticeProblem() },
                onClose = { lessonsViewModel.closeAdaptivePractice() }
            )
        }
    }
}

@Composable
private fun AdaptivePracticeSheetContent(
    state: AdaptivePracticeUiState,
    onAnswerChange: (String) -> Unit,
    onToggleHint: () -> Unit,
    onSubmit: () -> Unit,
    onNext: () -> Unit,
    onClose: () -> Unit
) {
    val prob = state.currentProblem
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 560.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Adaptive Practice (${state.track.title})", style = MaterialTheme.typography.titleLarge, color = ApexTextPrimary)
                Text(
                    "Level ${state.difficulty}/3  •  Streak: ${state.streak}  •  Session: ${state.sessionCorrect}/${state.sessionSolved}",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 11.sp,
                    color = ApexCyan
                )
            }
            TextButton(onClick = onClose) { Text("Done") }
        }

        Text(state.ruleNote, style = MaterialTheme.typography.bodySmall, color = ApexTextSecondary)

        if (prob != null) {
            ApexCard(containerColor = ApexSurfaceElevated) {
                Text(prob.prompt, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium, color = ApexTextPrimary)
                if (state.showHint) {
                    Spacer(Modifier.height(6.dp))
                    Text("Hint: ${prob.hint}", style = MaterialTheme.typography.bodySmall, color = ApexAmber)
                }
            }

            OutlinedTextField(
                value = state.userAnswer,
                onValueChange = onAnswerChange,
                label = { Text("Your answer (number or expression)") },
                singleLine = true,
                enabled = state.evaluation == null,
                modifier = Modifier.fillMaxWidth().testTag("practice_answer_input"),
                colors = apexFieldColors()
            )

            state.evaluation?.let { eval ->
                StatusBanner(
                    text = eval.feedback,
                    isSuccess = eval.isCorrect,
                    isError = !eval.isCorrect
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextButton(onClick = onToggleHint) {
                    Icon(Icons.Outlined.Lightbulb, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(if (state.showHint) "Hide Hint" else "Hint")
                }
                Spacer(Modifier.weight(1f))
                if (state.evaluation == null) {
                    Button(
                        onClick = onSubmit,
                        colors = ButtonDefaults.buttonColors(containerColor = ApexCyan, contentColor = ApexBackground)
                    ) {
                        Text("Check Answer", fontWeight = FontWeight.SemiBold)
                    }
                } else {
                    Button(
                        onClick = onNext,
                        colors = ButtonDefaults.buttonColors(containerColor = ApexAmber, contentColor = ApexBackground)
                    ) {
                        Text("Next Problem", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
        Spacer(Modifier.height(14.dp))
    }
}
