package com.example.feature.calculator

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.curriculum.CalcTargetTier
import com.example.core.math.AlgebraEngine
import com.example.core.math.AngleUnit
import com.example.core.math.CalculusEngine
import com.example.core.math.Complex
import com.example.core.math.DefiniteIntegralResult
import com.example.core.math.ExpressionParser
import com.example.core.math.Fraction
import com.example.core.math.GeometryEngine
import com.example.core.math.GeometrySolveResult
import com.example.core.math.GraphEngine
import com.example.core.math.GraphPoint
import com.example.core.math.LimitResult
import com.example.core.math.MathEvalResult
import com.example.core.math.MathFormatter
import com.example.core.math.MatrixData
import com.example.core.math.PercentageUtils
import com.example.core.math.RootFindResult
import com.example.core.math.SolverResult
import com.example.core.math.SymbolicDiffResult
import com.example.core.math.SymbolicIntegralResult
import com.example.core.math.TaylorSeriesResult
import com.example.core.math.TriangleMode
import com.example.core.math.TriangleSolveResult
import com.example.core.storage.CalcHistoryEntity
import com.example.core.storage.StorageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class StandardCalcUiState(
    val input: TextFieldValue = TextFieldValue(""),
    val liveResult: String = "",
    val exactFraction: String? = null,
    val mixedFraction: String? = null,
    val complexPolar: String? = null,
    val errorMessage: String? = null,
    val isEvaluated: Boolean = false,
    val lastAns: Complex = Complex.ZERO,
    val secondMode: Boolean = false,
    val showConstantsSheet: Boolean = false,
    val showHistorySheet: Boolean = false,
    val showQuickTools: Boolean = false
)

class CalculatorViewModel(private val repository: StorageRepository) : ViewModel() {

    private val _selectedTier = MutableStateFlow(CalcTargetTier.STANDARD)
    val selectedTier: StateFlow<CalcTargetTier> = _selectedTier.asStateFlow()

    private val _standardState = MutableStateFlow(StandardCalcUiState())
    val standardState: StateFlow<StandardCalcUiState> = _standardState.asStateFlow()

    val history: StateFlow<List<CalcHistoryEntity>> = repository.calcHistory.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Tier 2 state
    private val _algebraResult = MutableStateFlow<SolverResult?>(null)
    val algebraResult: StateFlow<SolverResult?> = _algebraResult.asStateFlow()

    private val _matrixResultText = MutableStateFlow<String?>(null)
    val matrixResultText: StateFlow<String?> = _matrixResultText.asStateFlow()

    private val _matrixError = MutableStateFlow<String?>(null)
    val matrixError: StateFlow<String?> = _matrixError.asStateFlow()

    // Tier 3 state
    private val _triangleResult = MutableStateFlow<TriangleSolveResult?>(null)
    val triangleResult: StateFlow<TriangleSolveResult?> = _triangleResult.asStateFlow()

    private val _geometryResult = MutableStateFlow<GeometrySolveResult?>(null)
    val geometryResult: StateFlow<GeometrySolveResult?> = _geometryResult.asStateFlow()

    private val _geometryError = MutableStateFlow<String?>(null)
    val geometryError: StateFlow<String?> = _geometryError.asStateFlow()

    // Tier 4 state
    private val _diffResult = MutableStateFlow<SymbolicDiffResult?>(null)
    val diffResult: StateFlow<SymbolicDiffResult?> = _diffResult.asStateFlow()

    private val _symIntResult = MutableStateFlow<SymbolicIntegralResult?>(null)
    val symIntResult: StateFlow<SymbolicIntegralResult?> = _symIntResult.asStateFlow()

    private val _defIntResult = MutableStateFlow<DefiniteIntegralResult?>(null)
    val defIntResult: StateFlow<DefiniteIntegralResult?> = _defIntResult.asStateFlow()

