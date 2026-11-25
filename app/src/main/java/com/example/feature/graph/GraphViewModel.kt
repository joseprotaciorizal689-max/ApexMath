package com.example.feature.graph

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.math.AngleUnit
import com.example.core.math.CalculusEngine
import com.example.core.math.ExpressionParser
import com.example.core.math.GraphAnalysisFeature
import com.example.core.math.GraphEngine
import com.example.core.math.GraphPoint
import com.example.core.math.MathFormatter
import com.example.core.storage.SavedGraphEntity
import com.example.core.storage.StorageRepository
import com.example.ui.theme.GraphPalette
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class GraphFunctionItem(
    val id: Int,
    val expression: String,
    val color: Color,
    val visible: Boolean = true,
    val segments: List<List<GraphPoint>> = emptyList(),
    val error: String? = null
)

data class GraphUiState(
    val functions: List<GraphFunctionItem> = listOf(
        GraphFunctionItem(1, "x^2 - 3", GraphPalette[0]),
        GraphFunctionItem(2, "sin(x)*2", GraphPalette[1])
    ),
    val xMin: Double = -8.0,
    val xMax: Double = 8.0,
    val yMin: Double = -6.0,
    val yMax: Double = 6.0,
    val features: List<GraphAnalysisFeature> = emptyList(),
    val showDerivative: Boolean = false,
    val derivativeExpr: String? = null,
    val derivativeSegments: List<List<GraphPoint>> = emptyList(),
    val showTangent: Boolean = false,
    val tangentX: Double = 1.0,
    val tangentY: Double? = null,
    val tangentSlope: Double? = null,
    val showIntegralShade: Boolean = false,
    val integralA: Double = -1.5,
    val integralB: Double = 1.5,
    val integralValue: Double? = null,
    val traceX: Double? = null,
    val traceY: Double? = null
)

