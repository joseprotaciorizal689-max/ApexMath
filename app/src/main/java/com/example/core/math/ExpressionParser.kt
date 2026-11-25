package com.example.core.math

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.acosh
import kotlin.math.asin
import kotlin.math.asinh
import kotlin.math.atan
import kotlin.math.atan2
import kotlin.math.atanh
import kotlin.math.cbrt
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.cosh
import kotlin.math.exp
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.log2
import kotlin.math.pow
import kotlin.math.round
import kotlin.math.sign
import kotlin.math.sin
import kotlin.math.sinh
import kotlin.math.sqrt
import kotlin.math.tan
import kotlin.math.tanh

sealed class Expr {
    data class Num(val value: Double) : Expr()
    data class ConstSymbol(val name: String, val value: Double) : Expr()
    data class Var(val name: String) : Expr()
    data class Neg(val inner: Expr) : Expr()
    data class Factorial(val inner: Expr) : Expr()
    data class Add(val left: Expr, val right: Expr) : Expr()
    data class Sub(val left: Expr, val right: Expr) : Expr()
    data class Mul(val left: Expr, val right: Expr) : Expr()
    data class Div(val left: Expr, val right: Expr) : Expr()
    data class Mod(val left: Expr, val right: Expr) : Expr()
    data class Pow(val base: Expr, val exp: Expr) : Expr()
    data class Call(val name: String, val args: List<Expr>) : Expr()
}

private enum class TokType {
    NUMBER, IDENT, PLUS, MINUS, MUL, DIV, POW, MOD, FACT, LPAREN, RPAREN, COMMA, EOF
}

private data class Tok(val type: TokType, val text: String, val pos: Int)

class MathParseException(message: String, val pos: Int? = null) : RuntimeException(message)

object ExpressionParser {

    private val knownConstants = mapOf(
        "pi" to PI,
        "π" to PI,
        "e" to kotlin.math.E,
        "phi" to (1.0 + sqrt(5.0)) / 2.0,
        "φ" to (1.0 + sqrt(5.0)) / 2.0,
        "tau" to 2.0 * PI,
        "τ" to 2.0 * PI
    )

    private val knownFunctions = setOf(
        "sin", "cos", "tan", "csc", "sec", "cot",
        "asin", "acos", "atan", "atan2", "arcsin", "arccos", "arctan",
        "sinh", "cosh", "tanh", "asinh", "acosh", "atanh",
        "ln", "log", "log10", "log2", "logb", "exp",
        "sqrt", "cbrt", "root", "abs", "floor", "ceil", "round", "sign",
        "fact", "ncr", "npr", "gcd", "lcm", "re", "im", "conj", "arg", "mag"
    )

    fun parse(input: String): Expr {
        val normalized = normalizeInput(input)
        if (normalized.isBlank()) throw MathParseException("Expression is empty", 0)
        val rawTokens = tokenize(normalized)
        val tokens = insertImplicitMultiplication(rawTokens)
        val parser = RecursiveParser(tokens)
        val expr = parser.parseExpression()
        if (parser.peek().type != TokType.EOF) {
            throw MathParseException("Unexpected token '${parser.peek().text}'", parser.peek().pos)
        }
        return expr
    }

    fun evaluate(
        input: String,
        angleUnit: AngleUnit = AngleUnit.DEG,
        variables: Map<String, Complex> = emptyMap(),
        allowComplex: Boolean = true,
        precision: Int = 10
    ): MathEvalResult {
        return try {
            val expr = parse(input)
            val result = evalAst(expr, angleUnit, variables, allowComplex)
            val fraction = if (result.isReal) Fraction.fromDouble(result.re) else null
            val formatted = result.format(precision)
            MathEvalResult.Success(result, fraction, formatted)
        } catch (e: MathParseException) {
            MathEvalResult.Error(e.message ?: "Syntax error", e.pos)
        } catch (e: ArithmeticException) {
            MathEvalResult.Error(e.message ?: "Math domain error")
        } catch (e: Exception) {
            MathEvalResult.Error(e.message ?: "Invalid expression")
        }
    }