    private val _integralCurve = MutableStateFlow<List<List<GraphPoint>>>(emptyList())
    val integralCurve: StateFlow<List<List<GraphPoint>>> = _integralCurve.asStateFlow()

    private val _limitResult = MutableStateFlow<LimitResult?>(null)
    val limitResult: StateFlow<LimitResult?> = _limitResult.asStateFlow()

    private val _taylorResult = MutableStateFlow<TaylorSeriesResult?>(null)
    val taylorResult: StateFlow<TaylorSeriesResult?> = _taylorResult.asStateFlow()

    private val _rootResult = MutableStateFlow<RootFindResult?>(null)
    val rootResult: StateFlow<RootFindResult?> = _rootResult.asStateFlow()

    private val _calculusError = MutableStateFlow<String?>(null)
    val calculusError: StateFlow<String?> = _calculusError.asStateFlow()

    private var currentAngleUnit: AngleUnit = AngleUnit.DEG
    private var currentPrecision: Int = 10

    // Pre-filled calculus expression when injected from a lesson
    private val _injectedExprHint = MutableStateFlow<String?>(null)
    val injectedExprHint: StateFlow<String?> = _injectedExprHint.asStateFlow()

    fun syncSettings(angleUnit: AngleUnit, precision: Int) {
        val changed = currentAngleUnit != angleUnit || currentPrecision != precision
        currentAngleUnit = angleUnit
        currentPrecision = precision
        if (changed && _standardState.value.input.text.isNotBlank()) {
            evaluateLive(_standardState.value.input.text)
        }
    }

    fun selectTier(tier: CalcTargetTier) {
        _selectedTier.value = tier
    }

    fun hasStandardExpression(): Boolean = _standardState.value.input.text.isNotBlank()

    fun applyInjectedPayload(tier: CalcTargetTier, expr: String, replace: Boolean) {
        _selectedTier.value = tier
        if (tier == CalcTargetTier.STANDARD) {
            if (replace) {
                val tfv = TextFieldValue(expr, TextRange(expr.length))
                _standardState.update { it.copy(input = tfv, isEvaluated = false) }
                evaluateLive(expr)
            } else {
                insertAtCursor(expr)
            }
        } else {
            _injectedExprHint.value = expr
        }
    }

    fun consumeInjectedHint(): String? {
        val v = _injectedExprHint.value
        _injectedExprHint.value = null
        return v
    }

    fun updateTextFieldValue(tfv: TextFieldValue) {
        _standardState.update { it.copy(input = tfv, isEvaluated = false) }
        evaluateLive(tfv.text)
    }

    fun insertAtCursor(token: String, cursorOffsetFromEnd: Int = 0) {
        val cur = _standardState.value.input
        val text = cur.text
        val start = cur.selection.min.coerceIn(0, text.length)
        val end = cur.selection.max.coerceIn(0, text.length)
        val newText = text.substring(0, start) + token + text.substring(end)
        val newCursor = (start + token.length + cursorOffsetFromEnd).coerceIn(0, newText.length)
        _standardState.update {
            it.copy(
                input = TextFieldValue(newText, TextRange(newCursor)),
                isEvaluated = false
            )
        }
        evaluateLive(newText)
    }

    fun insertFunction(fnName: String) {
        insertAtCursor("$fnName()", cursorOffsetFromEnd = -1)
    }

    fun backspace() {
        val cur = _standardState.value.input
        val text = cur.text
        val start = cur.selection.min.coerceIn(0, text.length)
        val end = cur.selection.max.coerceIn(0, text.length)
        val (newText, newCursor) = if (start != end) {
            text.removeRange(start, end) to start
        } else if (start > 0) {
            text.removeRange(start - 1, start) to (start - 1)
        } else {
            text to 0
        }
        _standardState.update {
            it.copy(
                input = TextFieldValue(newText, TextRange(newCursor)),
                isEvaluated = false
            )
        }
        evaluateLive(newText)
    }

