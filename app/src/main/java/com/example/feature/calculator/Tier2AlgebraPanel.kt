package com.example.feature.calculator

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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.math.AlgebraEngine
import com.example.core.math.MathFormatter
import com.example.core.math.MatrixData
import com.example.core.math.SolverResult
import com.example.core.ui.ApexActionChip
import com.example.core.ui.ApexCard
import com.example.core.ui.ApexSectionTitle
import com.example.core.ui.MonoFormulaBox
import com.example.core.ui.StatusBanner
import com.example.ui.theme.ApexAmber
import com.example.ui.theme.ApexBackground
import com.example.ui.theme.ApexCyan
import com.example.ui.theme.ApexCyanContainer
import com.example.ui.theme.ApexSuccess
import com.example.ui.theme.ApexSurfaceElevated
import com.example.ui.theme.ApexTextPrimary
import com.example.ui.theme.ApexTextSecondary
import com.example.ui.theme.JetBrainsMonoFontFamily

@Composable
fun Tier2AlgebraPanel(
    viewModel: CalculatorViewModel,
    onSendExprToStandard: (String) -> Unit,
    onSaveToNotebook: (title: String, content: String, expr: String) -> Unit
) {
    var selectedTool by remember { mutableIntStateOf(0) }
    val tools = listOf("Equations", "Systems", "Matrices", "Polynomials", "Number Theory")
    val solverResult by viewModel.algebraResult.collectAsStateWithLifecycle()

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
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ApexCyanContainer,
                        selectedLabelColor = ApexCyan
                    )
                )
            }
        }

        when (selectedTool) {
            0 -> EquationsTool(viewModel, solverResult, onSendExprToStandard, onSaveToNotebook)
            1 -> SystemsTool(viewModel, solverResult, onSaveToNotebook)
            2 -> MatrixTool(viewModel, onSaveToNotebook)
            3 -> PolynomialTool(onSendExprToStandard, onSaveToNotebook)
            4 -> NumberTheoryTool(viewModel, solverResult, onSaveToNotebook)
        }
    }
}

