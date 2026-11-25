package com.example.core.math

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.round

data class SymbolicDiffResult(
    val original: String,
    val derivativeExpr: Expr,
    val derivativeFormatted: String,
    val secondDerivativeFormatted: String?,
    val steps: List<String>,
    val numericAtPoint: Double? = null,
    val pointX: Double? = null
)

sealed class SymbolicIntegralResult {
    data class Success(val antiderivative: String, val steps: List<String>) : SymbolicIntegralResult()
    data class Unsupported(val reason: String) : SymbolicIntegralResult()
}

data class DefiniteIntegralResult(
    val value: Double,
    val estimatedError: Double,
    val evaluations: Int,
    val method: String,
    val warning: String? = null
)

data class LimitSample(val x: Double, val fx: Double?)

data class LimitResult(
    val targetDisplay: String,
    val leftEstimate: Double?,
    val rightEstimate: Double?,
    val twoSidedEstimate: Double?,
    val status: String,
    val note: String,
    val leftSamples: List<LimitSample>,
    val rightSamples: List<LimitSample>,
    val isWarning: Boolean = false
)

data class TaylorTerm(val degree: Int, val coeff: Double, val derivativeAtCenter: Double)

data class TaylorSeriesResult(
    val center: Double,
    val order: Int,
    val polynomialExpr: String,
    val terms: List<TaylorTerm>,
    val testX: Double?,
    val exactAtTest: Double?,
    val approxAtTest: Double?,
    val errorAtTest: Double?
)

data class RootIteration(val iter: Int, val x: Double, val fx: Double)

data class RootFindResult(
    val method: String,
    val root: Double?,
    val residual: Double?,
    val converged: Boolean,
    val message: String,
    val iterations: List<RootIteration>
)

object CalculusEngine {

    fun formatExpr(expr: Expr): String = when (expr) {
        is Expr.Num -> MathFormatter.formatNumber(expr.value, 8)
        is Expr.ConstSymbol -> expr.name
        is Expr.Var -> expr.name
        is Expr.Neg -> {
            val inner = expr.inner
            if (inner is Expr.Add || inner is Expr.Sub) "-(${formatExpr(inner)})" else "-${formatExpr(inner)}"
        }
        is Expr.Factorial -> "${wrapIfComplex(expr.inner)}!"
        is Expr.Add -> "${formatExpr(expr.left)} + ${formatExpr(expr.right)}"
        is Expr.Sub -> {
            val r = expr.right
            val rStr = if (r is Expr.Add || r is Expr.Sub) "(${formatExpr(r)})" else formatExpr(r)
            "${formatExpr(expr.left)} - $rStr"
        }
        is Expr.Mul -> {
            val l = formatFactor(expr.left)
            val r = formatFactor(expr.right)
            if (expr.left is Expr.Num && (expr.right is Expr.Var || expr.right is Expr.ConstSymbol || expr.right is Expr.Call)) {
                "$l*$r"
            } else {
                "$l*$r"
            }
        }
        is Expr.Div -> "${formatFactor(expr.left)} / ${wrapDenominator(expr.right)}"
        is Expr.Mod -> "${formatFactor(expr.left)} % ${formatFactor(expr.right)}"
        is Expr.Pow -> "${wrapBase(expr.base)}^${wrapExp(expr.exp)}"
        is Expr.Call -> "${expr.name}(${expr.args.joinToString(", ") { formatExpr(it) }})"
    }

    private fun wrapIfComplex(e: Expr): String = when (e) {
        is Expr.Num, is Expr.Var, is Expr.ConstSymbol, is Expr.Call -> formatExpr(e)
        else -> "(${formatExpr(e)})"
    }

    private fun formatFactor(e: Expr): String = when (e) {
        is Expr.Add, is Expr.Sub -> "(${formatExpr(e)})"
        else -> formatExpr(e)
    }

    private fun wrapDenominator(e: Expr): String = when (e) {
        is Expr.Num, is Expr.Var, is Expr.ConstSymbol, is Expr.Call -> formatExpr(e)
        else -> "(${formatExpr(e)})"
    }

    private fun wrapBase(e: Expr): String = when (e) {
        is Expr.Num -> if (e.value < 0) "(${formatExpr(e)})" else formatExpr(e)
        is Expr.Var, is Expr.ConstSymbol, is Expr.Call -> formatExpr(e)
        else -> "(${formatExpr(e)})"
    }

    private fun wrapExp(e: Expr): String = when (e) {
        is Expr.Num -> if (e.value < 0) "(${formatExpr(e)})" else formatExpr(e)
        is Expr.Var, is Expr.ConstSymbol, is Expr.Call -> formatExpr(e)
        else -> "(${formatExpr(e)})"
    }

