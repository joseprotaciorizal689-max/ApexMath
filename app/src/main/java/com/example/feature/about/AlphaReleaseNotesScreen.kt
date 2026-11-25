package com.example.feature.about

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.core.storage.UserSettings
import com.example.core.ui.ApexCard
import com.example.core.ui.ApexPillBadge
import com.example.core.ui.StatusBanner
import com.example.ui.theme.ApexAmber
import com.example.ui.theme.ApexAmberContainer
import com.example.ui.theme.ApexBackground
import com.example.ui.theme.ApexCyan
import com.example.ui.theme.ApexSuccess
import com.example.ui.theme.ApexTextPrimary
import com.example.ui.theme.ApexTextSecondary
import com.example.ui.theme.JetBrainsMonoFontFamily

@Composable
fun AlphaReleaseNotesScreen(
    settings: UserSettings,
    onToggleHaptics: (Boolean) -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ApexBackground)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.size(44.dp)) {
                Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back", tint = ApexTextPrimary)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("About ApexMath", style = MaterialTheme.typography.titleLarge, color = ApexTextPrimary)
                Text("Release Notes & Offline Preferences", style = MaterialTheme.typography.bodySmall, color = ApexTextSecondary)
            }
            ApexPillBadge("Alpha-112626", ApexAmber, ApexAmberContainer)
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ApexCard {
                Text("Version Identity", style = MaterialTheme.typography.titleSmall, color = ApexCyan)
                Spacer(Modifier.height(6.dp))
                Text(
                    "App: ApexMath\nChannel: Alpha\nRelease Label: Alpha-112626\nVersion Code: ${BuildConfig.VERSION_CODE}\nVersion Name: ${BuildConfig.VERSION_NAME}\nConnectivity: 100% Offline (Zero network permissions)",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 12.sp,
                    color = ApexTextPrimary
                )
            }

            ApexCard {
                Text("Preferences", style = MaterialTheme.typography.titleSmall, color = ApexCyan)
                Spacer(Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Keypad Haptic Feedback", style = MaterialTheme.typography.bodyMedium, color = ApexTextPrimary)
                        Text("Subtle tactile vibration on mathematical key taps", style = MaterialTheme.typography.bodySmall, color = ApexTextSecondary)
                    }
                    Switch(checked = settings.hapticEnabled, onCheckedChange = onToggleHaptics)
                }
            }

            ApexCard {
                Text("Implemented Core Features", style = MaterialTheme.typography.titleSmall, color = ApexSuccess)
                Spacer(Modifier.height(6.dp))
                val implemented = listOf(
                    "Recursive-descent math parser with implicit multiplication, complex numbers (a+bi & polar), exact continued-fraction conversion, percentages, combinatorics, and 18 physical/math constants.",
                    "Algebra & Matrix engine: linear, quadratic, cubic (Cardano/Vieta), 2×2 & 3×3 Gaussian elimination with partial pivoting, matrix ops up to 4×4 + 2×2 eigenvalues, polynomials, and number theory.",
                    "Geometry & Trig engine: SSS/SAS/ASA/AAS/SSA (including ambiguous 2-triangle cases) with live scaled Canvas diagram, 2D shapes, 3D solids, and coordinate geometry.",
                    "Calculus & Numerical engine: symbolic AST differentiation (1st & 2nd order + rules), elementary symbolic antiderivatives, Adaptive Simpson definite integration with shaded plot, numerical limit convergence tables, Taylor series up to degree 8, and Bisection/Newton/Secant root finders.",
                    "Interactive 2D Graphing Studio with discontinuity-aware sampling, touch-pan, pinch-zoom, roots/extrema/intersections, tangent line, derivative curve, and integral shading.",
                    "24-module offline curriculum (8 Algebra, 8 Geometry, 8 Calculus) with interactive touch sandboxes, worked examples, quick-checks, deterministic adaptive practice, Room notebook, and DataStore settings."
                )
                implemented.forEach {
                    Text("• $it", style = MaterialTheme.typography.bodySmall, color = ApexTextPrimary)
                    Spacer(Modifier.height(4.dp))
                }
            }

            ApexCard {
                Text("Experimental Features & Known Limitations", style = MaterialTheme.typography.titleSmall, color = ApexAmber)
                Spacer(Modifier.height(6.dp))
                val limitations = listOf(
                    "Symbolic Integration Scope: Symbolic antiderivatives cover elementary polynomial, exponential, reciprocal, and basic trigonometric forms. Integration by parts, partial fractions, and composite substitutions fall back honestly to Adaptive Simpson numerical quadrature.",
                    "Matrix Eigenanalysis: Symbolic eigenvalues/eigenvectors are currently scoped to 2×2 matrices; general n×n QR eigensolvers are planned for future releases.",
                    "Limit Analysis: Limit evaluation uses numerical approaching sequences (h = 10⁻¹..10⁻⁶) rather than formal algebraic L'Hôpital/squeeze proofs; removable singularities with severe floating-point cancellation may trigger warnings.",
                    "Cubic & High-Degree Roots: Ill-conditioned cubics report elevated residual warnings; general degree ≥ 4 polynomial symbolic radicals are not included."
                )
                limitations.forEach {
                    Text("• $it", style = MaterialTheme.typography.bodySmall, color = ApexTextSecondary)
                    Spacer(Modifier.height(4.dp))
                }
            }

            StatusBanner(
                text = "Disclaimer: ApexMath Alpha-112626 is an experimental educational mathematical tool. Numerical approximations, ill-conditioned systems, and limit estimates may require independent verification for safety-critical or formal engineering work.",
                isError = false
            )

            Spacer(Modifier.height(16.dp))
        }
    }
}
