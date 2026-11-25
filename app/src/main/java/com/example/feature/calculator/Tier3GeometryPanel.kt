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
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.math.GeometryEngine
import com.example.core.math.GeometrySolveResult
import com.example.core.math.MathFormatter
import com.example.core.math.TriangleMode
import com.example.core.math.TriangleSolution
import com.example.core.math.TriangleSolveResult
import com.example.core.ui.ApexActionChip
import com.example.core.ui.ApexCard
import com.example.core.ui.ApexPillBadge
import com.example.core.ui.ApexSectionTitle
import com.example.core.ui.StatusBanner
import com.example.ui.theme.ApexAmber
import com.example.ui.theme.ApexBackground
import com.example.ui.theme.ApexBorder
import com.example.ui.theme.ApexCyan
import com.example.ui.theme.ApexCyanContainer
import com.example.ui.theme.ApexSurfaceElevated
import com.example.ui.theme.ApexTextPrimary
import com.example.ui.theme.ApexTextSecondary
import com.example.ui.theme.JetBrainsMonoFontFamily
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun Tier3GeometryPanel(
    viewModel: CalculatorViewModel,
    onSendExprToStandard: (String) -> Unit,
    onSaveToNotebook: (title: String, content: String, expr: String) -> Unit
) {
    var selectedTool by remember { mutableIntStateOf(0) }
    val tools = listOf("Triangle", "2D Shapes", "3D Solids", "Coordinates")

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

        when (selectedTool) {
            0 -> TriangleSolverSection(viewModel, onSendExprToStandard, onSaveToNotebook)
            1 -> TwoDShapesSection(viewModel, onSendExprToStandard, onSaveToNotebook)
            2 -> ThreeDSolidsSection(viewModel, onSendExprToStandard, onSaveToNotebook)
            3 -> CoordinateGeometrySection(viewModel, onSendExprToStandard, onSaveToNotebook)
        }
    }
}

