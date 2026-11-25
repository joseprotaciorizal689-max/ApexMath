package com.example.feature.graph

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.BookmarkAdd
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.CenterFocusStrong
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material.icons.outlined.ZoomIn
import androidx.compose.material.icons.outlined.ZoomOut
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.MainViewModel
import com.example.core.curriculum.CalcTargetTier
import com.example.core.math.MathFormatter
import com.example.core.ui.ApexActionChip
import com.example.core.ui.ApexCard
import com.example.core.ui.ApexPillBadge
import com.example.feature.calculator.apexFieldColors
import com.example.ui.theme.ApexAmber
import com.example.ui.theme.ApexAmberContainer
import com.example.ui.theme.ApexBackground
import com.example.ui.theme.ApexBorder
import com.example.ui.theme.ApexCyan
import com.example.ui.theme.ApexCyanContainer
import com.example.ui.theme.ApexError
import com.example.ui.theme.ApexSuccess
import com.example.ui.theme.ApexSurface
import com.example.ui.theme.ApexSurfaceElevated
import com.example.ui.theme.ApexTextMuted
import com.example.ui.theme.ApexTextPrimary
import com.example.ui.theme.ApexTextSecondary
import com.example.ui.theme.JetBrainsMonoFontFamily
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow

@Composable
fun GraphScreen(
    mainViewModel: MainViewModel,
    graphViewModel: GraphViewModel
) {
    val state by graphViewModel.uiState.collectAsStateWithLifecycle()
    val savedGraphs by graphViewModel.savedGraphs.collectAsStateWithLifecycle()
    var showSavedDrawer by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ApexBackground)
            .verticalScroll(rememberScrollState())
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Header + Zoom/Save controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Graphing Studio", style = MaterialTheme.typography.titleLarge, color = ApexTextPrimary)
                Text(
                    "x: [${fmt(state.xMin)}, ${fmt(state.xMax)}]  y: [${fmt(state.yMin)}, ${fmt(state.yMax)}]",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 11.sp,
                    color = ApexTextSecondary
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                IconButton(onClick = { graphViewModel.zoom(0.75) }, modifier = Modifier.size(44.dp)) {
                    Icon(Icons.Outlined.ZoomIn, contentDescription = "Zoom in", tint = ApexCyan)
                }
                IconButton(onClick = { graphViewModel.zoom(1.33) }, modifier = Modifier.size(44.dp)) {
                    Icon(Icons.Outlined.ZoomOut, contentDescription = "Zoom out", tint = ApexCyan)
                }
                IconButton(onClick = { graphViewModel.resetView() }, modifier = Modifier.size(44.dp)) {
                    Icon(Icons.Outlined.CenterFocusStrong, contentDescription = "Reset viewport", tint = ApexAmber)
                }
                IconButton(onClick = { showSavedDrawer = !showSavedDrawer }, modifier = Modifier.size(44.dp)) {
                    Icon(Icons.Outlined.FolderOpen, contentDescription = "Saved graphs", tint = ApexTextSecondary)
                }
            }
        }

        // Interactive Graph Canvas
        GraphCanvas(
            state = state,
            onPan = { dx, dy -> graphViewModel.pan(dx, dy) },
            onZoom = { factor -> graphViewModel.zoom(factor) },
            onTraceFraction = { frac -> graphViewModel.updateTraceAtScreenFraction(frac) }
        )

        // Live Trace / Tangent / Integral Readout bar
        if (state.traceX != null || state.tangentSlope != null || state.integralValue != null) {
            ApexCard(containerColor = ApexSurfaceElevated) {
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    state.traceX?.let { tx ->
                        val ty = state.traceY?.let { fmt(it) } ?: "undef"
                        Text("Trace: (${fmt(tx)}, $ty)", fontFamily = JetBrainsMonoFontFamily, fontSize = 12.sp, color = ApexCyan)
                    }
                    if (state.showTangent && state.tangentSlope != null && state.tangentY != null) {
                        Text(
                            "Tangent @ ${fmt(state.tangentX)}: y=${fmt(state.tangentY!!)}, m=${fmt(state.tangentSlope!!)}",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 12.sp,
                            color = ApexAmber
                        )
                    }
                    if (state.showIntegralShade && state.integralValue != null) {
                        Text(
                            "∫[${fmt(state.integralA)}, ${fmt(state.integralB)}] ≈ ${fmt(state.integralValue!!)}",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 12.sp,
                            color = ApexSuccess
                        )
                    }
                }
            }
        }

        // Analysis Mode Toggles
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = state.showDerivative,
                onClick = { graphViewModel.toggleDerivativeOverlay() },
                label = { Text("f'(x) Curve") },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = ApexCyanContainer, selectedLabelColor = ApexCyan)
            )
            FilterChip(
                selected = state.showTangent,
                onClick = { graphViewModel.toggleTangentOverlay() },
                label = { Text("Tangent Line") },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = ApexAmberContainer, selectedLabelColor = ApexAmber)
            )
            FilterChip(
                selected = state.showIntegralShade,
                onClick = { graphViewModel.toggleIntegralShade() },
                label = { Text("Area ∫ dx") },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = ApexCyanContainer, selectedLabelColor = ApexCyan)
            )
            ApexActionChip("Save Graph", Icons.Outlined.BookmarkAdd, onClick = {
                val primary = state.functions.firstOrNull()?.expression ?: "Graph"
                graphViewModel.saveCurrentGraph(primary)
                mainViewModel.saveToNotebook(
                    title = "Graph: $primary",
                    content = "Equations:\n${state.functions.joinToString("\n") { it.expression }}",
                    category = "Graph",
                    expression = primary,
                    graphExpressions = state.functions.joinToString(";") { it.expression }
                )
            })
            state.functions.firstOrNull { it.visible && it.expression.isNotBlank() }?.let { fn ->
                ApexActionChip("To Calc", Icons.Outlined.Calculate, onClick = {
                    mainViewModel.requestSendToCalculator(CalcTargetTier.CALCULUS, fn.expression, "Graph")
                })
            }
        }

        AnimatedVisibility(visible = state.showTangent) {
            ApexCard {
                Text("Tangent Point x₀ = ${fmt(state.tangentX)}", style = MaterialTheme.typography.labelLarge, color = ApexAmber)
                Slider(
                    value = state.tangentX.toFloat().coerceIn(state.xMin.toFloat(), state.xMax.toFloat()),
                    onValueChange = { graphViewModel.setTangentX(it.toDouble()) },
                    valueRange = state.xMin.toFloat()..state.xMax.toFloat(),
                    colors = SliderDefaults.colors(thumbColor = ApexAmber, activeTrackColor = ApexAmber)
                )
            }
        }

        AnimatedVisibility(visible = state.showIntegralShade) {
            ApexCard {
                Text("Integration Bounds [${fmt(state.integralA)}, ${fmt(state.integralB)}]", style = MaterialTheme.typography.labelLarge, color = ApexCyan)
                Slider(
                    value = state.integralA.toFloat().coerceIn(-8f, 8f),
                    onValueChange = { graphViewModel.setIntegralBounds(it.toDouble(), state.integralB) },
                    valueRange = -8f..8f,
                    colors = SliderDefaults.colors(thumbColor = ApexCyan, activeTrackColor = ApexCyan)
                )
                Slider(
                    value = state.integralB.toFloat().coerceIn(-8f, 8f),
                    onValueChange = { graphViewModel.setIntegralBounds(state.integralA, it.toDouble()) },
                    valueRange = -8f..8f,
                    colors = SliderDefaults.colors(thumbColor = ApexAmber, activeTrackColor = ApexAmber)
                )
            }
        }

        // Saved Graphs list (collapsible)
        AnimatedVisibility(visible = showSavedDrawer) {
            ApexCard(containerColor = ApexSurfaceElevated) {
                Text("Saved Graph Configurations", style = MaterialTheme.typography.titleSmall, color = ApexCyan)
                Spacer(Modifier.height(6.dp))
                if (savedGraphs.isEmpty()) {
                    Text("No saved graphs yet.", style = MaterialTheme.typography.bodySmall, color = ApexTextSecondary)
                } else {
                    savedGraphs.forEach { sg ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    graphViewModel.loadSavedGraph(sg)
                                    showSavedDrawer = false
                                }
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(sg.name, fontFamily = JetBrainsMonoFontFamily, fontSize = 12.sp, color = ApexTextPrimary)
                            IconButton(onClick = { graphViewModel.deleteSavedGraph(sg.id) }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Outlined.Close, contentDescription = "Delete saved graph", tint = ApexTextSecondary)
                            }
                        }
                    }
                }
            }
        }

        // Equation List
        ApexCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Equations f(x)", style = MaterialTheme.typography.titleSmall, color = ApexTextPrimary)
                ApexActionChip("Add f(x)", Icons.Outlined.Add, onClick = { graphViewModel.addFunction("") })
            }
            Spacer(Modifier.height(8.dp))
            state.functions.forEachIndexed { idx, item ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(item.color))
                    OutlinedTextField(
                        value = item.expression,
                        onValueChange = { graphViewModel.updateFunction(item.id, it) },
                        label = { Text("f${idx + 1}(x)", fontSize = 11.sp) },
                        singleLine = true,
                        isError = item.error != null,
                        modifier = Modifier.weight(1f).testTag("graph_fn_input_${idx}"),
                        colors = apexFieldColors()
                    )
                    IconButton(onClick = { graphViewModel.toggleVisibility(item.id) }, modifier = Modifier.size(40.dp)) {
                        Icon(
                            if (item.visible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                            contentDescription = "Toggle visibility",
                            tint = if (item.visible) item.color else ApexTextMuted
                        )
                    }
                    if (state.functions.size > 1) {
                        IconButton(onClick = { graphViewModel.removeFunction(item.id) }, modifier = Modifier.size(40.dp)) {
                            Icon(Icons.Outlined.Close, contentDescription = "Remove equation", tint = ApexTextSecondary)
                        }
                    }
                }
                item.error?.let { err ->
                    Text(err, fontSize = 11.sp, color = ApexError, modifier = Modifier.padding(start = 18.dp))
                }
            }
            state.derivativeExpr?.let { dExpr ->
                Spacer(Modifier.height(4.dp))
                Text("f₁'(x) = $dExpr", fontFamily = JetBrainsMonoFontFamily, fontSize = 11.sp, color = ApexAmber)
            }
        }

        // Key Features (Roots, Extrema, Intersections)
        if (state.features.isNotEmpty()) {
            ApexCard(containerColor = ApexSurfaceElevated) {
                Text("Detected Roots, Extrema & Intersections (Numerical)", style = MaterialTheme.typography.titleSmall, color = ApexAmber)
                Spacer(Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    state.features.forEach { feat ->
                        ApexPillBadge(
                            text = feat.label,
                            accentColor = when (feat.type) {
                                "Root" -> ApexCyan
                                "Intersection" -> ApexSuccess
                                else -> ApexAmber
                            }
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun GraphCanvas(
    state: GraphUiState,
    onPan: (Double, Double) -> Unit,
    onZoom: (Double) -> Unit,
    onTraceFraction: (Double) -> Unit
) {
    val textMeasurer = rememberTextMeasurer()

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = ApexBackground,
        border = BorderStroke(1.dp, ApexBorder),
        modifier = Modifier
            .fillMaxWidth()
            .height(310.dp)
            .testTag("graph_canvas")
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { centroid, pan, zoom, _ ->
                        if (kotlin.math.abs(zoom - 1f) > 0.01f) {
                            onZoom((1f / zoom).toDouble())
                        }
                        if (pan != Offset.Zero) {
                            onPan((pan.x / size.width).toDouble(), (pan.y / size.height).toDouble())
                        }
                        onTraceFraction((centroid.x / size.width).toDouble())
                    }
                }
        ) {
            val xSpan = (state.xMax - state.xMin).coerceAtLeast(1e-6)
            val ySpan = (state.yMax - state.yMin).coerceAtLeast(1e-6)

            fun toScreenX(x: Double): Float = ((x - state.xMin) / xSpan * size.width).toFloat()
            fun toScreenY(y: Double): Float = (size.height - (y - state.yMin) / ySpan * size.height).toFloat()

            // Grid lines
            val xStep = niceStep(xSpan / 6.0)
            val yStep = niceStep(ySpan / 5.0)
            val labelStyle = TextStyle(color = ApexTextMuted, fontSize = 9.sp, fontFamily = JetBrainsMonoFontFamily)

            var gx = ceil(state.xMin / xStep) * xStep
            while (gx <= state.xMax) {
                val sx = toScreenX(gx)
                drawLine(ApexBorder.copy(alpha = 0.45f), Offset(sx, 0f), Offset(sx, size.height), strokeWidth = 1f)
                if (kotlin.math.abs(gx) > xStep * 0.25 && sx in 12f..(size.width - 28f)) {
                    val axisY = toScreenY(0.0).coerceIn(4f, size.height - 16f)
                    drawText(textMeasurer, fmt(gx), Offset(sx + 2f, axisY + 2f), style = labelStyle)
                }
                gx += xStep
            }

            var gy = ceil(state.yMin / yStep) * yStep
            while (gy <= state.yMax) {
                val sy = toScreenY(gy)
                drawLine(ApexBorder.copy(alpha = 0.45f), Offset(0f, sy), Offset(size.width, sy), strokeWidth = 1f)
                if (kotlin.math.abs(gy) > yStep * 0.25 && sy in 10f..(size.height - 16f)) {
                    val axisX = toScreenX(0.0).coerceIn(4f, size.width - 36f)
                    drawText(textMeasurer, fmt(gy), Offset(axisX + 4f, sy - 11f), style = labelStyle)
                }
                gy += yStep
            }

            // Coordinate Axes
            if (0.0 in state.xMin..state.xMax) {
                val sx = toScreenX(0.0)
                drawLine(ApexTextSecondary.copy(alpha = 0.7f), Offset(sx, 0f), Offset(sx, size.height), strokeWidth = 2f)
            }
            if (0.0 in state.yMin..state.yMax) {
                val sy = toScreenY(0.0)
                drawLine(ApexTextSecondary.copy(alpha = 0.7f), Offset(0f, sy), Offset(size.width, sy), strokeWidth = 2f)
            }

            // Integral shading under primary function
            if (state.showIntegralShade) {
                val primary = state.functions.firstOrNull { it.visible }
                val lo = minOf(state.integralA, state.integralB)
                val hi = maxOf(state.integralA, state.integralB)
                val zeroY = toScreenY(0.0)
                primary?.segments?.forEach { seg ->
                    val sub = seg.filter { it.x in lo..hi }
                    if (sub.size >= 2) {
                        val p = Path().apply {
                            moveTo(toScreenX(sub.first().x), zeroY)
                            sub.forEach { pt -> lineTo(toScreenX(pt.x), toScreenY(pt.y)) }
                            lineTo(toScreenX(sub.last().x), zeroY)
                            close()
                        }
                        drawPath(p, color = primary.color.copy(alpha = 0.22f))
                    }
                }
            }

            // Function curves
            state.functions.filter { it.visible }.forEach { fn ->
                fn.segments.forEach { seg ->
                    val path = Path().apply {
                        seg.forEachIndexed { idx, pt ->
                            val sx = toScreenX(pt.x)
                            val sy = toScreenY(pt.y)
                            if (idx == 0) moveTo(sx, sy) else lineTo(sx, sy)
                        }
                    }
                    drawPath(path, color = fn.color, style = Stroke(width = 3.5f))
                }
            }

            // Derivative curve overlay (dashed amber)
            if (state.showDerivative) {
                val dash = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f)
                state.derivativeSegments.forEach { seg ->
                    val path = Path().apply {
                        seg.forEachIndexed { idx, pt ->
                            val sx = toScreenX(pt.x)
                            val sy = toScreenY(pt.y)
                            if (idx == 0) moveTo(sx, sy) else lineTo(sx, sy)
                        }
                    }
                    drawPath(path, color = ApexAmber, style = Stroke(width = 2.5f, pathEffect = dash))
                }
            }

            // Tangent line overlay
            if (state.showTangent && state.tangentY != null && state.tangentSlope != null) {
                val x0 = state.tangentX
                val y0 = state.tangentY
                val m = state.tangentSlope
                val yLeft = y0 + m * (state.xMin - x0)
                val yRight = y0 + m * (state.xMax - x0)
                drawLine(
                    color = ApexAmber,
                    start = Offset(toScreenX(state.xMin), toScreenY(yLeft)),
                    end = Offset(toScreenX(state.xMax), toScreenY(yRight)),
                    strokeWidth = 2.5f
                )
                drawCircle(ApexAmber, radius = 6f, center = Offset(toScreenX(x0), toScreenY(y0)))
            }

            // Feature markers (roots, extrema, intersections)
            state.features.forEach { feat ->
                if (feat.x in state.xMin..state.xMax && feat.y in state.yMin..state.yMax) {
                    val c = when (feat.type) {
                        "Root" -> ApexCyan
                        "Intersection" -> ApexSuccess
                        else -> ApexAmber
                    }
                    drawCircle(c, radius = 5.5f, center = Offset(toScreenX(feat.x), toScreenY(feat.y)))
                }
            }
        }
    }
}

private fun niceStep(rawStep: Double): Double {
    val exp = floor(log10(rawStep.coerceAtLeast(1e-6)))
    val mag = 10.0.pow(exp)
    val norm = rawStep / mag
    val nice = when {
        norm < 1.5 -> 1.0
        norm < 3.5 -> 2.0
        norm < 7.5 -> 5.0
        else -> 10.0
    }
    return nice * mag
}

private fun fmt(v: Double): String = MathFormatter.formatNumber(v, 3)
