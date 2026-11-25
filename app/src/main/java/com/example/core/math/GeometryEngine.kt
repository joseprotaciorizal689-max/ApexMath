package com.example.core.math

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

enum class TriangleMode { SSS, SAS, ASA, AAS, SSA }

data class TriangleSolution(
    val a: Double,
    val b: Double,
    val c: Double,
    val angleADeg: Double,
    val angleBDeg: Double,
    val angleCDeg: Double,
    val area: Double,
    val perimeter: Double,
    val heightA: Double,
    val circumradius: Double,
    val inradius: Double
)

sealed class TriangleSolveResult {
    data class Valid(
        val solutions: List<TriangleSolution>,
        val steps: List<String>,
        val isAmbiguous: Boolean = false
    ) : TriangleSolveResult()

    data class Invalid(val reason: String) : TriangleSolveResult()
}

data class GeometryMeasurement(
    val label: String,
    val value: Double,
    val unitHint: String = ""
)

data class GeometrySolveResult(
    val title: String,
    val measurements: List<GeometryMeasurement>,
    val formulas: List<String>,
    val calculatorExpr: String? = null
)

object GeometryEngine {

    fun solveTriangle(
        mode: TriangleMode,
        v1: Double,
        v2: Double,
        v3: Double
    ): TriangleSolveResult {
        if (v1 <= 0 || v2 <= 0 || v3 <= 0 || !v1.isFinite() || !v2.isFinite() || !v3.isFinite()) {
            return TriangleSolveResult.Invalid("All input sides and angles must be positive finite numbers.")
        }

        val steps = mutableListOf<String>()
        return when (mode) {
            TriangleMode.SSS -> {
                val (a, b, c) = Triple(v1, v2, v3)
                if (a + b <= c || a + c <= b || b + c <= a) {
                    return TriangleSolveResult.Invalid("Triangle inequality violated: sum of any two sides must exceed the third.")
                }
                val angA = Math.toDegrees(acos(((b * b + c * c - a * a) / (2 * b * c)).coerceIn(-1.0, 1.0)))
                val angB = Math.toDegrees(acos(((a * a + c * c - b * b) / (2 * a * c)).coerceIn(-1.0, 1.0)))
                val angC = 180.0 - angA - angB
                steps += "Law of Cosines: cos(A) = (b² + c² - a²) / (2bc)"
                steps += "A = ${fmt(angA)}°, B = ${fmt(angB)}°, C = ${fmt(angC)}°"
                TriangleSolveResult.Valid(listOf(buildTriangle(a, b, c, angA, angB, angC)), steps)
            }
            TriangleMode.SAS -> {
                val (a, angleCDeg, b) = Triple(v1, v2, v3)
                if (angleCDeg >= 180.0) return TriangleSolveResult.Invalid("Included angle C must be strictly less than 180°.")
                val c = sqrt(a * a + b * b - 2 * a * b * cos(Math.toRadians(angleCDeg)))
                val angA = Math.toDegrees(acos(((b * b + c * c - a * a) / (2 * b * c)).coerceIn(-1.0, 1.0)))
                val angB = 180.0 - angleCDeg - angA
                steps += "Law of Cosines: c = √(a² + b² - 2ab·cos(C)) = ${fmt(c)}"
                steps += "Angle sum: A = ${fmt(angA)}°, B = ${fmt(angB)}°"
                TriangleSolveResult.Valid(listOf(buildTriangle(a, b, c, angA, angB, angleCDeg)), steps)
            }
            TriangleMode.ASA -> {
                val (angA, c, angB) = Triple(v1, v2, v3)
                if (angA + angB >= 180.0) return TriangleSolveResult.Invalid("Sum of angles A + B must be less than 180°.")
                val angC = 180.0 - angA - angB
                val sinC = sin(Math.toRadians(angC))
                val a = c * sin(Math.toRadians(angA)) / sinC
                val b = c * sin(Math.toRadians(angB)) / sinC
                steps += "C = 180° - A - B = ${fmt(angC)}°"
                steps += "Law of Sines: a/sin(A) = b/sin(B) = c/sin(C)"
                TriangleSolveResult.Valid(listOf(buildTriangle(a, b, c, angA, angB, angC)), steps)
            }
            TriangleMode.AAS -> {
                val (angA, angB, a) = Triple(v1, v2, v3)
                if (angA + angB >= 180.0) return TriangleSolveResult.Invalid("Sum of angles A + B must be less than 180°.")
                val angC = 180.0 - angA - angB
                val sinA = sin(Math.toRadians(angA))
                val b = a * sin(Math.toRadians(angB)) / sinA
                val c = a * sin(Math.toRadians(angC)) / sinA
                steps += "C = 180° - A - B = ${fmt(angC)}°"
                steps += "Law of Sines: b = a·sin(B)/sin(A), c = a·sin(C)/sin(A)"
                TriangleSolveResult.Valid(listOf(buildTriangle(a, b, c, angA, angB, angC)), steps)
            }
            TriangleMode.SSA -> {
                // Given side a, side b, angle A
                val (a, b, angA) = Triple(v1, v2, v3)
                if (angA >= 180.0) return TriangleSolveResult.Invalid("Angle A must be < 180°.")
                val sinA = sin(Math.toRadians(angA))
                val h = b * sinA
                steps += "Altitude h = b·sin(A) = ${fmt(h)}"
                if (angA < 90.0) {
                    if (a < h - 1e-9) {
                        return TriangleSolveResult.Invalid("No triangle exists: side a (${fmt(a)}) < height b·sin(A) (${fmt(h)}).")
                    }
                    if (abs(a - h) <= 1e-9) {
                        val angB = 90.0
                        val angC = 90.0 - angA
                        val c = b * cos(Math.toRadians(angA))
                        steps += "a = b·sin(A) → Exact right triangle (B = 90°)."
                        return TriangleSolveResult.Valid(listOf(buildTriangle(a, b, c, angA, angB, angC)), steps)
                    }
                    val sinB = (b * sinA / a).coerceIn(-1.0, 1.0)
                    val angB1 = Math.toDegrees(asin(sinB))
                    val angC1 = 180.0 - angA - angB1
                    val c1 = a * sin(Math.toRadians(angC1)) / sinA
                    val sol1 = buildTriangle(a, b, c1, angA, angB1, angC1)
                    if (a < b - 1e-9) {
                        // Ambiguous case: 2 valid triangles!
                        val angB2 = 180.0 - angB1
                        val angC2 = 180.0 - angA - angB2
                        val c2 = a * sin(Math.toRadians(angC2)) / sinA
                        val sol2 = buildTriangle(a, b, c2, angA, angB2, angC2)
                        steps += "Ambiguous SSA case (h < a < b): Two distinct valid triangles exist."
                        steps += "Triangle 1: B = ${fmt(angB1)}°, Triangle 2: B' = ${fmt(angB2)}°"
                        return TriangleSolveResult.Valid(listOf(sol1, sol2), steps, isAmbiguous = true)
                    } else {
                        steps += "a ≥ b → Unique triangle."
                        return TriangleSolveResult.Valid(listOf(sol1), steps)
                    }
                } else {
                    if (a <= b) {
                        return TriangleSolveResult.Invalid("For obtuse/right angle A ≥ 90°, side a must exceed side b.")
                    }
                    val sinB = (b * sinA / a).coerceIn(-1.0, 1.0)
                    val angB = Math.toDegrees(asin(sinB))
                    val angC = 180.0 - angA - angB
                    val c = a * sin(Math.toRadians(angC)) / sinA
                    steps += "Obtuse angle A with a > b → Unique triangle."
                    return TriangleSolveResult.Valid(listOf(buildTriangle(a, b, c, angA, angB, angC)), steps)
                }
            }
        }
    }

