package com.example.core.math

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.cbrt
import kotlin.math.cos
import kotlin.math.roundToLong
import kotlin.math.sqrt

data class SolverResult(
    val title: String,
    val summary: String,
    val steps: List<String>,
    val verification: String = "",
    val isWarning: Boolean = false,
    val calculatorExpr: String? = null
)

data class MatrixData(val rows: Int, val cols: Int, val values: List<List<Double>>) {
    init {
        require(rows > 0 && cols > 0) { "Matrix dimensions must be positive" }
        require(values.size == rows && values.all { it.size == cols }) { "Matrix dimensions mismatch" }
    }

    operator fun get(r: Int, c: Int): Double = values[r][c]

    fun format(): String = values.joinToString("\n") { row ->
        "[ " + row.joinToString("   ") { MathFormatter.formatNumber(it, 6) } + " ]"
    }
}

object AlgebraEngine {

    fun solveLinear(a: Double, b: Double): SolverResult {
        val steps = mutableListOf<String>()
        val eq = "${fmtCoeff(a)}x ${fmtSigned(b)} = 0"
        steps += "Original equation: $eq"
        if (abs(a) < 1e-12) {
            return if (abs(b) < 1e-12) {
                steps += "0x + 0 = 0 holds for every real number x."
                SolverResult("Linear Equation", "Infinite solutions (Identity: x ∈ ℝ)", steps, "0 = 0 (always true)")
            } else {
                steps += "Constant contradiction: ${MathFormatter.formatNumber(b)} = 0 is impossible."
                SolverResult("Linear Equation", "No solution (Inconsistent)", steps, isWarning = true)
            }
        }
        steps += "Subtract ${MathFormatter.formatNumber(b)} from both sides: ${fmtCoeff(a)}x = ${MathFormatter.formatNumber(-b)}"
        val x = -b / a
        steps += "Divide by ${MathFormatter.formatNumber(a)}: x = ${MathFormatter.formatNumber(-b)} / ${MathFormatter.formatNumber(a)}"
        val frac = Fraction.fromDouble(x)
        val fracNote = if (frac != null && frac.den != 1L) " (${frac.format()})" else ""
        val solStr = "x = ${MathFormatter.formatNumber(x)}$fracNote"
        val check = a * x + b
        return SolverResult(
            title = "Linear Equation",
            summary = solStr,
            steps = steps,
            verification = "Check: ${MathFormatter.formatNumber(a)}(${MathFormatter.formatNumber(x)}) ${fmtSigned(b)} = ${MathFormatter.formatNumber(check)}",
            calculatorExpr = "${MathFormatter.formatNumber(-b)}/(${MathFormatter.formatNumber(a)})"
        )
    }

