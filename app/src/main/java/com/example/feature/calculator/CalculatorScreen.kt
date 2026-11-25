package com.example.feature.calculator

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.MainViewModel
import com.example.core.curriculum.CalcTargetTier
import com.example.core.ui.ApexPillBadge
import com.example.ui.theme.ApexAmber
import com.example.ui.theme.ApexAmberContainer
import com.example.ui.theme.ApexBackground
import com.example.ui.theme.ApexBorder
import com.example.ui.theme.ApexCyan
import com.example.ui.theme.ApexCyanContainer
import com.example.ui.theme.ApexSurface
import com.example.ui.theme.ApexTextPrimary
import com.example.ui.theme.ApexTextSecondary

@Composable
fun CalculatorScreen(
    mainViewModel: MainViewModel,
    calcViewModel: CalculatorViewModel
) {
    val selectedTier by calcViewModel.selectedTier.collectAsStateWithLifecycle()
    val standardState by calcViewModel.standardState.collectAsStateWithLifecycle()
    val settings by mainViewModel.settings.collectAsStateWithLifecycle()
    val history by calcViewModel.history.collectAsStateWithLifecycle()

    LaunchedEffect(settings.angleUnit, settings.decimalPrecision) {
        calcViewModel.syncSettings(settings.angleUnit, settings.decimalPrecision)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ApexBackground)
    ) {
        // Top header bar with app badge + horizontal 4-tier selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 3.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "ApexMath",
                    style = MaterialTheme.typography.titleLarge,
                    color = ApexTextPrimary
                )
                ApexPillBadge(
                    text = "Alpha-112626",
                    accentColor = ApexAmber,
                    containerColor = ApexAmberContainer
                )
            }
            IconButton(
                onClick = { mainViewModel.openReleaseNotes() },
                modifier = Modifier.size(44.dp).testTag("open_release_notes_btn")
            ) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = "About & Alpha Release Notes",
                    tint = ApexTextSecondary
                )
            }
        }

        // 4-Tier Selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val tiers = listOf(
                CalcTargetTier.STANDARD to "Standard & Sci",
                CalcTargetTier.ALGEBRA to "Algebra & Matrix",
                CalcTargetTier.GEOMETRY to "Geometry & Trig",
                CalcTargetTier.CALCULUS to "Calculus & Num"
            )
            tiers.forEach { (tier, label) ->
                val isSelected = selectedTier == tier
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) ApexCyanContainer else ApexSurface)
                        .border(
                            1.dp,
                            if (isSelected) ApexCyan else ApexBorder,
                            RoundedCornerShape(10.dp)
                        )
                        .clickable { calcViewModel.selectTier(tier) }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .testTag("tier_${tier.name.lowercase()}")
                ) {
                    Text(
                        text = label,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                        color = if (isSelected) ApexCyan else ApexTextSecondary
                    )
                }
            }
        }

        AnimatedContent(
            targetState = selectedTier,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "tier_switch",
            modifier = Modifier.weight(1f)
        ) { tier ->
            when (tier) {
                CalcTargetTier.STANDARD -> Tier1StandardPanel(
                    state = standardState,
                    settings = settings,
                    history = history,
                    viewModel = calcViewModel,
                    onToggleAngleUnit = { mainViewModel.toggleAngleUnit() },
                    onToggleFractionDisplay = { mainViewModel.toggleFractionDisplay() },
                    onSendToGraph = { expr -> mainViewModel.sendToGraph(listOf(expr)) },
                    onSaveToNotebook = { expr, res ->
                        mainViewModel.saveToNotebook(
                            title = "Calculation: $expr",
                            content = "$expr = $res",
                            category = "General",
                            expression = expr
                        )
                    }
                )
                CalcTargetTier.ALGEBRA -> Tier2AlgebraPanel(
                    viewModel = calcViewModel,
                    onSendExprToStandard = { expr ->
                        mainViewModel.requestSendToCalculator(CalcTargetTier.STANDARD, expr, "Algebra Solver")
                    },
                    onSaveToNotebook = { title, content, expr ->
                        mainViewModel.saveToNotebook(title, content, "Algebra", expression = expr)
                    }
                )
                CalcTargetTier.GEOMETRY -> Tier3GeometryPanel(
                    viewModel = calcViewModel,
                    onSendExprToStandard = { expr ->
                        mainViewModel.requestSendToCalculator(CalcTargetTier.STANDARD, expr, "Geometry Solver")
                    },
                    onSaveToNotebook = { title, content, expr ->
                        mainViewModel.saveToNotebook(title, content, "Geometry", expression = expr)
                    }
                )
                CalcTargetTier.CALCULUS -> Tier4CalculusPanel(
                    viewModel = calcViewModel,
                    onSendToGraph = { exprs -> mainViewModel.sendToGraph(exprs) },
                    onSaveToNotebook = { title, content, expr ->
                        mainViewModel.saveToNotebook(title, content, "Calculus", expression = expr)
                    }
                )
            }
        }
    }
}
