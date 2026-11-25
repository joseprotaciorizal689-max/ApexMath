package com.example.core.curriculum

import com.example.core.math.MathFormatter
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

object CurriculumRepository {
    val allModules: List<LessonModule> =
        AlgebraCurriculum.modules + GeometryCurriculum.modules + CalculusCurriculum.modules

    fun modulesForTrack(track: MathTrack): List<LessonModule> =
        allModules.filter { it.track == track }.sortedBy { it.order }

    fun findById(id: String): LessonModule? = allModules.find { it.id == id }
}

data class PracticeProblem(
    val id: String,
    val lessonId: String,
    val track: MathTrack,
    val difficulty: Int, // 1 = Foundation, 2 = Intermediate, 3 = Challenge
    val prompt: String,
    val hint: String,
    val expectedNumeric: Double,
    val tolerance: Double,
    val explanation: String,
    val unitHint: String = ""
)

data class PracticeEvaluation(
    val isCorrect: Boolean,
    val errorCategory: String,
    val feedback: String
)

object AdaptivePracticeEngine {

    /**
     * Deterministic adaptive rule:
     * - Starts at difficulty 1 or scales up to 2/3 when consecutive streak >= 2 or mastery score >= 65/85.
     * - Drops difficulty by 1 after a miss to rebuild mastery.
     */
    fun chooseNextDifficulty(currentDifficulty: Int, streak: Int, recentMiss: Boolean): Int {
        return when {
            recentMiss -> maxOf(1, currentDifficulty - 1)
            streak >= 2 -> minOf(3, currentDifficulty + 1)
            else -> currentDifficulty.coerceIn(1, 3)
        }
    }

    fun adaptationRuleNote(difficulty: Int, streak: Int, recentMiss: Boolean): String = when {
        recentMiss -> "Deterministic rule: stepped down to Level $difficulty to reinforce fundamentals after a miss."
        streak >= 2 -> "Deterministic rule: promoted to Level $difficulty (streak = $streak)."
        else -> "Deterministic rule: maintaining Level $difficulty based on recent accuracy."
    }

    fun generateProblem(track: MathTrack, lessonId: String?, difficulty: Int, seed: Long = System.nanoTime()): PracticeProblem {
        val rng = Random(seed)
        val lvl = difficulty.coerceIn(1, 3)
        return when (track) {
            MathTrack.ALGEBRA -> generateAlgebraProblem(lessonId ?: "alg_3", lvl, rng)
            MathTrack.GEOMETRY -> generateGeometryProblem(lessonId ?: "geo_2", lvl, rng)
            MathTrack.CALCULUS -> generateCalculusProblem(lessonId ?: "calc_4", lvl, rng)
        }
    }

