package com.example.feature.calculator

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.math.AngleUnit
import com.example.ui.theme.ApexAmber
import com.example.ui.theme.ApexAmberContainer
import com.example.ui.theme.ApexBackground
import com.example.ui.theme.ApexBorder
import com.example.ui.theme.ApexCyan
import com.example.ui.theme.ApexCyanContainer
import com.example.ui.theme.ApexError
import com.example.ui.theme.ApexErrorContainer
import com.example.ui.theme.ApexSurface
import com.example.ui.theme.ApexSurfaceElevated
import com.example.ui.theme.ApexTextPrimary
import com.example.ui.theme.ApexTextSecondary
import com.example.ui.theme.JetBrainsMonoFontFamily

enum class KeypadSizeTier { NORMAL, COMPACT, ULTRA }

@Composable
fun ScientificKeypad(
    secondMode: Boolean,
    angleUnit: AngleUnit,
    hapticEnabled: Boolean,
    onToggleSecond: () -> Unit,
    onToggleAngleUnit: () -> Unit,
    onInsertToken: (String) -> Unit,
    onInsertFunction: (String) -> Unit,
    onMoveCursor: (Int) -> Unit,
    onBackspace: () -> Unit,
    onClearAll: () -> Unit,
    onEquals: () -> Unit,
    modifier: Modifier = Modifier,
    sizeTier: KeypadSizeTier = KeypadSizeTier.NORMAL
) {
    val topH = when (sizeTier) {
        KeypadSizeTier.NORMAL -> 38.dp
        KeypadSizeTier.COMPACT -> 30.dp
        KeypadSizeTier.ULTRA -> 18.dp
    }
    val sciH = when (sizeTier) {
        KeypadSizeTier.NORMAL -> 44.dp
        KeypadSizeTier.COMPACT -> 32.dp
        KeypadSizeTier.ULTRA -> 18.dp
    }
    val numH = when (sizeTier) {
        KeypadSizeTier.NORMAL -> 48.dp
        KeypadSizeTier.COMPACT -> 36.dp
        KeypadSizeTier.ULTRA -> 20.dp
    }
    val eqH = when (sizeTier) {
        KeypadSizeTier.NORMAL -> 50.dp
        KeypadSizeTier.COMPACT -> 38.dp
        KeypadSizeTier.ULTRA -> 20.dp
    }
    val gap = when (sizeTier) {
        KeypadSizeTier.NORMAL -> 5.dp
        KeypadSizeTier.COMPACT -> 3.dp
        KeypadSizeTier.ULTRA -> 2.dp
    }
    val vPad = if (sizeTier == KeypadSizeTier.ULTRA) 2.dp else 4.dp
    val corner = if (sizeTier == KeypadSizeTier.ULTRA) 6.dp else 10.dp
    val numFont = when (sizeTier) {
        KeypadSizeTier.NORMAL -> 16.sp
        KeypadSizeTier.COMPACT -> 14.sp
        KeypadSizeTier.ULTRA -> 11.sp
    }
    val sciFont = when (sizeTier) {
        KeypadSizeTier.NORMAL -> 13.sp
        KeypadSizeTier.COMPACT -> 11.sp
        KeypadSizeTier.ULTRA -> 10.sp
    }
    val opFont = when (sizeTier) {
        KeypadSizeTier.NORMAL -> 20.sp
        KeypadSizeTier.COMPACT -> 17.sp
        KeypadSizeTier.ULTRA -> 12.sp
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(ApexBackground)
            .padding(horizontal = 8.dp, vertical = vPad),
        verticalArrangement = Arrangement.spacedBy(gap)
    ) {
        // Row 1: Scientific mode, angle, navigation & constants
        Row(horizontalArrangement = Arrangement.spacedBy(5.dp), modifier = Modifier.fillMaxWidth()) {
            KeypadButton(
                label = if (secondMode) "2nd●" else "2nd",
                containerColor = if (secondMode) ApexAmberContainer else ApexSurface,
                contentColor = if (secondMode) ApexAmber else ApexTextSecondary,
                height = topH,
                fontSize = sciFont,
                cornerRadius = corner,
                hapticEnabled = hapticEnabled,
                onClick = onToggleSecond
            )
            KeypadButton(
                label = angleUnit.name,
                containerColor = ApexSurface,
                contentColor = ApexCyan,
                height = topH,
                fontSize = sciFont,
                cornerRadius = corner,
                hapticEnabled = hapticEnabled,
                onClick = onToggleAngleUnit
            )
            KeypadButton(
                label = if (secondMode) "τ" else "π",
                containerColor = ApexSurface,
                contentColor = ApexTextPrimary,
                height = topH,
                fontSize = numFont,
                cornerRadius = corner,
                hapticEnabled = hapticEnabled,
                onClick = { onInsertToken(if (secondMode) "tau" else "π") }
            )
            KeypadButton(
                label = if (secondMode) "i" else "e",
                containerColor = ApexSurface,
                contentColor = ApexTextPrimary,
                height = topH,
                fontSize = numFont,
                cornerRadius = corner,
                hapticEnabled = hapticEnabled,
                onClick = { onInsertToken(if (secondMode) "i" else "e") }
            )
            KeypadButton(
                label = "◀",
                contentDesc = "Cursor left",
                containerColor = ApexSurface,
                contentColor = ApexTextSecondary,
                height = topH,
                fontSize = sciFont,
                cornerRadius = corner,
                hapticEnabled = hapticEnabled,
                onClick = { onMoveCursor(-1) }
            )
            KeypadButton(
                label = "▶",
                contentDesc = "Cursor right",
                containerColor = ApexSurface,
                contentColor = ApexTextSecondary,
                height = topH,
                fontSize = sciFont,
                cornerRadius = corner,
                hapticEnabled = hapticEnabled,
                onClick = { onMoveCursor(1) }
            )
        }

        // Row 2: Trig & Log functions
        Row(horizontalArrangement = Arrangement.spacedBy(5.dp), modifier = Modifier.fillMaxWidth()) {
            KeypadButton(
                label = if (secondMode) "asin" else "sin",
                containerColor = ApexSurface,
                contentColor = ApexTextPrimary,
                height = sciH,
                fontSize = sciFont,
                cornerRadius = corner,
                hapticEnabled = hapticEnabled,
                onClick = { onInsertFunction(if (secondMode) "asin" else "sin") }
            )
            KeypadButton(
                label = if (secondMode) "acos" else "cos",
                containerColor = ApexSurface,
                contentColor = ApexTextPrimary,
                height = sciH,
                fontSize = sciFont,
                cornerRadius = corner,
                hapticEnabled = hapticEnabled,
                onClick = { onInsertFunction(if (secondMode) "acos" else "cos") }
            )
            KeypadButton(
                label = if (secondMode) "atan" else "tan",
                containerColor = ApexSurface,
                contentColor = ApexTextPrimary,
                height = sciH,
                fontSize = sciFont,
                cornerRadius = corner,
                hapticEnabled = hapticEnabled,
                onClick = { onInsertFunction(if (secondMode) "atan" else "tan") }
            )
            KeypadButton(
                label = if (secondMode) "eˣ" else "ln",
                containerColor = ApexSurface,
                contentColor = ApexTextPrimary,
                height = sciH,
                fontSize = sciFont,
                cornerRadius = corner,
                hapticEnabled = hapticEnabled,
                onClick = { if (secondMode) onInsertFunction("exp") else onInsertFunction("ln") }
            )
            KeypadButton(
                label = if (secondMode) "10ˣ" else "log",
                containerColor = ApexSurface,
                contentColor = ApexTextPrimary,
                height = sciH,
                fontSize = sciFont,
                cornerRadius = corner,
                hapticEnabled = hapticEnabled,
                onClick = { if (secondMode) onInsertToken("10^") else onInsertFunction("log") }
            )
        }

        // Row 3: Powers, Roots, Combinatorics, Variables
        Row(horizontalArrangement = Arrangement.spacedBy(5.dp), modifier = Modifier.fillMaxWidth()) {
            KeypadButton(
                label = if (secondMode) "∛" else "√",
                containerColor = ApexSurface,
                contentColor = ApexTextPrimary,
                height = sciH,
                fontSize = numFont,
                cornerRadius = corner,
                hapticEnabled = hapticEnabled,
                onClick = { onInsertFunction(if (secondMode) "cbrt" else "sqrt") }
            )
            KeypadButton(
                label = if (secondMode) "x²" else "^",
                containerColor = ApexSurface,
                contentColor = ApexTextPrimary,
                height = sciH,
                fontSize = numFont,
                cornerRadius = corner,
                hapticEnabled = hapticEnabled,
                onClick = { onInsertToken(if (secondMode) "^2" else "^") }
            )
            KeypadButton(
                label = if (secondMode) "nPr" else "nCr",
                containerColor = ApexSurface,
                contentColor = ApexTextPrimary,
                height = sciH,
                fontSize = sciFont,
                cornerRadius = corner,
                hapticEnabled = hapticEnabled,
                onClick = { onInsertFunction(if (secondMode) "nPr" else "nCr") }
            )
            KeypadButton(
                label = if (secondMode) "x" else "!",
                containerColor = ApexSurface,
                contentColor = ApexTextPrimary,
                height = sciH,
                fontSize = numFont,
                cornerRadius = corner,
                hapticEnabled = hapticEnabled,
                onClick = { onInsertToken(if (secondMode) "x" else "!") }
            )
            KeypadButton(
                label = if (secondMode) "mod" else "a/b",
                containerColor = ApexSurface,
                contentColor = ApexAmber,
                height = sciH,
                fontSize = sciFont,
                cornerRadius = corner,
                hapticEnabled = hapticEnabled,
                onClick = { onInsertToken(if (secondMode) "%" else "/") }
            )
        }

        // Row 4: AC, (, ), ,, ÷
        Row(horizontalArrangement = Arrangement.spacedBy(5.dp), modifier = Modifier.fillMaxWidth()) {
            KeypadButton(
                label = "AC",
                containerColor = ApexErrorContainer,
                contentColor = ApexError,
                height = numH,
                fontSize = numFont,
                cornerRadius = corner,
                hapticEnabled = hapticEnabled,
                onClick = onClearAll
            )
            KeypadButton(
                label = "(",
                containerColor = ApexSurfaceElevated,
                contentColor = ApexCyan,
                height = numH,
                fontSize = numFont,
                cornerRadius = corner,
                hapticEnabled = hapticEnabled,
                onClick = { onInsertToken("(") }
            )
            KeypadButton(
                label = ")",
                containerColor = ApexSurfaceElevated,
                contentColor = ApexCyan,
                height = numH,
                fontSize = numFont,
                cornerRadius = corner,
                hapticEnabled = hapticEnabled,
                onClick = { onInsertToken(")") }
            )
            KeypadButton(
                label = ",",
                containerColor = ApexSurfaceElevated,
                contentColor = ApexTextSecondary,
                height = numH,
                fontSize = numFont,
                cornerRadius = corner,
                hapticEnabled = hapticEnabled,
                onClick = { onInsertToken(",") }
            )
            KeypadButton(
                label = "÷",
                containerColor = ApexCyanContainer,
                contentColor = ApexCyan,
                height = numH,
                fontSize = opFont,
                cornerRadius = corner,
                hapticEnabled = hapticEnabled,
                onClick = { onInsertToken("/") }
            )
        }

        // Row 5: 7 8 9 ×
        Row(horizontalArrangement = Arrangement.spacedBy(5.dp), modifier = Modifier.fillMaxWidth()) {
            KeypadButton("7", weight = 1f, height = numH, fontSize = numFont, cornerRadius = corner, hapticEnabled = hapticEnabled) { onInsertToken("7") }
            KeypadButton("8", weight = 1f, height = numH, fontSize = numFont, cornerRadius = corner, hapticEnabled = hapticEnabled) { onInsertToken("8") }
            KeypadButton("9", weight = 1f, height = numH, fontSize = numFont, cornerRadius = corner, hapticEnabled = hapticEnabled) { onInsertToken("9") }
            KeypadButton(
                label = "×",
                weight = 1f,
                containerColor = ApexCyanContainer,
                contentColor = ApexCyan,
                height = numH,
                fontSize = opFont,
                cornerRadius = corner,
                hapticEnabled = hapticEnabled
            ) { onInsertToken("*") }
        }

        // Row 6: 4 5 6 −
        Row(horizontalArrangement = Arrangement.spacedBy(5.dp), modifier = Modifier.fillMaxWidth()) {
            KeypadButton("4", weight = 1f, height = numH, fontSize = numFont, cornerRadius = corner, hapticEnabled = hapticEnabled) { onInsertToken("4") }
            KeypadButton("5", weight = 1f, height = numH, fontSize = numFont, cornerRadius = corner, hapticEnabled = hapticEnabled) { onInsertToken("5") }
            KeypadButton("6", weight = 1f, height = numH, fontSize = numFont, cornerRadius = corner, hapticEnabled = hapticEnabled) { onInsertToken("6") }
            KeypadButton(
                label = "−",
                weight = 1f,
                containerColor = ApexCyanContainer,
                contentColor = ApexCyan,
                height = numH,
                fontSize = opFont,
                cornerRadius = corner,
                hapticEnabled = hapticEnabled
            ) { onInsertToken("-") }
        }

        // Row 7: 1 2 3 +
        Row(horizontalArrangement = Arrangement.spacedBy(5.dp), modifier = Modifier.fillMaxWidth()) {
            KeypadButton("1", weight = 1f, height = numH, fontSize = numFont, cornerRadius = corner, hapticEnabled = hapticEnabled) { onInsertToken("1") }
            KeypadButton("2", weight = 1f, height = numH, fontSize = numFont, cornerRadius = corner, hapticEnabled = hapticEnabled) { onInsertToken("2") }
            KeypadButton("3", weight = 1f, height = numH, fontSize = numFont, cornerRadius = corner, hapticEnabled = hapticEnabled) { onInsertToken("3") }
            KeypadButton(
                label = "+",
                weight = 1f,
                containerColor = ApexCyanContainer,
                contentColor = ApexCyan,
                height = numH,
                fontSize = opFont,
                cornerRadius = corner,
                hapticEnabled = hapticEnabled
            ) { onInsertToken("+") }
        }

        // Row 8: Ans 0 . ⌫ =
        Row(horizontalArrangement = Arrangement.spacedBy(5.dp), modifier = Modifier.fillMaxWidth()) {
            KeypadButton(
                label = "Ans",
                weight = 1f,
                containerColor = ApexSurface,
                contentColor = ApexAmber,
                height = eqH,
                fontSize = sciFont,
                cornerRadius = corner,
                hapticEnabled = hapticEnabled,
                onClick = { onInsertToken("Ans") }
            )
            KeypadButton("0", weight = 1f, height = eqH, fontSize = numFont, cornerRadius = corner, hapticEnabled = hapticEnabled) { onInsertToken("0") }
            KeypadButton(".", weight = 1f, height = eqH, fontSize = numFont, cornerRadius = corner, hapticEnabled = hapticEnabled) { onInsertToken(".") }
            KeypadButton(
                label = "⌫",
                contentDesc = "Backspace, long press to clear",
                weight = 1f,
                containerColor = ApexSurface,
                contentColor = ApexTextSecondary,
                height = eqH,
                fontSize = numFont,
                cornerRadius = corner,
                hapticEnabled = hapticEnabled,
                onLongClick = onClearAll,
                onClick = onBackspace
            )
            KeypadButton(
                label = "=",
                contentDesc = "Evaluate",
                weight = 1.2f,
                containerColor = ApexCyan,
                contentColor = ApexBackground,
                height = eqH,
                fontSize = opFont,
                cornerRadius = corner,
                hapticEnabled = hapticEnabled,
                onClick = onEquals
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun RowScope.KeypadButton(
    label: String,
    weight: Float = 1f,
    containerColor: Color = ApexSurfaceElevated,
    contentColor: Color = ApexTextPrimary,
    height: Dp = 48.dp,
    fontSize: androidx.compose.ui.unit.TextUnit = 16.sp,
    cornerRadius: Dp = 10.dp,
    contentDesc: String? = null,
    hapticEnabled: Boolean = true,
    onLongClick: (() -> Unit)? = null,
    onClick: () -> Unit
) {
    val view = LocalView.current
    val shape = RoundedCornerShape(cornerRadius)
    Surface(
        modifier = Modifier
            .weight(weight)
            .height(height)
            .clip(shape)
            .combinedClickable(
                onClick = {
                    if (hapticEnabled) view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    onClick()
                },
                onLongClick = onLongClick?.let {
                    {
                        if (hapticEnabled) view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                        it()
                    }
                }
            )
            .semantics(mergeDescendants = true) {
                contentDescription = contentDesc ?: label
            },
        shape = shape,
        color = containerColor,
        border = BorderStroke(1.dp, ApexBorder.copy(alpha = 0.6f))
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                fontFamily = JetBrainsMonoFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = fontSize,
                color = contentColor,
                maxLines = 1
            )
        }
    }
}