    fun evaluateReal(
        expr: Expr,
        x: Double,
        angleUnit: AngleUnit = AngleUnit.RAD,
        extraVars: Map<String, Double> = emptyMap()
    ): Double {
        val vars = HashMap<String, Complex>(extraVars.size + 1)
        vars["x"] = Complex(x, 0.0)
        for ((k, v) in extraVars) {
            vars[k.lowercase()] = Complex(v, 0.0)
        }
        val res = evalAst(expr, angleUnit, vars, allowComplex = false)
        if (!res.isReal || !res.re.isFinite()) throw ArithmeticException("Result is undefined or non-real")
        return res.re
    }

    fun evalAst(
        expr: Expr,
        angleUnit: AngleUnit,
        variables: Map<String, Complex>,
        allowComplex: Boolean
    ): Complex {
        return when (expr) {
            is Expr.Num -> Complex(expr.value, 0.0)
            is Expr.ConstSymbol -> Complex(expr.value, 0.0)
            is Expr.Var -> {
                val lower = expr.name.lowercase()
                if (lower == "i") {
                    if (!allowComplex) throw ArithmeticException("Imaginary unit 'i' is not real")
                    Complex.I
                } else {
                    variables[lower] ?: variables[expr.name]
                    ?: throw MathParseException("Unknown variable '${expr.name}'")
                }
            }
            is Expr.Neg -> -evalAst(expr.inner, angleUnit, variables, allowComplex)
            is Expr.Factorial -> {
                val v = evalAst(expr.inner, angleUnit, variables, allowComplex)
                if (!v.isReal) throw ArithmeticException("Factorial requires a real integer")
                Complex(computeFactorial(v.re), 0.0)
            }
            is Expr.Add -> evalAst(expr.left, angleUnit, variables, allowComplex) +
                evalAst(expr.right, angleUnit, variables, allowComplex)
            is Expr.Sub -> evalAst(expr.left, angleUnit, variables, allowComplex) -
                evalAst(expr.right, angleUnit, variables, allowComplex)
            is Expr.Mul -> evalAst(expr.left, angleUnit, variables, allowComplex) *
                evalAst(expr.right, angleUnit, variables, allowComplex)
            is Expr.Div -> evalAst(expr.left, angleUnit, variables, allowComplex) /
                evalAst(expr.right, angleUnit, variables, allowComplex)
            is Expr.Mod -> {
                val a = evalAst(expr.left, angleUnit, variables, allowComplex)
                val b = evalAst(expr.right, angleUnit, variables, allowComplex)
                if (!a.isReal || !b.isReal) throw ArithmeticException("Modulo requires real operands")
                if (abs(b.re) < 1e-15) throw ArithmeticException("Modulo by zero")
                Complex(a.re % b.re, 0.0)
            }
            is Expr.Pow -> {
                val base = evalAst(expr.base, angleUnit, variables, allowComplex)
                val exp = evalAst(expr.exp, angleUnit, variables, allowComplex)
                if (base.isReal && exp.isReal) {
                    val b = base.re
                    val p = exp.re
                    if (b == 0.0 && p == 0.0) throw ArithmeticException("Indeterminate form 0^0")
                    if (b == 0.0 && p < 0.0) throw ArithmeticException("Division by zero: 0 raised to negative power")
                    if (b < 0.0 && abs(p - round(p)) > 1e-10) {
                        // Check odd integer root like (-8)^(1/3)
                        val frac = Fraction.fromDouble(p, 1000, 1e-9)
                        if (frac != null && frac.den % 2L == 1L) {
                            val rootVal = (-b).pow(1.0 / frac.den)
                            val signed = -rootVal
                            return Complex(signed.pow(frac.num.toDouble()), 0.0)
                        }
                        if (!allowComplex) throw ArithmeticException("Non-real fractional power of negative number")
                        return base.pow(exp)
                    }
                    val res = b.pow(p)
                    if (!res.isFinite()) throw ArithmeticException("Numeric overflow in exponentiation")
                    Complex(res, 0.0)
                } else {
                    if (!allowComplex) throw ArithmeticException("Complex exponentiation not supported in real mode")
                    base.pow(exp)
                }
            }
            is Expr.Call -> evalFunction(expr.name.lowercase(), expr.args.map {
                evalAst(it, angleUnit, variables, allowComplex)
            }, angleUnit, allowComplex)
        }
    }

