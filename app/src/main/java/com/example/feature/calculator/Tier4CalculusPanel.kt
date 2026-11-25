package com.example.feature.calculator

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BookmarkAdd
import androidx.compose.material.icons.outlined.ShowChart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.math.GraphPoint
import com.example.core.math.MathFormatter
import com.example.core.math.SymbolicIntegralResult
import com.example.core.ui.ApexActionChip
import com.example.core.ui.ApexCard
import com.example.core.ui.ApexPillBadge
import com.example.core.ui.ApexSectionTitle
import com.example.core.ui.MonoFormulaBox
import com.example.core.ui.StatusBanner
import com.example.ui.theme.ApexAmber
import com.example.ui.theme.ApexAmberContainer
import com.example.ui.theme.ApexBackground
import com.example.ui.theme.ApexBorder
import com.example.ui.theme.ApexCyan
import com.example.ui.theme.ApexCyanContainer
import com.example.ui.theme.ApexSuccess
import com.example.ui.theme.ApexSurfaceElevated
import com.example.ui.theme.ApexTextPrimary
import com.example.ui.theme.ApexTextSecondary
import com.example.ui.theme.JetBrainsMonoFontFamily

@Composable
fun Tier4CalculusPanel(
    viewModel: CalculatorViewModel,
    onSendToGraph: (List<String>) -> Unit,
    onSaveToNotebook: (title: String, content: String, expr: String) -> Unit
) {
    var selectedTool by remember { mutableIntStateOf(0) }
    val tools = listOf("Derivative", "Integral", "Limits", "Taylor Series", "Root Finder")
    var expr by remember { mutableStateOf("x^3 - 3*x") }

    val injected by viewModel.injectedExprHint.collectAsStateWithLifecycle()
    LaunchedEffect(injected) {
        val v = viewModel.consumeInjectedHint()
        if (!v.isNullOrBlank()) expr = v
    }

    val error by viewModel.calculusError.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            tools.forEachIndexed { idx, label ->
                FilterChip(
                    selected = selectedTool == idx,
                    onClick = { selectedTool = idx },
                    label = { Text(label) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = ApexCyanContainer, selectedLabelColor = ApexCyan)
                )
            }
        }

        ApexCard {
            OutlinedTextField(
                value = expr,
                onValueChange = { expr = it },
                label = { Text("Function f(x)") },
                placeholder = { Text("e.g. sin(x)*exp(-0.2*x) or x^3 - 3*x") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = apexFieldColors()
            )
            Spacer(Modifier.height(6.dp))
            Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("x^3 - 3*x", "sin(x)/x", "exp(-x^2)", "ln(x^2 + 1)", "x*cos(x)").forEach { preset ->
                    FilterChip(
                        selected = expr == preset,
                        onClick = { expr = preset },
                        label = { Text(preset, fontFamily = JetBrainsMonoFontFamily, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = ApexAmberContainer, selectedLabelColor = ApexAmber)
                    )
                }
            }
        }

        error?.let { StatusBanner(it, isError = true) }

        when (selectedTool) {
            0 -> DerivativeTool(expr, viewModel, onSendToGraph, onSaveToNotebook)
            1 -> IntegralTool(expr, viewModel, onSendToGraph, onSaveToNotebook)
            2 -> LimitTool(expr, viewModel, onSaveToNotebook)
            3 -> TaylorTool(expr, viewModel, onSendToGraph, onSaveToNotebook)
            4 -> RootFinderTool(expr, viewModel, onSendToGraph, onSaveToNotebook)
        }
    }
}