    fun simplify(expr: Expr): Expr {
        return when (expr) {
            is Expr.Num, is Expr.ConstSymbol, is Expr.Var -> expr
            is Expr.Neg -> {
                val inner = simplify(expr.inner)
                when (inner) {
                    is Expr.Num -> Expr.Num(-inner.value)
                    is Expr.Neg -> inner.inner
                    else -> Expr.Neg(inner)
                }
            }
            is Expr.Factorial -> Expr.Factorial(simplify(expr.inner))
            is Expr.Add -> {
                val l = simplify(expr.left)
                val r = simplify(expr.right)
                when {
                    l is Expr.Num && r is Expr.Num -> Expr.Num(l.value + r.value)
                    l is Expr.Num && abs(l.value) < 1e-14 -> r
                    r is Expr.Num && abs(r.value) < 1e-14 -> l
                    r is Expr.Neg -> simplify(Expr.Sub(l, r.inner))
                    r is Expr.Num && r.value < 0 -> simplify(Expr.Sub(l, Expr.Num(-r.value)))
                    else -> Expr.Add(l, r)
                }
            }
            is Expr.Sub -> {
                val l = simplify(expr.left)
                val r = simplify(expr.right)
                when {
                    l is Expr.Num && r is Expr.Num -> Expr.Num(l.value - r.value)
                    r is Expr.Num && abs(r.value) < 1e-14 -> l
                    l is Expr.Num && abs(l.value) < 1e-14 -> simplify(Expr.Neg(r))
                    r is Expr.Neg -> simplify(Expr.Add(l, r.inner))
                    l == r -> Expr.Num(0.0)
                    else -> Expr.Sub(l, r)
                }
            }
            is Expr.Mul -> {
                val l = simplify(expr.left)
                val r = simplify(expr.right)
                when {
                    l is Expr.Num && abs(l.value) < 1e-14 -> Expr.Num(0.0)
                    r is Expr.Num && abs(r.value) < 1e-14 -> Expr.Num(0.0)
                    l is Expr.Num && r is Expr.Num -> Expr.Num(l.value * r.value)
                    l is Expr.Num && abs(l.value - 1.0) < 1e-14 -> r
                    r is Expr.Num && abs(r.value - 1.0) < 1e-14 -> l
                    l is Expr.Num && abs(l.value + 1.0) < 1e-14 -> simplify(Expr.Neg(r))
                    r is Expr.Num && abs(r.value + 1.0) < 1e-14 -> simplify(Expr.Neg(l))
                    r is Expr.Num && l !is Expr.Num -> simplify(Expr.Mul(r, l))
                    l is Expr.Num && r is Expr.Mul && r.left is Expr.Num -> {
                        simplify(Expr.Mul(Expr.Num(l.value * r.left.value), r.right))
                    }
                    else -> Expr.Mul(l, r)
                }
            }
            is Expr.Div -> {
                val l = simplify(expr.left)
                val r = simplify(expr.right)
                when {
                    l is Expr.Num && abs(l.value) < 1e-14 -> Expr.Num(0.0)
                    r is Expr.Num && abs(r.value - 1.0) < 1e-14 -> l
                    l is Expr.Num && r is Expr.Num && abs(r.value) > 1e-14 -> Expr.Num(l.value / r.value)
                    l == r -> Expr.Num(1.0)
                    else -> Expr.Div(l, r)
                }
            }
            is Expr.Mod -> Expr.Mod(simplify(expr.left), simplify(expr.right))
            is Expr.Pow -> {
                val b = simplify(expr.base)
                val e = simplify(expr.exp)
                when {
                    e is Expr.Num && abs(e.value) < 1e-14 -> Expr.Num(1.0)
                    e is Expr.Num && abs(e.value - 1.0) < 1e-14 -> b
                    b is Expr.Num && abs(b.value) < 1e-14 -> Expr.Num(0.0)
                    b is Expr.Num && abs(b.value - 1.0) < 1e-14 -> Expr.Num(1.0)
                    b is Expr.Num && e is Expr.Num -> Expr.Num(b.value.pow(e.value))
                    b is Expr.Pow && e is Expr.Num && b.exp is Expr.Num -> {
                        simplify(Expr.Pow(b.base, Expr.Num(b.exp.value * e.value)))
                    }
                    else -> Expr.Pow(b, e)
                }
            }
            is Expr.Call -> Expr.Call(expr.name, expr.args.map { simplify(it) })
        }
    }

    fun containsVar(expr: Expr, varName: String = "x"): Boolean = when (expr) {
        is Expr.Num, is Expr.ConstSymbol -> false
        is Expr.Var -> expr.name.equals(varName, ignoreCase = true)
        is Expr.Neg -> containsVar(expr.inner, varName)
        is Expr.Factorial -> containsVar(expr.inner, varName)
        is Expr.Add -> containsVar(expr.left, varName) || containsVar(expr.right, varName)
        is Expr.Sub -> containsVar(expr.left, varName) || containsVar(expr.right, varName)
        is Expr.Mul -> containsVar(expr.left, varName) || containsVar(expr.right, varName)
        is Expr.Div -> containsVar(expr.left, varName) || containsVar(expr.right, varName)
        is Expr.Mod -> containsVar(expr.left, varName) || containsVar(expr.right, varName)
        is Expr.Pow -> containsVar(expr.base, varName) || containsVar(expr.exp, varName)
        is Expr.Call -> expr.args.any { containsVar(it, varName) }
    }

