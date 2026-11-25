package com.example.core.math

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.hypot
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.roundToLong
import kotlin.math.sin
import kotlin.math.sqrt

enum class AngleUnit {
    DEG, RAD
}

data class Complex(val re: Double, val im: Double = 0.0) {
    val isReal: Boolean get() = abs(im) < 1e-12
    val magnitude: Double get() = hypot(re, im)
    fun argumentRad(): Double = atan2(im, re)
    fun argument(unit: AngleUnit): Double {
        val rad = argumentRad()
        return if (unit == AngleUnit.DEG) Math.toDegrees(rad) else rad
    }

    fun conjugate(): Complex = Complex(re, -im)

    operator fun plus(other: Complex) = Complex(re + other.re, im + other.im)
    operator fun minus(other: Complex) = Complex(re - other.re, im - other.im)
    operator fun times(other: Complex) = Complex(
        re * other.re - im * other.im,
        re * other.im + im * other.re
    )

    operator fun div(other: Complex): Complex {
        val denom = other.re * other.re + other.im * other.im
        if (denom < 1e-300) throw ArithmeticException("Division by zero (complex modulus is 0)")
        return Complex(
            (re * other.re + im * other.im) / denom,
            (im * other.re - re * other.im) / denom
        )
    }

    operator fun unaryMinus() = Complex(-re, -im)

    fun pow(exponent: Complex): Complex {
        if (re == 0.0 && im == 0.0) {
            if (exponent.isReal && exponent.re > 0.0) return Complex(0.0, 0.0)
            if (exponent.isReal && exponent.re == 0.0) throw ArithmeticException("Indeterminate form 0^0")
            throw ArithmeticException("Division by zero: 0 raised to non-positive power")
        }
        val r = magnitude
        val theta = argumentRad()
        val logR = ln(r)
        val a = exponent.re
        val b = exponent.im
        val newLogR = a * logR - b * theta
        val newTheta = a * theta + b * logR
        val newR = kotlin.math.exp(newLogR)
        return Complex(newR * cos(newTheta), newR * sin(newTheta))
    }

    fun format(precision: Int = 8): String {
        val rClean = if (abs(re) < 1e-11) 0.0 else re
        val iClean = if (abs(im) < 1e-11) 0.0 else im
        if (iClean == 0.0) return MathFormatter.formatNumber(rClean, precision)
        val imAbsStr = MathFormatter.formatNumber(abs(iClean), precision)
        val imPart = if (imAbsStr == "1") "i" else "${imAbsStr}i"
        if (rClean == 0.0) return if (iClean < 0) "-$imPart" else imPart
        val rStr = MathFormatter.formatNumber(rClean, precision)
        val sign = if (iClean < 0) " - " else " + "
        return "$rStr$sign$imPart"
    }

    fun toPolarString(unit: AngleUnit, precision: Int = 6): String {
        val rStr = MathFormatter.formatNumber(magnitude, precision)
        val angStr = MathFormatter.formatNumber(argument(unit), precision)
        val unitLabel = if (unit == AngleUnit.DEG) "°" else " rad"
        return "$rStr ∠ $angStr$unitLabel"
    }

    companion object {
        val ZERO = Complex(0.0, 0.0)
        val ONE = Complex(1.0, 0.0)
        val I = Complex(0.0, 1.0)

        fun fromPolar(r: Double, angle: Double, unit: AngleUnit): Complex {
            val rad = if (unit == AngleUnit.DEG) Math.toRadians(angle) else angle
            return Complex(r * cos(rad), r * sin(rad))
        }
    }
}

data class Fraction(val num: Long, val den: Long) {
    init {
        require(den != 0L) { "Denominator cannot be zero" }
    }

    val reduced: Fraction by lazy {
        if (num == 0L) Fraction(0L, 1L)
        else {
            val sign = if (den < 0L) -1L else 1L
            val g = gcd(abs(num), abs(den))
            Fraction((num * sign) / g, abs(den) / g)
        }
    }

    operator fun plus(other: Fraction): Fraction {
        val a = this.reduced
        val b = other.reduced
        val lcmDen = lcm(a.den, b.den)
        val newNum = a.num * (lcmDen / a.den) + b.num * (lcmDen / b.den)
        return Fraction(newNum, lcmDen).reduced
    }

    operator fun minus(other: Fraction): Fraction = plus(Fraction(-other.num, other.den))

    operator fun times(other: Fraction): Fraction {
        val a = this.reduced
        val b = other.reduced
        return Fraction(a.num * b.num, a.den * b.den).reduced
    }