    private fun buildTriangle(a: Double, b: Double, c: Double, angA: Double, angB: Double, angC: Double): TriangleSolution {
        val p = a + b + c
        val s = p / 2.0
        val area = sqrt(maxOf(0.0, s * (s - a) * (s - b) * (s - c)))
        val heightA = 2.0 * area / a
        val circumR = (a * b * c) / (4.0 * area).coerceAtLeast(1e-12)
        val inR = area / s
        return TriangleSolution(a, b, c, angA, angB, angC, area, p, heightA, circumR, inR)
    }

    // 2D Geometry
    fun circle(r: Double): GeometrySolveResult {
        require(r > 0) { "Radius must be > 0" }
        val area = PI * r * r
        val circ = 2 * PI * r
        return GeometrySolveResult(
            "Circle (r = ${fmt(r)})",
            listOf(
                GeometryMeasurement("Diameter", 2 * r),
                GeometryMeasurement("Circumference", circ),
                GeometryMeasurement("Area", area)
            ),
            listOf("C = 2πr", "A = πr²"),
            "pi*(${fmt(r)})^2"
        )
    }

    fun sector(r: Double, angleDeg: Double): GeometrySolveResult {
        require(r > 0 && angleDeg > 0 && angleDeg <= 360) { "Radius > 0 and 0 < θ ≤ 360° required" }
        val rad = Math.toRadians(angleDeg)
        val arc = r * rad
        val area = 0.5 * r * r * rad
        val chord = 2 * r * sin(rad / 2.0)
        return GeometrySolveResult(
            "Circular Sector & Arc",
            listOf(
                GeometryMeasurement("Arc Length", arc),
                GeometryMeasurement("Sector Area", area),
                GeometryMeasurement("Chord Length", chord),
                GeometryMeasurement("Perimeter", arc + 2 * r)
            ),
            listOf("s = rθ (rad)", "A = ½r²θ", "c = 2r·sin(θ/2)"),
            "0.5*(${fmt(r)})^2*(${fmt(rad)})"
        )
    }