    fun solveQuadratic(a: Double, b: Double, c: Double): SolverResult {
        if (abs(a) < 1e-12) return solveLinear(b, c)
        val steps = mutableListOf<String>()
        steps += "Standard form: ${fmtCoeff(a)}x² ${fmtSigned(b)}x ${fmtSigned(c)} = 0"
        val disc = b * b - 4.0 * a * c
        steps += "Discriminant Δ = b² - 4ac = (${MathFormatter.formatNumber(b)})² - 4(${MathFormatter.formatNumber(a)})(${MathFormatter.formatNumber(c)}) = ${MathFormatter.formatNumber(disc)}"

        val vertexX = -b / (2.0 * a)
        val vertexY = a * vertexX * vertexX + b * vertexX + c
        steps += "Vertex: (${MathFormatter.formatNumber(vertexX)}, ${MathFormatter.formatNumber(vertexY)})"

        return when {
            disc > 1e-12 -> {
                val sqrtD = sqrt(disc)
                // Numerically stable Citardauq formula to avoid catastrophic cancellation
                val q = -0.5 * (b + (if (b >= 0) 1.0 else -1.0) * sqrtD)
                val x1 = q / a
                val x2 = if (abs(q) > 1e-15) c / q else (-b + sqrtD) / (2.0 * a)
                steps += "Δ > 0 → Two distinct real roots."
                steps += "x = (-b ± √Δ) / (2a) = (${MathFormatter.formatNumber(-b)} ± ${MathFormatter.formatNumber(sqrtD)}) / ${MathFormatter.formatNumber(2 * a)}"
                val check1 = a * x1 * x1 + b * x1 + c
                val check2 = a * x2 * x2 + b * x2 + c
                SolverResult(
                    title = "Quadratic Equation",
                    summary = "x₁ = ${MathFormatter.formatNumber(x1)},  x₂ = ${MathFormatter.formatNumber(x2)}",
                    steps = steps,
                    verification = "f(x₁) = ${MathFormatter.formatNumber(check1)}, f(x₂) = ${MathFormatter.formatNumber(check2)}",
                    calculatorExpr = "(-(${MathFormatter.formatNumber(b)})+sqrt((${MathFormatter.formatNumber(b)})^2-4*(${MathFormatter.formatNumber(a)})*(${MathFormatter.formatNumber(c)})))/(2*(${MathFormatter.formatNumber(a)}))"
                )
            }
            abs(disc) <= 1e-12 -> {
                val x = -b / (2.0 * a)
                steps += "Δ = 0 → One repeated real root (multiplicity 2)."
                steps += "x = -b / (2a) = ${MathFormatter.formatNumber(x)}"
                val check = a * x * x + b * x + c
                SolverResult(
                    title = "Quadratic Equation",
                    summary = "x = ${MathFormatter.formatNumber(x)} (double root)",
                    steps = steps,
                    verification = "f(x) = ${MathFormatter.formatNumber(check)}",
                    calculatorExpr = "${MathFormatter.formatNumber(x)}"
                )
            }
            else -> {
                val realPart = -b / (2.0 * a)
                val imagPart = sqrt(-disc) / (2.0 * abs(a))
                val z1 = Complex(realPart, imagPart)
                val z2 = Complex(realPart, -imagPart)
                steps += "Δ < 0 → Two complex conjugate roots."
                steps += "x = ${MathFormatter.formatNumber(realPart)} ± ${MathFormatter.formatNumber(imagPart)}i"
                SolverResult(
                    title = "Quadratic Equation",
                    summary = "x₁ = ${z1.format()},  x₂ = ${z2.format()}",
                    steps = steps,
                    verification = "Conjugate pair with |z| = ${MathFormatter.formatNumber(z1.magnitude)}"
                )
            }
        }
    }