    fun differentiate(expressionText: String, evalAtX: Double? = null): SymbolicDiffResult {
        val parsed = simplify(ExpressionParser.parse(expressionText))
        val steps = mutableListOf<String>()
        val d1 = simplify(diffAst(parsed, "x", steps))
        val d2 = try {
            simplify(diffAst(d1, "x", mutableListOf()))
        } catch (_: Exception) {
            null
        }
        val numVal = evalAtX?.let { x ->
            try {
                ExpressionParser.evaluateReal(d1, x, AngleUnit.RAD)
            } catch (_: Exception) {
                null
            }
        }
        return SymbolicDiffResult(
            original = formatExpr(parsed),
            derivativeExpr = d1,
            derivativeFormatted = formatExpr(d1),
            secondDerivativeFormatted = d2?.let { formatExpr(it) },
            steps = steps.distinct().take(12),
            numericAtPoint = numVal,
            pointX = evalAtX
        )
    }

    fun diffAst(expr: Expr, v: String = "x", steps: MutableList<String> = mutableListOf()): Expr {
        if (!containsVar(expr, v)) {
            return Expr.Num(0.0)
        }
        return when (expr) {
            is Expr.Var -> {
                if (expr.name.equals(v, ignoreCase = true)) {
                    steps += "d/dx(x) = 1"
                    Expr.Num(1.0)
                } else Expr.Num(0.0)
            }
            is Expr.Num, is Expr.ConstSymbol -> Expr.Num(0.0)
            is Expr.Neg -> Expr.Neg(diffAst(expr.inner, v, steps))
            is Expr.Add -> {
                steps += "Sum rule: d/dx(u + v) = u' + v'"
                Expr.Add(diffAst(expr.left, v, steps), diffAst(expr.right, v, steps))
            }
            is Expr.Sub -> {
                steps += "Difference rule: d/dx(u - v) = u' - v'"
                Expr.Sub(diffAst(expr.left, v, steps), diffAst(expr.right, v, steps))
            }
            is Expr.Mul -> {
                val lHas = containsVar(expr.left, v)
                val rHas = containsVar(expr.right, v)
                when {
                    !lHas -> {
                        steps += "Constant multiple rule: d/dx(c·u) = c·u'"
                        Expr.Mul(expr.left, diffAst(expr.right, v, steps))
                    }
                    !rHas -> {
                        steps += "Constant multiple rule: d/dx(u·c) = c·u'"
                        Expr.Mul(expr.right, diffAst(expr.left, v, steps))
                    }
                    else -> {
                        steps += "Product rule: d/dx(u·v) = u'v + uv'"
                        Expr.Add(
                            Expr.Mul(diffAst(expr.left, v, steps), expr.right),
                            Expr.Mul(expr.left, diffAst(expr.right, v, steps))
                        )
                    }
                }
            }
            is Expr.Div -> {
                val rHas = containsVar(expr.right, v)
                if (!rHas) {
                    steps += "Constant divisor rule: d/dx(u/c) = u'/c"
                    Expr.Div(diffAst(expr.left, v, steps), expr.right)
                } else {
                    steps += "Quotient rule: d/dx(u/v) = (u'v - uv') / v²"
                    Expr.Div(
                        Expr.Sub(
                            Expr.Mul(diffAst(expr.left, v, steps), expr.right),
                            Expr.Mul(expr.left, diffAst(expr.right, v, steps))
                        ),
                        Expr.Pow(expr.right, Expr.Num(2.0))
                    )
                }
            }
            is Expr.Pow -> {
                val bHas = containsVar(expr.base, v)
                val eHas = containsVar(expr.exp, v)
                when {
                    bHas && !eHas -> {
                        steps += "Power + Chain rule: d/dx(u^n) = n·u^(n-1)·u'"
                        val uPrime = diffAst(expr.base, v, steps)
                        val newExp = simplify(Expr.Sub(expr.exp, Expr.Num(1.0)))
                        Expr.Mul(Expr.Mul(expr.exp, Expr.Pow(expr.base, newExp)), uPrime)
                    }
                    !bHas && eHas -> {
                        steps += "Exponential rule: d/dx(a^u) = a^u · ln(a) · u'"
                        val vPrime = diffAst(expr.exp, v, steps)
                        Expr.Mul(Expr.Mul(expr, Expr.Call("ln", listOf(expr.base))), vPrime)
                    }
                    else -> {
                        // General u^v = exp(v * ln(u))
                        steps += "Logarithmic differentiation: d/dx(u^v) = u^v · (v'·ln(u) + v·u'/u)"
                        val u = expr.base
                        val w = expr.exp
                        val du = diffAst(u, v, steps)
                        val dw = diffAst(w, v, steps)
                        Expr.Mul(
                            expr,
                            Expr.Add(
                                Expr.Mul(dw, Expr.Call("ln", listOf(u))),
                                Expr.Div(Expr.Mul(w, du), u)
                            )
                        )
                    }
                }
            }
            is Expr.Call -> {
                if (expr.args.size != 1) {
                    throw UnsupportedOperationException("Symbolic differentiation supports single-argument functions")
                }
                val u = expr.args[0]
                val uPrime = diffAst(u, v, steps)
                val name = expr.name.lowercase()
                val isChain = u !is Expr.Var
                if (isChain) steps += "Chain rule applied to $name(${formatExpr(u)})"
                val outerDeriv: Expr = when (name) {
                    "sin" -> {
                        steps += "d/du sin(u) = cos(u)"
                        Expr.Call("cos", listOf(u))
                    }
                    "cos" -> {
                        steps += "d/du cos(u) = -sin(u)"
                        Expr.Neg(Expr.Call("sin", listOf(u)))
                    }
                    "tan" -> {
                        steps += "d/du tan(u) = 1 / cos²(u)"
                        Expr.Div(Expr.Num(1.0), Expr.Pow(Expr.Call("cos", listOf(u)), Expr.Num(2.0)))
                    }
                    "sec" -> {
                        steps += "d/du sec(u) = sec(u)·tan(u)"
                        Expr.Mul(Expr.Call("sec", listOf(u)), Expr.Call("tan", listOf(u)))
                    }
                    "csc" -> {
                        steps += "d/du csc(u) = -csc(u)·cot(u)"
                        Expr.Neg(Expr.Mul(Expr.Call("csc", listOf(u)), Expr.Call("cot", listOf(u))))
                    }
                    "cot" -> {
                        steps += "d/du cot(u) = -1 / sin²(u)"
                        Expr.Neg(Expr.Div(Expr.Num(1.0), Expr.Pow(Expr.Call("sin", listOf(u)), Expr.Num(2.0))))
                    }
                    "asin", "arcsin" -> {
                        steps += "d/du arcsin(u) = 1 / √(1 - u²)"
                        Expr.Div(Expr.Num(1.0), Expr.Call("sqrt", listOf(Expr.Sub(Expr.Num(1.0), Expr.Pow(u, Expr.Num(2.0))))))
                    }
                    "acos", "arccos" -> {
                        steps += "d/du arccos(u) = -1 / √(1 - u²)"
                        Expr.Neg(Expr.Div(Expr.Num(1.0), Expr.Call("sqrt", listOf(Expr.Sub(Expr.Num(1.0), Expr.Pow(u, Expr.Num(2.0)))))))
                    }
                    "atan", "arctan" -> {
                        steps += "d/du arctan(u) = 1 / (1 + u²)"
                        Expr.Div(Expr.Num(1.0), Expr.Add(Expr.Num(1.0), Expr.Pow(u, Expr.Num(2.0))))
                    }
                    "sinh" -> {
                        steps += "d/du sinh(u) = cosh(u)"
                        Expr.Call("cosh", listOf(u))
                    }
                    "cosh" -> {
                        steps += "d/du cosh(u) = sinh(u)"
                        Expr.Call("sinh", listOf(u))
                    }
                    "tanh" -> {
                        steps += "d/du tanh(u) = 1 / cosh²(u)"
                        Expr.Div(Expr.Num(1.0), Expr.Pow(Expr.Call("cosh", listOf(u)), Expr.Num(2.0)))
                    }
                    "ln" -> {
                        steps += "d/du ln(u) = 1 / u"
                        Expr.Div(Expr.Num(1.0), u)
                    }
                    "log", "log10" -> {
                        steps += "d/du log₁₀(u) = 1 / (u · ln(10))"
                        Expr.Div(Expr.Num(1.0), Expr.Mul(u, Expr.Call("ln", listOf(Expr.Num(10.0)))))
                    }
                    "exp" -> {
                        steps += "d/du exp(u) = exp(u)"
                        Expr.Call("exp", listOf(u))
                    }
                    "sqrt" -> {
                        steps += "d/du √u = 1 / (2√u)"
                        Expr.Div(Expr.Num(1.0), Expr.Mul(Expr.Num(2.0), Expr.Call("sqrt", listOf(u))))
                    }
                    "cbrt" -> {
                        steps += "d/du ∛u = 1 / (3·u^(2/3))"
                        Expr.Div(Expr.Num(1.0), Expr.Mul(Expr.Num(3.0), Expr.Pow(u, Expr.Div(Expr.Num(2.0), Expr.Num(3.0)))))
                    }
                    "abs" -> {
                        steps += "d/du |u| = u / |u| (for u ≠ 0)"
                        Expr.Div(u, Expr.Call("abs", listOf(u)))
                    }
                    else -> throw UnsupportedOperationException("Symbolic derivative for '$name' is not supported")
                }
                Expr.Mul(outerDeriv, uPrime)
            }
            is Expr.Factorial, is Expr.Mod -> {
                throw UnsupportedOperationException("Symbolic derivative is undefined for discrete/modulo operator")
            }
        }
    }