    operator fun div(other: Fraction): Fraction {
        val b = other.reduced
        if (b.num == 0L) throw ArithmeticException("Division by zero in fraction arithmetic")
        return times(Fraction(b.den, b.num))
    }

    fun toDecimal(): Double = num.toDouble() / den.toDouble()

    fun format(): String {
        val r = reduced
        return if (r.den == 1L) "${r.num}" else "${r.num}/${r.den}"
    }

    fun toMixedString(): String {
        val r = reduced
        if (r.den == 1L) return "${r.num}"
        val whole = r.num / r.den
        val rem = abs(r.num % r.den)
        return if (whole == 0L) "${r.num}/${r.den}" else "$whole $rem/${r.den}"
    }

    companion object {
        fun gcd(a: Long, b: Long): Long {
            var x = abs(a)
            var y = abs(b)
            while (y != 0L) {
                val t = x % y
                x = y
                y = t
            }
            return if (x == 0L) 1L else x
        }

        fun lcm(a: Long, b: Long): Long {
            if (a == 0L || b == 0L) return 0L
            return abs(a / gcd(a, b) * b)
        }

        fun fromDouble(value: Double, maxDenominator: Long = 10000L, tolerance: Double = 1e-9): Fraction? {
            if (!value.isFinite() || abs(value) > 1e12) return null
            val sign = if (value < 0) -1L else 1L
            val v = abs(value)
            var h1 = 1L
            var h2 = 0L
            var k1 = 0L
            var k2 = 1L
            var b = v
            var iter = 0
            while (iter < 64) {
                iter++
                val a = floor(b).toLong()
                val nextH = a * h1 + h2
                val nextK = a * k1 + k2
                if (nextK > maxDenominator || nextH > 1_000_000_000L) break
                h2 = h1
                h1 = nextH
                k2 = k1
                k1 = nextK
                val approx = h1.toDouble() / k1.toDouble()
                if (abs(v - approx) <= tolerance) {
                    return Fraction(sign * h1, k1).reduced
                }
                val rem = b - a
                if (rem < 1e-12) break
                b = 1.0 / rem
            }
            val approx = h1.toDouble() / k1.toDouble()
            return if (k1 in 1..maxDenominator && abs(v - approx) <= 1e-6) {
                Fraction(sign * h1, k1).reduced
            } else null
        }
    }
}

sealed class MathEvalResult {
    data class Success(
        val complex: Complex,
        val fraction: Fraction?,
        val formatted: String
    ) : MathEvalResult() {
        val realValue: Double? get() = if (complex.isReal) complex.re else null
    }

    data class Error(val message: String, val position: Int? = null) : MathEvalResult()
}

object MathFormatter {
    fun formatNumber(value: Double, precision: Int = 10): String {
        if (value.isNaN()) return "NaN"
        if (value.isInfinite()) return if (value > 0) "∞" else "-∞"
        val cleaned = if (abs(value) < 1e-13) 0.0 else value
        if (cleaned == 0.0) return "0"
        val roundedInt = cleaned.roundToLong()
        if (abs(cleaned) < 1e14 && abs(cleaned - roundedInt.toDouble()) < 1e-11) {
            return roundedInt.toString()
        }
        val absVal = abs(cleaned)
        if (absVal >= 1e10 || (absVal > 0 && absVal < 1e-5)) {
            val formatted = "%.${minOf(precision, 8)}e".format(java.util.Locale.US, cleaned)
            return trimScientific(formatted)
        }
        val p = precision.coerceIn(1, 12)
        val raw = "%.${p}f".format(java.util.Locale.US, cleaned)
        return raw.trimEnd('0').trimEnd('.')
    }

    private fun trimScientific(s: String): String {
        val parts = s.split("e", "E")
        if (parts.size != 2) return s
        val mantissa = parts[0].trimEnd('0').trimEnd('.')
        val exp = parts[1].toIntOrNull()?.toString() ?: parts[1]
        return "${mantissa}e$exp"
    }
}

object PercentageUtils {
    fun percentOf(percent: Double, base: Double): Double = (percent / 100.0) * base
    fun percentIncrease(base: Double, percent: Double): Double = base * (1.0 + percent / 100.0)
    fun percentDecrease(base: Double, percent: Double): Double = base * (1.0 - percent / 100.0)
    fun percentChange(oldValue: Double, newValue: Double): Double {
        if (abs(oldValue) < 1e-15) throw ArithmeticException("Initial value cannot be zero for percent change")
        return ((newValue - oldValue) / oldValue) * 100.0
    }
    fun reversePercentAfterChange(finalValue: Double, changePercent: Double): Double {
        val factor = 1.0 + changePercent / 100.0
        if (abs(factor) < 1e-15) throw ArithmeticException("Factor (1 + p/100) cannot be zero")
        return finalValue / factor
    }
}