    fun solveCubic(a: Double, b: Double, c: Double, d: Double): SolverResult {
        if (abs(a) < 1e-12) return solveQuadratic(b, c, d)
        val steps = mutableListOf<String>()
        steps += "Cubic: ${fmtCoeff(a)}x³ ${fmtSigned(b)}x² ${fmtSigned(c)}x ${fmtSigned(d)} = 0"
        // Normalize to x^3 + A x^2 + B x + C = 0
        val A = b / a
        val B = c / a
        val C = d / a
        steps += "Monic form: x³ ${fmtSigned(A)}x² ${fmtSigned(B)}x ${fmtSigned(C)} = 0"
        // Depress using x = t - A/3 -> t^3 + p t + q = 0
        val shift = A / 3.0
        val p = B - (A * A) / 3.0
        val q = (2.0 * A * A * A) / 27.0 - (A * B) / 3.0 + C
        steps += "Substitute x = t - ${MathFormatter.formatNumber(shift)} → t³ ${fmtSigned(p)}t ${fmtSigned(q)} = 0"
        val disc = (q * q) / 4.0 + (p * p * p) / 27.0
        steps += "Cardano discriminant (q/2)² + (p/3)³ = ${MathFormatter.formatNumber(disc)}"

        val roots = mutableListOf<Complex>()
        if (abs(disc) < 1e-12) {
            if (abs(q) < 1e-12) {
                val r = -shift
                roots.addAll(listOf(Complex(r), Complex(r), Complex(r)))
                steps += "Triple real root via depressed cubic."
            } else {
                val u = cbrt(-q / 2.0)
                roots += Complex(2.0 * u - shift)
                roots += Complex(-u - shift)
                roots += Complex(-u - shift)
                steps += "Multiple real roots via Cardano's formula."
            }
        } else if (disc > 0) {
            val sqrtDisc = sqrt(disc)
            val u = cbrt(-q / 2.0 + sqrtDisc)
            val v = cbrt(-q / 2.0 - sqrtDisc)
            val t1 = u + v
            val realPart = -0.5 * t1 - shift
            val imagPart = (sqrt(3.0) / 2.0) * (u - v)
            roots += Complex(t1 - shift)
            roots += Complex(realPart, imagPart)
            roots += Complex(realPart, -imagPart)
            steps += "Method: Cardano's formula (1 real root, 2 complex conjugate roots)."
        } else {
            // Casus irreducibilis: 3 distinct real roots using Vieta's trigonometric method
            val r = sqrt(-p * p * p / 27.0)
            val cosPhi = ((-q / 2.0) / r).coerceIn(-1.0, 1.0)
            val phi = acos(cosPhi)
            val twoSqrtNegP3 = 2.0 * sqrt(-p / 3.0)
            for (k in 0..2) {
                val t = twoSqrtNegP3 * cos((phi + 2.0 * PI * k) / 3.0)
                roots += Complex(t - shift)
            }
            steps += "Method: Vieta trigonometric casus irreducibilis (3 real roots)."
        }

        // One Newton-Raphson polish step for real roots to reduce floating-point drift
        val polished = roots.map { z ->
            if (z.isReal) {
                var x = z.re
                repeat(2) {
                    val fx = ((a * x + b) * x + c) * x + d
                    val dfx = (3.0 * a * x + 2.0 * b) * x + c
                    if (abs(dfx) > 1e-12) x -= fx / dfx
                }
                Complex(x, 0.0)
            } else z
        }

        val summary = polished.mapIndexed { idx, r -> "x${idx + 1} = ${r.format(7)}" }.joinToString("\n")
        val maxResidual = polished.maxOf { z ->
            val fz = Complex(a) * z.pow(Complex(3.0)) + Complex(b) * z.pow(Complex(2.0)) + Complex(c) * z + Complex(d)
            fz.magnitude
        }
        val warning = maxResidual > 1e-4
        if (warning) steps += "Warning: Elevated residual (${MathFormatter.formatNumber(maxResidual)}); verify ill-conditioned coefficients."

        return SolverResult(
            title = "Cubic Equation",
            summary = summary,
            steps = steps,
            verification = "Max residual |f(x)| = ${MathFormatter.formatNumber(maxResidual, 4)}",
            isWarning = warning
        )
    }

