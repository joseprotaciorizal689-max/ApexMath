package com.example.feature.lessons

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.BookmarkAdd
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.ShowChart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.curriculum.LessonModule
import com.example.core.storage.LessonProgressEntity
import com.example.core.storage.MasteryLevel
import com.example.core.ui.ApexActionChip
import com.example.core.ui.ApexCard
import com.example.core.ui.ApexPillBadge
import com.example.core.ui.MonoFormulaBox
import com.example.core.ui.StatusBanner
import com.example.ui.theme.ApexAmber
import com.example.ui.theme.ApexAmberContainer
import com.example.ui.theme.ApexBackground
import com.example.ui.theme.ApexBorder
import com.example.ui.theme.ApexCyan
import com.example.ui.theme.ApexCyanContainer
import com.example.ui.theme.ApexError
import com.example.ui.theme.ApexErrorContainer
import com.example.ui.theme.ApexSuccess
import com.example.ui.theme.ApexSuccessContainer
import com.example.ui.theme.ApexSurfaceElevated
import com.example.ui.theme.ApexTextPrimary
import com.example.ui.theme.ApexTextSecondary
import com.example.ui.theme.JetBrainsMonoFontFamily

@Composable
fun LessonDetailScreen(
    module: LessonModule,
    progress: LessonProgressEntity?,
    onBack: () -> Unit,
    onQuickCheckAnswer: (questionIndex: Int, chosenIndex: Int) -> Boolean,
    onSendToCalculator: (String) -> Unit,
    onSendToGraph: (String) -> Unit,
    onSaveToNotebook: () -> Unit,
    onStartPractice: () -> Unit
) {
    BackHandler(onBack = onBack)

    val selectedAnswers = remember(module.id) { mutableStateMapOf<Int, Int>() }
    val mastery = progress?.masteryLevel ?: MasteryLevel.STARTED
    val score = progress?.score ?: 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ApexBackground)
    ) {
        // Top bar
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.size(44.dp).testTag("lesson_back_btn")
            ) {
                Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back to modules", tint = ApexTextPrimary)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "${module.track.title} • Module ${module.order}",
                    style = MaterialTheme.typography.labelMedium,
                    color = ApexCyan
                )
                Text(
                    module.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = ApexTextPrimary,
                    maxLines = 2
                )
            }
            ApexPillBadge(
                text = mastery.label,
                accentColor = when (mastery) {
                    MasteryLevel.MASTERED -> ApexSuccess
                    MasteryLevel.PROFICIENT -> ApexCyan
                    else -> ApexAmber
                }
            )
        }

        LinearProgressIndicator(
            progress = { score / 100f },
            modifier = Modifier.fillMaxWidth().height(3.dp),
            color = ApexCyan,
            trackColor = ApexSurfaceElevated
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Action bar
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ApexActionChip("Send to Calc", Icons.Outlined.Calculate, onClick = {
                    onSendToCalculator(module.sendToCalculatorExpr)
                })
                ApexActionChip("Plot Graph", Icons.Outlined.ShowChart, onClick = {
                    onSendToGraph(module.graphExpr)
                })
                ApexActionChip("Save Note", Icons.Outlined.BookmarkAdd, onClick = onSaveToNotebook)
                ApexActionChip("Practice", Icons.Outlined.FitnessCenter, accentColor = ApexAmber, onClick = onStartPractice)
            }

            // 1. Concept Explanation & Mathematical Intuition
            ApexCard {
                Text("1. Concept Explanation", style = MaterialTheme.typography.titleSmall, color = ApexCyan)
                Spacer(Modifier.height(6.dp))
                Text(module.conceptExplanation, style = MaterialTheme.typography.bodyMedium, color = ApexTextPrimary)
                Spacer(Modifier.height(10.dp))
                Text("Mathematical Intuition", style = MaterialTheme.typography.titleSmall, color = ApexAmber)
                Spacer(Modifier.height(4.dp))
                Text(module.mathematicalIntuition, style = MaterialTheme.typography.bodyMedium, color = ApexTextSecondary)
            }

            // 2. Core Formulas & Definitions
            ApexCard {
                Text("2. Core Formulas & Definitions", style = MaterialTheme.typography.titleSmall, color = ApexCyan)
                Spacer(Modifier.height(8.dp))
                MonoFormulaBox(module.coreFormulas.joinToString("\n"))
                Spacer(Modifier.height(10.dp))
                module.definitions.forEach { (term, def) ->
                    Text(term, style = MaterialTheme.typography.labelLarge, color = ApexAmber)
                    Text(def, style = MaterialTheme.typography.bodySmall, color = ApexTextSecondary)
                    Spacer(Modifier.height(6.dp))
                }
            }

            // 3. Common Mistakes
            ApexCard(borderColor = ApexAmber.copy(alpha = 0.4f)) {
                Text("3. Common Mistakes to Avoid", style = MaterialTheme.typography.titleSmall, color = ApexAmber)
                Spacer(Modifier.height(6.dp))
                module.commonMistakes.forEach { m ->
                    Text("• $m", style = MaterialTheme.typography.bodySmall, color = ApexTextPrimary)
                    Spacer(Modifier.height(4.dp))
                }
            }

            // 4. Interactive Sandbox
            LessonSandbox(
                module = module,
                onSendExprToCalc = onSendToCalculator,
                onSendExprToGraph = onSendToGraph
            )

            // 5. Worked Example
            ApexCard(containerColor = ApexSurfaceElevated) {
                Text("5. Worked Example", style = MaterialTheme.typography.titleSmall, color = ApexCyan)
                Spacer(Modifier.height(6.dp))
                Text(module.workedExampleProblem, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = ApexTextPrimary)
                Spacer(Modifier.height(8.dp))
                module.workedExampleSteps.forEach { step ->
                    Text(step, fontFamily = JetBrainsMonoFontFamily, fontSize = 12.sp, color = ApexTextSecondary)
                    Spacer(Modifier.height(4.dp))
                }
                Spacer(Modifier.height(6.dp))
                MonoFormulaBox("Answer: ${module.workedExampleAnswer}", accent = ApexSuccess)
            }

            // 6. Three Quick-Check Problems
            Text("6. Quick-Check Problems", style = MaterialTheme.typography.titleMedium, color = ApexTextPrimary)
            module.quickChecks.forEachIndexed { qIdx, q ->
                val chosen = selectedAnswers[qIdx]
                val solvedBit = ((progress?.quickCheckMask ?: 0) and (1 shl qIdx)) != 0
                ApexCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Check ${qIdx + 1} of 3", style = MaterialTheme.typography.labelLarge, color = ApexCyan)
                        if (solvedBit) ApexPillBadge("Passed", ApexSuccess, ApexSuccessContainer)
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(q.prompt, style = MaterialTheme.typography.bodyMedium, color = ApexTextPrimary)
                    Spacer(Modifier.height(8.dp))

                    q.options.forEachIndexed { optIdx, optText ->
                        val isPicked = chosen == optIdx
                        val isRightOpt = optIdx == q.correctIndex
                        val bg = when {
                            chosen == null -> ApexSurfaceElevated
                            isPicked && isRightOpt -> ApexSuccessContainer
                            isPicked && !isRightOpt -> ApexErrorContainer
                            isRightOpt -> ApexSuccessContainer.copy(alpha = 0.55f)
                            else -> ApexSurfaceElevated
                        }
                        val border = when {
                            chosen != null && isPicked && isRightOpt -> ApexSuccess
                            chosen != null && isPicked && !isRightOpt -> ApexError
                            else -> ApexBorder
                        }
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = bg,
                            border = BorderStroke(1.dp, border),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    selectedAnswers[qIdx] = optIdx
                                    onQuickCheckAnswer(qIdx, optIdx)
                                }
                        ) {
                            Text(
                                text = optText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = ApexTextPrimary,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                            )
                        }
                    }

                    if (chosen != null) {
                        Spacer(Modifier.height(6.dp))
                        val ok = chosen == q.correctIndex
                        StatusBanner(
                            text = (if (ok) "Correct! " else "Review: ") + q.deductiveExplanation,
                            isSuccess = ok,
                            isError = !ok
                        )
                    }
                }
            }

            Button(
                onClick = onStartPractice,
                modifier = Modifier.fillMaxWidth().testTag("launch_adaptive_practice_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = ApexAmber, contentColor = ApexBackground)
            ) {
                Icon(Icons.Outlined.FitnessCenter, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Launch Adaptive Practice on ${module.track.title}", fontWeight = FontWeight.SemiBold)
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}