    private fun generateAlgebraProblem(lessonId: String, lvl: Int, rng: Random): PracticeProblem {
        return when {
            lvl == 1 -> {
                val a = rng.nextInt(2, 9)
                val x = rng.nextInt(-8, 10)
                val b = rng.nextInt(-12, 13)
                val rhs = a * x + b
                val bSign = if (b >= 0) "+ $b" else "- ${abs(b)}"
                PracticeProblem(
                    id = "p_${rng.nextInt()}",
                    lessonId = lessonId,
                    track = MathTrack.ALGEBRA,
                    difficulty = lvl,
                    prompt = "Solve for x: ${a}x $bSign = $rhs",
                    hint = "Isolate ${a}x first by subtracting/adding ${abs(b)} on both sides, then divide by $a.",
                    expectedNumeric = x.toDouble(),
                    tolerance = 1e-4,
                    explanation = "${a}x = $rhs - ($b) = ${rhs - b}, so x = ${rhs - b} / $a = $x."
                )
            }
            lvl == 2 -> {
                val r1 = rng.nextInt(-6, 7)
                val r2 = rng.nextInt(r1 + 1, 9)
                // (x - r1)(x - r2) = x^2 - (r1+r2)x + r1*r2 = 0 -> ask for larger root
                val b = -(r1 + r2)
                val c = r1 * r2
                val bStr = if (b == 0) "" else if (b > 0) " + ${b}x" else " - ${abs(b)}x"
                val cStr = if (c == 0) "" else if (c > 0) " + $c" else " - ${abs(c)}"
                PracticeProblem(
                    id = "p_${rng.nextInt()}",
                    lessonId = lessonId,
                    track = MathTrack.ALGEBRA,
                    difficulty = lvl,
                    prompt = "Find the LARGER root of x²$bStr$cStr = 0",
                    hint = "Factor as (x - r₁)(x - r₂) = 0 or compute Δ = b² - 4ac.",
                    expectedNumeric = r2.toDouble(),
                    tolerance = 1e-4,
                    explanation = "Factoring gives (x - ($r1))(x - ($r2)) = 0, so roots are $r1 and $r2. Larger root = $r2."
                )
            }
            else -> {
                // 2x2 system: find x
                val x = rng.nextInt(-5, 6)
                val y = rng.nextInt(-5, 6)
                val a1 = rng.nextInt(1, 5)
                val b1 = rng.nextInt(1, 5)
                val a2 = rng.nextInt(1, 5)
                var b2 = rng.nextInt(-4, 5)
                if (a1 * b2 == a2 * b1) b2 += 1
                val c1 = a1 * x + b1 * y
                val c2 = a2 * x + b2 * y
                PracticeProblem(
                    id = "p_${rng.nextInt()}",
                    lessonId = lessonId,
                    track = MathTrack.ALGEBRA,
                    difficulty = lvl,
                    prompt = "Solve the system for x:\n${a1}x + ${b1}y = $c1\n${a2}x ${if (b2 >= 0) "+ $b2" else "- ${abs(b2)}"}y = $c2",
                    hint = "Use Cramer's rule: x = (c₁b₂ - c₂b₁) / (a₁b₂ - a₂b₁).",
                    expectedNumeric = x.toDouble(),
                    tolerance = 1e-4,
                    explanation = "det(A) = ${a1 * b2 - a2 * b1}; eliminating y yields x = $x (and y = $y)."
                )
            }
        }
    }

    private fun generateGeometryProblem(lessonId: String, lvl: Int, rng: Random): PracticeProblem {
        return when {
            lvl == 1 -> {
                val r = rng.nextInt(2, 12).toDouble()
                val area = PI * r * r
                PracticeProblem(
                    id = "p_${rng.nextInt()}",
                    lessonId = lessonId,
                    track = MathTrack.GEOMETRY,
                    difficulty = lvl,
                    prompt = "Calculate the area of a circle with radius r = ${r.toInt()} (to 2 decimal places).",
                    hint = "Use A = πr².",
                    expectedNumeric = area,
                    tolerance = 0.05,
                    explanation = "A = π × ${r.toInt()}² = ${(r * r).toInt()}π ≈ ${MathFormatter.formatNumber(area, 4)}."
                )
            }
            lvl == 2 -> {
                val a = rng.nextInt(4, 12).toDouble()
                val b = rng.nextInt(4, 12).toDouble()
                val deg = listOf(30, 45, 60, 120).random(rng)
                val c = sqrt(a * a + b * b - 2 * a * b * cos(Math.toRadians(deg.toDouble())))
                PracticeProblem(
                    id = "p_${rng.nextInt()}",
                    lessonId = lessonId,
                    track = MathTrack.GEOMETRY,
                    difficulty = lvl,
                    prompt = "Triangle has sides a = ${a.toInt()}, b = ${b.toInt()}, and included angle C = $deg°. Find side c (to 2 decimals).",
                    hint = "Apply Law of Cosines: c = √(a² + b² - 2ab·cos(C)).",
                    expectedNumeric = c,
                    tolerance = 0.05,
                    explanation = "c = √(${a.toInt()}² + ${b.toInt()}² - 2·${a.toInt()}·${b.toInt()}·cos($deg°)) ≈ ${MathFormatter.formatNumber(c, 4)}."
                )
            }
            else -> {
                val r = rng.nextInt(3, 10).toDouble()
                val h = rng.nextInt(4, 12).toDouble()
                val vol = (1.0 / 3.0) * PI * r * r * h
                PracticeProblem(
                    id = "p_${rng.nextInt()}",
                    lessonId = lessonId,
                    track = MathTrack.GEOMETRY,
                    difficulty = lvl,
                    prompt = "Find the volume of a right circular cone with radius r = ${r.toInt()} and height h = ${h.toInt()} (to 2 decimals).",
                    hint = "V = (1/3)πr²h.",
                    expectedNumeric = vol,
                    tolerance = 0.08,
                    explanation = "V = (1/3)π(${r.toInt()}²)(${h.toInt()}) ≈ ${MathFormatter.formatNumber(vol, 4)}."
                )
            }
        }
    }