    fun numericalDerivative(expressionText: String, x: Double): Double {
        val expr = ExpressionParser.parse(expressionText)
        val h = maxOf(1e-6, 1e-5 * abs(x))
        // 4th-order central difference (Richardson extrapolation)
        val fPlus1 = ExpressionParser.evaluateReal(expr, x + h, AngleUnit.RAD)
        val fMinus1 = ExpressionParser.evaluateReal(expr, x - h, AngleUnit.RAD)
        val fPlus2 = ExpressionParser.evaluateReal(expr, x + 2 * h, AngleUnit.RAD)
        val fMinus2 = ExpressionParser.evaluateReal(expr, x - 2 * h, AngleUnit.RAD)
        return (-fPlus2 + 8.0 * fPlus1 - 8.0 * fMinus1 + fMinus2) / (12.0 * h)
    }

    // Honest Symbolic Antiderivative for elementary forms
    fun integrateSymbolic(expressionText: String): SymbolicIntegralResult {
        return try {
            val expr = simplify(ExpressionParser.parse(expressionText))
            val steps = mutableListOf<String>()
            val anti = simplify(integrateAst(expr, steps))
            SymbolicIntegralResult.Success("${formatExpr(anti)} + C", steps.distinct())
        } catch (e: UnsupportedOperationException) {
            SymbolicIntegralResult.Unsupported(e.message ?: "No elementary closed-form antiderivative found in Alpha symbolic engine.")
        } catch (e: Exception) {
            SymbolicIntegralResult.Unsupported(e.message ?: "Unable to integrate expression symbolically.")
        }
    }