@Composable
private fun DerivativeTool(
    expr: String,
    viewModel: CalculatorViewModel,
    onSendToGraph: (List<String>) -> Unit,
    onSave: (String, String, String) -> Unit
) {
    var evalX by remember { mutableStateOf("2") }
    val diffRes by viewModel.diffResult.collectAsStateWithLifecycle()

    ApexCard {
        ApexSectionTitle("Symbolic & Numerical Derivative", "AST differentiation + chain/product/quotient rules")
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = evalX,
            onValueChange = { evalX = it },
            label = { Text("Evaluate f'(x) at x = (optional)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = apexFieldColors()
        )
        Spacer(Modifier.height(10.dp))
        Button(
            onClick = { viewModel.runDifferentiation(expr, evalX.toDoubleOrNull()) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = ApexCyan, contentColor = ApexBackground)
        ) {
            Text("Differentiate f(x)", fontWeight = FontWeight.SemiBold)
        }
    }

    diffRes?.let { res ->
        ApexCard(containerColor = ApexSurfaceElevated) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ApexPillBadge("SYMBOLIC", ApexSuccess)
                if (res.numericAtPoint != null) ApexPillBadge("f'(${MathFormatter.formatNumber(res.pointX ?: 0.0)}) = ${MathFormatter.formatNumber(res.numericAtPoint, 6)}", ApexCyan)
            }
            Spacer(Modifier.height(8.dp))
            MonoFormulaBox("f'(x) = ${res.derivativeFormatted}")
            res.secondDerivativeFormatted?.let { d2 ->
                Spacer(Modifier.height(6.dp))
                MonoFormulaBox("f''(x) = $d2", accent = ApexAmber)
            }
            Spacer(Modifier.height(8.dp))
            res.steps.forEach { st ->
                Text("• $st", style = MaterialTheme.typography.bodySmall, color = ApexTextSecondary)
            }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ApexActionChip("Plot f & f'", Icons.Outlined.ShowChart, onClick = {
                    onSendToGraph(listOf(expr, res.derivativeFormatted))
                })
                ApexActionChip("Save Note", Icons.Outlined.BookmarkAdd, onClick = {
                    onSave("Derivative of $expr", "f'(x) = ${res.derivativeFormatted}\nf''(x) = ${res.secondDerivativeFormatted ?: "N/A"}", res.derivativeFormatted)
                })
            }
        }
    }
}

@Composable
private fun IntegralTool(
    expr: String,
    viewModel: CalculatorViewModel,
    onSendToGraph: (List<String>) -> Unit,
    onSave: (String, String, String) -> Unit
) {
    var lower by remember { mutableFloatStateOf(0f) }
    var upper by remember { mutableFloatStateOf(2f) }
    val symRes by viewModel.symIntResult.collectAsStateWithLifecycle()
    val defRes by viewModel.defIntResult.collectAsStateWithLifecycle()
    val curve by viewModel.integralCurve.collectAsStateWithLifecycle()

    ApexCard {
        ApexSectionTitle("Symbolic & Definite Integration", "Elementary antiderivative + Adaptive Simpson's quadrature")
        Spacer(Modifier.height(8.dp))
        Text("Lower bound a = ${MathFormatter.formatNumber(lower.toDouble(), 2)}", style = MaterialTheme.typography.labelLarge, color = ApexCyan)
        Slider(
            value = lower,
            onValueChange = {
                lower = it
                viewModel.runIntegration(expr, lower.toDouble(), upper.toDouble())
            },
            valueRange = -5f..5f,
            colors = SliderDefaults.colors(thumbColor = ApexCyan, activeTrackColor = ApexCyan)
        )
        Text("Upper bound b = ${MathFormatter.formatNumber(upper.toDouble(), 2)}", style = MaterialTheme.typography.labelLarge, color = ApexAmber)
        Slider(
            value = upper,
            onValueChange = {
                upper = it
                viewModel.runIntegration(expr, lower.toDouble(), upper.toDouble())
            },
            valueRange = -5f..5f,
            colors = SliderDefaults.colors(thumbColor = ApexAmber, activeTrackColor = ApexAmber)
        )
        Button(
            onClick = { viewModel.runIntegration(expr, lower.toDouble(), upper.toDouble()) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = ApexCyan, contentColor = ApexBackground)
        ) {
            Text("Integrate ∫ f(x) dx", fontWeight = FontWeight.SemiBold)
        }
    }

    if (curve.isNotEmpty()) {
        ApexCard(containerColor = ApexSurfaceElevated) {
            Text("Graphical Area Preview [${MathFormatter.formatNumber(lower.toDouble(), 2)}, ${MathFormatter.formatNumber(upper.toDouble(), 2)}]", style = MaterialTheme.typography.titleSmall, color = ApexTextPrimary)
            Spacer(Modifier.height(8.dp))
            IntegralPlotCanvas(curve, lower.toDouble(), upper.toDouble())
        }
    }

    defRes?.let { def ->
        ApexCard(containerColor = ApexSurfaceElevated) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ApexPillBadge("NUMERICAL", ApexAmber, ApexAmberContainer)
                ApexPillBadge("Evals: ${def.evaluations}", ApexCyan)
            }
            Spacer(Modifier.height(6.dp))
            MonoFormulaBox("∫[${MathFormatter.formatNumber(lower.toDouble(), 2)}, ${MathFormatter.formatNumber(upper.toDouble(), 2)}] ≈ ${MathFormatter.formatNumber(def.value, 8)}")
            Spacer(Modifier.height(4.dp))
            Text("${def.method} • Est. error ≤ ${MathFormatter.formatNumber(def.estimatedError, 3)}", style = MaterialTheme.typography.bodySmall, color = ApexTextSecondary)
            def.warning?.let {
                Spacer(Modifier.height(6.dp))
                StatusBanner(it)
            }
        }
    }

    symRes?.let { sym ->
        ApexCard(containerColor = ApexSurfaceElevated) {
            when (sym) {
                is SymbolicIntegralResult.Success -> {
                    ApexPillBadge("SYMBOLIC ANTIDERIVATIVE", ApexSuccess)
                    Spacer(Modifier.height(6.dp))
                    MonoFormulaBox("∫ f(x) dx = ${sym.antiderivative}")
                    sym.steps.forEach { Text("• $it", style = MaterialTheme.typography.bodySmall, color = ApexTextSecondary) }
                }
                is SymbolicIntegralResult.Unsupported -> {
                    ApexPillBadge("SYMBOLIC UNAVAILABLE", ApexAmber, ApexAmberContainer)
                    Spacer(Modifier.height(6.dp))
                    Text(sym.reason, style = MaterialTheme.typography.bodySmall, color = ApexTextSecondary)
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ApexActionChip("Plot f(x)", Icons.Outlined.ShowChart, onClick = { onSendToGraph(listOf(expr)) })
                defRes?.let { def ->
                    ApexActionChip("Save Note", Icons.Outlined.BookmarkAdd, onClick = {
                        onSave("Integral of $expr", "Definite [${lower}, ${upper}] ≈ ${MathFormatter.formatNumber(def.value, 8)}", expr)
                    })
                }
            }
        }
    }
}