@Composable
private fun EquationsTool(
    viewModel: CalculatorViewModel,
    result: SolverResult?,
    onSendExpr: (String) -> Unit,
    onSave: (String, String, String) -> Unit
) {
    var degree by remember { mutableIntStateOf(2) }
    var a by remember { mutableStateOf("1") }
    var b by remember { mutableStateOf("-5") }
    var c by remember { mutableStateOf("6") }
    var d by remember { mutableStateOf("0") }

    ApexCard {
        ApexSectionTitle("Equation Solver", "Linear, Quadratic (real & complex), and Cubic (Cardano/Vieta)")
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(1 to "Linear", 2 to "Quadratic", 3 to "Cubic").forEach { (deg, lbl) ->
                FilterChip(
                    selected = degree == deg,
                    onClick = { degree = deg },
                    label = { Text(lbl) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ApexCyanContainer,
                        selectedLabelColor = ApexCyan
                    )
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(value = a, onValueChange = { a = it }, label = { Text("a") }, singleLine = true, modifier = Modifier.weight(1f), colors = apexFieldColors())
            OutlinedTextField(value = b, onValueChange = { b = it }, label = { Text("b") }, singleLine = true, modifier = Modifier.weight(1f), colors = apexFieldColors())
            if (degree >= 2) {
                OutlinedTextField(value = c, onValueChange = { c = it }, label = { Text("c") }, singleLine = true, modifier = Modifier.weight(1f), colors = apexFieldColors())
            }
            if (degree == 3) {
                OutlinedTextField(value = d, onValueChange = { d = it }, label = { Text("d") }, singleLine = true, modifier = Modifier.weight(1f), colors = apexFieldColors())
            }
        }
        Spacer(Modifier.height(10.dp))
        Button(
            onClick = {
                val av = a.toDoubleOrNull() ?: 0.0
                val bv = b.toDoubleOrNull() ?: 0.0
                val cv = c.toDoubleOrNull() ?: 0.0
                val dv = d.toDoubleOrNull() ?: 0.0
                when (degree) {
                    1 -> viewModel.solveLinear(av, bv)
                    2 -> viewModel.solveQuadratic(av, bv, cv)
                    else -> viewModel.solveCubic(av, bv, cv, dv)
                }
            },
            modifier = Modifier.fillMaxWidth().testTag("solve_equation_btn"),
            colors = ButtonDefaults.buttonColors(containerColor = ApexCyan, contentColor = ApexBackground)
        ) {
            Text("Solve Equation", fontWeight = FontWeight.SemiBold)
        }
    }

    if (result != null) {
        SolverResultCard(result, onSendExpr, onSave)
    }
}

@Composable
private fun SystemsTool(
    viewModel: CalculatorViewModel,
    result: SolverResult?,
    onSave: (String, String, String) -> Unit
) {
    var size by remember { mutableIntStateOf(2) }
    var rows by remember {
        mutableStateOf(
            listOf(
                mutableListOf("2", "1", "0", "7"),
                mutableListOf("1", "-1", "0", "2"),
                mutableListOf("1", "1", "1", "6")
            )
        )
    }

    ApexCard {
        ApexSectionTitle("Linear System Solver", "Gaussian elimination with partial pivoting")
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(2 to "2 × 2 System", 3 to "3 × 3 System").forEach { (n, lbl) ->
                FilterChip(
                    selected = size == n,
                    onClick = { size = n },
                    label = { Text(lbl) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = ApexCyanContainer, selectedLabelColor = ApexCyan)
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        val colLabels = if (size == 2) listOf("x", "y", "=") else listOf("x", "y", "z", "=")
        for (r in 0 until size) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
                for (cIdx in 0..size) {
                    val actualCol = if (size == 2 && cIdx == 2) 3 else cIdx
                    OutlinedTextField(
                        value = rows[r][actualCol],
                        onValueChange = { newVal ->
                            rows = rows.mapIndexed { ri, row ->
                                if (ri == r) row.toMutableList().apply { this[actualCol] = newVal } else row
                            }
                        },
                        label = { Text(colLabels[cIdx], fontSize = 11.sp) },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = apexFieldColors()
                    )
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        Button(
            onClick = {
                val aug = (0 until size).map { r ->
                    val cols = if (size == 2) listOf(0, 1, 3) else listOf(0, 1, 2, 3)
                    cols.map { c -> rows[r][c].toDoubleOrNull() ?: 0.0 }
                }
                viewModel.solveSystem(aug)
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = ApexCyan, contentColor = ApexBackground)
        ) {
            Text("Solve System", fontWeight = FontWeight.SemiBold)
        }
    }

    if (result != null) {
        SolverResultCard(result, null, onSave)
    }
}

@Composable
private fun MatrixTool(
    viewModel: CalculatorViewModel,
    onSave: (String, String, String) -> Unit
) {
    var dim by remember { mutableIntStateOf(2) }
    var matAText by remember { mutableStateOf("4, 2\n1, 3") }
    var matBText by remember { mutableStateOf("1, 0\n2, -1") }
    var scalarText by remember { mutableStateOf("2") }
    var powerText by remember { mutableStateOf("2") }

    val resultText by viewModel.matrixResultText.collectAsStateWithLifecycle()
    val errorText by viewModel.matrixError.collectAsStateWithLifecycle()

    fun parseMatrix(text: String): MatrixData {
        val rowLines = text.trim().lines().filter { it.isNotBlank() }
        require(rowLines.isNotEmpty()) { "Matrix is empty" }
        val parsed = rowLines.map { line ->
            line.split(",", " ", "\t").filter { it.isNotBlank() }.map {
                it.toDoubleOrNull() ?: throw IllegalArgumentException("Invalid matrix number '$it'")
            }
        }
        val cols = parsed[0].size
        require(parsed.all { it.size == cols }) { "All rows must have equal column count" }
        return MatrixData(parsed.size, cols, parsed)
    }

    ApexCard {
        ApexSectionTitle("Matrix Calculator", "Up to 4×4; enter rows on new lines, values separated by spaces or commas")
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(2, 3, 4).forEach { n ->
                FilterChip(
                    selected = dim == n,
                    onClick = {
                        dim = n
                        matAText = List(n) { r -> List(n) { c -> if (r == c) "1" else "0" }.joinToString(", ") }.joinToString("\n")
                        matBText = List(n) { r -> List(n) { c -> "${r + c + 1}" }.joinToString(", ") }.joinToString("\n")
                    },
                    label = { Text("${n}×${n}") },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = ApexCyanContainer, selectedLabelColor = ApexCyan)
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = matAText,
            onValueChange = { matAText = it },
            label = { Text("Matrix A") },
            minLines = 2,
            maxLines = 4,
            modifier = Modifier.fillMaxWidth(),
            colors = apexFieldColors()
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = matBText,
            onValueChange = { matBText = it },
            label = { Text("Matrix B") },
            minLines = 2,
            maxLines = 4,
            modifier = Modifier.fillMaxWidth(),
            colors = apexFieldColors()
        )
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = scalarText, onValueChange = { scalarText = it }, label = { Text("Scalar k") }, singleLine = true, modifier = Modifier.weight(1f), colors = apexFieldColors())
            OutlinedTextField(value = powerText, onValueChange = { powerText = it }, label = { Text("Power p") }, singleLine = true, modifier = Modifier.weight(1f), colors = apexFieldColors())
        }
        Spacer(Modifier.height(10.dp))
        val ops = listOf("A+B", "A-B", "A×B", "k·A", "Aᵀ", "det(A)", "A⁻¹", "rank(A)", "A^k", "Eigen(A)")
        Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            ops.forEach { op ->
                Button(
                    onClick = {
                        try {
                            val mA = parseMatrix(matAText)
                            val mB = parseMatrix(matBText)
                            viewModel.runMatrixOp(
                                op,
                                mA,
                                mB,
                                scalarText.toDoubleOrNull() ?: 1.0,
                                powerText.toIntOrNull() ?: 2
                            )
                        } catch (e: Exception) {
                            viewModel.runMatrixOp("__invalid__", MatrixData(1, 1, listOf(listOf(0.0))), MatrixData(1, 1, listOf(listOf(0.0))), 1.0, 1)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ApexSurfaceElevated, contentColor = ApexCyan)
                ) {
                    Text(op, fontFamily = JetBrainsMonoFontFamily, fontSize = 12.sp)
                }
            }
        }
    }

    errorText?.let { StatusBanner(it, isError = true) }
    resultText?.let { out ->
        ApexCard(containerColor = ApexSurfaceElevated) {
            MonoFormulaBox(out)
            Spacer(Modifier.height(8.dp))
            ApexActionChip("Save to Notebook", Icons.Outlined.BookmarkAdd, onClick = {
                onSave("Matrix Computation", out, "")
            })
        }
    }
}

@Composable
private fun PolynomialTool(
    onSendExpr: (String) -> Unit,
    onSave: (String, String, String) -> Unit
) {
    var p1Text by remember { mutableStateOf("1, -3, 2") }
    var p2Text by remember { mutableStateOf("1, 1") }
    var evalX by remember { mutableStateOf("4") }
    var output by remember { mutableStateOf<String?>(null) }

    fun parseCoeffs(s: String): List<Double> =
        s.split(",").mapNotNull { it.trim().toDoubleOrNull() }.ifEmpty { listOf(0.0) }

    ApexCard {
        ApexSectionTitle("Polynomial Toolkit", "Enter coefficients descending (e.g. '1, -3, 2' = x² - 3x + 2)")
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = p1Text, onValueChange = { p1Text = it }, label = { Text("P(x) coefficients") }, singleLine = true, modifier = Modifier.fillMaxWidth(), colors = apexFieldColors())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = p2Text, onValueChange = { p2Text = it }, label = { Text("Q(x) coefficients") }, singleLine = true, modifier = Modifier.fillMaxWidth(), colors = apexFieldColors())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = evalX, onValueChange = { evalX = it }, label = { Text("Evaluate at x =") }, singleLine = true, modifier = Modifier.fillMaxWidth(), colors = apexFieldColors())
        Spacer(Modifier.height(10.dp))
        Button(
            onClick = {
                val p1 = AlgebraEngine.trimPolynomial(parseCoeffs(p1Text))
                val p2 = AlgebraEngine.trimPolynomial(parseCoeffs(p2Text))
                val x = evalX.toDoubleOrNull() ?: 0.0
                val sum = AlgebraEngine.addPolynomials(p1, p2)
                val prod = AlgebraEngine.multiplyPolynomials(p1, p2)
                val diff = AlgebraEngine.differentiatePolynomial(p1)
                val integ = AlgebraEngine.integratePolynomial(p1)
                val valAtX = AlgebraEngine.evaluatePolynomial(p1, x)
                output = buildString {
                    appendLine("P(x) = ${AlgebraEngine.formatPolynomial(p1)}  (deg ${p1.size - 1})")
                    appendLine("P(${MathFormatter.formatNumber(x)}) = ${MathFormatter.formatNumber(valAtX)}")
                    appendLine("P(x) + Q(x) = ${AlgebraEngine.formatPolynomial(sum)}")
                    appendLine("P(x) · Q(x) = ${AlgebraEngine.formatPolynomial(prod)}")
                    appendLine("P'(x) = ${AlgebraEngine.formatPolynomial(diff)}")
                    append("∫P(x)dx = ${AlgebraEngine.formatPolynomial(integ)} + C")
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = ApexCyan, contentColor = ApexBackground)
        ) {
            Text("Analyze Polynomials", fontWeight = FontWeight.SemiBold)
        }
    }

    output?.let { out ->
        ApexCard(containerColor = ApexSurfaceElevated) {
            MonoFormulaBox(out)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ApexActionChip("Send P(x) to Calc", Icons.Outlined.Calculate, onClick = {
                    val p1 = AlgebraEngine.trimPolynomial(parseCoeffs(p1Text))
                    onSendExpr(AlgebraEngine.formatPolynomial(p1))
                })
                ApexActionChip("Save Note", Icons.Outlined.BookmarkAdd, onClick = {
                    onSave("Polynomial Analysis", out, "")
                })
            }
        }
    }
}