    private fun integrateAst(expr: Expr, steps: MutableList<String>): Expr {
        if (!containsVar(expr, "x")) {
            steps += "Constant rule: ∫ k dx = k·x"
            return Expr.Mul(expr, Expr.Var("x"))
        }
        return when (expr) {
            is Expr.Var -> {
                steps += "Power rule: ∫ x dx = x² / 2"
                Expr.Div(Expr.Pow(Expr.Var("x"), Expr.Num(2.0)), Expr.Num(2.0))
            }
            is Expr.Neg -> Expr.Neg(integrateAst(expr.inner, steps))
            is Expr.Add -> {
                steps += "Linearity: ∫(u + v)dx = ∫u dx + ∫v dx"
                Expr.Add(integrateAst(expr.left, steps), integrateAst(expr.right, steps))
            }
            is Expr.Sub -> {
                steps += "Linearity: ∫(u - v)dx = ∫u dx - ∫v dx"
                Expr.Sub(integrateAst(expr.left, steps), integrateAst(expr.right, steps))
            }
            is Expr.Mul -> {
                val lConst = !containsVar(expr.left, "x")
                val rConst = !containsVar(expr.right, "x")
                when {
                    lConst -> {
                        steps += "Constant factor: ∫ c·f(x) dx = c·∫ f(x) dx"
                        Expr.Mul(expr.left, integrateAst(expr.right, steps))
                    }
                    rConst -> {
                        steps += "Constant factor: ∫ f(x)·c dx = c·∫ f(x) dx"
                        Expr.Mul(expr.right, integrateAst(expr.left, steps))
                    }
                    else -> throw UnsupportedOperationException("Product integral requires integration by parts/substitution (use Definite Numerical Integral).")
                }
            }
            is Expr.Div -> {
                if (!containsVar(expr.right, "x")) {
                    Expr.Div(integrateAst(expr.left, steps), expr.right)
                } else if (!containsVar(expr.left, "x") && expr.right is Expr.Var) {
                    steps += "Reciprocal rule: ∫ (c/x) dx = c·ln|x|"
                    Expr.Mul(expr.left, Expr.Call("ln", listOf(Expr.Call("abs", listOf(Expr.Var("x"))))))
                } else if (!containsVar(expr.left, "x") && expr.right is Expr.Pow && expr.right.base is Expr.Var && !containsVar(expr.right.exp, "x")) {
                    val nVal = ExpressionParser.evaluateReal(expr.right.exp, 0.0)
                    val newExp = -nVal
                    return integrateAst(Expr.Mul(expr.left, Expr.Pow(Expr.Var("x"), Expr.Num(newExp))), steps)
                } else {
                    throw UnsupportedOperationException("Quotient integral not in elementary symbolic table (use Definite Numerical Integral).")
                }
            }
            is Expr.Pow -> {
                if (expr.base is Expr.Var && !containsVar(expr.exp, "x")) {
                    val n = ExpressionParser.evaluateReal(expr.exp, 0.0)
                    if (abs(n + 1.0) < 1e-12) {
                        steps += "∫ x⁻¹ dx = ln|x|"
                        Expr.Call("ln", listOf(Expr.Call("abs", listOf(Expr.Var("x")))))
                    } else {
                        steps += "Power rule: ∫ x^n dx = x^(n+1)/(n+1)"
                        val np1 = Expr.Num(n + 1.0)
                        Expr.Div(Expr.Pow(Expr.Var("x"), np1), np1)
                    }
                } else if (!containsVar(expr.base, "x") && expr.exp is Expr.Var) {
                    steps += "Exponential rule: ∫ a^x dx = a^x / ln(a)"
                    Expr.Div(expr, Expr.Call("ln", listOf(expr.base)))
                } else {
                    throw UnsupportedOperationException("Composite power integral not in elementary symbolic table.")
                }
            }
            is Expr.Call -> {
                if (expr.args.size != 1 || expr.args[0] !is Expr.Var) {
                    throw UnsupportedOperationException("Symbolic antiderivative for composite '${expr.name}(...)' not supported; use numerical integration.")
                }
                val x = Expr.Var("x")
                when (expr.name.lowercase()) {
                    "sin" -> {
                        steps += "∫ sin(x) dx = -cos(x)"
                        Expr.Neg(Expr.Call("cos", listOf(x)))
                    }
                    "cos" -> {
                        steps += "∫ cos(x) dx = sin(x)"
                        Expr.Call("sin", listOf(x))
                    }
                    "exp" -> {
                        steps += "∫ exp(x) dx = exp(x)"
                        Expr.Call("exp", listOf(x))
                    }
                    "sinh" -> {
                        steps += "∫ sinh(x) dx = cosh(x)"
                        Expr.Call("cosh", listOf(x))
                    }
                    "cosh" -> {
                        steps += "∫ cosh(x) dx = sinh(x)"
                        Expr.Call("sinh", listOf(x))
                    }
                    "sqrt" -> {
                        steps += "∫ √x dx = (2/3)·x^(3/2)"
                        Expr.Mul(Expr.Div(Expr.Num(2.0), Expr.Num(3.0)), Expr.Pow(x, Expr.Num(1.5)))
                    }
                    "ln" -> {
                        steps += "Integration by parts: ∫ ln(x) dx = x·ln(x) - x"
                        Expr.Sub(Expr.Mul(x, Expr.Call("ln", listOf(x))), x)
                    }
                    else -> throw UnsupportedOperationException("No elementary antiderivative rule registered for '${expr.name}(x)'.")
                }
            }
            else -> throw UnsupportedOperationException("Unsupported expression structure for symbolic integration.")
        }
    }