    fun moveCursor(delta: Int) {
        val cur = _standardState.value.input
        val next = (cur.selection.start + delta).coerceIn(0, cur.text.length)
        _standardState.update { it.copy(input = cur.copy(selection = TextRange(next))) }
    }

    fun clearAll() {
        _standardState.update {
            it.copy(
                input = TextFieldValue(""),
                liveResult = "",
                exactFraction = null,
                mixedFraction = null,
                complexPolar = null,
                errorMessage = null,
                isEvaluated = false
            )
        }
    }

    fun toggleSecondMode() {
        _standardState.update { it.copy(secondMode = !it.secondMode) }
    }

    fun setConstantsVisible(show: Boolean) {
        _standardState.update { it.copy(showConstantsSheet = show) }
    }

    fun setHistoryVisible(show: Boolean) {
        _standardState.update { it.copy(showHistorySheet = show) }
    }

    fun toggleQuickTools() {
        _standardState.update { it.copy(showQuickTools = !it.showQuickTools) }
    }

    private fun evaluateLive(exprText: String) {
        if (exprText.isBlank()) {
            _standardState.update {
                it.copy(
                    liveResult = "",
                    exactFraction = null,
                    mixedFraction = null,
                    complexPolar = null,
                    errorMessage = null
                )
            }
            return
        }
        val vars = mapOf("ans" to _standardState.value.lastAns)
        when (val res = ExpressionParser.evaluate(exprText, currentAngleUnit, vars, allowComplex = true, precision = currentPrecision)) {
            is MathEvalResult.Success -> {
                _standardState.update {
                    it.copy(
                        liveResult = res.formatted,
                        exactFraction = res.fraction?.takeIf { f -> f.den != 1L }?.format(),
                        mixedFraction = res.fraction?.takeIf { f -> f.den != 1L && kotlin.math.abs(f.num) > f.den }?.toMixedString(),
                        complexPolar = if (!res.complex.isReal) res.complex.toPolarString(currentAngleUnit) else null,
                        errorMessage = null
                    )
                }
            }
            is MathEvalResult.Error -> {
                // In live typing, keep message subtle until Equals is pressed
                _standardState.update {
                    it.copy(errorMessage = res.message)
                }
            }
        }
    }

    fun evaluateCommit() {
        val exprText = _standardState.value.input.text.trim()
        if (exprText.isBlank()) return
        val vars = mapOf("ans" to _standardState.value.lastAns)
        when (val res = ExpressionParser.evaluate(exprText, currentAngleUnit, vars, allowComplex = true, precision = currentPrecision)) {
            is MathEvalResult.Success -> {
                _standardState.update {
                    it.copy(
                        liveResult = res.formatted,
                        exactFraction = res.fraction?.takeIf { f -> f.den != 1L }?.format(),
                        mixedFraction = res.fraction?.takeIf { f -> f.den != 1L && kotlin.math.abs(f.num) > f.den }?.toMixedString(),
                        complexPolar = if (!res.complex.isReal) res.complex.toPolarString(currentAngleUnit) else null,
                        errorMessage = null,
                        isEvaluated = true,
                        lastAns = res.complex
                    )
                }
                viewModelScope.launch {
                    repository.addCalcHistory(exprText, res.formatted, "Standard")
                }
            }
            is MathEvalResult.Error -> {
                _standardState.update {
                    it.copy(
                        errorMessage = res.message,
                        isEvaluated = true
                    )
                }
            }
        }
    }

    fun reuseHistoryItem(item: CalcHistoryEntity) {
        val tfv = TextFieldValue(item.expression, TextRange(item.expression.length))
        _standardState.update { it.copy(input = tfv, showHistorySheet = false, isEvaluated = false) }
        evaluateLive(item.expression)
    }

    fun clearHistory() {
        viewModelScope.launch { repository.clearCalcHistory() }
    }

