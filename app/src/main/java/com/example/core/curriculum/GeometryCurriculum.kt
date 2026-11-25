package com.example.core.curriculum

object GeometryCurriculum {
    val modules: List<LessonModule> = listOf(
        LessonModule(
            id = "geo_1",
            track = MathTrack.GEOMETRY,
            order = 1,
            title = "Points, Lines, Angles & Basic Definitions",
            summary = "Euclidean axioms, parallel lines with transversals, and complementary/supplementary angle pairs.",
            conceptExplanation = "Euclidean geometry builds from undefined primitives (point, line, plane) and axioms governing distance and angle measure. When a transversal cuts parallel lines, corresponding and alternate interior angles are congruent.",
            mathematicalIntuition = "Rotating a ray around a vertex sweeps out 360° for a full turn; straight lines span 180°, so any linear pair shares a straight angle and sums to 180°.",
            coreFormulas = listOf(
                "Complementary: α + β = 90°",
                "Supplementary: α + β = 180°",
                "Vertical angles are always congruent (α = γ)"
            ),
            definitions = listOf(
                "Collinear Points" to "Points lying on a single straight line.",
                "Transversal" to "A line intersecting two or more coplanar lines at distinct points."
            ),
            commonMistakes = listOf(
                "Assuming lines look parallel in a diagram without explicit parallel markers or equal alternate angles.",
                "Confusing complementary (90°) with supplementary (180°)."
            ),
            sandboxType = SandboxType.UNIT_CIRCLE,
            sandboxTitle = "Interactive Angle & Radian Explorer",
            sandboxExpression = "sin(x)",
            workedExampleProblem = "Two supplementary angles are in ratio 2:7. Find both angles.",
            workedExampleSteps = listOf(
                "1. Let the angles be 2k and 7k",
                "2. Supplementary sum: 2k + 7k = 180° → 9k = 180°",
                "3. k = 20°, so angles are 40° and 140°"
            ),
            workedExampleAnswer = "40° and 140°",
            quickChecks = listOf(
                QuickCheckQuestion(
                    "An angle is 28° larger than its complement. What is the angle?",
                    listOf("59°", "31°", "62°", "104°"),
                    0,
                    "x + (x - 28°) = 90° → 2x = 118° → x = 59°."
                ),
                QuickCheckQuestion(
                    "If two parallel lines are cut by a transversal, same-side interior angles are:",
                    listOf("Supplementary (sum 180°)", "Complementary (sum 90°)", "Always 90°", "Congruent only if acute"),
                    0,
                    "Same-side interior angles form a linear pair with an alternate/corresponding angle, summing to 180°."
                ),
                QuickCheckQuestion(
                    "Convert 150° to radians.",
                    listOf("5π/6", "3π/4", "2π/3", "7π/6"),
                    0,
                    "150° × (π / 180°) = 15π/18 = 5π/6."
                )
            ),
            calculatorTier = CalcTargetTier.GEOMETRY,
            sendToCalculatorExpr = "150 * pi / 180",
            graphExpr = "sin(x)"
        ),
        LessonModule(
            id = "geo_2",
            track = MathTrack.GEOMETRY,
            order = 2,
            title = "Triangle Classification & Properties",
            summary = "Triangle inequality, interior/exterior angle theorems, Heron's area, and special centers.",
            conceptExplanation = "A triangle is the simplest rigid polygon: three side lengths uniquely determine its three angles. Every triangle obeys the Triangle Inequality (a + b > c) and its interior angles sum to exactly 180° in Euclidean space.",
            mathematicalIntuition = "If two sides of lengths 3 and 4 tried to bridge a third side of length 8, they couldn't reach each other even stretched flat—so a + b must strictly exceed c.",
            coreFormulas = listOf(
                "A + B + C = 180°",
                "Triangle Inequality: a + b > c",
                "Heron's Formula: Area = √(s(s-a)(s-b)(s-c)),  s = (a+b+c)/2"
            ),
            definitions = listOf(
                "Incenter" to "Intersection of angle bisectors; center of the inscribed circle.",
                "Circumcenter" to "Intersection of perpendicular bisectors; center of the circumscribed circle."
            ),
            commonMistakes = listOf(
                "Testing only one pair in the triangle inequality instead of ensuring the two shorter sides exceed the longest.",
                "Confusing side length with angle measure when computing area."
            ),
            sandboxType = SandboxType.TRIANGLE_INTERACTIVE,
            sandboxTitle = "Interactive Triangle Geometry Lab",
            sandboxExpression = "sqrt(6*(6-3)*(6-4)*(6-5))",
            workedExampleProblem = "Find the area of a triangle with sides 5, 6, and 7.",
            workedExampleSteps = listOf(
                "1. Semiperimeter s = (5 + 6 + 7)/2 = 9",
                "2. Area = √(9 × (9-5) × (9-6) × (9-7)) = √(9 × 4 × 3 × 2) = √216",
                "3. √216 = 6√6 ≈ 14.6969"
            ),
            workedExampleAnswer = "6√6 ≈ 14.70",
            quickChecks = listOf(
                QuickCheckQuestion(
                    "Which side triple CANNOT form a non-degenerate triangle?",
                    listOf("4, 5, 9", "5, 6, 10", "7, 7, 13", "3, 4, 5"),
                    0,
                    "4 + 5 = 9 does not strictly exceed 9; the points collapse onto a straight segment (area = 0)."
                ),
                QuickCheckQuestion(
                    "In a triangle, two angles are 48° and 67°. What is the third angle?",
                    listOf("65°", "75°", "55°", "115°"),
                    0,
                    "180° - (48° + 67°) = 180° - 115° = 65°."
                ),
                QuickCheckQuestion(
                    "What is the exterior angle adjacent to a 52° interior vertex?",
                    listOf("128°", "38°", "52°", "142°"),
                    0,
                    "An interior and adjacent exterior angle form a linear pair: 180° - 52° = 128°."
                )
            ),
            calculatorTier = CalcTargetTier.GEOMETRY,
            sendToCalculatorExpr = "5,6,7",
            graphExpr = "sqrt(max(0, 25 - x^2))"
        ),
        LessonModule(
            id = "geo_3",
            track = MathTrack.GEOMETRY,
            order = 3,
            title = "Congruence & Triangle Proofs",
            summary = "SSS, SAS, ASA, AAS, HL congruence criteria and why SSA can be ambiguous.",
            conceptExplanation = "Two triangles are congruent (≅) if rigid motions map one exactly onto the other. SSS, SAS, ASA, AAS, and Right-Hypotenuse-Leg (HL) guarantee uniqueness, whereas AAA only determines shape (similarity) and SSA can produce two distinct triangles.",
            mathematicalIntuition = "In SSA, swinging the known opposite side like a pendulum from the top vertex can strike the base ray in two places when b·sin(A) < a < b—creating one acute and one obtuse solution.",
            coreFormulas = listOf(
                "Valid Congruence: SSS, SAS, ASA, AAS, HL",
                "Ambiguous (SSA): 0, 1, or 2 triangles depending on a vs b·sin(A)"
            ),
            definitions = listOf(
                "CPCTC" to "Corresponding Parts of Congruent Triangles are Congruent.",
                "Included Angle" to "The angle formed directly between two specified sides."
            ),
            commonMistakes = listOf(
                "Using SSA as a universal congruence theorem without checking for the obtuse second solution.",
                "Assuming AAA proves congruence instead of similarity."
            ),
            sandboxType = SandboxType.TRIANGLE_INTERACTIVE,
            sandboxTitle = "SAS / SSS Uniqueness Lab",
            sandboxExpression = "sqrt(5^2 + 7^2 - 2*5*7*cos(60))",
            workedExampleProblem = "Given a = 5, b = 7, A = 35° (SSA), how many triangles exist?",
            workedExampleSteps = listOf(
                "1. Compute altitude h = b·sin(A) = 7·sin(35°) ≈ 4.015",
                "2. Compare a against h and b: 4.015 < 5 < 7 (h < a < b)",
                "3. Because h < a < b, two distinct valid triangles exist."
            ),
            workedExampleAnswer = "2 triangles (Ambiguous case)",
            quickChecks = listOf(
                QuickCheckQuestion(
                    "Which criterion does NOT guarantee triangle congruence?",
                    listOf("AAA", "ASA", "SAS", "SSS"),
                    0,
                    "Equilateral triangles of side 2 and side 10 have identical angles (60°, 60°, 60°) yet different sizes."
                ),
                QuickCheckQuestion(
                    "In right triangles, knowing the hypotenuse and one leg (HL) guarantees congruence because:",
                    listOf("Pythagorean theorem fixes the third side (SSS)", "All right triangles are similar", "Angles are all 45°", "Area is fixed"),
                    0,
                    "a = √(c² - b²) uniquely determines the remaining leg, reducing HL to SSS."
                ),
                QuickCheckQuestion(
                    "For SSA with A = 30°, b = 10, and a = 4, how many triangles exist?",
                    listOf("0 triangles", "1 right triangle", "2 triangles", "Infinitely many"),
                    0,
                    "Altitude h = 10·sin(30°) = 5. Since a = 4 < 5, the opposite side cannot reach the base."
                )
            ),
            calculatorTier = CalcTargetTier.GEOMETRY,
            sendToCalculatorExpr = "5,7,35",
            graphExpr = "sqrt(25 + 49 - 70*cos(x))"
        ),
        LessonModule(
            id = "geo_4",
            track = MathTrack.GEOMETRY,
            order = 4,
            title = "Similarity, Scale & Area/Volume Ratios",
            summary = "AA similarity, proportional side scaling k, area scaling k², and volume scaling k³.",
            conceptExplanation = "Two figures are similar (~) if corresponding angles are equal and all linear dimensions share a constant scale factor k. Scaling lengths by k multiplies perimeter by k, area by k², and volume by k³.",
            mathematicalIntuition = "Doubling every edge of a cube tiles the base with 2×2 = 4 squares (area ×4) and stacks 2 layers high for 2×2×2 = 8 cubes (volume ×8).",
            coreFormulas = listOf(
                "Linear ratio = k",
                "Area ratio = k²",
                "Volume ratio = k³"
            ),
            definitions = listOf(
                "AA Similarity" to "Two triangles with two pairs of congruent angles are similar.",
                "Scale Factor (k)" to "Ratio of a length in the image to the corresponding length in the original."
            ),
            commonMistakes = listOf(
                "Scaling area by k instead of k².",
                "Matching non-corresponding sides in rotated similar triangles."
            ),
            sandboxType = SandboxType.TRIANGLE_INTERACTIVE,
            sandboxTitle = "Proportional Scaling Triangle Sandbox",
            sandboxExpression = "3^2",
            workedExampleProblem = "Two similar cones have radii 3 cm and 9 cm. If the smaller has volume 40 cm³, find the larger cone's volume.",
            workedExampleSteps = listOf(
                "1. Linear scale factor k = 9 / 3 = 3",
                "2. Volume scales by k³ = 3³ = 27",
                "3. V_large = 40 × 27 = 1080 cm³"
            ),
            workedExampleAnswer = "1080 cm³",
            quickChecks = listOf(
                QuickCheckQuestion(
                    "If all sides of a polygon are tripled (k = 3), its area increases by a factor of:",
                    listOf("9", "3", "6", "27"),
                    0,
                    "Area scales quadratically with linear dimensions: k² = 3² = 9."
                ),
                QuickCheckQuestion(
                    "A 6 ft person casts a 4 ft shadow while a tree casts a 20 ft shadow. How tall is the tree?",
                    listOf("30 ft", "24 ft", "13.3 ft", "40 ft"),
                    0,
                    "Similar right triangles: h / 20 = 6 / 4 → h = 30 ft."
                ),
                QuickCheckQuestion(
                    "Two similar solids have surface areas 16 cm² and 100 cm². What is their volume ratio?",
                    listOf("8 : 125", "4 : 10", "16 : 100", "2 : 5"),
                    0,
                    "k² = 16/100 → k = 4/10 = 2/5, so k³ = (2/5)³ = 8/125."
                )
            ),
            calculatorTier = CalcTargetTier.GEOMETRY,
            sendToCalculatorExpr = "40*(9/3)^3",
            graphExpr = "x^2"
        ),
        LessonModule(
            id = "geo_5",
            track = MathTrack.GEOMETRY,
            order = 5,
            title = "Euclidean Geometry & Fundamental Proofs",
            summary = "Pythagorean theorem proof, Law of Sines, Law of Cosines, and polygon angle sums.",
            conceptExplanation = "The Law of Cosines generalizes the Pythagorean theorem to all triangles by adding the correction term -2ab·cos(C). When C = 90°, cos(90°) = 0 and it collapses to c² = a² + b².",
            mathematicalIntuition = "Subtracting 2ab·cos(C) adjusts for how far side b leans toward (acute C, shorter c) or away from (obtuse C, longer c) side a.",
            coreFormulas = listOf(
                "c² = a² + b² - 2ab·cos(C)  (Law of Cosines)",
                "a / sin(A) = b / sin(B) = c / sin(C) = 2R  (Law of Sines)",
                "Polygon Interior Sum = (n - 2)·180°"
            ),
            definitions = listOf(
                "Circumradius (R)" to "Radius of the unique circle passing through all three vertices.",
                "Regular Polygon" to "Both equilateral (equal sides) and equiangular (equal angles)."
            ),
            commonMistakes = listOf(
                "Using +2ab·cos(C) instead of -2ab·cos(C) in the Law of Cosines.",
                "Forgetting that cos(C) is negative for obtuse angles (90° < C < 180°)."
            ),
            sandboxType = SandboxType.TRIANGLE_INTERACTIVE,
            sandboxTitle = "Law of Cosines Angle Sweep",
            sandboxExpression = "sqrt(5^2 + 8^2 - 2*5*8*cos(x))",
            workedExampleProblem = "In ΔABC, a = 6, b = 10, and included angle C = 120°. Find side c.",
            workedExampleSteps = listOf(
                "1. Apply Law of Cosines: c² = 6² + 10² - 2(6)(10)cos(120°)",
                "2. cos(120°) = -0.5, so c² = 36 + 100 - 120(-0.5) = 136 + 60 = 196",
                "3. c = √196 = 14"
            ),
            workedExampleAnswer = "c = 14",
            quickChecks = listOf(
                QuickCheckQuestion(
                    "What is each interior angle of a regular octagon (n = 8)?",
                    listOf("135°", "120°", "144°", "108°"),
                    0,
                    "Sum = (8 - 2)×180° = 1080°. Each angle = 1080° / 8 = 135°."
                ),
                QuickCheckQuestion(
                    "In a triangle with sides 7, 8, 12, the largest angle is:",
                    listOf("Obtuse", "Acute", "Right", "Cannot tell"),
                    0,
                    "Compare 7² + 8² = 49 + 64 = 113 against 12² = 144. Since a² + b² < c², cos(C) < 0 so C > 90°."
                ),
                QuickCheckQuestion(
                    "What is the sum of exterior angles (one per vertex) of any convex 20-gon?",
                    listOf("360°", "3240°", "180°", "720°"),
                    0,
                    "Walking completely around any convex polygon always turns a total of 360°."
                )
            ),
            calculatorTier = CalcTargetTier.GEOMETRY,
            sendToCalculatorExpr = "sqrt(6^2 + 10^2 - 2*6*10*cos(120))",
            graphExpr = "sqrt(25 + 64 - 80*cos(x))"
        ),
        LessonModule(
            id = "geo_6",
            track = MathTrack.GEOMETRY,
            order = 6,
            title = "Circles, Arcs & Circle Theorems",
            summary = "Inscribed angle theorem, Thales' theorem, tangents, chord power, and sector geometry.",
            conceptExplanation = "A circle is the locus of points equidistant (radius r) from a center. An inscribed angle subtending an arc measures exactly half the central angle subtending the same arc.",
            mathematicalIntuition = "Because a tangent touches a circle at a single point of symmetry, it cannot lean closer on either side—forcing it to sit perpendicular (90°) to the radius at that point.",
            coreFormulas = listOf(
                "Inscribed Angle = ½ × Central Angle",
                "Arc Length s = rθ (rad),  Sector Area = ½r²θ",
                "Power of a Point (chords): a·b = c·d"
            ),
            definitions = listOf(
                "Thales' Theorem" to "Any angle inscribed in a semicircle is a right angle (90°).",
                "Secant Line" to "A line intersecting a circle at two distinct points."
            ),
            commonMistakes = listOf(
                "Plugging degrees directly into s = rθ instead of converting θ to radians.",
                "Confusing central angle with inscribed angle."
            ),
            sandboxType = SandboxType.UNIT_CIRCLE,
            sandboxTitle = "Circle Arc & Central Angle Lab",
            sandboxExpression = "sqrt(1 - x^2)",
            workedExampleProblem = "Find the arc length and area of a sector with radius 6 and central angle 60°.",
            workedExampleSteps = listOf(
                "1. Convert 60° to radians: θ = 60° × π/180° = π/3",
                "2. Arc length s = rθ = 6 × (π/3) = 2π ≈ 6.283",
                "3. Sector area A = ½r²θ = ½(36)(π/3) = 6π ≈ 18.850"
            ),
            workedExampleAnswer = "s = 2π, Area = 6π",
            quickChecks = listOf(
                QuickCheckQuestion(
                    "If central angle ∠AOB = 110°, what is inscribed angle ∠ACB on the major arc?",
                    listOf("55°", "110°", "220°", "70°"),
                    0,
                    "The inscribed angle is half the central angle subtending the same arc: 110° / 2 = 55°."
                ),
                QuickCheckQuestion(
                    "Two chords intersect inside a circle with segments 4, 9 and 6, x. Find x.",
                    listOf("6", "13.5", "3", "9"),
                    0,
                    "Intersecting chords theorem: 4 × 9 = 6 × x → 36 = 6x → x = 6."
                ),
                QuickCheckQuestion(
                    "A tangent and radius meet at the point of tangency at what angle?",
                    listOf("90°", "45°", "60°", "Depends on radius"),
                    0,
                    "A tangent line is always perpendicular to the radius drawn to the point of tangency."
                )
            ),
            calculatorTier = CalcTargetTier.GEOMETRY,
            sendToCalculatorExpr = "0.5 * 6^2 * (pi/3)",
            graphExpr = "sqrt(max(0, 36 - x^2))"
        ),
        LessonModule(
            id = "geo_7",
            track = MathTrack.GEOMETRY,
            order = 7,
            title = "Trigonometry & the Unit Circle",
            summary = "Unit circle coordinates (cos θ, sin θ), Pythagorean identities, and periodic graphs.",
            conceptExplanation = "Extending right-triangle ratios to the unit circle (x² + y² = 1) defines cos(θ) as the x-coordinate and sin(θ) as the y-coordinate for every real angle θ, making trigonometric functions periodic waves.",
            mathematicalIntuition = "Since every point on the unit circle satisfies x² + y² = 1, substituting x = cos(θ) and y = sin(θ) immediately proves cos²(θ) + sin²(θ) = 1 for all angles!",
            coreFormulas = listOf(
                "sin²(θ) + cos²(θ) = 1",
                "tan(θ) = sin(θ) / cos(θ)  (cos θ ≠ 0)",
                "sin(2θ) = 2·sin(θ)·cos(θ)"
            ),
            definitions = listOf(
                "Amplitude" to "Half the vertical distance from crest to trough in A·sin(Bx).",
                "Period" to "Horizontal length of one full wave cycle: 2π / |B| for sin(Bx)."
            ),
            commonMistakes = listOf(
                "Evaluating calculus or graphing trig formulas in DEG mode when RAD is expected.",
                "Forgetting that tan(θ) has vertical asymptotes wherever cos(θ) = 0."
            ),
            sandboxType = SandboxType.UNIT_CIRCLE,
            sandboxTitle = "Interactive Unit Circle (cos θ, sin θ)",
            sandboxExpression = "sin(x)",
            workedExampleProblem = "Given sin(θ) = 3/5 in Quadrant II, find cos(θ) and tan(θ).",
            workedExampleSteps = listOf(
                "1. Use cos²(θ) = 1 - sin²(θ) = 1 - 9/25 = 16/25",
                "2. In Quadrant II, x = cos(θ) < 0, so cos(θ) = -4/5",
                "3. tan(θ) = sin(θ)/cos(θ) = (3/5) / (-4/5) = -3/4"
            ),
            workedExampleAnswer = "cos θ = -4/5, tan θ = -3/4",
            quickChecks = listOf(
                QuickCheckQuestion(
                    "Which exact value equals cos(π/3)?",
                    listOf("1/2", "√3/2", "√2/2", "0"),
                    0,
                    "π/3 rad = 60°, and cos(60°) = 1/2."
                ),
                QuickCheckQuestion(
                    "In which quadrant are both sin(θ) < 0 and cos(θ) > 0?",
                    listOf("Quadrant IV", "Quadrant II", "Quadrant III", "Quadrant I"),
                    0,
                    "x = cos(θ) > 0 (right) and y = sin(θ) < 0 (down) places θ in Quadrant IV."
                ),
                QuickCheckQuestion(
                    "What is the period of y = 3·sin(4x)?",
                    listOf("π/2", "2π", "8π", "π/4"),
                    0,
                    "Period = 2π / |B| = 2π / 4 = π/2."
                )
            ),
            calculatorTier = CalcTargetTier.STANDARD,
            sendToCalculatorExpr = "sin(pi/3)^2 + cos(pi/3)^2",
            graphExpr = "sin(x)"
        ),
        LessonModule(
            id = "geo_8",
            track = MathTrack.GEOMETRY,
            order = 8,
            title = "Coordinate Geometry & Conic Sections",
            summary = "Distance, perpendicular slopes, point-to-line distance, circles, parabolas, and ellipses.",
            conceptExplanation = "Analytic geometry bridges algebra and shape: equations like (x - h)² + (y - k)² = r² represent geometric loci of points satisfying distance conditions in the Cartesian plane.",
            mathematicalIntuition = "Two non-vertical lines are perpendicular when rotating a slope triangle (dx, dy) by 90° swaps and negates a component to (-dy, dx), turning slope dy/dx into -dx/dy = -1/m.",
            coreFormulas = listOf(
                "d = √((x₂ - x₁)² + (y₂ - y₁)²)",
                "Circle: (x - h)² + (y - k)² = r²",
                "Point-to-Line: d = |Ax₀ + By₀ + C| / √(A² + B²)"
            ),
            definitions = listOf(
                "Ellipse" to "(x-h)²/a² + (y-k)²/b² = 1; locus with constant sum of distances to two foci.",
                "Hyperbola" to "(x-h)²/a² - (y-k)²/b² = 1; locus with constant difference of distances to two foci."
            ),
            commonMistakes = listOf(
                "Reading the center of (x + 3)² + (y - 2)² = 16 as (3, -2) instead of (-3, 2).",
                "Using r² directly as the radius instead of taking √(r²)."
            ),
            sandboxType = SandboxType.QUADRATIC_EXPLORER,
            sandboxTitle = "Conic Parabola Focus/Vertex Explorer",
            sandboxExpression = "0.25*x^2 - 2",
            workedExampleProblem = "Find the center and radius of x² - 6x + y² + 4y = 12.",
            workedExampleSteps = listOf(
                "1. Group x and y terms and complete the square: (x² - 6x + 9) + (y² + 4y + 4) = 12 + 9 + 4",
                "2. Rewrite: (x - 3)² + (y + 2)² = 25",
                "3. Center is (3, -2) and radius r = √25 = 5"
            ),
            workedExampleAnswer = "Center (3, -2), r = 5",
            quickChecks = listOf(
                QuickCheckQuestion(
                    "What is the distance between (-1, 2) and (5, 10)?",
                    listOf("10", "8", "14", "√52"),
                    0,
                    "d = √((5 - (-1))² + (10 - 2)²) = √(6² + 8²) = √100 = 10."
                ),
                QuickCheckQuestion(
                    "A line has slope 2/3. What is the slope of any line perpendicular to it?",
                    listOf("-3/2", "3/2", "-2/3", "2/3"),
                    0,
                    "Perpendicular slopes are negative reciprocals: m₁·m₂ = -1 → m₂ = -3/2."
                ),
                QuickCheckQuestion(
                    "Find the perpendicular distance from (0, 0) to 3x + 4y - 15 = 0.",
                    listOf("3", "15", "5", "1.5"),
                    0,
                    "d = |3(0) + 4(0) - 15| / √(3² + 4²) = 15 / 5 = 3."
                )
            ),
            calculatorTier = CalcTargetTier.GEOMETRY,
            sendToCalculatorExpr = "sqrt((5 - (-1))^2 + (10 - 2)^2)",
            graphExpr = "0.25*x^2 - 2"
        )
    )
}
