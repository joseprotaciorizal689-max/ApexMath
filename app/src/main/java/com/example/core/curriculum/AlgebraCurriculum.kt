package com.example.core.curriculum

object AlgebraCurriculum {
    val modules: List<LessonModule> = listOf(
        LessonModule(
            id = "alg_1",
            track = MathTrack.ALGEBRA,
            order = 1,
            title = "Arithmetic, Order of Operations & Number Systems",
            summary = "Precedence hierarchy, number classification (ℕ ⊂ ℤ ⊂ ℚ ⊂ ℝ ⊂ ℂ), and exact evaluation.",
            conceptExplanation = "Mathematical expressions are unambiguous only when evaluated under a universal precedence hierarchy: Parentheses/Grouping, Exponents & Roots, Multiplication & Division (left-to-right), and Addition & Subtraction (left-to-right).",
            mathematicalIntuition = "Think of operations as binding strength: exponentiation scales repeatedly, multiplication bundles additions, and addition combines independent quantities—so stronger bindings execute before weaker ones unless parentheses override them.",
            coreFormulas = listOf(
                "a(b + c) = ab + ac  (Distributive Law)",
                "a + b = b + a,  ab = ba  (Commutative)",
                "a⁰ = 1 (a ≠ 0),  a⁻ⁿ = 1 / aⁿ"
            ),
            definitions = listOf(
                "Rational Number (ℚ)" to "Any number expressible as p/q with integers p, q and q ≠ 0.",
                "Irrational Number (ℝ \\ ℚ)" to "Non-terminating, non-repeating real decimals like π or √2.",
                "Unary Minus" to "Negation applied after exponentiation unless grouped: -3² = -9, whereas (-3)² = 9."
            ),
            commonMistakes = listOf(
                "Evaluating -3² as +9 instead of -(3²) = -9.",
                "Doing multiplication before division strictly left-to-right: 8 ÷ 2 × 4 = 16, not 1."
            ),
            sandboxType = SandboxType.LINEAR_EXPLORER,
            sandboxTitle = "Scaling & Offset Explorer: y = m·x + b",
            sandboxExpression = "2*x + 3",
            workedExampleProblem = "Evaluate: 18 - 2 × (3 + 4)² ÷ 7",
            workedExampleSteps = listOf(
                "1. Parentheses first: 3 + 4 = 7",
                "2. Exponent: 7² = 49",
                "3. Multiply & Divide left-to-right: 2 × 49 = 98, then 98 ÷ 7 = 14",
                "4. Subtract: 18 - 14 = 4"
            ),
            workedExampleAnswer = "4",
            quickChecks = listOf(
                QuickCheckQuestion(
                    "What is the exact value of -2⁴ + (-2)⁴?",
                    listOf("0", "32", "-32", "16"),
                    0,
                    "-2⁴ means -(2⁴) = -16, whereas (-2)⁴ = +16. Their sum is -16 + 16 = 0."
                ),
                QuickCheckQuestion(
                    "Evaluate 24 ÷ 6 × 2.",
                    listOf("8", "2", "12", "6"),
                    0,
                    "Division and multiplication share equal precedence and associate left-to-right: (24 ÷ 6) × 2 = 4 × 2 = 8."
                ),
                QuickCheckQuestion(
                    "Which number is irrational?",
                    listOf("√12", "0.333...", "22/7", "√49"),
                    0,
                    "√12 = 2√3 cannot be written as a ratio of two integers, whereas 0.333... = 1/3, 22/7 ∈ ℚ, and √49 = 7."
                )
            ),
            calculatorTier = CalcTargetTier.STANDARD,
            sendToCalculatorExpr = "18 - 2*(3 + 4)^2 / 7",
            graphExpr = "2*x + 3"
        ),
        LessonModule(
            id = "alg_2",
            track = MathTrack.ALGEBRA,
            order = 2,
            title = "Fractions, Ratios & Proportions",
            summary = "Exact rational arithmetic, cross-multiplication, and proportional scaling.",
            conceptExplanation = "Fractions represent exact quotients of integers. Unlike floating-point decimals, reduced fractions preserve exact ratios without rounding loss across chained operations.",
            mathematicalIntuition = "Adding fractions requires a common unit size (common denominator). Multiplying scales both numerator and denominator directly because areas scale along both dimensions.",
            coreFormulas = listOf(
                "a/b ± c/d = (ad ± bc) / (bd)",
                "(a/b) ÷ (c/d) = (ad) / (bc)  (c ≠ 0)",
                "a/b = c/d ⟺ ad = bc  (Proportion)"
            ),
            definitions = listOf(
                "Coprime" to "Two integers whose greatest common divisor (GCD) is 1.",
                "Unit Rate" to "A ratio scaled so the denominator equals 1."
            ),
            commonMistakes = listOf(
                "Adding numerators and denominators directly: 1/2 + 1/3 ≠ 2/5.",
                "Canceling addends across a fraction bar: (x + 2)/2 ≠ x + 1."
            ),
            sandboxType = SandboxType.LINEAR_EXPLORER,
            sandboxTitle = "Proportional Slope Ratio Explorer",
            sandboxExpression = "(3/4)*x",
            workedExampleProblem = "Solve for x: (x + 2)/5 = 3/4",
            workedExampleSteps = listOf(
                "1. Cross-multiply: 4(x + 2) = 5 × 3",
                "2. Expand: 4x + 8 = 15",
                "3. Isolate x: 4x = 7 → x = 7/4 = 1.75"
            ),
            workedExampleAnswer = "7/4 (1.75)",
            quickChecks = listOf(
                QuickCheckQuestion(
                    "Compute 2/3 + 3/4 as a reduced fraction.",
                    listOf("17/12", "5/7", "6/12", "13/12"),
                    0,
                    "Common denominator is 12: 8/12 + 9/12 = 17/12, and gcd(17, 12) = 1."
                ),
                QuickCheckQuestion(
                    "Divide (5/6) ÷ (10/9).",
                    listOf("3/4", "25/27", "4/3", "50/54"),
                    0,
                    "Multiply by reciprocal: (5/6) × (9/10) = 45/60 = 3/4."
                ),
                QuickCheckQuestion(
                    "If a 25% price increase yields $150, what was the original price?",
                    listOf("$120", "$112.50", "$125", "$130"),
                    0,
                    "1.25 × P = 150 → P = 150 / 1.25 = $120. Subtracting 25% of 150 is a classic reverse-percent error."
                )
            ),
            calculatorTier = CalcTargetTier.STANDARD,
            sendToCalculatorExpr = "2/3 + 3/4",
            graphExpr = "(3/4)*x"
        ),
        LessonModule(
            id = "alg_3",
            track = MathTrack.ALGEBRA,
            order = 3,
            title = "Linear Equations & Inequalities",
            summary = "Isolating first-degree variables, interval solutions, and sign reversal under negative scaling.",
            conceptExplanation = "A linear equation ax + b = 0 represents a straight line crossing the x-axis. Equivalent transformations preserve the solution set by applying the same invertible operation to both sides.",
            mathematicalIntuition = "Multiplying an inequality by -1 reflects the number line across 0, reversing left and right—which is why the inequality direction flips.",
            coreFormulas = listOf(
                "ax + b = 0  (a ≠ 0) ⟹ x = -b/a",
                "y - y₁ = m(x - x₁)  (Point-Slope)",
                "a < b and c < 0 ⟹ ac > bc"
            ),
            definitions = listOf(
                "Consistent Equation" to "Has at least one solution.",
                "Identity" to "True for all real values (0x = 0)."
            ),
            commonMistakes = listOf(
                "Forgetting to flip < to > when dividing both sides by a negative coefficient.",
                "Dividing by a variable expression without checking if it can equal zero."
            ),
            sandboxType = SandboxType.LINEAR_EXPLORER,
            sandboxTitle = "Slope & Intercept Explorer (y = mx + b)",
            sandboxExpression = "1.5*x - 3",
            workedExampleProblem = "Solve: 3(2x - 4) + 5 = 4x - 1",
            workedExampleSteps = listOf(
                "1. Distribute: 6x - 12 + 5 = 4x - 1 → 6x - 7 = 4x - 1",
                "2. Collect x terms: 2x - 7 = -1",
                "3. Isolate: 2x = 6 → x = 3"
            ),
            workedExampleAnswer = "x = 3",
            quickChecks = listOf(
                QuickCheckQuestion(
                    "Solve -3x + 5 ≥ 14.",
                    listOf("x ≤ -3", "x ≥ -3", "x ≤ 3", "x ≥ 3"),
                    0,
                    "Subtract 5: -3x ≥ 9. Dividing by -3 flips the inequality: x ≤ -3."
                ),
                QuickCheckQuestion(
                    "When does ax + b = 0 have no solution?",
                    listOf("a = 0 and b ≠ 0", "a = 0 and b = 0", "a ≠ 0 and b = 0", "a < 0"),
                    0,
                    "If a = 0 and b ≠ 0, the equation becomes b = 0, a contradiction."
                ),
                QuickCheckQuestion(
                    "What is the x-intercept of y = 4x - 10?",
                    listOf("2.5", "-10", "4", "-2.5"),
                    0,
                    "Set y = 0: 4x - 10 = 0 → x = 10/4 = 2.5."
                )
            ),
            calculatorTier = CalcTargetTier.ALGEBRA,
            sendToCalculatorExpr = "2*x - 6",
            graphExpr = "1.5*x - 3"
        ),
        LessonModule(
            id = "alg_4",
            track = MathTrack.ALGEBRA,
            order = 4,
            title = "Systems of Linear Equations",
            summary = "Geometric intersections, Gaussian elimination, determinants, and rank conditions.",
            conceptExplanation = "An n×n linear system Ax = b seeks the point(s) satisfying all equations simultaneously. Gaussian elimination transforms [A|b] into row-echelon form while preserving the exact solution set.",
            mathematicalIntuition = "In 2D, two lines either cross at one point (unique), run parallel (no solution), or coincide (infinitely many). Partial pivoting picks the largest magnitude pivot to avoid dividing by small floating-point errors.",
            coreFormulas = listOf(
                "det([a b; c d]) = ad - bc",
                "Cramer's Rule: x = (ed - bf)/(ad - bc)",
                "Ax = b ⟹ x = A⁻¹b  (when det(A) ≠ 0)"
            ),
            definitions = listOf(
                "Singular Matrix" to "A square matrix with determinant 0 (non-invertible).",
                "Row Echelon Form" to "Staircase pattern of leading pivots achieved by row swaps, scaling, and elimination."
            ),
            commonMistakes = listOf(
                "Assuming det(A) = 0 always means 'no solution'—consistent dependent systems have infinitely many.",
                "Forgetting to scale the right-hand side constant when multiplying a row."
            ),
            sandboxType = SandboxType.LINEAR_EXPLORER,
            sandboxTitle = "Intersecting Lines Visualizer",
            sandboxExpression = "2*x - 1",
            workedExampleProblem = "Solve: 2x + y = 7 and x - y = 2",
            workedExampleSteps = listOf(
                "1. Add both equations to eliminate y: (2x + x) + (y - y) = 7 + 2 → 3x = 9",
                "2. Solve for x: x = 3",
                "3. Back-substitute into x - y = 2: 3 - y = 2 → y = 1"
            ),
            workedExampleAnswer = "(3, 1)",
            quickChecks = listOf(
                QuickCheckQuestion(
                    "If two lines in ℝ² have identical slopes and different y-intercepts, the system is:",
                    listOf("Inconsistent (0 solutions)", "Consistent & independent (1 solution)", "Dependent (∞ solutions)", "Always trivial"),
                    0,
                    "Parallel non-overlapping lines never intersect, so the system has no solution."
                ),
                QuickCheckQuestion(
                    "What is det([[3, 2], [6, 4]])?",
                    listOf("0", "24", "12", "-12"),
                    0,
                    "ad - bc = 3×4 - 2×6 = 12 - 12 = 0; the rows are linearly dependent."
                ),
                QuickCheckQuestion(
                    "Solve x + y = 10 and x - y = 4.",
                    listOf("(7, 3)", "(6, 4)", "(8, 2)", "(3, 7)"),
                    0,
                    "Adding yields 2x = 14 → x = 7, then y = 10 - 7 = 3."
                )
            ),
            calculatorTier = CalcTargetTier.ALGEBRA,
            sendToCalculatorExpr = "2*x - 1",
            graphExpr = "2*x - 1"
        ),
        LessonModule(
            id = "alg_5",
            track = MathTrack.ALGEBRA,
            order = 5,
            title = "Polynomial Operations & Factoring",
            summary = "Degree, polynomial arithmetic, remainder theorem, and structural factoring patterns.",
            conceptExplanation = "A polynomial P(x) = Σ aₖxᵏ is closed under addition, subtraction, multiplication, differentiation, and integration. Factoring decomposes P(x) into lower-degree factors whose zeros reveal the roots of P.",
            mathematicalIntuition = "By the Factor Theorem, (x - r) divides P(x) cleanly if and only if P(r) = 0—meaning algebraic divisibility corresponds directly to geometric x-intercepts.",
            coreFormulas = listOf(
                "a² - b² = (a - b)(a + b)",
                "a³ ± b³ = (a ± b)(a² ∓ ab + b²)",
                "Remainder of P(x)/(x - r) is P(r)"
            ),
            definitions = listOf(
                "Degree" to "Highest exponent of x with a non-zero coefficient.",
                "Multiplicity" to "Exponent k of (x - r)ᵏ in the complete factorization."
            ),
            commonMistakes = listOf(
                "Factoring a² + b² over ℝ as (a + b)²—sums of squares do not factor into real linear terms.",
                "Dropping the middle term 2ab when expanding (a + b)²."
            ),
            sandboxType = SandboxType.QUADRATIC_EXPLORER,
            sandboxTitle = "Polynomial Factor & Root Explorer",
            sandboxExpression = "x^2 - 5*x + 6",
            workedExampleProblem = "Factor completely: 2x³ - 8x",
            workedExampleSteps = listOf(
                "1. Factor out the GCF 2x: 2x(x² - 4)",
                "2. Recognize difference of squares: x² - 4 = (x - 2)(x + 2)",
                "3. Complete factorization: 2x(x - 2)(x + 2)"
            ),
            workedExampleAnswer = "2x(x - 2)(x + 2)",
            quickChecks = listOf(
                QuickCheckQuestion(
                    "What is the remainder when P(x) = x³ - 2x + 5 is divided by (x - 2)?",
                    listOf("9", "5", "1", "-1"),
                    0,
                    "By the Remainder Theorem, the remainder equals P(2) = 2³ - 2(2) + 5 = 8 - 4 + 5 = 9."
                ),
                QuickCheckQuestion(
                    "Expand (2x - 3)².",
                    listOf("4x² - 12x + 9", "4x² - 9", "4x² + 9", "4x² - 6x + 9"),
                    0,
                    "(a - b)² = a² - 2ab + b² = 4x² - 2(2x)(3) + 9 = 4x² - 12x + 9."
                ),
                QuickCheckQuestion(
                    "What is the degree of (x² + 1)(x³ - 4x)?",
                    listOf("5", "6", "3", "2"),
                    0,
                    "Multiplying polynomials adds their leading degrees: 2 + 3 = 5."
                )
            ),
            calculatorTier = CalcTargetTier.ALGEBRA,
            sendToCalculatorExpr = "x^2 - 5*x + 6",
            graphExpr = "x^2 - 5*x + 6"
        ),
        LessonModule(
            id = "alg_6",
            track = MathTrack.ALGEBRA,
            order = 6,
            title = "Quadratic Equations & the Discriminant",
            summary = "Completing the square, quadratic formula, vertex form, and complex conjugate roots.",
            conceptExplanation = "Every quadratic ax² + bx + c = 0 (a ≠ 0) can be transformed by completing the square into a shifted pure square, yielding the universal quadratic formula.",
            mathematicalIntuition = "The parabola's vertex sits symmetrically at x = -b/(2a), and the two roots lie equally far left and right by √(b² - 4ac)/(2a). When b² - 4ac < 0, the parabola never touches the x-axis, so the offsets step into the imaginary axis ±i.",
            coreFormulas = listOf(
                "x = (-b ± √(b² - 4ac)) / (2a)",
                "Δ = b² - 4ac",
                "Sum of roots = -b/a,  Product = c/a"
            ),
            definitions = listOf(
                "Discriminant (Δ)" to "Determines root nature: Δ > 0 (2 real), Δ = 0 (1 repeated), Δ < 0 (2 complex conjugates).",
                "Vertex Form" to "a(x - h)² + k with extremum at (h, k)."
            ),
            commonMistakes = listOf(
                "Dividing only √Δ by 2a instead of the entire numerator (-b ± √Δ).",
                "Losing numerical precision when b² ≫ 4ac (use the Citardauq form c/q for the smaller root)."
            ),
            sandboxType = SandboxType.QUADRATIC_EXPLORER,
            sandboxTitle = "Parabola & Discriminant Sandbox",
            sandboxExpression = "x^2 - 4*x + 3",
            workedExampleProblem = "Solve x² - 6x + 13 = 0",
            workedExampleSteps = listOf(
                "1. Compute Δ = (-6)² - 4(1)(13) = 36 - 52 = -16",
                "2. Since Δ < 0, √Δ = 4i",
                "3. x = (6 ± 4i) / 2 = 3 ± 2i"
            ),
            workedExampleAnswer = "x = 3 + 2i, 3 - 2i",
            quickChecks = listOf(
                QuickCheckQuestion(
                    "For 2x² - 4x + k = 0 to have exactly one repeated real root, k must equal:",
                    listOf("2", "4", "1", "-2"),
                    0,
                    "Set Δ = 0: (-4)² - 4(2)(k) = 16 - 8k = 0 → k = 2."
                ),
                QuickCheckQuestion(
                    "What is the sum of the roots of 3x² - 12x + 7 = 0?",
                    listOf("4", "-4", "7/3", "12"),
                    0,
                    "By Vieta's formulas, r₁ + r₂ = -b/a = -(-12)/3 = 4."
                ),
                QuickCheckQuestion(
                    "Where is the vertex of y = 2(x - 3)² - 5?",
                    listOf("(3, -5)", "(-3, -5)", "(3, 5)", "(-3, 5)"),
                    0,
                    "Vertex form a(x - h)² + k directly places the vertex at (h, k) = (3, -5)."
                )
            ),
            calculatorTier = CalcTargetTier.ALGEBRA,
            sendToCalculatorExpr = "x^2 - 4*x + 3",
            graphExpr = "x^2 - 4*x + 3"
        ),
        LessonModule(
            id = "alg_7",
            track = MathTrack.ALGEBRA,
            order = 7,
            title = "Exponents & Logarithms",
            summary = "Exponential growth/decay, inverse relationship with logarithms, and change-of-base.",
            conceptExplanation = "An exponential function bˣ maps sums to products: b^(x+y) = bˣ·bʸ. Its inverse, log_b(x), maps products back to sums and is defined strictly for positive real arguments x > 0 (and b > 0, b ≠ 1).",
            mathematicalIntuition = "log_b(x) literally asks: 'What exponent must I put on base b to produce x?' Because bˣ is always strictly positive for real x, you can never take the real logarithm of 0 or a negative number.",
            coreFormulas = listOf(
                "log_b(xy) = log_b(x) + log_b(y)",
                "log_b(x^k) = k·log_b(x)",
                "log_b(x) = ln(x) / ln(b)"
            ),
            definitions = listOf(
                "Natural Logarithm (ln)" to "Logarithm base e ≈ 2.71828.",
                "Common Logarithm (log)" to "Logarithm base 10."
            ),
            commonMistakes = listOf(
                "Assuming ln(x + y) = ln(x) + ln(y)—logarithms split products, not sums.",
                "Forgetting to check domain restrictions (x > 0) after solving logarithmic equations."
            ),
            sandboxType = SandboxType.EXPONENTIAL_LOG,
            sandboxTitle = "Exponential Growth vs Decay Explorer",
            sandboxExpression = "2^x",
            workedExampleProblem = "Solve: 3^(2x - 1) = 27",
            workedExampleSteps = listOf(
                "1. Express 27 as a power of 3: 27 = 3³",
                "2. Equate exponents (since 3ˣ is one-to-one): 2x - 1 = 3",
                "3. Solve: 2x = 4 → x = 2"
            ),
            workedExampleAnswer = "x = 2",
            quickChecks = listOf(
                QuickCheckQuestion(
                    "Evaluate log₂(32) - log₂(4).",
                    listOf("3", "8", "4", "2"),
                    0,
                    "log₂(32/4) = log₂(8) = 3 (or 5 - 2 = 3)."
                ),
                QuickCheckQuestion(
                    "What is the domain of f(x) = ln(x - 3)?",
                    listOf("x > 3", "x ≥ 3", "x ≠ 3", "All real x"),
                    0,
                    "The argument of ln must be strictly positive: x - 3 > 0 → x > 3."
                ),
                QuickCheckQuestion(
                    "Simplify e^(2·ln(5)).",
                    listOf("25", "10", "5e²", "32"),
                    0,
                    "2·ln(5) = ln(5²) = ln(25), and e^(ln(25)) = 25."
                )
            ),
            calculatorTier = CalcTargetTier.STANDARD,
            sendToCalculatorExpr = "ln(25)/ln(5)",
            graphExpr = "exp(x)"
        ),
        LessonModule(
            id = "alg_8",
            track = MathTrack.ALGEBRA,
            order = 8,
            title = "Functions, Composition & Transformations",
            summary = "Domain & range, f(g(x)), invertibility, and geometric shifts/scaling of graphs.",
            conceptExplanation = "A function f: A → B assigns each input x ∈ A a unique output f(x) ∈ B. Composition (f ∘ g)(x) = f(g(x)) pipelines one function's output into another's input.",
            mathematicalIntuition = "Vertical transformations f(x) + k and a·f(x) behave directly on the output y, while horizontal transformations f(x - h) and f(bx) act inversely on x because x must compensate to reach the same inner value.",
            coreFormulas = listOf(
                "(f ∘ g)(x) = f(g(x))",
                "f⁻¹(f(x)) = x  (Horizontal Line Test)",
                "y = a·f(x - h) + k  (Shift right h, up k)"
            ),
            definitions = listOf(
                "One-to-One (Injective)" to "Distinct inputs yield distinct outputs; required for an inverse function to exist.",
                "Even / Odd Function" to "Even: f(-x) = f(x); Odd: f(-x) = -f(x)."
            ),
            commonMistakes = listOf(
                "Shifting f(x - 3) left instead of right (x = 3 gives the original 0 input).",
                "Assuming (f ∘ g)(x) equals (g ∘ f)(x)—function composition is not commutative."
            ),
            sandboxType = SandboxType.QUADRATIC_EXPLORER,
            sandboxTitle = "Transformations: a·(x - h)² + k",
            sandboxExpression = "(x - 2)^2 + 1",
            workedExampleProblem = "Given f(x) = x² + 1 and g(x) = 2x - 3, find f(g(2)).",
            workedExampleSteps = listOf(
                "1. Compute inner function first: g(2) = 2(2) - 3 = 1",
                "2. Feed result into outer function: f(1) = 1² + 1 = 2"
            ),
            workedExampleAnswer = "2",
            quickChecks = listOf(
                QuickCheckQuestion(
                    "If f(x) = x² and g(x) = x + 3, what is (f ∘ g)(x)?",
                    listOf("(x + 3)²", "x² + 3", "x² + 9", "2x + 3"),
                    0,
                    "(f ∘ g)(x) = f(g(x)) = f(x + 3) = (x + 3)²."
                ),
                QuickCheckQuestion(
                    "Compared to y = √x, the graph of y = √(x + 4) - 2 is shifted:",
                    listOf("4 left, 2 down", "4 right, 2 down", "4 left, 2 up", "4 right, 2 up"),
                    0,
                    "x + 4 = x - (-4) shifts 4 units left, and -2 shifts 2 units down."
                ),
                QuickCheckQuestion(
                    "Find the inverse of f(x) = 3x - 6.",
                    listOf("(x + 6)/3", "(x - 6)/3", "3x + 6", "1/(3x - 6)"),
                    0,
                    "Swap x and y: x = 3y - 6 → 3y = x + 6 → y = (x + 6)/3."
                )
            ),
            calculatorTier = CalcTargetTier.ALGEBRA,
            sendToCalculatorExpr = "(x - 2)^2 + 1",
            graphExpr = "(x - 2)^2 + 1"
        )
    )
}