@Composable
private fun IntegralPlotCanvas(
    segments: List<List<GraphPoint>>,
    a: Double,
    b: Double
) {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(ApexBackground)
            .border(1.dp, ApexBorder, RoundedCornerShape(10.dp))
    ) {
        val allPts = segments.flatten()
        if (allPts.isEmpty()) return@Canvas
        val xMin = allPts.minOf { it.x }
        val xMax = allPts.maxOf { it.x }.coerceAtLeast(xMin + 1e-3)
        val yMin = minOf(0.0, allPts.minOf { it.y }).coerceAtLeast(-20.0)
        val yMax = maxOf(0.0, allPts.maxOf { it.y }).coerceAtMost(20.0).coerceAtLeast(yMin + 1e-3)

        fun toX(x: Double) = ((x - xMin) / (xMax - xMin) * size.width).toFloat()
        fun toY(y: Double) = (size.height - (y - yMin) / (yMax - yMin) * size.height).toFloat()

        val zeroY = toY(0.0)
        drawLine(ApexBorder, Offset(0f, zeroY), Offset(size.width, zeroY), strokeWidth = 2f)

        val lo = minOf(a, b)
        val hi = maxOf(a, b)
        // Shade area between lo and hi
        segments.forEach { seg ->
            val inRange = seg.filter { it.x in lo..hi }
            if (inRange.size >= 2) {
                val fillPath = Path().apply {
                    moveTo(toX(inRange.first().x), zeroY)
                    inRange.forEach { pt -> lineTo(toX(pt.x), toY(pt.y)) }
                    lineTo(toX(inRange.last().x), zeroY)
                    close()
                }
                drawPath(fillPath, color = ApexCyan.copy(alpha = 0.25f))
            }
            val linePath = Path().apply {
                seg.forEachIndexed { idx, pt ->
                    val px = toX(pt.x)
                    val py = toY(pt.y)
                    if (idx == 0) moveTo(px, py) else lineTo(px, py)
                }
            }
            drawPath(linePath, color = ApexCyan, style = Stroke(width = 3f))
        }
    }
}

