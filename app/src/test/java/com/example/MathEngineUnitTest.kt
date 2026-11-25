package com.example

import com.example.core.curriculum.AdaptivePracticeEngine
import com.example.core.curriculum.CurriculumRepository
import com.example.core.curriculum.MathTrack
import com.example.core.math.AlgebraEngine
import com.example.core.math.AngleUnit
import com.example.core.math.CalculusEngine
import com.example.core.math.Complex
import com.example.core.math.ExpressionParser
import com.example.core.math.Fraction
import com.example.core.math.GeometryEngine
import com.example.core.math.GraphEngine
import com.example.core.math.MathEvalResult
import com.example.core.math.MatrixData
import com.example.core.math.PercentageUtils
import com.example.core.math.SymbolicIntegralResult
import com.example.core.math.TriangleMode
import com.example.core.math.TriangleSolveResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.PI
import kotlin.math.abs

class MathEngineUnitTest {

    @Test
    fun expressionPrecedenceAndImplicitMultiplication() {
        val r1 = ExpressionParser.evaluate("2 + 3 * 4") as MathEvalResult.Success
        assertEquals(14.0, r1.realValue!!, 1e-9)

        val r2 = ExpressionParser.evaluate("-3^2") as MathEvalResult.Success
        assertEquals(-9.0, r2.realValue!!, 1e-9)

        val r3 = ExpressionParser.evaluate("(-3)^2") as MathEvalResult.Success
        assertEquals(9.0, r3.realValue!!, 1e-9)

        val r4 = ExpressionParser.evaluate("2(3 + 4)", AngleUnit.RAD) as MathEvalResult.Success
        assertEquals(14.0, r4.realValue!!, 1e-9)

        val r5 = ExpressionParser.evaluate("2pi", AngleUnit.RAD) as MathEvalResult.Success
        assertEquals(2.0 * PI, r5.realValue!!, 1e-9)
    }

    @Test
    fun fractionsAndPercentages() {
        val f = Fraction.fromDouble(0.375)
        assertNotNull(f)
        assertEquals(3L, f!!.num)
        assertEquals(8L, f.den)

        val sum = Fraction(1, 3) + Fraction(1, 6)
        assertEquals("1/2", sum.format())

        val mixed = Fraction(7, 4).toMixedString()
        assertEquals("1 3/4", mixed)

        assertEquals(120.0, PercentageUtils.reversePercentAfterChange(150.0, 25.0), 1e-9)
        assertEquals(25.0, PercentageUtils.percentChange(80.0, 100.0), 1e-9)
    }

    @Test
    fun trigonometryAndDomainErrors() {
        val sin30 = ExpressionParser.evaluate("sin(30)", AngleUnit.DEG) as MathEvalResult.Success
        assertEquals(0.5, sin30.realValue!!, 1e-9)

        val sinPi2 = ExpressionParser.evaluate("sin(pi/2)", AngleUnit.RAD) as MathEvalResult.Success
        assertEquals(1.0, sinPi2.realValue!!, 1e-9)

        assertTrue(ExpressionParser.evaluate("1 / 0") is MathEvalResult.Error)
        assertTrue(ExpressionParser.evaluate("ln(-5)", allowComplex = false) is MathEvalResult.Error)
        assertTrue(ExpressionParser.evaluate("asin(2)") is MathEvalResult.Error)
        assertTrue(ExpressionParser.evaluate("tan(90)", AngleUnit.DEG) is MathEvalResult.Error)
        assertTrue(ExpressionParser.evaluate("171!") is MathEvalResult.Error)
    }

    @Test
    fun complexArithmeticAndPolar() {
        val res = ExpressionParser.evaluate("(3 + 4i) / (1 - 2i)") as MathEvalResult.Success
        // (3+4i)(1+2i)/5 = (-5 + 10i)/5 = -1 + 2i
        assertEquals(-1.0, res.complex.re, 1e-9)
        assertEquals(2.0, res.complex.im, 1e-9)

        val rootNeg = ExpressionParser.evaluate("sqrt(-9)") as MathEvalResult.Success
        assertEquals(0.0, rootNeg.complex.re, 1e-9)
        assertEquals(3.0, rootNeg.complex.im, 1e-9)

        val z = Complex(3.0, 4.0)
        assertEquals(5.0, z.magnitude, 1e-9)
    }