    private fun evalFunction(
        name: String,
        args: List<Complex>,
        angleUnit: AngleUnit,
        allowComplex: Boolean
    ): Complex {
        fun needArgs(count: Int) {
            if (args.size != count) throw MathParseException("$name() requires $count argument(s)")
        }

        fun toRad(x: Double) = if (angleUnit == AngleUnit.DEG) Math.toRadians(x) else x
        fun fromRad(x: Double) = if (angleUnit == AngleUnit.DEG) Math.toDegrees(x) else x

        return when (name) {
            "sin" -> {
                needArgs(1)
                val z = args[0]
                if (z.isReal) Complex(sin(toRad(z.re)), 0.0)
                else {
                    val r = toRad(z.re)
                    val im = z.im
                    Complex(sin(r) * cosh(im), cos(r) * sinh(im))
                }
            }
            "cos" -> {
                needArgs(1)
                val z = args[0]
                if (z.isReal) Complex(cos(toRad(z.re)), 0.0)
                else {
                    val r = toRad(z.re)
                    val im = z.im
                    Complex(cos(r) * cosh(im), -sin(r) * sinh(im))
                }
            }
            "tan" -> {
                needArgs(1)
                val z = args[0]
                if (z.isReal) {
                    val rad = toRad(z.re)
                    val c = cos(rad)
                    if (abs(c) < 1e-12) throw ArithmeticException("Undefined: tan asymptote")
                    Complex(sin(rad) / c, 0.0)
                } else {
                    evalFunction("sin", args, angleUnit, allowComplex) /
                        evalFunction("cos", args, angleUnit, allowComplex)
                }
            }
            "csc" -> {
                needArgs(1)
                val s = evalFunction("sin", args, angleUnit, allowComplex)
                Complex.ONE / s
            }
            "sec" -> {
                needArgs(1)
                val c = evalFunction("cos", args, angleUnit, allowComplex)
                Complex.ONE / c
            }
            "cot" -> {
                needArgs(1)
                val s = evalFunction("sin", args, angleUnit, allowComplex)
                val c = evalFunction("cos", args, angleUnit, allowComplex)
                c / s
            }
            "asin", "arcsin" -> {
                needArgs(1)
                val z = args[0]
                if (!z.isReal) throw ArithmeticException("asin requires real argument in [-1, 1]")
                if (z.re < -1.0 || z.re > 1.0) throw ArithmeticException("Domain error: asin argument must be in [-1, 1]")
                Complex(fromRad(asin(z.re)), 0.0)
            }
            "acos", "arccos" -> {
                needArgs(1)
                val z = args[0]
                if (!z.isReal) throw ArithmeticException("acos requires real argument in [-1, 1]")
                if (z.re < -1.0 || z.re > 1.0) throw ArithmeticException("Domain error: acos argument must be in [-1, 1]")
                Complex(fromRad(acos(z.re)), 0.0)
            }
            "atan", "arctan" -> {
                needArgs(1)
                val z = args[0]
                if (!z.isReal) throw ArithmeticException("atan requires a real argument")
                Complex(fromRad(atan(z.re)), 0.0)
            }
            "atan2" -> {
                needArgs(2)
                val y = args[0]
                val x = args[1]
                if (!y.isReal || !x.isReal) throw ArithmeticException("atan2 requires real arguments")
                if (x.re == 0.0 && y.re == 0.0) throw ArithmeticException("atan2(0, 0) is undefined")
                Complex(fromRad(atan2(y.re, x.re)), 0.0)
            }
            "sinh" -> {
                needArgs(1)
                val z = args[0]
                if (!z.isReal) throw ArithmeticException("sinh complex input not supported")
                val v = sinh(z.re)
                if (!v.isFinite()) throw ArithmeticException("Overflow in sinh")
                Complex(v, 0.0)
            }
            "cosh" -> {
                needArgs(1)
                val z = args[0]
                if (!z.isReal) throw ArithmeticException("cosh complex input not supported")
                val v = cosh(z.re)
                if (!v.isFinite()) throw ArithmeticException("Overflow in cosh")
                Complex(v, 0.0)
            }
            "tanh" -> {
                needArgs(1)
                val z = args[0]
                if (!z.isReal) throw ArithmeticException("tanh complex input not supported")
                Complex(tanh(z.re), 0.0)
            }
            "asinh" -> {
                needArgs(1)
                val z = args[0]
                if (!z.isReal) throw ArithmeticException("asinh requires real input")
                Complex(asinh(z.re), 0.0)
            }
            "acosh" -> {
                needArgs(1)
                val z = args[0]
                if (!z.isReal || z.re < 1.0) throw ArithmeticException("Domain error: acosh requires x ≥ 1")
                Complex(acosh(z.re), 0.0)
            }
            "atanh" -> {
                needArgs(1)
                val z = args[0]
                if (!z.isReal || z.re <= -1.0 || z.re >= 1.0) throw ArithmeticException("Domain error: atanh requires -1 < x < 1")
                Complex(atanh(z.re), 0.0)
            }
            "ln" -> {
                needArgs(1)
                val z = args[0]
                if (z.isReal) {
                    if (z.re <= 0.0) {
                        if (allowComplex && z.re < 0.0) {
                            return Complex(ln(-z.re), PI)
                        }
                        throw ArithmeticException("Domain error: ln(x) requires x > 0")
                    }
                    Complex(ln(z.re), 0.0)
                } else {
                    Complex(ln(z.magnitude), z.argumentRad())
                }
            }
            "log", "log10" -> {
                needArgs(1)
                val z = args[0]
                if (!z.isReal || z.re <= 0.0) throw ArithmeticException("Domain error: log(x) requires real x > 0")
                Complex(log10(z.re), 0.0)
            }
            "log2" -> {
                needArgs(1)
                val z = args[0]
                if (!z.isReal || z.re <= 0.0) throw ArithmeticException("Domain error: log2(x) requires real x > 0")
                Complex(log2(z.re), 0.0)
            }
            "logb" -> {
                needArgs(2)
                val x = args[0]
                val base = args[1]
                if (!x.isReal || !base.isReal || x.re <= 0.0 || base.re <= 0.0 || abs(base.re - 1.0) < 1e-12) {
                    throw ArithmeticException("Domain error: logb(x, b) requires x > 0, b > 0, b ≠ 1")
                }
                Complex(ln(x.re) / ln(base.re), 0.0)
            }
            "exp" -> {
                needArgs(1)
                val z = args[0]
                if (z.isReal) {
                    val v = exp(z.re)
                    if (!v.isFinite()) throw ArithmeticException("Overflow in exp")
                    Complex(v, 0.0)
                } else {
                    val mag = exp(z.re)
                    if (!mag.isFinite()) throw ArithmeticException("Overflow in complex exp")
                    Complex(mag * cos(z.im), mag * sin(z.im))
                }
            }
            "sqrt" -> {
                needArgs(1)
                val z = args[0]
                if (z.isReal) {
                    if (z.re < 0.0) {
                        if (!allowComplex) throw ArithmeticException("Domain error: sqrt of negative number")
                        Complex(0.0, sqrt(-z.re))
                    } else {
                        Complex(sqrt(z.re), 0.0)
                    }
                } else {
                    val r = sqrt(z.magnitude)
                    val halfTheta = z.argumentRad() / 2.0
                    Complex(r * cos(halfTheta), r * sin(halfTheta))
                }
            }
            "cbrt" -> {
                needArgs(1)
                val z = args[0]
                if (!z.isReal) throw ArithmeticException("cbrt requires real argument")
                Complex(cbrt(z.re), 0.0)
            }
            "root" -> {
                needArgs(2)
                val n = args[0]
                val x = args[1]
                if (!n.isReal || !x.isReal || n.re == 0.0) {
                    throw ArithmeticException("root(n, x) requires real n ≠ 0 and real x")
                }
                val nInt = round(n.re)
                if (x.re < 0.0) {
                    if (abs(n.re - nInt) < 1e-10 && (nInt.toLong() % 2L != 0L)) {
                        Complex(-(-x.re).pow(1.0 / n.re), 0.0)
                    } else {
                        throw ArithmeticException("Even root of negative number is non-real")
                    }
                } else {
                    Complex(x.re.pow(1.0 / n.re), 0.0)
                }
            }
            "abs", "mag" -> {
                needArgs(1)
                Complex(args[0].magnitude, 0.0)
            }
            "floor" -> {
                needArgs(1)
                if (!args[0].isReal) throw ArithmeticException("floor requires real input")
                Complex(floor(args[0].re), 0.0)
            }
            "ceil" -> {
                needArgs(1)
                if (!args[0].isReal) throw ArithmeticException("ceil requires real input")
                Complex(ceil(args[0].re), 0.0)
            }
            "round" -> {
                needArgs(1)
                if (!args[0].isReal) throw ArithmeticException("round requires real input")
                Complex(round(args[0].re), 0.0)
            }
            "sign" -> {
                needArgs(1)
                if (!args[0].isReal) throw ArithmeticException("sign requires real input")
                Complex(sign(args[0].re), 0.0)
            }
            "fact" -> {
                needArgs(1)
                if (!args[0].isReal) throw ArithmeticException("Factorial requires real integer")
                Complex(computeFactorial(args[0].re), 0.0)
            }
            "ncr" -> {
                needArgs(2)
                if (!args[0].isReal || !args[1].isReal) throw ArithmeticException("nCr requires integer arguments")
                Complex(computeCombination(args[0].re, args[1].re).toDouble(), 0.0)
            }
            "npr" -> {
                needArgs(2)
                if (!args[0].isReal || !args[1].isReal) throw ArithmeticException("nPr requires integer arguments")
                Complex(computePermutation(args[0].re, args[1].re).toDouble(), 0.0)
            }
            "gcd" -> {
                needArgs(2)
                if (!args[0].isReal || !args[1].isReal) throw ArithmeticException("gcd requires integers")
                val a = requireWholeLong(args[0].re, "gcd")
                val b = requireWholeLong(args[1].re, "gcd")
                Complex(Fraction.gcd(a, b).toDouble(), 0.0)
            }
            "lcm" -> {
                needArgs(2)
                if (!args[0].isReal || !args[1].isReal) throw ArithmeticException("lcm requires integers")
                val a = requireWholeLong(args[0].re, "lcm")
                val b = requireWholeLong(args[1].re, "lcm")
                Complex(Fraction.lcm(a, b).toDouble(), 0.0)
            }
            "re" -> {
                needArgs(1)
                Complex(args[0].re, 0.0)
            }
            "im" -> {
                needArgs(1)
                Complex(args[0].im, 0.0)
            }
            "conj" -> {
                needArgs(1)
                args[0].conjugate()
            }
            "arg" -> {
                needArgs(1)
                Complex(args[0].argument(angleUnit), 0.0)
            }
            else -> throw MathParseException("Unknown function '$name'")
        }
    }

