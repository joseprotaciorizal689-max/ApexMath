<div align="center">

<img src="https://files.catbox.moe/5o0bjg.png" width="180" alt="ApexMath Icon"/>

# ApexMath

### The Swiss Army Calculator.

**One application. Algebra, Geometry, Calculus, Graphing, and beyond.**

<sub>First Release · Alpha-112626</sub>

</div>

---

## 🌌 About

**ApexMath** is an offline-first, native Android mathematical toolkit designed to bring an entire collection of mathematical tools into one unified application.

From everyday calculations and complex numbers to matrix operations, geometry, symbolic calculus, numerical methods, and interactive graphing, ApexMath is built to be more than a conventional calculator.

It's a mathematical workspace.

With an integrated mathematics engine, interactive graphing studio, offline curriculum, adaptive practice, and personal mathematical notebook, ApexMath combines computation, visualization, and learning into a single experience.

> A Swiss Army calculator for mathematical exploration.

---

## 🚀 Alpha-112626

<sub>The first release of ApexMath.</sub>

Alpha-112626 establishes the foundation of ApexMath, combining multiple mathematical disciplines and computational tools into one offline-capable Android application.

### 🧮 Core Calculator Engine

- Recursive-descent mathematical expression parser
- Implicit multiplication
- Complex number arithmetic
- Rectangular and polar complex number notation
- Exact continued-fraction conversion
- Percentage calculations
- Combinatorics — permutations and combinations
- 18 mathematical and physical constants

### 📐 Algebra & Matrix Engine

- Linear equation solver
- Quadratic equation solver
- Cubic equation solver using Cardano and Vieta methods
- 2×2 and 3×3 systems of equations
- Gaussian elimination with partial pivoting
- Matrix operations up to 4×4
- 2×2 matrix eigenvalue calculations
- Polynomial operations
- Number theory tools

### 📏 Geometry & Trigonometry Engine

- Triangle solving:
  - SSS
  - SAS
  - ASA
  - AAS
  - SSA
- Ambiguous SSA cases, including two possible triangles
- Live-scaled Canvas triangle diagrams
- 2D geometric shape calculations
- 3D solid calculations
- Coordinate geometry

### ∫ Calculus & Numerical Engine

- Symbolic differentiation
- First and second derivatives
- Differentiation rules
- Elementary symbolic antiderivatives
- Adaptive Simpson definite integration
- Integral plot shading
- Numerical limit analysis
- Limit convergence tables
- Taylor series up to degree 8
- Bisection root finder
- Newton root finder
- Secant root finder

### 📈 Interactive Graphing Studio

An interactive 2D graphing environment featuring:

- Discontinuity-aware sampling
- Touch-based panning
- Pinch-to-zoom
- Root detection
- Extremum detection
- Function intersections
- Tangent line visualization
- Derivative curve visualization
- Integral area shading

### 📚 Offline Learning System

A complete 24-module mathematics curriculum:

| Subject | Modules |
|---|---:|
| Algebra | 8 |
| Geometry | 8 |
| Calculus | 8 |
| **Total** | **24** |

Features include:

- Interactive touch-based sandboxes
- Worked examples
- Quick-check exercises
- Deterministic adaptive practice
- Offline learning content
- Room-based notebook
- DataStore settings

---

## 🧪 Experimental Features & Known Limitations

ApexMath Alpha-112626 includes experimental computational features. Some mathematical operations currently have limited symbolic support or rely on numerical approximations.

### Symbolic Integration

Symbolic antiderivatives currently cover elementary polynomial, exponential, reciprocal, and basic trigonometric forms.

Integration by parts, partial fractions, and composite substitutions are not currently implemented symbolically and instead fall back to Adaptive Simpson numerical quadrature.

### Matrix Eigenanalysis

Symbolic eigenvalues and eigenvectors are currently scoped to 2×2 matrices.

General n×n QR-based eigensolvers are planned for future releases.

### Limit Analysis

Limit evaluation uses numerical approaching sequences with step sizes from \(10^{-1}\) through \(10^{-6}\).

Formal algebraic methods such as L'Hôpital's rule and squeeze theorem proofs are not currently implemented.

Removable singularities involving severe floating-point cancellation may produce warnings.

### Cubic & High-Degree Polynomial Roots

Ill-conditioned cubic equations may report elevated residual warnings.

General symbolic radical solutions for polynomial equations of degree 4 and higher are not included.

---

## 🛠️ Built With

- Kotlin
- Native Android
- Jetpack Compose
- Material 3
- Room
- DataStore
- Canvas-based visualizations

---

## 📱 Design Philosophy

ApexMath is built around three principles:

- **Unified:** Multiple mathematical tools in one application.
- **Explorable:** Interactive visualizations and mathematical experimentation.
- **Accessible Offline:** Mathematics tools and learning content without requiring a constant internet connection.

---

## 🔮 The ApexMath Lineage

ApexMath follows a developer-defined release naming system.

Each release designation is manually assigned and forms part of the project's naming lineage.

The initial release begins with:

**Alpha-112626**

The future of ApexMath is not defined by conventional version numbering alone, but by its own release identity.

---

## 🌠 The Vision

ApexMath aims to bridge the gap between a calculator, a mathematics learning environment, and an engineering-style computational workspace.

Calculate.

Explore.

Visualize.

Learn.

All in one place.

---

<div align="center">

**ApexMath**

*Alpha-112626*

</div>