@Composable
private fun TriangleSolverSection(
    viewModel: CalculatorViewModel,
    onSendExpr: (String) -> Unit,
    onSave: (String, String, String) -> Unit
) {
    var mode by remember { mutableStateOf(TriangleMode.SSS) }
    var p1 by remember { mutableStateOf("5") }
    var p2 by remember { mutableStateOf("6") }
    var p3 by remember { mutableStateOf("7") }
    val result by viewModel.triangleResult.collectAsStateWithLifecycle()

    val labels = when (mode) {
        TriangleMode.SSS -> Triple("Side a", "Side b", "Side c")
        TriangleMode.SAS -> Triple("Side a", "Angle C (°)", "Side b")
        TriangleMode.ASA -> Triple("Angle A (°)", "Side c", "Angle B (°)")
        TriangleMode.AAS -> Triple("Angle A (°)", "Angle B (°)", "Side a")
        TriangleMode.SSA -> Triple("Side a", "Side b", "Angle A (°)")
    }

    ApexCard {
        ApexSectionTitle("Triangle Solver", "Law of Sines, Law of Cosines, Heron's formula & SSA ambiguity")
        Spacer(Modifier.height(8.dp))
        Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            TriangleMode.entries.forEach { m ->
                FilterChip(
                    selected = mode == m,
                    onClick = { mode = m },
                    label = { Text(m.name) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = ApexCyanContainer, selectedLabelColor = ApexCyan)
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(value = p1, onValueChange = { p1 = it }, label = { Text(labels.first, fontSize = 11.sp) }, singleLine = true, modifier = Modifier.weight(1f), colors = apexFieldColors())
            OutlinedTextField(value = p2, onValueChange = { p2 = it }, label = { Text(labels.second, fontSize = 11.sp) }, singleLine = true, modifier = Modifier.weight(1f), colors = apexFieldColors())
            OutlinedTextField(value = p3, onValueChange = { p3 = it }, label = { Text(labels.third, fontSize = 11.sp) }, singleLine = true, modifier = Modifier.weight(1f), colors = apexFieldColors())
        }
        Spacer(Modifier.height(10.dp))
        Button(
            onClick = {
                viewModel.solveTriangle(
                    mode,
                    p1.toDoubleOrNull() ?: 0.0,
                    p2.toDoubleOrNull() ?: 0.0,
                    p3.toDoubleOrNull() ?: 0.0
                )
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = ApexCyan, contentColor = ApexBackground)
        ) {
            Text("Solve Triangle", fontWeight = FontWeight.SemiBold)
        }
    }

    when (val res = result) {
        is TriangleSolveResult.Invalid -> StatusBanner(res.reason, isError = true)
        is TriangleSolveResult.Valid -> {
            res.solutions.forEachIndexed { idx, sol ->
                ApexCard(containerColor = ApexSurfaceElevated) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            if (res.solutions.size > 1) "Solution #${idx + 1}" else "Triangle Diagram & Measurements",
                            style = MaterialTheme.typography.titleSmall,
                            color = ApexAmber
                        )
                        if (res.isAmbiguous) ApexPillBadge("Ambiguous SSA", ApexAmber)
                    }
                    Spacer(Modifier.height(8.dp))
                    TriangleCanvasDiagram(sol)
                    Spacer(Modifier.height(10.dp))
                    val lines = listOf(
                        "Sides: a=${fmt(sol.a)}, b=${fmt(sol.b)}, c=${fmt(sol.c)}",
                        "Angles: A=${fmt(sol.angleADeg)}°, B=${fmt(sol.angleBDeg)}°, C=${fmt(sol.angleCDeg)}°",
                        "Area = ${fmt(sol.area)}   Perimeter = ${fmt(sol.perimeter)}",
                        "Height h_a = ${fmt(sol.heightA)}   R = ${fmt(sol.circumradius)}   r = ${fmt(sol.inradius)}"
                    )
                    lines.forEach {
                        Text(it, fontFamily = JetBrainsMonoFontFamily, fontSize = 12.sp, color = ApexTextPrimary)
                    }
                    if (idx == 0) {
                        Spacer(Modifier.height(6.dp))
                        res.steps.forEach { st ->
                            Text("• $st", style = MaterialTheme.typography.bodySmall, color = ApexTextSecondary)
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ApexActionChip("Area to Calc", Icons.Outlined.Calculate, onClick = {
                                onSendExpr(fmt(sol.area))
                            })
                            ApexActionChip("Save Note", Icons.Outlined.BookmarkAdd, onClick = {
                                onSave("Triangle (${mode.name})", lines.joinToString("\n"), fmt(sol.area))
                            })
                        }
                    }
                }
            }
        }
        null -> Unit
    }
}