    fun computeFactorial(value: Double): Double {
        if (value < 0 || abs(value - round(value)) > 1e-9) {
            throw ArithmeticException("Factorial is only defined for non-negative integers")
        }
        val n = round(value).toInt()
        if (n > 170) throw ArithmeticException("Factorial overflow (n > 170 exceeds IEEE 754 Double)")
        var acc = 1.0
        for (i in 2..n) acc *= i.toDouble()
        return acc
    }

    fun computeCombination(nVal: Double, rVal: Double): Long {
        val n = requireWholeLong(nVal, "nCr")
        val r = requireWholeLong(rVal, "nCr")
        if (n < 0 || r < 0 || r > n) throw ArithmeticException("nCr requires 0 ≤ r ≤ n")
        val k = minOf(r, n - r)
        var res = 1L
        for (i in 1L..k) {
            // Safe overflow check using BigInteger-free check or BigInteger
            val num = res * (n - k + i)
            if (res != 0L && num / res != (n - k + i)) {
                throw ArithmeticException("nCr integer overflow")
            }
            res = num / i
        }
        return res
    }

    fun computePermutation(nVal: Double, rVal: Double): Long {
        val n = requireWholeLong(nVal, "nPr")
        val r = requireWholeLong(rVal, "nPr")
        if (n < 0 || r < 0 || r > n) throw ArithmeticException("nPr requires 0 ≤ r ≤ n")
        var res = 1L
        for (i in 0L until r) {
            val factor = n - i
            if (res > Long.MAX_VALUE / factor.coerceAtLeast(1L)) {
                throw ArithmeticException("nPr integer overflow")
            }
            res *= factor
        }
        return res
    }