class GraphViewModel(private val repository: StorageRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(GraphUiState())
    val uiState: StateFlow<GraphUiState> = _uiState.asStateFlow()

    val savedGraphs: StateFlow<List<SavedGraphEntity>> = repository.savedGraphs.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private var nextId = 3

    init {
        recomputeAll()
    }

    fun injectExpressions(exprs: List<String>) {
        if (exprs.isEmpty()) return
        val newList = exprs.mapIndexed { idx, e ->
            GraphFunctionItem(
                id = nextId++,
                expression = e,
                color = GraphPalette[idx % GraphPalette.size],
                visible = true
            )
        }
        _uiState.update { it.copy(functions = newList) }
        recomputeAll()
    }

    fun loadSavedGraph(saved: SavedGraphEntity) {
        val exprs = saved.expressionsSerialized.lines().map { it.trim() }.filter { it.isNotBlank() }
        if (exprs.isEmpty()) return
        val items = exprs.mapIndexed { idx, e ->
            GraphFunctionItem(nextId++, e, GraphPalette[idx % GraphPalette.size])
        }
        _uiState.update {
            it.copy(
                functions = items,
                xMin = saved.xMin,
                xMax = saved.xMax,
                yMin = saved.yMin,
                yMax = saved.yMax
            )
        }
        recomputeAll()
    }

    fun addFunction(expr: String = "cos(x)") {
        val id = nextId++
        val color = GraphPalette[(id - 1) % GraphPalette.size]
        _uiState.update {
            it.copy(functions = it.functions + GraphFunctionItem(id, expr, color))
        }
        recomputeAll()
    }

    fun updateFunction(id: Int, newExpr: String) {
        _uiState.update { state ->
            state.copy(
                functions = state.functions.map {
                    if (it.id == id) it.copy(expression = newExpr) else it
                }
            )
        }
        recomputeAll()
    }

    fun toggleVisibility(id: Int) {
        _uiState.update { state ->
            state.copy(
                functions = state.functions.map {
                    if (it.id == id) it.copy(visible = !it.visible) else it
                }
            )
        }
        recomputeAll()
    }

    fun removeFunction(id: Int) {
        _uiState.update { state ->
            val filtered = state.functions.filterNot { it.id == id }
            state.copy(functions = filtered.ifEmpty { listOf(GraphFunctionItem(nextId++, "x", GraphPalette[0])) })
        }
        recomputeAll()
    }

    fun pan(dxFraction: Double, dyFraction: Double) {
        _uiState.update { s ->
            val spanX = s.xMax - s.xMin
            val spanY = s.yMax - s.yMin
            val shiftX = -dxFraction * spanX
            val shiftY = dyFraction * spanY
            s.copy(
                xMin = s.xMin + shiftX,
                xMax = s.xMax + shiftX,
                yMin = s.yMin + shiftY,
                yMax = s.yMax + shiftY
            )
        }
        recomputeAll()
    }

    fun zoom(factor: Double) {
        _uiState.update { s ->
            val cx = 0.5 * (s.xMin + s.xMax)
            val cy = 0.5 * (s.yMin + s.yMax)
            val halfW = (0.5 * (s.xMax - s.xMin) * factor).coerceIn(0.25, 200.0)
            val halfH = (0.5 * (s.yMax - s.yMin) * factor).coerceIn(0.25, 200.0)
            s.copy(
                xMin = cx - halfW,
                xMax = cx + halfW,
                yMin = cy - halfH,
                yMax = cy + halfH
            )
        }
        recomputeAll()
    }

    fun resetView() {
        _uiState.update {
            it.copy(xMin = -8.0, xMax = 8.0, yMin = -6.0, yMax = 6.0)
        }
        recomputeAll()
    }

    fun toggleDerivativeOverlay() {
        _uiState.update { it.copy(showDerivative = !it.showDerivative) }
        recomputeAll()
    }

    fun toggleTangentOverlay() {
        _uiState.update { it.copy(showTangent = !it.showTangent) }
        recomputeAll()
    }

    fun setTangentX(x: Double) {
        _uiState.update { it.copy(tangentX = x) }
        recomputeAll()
    }

    fun toggleIntegralShade() {
        _uiState.update { it.copy(showIntegralShade = !it.showIntegralShade) }
        recomputeAll()
    }

    fun setIntegralBounds(a: Double, b: Double) {
        _uiState.update { it.copy(integralA = a, integralB = b) }
        recomputeAll()
    }

    fun updateTraceAtScreenFraction(fracX: Double) {
        val s = _uiState.value
        val x = s.xMin + fracX.coerceIn(0.0, 1.0) * (s.xMax - s.xMin)
        val primary = s.functions.firstOrNull { it.visible }?.expression ?: return
        val y = try {
            val expr = ExpressionParser.parse(primary)
            ExpressionParser.evaluateReal(expr, x, AngleUnit.RAD)
        } catch (_: Exception) {
            null
        }
        _uiState.update { it.copy(traceX = x, traceY = y) }
    }

    fun saveCurrentGraph(name: String) {
        val s = _uiState.value
        viewModelScope.launch {
            repository.saveGraph(
                name = name.ifBlank { "Graph ${MathFormatter.formatNumber(System.currentTimeMillis().toDouble(), 4)}" },
                expressions = s.functions.map { it.expression },
                xMin = s.xMin,
                xMax = s.xMax,
                yMin = s.yMin,
                yMax = s.yMax
            )
        }
    }

    fun deleteSavedGraph(id: Long) {
        viewModelScope.launch { repository.deleteSavedGraph(id) }
    }

    private fun recomputeAll() {
        val snapshot = _uiState.value
        viewModelScope.launch {
            val updated = withContext(Dispatchers.Default) {
                val funcs = snapshot.functions.map { fn ->
                    if (!fn.visible || fn.expression.isBlank()) {
                        fn.copy(segments = emptyList(), error = null)
                    } else {
                        try {
                            ExpressionParser.parse(fn.expression)
                            val segs = GraphEngine.sampleFunction(
                                fn.expression,
                                snapshot.xMin,
                                snapshot.xMax,
                                snapshot.yMin,
                                snapshot.yMax,
                                samples = 360,
                                angleUnit = AngleUnit.RAD
                            )
                            fn.copy(segments = segs, error = null)
                        } catch (e: Exception) {
                            fn.copy(segments = emptyList(), error = e.message ?: "Parse error")
                        }
                    }
                }
                val visibleExprs = funcs.filter { it.visible && it.error == null && it.expression.isNotBlank() }.map { it.expression }
                val features = GraphEngine.analyzeFeatures(visibleExprs, snapshot.xMin, snapshot.xMax, AngleUnit.RAD)

                val primaryExpr = visibleExprs.firstOrNull()
                var derivExpr: String? = null
                var derivSegs: List<List<GraphPoint>> = emptyList()
                if (snapshot.showDerivative && primaryExpr != null) {
                    try {
                        val d = CalculusEngine.differentiate(primaryExpr)
                        derivExpr = d.derivativeFormatted
                        derivSegs = GraphEngine.sampleFunction(
                            d.derivativeFormatted,
                            snapshot.xMin,
                            snapshot.xMax,
                            snapshot.yMin,
                            snapshot.yMax,
                            samples = 300,
                            angleUnit = AngleUnit.RAD
                        )
                    } catch (_: Exception) {
                        derivExpr = "Unavailable"
                    }
                }

                var tanY: Double? = null
                var tanSlope: Double? = null
                if (snapshot.showTangent && primaryExpr != null) {
                    try {
                        tanY = ExpressionParser.evaluateReal(ExpressionParser.parse(primaryExpr), snapshot.tangentX, AngleUnit.RAD)
                        tanSlope = CalculusEngine.numericalDerivative(primaryExpr, snapshot.tangentX)
                    } catch (_: Exception) { }
                }

                var intVal: Double? = null
                if (snapshot.showIntegralShade && primaryExpr != null) {
                    try {
                        intVal = CalculusEngine.integrateDefinite(primaryExpr, snapshot.integralA, snapshot.integralB).value
                    } catch (_: Exception) { }
                }

                snapshot.copy(
                    functions = funcs,
                    features = features,
                    derivativeExpr = derivExpr,
                    derivativeSegments = derivSegs,
                    tangentY = tanY,
                    tangentSlope = tanSlope,
                    integralValue = intVal
                )
            }
            _uiState.value = updated
        }
    }
}
