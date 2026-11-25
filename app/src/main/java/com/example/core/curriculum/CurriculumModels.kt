package com.example.core.curriculum

enum class MathTrack(val title: String, val subtitle: String) {
    ALGEBRA("Algebra", "Foundations, Equations, Matrices & Functions"),
    GEOMETRY("Geometry", "Euclidean Proofs, Triangles, Circles & Trig"),
    CALCULUS("Calculus", "Limits, Derivatives, Integrals & Series")
}

enum class SandboxType {
    LINEAR_EXPLORER,        // Adjust m and b in y = mx + b
    QUADRATIC_EXPLORER,     // Adjust a, b, c in ax^2 + bx + c
    EXPONENTIAL_LOG,        // Adjust base b & scale a in a*b^x vs log
    TRIANGLE_INTERACTIVE,   // Drag/adjust vertices & angles, live Law of Sines/Cosines
    UNIT_CIRCLE,            // Drag angle theta around unit circle, live sin/cos/tan
    SECANT_TANGENT,         // Drag point x0 and step h to watch secant approach tangent
    INTEGRAL_SHADE,         // Adjust lower/upper bounds [a, b], live area under curve
    TAYLOR_APPROX           // Adjust expansion order N and center, compare polynomial vs f(x)
}

enum class CalcTargetTier {
    STANDARD, ALGEBRA, GEOMETRY, CALCULUS
}

data class QuickCheckQuestion(
    val prompt: String,
    val options: List<String>,
    val correctIndex: Int,
    val deductiveExplanation: String
)

data class LessonModule(
    val id: String,
    val track: MathTrack,
    val order: Int,
    val title: String,
    val summary: String,
    val conceptExplanation: String,
    val mathematicalIntuition: String,
    val coreFormulas: List<String>,
    val definitions: List<Pair<String, String>>,
    val commonMistakes: List<String>,
    val sandboxType: SandboxType,
    val sandboxTitle: String,
    val sandboxExpression: String,
    val workedExampleProblem: String,
    val workedExampleSteps: List<String>,
    val workedExampleAnswer: String,
    val quickChecks: List<QuickCheckQuestion>,
    val calculatorTier: CalcTargetTier,
    val sendToCalculatorExpr: String,
    val graphExpr: String
)