    fun solveLinearSystem(augmented: List<List<Double>>): SolverResult {
        val n = augmented.size
        require(n in 2..3 && augmented.all { it.size == n + 1 }) { "Expected 2x3 or 3x4 augmented matrix" }
        val steps = mutableListOf<String>()
        val m = augmented.map { it.toDoubleArray() }.toTypedArray()
        val vars = if (n == 2) listOf("x", "y") else listOf("x", "y", "z")

        steps += "Augmented matrix [A|b]:"
        steps += m.joinToString("\n") { row ->
            "[ " + row.dropLast(1).joinToString("  ") { MathFormatter.formatNumber(it, 5) } +
                " | " + MathFormatter.formatNumber(row.last(), 5) + " ]"
        }

        // Gaussian elimination with partial pivoting (RREF)
        var pivotRow = 0
        val pivotCols = mutableListOf<Int>()
        for (col in 0 until n) {
            var bestRow = pivotRow
            var bestVal = 0.0
            for (r in pivotRow until n) {
                if (abs(m[r][col]) > bestVal) {
                    bestVal = abs(m[r][col])
                    bestRow = r
                }
            }
            if (bestVal < 1e-11) continue
            if (bestRow != pivotRow) {
                val tmp = m[pivotRow]
                m[pivotRow] = m[bestRow]
                m[bestRow] = tmp
                steps += "Swap R${pivotRow + 1} ↔ R${bestRow + 1}"
            }
            val pivot = m[pivotRow][col]
            for (j in col..n) m[pivotRow][j] /= pivot
            for (r in 0 until n) {
                if (r != pivotRow && abs(m[r][col]) > 1e-11) {
                    val factor = m[r][col]
                    for (j in col..n) m[r][j] -= factor * m[pivotRow][j]
                }
            }
            pivotCols += col
            pivotRow++
        }

        // Check consistency
        for (r in pivotRow until n) {
            if (abs(m[r][n]) > 1e-9) {
                steps += "Row ${r + 1} reduces to [0 ... 0 | ${MathFormatter.formatNumber(m[r][n])}] ≠ 0."
                return SolverResult(
                    title = "${n}×${n} Linear System",
                    summary = "No solution (Inconsistent system)",
                    steps = steps,
                    isWarning = true
                )
            }
        }

        if (pivotCols.size < n) {
            steps += "Rank(A) = ${pivotCols.size} < $n (singular matrix with consistent RHS)."
            return SolverResult(
                title = "${n}×${n} Linear System",
                summary = "Infinitely many solutions (Rank = ${pivotCols.size} < $n)",
                steps = steps,
                isWarning = true
            )
        }

        val sol = DoubleArray(n) { r -> m[r][n] }
        val summary = vars.indices.joinToString(",  ") { i -> "${vars[i]} = ${MathFormatter.formatNumber(sol[i], 7)}" }

        // Verify against original equations
        val residuals = (0 until n).map { r ->
            val lhs = (0 until n).sumOf { c -> augmented[r][c] * sol[c] }
            abs(lhs - augmented[r][n])
        }
        val maxErr = residuals.maxOrNull() ?: 0.0
        steps += "RREF identity reached via partial pivoting."
        return SolverResult(
            title = "${n}×${n} Linear System",
            summary = summary,
            steps = steps,
            verification = "Max residual ||Ax - b||∞ = ${MathFormatter.formatNumber(maxErr, 4)}"
        )
    }

    // Matrix operations
    fun addMatrices(a: MatrixData, b: MatrixData): MatrixData {
        require(a.rows == b.rows && a.cols == b.cols) { "Dimension mismatch: ${a.rows}×${a.cols} vs ${b.rows}×${b.cols}" }
        return MatrixData(a.rows, a.cols, List(a.rows) { r -> List(a.cols) { c -> a[r, c] + b[r, c] } })
    }

    fun subMatrices(a: MatrixData, b: MatrixData): MatrixData {
        require(a.rows == b.rows && a.cols == b.cols) { "Dimension mismatch: ${a.rows}×${a.cols} vs ${b.rows}×${b.cols}" }
        return MatrixData(a.rows, a.cols, List(a.rows) { r -> List(a.cols) { c -> a[r, c] - b[r, c] } })
    }

    fun multiplyMatrices(a: MatrixData, b: MatrixData): MatrixData {
        require(a.cols == b.rows) { "Cannot multiply ${a.rows}×${a.cols} by ${b.rows}×${b.cols} (inner dimensions must match)" }
        return MatrixData(a.rows, b.cols, List(a.rows) { r ->
            List(b.cols) { c ->
                (0 until a.cols).sumOf { k -> a[r, k] * b[k, c] }
            }
        })
    }

    fun scaleMatrix(a: MatrixData, scalar: Double): MatrixData {
        return MatrixData(a.rows, a.cols, List(a.rows) { r -> List(a.cols) { c -> a[r, c] * scalar } })
    }

    fun transposeMatrix(a: MatrixData): MatrixData {
        return MatrixData(a.cols, a.rows, List(a.cols) { r -> List(a.rows) { c -> a[c, r] } })
    }

