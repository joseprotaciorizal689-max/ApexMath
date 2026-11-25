package com.example.feature.lessons

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.ShowChart
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.curriculum.LessonModule
import com.example.core.curriculum.SandboxType
import com.example.core.math.CalculusEngine
import com.example.core.math.GeometryEngine
import com.example.core.math.MathFormatter
import com.example.core.math.TriangleMode
import com.example.core.math.TriangleSolveResult
import com.example.core.ui.ApexActionChip
import com.example.core.ui.ApexCard
import com.example.feature.calculator.TriangleCanvasDiagram
import com.example.ui.theme.ApexAmber
import com.example.ui.theme.ApexBackground
import com.example.ui.theme.ApexBorder
import com.example.ui.theme.ApexCyan
import com.example.ui.theme.ApexSuccess
import com.example.ui.theme.ApexSurfaceElevated
import com.example.ui.theme.ApexTextPrimary
import com.example.ui.theme.ApexTextSecondary
import com.example.ui.theme.JetBrainsMonoFontFamily
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.tan

@Composable
fun LessonSandbox(
    module: LessonModule,
    onSendExprToCalc: (String) -> Unit,
    onSendExprToGraph: (String) -> Unit
) {
    ApexCard(containerColor = ApexSurfaceElevated) {
        Text(module.sandboxTitle, style = MaterialTheme.typography.titleSmall, color = ApexCyan)
        Spacer(Modifier.height(6.dp))

        var generatedExpr = module.sandboxExpression
        when (module.sandboxType) {
            SandboxType.LINEAR_EXPLORER -> {
                var m by remember { mutableFloatStateOf(1.5f) }
                var b by remember { mutableFloatStateOf(-2f) }
                generatedExpr = "${fmt(m)}*x ${if (b >= 0) "+ ${fmt(b)}" else "- ${fmt(-b)}"}"
                SandboxFunctionCanvas(fn = { x -> m * x + b })
                Spacer(Modifier.height(6.dp))
                Text(
                    "y = ${fmt(m)}x ${if (b >= 0) "+" else "-"} ${fmt(kotlin.math.abs(b))}   |   x-int = ${if (kotlin.math.abs(m) > 1e-4f) fmt(-b / m) else "none"}",
                    fontFamily = JetBrainsMonoFontFamily, fontSize = 12.sp, color = ApexAmber
                )
                Slider(value = m, onValueChange = { m = it }, valueRange = -4f..4f, colors = SliderDefaults.colors(thumbColor = ApexCyan, activeTrackColor = ApexCyan))
                Slider(value = b, onValueChange = { b = it }, valueRange = -5f..5f, colors = SliderDefaults.colors(thumbColor = ApexAmber, activeTrackColor = ApexAmber))
            }

            SandboxType.QUADRATIC_EXPLORER -> {
                var a by remember { mutableFloatStateOf(1f) }
                var b by remember { mutableFloatStateOf(-4f) }
                var c by remember { mutableFloatStateOf(3f) }
                generatedExpr = "${fmt(a)}*x^2 ${signed(b)}*x ${signed(c)}"
                SandboxFunctionCanvas(fn = { x -> a * x * x + b * x + c })
                Spacer(Modifier.height(6.dp))
                val disc = b * b - 4 * a * c
                val vx = if (kotlin.math.abs(a) > 1e-4f) -b / (2 * a) else 0f
                val vy = a * vx * vx + b * vx + c
                Text(
                    "y = ${fmt(a)}x² ${signed(b)}x ${signed(c)}  |  Δ = ${fmt(disc)}  |  Vertex(${fmt(vx)}, ${fmt(vy)})",
                    fontFamily = JetBrainsMonoFontFamily, fontSize = 11.sp, color = ApexAmber
                )
                Slider(value = a, onValueChange = { a = if (kotlin.math.abs(it) < 0.2f) 0.2f else it }, valueRange = -3f..3f, colors = SliderDefaults.colors(thumbColor = ApexCyan, activeTrackColor = ApexCyan))
                Slider(value = b, onValueChange = { b = it }, valueRange = -6f..6f, colors = SliderDefaults.colors(thumbColor = ApexAmber, activeTrackColor = ApexAmber))
                Slider(value = c, onValueChange = { c = it }, valueRange = -6f..6f, colors = SliderDefaults.colors(thumbColor = ApexSuccess, activeTrackColor = ApexSuccess))
            }

            SandboxType.EXPONENTIAL_LOG -> {
                var base by remember { mutableFloatStateOf(2f) }
                var scale by remember { mutableFloatStateOf(1f) }
                generatedExpr = "${fmt(scale)}*(${fmt(base)})^x"
                SandboxFunctionCanvas(fn = { x -> scale * base.toDouble().pow(x) })
                Spacer(Modifier.height(6.dp))
                Text(
                    "f(x) = ${fmt(scale)} · (${fmt(base)})^x   |   f(0) = ${fmt(scale)}   |   f(1) = ${fmt(scale * base)}",
                    fontFamily = JetBrainsMonoFontFamily, fontSize = 12.sp, color = ApexAmber
                )
                Slider(value = base, onValueChange = { base = it }, valueRange = 0.25f..4f, colors = SliderDefaults.colors(thumbColor = ApexCyan, activeTrackColor = ApexCyan))
                Slider(value = scale, onValueChange = { scale = it }, valueRange = 0.5f..3f, colors = SliderDefaults.colors(thumbColor = ApexAmber, activeTrackColor = ApexAmber))
            }

            SandboxType.TRIANGLE_INTERACTIVE -> {
                var sideA by remember { mutableFloatStateOf(5f) }
                var sideB by remember { mutableFloatStateOf(7f) }
                var angleC by remember { mutableFloatStateOf(60f) }
                val solve = GeometryEngine.solveTriangle(TriangleMode.SAS, sideA.toDouble(), angleC.toDouble(), sideB.toDouble())
                if (solve is TriangleSolveResult.Valid) {
                    val sol = solve.solutions.first()
                    TriangleCanvasDiagram(sol)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "a=${fmt(sol.a)}, b=${fmt(sol.b)}, c=${fmt(sol.c)} | C=${fmt(sol.angleCDeg)}° | Area=${fmt(sol.area)}",
                        fontFamily = JetBrainsMonoFontFamily, fontSize = 11.sp, color = ApexAmber
                    )
                    generatedExpr = "sqrt(${fmt(sideA)}^2 + ${fmt(sideB)}^2 - 2*${fmt(sideA)}*${fmt(sideB)}*cos(${fmt(angleC)}))"
                }
                Slider(value = sideA, onValueChange = { sideA = it }, valueRange = 2f..12f, colors = SliderDefaults.colors(thumbColor = ApexCyan, activeTrackColor = ApexCyan))
                Slider(value = angleC, onValueChange = { angleC = it }, valueRange = 15f..150f, colors = SliderDefaults.colors(thumbColor = ApexAmber, activeTrackColor = ApexAmber))
            }

            SandboxType.UNIT_CIRCLE -> {
                var angleDeg by remember { mutableFloatStateOf(45f) }
                val rad = Math.toRadians(angleDeg.toDouble())
                val c = cos(rad)
                val s = sin(rad)
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(175.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(ApexBackground)
                        .border(1.dp, ApexBorder, RoundedCornerShape(10.dp))
                        .pointerInput(Unit) {
                            detectDragGestures { change, _ ->
                                val cx = size.width / 2f
                                val cy = size.height / 2f
                                val dx = change.position.x - cx
                                val dy = cy - change.position.y
                                var deg = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
                                if (deg < 0f) deg += 360f
                                angleDeg = deg
                            }
                        }
                ) {
                    val cx = size.width / 2f
                    val cy = size.height / 2f
                    val r = minOf(size.width, size.height) * 0.38f
                    drawLine(ApexBorder, Offset(0f, cy), Offset(size.width, cy), 1.5f)
                    drawLine(ApexBorder, Offset(cx, 0f), Offset(cx, size.height), 1.5f)
                    drawCircle(ApexTextSecondary, radius = r, center = Offset(cx, cy), style = Stroke(2.5f))
                    val px = cx + (r * c).toFloat()
                    val py = cy - (r * s).toFloat()
                    drawLine(ApexCyan, Offset(cx, cy), Offset(px, py), 3.5f)
                    drawLine(ApexAmber, Offset(px, cy), Offset(px, py), 2.5f)
                    drawLine(ApexSuccess, Offset(cx, cy), Offset(px, cy), 2.5f)
                    drawCircle(ApexAmber, radius = 8f, center = Offset(px, py))
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    "Drag point! θ=${fmt(angleDeg)}° (${fmt(rad)} rad) | cos=${fmt(c)} | sin=${fmt(s)} | tan=${if (kotlin.math.abs(c) > 1e-4) fmt(tan(rad)) else "undef"}",
                    fontFamily = JetBrainsMonoFontFamily, fontSize = 11.sp, color = ApexAmber
                )
                Slider(value = angleDeg, onValueChange = { angleDeg = it }, valueRange = 0f..360f, colors = SliderDefaults.colors(thumbColor = ApexCyan, activeTrackColor = ApexCyan))
                generatedExpr = "sin(${fmt(rad)})"
            }

            SandboxType.SECANT_TANGENT -> {
                var x0 by remember { mutableFloatStateOf(1.0f) }
                var h by remember { mutableFloatStateOf(1.2f) }
                val f: (Double) -> Double = { x -> 0.5 * x * x }
                val f1 = f(x0.toDouble())
                val f2 = f((x0 + h).toDouble())
                val secantSlope = (f2 - f1) / h.toDouble()
                val trueSlope = x0.toDouble() // derivative of 0.5x^2 is x
                SandboxFunctionCanvas(
                    fn = f,
                    overlay = { toX, toY ->
                        val p1 = Offset(toX(x0.toDouble()), toY(f1))
                        val p2 = Offset(toX((x0 + h).toDouble()), toY(f2))
                        drawLine(ApexAmber, p1, p2, strokeWidth = 3f)
                        drawCircle(ApexCyan, 7f, p1)
                        drawCircle(ApexAmber, 6f, p2)
                    }
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "f(x)=0.5x² | x₀=${fmt(x0)}, h=${fmt(h)} | Secant=${fmt(secantSlope)} vs Tangent f'(x₀)=${fmt(trueSlope)}",
                    fontFamily = JetBrainsMonoFontFamily, fontSize = 11.sp, color = ApexAmber
                )
                Slider(value = x0, onValueChange = { x0 = it }, valueRange = -2.5f..2.5f, colors = SliderDefaults.colors(thumbColor = ApexCyan, activeTrackColor = ApexCyan))
                Slider(value = h, onValueChange = { h = it.coerceAtLeast(0.05f) }, valueRange = 0.05f..2.5f, colors = SliderDefaults.colors(thumbColor = ApexAmber, activeTrackColor = ApexAmber))
                generatedExpr = "0.5*x^2"
            }

            SandboxType.INTEGRAL_SHADE -> {
                var a by remember { mutableFloatStateOf(0f) }
                var b by remember { mutableFloatStateOf(2f) }
                val f: (Double) -> Double = { x -> x * x + 1.0 }
                val area = ((b * b * b / 3.0 + b) - (a * a * a / 3.0 + a))
                SandboxFunctionCanvas(
                    fn = f,
                    shadeRange = minOf(a, b).toDouble()..maxOf(a, b).toDouble()
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "∫[${fmt(a)}, ${fmt(b)}] (x² + 1) dx = ${fmt(area)}",
                    fontFamily = JetBrainsMonoFontFamily, fontSize = 12.sp, color = ApexCyan
                )
                Slider(value = a, onValueChange = { a = it }, valueRange = -3f..3f, colors = SliderDefaults.colors(thumbColor = ApexCyan, activeTrackColor = ApexCyan))
                Slider(value = b, onValueChange = { b = it }, valueRange = -3f..3f, colors = SliderDefaults.colors(thumbColor = ApexAmber, activeTrackColor = ApexAmber))
                generatedExpr = "x^2 + 1"
            }

            SandboxType.TAYLOR_APPROX -> {
                var order by remember { mutableIntStateOf(3) }
                val taylor = remember(order) { CalculusEngine.taylorSeries("sin(x)", 0.0, order, 1.0) }
                SandboxFunctionCanvas(
                    fn = { x -> sin(x) },
                    secondaryFn = { x ->
                        var sum = 0.0
                        var xp = 1.0
                        for (t in taylor.terms) {
                            sum += t.coeff * xp
                            xp *= x
                        }
                        sum
                    }
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "sin(x) vs T_$order(x) | Error at x=1: ${fmt(taylor.errorAtTest ?: 0.0)}",
                    fontFamily = JetBrainsMonoFontFamily, fontSize = 11.sp, color = ApexAmber
                )
                Slider(
                    value = order.toFloat(),
                    onValueChange = { order = it.toInt() },
                    valueRange = 1f..7f,
                    steps = 5,
                    colors = SliderDefaults.colors(thumbColor = ApexCyan, activeTrackColor = ApexCyan)
                )
                generatedExpr = taylor.polynomialExpr
            }
        }

        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ApexActionChip("To Calculator", Icons.Outlined.Calculate, onClick = { onSendExprToCalc(generatedExpr) })
            ApexActionChip("Open in Graph", Icons.Outlined.ShowChart, onClick = { onSendExprToGraph(generatedExpr) })
        }
    }
}