@Composable
private fun NumberTheoryTool(
    viewModel: CalculatorViewModel,
    result: SolverResult?,
    onSave: (String, String, String) -> Unit
) {
    var n1 by remember { mutableStateOf("84") }
    var n2 by remember { mutableStateOf("36") }
    var mod by remember { mutableStateOf("13") }

    ApexCard {
        ApexSectionTitle("Number Theory", "Primes, factorization, GCD/LCM, divisors, modular arithmetic")
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(value = n1, onValueChange = { n1 = it }, label = { Text("Integer a") }, singleLine = true, modifier = Modifier.weight(1f), colors = apexFieldColors())
            OutlinedTextField(value = n2, onValueChange = { n2 = it }, label = { Text("Integer b / exp") }, singleLine = true, modifier = Modifier.weight(1f), colors = apexFieldColors())
            OutlinedTextField(value = mod, onValueChange = { mod = it }, label = { Text("Modulus m") }, singleLine = true, modifier = Modifier.weight(1f), colors = apexFieldColors())
        }
        Spacer(Modifier.height(10.dp))
        Button(
            onClick = {
                viewModel.solveNumberTheory(
                    n1.toLongOrNull() ?: 0L,
                    n2.toLongOrNull() ?: 0L,
                    mod.toLongOrNull() ?: 0L
                )
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = ApexCyan, contentColor = ApexBackground)
        ) {
            Text("Compute Number Theory", fontWeight = FontWeight.SemiBold)
        }
    }

    if (result != null) {
        SolverResultCard(result, null, onSave)
    }
}