    private fun generateCalculusProblem(lessonId: String, lvl: Int, rng: Random): PracticeProblem {
        return when {
            lvl == 1 -> {
                val c = rng.nextInt(2, 7)
                val p = rng.nextInt(2, 5)
                val x0 = rng.nextInt(1, 4)
                val deriv = c * p * Math.pow(x0.toDouble(), (p - 1).toDouble())
                PracticeProblem(
                    id = "p_${rng.nextInt()}",
                    lessonId = lessonId,
                    track = MathTrack.CALCULUS,
                    difficulty = lvl,
                    prompt = "For f(x) = ${c}x^$p, compute the derivative f'($x0).",
                    hint = "Power rule: d/dx(c·xⁿ) = c·n·xⁿ⁻¹.",
                    expectedNumeric = deriv,
                    tolerance = 1e-3,
                    explanation = "f'(x) = ${c * p}x^${p - 1}, so f'($x0) = ${c * p}·$x0^${p - 1} = ${MathFormatter.formatNumber(deriv, 4)}."
                )
            }
            lvl == 2 -> {
                val a = rng.nextInt(1, 5)
                val upper = rng.nextInt(2, 5)
                // Integral of (3*a*x^2 + 2) from 0 to upper = a*upper^3 + 2*upper
                val ans = (a * upper * upper * upper + 2 * upper).toDouble()
                PracticeProblem(
                    id = "p_${rng.nextInt()}",
                    lessonId = lessonId,
                    track = MathTrack.CALCULUS,
                    difficulty = lvl,
                    prompt = "Evaluate the definite integral: ∫₀^$upper (${3 * a}x² + 2) dx",
                    hint = "Antiderivative is ${a}x³ + 2x; evaluate F($upper) - F(0).",
                    expectedNumeric = ans,
                    tolerance = 1e-3,
                    explanation = "F(x) = ${a}x³ + 2x. F($upper) - F(0) = $a(${upper}³) + 2($upper) = ${ans.toInt()}."
                )
            }
            else -> {
                val k = rng.nextInt(2, 7)
                // d/dx sin(k*x) at x = 0 is k*cos(0) = k
                PracticeProblem(
                    id = "p_${rng.nextInt()}",
                    lessonId = lessonId,
                    track = MathTrack.CALCULUS,
                    difficulty = lvl,
                    prompt = "Find f'(0) for f(x) = sin(${k}x) + e^(${k - 1}x) (in radians).",
                    hint = "Chain rule: f'(x) = ${k}cos(${k}x) + ${k - 1}e^(${k - 1}x).",
                    expectedNumeric = (k + (k - 1)).toDouble(),
                    tolerance = 1e-3,
                    explanation = "f'(0) = ${k}·cos(0) + ${k - 1}·e⁰ = $k + ${k - 1} = ${2 * k - 1}."
                )
            }
        }
    }

    fun evaluateAnswer(problem: PracticeProblem, userInput: String): PracticeEvaluation {
        val parsed = com.example.core.math.ExpressionParser.evaluate(
            userInput.trim(),
            com.example.core.math.AngleUnit.RAD,
            allowComplex = false
        )
        val userVal = (parsed as? com.example.core.math.MathEvalResult.Success)?.realValue
            ?: return PracticeEvaluation(
                isCorrect = false,
                errorCategory = "Invalid Format",
                feedback = "Enter a valid number or math expression (e.g. 3/4 or 2*pi)."
            )

        val diff = abs(userVal - problem.expectedNumeric)
        if (diff <= problem.tolerance) {
            return PracticeEvaluation(
                isCorrect = true,
                errorCategory = "",
                feedback = "Correct! ${problem.explanation}"
            )
        }
        val category = when {
            abs(userVal + problem.expectedNumeric) <= problem.tolerance -> "Sign Error"
            diff < maxOf(0.5, problem.tolerance * 10.0) -> "Precision / Rounding"
            else -> "Formula / Concept Mismatch"
        }
        return PracticeEvaluation(
            isCorrect = false,
            errorCategory = category,
            feedback = "$category — Expected ≈ ${MathFormatter.formatNumber(problem.expectedNumeric, 4)}. ${problem.explanation}"
        )
    }
}