    fun computePercentTool(mode: String, a: Double, b: Double): String {
        return try {
            val res = when (mode) {
                "of" -> PercentageUtils.percentOf(a, b)
                "inc" -> PercentageUtils.percentIncrease(a, b)
                "dec" -> PercentageUtils.percentDecrease(a, b)
                "chg" -> PercentageUtils.percentChange(a, b)
                "rev" -> PercentageUtils.reversePercentAfterChange(a, b)
                else -> 0.0
            }
            val suffix = if (mode == "chg") "%" else ""
            "${MathFormatter.formatNumber(res, 6)}$suffix"
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }

    // Tier 2 Algebra solvers
    fun solveLinear(a: Double, b: Double) {
        _algebraResult.value = AlgebraEngine.solveLinear(a, b)
    }

    fun solveQuadratic(a: Double, b: Double, c: Double) {
        _algebraResult.value = AlgebraEngine.solveQuadratic(a, b, c)
    }

    fun solveCubic(a: Double, b: Double, c: Double, d: Double) {
        _algebraResult.value = AlgebraEngine.solveCubic(a, b, c, d)
    }

    fun solveSystem(aug: List<List<Double>>) {
        _algebraResult.value = AlgebraEngine.solveLinearSystem(aug)
    }

    fun solveNumberTheory(n1: Long, n2: Long, mod: Long) {
        val steps = mutableListOf<String>()
        val summary = buildString {
            if (n1 >= 2) {
                val prime = AlgebraEngine.isPrime(n1)
                val factors = AlgebraEngine.primeFactorization(n1)
                    .entries.joinToString(" × ") { (p, e) -> if (e == 1) "$p" else "$p^$e" }
                steps += "$n1 is ${if (prime) "PRIME" else "COMPOSITE"}"
                steps += "Prime factorization of $n1: $factors"
                if (n1 <= 10_000_000L) {
                    val divs = AlgebraEngine.divisors(n1)
                    steps += "Divisors (${divs.size}): ${divs.take(24).joinToString(", ")}${if (divs.size > 24) "…" else ""}"
                }
                append("$n1 = $factors")
            } else {
                append("Enter integer ≥ 2 for factorization")
            }
            if (n1 != 0L || n2 != 0L) {
                val g = Fraction.gcd(n1, n2)
                val l = Fraction.lcm(n1, n2)
                steps += "gcd($n1, $n2) = $g,  lcm($n1, $n2) = $l"
            }
            if (mod > 1L) {
                val m = ((n1 % mod) + mod) % mod
                val pow = if (n2 >= 0) AlgebraEngine.modPow(n1, n2, mod) else null
                val inv = AlgebraEngine.modInverse(n1, mod)
                steps += "$n1 mod $mod = $m"
                if (pow != null) steps += "$n1^$n2 mod $mod = $pow"
                steps += if (inv != null) "Modular inverse $n1⁻¹ ≡ $inv (mod $mod)" else "No modular inverse mod $mod (gcd ≠ 1)"
            }
        }
        _algebraResult.value = SolverResult("Number Theory Analysis", summary, steps)
    }

    fun runMatrixOp(op: String, a: MatrixData, b: MatrixData, scalar: Double, power: Int) {
        _matrixError.value = null
        try {
            val out = when (op) {
                "A+B" -> "A + B =\n${AlgebraEngine.addMatrices(a, b).format()}"
                "A-B" -> "A - B =\n${AlgebraEngine.subMatrices(a, b).format()}"
                "A×B" -> "A × B =\n${AlgebraEngine.multiplyMatrices(a, b).format()}"
                "k·A" -> "${MathFormatter.formatNumber(scalar)} · A =\n${AlgebraEngine.scaleMatrix(a, scalar).format()}"
                "Aᵀ" -> "Transpose(A) =\n${AlgebraEngine.transposeMatrix(a).format()}"
                "det(A)" -> "det(A) = ${MathFormatter.formatNumber(AlgebraEngine.determinant(a), 8)}"
                "A⁻¹" -> "Inverse(A) =\n${AlgebraEngine.inverseMatrix(a).format()}"
                "rank(A)" -> "rank(A) = ${AlgebraEngine.matrixRank(a)}"
                "A^k" -> "A^$power =\n${AlgebraEngine.matrixPower(a, power).format()}"
                "Eigen(A)" -> {
                    val res = AlgebraEngine.eigen2x2(a)
                    "${res.summary}\n${res.steps.joinToString("\n")}"
                }
                else -> ""
            }
            _matrixResultText.value = out
        } catch (e: Exception) {
            _matrixError.value = e.message ?: "Matrix operation failed"
            _matrixResultText.value = null
        }
    }

    // Tier 3 Geometry solvers
    fun solveTriangle(mode: TriangleMode, v1: Double, v2: Double, v3: Double) {
        _triangleResult.value = GeometryEngine.solveTriangle(mode, v1, v2, v3)
    }

    fun computeGeometryShape(block: () -> GeometrySolveResult) {
        _geometryError.value = null
        try {
            _geometryResult.value = block()
        } catch (e: Exception) {
            _geometryError.value = e.message ?: "Invalid geometry parameters"
        }
    }

    // Tier 4 Calculus solvers
    fun runDifferentiation(expr: String, atX: Double?) {
        _calculusError.value = null
        viewModelScope.launch {
            try {
                val res = withContext(Dispatchers.Default) {
                    CalculusEngine.differentiate(expr, atX)
                }
                _diffResult.value = res
            } catch (e: Exception) {
                _calculusError.value = e.message ?: "Differentiation error"
            }
        }
    }

    fun runIntegration(expr: String, a: Double, b: Double) {
        _calculusError.value = null
        viewModelScope.launch {
            try {
                val (sym, def, curve) = withContext(Dispatchers.Default) {
                    val s = CalculusEngine.integrateSymbolic(expr)
                    val d = CalculusEngine.integrateDefinite(expr, a, b)
                    val span = maxOf(2.0, kotlin.math.abs(b - a) * 1.5)
                    val mid = 0.5 * (a + b)
                    val samples = GraphEngine.sampleFunction(expr, mid - span, mid + span, -20.0, 20.0, 220, AngleUnit.RAD)
                    Triple(s, d, samples)
                }
                _symIntResult.value = sym
                _defIntResult.value = def
                _integralCurve.value = curve
            } catch (e: Exception) {
                _calculusError.value = e.message ?: "Integration error"
            }
        }
    }

    fun runLimit(expr: String, target: Double) {
        _calculusError.value = null
        viewModelScope.launch {
            try {
                _limitResult.value = withContext(Dispatchers.Default) {
                    CalculusEngine.evaluateLimit(expr, target)
                }
            } catch (e: Exception) {
                _calculusError.value = e.message ?: "Limit evaluation error"
            }
        }
    }

    fun runTaylor(expr: String, center: Double, order: Int, testX: Double?) {
        _calculusError.value = null
        viewModelScope.launch {
            try {
                _taylorResult.value = withContext(Dispatchers.Default) {
                    CalculusEngine.taylorSeries(expr, center, order, testX)
                }
            } catch (e: Exception) {
                _calculusError.value = e.message ?: "Taylor expansion error"
            }
        }
    }

    fun runRootFinder(method: String, expr: String, aOrX0: Double, bOrX1: Double) {
        _calculusError.value = null
        viewModelScope.launch {
            try {
                _rootResult.value = withContext(Dispatchers.Default) {
                    when (method) {
                        "Bisection" -> CalculusEngine.findRootBisection(expr, aOrX0, bOrX1)
                        "Newton" -> CalculusEngine.findRootNewton(expr, aOrX0)
                        else -> CalculusEngine.findRootSecant(expr, aOrX0, bOrX1)
                    }
                }
            } catch (e: Exception) {
                _calculusError.value = e.message ?: "Root finder error"
            }
        }
    }
}
