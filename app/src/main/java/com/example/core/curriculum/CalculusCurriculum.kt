package com.example.core.curriculum

object CalculusCurriculum {
    val modules: List<LessonModule> = listOf(
        LessonModule(
            id = "calc_1",
            track = MathTrack.CALCULUS,
            order = 1,
            title = "Functions & Graphs as Calculus Foundations",
            summary = "Average rate of change, piecewise behavior, asymptotes, and domain singularities.",
            conceptExplanation = "Calculus studies how quantities change continuously. Before differentiating or integrating, we analyze function behavior: domain boundaries, zeros, vertical/horizontal asymptotes, and average rates of change [f(b) - f(a)]/(b - a).",
            mathematicalIntuition = "A secant line's slope gives the average speed between two times; recognizing where denominators hit zero prevents graphing artifacts and false derivatives.",
            coreFormulas = listOf(
                "Average Rate of Change = (f(b) - f(a)) / (b - a)",
                "Difference Quotient = (f(x + h) - f(x)) / h",
                "Vertical asymptote where denominator → 0 and numerator ≠ 0"
            ),
            definitions = listOf(
                "Secant Line" to "A line connecting two distinct points (a, f(a)) and (b, f(b)) on a curve.",
                "Asymptote" to "A line that a curve approaches arbitrarily closely in the limit."
            ),
            commonMistakes = listOf(
                "Connecting points across a vertical asymptote when plotting.",
                "Assuming a hole (removable discontinuity) is a vertical asymptote."
            ),
            sandboxType = SandboxType.SECANT_TANGENT,
            sandboxTitle = "Average vs Instantaneous Rate Explorer",
            sandboxExpression = "x^2",
            workedExampleProblem = "Compute the average rate of change of f(x) = x³ - x on [1, 3].",
            workedExampleSteps = listOf(
                "1. Evaluate endpoints: f(1) = 1 - 1 = 0, f(3) = 27 - 3 = 24",
                "2. Divide change in output by change in input: (24 - 0) / (3 - 1) = 24 / 2 = 12"
            ),
            workedExampleAnswer = "12",
            quickChecks = listOf(
                QuickCheckQuestion(
                    "For f(x) = (x² - 9)/(x - 3), what occurs at x = 3?",
                    listOf("Removable discontinuity (hole) at (3, 6)", "Vertical asymptote", "Horizontal asymptote", "Continuous value 0"),
                    0,
                    "(x² - 9)/(x - 3) = x + 3 for x ≠ 3, so the graph approaches 6 from both sides with a hole at x = 3."
                ),
                QuickCheckQuestion(
                    "What is the average rate of change of f(x) = 2x² on [1, 4]?",
                    listOf("10", "30", "15", "8"),
                    0,
                    "(2(16) - 2(1)) / (4 - 1) = (32 - 2)/3 = 10."
                ),
                QuickCheckQuestion(
                    "As x → ∞, f(x) = (3x² + 1)/(x² - 5) approaches:",
                    listOf("3", "0", "∞", "-1/5"),
                    0,
                    "Dividing numerator and denominator by x² gives (3 + 1/x²)/(1 - 5/x²) → 3."
                )
            ),
            calculatorTier = CalcTargetTier.CALCULUS,
            sendToCalculatorExpr = "x^3 - x",
            graphExpr = "(x^2 - 4)/(x - 2)"
        ),
        LessonModule(
            id = "calc_2",
            track = MathTrack.CALCULUS,
            order = 2,
            title = "Limits & Continuity",
            summary = "Left/right limits, indeterminate forms 0/0, continuity criteria, and numerical caveats.",
            conceptExplanation = "lim_{x→a} f(x) = L describes what f(x) approaches near a, independent of f(a). Continuity at a requires three conditions: f(a) is defined, the two-sided limit exists, and lim_{x→a} f(x) = f(a).",
            mathematicalIntuition = "A limit inspects the neighborhood around x = a without stepping directly onto x = a. Numerical tables reveal trends, though formal algebra (factoring, conjugates, squeeze theorem) is needed to prove exact limits.",
            coreFormulas = listOf(
                "lim_{x→a} f(x) = L ⟺ lim_{x→a⁻} f(x) = lim_{x→a⁺} f(x) = L",
                "lim_{x→0} sin(x)/x = 1  (radians)",
                "lim_{x→0} (1 - cos(x))/x = 0"
            ),
            definitions = listOf(
                "Indeterminate Form (0/0)" to "Both numerator and denominator approach 0; limit may be finite, 0, or ∞.",
                "Jump Discontinuity" to "Left-hand and right-hand limits both exist finitely but disagree."
            ),
            commonMistakes = listOf(
                "Trusting a handful of floating-point samples near x = a when catastrophic cancellation occurs.",
                "Concluding a limit is 0 merely because the numerator approaches 0."
            ),
            sandboxType = SandboxType.SECANT_TANGENT,
            sandboxTitle = "Limit & Difference Quotient Approach Lab",
            sandboxExpression = "sin(x)/x",
            workedExampleProblem = "Evaluate lim_{x→4} (√x - 2) / (x - 4)",
            workedExampleSteps = listOf(
                "1. Direct substitution yields 0/0 (indeterminate)",
                "2. Multiply by conjugate (√x + 2): (x - 4) / ((x - 4)(√x + 2))",
                "3. Cancel (x - 4) for x ≠ 4: 1 / (√x + 2)",
                "4. Substitute x = 4: 1 / (2 + 2) = 1/4"
            ),
            workedExampleAnswer = "1/4",
            quickChecks = listOf(
                QuickCheckQuestion(
                    "If lim_{x→2⁻} f(x) = 5 and lim_{x→2⁺} f(x) = -1, then lim_{x→2} f(x):",
                    listOf("Does not exist", "Equals 2", "Equals 5", "Equals -1"),
                    0,
                    "Left and right limits disagree (5 ≠ -1), so the two-sided limit does not exist."
                ),
                QuickCheckQuestion(
                    "Evaluate lim_{x→0} sin(3x) / x (in radians).",
                    listOf("3", "1", "0", "1/3"),
                    0,
                    "Rewrite as 3 · [sin(3x)/(3x)] → 3 · 1 = 3."
                ),
                QuickCheckQuestion(
                    "Which condition is NOT required for f to be continuous at a?",
                    listOf("f is differentiable at a", "f(a) is defined", "lim_{x→a} f(x) exists", "lim_{x→a} f(x) = f(a)"),
                    0,
                    "Functions like |x| are continuous at 0 without being differentiable there."
                )
            ),
            calculatorTier = CalcTargetTier.CALCULUS,
            sendToCalculatorExpr = "sin(x)/x",
            graphExpr = "sin(x)/x"
        ),
        LessonModule(
            id = "calc_3",
            track = MathTrack.CALCULUS,
            order = 3,
            title = "Definition of the Derivative",
            summary = "Secant slope limit, instantaneous rate of change, tangent line equation, and differentiability.",
            conceptExplanation = "The derivative f'(x) is defined as lim_{h→0} [f(x + h) - f(x)] / h. Geometrically it is the slope of the tangent line at x; physically it is the instantaneous rate of change.",
            mathematicalIntuition = "Zooming infinitely far into any smooth curve makes it look like a straight line. The secant line through x and x + h locks onto that local straight line as h → 0.",
            coreFormulas = listOf(
                "f'(x) = lim_{h→0} (f(x + h) - f(x)) / h",
                "Tangent Line at x₀: y = f(x₀) + f'(x₀)(x - x₀)",
                "Differentiable ⟹ Continuous (converse is false)"
            ),
            definitions = listOf(
                "Derivative" to "Linear best approximation of change per unit input at a point.",
                "Corner / Cusp" to "A point where left and right derivatives differ, making f'(x) undefined."
            ),
            commonMistakes = listOf(
                "Using a large step h in numerical differentiation without checking truncation error.",
                "Forgetting that f'(x₀) is a slope, not the entire tangent line equation."
            ),
            sandboxType = SandboxType.SECANT_TANGENT,
            sandboxTitle = "Interactive Secant → Tangent Slider",
            sandboxExpression = "0.5*x^2",
            workedExampleProblem = "Using the limit definition, find f'(x) for f(x) = x² + 3x.",
            workedExampleSteps = listOf(
                "1. f(x + h) = (x + h)² + 3(x + h) = x² + 2xh + h² + 3x + 3h",
                "2. f(x + h) - f(x) = 2xh + h² + 3h",
                "3. Divide by h: 2x + h + 3",
                "4. Take h → 0: f'(x) = 2x + 3"
            ),
            workedExampleAnswer = "f'(x) = 2x + 3",
            quickChecks = listOf(
                QuickCheckQuestion(
                    "Find the tangent line slope to y = x³ at x = 2.",
                    listOf("12", "8", "6", "4"),
                    0,
                    "y' = 3x², so at x = 2 the slope is 3(2²) = 12."
                ),
                QuickCheckQuestion(
                    "Why is f(x) = |x| NOT differentiable at x = 0?",
                    listOf("Left slope (-1) ≠ right slope (+1)", "It is discontinuous at 0", "f(0) is undefined", "It has a vertical asymptote"),
                    0,
                    "At x = 0, the graph has a sharp corner where the left derivative is -1 and right derivative is +1."
                ),
                QuickCheckQuestion(
                    "If f(2) = 5 and f'(2) = -3, what is the tangent line equation at x = 2?",
                    listOf("y = -3x + 11", "y = -3x + 5", "y = 5x - 3", "y = -3x - 1"),
                    0,
                    "y - 5 = -3(x - 2) → y = -3x + 6 + 5 = -3x + 11."
                )
            ),
            calculatorTier = CalcTargetTier.CALCULUS,
            sendToCalculatorExpr = "x^2 + 3*x",
            graphExpr = "x^2"
        ),
        LessonModule(
            id = "calc_4",
            track = MathTrack.CALCULUS,
            order = 4,
            title = "Differentiation Rules",
            summary = "Power, product, quotient, chain, exponential, logarithmic, and trigonometric derivatives.",
            conceptExplanation = "Symbolic differentiation applies structural rules recursively on an expression's AST: linearity, power rule, product rule (u'v + uv'), quotient rule, and the chain rule for nested compositions f(g(x)).",
            mathematicalIntuition = "The chain rule multiplies sensitivity rates like linked gears: if y turns 3× faster than u and u turns 2× faster than x, then y turns 3×2 = 6× faster than x.",
            coreFormulas = listOf(
                "(u·v)' = u'v + uv'",
                "(u/v)' = (u'v - uv') / v²",
                "(f(g(x)))' = f'(g(x)) · g'(x)"
            ),
            definitions = listOf(
                "Chain Rule" to "Differentiates composite functions by multiplying outer derivative evaluated at inner by inner derivative.",
                "Higher-Order Derivative" to "f''(x) = d/dx[f'(x)] measures concavity / acceleration."
            ),
            commonMistakes = listOf(
                "Assuming (f·g)' = f'·g' (forgetting the cross terms).",
                "Forgetting to multiply by g'(x) in the chain rule, e.g., differentiating sin(3x) as cos(3x) instead of 3cos(3x)."
            ),
            sandboxType = SandboxType.SECANT_TANGENT,
            sandboxTitle = "Slope & Chain Rule Visualizer",
            sandboxExpression = "sin(2*x)",
            workedExampleProblem = "Differentiate f(x) = x² · sin(3x)",
            workedExampleSteps = listOf(
                "1. Apply Product Rule with u = x², v = sin(3x)",
                "2. u' = 2x",
                "3. By Chain Rule, v' = cos(3x) · 3 = 3cos(3x)",
                "4. f'(x) = 2x·sin(3x) + 3x²·cos(3x)"
            ),
            workedExampleAnswer = "2x·sin(3x) + 3x²·cos(3x)",
            quickChecks = listOf(
                QuickCheckQuestion(
                    "What is d/dx [ln(x² + 1)]?",
                    listOf("2x / (x² + 1)", "1 / (x² + 1)", "2x · ln(x² + 1)", "1 / (2x)"),
                    0,
                    "Chain rule: (1 / (x² + 1)) · d/dx(x² + 1) = 2x / (x² + 1)."
                ),
                QuickCheckQuestion(
                    "What is d/dx [e^(5x)]?",
                    listOf("5e^(5x)", "e^(5x)", "5x·e^(5x-1)", "e^(5x)/5"),
                    0,
                    "d/dx(e^u) = e^u · u', where u = 5x and u' = 5."
                ),
                QuickCheckQuestion(
                    "Differentiate f(x) = (2x - 1)⁴.",
                    listOf("8(2x - 1)³", "4(2x - 1)³", "2(2x - 1)³", "8(2x - 1)⁴"),
                    0,
                    "Power + chain rule: 4(2x - 1)³ · 2 = 8(2x - 1)³."
                )
            ),
            calculatorTier = CalcTargetTier.CALCULUS,
            sendToCalculatorExpr = "x^2 * sin(3*x)",
            graphExpr = "sin(2*x)"
        ),
        LessonModule(
            id = "calc_5",
            track = MathTrack.CALCULUS,
            order = 5,
            title = "Applications of Derivatives",
            summary = "Critical points, local extrema, concavity, optimization, and Newton-Raphson root finding.",
            conceptExplanation = "Where f'(x) = 0 or is undefined, the tangent is horizontal or singular—marking potential local maxima or minima. The sign of f''(x) classifies curvature (f'' > 0 concave up → local min; f'' < 0 concave down → local max).",
            mathematicalIntuition = "Newton's method slides down the tangent line at xₙ straight to where it hits the x-axis, giving quadratic convergence near simple roots.",
            coreFormulas = listOf(
                "Critical Point: f'(c) = 0 or undefined",
                "Second Derivative Test: f'(c)=0, f''(c)>0 ⟹ local min",
                "Newton-Raphson: x_{n+1} = x_n - f(x_n) / f'(x_n)"
            ),
            definitions = listOf(
                "Inflection Point" to "Point where concavity changes sign (f'' switches +/−).",
                "Global Extremum" to "Overall highest or lowest value on the domain/interval."
            ),
            commonMistakes = listOf(
                "Assuming every point with f'(c) = 0 is a max or min (e.g., x³ at 0 is a saddle inflection).",
                "Running Newton's method from a point where f'(x₀) ≈ 0."
            ),
            sandboxType = SandboxType.QUADRATIC_EXPLORER,
            sandboxTitle = "Extrema & Tangent Optimization Lab",
            sandboxExpression = "x^3 - 3*x",
            workedExampleProblem = "Find and classify the critical points of f(x) = x³ - 3x.",
            workedExampleSteps = listOf(
                "1. Compute f'(x) = 3x² - 3 = 3(x² - 1) = 0 → x = -1, x = 1",
                "2. Compute f''(x) = 6x",
                "3. At x = -1: f''(-1) = -6 < 0 → Local maximum (-1, 2)",
                "4. At x = 1: f''(1) = +6 > 0 → Local minimum (1, -2)"
            ),
            workedExampleAnswer = "Local Max (-1, 2), Local Min (1, -2)",
            quickChecks = listOf(
                QuickCheckQuestion(
                    "If f'(c) = 0 and f''(c) = -4, then at x = c the function has:",
                    listOf("A local maximum", "A local minimum", "An asymptote", "Zero value"),
                    0,
                    "f''(c) < 0 means the graph curves downward (concave down), so a horizontal tangent is a peak."
                ),
                QuickCheckQuestion(
                    "Using one Newton-Raphson step on f(x) = x² - 2 starting at x₀ = 1:",
                    listOf("1.5", "1.4", "2.0", "1.25"),
                    0,
                    "x₁ = 1 - (1² - 2)/(2·1) = 1 - (-1)/2 = 1.5."
                ),
                QuickCheckQuestion(
                    "A rectangle has perimeter 40. What dimensions maximize its area?",
                    listOf("10 × 10", "15 × 5", "12 × 8", "18 × 2"),
                    0,
                    "A(x) = x(20 - x) = 20x - x² → A'(x) = 20 - 2x = 0 → x = 10, y = 10."
                )
            ),
            calculatorTier = CalcTargetTier.CALCULUS,
            sendToCalculatorExpr = "x^3 - 3*x",
            graphExpr = "x^3 - 3*x"
        ),
        LessonModule(
            id = "calc_6",
            track = MathTrack.CALCULUS,
            order = 6,
            title = "Definite & Indefinite Integrals",
            summary = "Riemann sums, net signed area, Fundamental Theorem of Calculus, and Adaptive Simpson quadrature.",
            conceptExplanation = "The definite integral ∫ₐᵇ f(x) dx is the limit of Riemann sums accumulating net signed area between f(x) and the x-axis. The Fundamental Theorem of Calculus links area directly to antiderivatives: ∫ₐᵇ f(x) dx = F(b) - F(a).",
            mathematicalIntuition = "Differentiation slices distance into velocity; integration sums velocity over tiny dt slices back into total displacement.",
            coreFormulas = listOf(
                "∫ xⁿ dx = xⁿ⁺¹ / (n + 1) + C  (n ≠ -1)",
                "∫ₐᵇ f(x) dx = F(b) - F(a)  (FTC Part 2)",
                "Simpson's Rule: (b-a)/6 · [f(a) + 4f(m) + f(b)]"
            ),
            definitions = listOf(
                "Signed Area" to "Regions above the x-axis contribute positively; regions below contribute negatively.",
                "Antiderivative" to "A function F(x) whose derivative equals f(x)."
            ),
            commonMistakes = listOf(
                "Forgetting '+ C' in indefinite integrals.",
                "Assuming ∫ₐᵇ f(x) dx gives geometric area when f(x) dips below the x-axis."
            ),
            sandboxType = SandboxType.INTEGRAL_SHADE,
            sandboxTitle = "Interactive Area Under Curve Lab",
            sandboxExpression = "x^2 + 1",
            workedExampleProblem = "Evaluate ∫₀² (3x² + 1) dx",
            workedExampleSteps = listOf(
                "1. Find antiderivative: F(x) = x³ + x",
                "2. Evaluate F(2) = 2³ + 2 = 10",
                "3. Evaluate F(0) = 0³ + 0 = 0",
                "4. F(2) - F(0) = 10 - 0 = 10"
            ),
            workedExampleAnswer = "10",
            quickChecks = listOf(
                QuickCheckQuestion(
                    "Compute ∫₁ᵉ (1/x) dx.",
                    listOf("1", "0", "e", "ln(e) - e"),
                    0,
                    "Antiderivative is ln|x|. Thus ln(e) - ln(1) = 1 - 0 = 1."
                ),
                QuickCheckQuestion(
                    "What is ∫₀^(2π) sin(x) dx?",
                    listOf("0", "2", "4", "-2"),
                    0,
                    "The positive lobe on [0, π] (+2) cancels the negative lobe on [π, 2π] (-2), giving net signed area 0."
                ),
                QuickCheckQuestion(
                    "If F(x) = ∫₀ˣ cos(t) dt, what is F'(x)?",
                    listOf("cos(x)", "sin(x)", "-sin(x)", "cos(x) - 1"),
                    0,
                    "By FTC Part 1, d/dx ∫ₐˣ f(t) dt = f(x) = cos(x)."
                )
            ),
            calculatorTier = CalcTargetTier.CALCULUS,
            sendToCalculatorExpr = "3*x^2 + 1",
            graphExpr = "x^2 + 1"
        ),
        LessonModule(
            id = "calc_7",
            track = MathTrack.CALCULUS,
            order = 7,
            title = "Integration Techniques & Numerical Quadrature",
            summary = "u-Substitution, integration by parts, and when numerical quadrature (Adaptive Simpson) is required.",
            conceptExplanation = "Not every elementary function has an elementary closed-form antiderivative (e.g., e^(-x²), sin(x)/x). When symbolic substitution or integration by parts does not apply, adaptive numerical quadrature achieves high precision on definite integrals.",
            mathematicalIntuition = "u-Substitution reverses the chain rule, integration by parts reverses the product rule, and Adaptive Simpson subdivides intervals wherever curvature is steep.",
            coreFormulas = listOf(
                "u-Sub: ∫ f(g(x))·g'(x) dx = ∫ f(u) du",
                "By Parts: ∫ u dv = u·v - ∫ v du",
                "Non-elementary examples: ∫ e^(-x²) dx, ∫ sin(x)/x dx"
            ),
            definitions = listOf(
                "Quadrature" to "Numerical approximation of a definite integral using weighted function samples.",
                "LIATE Heuristic" to "Priority for choosing u in integration by parts: Log, Inverse trig, Algebraic, Trig, Exp."
            ),
            commonMistakes = listOf(
                "Changing variables to u without transforming the limits a, b or the dx differential.",
                "Expecting every smooth function to have a closed-form symbolic antiderivative."
            ),
            sandboxType = SandboxType.INTEGRAL_SHADE,
            sandboxTitle = "Gaussian e^(-x²) Numerical Integral Sandbox",
            sandboxExpression = "exp(-x^2)",
            workedExampleProblem = "Evaluate ∫ x·e^x dx",
            workedExampleSteps = listOf(
                "1. Choose u = x (du = dx) and dv = e^x dx (v = e^x)",
                "2. Apply ∫ u dv = uv - ∫ v du = x·e^x - ∫ e^x dx",
                "3. Result: x·e^x - e^x + C = e^x(x - 1) + C"
            ),
            workedExampleAnswer = "e^x(x - 1) + C",
            quickChecks = listOf(
                QuickCheckQuestion(
                    "In ∫ 2x·cos(x²) dx, the best substitution is:",
                    listOf("u = x², du = 2x dx", "u = cos(x²)", "u = 2x", "u = sin(x)"),
                    0,
                    "u = x² turns 2x dx into du, yielding ∫ cos(u) du = sin(x²) + C."
                ),
                QuickCheckQuestion(
                    "Which definite integral has NO elementary closed-form antiderivative?",
                    listOf("∫₀¹ exp(-x²) dx", "∫₀¹ x·exp(-x²) dx", "∫₀¹ cos(2x) dx", "∫₁² 1/x dx"),
                    0,
                    "e^(-x²) has no elementary antiderivative (defines erf), so numerical quadrature is the right tool."
                ),
                QuickCheckQuestion(
                    "Compute ∫ ln(x) dx using integration by parts (u = ln x, dv = dx).",
                    listOf("x·ln(x) - x + C", "1/x + C", "x·ln(x) + C", "ln(x)²/2 + C"),
                    0,
                    "uv - ∫ v du = x·ln(x) - ∫ x·(1/x) dx = x·ln(x) - x + C."
                )
            ),
            calculatorTier = CalcTargetTier.CALCULUS,
            sendToCalculatorExpr = "exp(-x^2)",
            graphExpr = "exp(-x^2)"
        ),
        LessonModule(
            id = "calc_8",
            track = MathTrack.CALCULUS,
            order = 8,
            title = "Series & Taylor/Maclaurin Expansions",
            summary = "Approximating smooth functions with polynomials built from higher-order derivatives.",
            conceptExplanation = "A Taylor series centered at x = a matches f(a), f'(a), f''(a), ..., f^(n)(a) by setting the k-th polynomial coefficient to f^(k)(a) / k!. Adding higher-degree terms widens the region of accurate approximation.",
            mathematicalIntuition = "A tangent line matches height and slope at a point; adding the quadratic term matches bend (concavity), cubic matches rate of bend, and so on—sculpting a polynomial that hugs the true curve.",
            coreFormulas = listOf(
                "T_n(x) = Σ_{k=0}^n [f^(k)(a) / k!] · (x - a)^k",
                "e^x = 1 + x + x²/2! + x³/3! + ...",
                "sin(x) = x - x³/3! + x⁵/5! - ..."
            ),
            definitions = listOf(
                "Maclaurin Series" to "A Taylor series centered specifically at a = 0.",
                "Truncation Error" to "Remainder |f(x) - T_n(x)| from stopping at degree n."
            ),
            commonMistakes = listOf(
                "Forgetting the k! denominator in f^(k)(a)/k!.",
                "Using a low-degree Maclaurin polynomial far away from x = 0 where error grows rapidly."
            ),
            sandboxType = SandboxType.TAYLOR_APPROX,
            sandboxTitle = "Interactive Taylor Polynomial Order Lab",
            sandboxExpression = "sin(x)",
            workedExampleProblem = "Find the 3rd-degree Maclaurin polynomial for e^(2x).",
            workedExampleSteps = listOf(
                "1. Derivatives at a = 0: f(0)=1, f'(0)=2, f''(0)=4, f'''(0)=8",
                "2. Divide by k!: 1/0! = 1, 2/1! = 2, 4/2! = 2, 8/3! = 4/3",
                "3. P₃(x) = 1 + 2x + 2x² + (4/3)x³"
            ),
            workedExampleAnswer = "1 + 2x + 2x² + (4/3)x³",
            quickChecks = listOf(
                QuickCheckQuestion(
                    "What is the coefficient of x³ in the Maclaurin series of sin(x)?",
                    listOf("-1/6", "1/6", "-1/3", "1/3"),
                    0,
                    "f'''(0) = -cos(0) = -1, divided by 3! = 6 yields -1/6."
                ),
                QuickCheckQuestion(
                    "What is the 2nd-degree Taylor polynomial for cos(x) centered at 0?",
                    listOf("1 - x²/2", "1 + x²/2", "x - x²/2", "1 - x"),
                    0,
                    "cos(0)=1, -sin(0)=0, -cos(0)=-1 → 1 + 0x - (1/2!)x² = 1 - x²/2."
                ),
                QuickCheckQuestion(
                    "Increasing Taylor degree n near the center a generally:",
                    listOf("Reduces local approximation error", "Moves the center away from a", "Always makes the polynomial linear", "Eliminates the 0! term"),
                    0,
                    "Within the radius of convergence, higher-degree Taylor polynomials match more derivatives and reduce error."
                )
            ),
            calculatorTier = CalcTargetTier.CALCULUS,
            sendToCalculatorExpr = "sin(x)",
            graphExpr = "sin(x)"
        )
    )
}