    private fun requireWholeLong(v: Double, fn: String): Long {
        if (!v.isFinite() || abs(v - round(v)) > 1e-9) {
            throw ArithmeticException("$fn requires integer arguments")
        }
        if (abs(v) > 9e15) throw ArithmeticException("$fn argument out of range")
        return round(v).toLong()
    }

    private fun normalizeInput(input: String): String {
        return input
            .replace("×", "*")
            .replace("·", "*")
            .replace("÷", "/")
            .replace("−", "-")
            .replace("–", "-")
            .replace("√", "sqrt")
            .replace("²", "^2")
            .replace("³", "^3")
            .trim()
    }

    private fun tokenize(s: String): List<Tok> {
        val tokens = mutableListOf<Tok>()
        var i = 0
        while (i < s.length) {
            val ch = s[i]
            when {
                ch.isWhitespace() -> i++
                ch.isDigit() || ch == '.' -> {
                    val start = i
                    var sawDot = ch == '.'
                    i++
                    while (i < s.length) {
                        val c = s[i]
                        if (c.isDigit()) {
                            i++
                        } else if (c == '.' && !sawDot) {
                            sawDot = true
                            i++
                        } else if ((c == 'e' || c == 'E') && i + 1 < s.length) {
                            val next = s[i + 1]
                            if (next.isDigit() || ((next == '+' || next == '-') && i + 2 < s.length && s[i + 2].isDigit())) {
                                i += if (next == '+' || next == '-') 2 else 1
                                while (i < s.length && s[i].isDigit()) i++
                                break
                            } else break
                        } else break
                    }
                    val numText = s.substring(start, i)
                    if (numText == ".") throw MathParseException("Invalid standalone '.'", start)
                    tokens += Tok(TokType.NUMBER, numText, start)
                }
                ch.isLetter() || ch == 'π' || ch == 'φ' || ch == 'τ' || ch == '_' -> {
                    val start = i
                    i++
                    while (i < s.length && (s[i].isLetterOrDigit() || s[i] == '_')) {
                        i++
                    }
                    tokens += Tok(TokType.IDENT, s.substring(start, i), start)
                }
                ch == '+' -> { tokens += Tok(TokType.PLUS, "+", i); i++ }
                ch == '-' -> { tokens += Tok(TokType.MINUS, "-", i); i++ }
                ch == '*' -> { tokens += Tok(TokType.MUL, "*", i); i++ }
                ch == '/' -> { tokens += Tok(TokType.DIV, "/", i); i++ }
                ch == '^' -> { tokens += Tok(TokType.POW, "^", i); i++ }
                ch == '%' -> { tokens += Tok(TokType.MOD, "%", i); i++ }
                ch == '!' -> { tokens += Tok(TokType.FACT, "!", i); i++ }
                ch == '(' -> { tokens += Tok(TokType.LPAREN, "(", i); i++ }
                ch == ')' -> { tokens += Tok(TokType.RPAREN, ")", i); i++ }
                ch == ',' -> { tokens += Tok(TokType.COMMA, ",", i); i++ }
                else -> throw MathParseException("Unsupported character '$ch'", i)
            }
        }
        tokens += Tok(TokType.EOF, "", s.length)
        return tokens
    }