    // Definite numerical integration using Adaptive Simpson's Rule
    fun integrateDefinite(
        expressionText: String,
        a: Double,
        b: Double,
        tolerance: Double = 1e-7
    ): DefiniteIntegralResult {
        if (!a.isFinite() || !b.isFinite()) throw IllegalArgumentException("Bounds must be finite numbers")
        if (abs(a - b) < 1e-14) {
            return DefiniteIntegralResult(0.0, 0.0, 1, "Adaptive Simpson's Rule")
        }
        val sign = if (b < a) -1.0 else 1.0
        val lo = minOf(a, b)
        val hi = maxOf(a, b)
        val expr = ExpressionParser.parse(expressionText)
        var evalCount = 0
        var discontinuityDetected = false

        fun f(x: Double): Double {
            evalCount++
            return try {
                val y = ExpressionParser.evaluateReal(expr, x, AngleUnit.RAD)
                if (!y.isFinite()) {
                    discontinuityDetected = true
                    0.0
                } else y
            } catch (_: Exception) {
                discontinuityDetected = true
                0.0
            }
        }

        val fa = f(lo)
        val fb = f(hi)
        val mid = 0.5 * (lo + hi)
        val fm = f(mid)
        val whole = (hi - lo) / 6.0 * (fa + 4.0 * fm + fb)
        var totalErr = 0.0
        val maxEvals = 25000

        fun adaptive(x0: Double, x2: Double, f0: Double, f1: Double, f2: Double, wholeVal: Double, tol: Double, depth: Int): Double {
            if (evalCount >= maxEvals || depth <= 0) {
                totalErr += tol
                return wholeVal
            }
            val x1 = 0.5 * (x0 + x2)
            val leftMid = 0.5 * (x0 + x1)
            val rightMid = 0.5 * (x1 + x2)
            val fLeft = f(leftMid)
            val fRight = f(rightMid)
            val h = (x2 - x0)
            val sLeft = (h / 12.0) * (f0 + 4.0 * fLeft + f1)
            val sRight = (h / 12.0) * (f1 + 4.0 * fRight + f2)
            val s2 = sLeft + sRight
            val diff = abs(s2 - wholeVal)
            if (diff <= 15.0 * tol) {
                totalErr += diff / 15.0
                return s2 + (s2 - wholeVal) / 15.0
            }
            return adaptive(x0, x1, f0, fLeft, f1, sLeft, tol / 2.0, depth - 1) +
                adaptive(x1, x2, f1, fRight, f2, sRight, tol / 2.0, depth - 1)
        }

        val value = sign * adaptive(lo, hi, fa, fm, fb, whole, tolerance, 16)
        val warning = when {
            discontinuityDetected -> "Discontinuity or undefined point encountered in [a, b]; result may be inaccurate."
            evalCount >= maxEvals -> "Evaluation limit reached ($maxEvals); integrand oscillates rapidly or is singular."
            else -> null
        }
        return DefiniteIntegralResult(value, totalErr, evalCount, "Adaptive Simpson's Rule", warning)
    }