@Composable
private fun LimitTool(
    expr: String,
    viewModel: CalculatorViewModel,
    onSave: (String, String, String) -> Unit
) {
    var target by remember { mutableStateOf("0") }
    val limitRes by viewModel.limitResult.collectAsStateWithLifecycle()

    ApexCard {
        ApexSectionTitle("Limit Convergence Analyzer", "Left-hand, right-hand, and two-sided numerical table")
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = target, onValueChange = { target = it }, label = { Text("x approaches a =") }, singleLine = true, modifier = Modifier.fillMaxWidth(), colors = apexFieldColors())
        Spacer(Modifier.height(10.dp))
        Button(
            onClick = { viewModel.runLimit(expr, target.toDoubleOrNull() ?: 0.0) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = ApexCyan, contentColor = ApexBackground)
        ) {
            Text("Evaluate Limit", fontWeight = FontWeight.SemiBold)
        }
    }

    limitRes?.let { res ->
        ApexCard(containerColor = ApexSurfaceElevated) {
            ApexPillBadge(
                text = "NUMERICAL ESTIMATE",
                accentColor = if (res.isWarning) ApexAmber else ApexCyan,
                containerColor = if (res.isWarning) ApexAmberContainer else ApexCyanContainer
            )
            Spacer(Modifier.height(6.dp))
            MonoFormulaBox(res.status, accent = if (res.isWarning) ApexAmber else ApexCyan)
            Spacer(Modifier.height(4.dp))
            Text(res.note, style = MaterialTheme.typography.bodySmall, color = ApexTextSecondary)
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("From Left (x → ${res.targetDisplay}⁻)", style = MaterialTheme.typography.labelMedium, color = ApexCyan)
                    res.leftSamples.forEach { s ->
                        Text(
                            "${MathFormatter.formatNumber(s.x, 6)} → ${s.fx?.let { MathFormatter.formatNumber(it, 6) } ?: "undef"}",
                            fontFamily = JetBrainsMonoFontFamily, fontSize = 10.sp, color = ApexTextPrimary
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("From Right (x → ${res.targetDisplay}⁺)", style = MaterialTheme.typography.labelMedium, color = ApexAmber)
                    res.rightSamples.forEach { s ->
                        Text(
                            "${MathFormatter.formatNumber(s.x, 6)} → ${s.fx?.let { MathFormatter.formatNumber(it, 6) } ?: "undef"}",
                            fontFamily = JetBrainsMonoFontFamily, fontSize = 10.sp, color = ApexTextPrimary
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            ApexActionChip("Save Note", Icons.Outlined.BookmarkAdd, onClick = {
                onSave("Limit of $expr as x→${res.targetDisplay}", "${res.status}\n${res.note}", expr)
            })
        }
    }
}

@Composable
private fun TaylorTool(
    expr: String,
    viewModel: CalculatorViewModel,
    onSendToGraph: (List<String>) -> Unit,
    onSave: (String, String, String) -> Unit
) {
    var center by remember { mutableStateOf("0") }
    var order by remember { mutableIntStateOf(5) }
    var testX by remember { mutableStateOf("0.5") }
    val taylorRes by viewModel.taylorResult.collectAsStateWithLifecycle()

    ApexCard {
        ApexSectionTitle("Taylor & Maclaurin Series", "Symbolic derivatives up to degree 8")
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(value = center, onValueChange = { center = it }, label = { Text("Center a") }, singleLine = true, modifier = Modifier.weight(1f), colors = apexFieldColors())
            OutlinedTextField(value = testX, onValueChange = { testX = it }, label = { Text("Test x") }, singleLine = true, modifier = Modifier.weight(1f), colors = apexFieldColors())
        }
        Spacer(Modifier.height(6.dp))
        Text("Order N = $order", style = MaterialTheme.typography.labelLarge, color = ApexCyan)
        Slider(
            value = order.toFloat(),
            onValueChange = { order = it.toInt() },
            valueRange = 1f..8f,
            steps = 6,
            colors = SliderDefaults.colors(thumbColor = ApexCyan, activeTrackColor = ApexCyan)
        )
        Button(
            onClick = { viewModel.runTaylor(expr, center.toDoubleOrNull() ?: 0.0, order, testX.toDoubleOrNull()) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = ApexCyan, contentColor = ApexBackground)
        ) {
            Text("Generate Series", fontWeight = FontWeight.SemiBold)
        }
    }

    taylorRes?.let { res ->
        ApexCard(containerColor = ApexSurfaceElevated) {
            Text("T_${res.order}(x) centered at ${MathFormatter.formatNumber(res.center)}", style = MaterialTheme.typography.titleSmall, color = ApexAmber)
            Spacer(Modifier.height(6.dp))
            MonoFormulaBox(res.polynomialExpr)
            if (res.approxAtTest != null && res.exactAtTest != null) {
                Spacer(Modifier.height(6.dp))
                Text(
                    "At x=${MathFormatter.formatNumber(res.testX ?: 0.0)}: Exact=${MathFormatter.formatNumber(res.exactAtTest, 7)} | Taylor=${MathFormatter.formatNumber(res.approxAtTest, 7)} | |Err|=${MathFormatter.formatNumber(res.errorAtTest ?: 0.0, 4)}",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 11.sp,
                    color = ApexSuccess
                )
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ApexActionChip("Compare on Graph", Icons.Outlined.ShowChart, onClick = {
                    onSendToGraph(listOf(expr, res.polynomialExpr))
                })
                ApexActionChip("Save Note", Icons.Outlined.BookmarkAdd, onClick = {
                    onSave("Taylor T_${res.order} of $expr", res.polynomialExpr, res.polynomialExpr)
                })
            }
        }
    }
}

@Composable
private fun RootFinderTool(
    expr: String,
    viewModel: CalculatorViewModel,
    onSendToGraph: (List<String>) -> Unit,
    onSave: (String, String, String) -> Unit
) {
    var method by remember { mutableStateOf("Newton") }
    var p1 by remember { mutableStateOf("1.5") }
    var p2 by remember { mutableStateOf("2.5") }
    val rootRes by viewModel.rootResult.collectAsStateWithLifecycle()

    ApexCard {
        ApexSectionTitle("Numerical Root Finding", "Bisection, Newton-Raphson, and Secant methods")
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Bisection", "Newton", "Secant").forEach { m ->
                FilterChip(
                    selected = method == m,
                    onClick = { method = m },
                    label = { Text(m) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = ApexCyanContainer, selectedLabelColor = ApexCyan)
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = p1,
                onValueChange = { p1 = it },
                label = { Text(if (method == "Bisection") "Lower a" else "Initial x₀") },
                singleLine = true,
                modifier = Modifier.weight(1f),
                colors = apexFieldColors()
            )
            if (method != "Newton") {
                OutlinedTextField(
                    value = p2,
                    onValueChange = { p2 = it },
                    label = { Text(if (method == "Bisection") "Upper b" else "Second x₁") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    colors = apexFieldColors()
                )
            }
        }
        Spacer(Modifier.height(10.dp))
        Button(
            onClick = { viewModel.runRootFinder(method, expr, p1.toDoubleOrNull() ?: 0.0, p2.toDoubleOrNull() ?: 1.0) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = ApexCyan, contentColor = ApexBackground)
        ) {
            Text("Find Root f(x) = 0", fontWeight = FontWeight.SemiBold)
        }
    }

    rootRes?.let { res ->
        ApexCard(containerColor = ApexSurfaceElevated) {
            ApexPillBadge(
                text = if (res.converged) "${res.method} Converged" else "${res.method} Warning",
                accentColor = if (res.converged) ApexSuccess else ApexAmber
            )
            Spacer(Modifier.height(6.dp))
            MonoFormulaBox(
                res.root?.let { "Root ≈ ${MathFormatter.formatNumber(it, 9)}\nResidual |f(x)| = ${MathFormatter.formatNumber(res.residual ?: 0.0, 4)}" }
                    ?: res.message,
                accent = if (res.converged) ApexCyan else ApexAmber
            )
            Spacer(Modifier.height(4.dp))
            Text(res.message, style = MaterialTheme.typography.bodySmall, color = ApexTextSecondary)
            if (res.iterations.isNotEmpty()) {
                Spacer(Modifier.height(6.dp))
                res.iterations.take(8).forEach { itRow ->
                    Text(
                        "k=${itRow.iter}: x = ${MathFormatter.formatNumber(itRow.x, 7)}, f(x) = ${MathFormatter.formatNumber(itRow.fx, 4)}",
                        fontFamily = JetBrainsMonoFontFamily, fontSize = 11.sp, color = ApexTextPrimary
                    )
                }
                if (res.iterations.size > 8) {
                    Text("… ${res.iterations.size} total iterations", fontSize = 11.sp, color = ApexTextSecondary)
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ApexActionChip("Plot f(x)", Icons.Outlined.ShowChart, onClick = { onSendToGraph(listOf(expr)) })
                res.root?.let { r ->
                    ApexActionChip("Save Note", Icons.Outlined.BookmarkAdd, onClick = {
                        onSave("Root of $expr (${res.method})", "x ≈ ${MathFormatter.formatNumber(r, 9)}\n${res.message}", expr)
                    })
                }
            }
        }
    }
}