    fun rectangle(w: Double, h: Double): GeometrySolveResult {
        require(w > 0 && h > 0) { "Width and height must be > 0" }
        return GeometrySolveResult(
            "Rectangle",
            listOf(
                GeometryMeasurement("Area", w * h),
                GeometryMeasurement("Perimeter", 2 * (w + h)),
                GeometryMeasurement("Diagonal", hypot(w, h))
            ),
            listOf("A = w·h", "P = 2(w + h)", "d = √(w² + h²)"),
            "${fmt(w)}*${fmt(h)}"
        )
    }

    fun trapezoid(b1: Double, b2: Double, h: Double): GeometrySolveResult {
        require(b1 > 0 && b2 > 0 && h > 0) { "Bases and height must be > 0" }
        val area = 0.5 * (b1 + b2) * h
        return GeometrySolveResult(
            "Trapezoid",
            listOf(
                GeometryMeasurement("Area", area),
                GeometryMeasurement("Median (Midsegment)", 0.5 * (b1 + b2))
            ),
            listOf("A = ½(b₁ + b₂)h", "m = ½(b₁ + b₂)"),
            "0.5*(${fmt(b1)}+${fmt(b2)})*${fmt(h)}"
        )
    }

    fun regularPolygon(sides: Int, sideLength: Double): GeometrySolveResult {
        require(sides >= 3 && sideLength > 0) { "Regular polygon requires n ≥ 3 and side length > 0" }
        val n = sides.toDouble()
        val perimeter = n * sideLength
        val interiorAngle = (n - 2.0) * 180.0 / n
        val exteriorAngle = 360.0 / n
        val apothem = sideLength / (2.0 * tan(PI / n))
        val area = 0.5 * perimeter * apothem
        return GeometrySolveResult(
            "Regular $sides-gon",
            listOf(
                GeometryMeasurement("Area", area),
                GeometryMeasurement("Perimeter", perimeter),
                GeometryMeasurement("Apothem", apothem),
                GeometryMeasurement("Interior Angle", interiorAngle, "°"),
                GeometryMeasurement("Exterior Angle", exteriorAngle, "°")
            ),
            listOf("A = ½Pa", "a = s / (2·tan(π/n))", "θ_int = (n-2)·180°/n"),
            "${fmt(area)}"
        )
    }

    // 3D Geometry
    fun sphere(r: Double): GeometrySolveResult {
        require(r > 0) { "Radius must be > 0" }
        return GeometrySolveResult(
            "Sphere",
            listOf(
                GeometryMeasurement("Volume", (4.0 / 3.0) * PI * r * r * r),
                GeometryMeasurement("Surface Area", 4.0 * PI * r * r)
            ),
            listOf("V = (4/3)πr³", "SA = 4πr²"),
            "(4/3)*pi*(${fmt(r)})^3"
        )
    }

    fun cylinder(r: Double, h: Double): GeometrySolveResult {
        require(r > 0 && h > 0) { "Radius and height must be > 0" }
        val lat = 2 * PI * r * h
        val total = lat + 2 * PI * r * r
        return GeometrySolveResult(
            "Cylinder",
            listOf(
                GeometryMeasurement("Volume", PI * r * r * h),
                GeometryMeasurement("Lateral Surface Area", lat),
                GeometryMeasurement("Total Surface Area", total)
            ),
            listOf("V = πr²h", "LSA = 2πrh", "TSA = 2πrh + 2πr²"),
            "pi*(${fmt(r)})^2*${fmt(h)}"
        )
    }

    fun cone(r: Double, h: Double): GeometrySolveResult {
        require(r > 0 && h > 0) { "Radius and height must be > 0" }
        val slant = hypot(r, h)
        val lat = PI * r * slant
        val total = lat + PI * r * r
        return GeometrySolveResult(
            "Right Circular Cone",
            listOf(
                GeometryMeasurement("Volume", (1.0 / 3.0) * PI * r * r * h),
                GeometryMeasurement("Slant Height", slant),
                GeometryMeasurement("Lateral Surface Area", lat),
                GeometryMeasurement("Total Surface Area", total)
            ),
            listOf("V = (1/3)πr²h", "l = √(r² + h²)", "LSA = πrl"),
            "(1/3)*pi*(${fmt(r)})^2*${fmt(h)}"
        )
    }