data class ConstantEntry(
    val symbol: String,
    val insertText: String,
    val name: String,
    val value: Double,
    val valueDisplay: String,
    val unit: String,
    val category: String,
    val description: String
)

object ConstantsLibrary {
    val all: List<ConstantEntry> = listOf(
        ConstantEntry("π", "pi", "Pi", PI, "3.14159265358979", "dimensionless", "Math", "Ratio of a circle's circumference to its diameter."),
        ConstantEntry("e", "e", "Euler's Number", kotlin.math.E, "2.71828182845905", "dimensionless", "Math", "Base of the natural logarithm; lim (1 + 1/n)^n."),
        ConstantEntry("φ", "phi", "Golden Ratio", (1.0 + sqrt(5.0)) / 2.0, "1.61803398874989", "dimensionless", "Math", "Positive solution to x² - x - 1 = 0."),
        ConstantEntry("τ", "tau", "Tau", 2.0 * PI, "6.28318530717959", "rad", "Math", "Full circle turn constant equal to 2π."),
        ConstantEntry("c", "299792458", "Speed of Light", 299792458.0, "2.99792458 × 10⁸", "m/s", "Physics", "Exact speed of electromagnetic waves in vacuum."),
        ConstantEntry("g", "9.80665", "Standard Gravity", 9.80665, "9.80665", "m/s²", "Physics", "Nominal acceleration due to gravity on Earth."),
        ConstantEntry("G", "6.67430e-11", "Gravitational Constant", 6.67430e-11, "6.67430 × 10⁻¹¹", "m³/(kg·s²)", "Physics", "Newtonian constant of universal gravitation."),
        ConstantEntry("h", "6.62607015e-34", "Planck Constant", 6.62607015e-34, "6.62607015 × 10⁻³⁴", "J·s", "Quantum", "Relates photon energy E to frequency ν (E = hν)."),
        ConstantEntry("ħ", "1.054571817e-34", "Reduced Planck", 1.054571817e-34, "1.054571817 × 10⁻³⁴", "J·s", "Quantum", "Dirac constant h / (2π) used in angular momentum."),
        ConstantEntry("k_B", "1.380649e-23", "Boltzmann Constant", 1.380649e-23, "1.380649 × 10⁻²³", "J/K", "Thermo", "Relates average thermal energy to temperature."),
        ConstantEntry("N_A", "6.02214076e23", "Avogadro Constant", 6.02214076e23, "6.02214076 × 10²³", "mol⁻¹", "Chemistry", "Number of constituent particles in one mole."),
        ConstantEntry("e₀", "1.602176634e-19", "Elementary Charge", 1.602176634e-19, "1.602176634 × 10⁻¹⁹", "C", "EM", "Magnitude of electric charge carried by one proton."),
        ConstantEntry("R", "8.314462618", "Molar Gas Constant", 8.314462618, "8.314462618", "J/(mol·K)", "Thermo", "Product of Boltzmann and Avogadro constants (PV = nRT)."),
        ConstantEntry("ε₀", "8.8541878128e-12", "Vacuum Permittivity", 8.8541878128e-12, "8.85418781 × 10⁻¹²", "F/m", "EM", "Electric constant relating charge to electric field."),
        ConstantEntry("μ₀", "1.25663706212e-6", "Vacuum Permeability", 1.25663706212e-6, "1.25663706 × 10⁻⁶", "N/A²", "EM", "Magnetic constant of classical electromagnetism."),
        ConstantEntry("m_e", "9.1093837015e-31", "Electron Mass", 9.1093837015e-31, "9.1093837 × 10⁻³¹", "kg", "Quantum", "Rest mass of a stationary electron."),
        ConstantEntry("m_p", "1.67262192369e-27", "Proton Mass", 1.67262192369e-27, "1.6726219 × 10⁻²⁷", "kg", "Quantum", "Rest mass of a stationary proton."),
        ConstantEntry("σ", "5.670374419e-8", "Stefan-Boltzmann", 5.670374419e-8, "5.670374 × 10⁻⁸", "W/(m²·K⁴)", "Thermo", "Proportionality constant for blackbody radiant emittance.")
    )
}