@Composable
private fun SandboxFunctionCanvas(
    fn: (Double) -> Double,
    secondaryFn: ((Double) -> Double)? = null,
    shadeRange: ClosedFloatingPointRange<Double>? = null,
    overlay: (androidx.compose.ui.graphics.drawscope.DrawScope.(toX: (Double) -> Float, toY: (Double) -> Float) -> Unit)? = null
) {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(155.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(ApexBackground)
            .border(1.dp, ApexBorder, RoundedCornerShape(10.dp))
    ) {
        val xMin = -4.0
        val xMax = 4.0
        val yMin = -4.0
        val yMax = 6.0
        fun toX(x: Double) = ((x - xMin) / (xMax - xMin) * size.width).toFloat()
        fun toY(y: Double) = (size.height - (y - yMin) / (yMax - yMin) * size.height).toFloat()

        drawLine(ApexBorder, Offset(0f, toY(0.0)), Offset(size.width, toY(0.0)), 1.5f)
        drawLine(ApexBorder, Offset(toX(0.0), 0f), Offset(toX(0.0), size.height), 1.5f)

        val steps = 120
        val dx = (xMax - xMin) / steps

        if (shadeRange != null) {
            val shadePath = Path()
            var started = false
            for (i in 0..steps) {
                val x = xMin + i * dx
                if (x in shadeRange) {
                    val y = fn(x).coerceIn(yMin, yMax)
                    if (!started) {
                        shadePath.moveTo(toX(x), toY(0.0))
                        started = true
                    }
                    shadePath.lineTo(toX(x), toY(y))
                }
            }
            if (started) {
                shadePath.lineTo(toX(shadeRange.endInclusive), toY(0.0))
                shadePath.close()
                drawPath(shadePath, color = ApexCyan.copy(alpha = 0.25f))
            }
        }

        val path = Path()
        for (i in 0..steps) {
            val x = xMin + i * dx
            val y = fn(x).coerceIn(-20.0, 20.0)
            if (i == 0) path.moveTo(toX(x), toY(y)) else path.lineTo(toX(x), toY(y))
        }
        drawPath(path, color = ApexCyan, style = Stroke(3f))

        if (secondaryFn != null) {
            val p2 = Path()
            for (i in 0..steps) {
                val x = xMin + i * dx
                val y = secondaryFn(x).coerceIn(-20.0, 20.0)
                if (i == 0) p2.moveTo(toX(x), toY(y)) else p2.lineTo(toX(x), toY(y))
            }
            drawPath(p2, color = ApexAmber, style = Stroke(2.5f))
        }

        overlay?.invoke(this, ::toX, ::toY)
    }
}

private fun fmt(v: Float): String = MathFormatter.formatNumber(v.toDouble(), 2)
private fun fmt(v: Double): String = MathFormatter.formatNumber(v, 2)
private fun signed(v: Float): String = if (v >= 0) "+ ${fmt(v)}" else "- ${fmt(-v)}"