    private fun insertImplicitMultiplication(tokens: List<Tok>): List<Tok> {
        if (tokens.size <= 2) return tokens
        val out = mutableListOf<Tok>()
        for (idx in 0 until tokens.size - 1) {
            val cur = tokens[idx]
            val next = tokens[idx + 1]
            out += cur
            val curEndsValue = cur.type == TokType.NUMBER ||
                cur.type == TokType.RPAREN ||
                cur.type == TokType.FACT ||
                (cur.type == TokType.IDENT && !(next.type == TokType.LPAREN && isKnownFunction(cur.text)))
            val nextStartsValue = next.type == TokType.NUMBER ||
                next.type == TokType.LPAREN ||
                next.type == TokType.IDENT
            if (curEndsValue && nextStartsValue) {
                // Avoid inserting * between IDENT and LPAREN if IDENT is a function call
                if (!(cur.type == TokType.IDENT && next.type == TokType.LPAREN && isKnownFunction(cur.text))) {
                    out += Tok(TokType.MUL, "*", next.pos)
                }
            }
        }
        out += tokens.last()
        return out
    }

    private fun isKnownFunction(name: String): Boolean = name.lowercase() in knownFunctions

    private class RecursiveParser(private val tokens: List<Tok>) {
        private var pos = 0

        fun peek(offset: Int = 0): Tok = tokens.getOrElse(pos + offset) { tokens.last() }
        private fun consume(): Tok = tokens[pos++]