    fun determinant(a: MatrixData): Double {
        require(a.rows == a.cols) { "Determinant requires a square matrix" }
        val n = a.rows
        val m = Array(n) { r -> DoubleArray(n) { c -> a[r, c] } }
        var det = 1.0
        for (i in 0 until n) {
            var pivot = i
            for (r in i + 1 until n) {
                if (abs(m[r][i]) > abs(m[pivot][i])) pivot = r
            }
            if (abs(m[pivot][i]) < 1e-12) return 0.0
            if (pivot != i) {
                val tmp = m[i]
                m[i] = m[pivot]
                m[pivot] = tmp
                det = -det
            }
            det *= m[i][i]
            for (r in i + 1 until n) {
                val factor = m[r][i] / m[i][i]
                for (c in i until n) {
                    m[r][c] -= factor * m[i][c]
                }
            }
        }
        return det
    }

    fun inverseMatrix(a: MatrixData): MatrixData {
        require(a.rows == a.cols) { "Inverse requires a square matrix" }
        val n = a.rows
        val aug = Array(n) { r ->
            DoubleArray(2 * n) { c ->
                if (c < n) a[r, c] else if (c - n == r) 1.0 else 0.0
            }
        }
        for (col in 0 until n) {
            var pivot = col
            for (r in col + 1 until n) {
                if (abs(aug[r][col]) > abs(aug[pivot][col])) pivot = r
            }
            if (abs(aug[pivot][col]) < 1e-12) {
                throw ArithmeticException("Matrix is singular (det ≈ 0) and has no inverse")
            }
            if (pivot != col) {
                val tmp = aug[col]
                aug[col] = aug[pivot]
                aug[pivot] = tmp
            }
            val div = aug[col][col]
            for (c in 0 until 2 * n) aug[col][c] /= div
            for (r in 0 until n) {
                if (r != col && abs(aug[r][col]) > 1e-12) {
                    val factor = aug[r][col]
                    for (c in 0 until 2 * n) aug[r][c] -= factor * aug[col][c]
                }
            }
        }
        return MatrixData(n, n, List(n) { r -> List(n) { c -> aug[r][c + n] } })
    }

    fun matrixRank(a: MatrixData): Int {
        val m = Array(a.rows) { r -> DoubleArray(a.cols) { c -> a[r, c] } }
        var rank = 0
        var r = 0
        for (c in 0 until a.cols) {
            if (r >= a.rows) break
            var pivot = r
            for (i in r + 1 until a.rows) {
                if (abs(m[i][c]) > abs(m[pivot][c])) pivot = i
            }
            if (abs(m[pivot][c]) < 1e-11) continue
            val tmp = m[r]
            m[r] = m[pivot]
            m[pivot] = tmp
            val div = m[r][c]
            for (j in c until a.cols) m[r][j] /= div
            for (i in 0 until a.rows) {
                if (i != r && abs(m[i][c]) > 1e-11) {
                    val f = m[i][c]
                    for (j in c until a.cols) m[i][j] -= f * m[r][j]
                }
            }
            rank++
            r++
        }
        return rank
    }

    fun matrixPower(a: MatrixData, power: Int): MatrixData {
        require(a.rows == a.cols) { "Matrix power requires a square matrix" }
        require(power in 0..12) { "Matrix power supported for integer exponents 0..12" }
        val n = a.rows
        var res = MatrixData(n, n, List(n) { r -> List(n) { c -> if (r == c) 1.0 else 0.0 } })
        var base = a
        var p = power
        while (p > 0) {
            if (p % 2 == 1) res = multiplyMatrices(res, base)
            p /= 2
            if (p > 0) base = multiplyMatrices(base, base)
        }
        return res
    }