    // Numerical Limit Estimator
    fun evaluateLimit(expressionText: String, target: Double): LimitResult {
        val expr = ExpressionParser.parse(expressionText)
        val deltas = listOf(1e-1, 1e-2, 1e-3, 1e-4, 1e-5, 1e-6)

        fun safeEval(x: Double): Double? = try {
            val v = ExpressionParser.evaluateReal(expr, x, AngleUnit.RAD)
            if (v.isFinite()) v else null
        } catch (_: Exception) {
            null
        }

        val leftSamples = deltas.map { h -> LimitSample(target - h, safeEval(target - h)) }
        val rightSamples = deltas.map { h -> LimitSample(target + h, safeEval(target + h)) }

        val leftVals = leftSamples.mapNotNull { it.fx }
        val rightVals = rightSamples.mapNotNull { it.fx }

        fun estimateSide(vals: List<Double>): Pair<Double?, String> {
            if (vals.size < 3) return Pair(null, "Undefined near target")
            val last = vals.last()
            val prev = vals[vals.size - 2]
            val prev2 = vals[vals.size - 3]
            if (abs(last) > 1e6 && abs(last) > abs(prev) * 2.0) {
                return Pair(null, if (last > 0) "Diverges toward +∞" else "Diverges toward -∞")
            }
            if (abs(last - prev) < 1e-3 && abs(prev - prev2) < 1e-2) {
                return Pair(last, "Converging")
            }
            return Pair(null, "Oscillating or slow convergence")
        }

        val (leftEst, leftStatus) = estimateSide(leftVals)
        val (rightEst, rightStatus) = estimateSide(rightVals)

        val direct = safeEval(target)
        val twoSided: Double?
        val status: String
        var warn = false
        if (leftEst != null && rightEst != null) {
            if (abs(leftEst - rightEst) < 1e-3 * maxOf(1.0, abs(leftEst))) {
                twoSided = 0.5 * (leftEst + rightEst)
                status = if (direct == null) {
                    "Estimated limit ≈ ${MathFormatter.formatNumber(twoSided, 7)} (removable/indeterminate at target)"
                } else {
                    "Estimated limit ≈ ${MathFormatter.formatNumber(twoSided, 7)}"
                }
            } else {
                twoSided = null
                status = "Does not exist (jump discontinuity: L⁻ ≠ L⁺)"
                warn = true
            }
        } else {
            twoSided = null
            status = "Two-sided limit inconclusive ($leftStatus / $rightStatus)"
            warn = true
        }

        return LimitResult(
            targetDisplay = MathFormatter.formatNumber(target),
            leftEstimate = leftEst,
            rightEstimate = rightEst,
            twoSidedEstimate = twoSided,
            status = status,
            note = "Numerical convergence analysis across h = 10⁻¹..10⁻⁶ (not a formal symbolic proof).",
            leftSamples = leftSamples,
            rightSamples = rightSamples,
            isWarning = warn
        )
    }

    // Taylor / Maclaurin Series using symbolic derivatives
    fun taylorSeries(
        expressionText: String,
        center: Double = 0.0,
        order: Int = 5,
        testX: Double? = null
    ): TaylorSeriesResult {
        val clampedOrder = order.coerceIn(1, 8)
        var currentExpr = simplify(ExpressionParser.parse(expressionText))
        val terms = mutableListOf<TaylorTerm>()
        var fact = 1.0
        for (k in 0..clampedOrder) {
            if (k > 0) fact *= k.toDouble()
            val derivAtA = ExpressionParser.evaluateReal(currentExpr, center, AngleUnit.RAD)
            val coeff = derivAtA / fact
            terms += TaylorTerm(k, coeff, derivAtA)
            if (k < clampedOrder) {
                currentExpr = simplify(diffAst(currentExpr, "x"))
            }
        }

        val polyParts = mutableListOf<String>()
        for (t in terms) {
            if (abs(t.coeff) < 1e-12) continue
            val sign = if (polyParts.isEmpty()) (if (t.coeff < 0) "-" else "") else (if (t.coeff < 0) " - " else " + ")
            val absC = MathFormatter.formatNumber(abs(t.coeff), 5)
            val varPart = when {
                t.degree == 0 -> ""
                abs(center) < 1e-12 && t.degree == 1 -> "*x"
                abs(center) < 1e-12 -> "*x^${t.degree}"
                t.degree == 1 -> "*(x - ${MathFormatter.formatNumber(center, 4)})"
                else -> "*(x - ${MathFormatter.formatNumber(center, 4)})^${t.degree}"
            }
            polyParts += "$sign$absC$varPart"
        }
        val polyStr = if (polyParts.isEmpty()) "0" else polyParts.joinToString("")

        var exact: Double? = null
        var approx: Double? = null
        var err: Double? = null
        if (testX != null) {
            val dx = testX - center
            var sum = 0.0
            var powDx = 1.0
            for (t in terms) {
                sum += t.coeff * powDx
                powDx *= dx
            }
            approx = sum
            exact = try {
                ExpressionParser.evaluateReal(ExpressionParser.parse(expressionText), testX, AngleUnit.RAD)
            } catch (_: Exception) {
                null
            }
            if (exact != null) err = abs(exact - approx)
        }

        return TaylorSeriesResult(center, clampedOrder, polyStr, terms, testX, exact, approx, err)
    }