    fun frustum(r1: Double, r2: Double, h: Double): GeometrySolveResult {
        require(r1 > 0 && r2 > 0 && h > 0) { "Radii and height must be > 0" }
        val slant = hypot(r1 - r2, h)
        val vol = (PI * h / 3.0) * (r1 * r1 + r1 * r2 + r2 * r2)
        val lat = PI * (r1 + r2) * slant
        val total = lat + PI * r1 * r1 + PI * r2 * r2
        return GeometrySolveResult(
            "Conical Frustum",
            listOf(
                GeometryMeasurement("Volume", vol),
                GeometryMeasurement("Slant Height", slant),
                GeometryMeasurement("Lateral Area", lat),
                GeometryMeasurement("Total Surface Area", total)
            ),
            listOf("V = (πh/3)(R² + Rr + r²)", "l = √((R-r)² + h²)"),
            "${fmt(vol)}"
        )
    }

    fun cuboid(l: Double, w: Double, h: Double): GeometrySolveResult {
        require(l > 0 && w > 0 && h > 0) { "Dimensions must be > 0" }
        return GeometrySolveResult(
            "Cuboid",
            listOf(
                GeometryMeasurement("Volume", l * w * h),
                GeometryMeasurement("Surface Area", 2 * (l * w + w * h + l * h)),
                GeometryMeasurement("Space Diagonal", sqrt(l * l + w * w + h * h))
            ),
            listOf("V = lwh", "SA = 2(lw + wh + lh)", "d = √(l² + w² + h²)"),
            "${fmt(l)}*${fmt(w)}*${fmt(h)}"
        )
    }

    fun squarePyramid(baseSide: Double, h: Double): GeometrySolveResult {
        require(baseSide > 0 && h > 0) { "Base side and height must be > 0" }
        val slant = hypot(baseSide / 2.0, h)
        val lat = 2.0 * baseSide * slant
        val baseArea = baseSide * baseSide
        return GeometrySolveResult(
            "Square Pyramid",
            listOf(
                GeometryMeasurement("Volume", baseArea * h / 3.0),
                GeometryMeasurement("Slant Height", slant),
                GeometryMeasurement("Lateral Surface Area", lat),
                GeometryMeasurement("Total Surface Area", lat + baseArea)
            ),
            listOf("V = ⅓b²h", "l = √((b/2)² + h²)", "TSA = b² + 2bl"),
            "(${fmt(baseSide)})^2*${fmt(h)}/3"
        )
    }

    // Coordinate geometry
    fun coordinateTwoPoints(x1: Double, y1: Double, x2: Double, y2: Double): GeometrySolveResult {
        val dx = x2 - x1
        val dy = y2 - y1
        val dist = hypot(dx, dy)
        val midX = (x1 + x2) / 2.0
        val midY = (y1 + y2) / 2.0
        val measurements = mutableListOf(
            GeometryMeasurement("Distance", dist),
            GeometryMeasurement("Midpoint X", midX),
            GeometryMeasurement("Midpoint Y", midY)
        )
        val formulas = mutableListOf("d = √((x₂-x₁)² + (y₂-y₁)²)")
        var calcExpr: String? = null
        if (abs(dx) > 1e-12) {
            val m = dy / dx
            val b = y1 - m * x1
            val perpSlope = if (abs(m) > 1e-12) -1.0 / m else Double.NaN
            measurements += GeometryMeasurement("Slope (m)", m)
            measurements += GeometryMeasurement("y-intercept (b)", b)
            if (perpSlope.isFinite()) measurements += GeometryMeasurement("Perpendicular Slope", perpSlope)
            val bStr = if (b >= 0) "+ ${fmt(b)}" else "- ${fmt(abs(b))}"
            formulas += "Line: y = ${fmt(m)}x $bStr"
            calcExpr = "${fmt(m)}*x $bStr"
        } else {
            formulas += "Vertical line: x = ${fmt(x1)} (slope undefined)"
        }
        return GeometrySolveResult("Coordinate Two-Point Analysis", measurements, formulas, calcExpr)
    }

    fun pointToLineDistance(px: Double, py: Double, a: Double, b: Double, c: Double): GeometrySolveResult {
        val norm = hypot(a, b)
        require(norm > 1e-12) { "Line coefficients A and B cannot both be zero" }
        val dist = abs(a * px + b * py + c) / norm
        return GeometrySolveResult(
            "Point-to-Line Distance",
            listOf(GeometryMeasurement("Perpendicular Distance", dist)),
            listOf("d = |Ax₀ + By₀ + C| / √(A² + B²)"),
            "${fmt(dist)}"
        )
    }

    private fun fmt(v: Double): String = MathFormatter.formatNumber(v, 6)
}