@Composable
fun TriangleCanvasDiagram(sol: TriangleSolution) {
    val measurer: TextMeasurer = rememberTextMeasurer()
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(170.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(ApexBackground)
            .border(1.dp, ApexBorder, RoundedCornerShape(10.dp))
    ) {
        // Place vertex A at (0,0), vertex B at (c, 0), vertex C at (b*cosA, b*sinA)
        val ax = 0f
        val ay = 0f
        val bx = sol.c.toFloat()
        val by = 0f
        val radA = Math.toRadians(sol.angleADeg)
        val cx = (sol.b * cos(radA)).toFloat()
        val cy = -(sol.b * sin(radA)).toFloat() // screen Y goes down

        val minX = minOf(ax, bx, cx)
        val maxX = maxOf(ax, bx, cx)
        val minY = minOf(ay, by, cy)
        val maxY = maxOf(ay, by, cy)
        val spanX = (maxX - minX).coerceAtLeast(1e-3f)
        val spanY = (maxY - minY).coerceAtLeast(1e-3f)
        val pad = 42f
        val scale = minOf((size.width - 2 * pad) / spanX, (size.height - 2 * pad) / spanY)

        val offsetX = (size.width - spanX * scale) / 2f - minX * scale
        val offsetY = (size.height - spanY * scale) / 2f - minY * scale

        val pA = Offset(ax * scale + offsetX, ay * scale + offsetY)
        val pB = Offset(bx * scale + offsetX, by * scale + offsetY)
        val pC = Offset(cx * scale + offsetX, cy * scale + offsetY)

        val path = Path().apply {
            moveTo(pA.x, pA.y)
            lineTo(pB.x, pB.y)
            lineTo(pC.x, pC.y)
            close()
        }
        drawPath(path, color = ApexCyan.copy(alpha = 0.14f))
        drawPath(path, color = ApexCyan, style = Stroke(width = 3f))

        listOf(pA to "A", pB to "B", pC to "C").forEach { (pt, lbl) ->
            drawCircle(color = ApexAmber, radius = 6f, center = pt)
            drawText(
                measurer,
                lbl,
                topLeft = Offset((pt.x - 6f).coerceIn(4f, size.width - 20f), (pt.y - 22f).coerceIn(2f, size.height - 20f)),
                style = TextStyle(color = ApexAmber, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            )
        }
        // Side labels at midpoints
        val labelStyle = TextStyle(color = ApexTextPrimary, fontSize = 10.sp, fontFamily = JetBrainsMonoFontFamily)
        drawText(measurer, "c=${fmt(sol.c)}", Offset((pA.x + pB.x) / 2f - 18f, pA.y + 4f), style = labelStyle)
        drawText(measurer, "b=${fmt(sol.b)}", Offset((pA.x + pC.x) / 2f - 28f, (pA.y + pC.y) / 2f - 8f), style = labelStyle)
        drawText(measurer, "a=${fmt(sol.a)}", Offset((pB.x + pC.x) / 2f + 4f, (pB.y + pC.y) / 2f - 8f), style = labelStyle)
    }
}

@Composable
private fun TwoDShapesSection(
    viewModel: CalculatorViewModel,
    onSendExpr: (String) -> Unit,
    onSave: (String, String, String) -> Unit
) {
    val shapes = listOf("Circle", "Sector", "Rectangle", "Trapezoid", "Regular Polygon")
    var shapeIdx by remember { mutableIntStateOf(0) }
    var v1 by remember { mutableStateOf("5") }
    var v2 by remember { mutableStateOf("60") }
    var v3 by remember { mutableStateOf("4") }
    val result by viewModel.geometryResult.collectAsStateWithLifecycle()
    val error by viewModel.geometryError.collectAsStateWithLifecycle()

    ApexCard {
        ApexSectionTitle("2D Geometry Solver")
        Spacer(Modifier.height(8.dp))
        Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            shapes.forEachIndexed { idx, s ->
                FilterChip(
                    selected = shapeIdx == idx,
                    onClick = { shapeIdx = idx },
                    label = { Text(s) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = ApexCyanContainer, selectedLabelColor = ApexCyan)
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            val l1 = when (shapeIdx) { 0, 1 -> "Radius r"; 2 -> "Width"; 3 -> "Base b₁"; else -> "Sides n" }
            OutlinedTextField(value = v1, onValueChange = { v1 = it }, label = { Text(l1, fontSize = 11.sp) }, singleLine = true, modifier = Modifier.weight(1f), colors = apexFieldColors())
            if (shapeIdx > 0) {
                val l2 = when (shapeIdx) { 1 -> "Angle θ°"; 2 -> "Height"; 3 -> "Base b₂"; else -> "Side len" }
                OutlinedTextField(value = v2, onValueChange = { v2 = it }, label = { Text(l2, fontSize = 11.sp) }, singleLine = true, modifier = Modifier.weight(1f), colors = apexFieldColors())
            }
            if (shapeIdx == 3) {
                OutlinedTextField(value = v3, onValueChange = { v3 = it }, label = { Text("Height h", fontSize = 11.sp) }, singleLine = true, modifier = Modifier.weight(1f), colors = apexFieldColors())
            }
        }
        Spacer(Modifier.height(10.dp))
        Button(
            onClick = {
                val a = v1.toDoubleOrNull() ?: 0.0
                val b = v2.toDoubleOrNull() ?: 0.0
                val c = v3.toDoubleOrNull() ?: 0.0
                viewModel.computeGeometryShape {
                    when (shapeIdx) {
                        0 -> GeometryEngine.circle(a)
                        1 -> GeometryEngine.sector(a, b)
                        2 -> GeometryEngine.rectangle(a, b)
                        3 -> GeometryEngine.trapezoid(a, b, c)
                        else -> GeometryEngine.regularPolygon(a.toInt(), b)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = ApexCyan, contentColor = ApexBackground)
        ) {
            Text("Compute 2D Properties", fontWeight = FontWeight.SemiBold)
        }
    }

    error?.let { StatusBanner(it, isError = true) }
    result?.let { GeometryResultCard(it, onSendExpr, onSave) }
}

@Composable
private fun ThreeDSolidsSection(
    viewModel: CalculatorViewModel,
    onSendExpr: (String) -> Unit,
    onSave: (String, String, String) -> Unit
) {
    val solids = listOf("Sphere", "Cylinder", "Cone", "Frustum", "Cuboid", "Pyramid")
    var idx by remember { mutableIntStateOf(0) }
    var v1 by remember { mutableStateOf("4") }
    var v2 by remember { mutableStateOf("8") }
    var v3 by remember { mutableStateOf("6") }
    val result by viewModel.geometryResult.collectAsStateWithLifecycle()
    val error by viewModel.geometryError.collectAsStateWithLifecycle()

    ApexCard {
        ApexSectionTitle("3D Solids Solver")
        Spacer(Modifier.height(8.dp))
        Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            solids.forEachIndexed { i, s ->
                FilterChip(
                    selected = idx == i,
                    onClick = { idx = i },
                    label = { Text(s) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = ApexCyanContainer, selectedLabelColor = ApexCyan)
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            val l1 = when (idx) { 3 -> "Radius R"; 4 -> "Length"; 5 -> "Base side"; else -> "Radius r" }
            OutlinedTextField(value = v1, onValueChange = { v1 = it }, label = { Text(l1, fontSize = 11.sp) }, singleLine = true, modifier = Modifier.weight(1f), colors = apexFieldColors())
            if (idx > 0) {
                val l2 = when (idx) { 3 -> "Radius r"; 4 -> "Width"; else -> "Height h" }
                OutlinedTextField(value = v2, onValueChange = { v2 = it }, label = { Text(l2, fontSize = 11.sp) }, singleLine = true, modifier = Modifier.weight(1f), colors = apexFieldColors())
            }
            if (idx == 3 || idx == 4) {
                OutlinedTextField(value = v3, onValueChange = { v3 = it }, label = { Text("Height h", fontSize = 11.sp) }, singleLine = true, modifier = Modifier.weight(1f), colors = apexFieldColors())
            }
        }
        Spacer(Modifier.height(10.dp))
        Button(
            onClick = {
                val a = v1.toDoubleOrNull() ?: 0.0
                val b = v2.toDoubleOrNull() ?: 0.0
                val c = v3.toDoubleOrNull() ?: 0.0
                viewModel.computeGeometryShape {
                    when (idx) {
                        0 -> GeometryEngine.sphere(a)
                        1 -> GeometryEngine.cylinder(a, b)
                        2 -> GeometryEngine.cone(a, b)
                        3 -> GeometryEngine.frustum(a, b, c)
                        4 -> GeometryEngine.cuboid(a, b, c)
                        else -> GeometryEngine.squarePyramid(a, b)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = ApexCyan, contentColor = ApexBackground)
        ) {
            Text("Compute 3D Volume & Area", fontWeight = FontWeight.SemiBold)
        }
    }

    error?.let { StatusBanner(it, isError = true) }
    result?.let { GeometryResultCard(it, onSendExpr, onSave) }
}

@Composable
private fun CoordinateGeometrySection(
    viewModel: CalculatorViewModel,
    onSendExpr: (String) -> Unit,
    onSave: (String, String, String) -> Unit
) {
    var x1 by remember { mutableStateOf("1") }
    var y1 by remember { mutableStateOf("2") }
    var x2 by remember { mutableStateOf("5") }
    var y2 by remember { mutableStateOf("10") }
    val result by viewModel.geometryResult.collectAsStateWithLifecycle()
    val error by viewModel.geometryError.collectAsStateWithLifecycle()

    ApexCard {
        ApexSectionTitle("Coordinate Geometry", "Distance, midpoint, slope, line equation & perpendicular slope")
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(value = x1, onValueChange = { x1 = it }, label = { Text("x₁") }, singleLine = true, modifier = Modifier.weight(1f), colors = apexFieldColors())
            OutlinedTextField(value = y1, onValueChange = { y1 = it }, label = { Text("y₁") }, singleLine = true, modifier = Modifier.weight(1f), colors = apexFieldColors())
            OutlinedTextField(value = x2, onValueChange = { x2 = it }, label = { Text("x₂") }, singleLine = true, modifier = Modifier.weight(1f), colors = apexFieldColors())
            OutlinedTextField(value = y2, onValueChange = { y2 = it }, label = { Text("y₂") }, singleLine = true, modifier = Modifier.weight(1f), colors = apexFieldColors())
        }
        Spacer(Modifier.height(10.dp))
        Button(
            onClick = {
                viewModel.computeGeometryShape {
                    GeometryEngine.coordinateTwoPoints(
                        x1.toDoubleOrNull() ?: 0.0,
                        y1.toDoubleOrNull() ?: 0.0,
                        x2.toDoubleOrNull() ?: 0.0,
                        y2.toDoubleOrNull() ?: 0.0
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = ApexCyan, contentColor = ApexBackground)
        ) {
            Text("Analyze Points", fontWeight = FontWeight.SemiBold)
        }
    }

    error?.let { StatusBanner(it, isError = true) }
    result?.let { GeometryResultCard(it, onSendExpr, onSave) }
}

@Composable
private fun GeometryResultCard(
    res: GeometrySolveResult,
    onSendExpr: (String) -> Unit,
    onSave: (String, String, String) -> Unit
) {
    ApexCard(containerColor = ApexSurfaceElevated) {
        Text(res.title, style = MaterialTheme.typography.titleSmall, color = ApexAmber)
        Spacer(Modifier.height(6.dp))
        res.measurements.forEach { m ->
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(m.label, style = MaterialTheme.typography.bodySmall, color = ApexTextSecondary)
                Text("${fmt(m.value)}${m.unitHint}", fontFamily = JetBrainsMonoFontFamily, fontSize = 13.sp, color = ApexCyan, fontWeight = FontWeight.SemiBold)
            }
        }
        Spacer(Modifier.height(6.dp))
        res.formulas.forEach { f ->
            Text("• $f", fontFamily = JetBrainsMonoFontFamily, fontSize = 11.sp, color = ApexTextSecondary)
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            res.calculatorExpr?.let { expr ->
                ApexActionChip("To Calculator", Icons.Outlined.Calculate, onClick = { onSendExpr(expr) })
            }
            ApexActionChip("Save Note", Icons.Outlined.BookmarkAdd, onClick = {
                val summary = res.measurements.joinToString("\n") { "${it.label}: ${fmt(it.value)}${it.unitHint}" }
                onSave(res.title, summary, res.calculatorExpr ?: "")
            })
        }
    }
}

private fun fmt(v: Double): String = MathFormatter.formatNumber(v, 4)
