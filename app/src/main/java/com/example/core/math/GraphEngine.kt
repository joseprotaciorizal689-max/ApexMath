package com.example.core.math

import kotlin.math.abs

data class GraphPoint(val x: Double, val y: Double)

data class GraphAnalysisFeature(
    val type: String, // "Root", "Local Max", "Local Min", "Intersection"
    val x: Double,
    val y: Double,
    val label: String
)

object GraphEngine {

    /**
     * Samples a function on [xMin, xMax] into continuous curve segments,
     * automatically splitting at undefined points and steep asymptotes/discontinuities.
     */
    fun sampleFunction(
        expressionText: String,
        xMin: Double,
        xMax: Double,
        yMin: Double,
        yMax: Double,
        samples: Int = 360,
        angleUnit: AngleUnit = AngleUnit.RAD
    ): List<List<GraphPoint>> {
        val expr = try {
            ExpressionParser.parse(expressionText)
        } catch (_: Exception) {
            return emptyList()
        }
        val spanY = maxOf(1e-6, yMax - yMin)
        val dx = (xMax - xMin) / samples.coerceAtLeast(32)
        val segments = mutableListOf<List<GraphPoint>>()
        var current = mutableListOf<GraphPoint>()
        var prevY: Double? = null

        for (i in 0..samples) {
            val x = xMin + i * dx
            val y = try {
                ExpressionParser.evaluateReal(expr, x, angleUnit)
            } catch (_: Exception) {
                null
            }
            if (y == null || !y.isFinite() || abs(y) > maxOf(1e6, spanY * 100.0)) {
                if (current.size >= 2) segments += current
                current = mutableListOf()
                prevY = null
                continue
            }
            // Detect vertical asymptote / jump discontinuity when y leaps across more than 1.5x viewport height in one step
            if (prevY != null && abs(y - prevY) > spanY * 1.4 && (y * prevY < 0 || abs(y - prevY) > spanY * 2.5)) {
                if (current.size >= 2) segments += current
                current = mutableListOf()
            }
            current += GraphPoint(x, y)
            prevY = y
        }
        if (current.size >= 2) segments += current
        return segments
    }

    fun analyzeFeatures(
        expressions: List<String>,
        xMin: Double,
        xMax: Double,
        angleUnit: AngleUnit = AngleUnit.RAD
    ): List<GraphAnalysisFeature> {
        val parsed = expressions.mapNotNull { exprStr ->
            try {
                exprStr to ExpressionParser.parse(exprStr)
            } catch (_: Exception) {
                null
            }
        }
        if (parsed.isEmpty()) return emptyList()
        val features = mutableListOf<GraphAnalysisFeature>()
        val steps = 240
        val dx = (xMax - xMin) / steps

        fun evalSafe(expr: Expr, x: Double): Double? = try {
            val v = ExpressionParser.evaluateReal(expr, x, angleUnit)
            if (v.isFinite()) v else null
        } catch (_: Exception) {
            null
        }

        // Roots and Extrema for primary function(s)
        for ((idx, pair) in parsed.withIndex()) {
            val expr = pair.second
            val prefix = if (parsed.size > 1) "f${idx + 1} " else ""
            var prevX = xMin
            var prevY = evalSafe(expr, prevX)
            var prevPrevY: Double? = null
            var prevPrevX = xMin

            for (i in 1..steps) {
                val x = xMin + i * dx
                val y = evalSafe(expr, x)
                // Root check by sign change (with bisection polish)
                if (prevY != null && y != null && abs(y - prevY) < 20.0) {
                    if (abs(y) < 1e-6) {
                        features += GraphAnalysisFeature("Root", x, 0.0, "${prefix}Root (${fmt(x)}, 0)")
                    } else if (prevY * y < 0) {
                        val r = refineZero({ t -> evalSafe(expr, t) }, prevX, x)
                        if (r != null) {
                            features += GraphAnalysisFeature("Root", r, 0.0, "${prefix}Root (${fmt(r)}, 0)")
                        }
                    }
                }
                // Local extremum check
                if (prevPrevY != null && prevY != null && y != null) {
                    if (prevY > prevPrevY && prevY > y && abs(prevY - y) < 20.0) {
                        features += GraphAnalysisFeature("Local Max", prevX, prevY, "${prefix}Max (${fmt(prevX)}, ${fmt(prevY)})")
                    } else if (prevY < prevPrevY && prevY < y && abs(prevY - y) < 20.0) {
                        features += GraphAnalysisFeature("Local Min", prevX, prevY, "${prefix}Min (${fmt(prevX)}, ${fmt(prevY)})")
                    }
                }
                prevPrevX = prevX
                prevPrevY = prevY
                prevX = x
                prevY = y
            }
        }

        // Pairwise intersections between first two active functions
        if (parsed.size >= 2) {
            val f1 = parsed[0].second
            val f2 = parsed[1].second
            var prevX = xMin
            var prevDiff = run {
                val a = evalSafe(f1, prevX)
                val b = evalSafe(f2, prevX)
                if (a != null && b != null) a - b else null
            }
            for (i in 1..steps) {
                val x = xMin + i * dx
                val y1 = evalSafe(f1, x)
                val y2 = evalSafe(f2, x)
                val diff = if (y1 != null && y2 != null) y1 - y2 else null
                if (prevDiff != null && diff != null && prevDiff * diff <= 0 && abs(diff - prevDiff) < 20.0) {
                    val ix = refineZero({ t ->
                        val a = evalSafe(f1, t)
                        val b = evalSafe(f2, t)
                        if (a != null && b != null) a - b else null
                    }, prevX, x)
                    if (ix != null) {
                        val iy = evalSafe(f1, ix)
                        if (iy != null) {
                            features += GraphAnalysisFeature("Intersection", ix, iy, "Intersect (${fmt(ix)}, ${fmt(iy)})")
                        }
                    }
                }
                prevX = x
                prevDiff = diff
            }
        }

        return features.distinctBy { "${it.type}_${fmt(it.x)}" }.take(16)
    }

    private fun refineZero(fn: (Double) -> Double?, aStart: Double, bStart: Double): Double? {
        var a = aStart
        var b = bStart
        var fa = fn(a) ?: return null
        var fb = fn(b) ?: return null
        if (fa * fb > 0) return null
        repeat(20) {
            val m = 0.5 * (a + b)
            val fm = fn(m) ?: return null
            if (abs(fm) < 1e-8) return m
            if (fa * fm <= 0) {
                b = m
                fb = fm
            } else {
                a = m
                fa = fm
            }
        }
        return 0.5 * (a + b)
    }

    private fun fmt(v: Double): String = MathFormatter.formatNumber(v, 3)
}