        fun parseExpression(): Expr = parseAddSub()

        private fun parseAddSub(): Expr {
            var left = parseMulDiv()
            while (peek().type == TokType.PLUS || peek().type == TokType.MINUS) {
                val op = consume()
                val right = parseMulDiv()
                left = if (op.type == TokType.PLUS) Expr.Add(left, right) else Expr.Sub(left, right)
            }
            return left
        }

        private fun parseMulDiv(): Expr {
            var left = parseUnary()
            while (peek().type == TokType.MUL || peek().type == TokType.DIV || peek().type == TokType.MOD) {
                val op = consume()
                val right = parseUnary()
                left = when (op.type) {
                    TokType.MUL -> Expr.Mul(left, right)
                    TokType.DIV -> Expr.Div(left, right)
                    else -> Expr.Mod(left, right)
                }
            }
            return left
        }

        private fun parseUnary(): Expr {
            return when (peek().type) {
                TokType.PLUS -> {
                    consume()
                    parseUnary()
                }
                TokType.MINUS -> {
                    consume()
                    Expr.Neg(parseUnary())
                }
                else -> parsePower()
            }
        }

        private fun parsePower(): Expr {
            val base = parsePostfix()
            return if (peek().type == TokType.POW) {
                consume()
                val exp = parseUnary() // right-associative, supports 2^-3
                Expr.Pow(base, exp)
            } else base
        }

        private fun parsePostfix(): Expr {
            var expr = parsePrimary()
            while (peek().type == TokType.FACT) {
                consume()
                expr = Expr.Factorial(expr)
            }
            return expr
        }

        private fun parsePrimary(): Expr {
            val t = peek()
            return when (t.type) {
                TokType.NUMBER -> {
                    consume()
                    val v = t.text.toDoubleOrNull()
                        ?: throw MathParseException("Invalid number '${t.text}'", t.pos)
                    Expr.Num(v)
                }
                TokType.LPAREN -> {
                    consume()
                    val inner = parseExpression()
                    if (peek().type != TokType.RPAREN) {
                        throw MathParseException("Missing closing ')'", peek().pos)
                    }
                    consume()
                    inner
                }
                TokType.IDENT -> {
                    consume()
                    if (peek().type == TokType.LPAREN) {
                        consume()
                        val args = mutableListOf<Expr>()
                        if (peek().type != TokType.RPAREN) {
                            args += parseExpression()
                            while (peek().type == TokType.COMMA) {
                                consume()
                                args += parseExpression()
                            }
                        }
                        if (peek().type != TokType.RPAREN) {
                            throw MathParseException("Missing ')' in ${t.text}(...)", peek().pos)
                        }
                        consume()
                        Expr.Call(t.text, args)
                    } else {
                        val lower = t.text.lowercase()
                        val constVal = knownConstants[lower]
                        if (constVal != null) Expr.ConstSymbol(t.text, constVal)
                        else Expr.Var(t.text)
                    }
                }
                else -> throw MathParseException("Expected value or '(' near '${t.text}'", t.pos)
            }
        }
    }
}