    @Test
    fun algebraSolversAndMatrices() {
        val quad = AlgebraEngine.solveQuadratic(1.0, -5.0, 6.0)
        assertTrue(quad.summary.contains("3") && quad.summary.contains("2"))

        val quadComplex = AlgebraEngine.solveQuadratic(1.0, 0.0, 4.0)
        assertTrue(quadComplex.summary.contains("2i"))

        val cubic = AlgebraEngine.solveCubic(1.0, -6.0, 11.0, -6.0)
        assertFalse(cubic.isWarning)
        assertTrue(cubic.summary.contains("1") && cubic.summary.contains("2") && cubic.summary.contains("3"))

        val sys = AlgebraEngine.solveLinearSystem(
            listOf(listOf(2.0, 1.0, 7.0), listOf(1.0, -1.0, 2.0))
        )
        assertTrue(sys.summary.contains("x = 3") && sys.summary.contains("y = 1"))

        val singularSys = AlgebraEngine.solveLinearSystem(
            listOf(listOf(1.0, 1.0, 2.0), listOf(2.0, 2.0, 5.0))
        )
        assertTrue(singularSys.isWarning)

        val a = MatrixData(2, 2, listOf(listOf(4.0, 2.0), listOf(1.0, 3.0)))
        assertEquals(10.0, AlgebraEngine.determinant(a), 1e-9)
        val inv = AlgebraEngine.inverseMatrix(a)
        val prod = AlgebraEngine.multiplyMatrices(a, inv)
        assertEquals(1.0, prod[0, 0], 1e-9)
        assertEquals(0.0, prod[0, 1], 1e-9)
        assertEquals(1.0, prod[1, 1], 1e-9)

        val eigen = AlgebraEngine.eigen2x2(a)
        assertTrue(eigen.summary.contains("5") && eigen.summary.contains("2"))

        val singularMat = MatrixData(2, 2, listOf(listOf(1.0, 2.0), listOf(2.0, 4.0)))
        assertEquals(0.0, AlgebraEngine.determinant(singularMat), 1e-9)
        assertEquals(1, AlgebraEngine.matrixRank(singularMat))
    }

    @Test(expected = ArithmeticException::class)
    fun singularMatrixInverseThrows() {
        val singular = MatrixData(2, 2, listOf(listOf(1.0, 2.0), listOf(2.0, 4.0)))
        AlgebraEngine.inverseMatrix(singular)
    }

    @Test
    fun numberTheoryAndGeometry() {
        assertTrue(AlgebraEngine.isPrime(97L))
        assertFalse(AlgebraEngine.isPrime(91L))
        assertEquals(mapOf(2L to 2, 3L to 1, 7L to 1), AlgebraEngine.primeFactorization(84L))
        assertEquals(4L, AlgebraEngine.modInverse(3L, 11L))

        val sss = GeometryEngine.solveTriangle(TriangleMode.SSS, 3.0, 4.0, 5.0) as TriangleSolveResult.Valid
        assertEquals(6.0, sss.solutions.first().area, 1e-6)
        assertEquals(90.0, sss.solutions.first().angleCDeg, 1e-6)

        val invalidTri = GeometryEngine.solveTriangle(TriangleMode.SSS, 1.0, 2.0, 10.0)
        assertTrue(invalidTri is TriangleSolveResult.Invalid)

        val ssaAmbiguous = GeometryEngine.solveTriangle(TriangleMode.SSA, 5.0, 7.0, 35.0) as TriangleSolveResult.Valid
        assertTrue(ssaAmbiguous.isAmbiguous)
        assertEquals(2, ssaAmbiguous.solutions.size)

        val coord = GeometryEngine.coordinateTwoPoints(0.0, 0.0, 3.0, 4.0)
        assertEquals(5.0, coord.measurements.first { it.label == "Distance" }.value, 1e-9)
    }

    @Test
    fun calculusSymbolicNumericalAndGraphing() {
        val diff = CalculusEngine.differentiate("x^3 + sin(2*x)", evalAtX = 0.0)
        assertEquals(2.0, diff.numericAtPoint!!, 1e-6)

        val numDeriv = CalculusEngine.numericalDerivative("x^3", 2.0)
        assertEquals(12.0, numDeriv, 1e-6)

        val symInt = CalculusEngine.integrateSymbolic("3*x^2 + cos(x)")
        assertTrue(symInt is SymbolicIntegralResult.Success)

        val unsupportedInt = CalculusEngine.integrateSymbolic("exp(-x^2)")
        assertTrue(unsupportedInt is SymbolicIntegralResult.Unsupported)

        val defInt = CalculusEngine.integrateDefinite("x^2", 0.0, 3.0)
        assertEquals(9.0, defInt.value, 1e-6)

        val lim = CalculusEngine.evaluateLimit("sin(x)/x", 0.0)
        assertNotNull(lim.twoSidedEstimate)
        assertEquals(1.0, lim.twoSidedEstimate!!, 1e-4)

        val taylor = CalculusEngine.taylorSeries("exp(x)", 0.0, 5, 1.0)
        assertTrue(abs(taylor.errorAtTest!!) < 0.005)

        val root = CalculusEngine.findRootNewton("x^2 - 2", 1.5)
        assertTrue(root.converged)
        assertEquals(kotlin.math.sqrt(2.0), root.root!!, 1e-7)

        // Graph sampling should split 1/x across x = 0 instead of drawing an asymptote connector
        val segments = GraphEngine.sampleFunction("1/x", -4.0, 4.0, -10.0, 10.0, 200, AngleUnit.RAD)
        assertTrue(segments.size >= 2)
    }

    @Test
    fun curriculumCompletenessAndAdaptivePractice() {
        assertEquals(8, CurriculumRepository.modulesForTrack(MathTrack.ALGEBRA).size)
        assertEquals(8, CurriculumRepository.modulesForTrack(MathTrack.GEOMETRY).size)
        assertEquals(8, CurriculumRepository.modulesForTrack(MathTrack.CALCULUS).size)
        assertTrue(CurriculumRepository.allModules.all { it.quickChecks.size == 3 })

        val p = AdaptivePracticeEngine.generateProblem(MathTrack.ALGEBRA, "alg_3", 1, seed = 42L)
        val ok = AdaptivePracticeEngine.evaluateAnswer(p, p.expectedNumeric.toString())
        assertTrue(ok.isCorrect)
    }
}