@Composable
private fun SolverResultCard(
    result: SolverResult,
    onSendExpr: ((String) -> Unit)?,
    onSave: (String, String, String) -> Unit
) {
    ApexCard(containerColor = ApexSurfaceElevated) {
        Text(result.title, style = MaterialTheme.typography.titleSmall, color = ApexAmber)
        Spacer(Modifier.height(6.dp))
        MonoFormulaBox(result.summary, accent = if (result.isWarning) ApexAmber else ApexCyan)
        if (result.verification.isNotBlank()) {
            Spacer(Modifier.height(6.dp))
            Text(result.verification, fontFamily = JetBrainsMonoFontFamily, fontSize = 11.sp, color = ApexSuccess)
        }
        Spacer(Modifier.height(8.dp))
        result.steps.forEach { step ->
            Text("• $step", style = MaterialTheme.typography.bodySmall, color = ApexTextSecondary)
        }
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (result.calculatorExpr != null && onSendExpr != null) {
                ApexActionChip("To Calculator", Icons.Outlined.Calculate, onClick = {
                    onSendExpr(result.calculatorExpr)
                })
            }
            ApexActionChip("Save Note", Icons.Outlined.BookmarkAdd, onClick = {
                onSave(result.title, "${result.summary}\n\n${result.steps.joinToString("\n")}", result.calculatorExpr ?: "")
            })
        }
    }
}