    fun eigen2x2(a: MatrixData): SolverResult {
        require(a.rows == 2 && a.cols == 2) { "Eigenanalysis is implemented for 2×2 matrices" }
        val m00 = a[0, 0]
        val m01 = a[0, 1]
        val m10 = a[1, 0]
        val m11 = a[1, 1]
        val trace = m00 + m11
        val det = m00 * m11 - m01 * m10
        val disc = trace * trace - 4.0 * det
        val steps = mutableListOf(
            "Characteristic polynomial: λ² - (${MathFormatter.formatNumber(trace)})λ + (${MathFormatter.formatNumber(det)}) = 0",
            "Trace = ${MathFormatter.formatNumber(trace)}, Det = ${MathFormatter.formatNumber(det)}, Discriminant = ${MathFormatter.formatNumber(disc)}"
        )
        return if (disc >= -1e-11) {
            val s = sqrt(maxOf(0.0, disc))
            val l1 = 0.5 * (trace + s)
            val l2 = 0.5 * (trace - s)
            val v1 = eigenvector2x2(m00, m01, m10, m11, l1)
            val v2 = eigenvector2x2(m00, m01, m10, m11, l2)
            steps += "v₁ ≈ (${MathFormatter.formatNumber(v1.first, 4)}, ${MathFormatter.formatNumber(v1.second, 4)})"
            steps += "v₂ ≈ (${MathFormatter.formatNumber(v2.first, 4)}, ${MathFormatter.formatNumber(v2.second, 4)})"
            SolverResult(
                title = "2×2 Eigenvalues & Eigenvectors",
                summary = "λ₁ = ${MathFormatter.formatNumber(l1, 6)},  λ₂ = ${MathFormatter.formatNumber(l2, 6)}",
                steps = steps
            )
        } else {
            val re = trace / 2.0
            val im = sqrt(-disc) / 2.0
            val z1 = Complex(re, im)
            val z2 = Complex(re, -im)
            steps += "Complex conjugate eigenvalues; real eigenvectors do not exist."
            SolverResult(
                title = "2×2 Eigenvalues",
                summary = "λ₁ = ${z1.format(5)},  λ₂ = ${z2.format(5)}",
                steps = steps
            )
        }
    }

    private fun eigenvector2x2(a: Double, b: Double, c: Double, d: Double, lambda: Double): Pair<Double, Double> {
        return when {
            abs(b) > 1e-9 -> Pair(b, lambda - a)
            abs(c) > 1e-9 -> Pair(lambda - d, c)
            abs(a - lambda) < 1e-9 -> Pair(1.0, 0.0)
            else -> Pair(0.0, 1.0)
        }
    }

    // Polynomial tools (descending order: coeffs[0]*x^n + ... + coeffs[n])
    fun trimPolynomial(coeffs: List<Double>): List<Double> {
        val idx = coeffs.indexOfFirst { abs(it) > 1e-12 }
        return if (idx == -1) listOf(0.0) else coeffs.subList(idx, coeffs.size)
    }

    fun evaluatePolynomial(coeffs: List<Double>, x: Double): Double {
        var acc = 0.0
        for (c in coeffs) acc = acc * x + c
        return acc
    }

    fun addPolynomials(p1: List<Double>, p2: List<Double>): List<Double> {
        val len = maxOf(p1.size, p2.size)
        val a = List(len - p1.size) { 0.0 } + p1
        val b = List(len - p2.size) { 0.0 } + p2
        return trimPolynomial(List(len) { i -> a[i] + b[i] })
    }

    fun multiplyPolynomials(p1: List<Double>, p2: List<Double>): List<Double> {
        val out = DoubleArray(p1.size + p2.size - 1)
        for (i in p1.indices) {
            for (j in p2.indices) {
                out[i + j] += p1[i] * p2[j]
            }
        }
        return trimPolynomial(out.toList())
    }

    fun differentiatePolynomial(coeffs: List<Double>): List<Double> {
        val trimmed = trimPolynomial(coeffs)
        if (trimmed.size <= 1) return listOf(0.0)
        val deg = trimmed.size - 1
        return trimPolynomial(List(deg) { i -> trimmed[i] * (deg - i) })
    }

    fun integratePolynomial(coeffs: List<Double>): List<Double> {
        val trimmed = trimPolynomial(coeffs)
        val deg = trimmed.size - 1
        return List(trimmed.size) { i -> trimmed[i] / (deg - i + 1) } + listOf(0.0)
    }