    // Root finding methods
    fun findRootBisection(expressionText: String, aStart: Double, bStart: Double, tol: Double = 1e-7, maxIter: Int = 50): RootFindResult {
        val expr = ExpressionParser.parse(expressionText)
        var a = minOf(aStart, bStart)
        var b = maxOf(aStart, bStart)
        var fa = ExpressionParser.evaluateReal(expr, a, AngleUnit.RAD)
        var fb = ExpressionParser.evaluateReal(expr, b, AngleUnit.RAD)
        if (fa * fb > 0) {
            return RootFindResult(
                "Bisection", null, null, false,
                "No sign change on [a, b]: f(a) = ${MathFormatter.formatNumber(fa, 4)}, f(b) = ${MathFormatter.formatNumber(fb, 4)}.",
                emptyList()
            )
        }
        val iters = mutableListOf<RootIteration>()
        var mid = 0.5 * (a + b)
        var fMid = fa
        for (i in 1..maxIter) {
            mid = 0.5 * (a + b)
            fMid = ExpressionParser.evaluateReal(expr, mid, AngleUnit.RAD)
            iters += RootIteration(i, mid, fMid)
            if (abs(fMid) < tol || 0.5 * (b - a) < tol) {
                return RootFindResult("Bisection", mid, abs(fMid), true, "Converged in $i iterations.", iters)
            }
            if (fa * fMid <= 0) {
                b = mid
                fb = fMid
            } else {
                a = mid
                fa = fMid
            }
        }
        return RootFindResult("Bisection", mid, abs(fMid), false, "Max iterations ($maxIter) reached.", iters)
    }

    fun findRootNewton(expressionText: String, x0: Double, tol: Double = 1e-8, maxIter: Int = 35): RootFindResult {
        val expr = ExpressionParser.parse(expressionText)
        val derivExpr = try {
            simplify(diffAst(simplify(expr), "x"))
        } catch (_: Exception) {
            null
        }
        val iters = mutableListOf<RootIteration>()
        var x = x0
        for (i in 1..maxIter) {
            val fx = try {
                ExpressionParser.evaluateReal(expr, x, AngleUnit.RAD)
            } catch (e: Exception) {
                return RootFindResult("Newton-Raphson", null, null, false, "Function undefined at x = ${MathFormatter.formatNumber(x)}: ${e.message}", iters)
            }
            iters += RootIteration(i, x, fx)
            if (abs(fx) < tol) {
                return RootFindResult("Newton-Raphson", x, abs(fx), true, "Converged in $i iterations.", iters)
            }
            val dfx = if (derivExpr != null) {
                ExpressionParser.evaluateReal(derivExpr, x, AngleUnit.RAD)
            } else {
                numericalDerivative(expressionText, x)
            }
            if (abs(dfx) < 1e-13 || !dfx.isFinite()) {
                return RootFindResult("Newton-Raphson", x, abs(fx), false, "Derivative near zero at x = ${MathFormatter.formatNumber(x)}; method stalled.", iters)
            }
            val nextX = x - fx / dfx
            if (!nextX.isFinite() || abs(nextX) > 1e12) {
                return RootFindResult("Newton-Raphson", null, null, false, "Iteration diverged.", iters)
            }
            if (abs(nextX - x) < tol && abs(fx) < 1e-4) {
                return RootFindResult("Newton-Raphson", nextX, abs(fx), true, "Converged in $i iterations.", iters)
            }
            x = nextX
        }
        return RootFindResult("Newton-Raphson", x, iters.lastOrNull()?.fx?.let { abs(it) }, false, "Did not converge within $maxIter iterations.", iters)
    }

    fun findRootSecant(expressionText: String, x0: Double, x1: Double, tol: Double = 1e-8, maxIter: Int = 40): RootFindResult {
        val expr = ExpressionParser.parse(expressionText)
        val iters = mutableListOf<RootIteration>()
        var a = x0
        var b = x1
        var fa = ExpressionParser.evaluateReal(expr, a, AngleUnit.RAD)
        var fb = ExpressionParser.evaluateReal(expr, b, AngleUnit.RAD)
        iters += RootIteration(0, a, fa)
        for (i in 1..maxIter) {
            iters += RootIteration(i, b, fb)
            if (abs(fb) < tol) {
                return RootFindResult("Secant", b, abs(fb), true, "Converged in $i iterations.", iters)
            }
            val denom = fb - fa
            if (abs(denom) < 1e-14) {
                return RootFindResult("Secant", b, abs(fb), false, "Secant slope near zero; stalled.", iters)
            }
            val next = b - fb * (b - a) / denom
            if (!next.isFinite()) return RootFindResult("Secant", null, null, false, "Diverged.", iters)
            a = b
            fa = fb
            b = next
            fb = ExpressionParser.evaluateReal(expr, b, AngleUnit.RAD)
            if (abs(b - a) < tol && abs(fb) < 1e-5) {
                return RootFindResult("Secant", b, abs(fb), true, "Converged in $i iterations.", iters)
            }
        }
        return RootFindResult("Secant", b, abs(fb), false, "Max iterations ($maxIter) reached.", iters)
    }
}