    fun formatPolynomial(coeffs: List<Double>): String {
        val p = trimPolynomial(coeffs)
        val deg = p.size - 1
        if (deg == 0) return MathFormatter.formatNumber(p[0])
        val parts = mutableListOf<String>()
        for (i in p.indices) {
            val c = p[i]
            if (abs(c) < 1e-12) continue
            val pow = deg - i
            val absC = abs(c)
            val sign = if (parts.isEmpty()) (if (c < 0) "-" else "") else (if (c < 0) " - " else " + ")
            val coeffStr = if (pow > 0 && abs(absC - 1.0) < 1e-12) "" else MathFormatter.formatNumber(absC)
            val xStr = when (pow) {
                0 -> ""
                1 -> "x"
                else -> "x^$pow"
            }
            parts += "$sign$coeffStr$xStr"
        }
        return if (parts.isEmpty()) "0" else parts.joinToString("")
    }

    // Number Theory
    fun isPrime(n: Long): Boolean {
        if (n < 2L) return false
        if (n == 2L || n == 3L) return true
        if (n % 2L == 0L || n % 3L == 0L) return false
        var i = 5L
        while (i * i <= n) {
            if (n % i == 0L || n % (i + 2L) == 0L) return false
            i += 6L
        }
        return true
    }

    fun primeFactorization(nInput: Long): Map<Long, Int> {
        require(nInput >= 2L) { "Prime factorization requires an integer ≥ 2" }
        var n = nInput
        val factors = linkedMapOf<Long, Int>()
        var d = 2L
        while (d * d <= n) {
            while (n % d == 0L) {
                factors[d] = (factors[d] ?: 0) + 1
                n /= d
            }
            d = if (d == 2L) 3L else d + 2L
        }
        if (n > 1L) factors[n] = (factors[n] ?: 0) + 1
        return factors
    }

    fun divisors(nInput: Long): List<Long> {
        val n = abs(nInput)
        require(n in 1L..1_000_000_000L) { "Divisors supported for 1 ≤ |n| ≤ 10⁹" }
        val small = mutableListOf<Long>()
        val large = mutableListOf<Long>()
        var i = 1L
        while (i * i <= n) {
            if (n % i == 0L) {
                small += i
                if (i * i != n) large += n / i
            }
            i++
        }
        return small + large.reversed()
    }

    fun modPow(base: Long, exp: Long, mod: Long): Long {
        require(mod > 0L && exp >= 0L) { "Modulus must be > 0 and exponent ≥ 0" }
        var result = 1L
        var b = ((base % mod) + mod) % mod
        var e = exp
        while (e > 0L) {
            if (e % 2L == 1L) result = multiplyModSafe(result, b, mod)
            b = multiplyModSafe(b, b, mod)
            e /= 2L
        }
        return result
    }

    private fun multiplyModSafe(a: Long, b: Long, mod: Long): Long {
        return java.math.BigInteger.valueOf(a)
            .multiply(java.math.BigInteger.valueOf(b))
            .mod(java.math.BigInteger.valueOf(mod))
            .toLong()
    }

    fun modInverse(a: Long, mod: Long): Long? {
        require(mod > 1L) { "Modulus must be > 1" }
        var t = 0L
        var newT = 1L
        var r = mod
        var newR = ((a % mod) + mod) % mod
        while (newR != 0L) {
            val q = r / newR
            val tmpT = t - q * newT
            t = newT
            newT = tmpT
            val tmpR = r - q * newR
            r = newR
            newR = tmpR
        }
        if (r > 1L) return null
        if (t < 0L) t += mod
        return t
    }

    private fun fmtCoeff(v: Double): String = MathFormatter.formatNumber(v)
    private fun fmtSigned(v: Double): String =
        if (v < 0) "- ${MathFormatter.formatNumber(abs(v))}" else "+ ${MathFormatter.formatNumber(v)}"
}
