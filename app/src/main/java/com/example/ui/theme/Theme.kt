package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

private val ApexDarkColorScheme = darkColorScheme(
    primary = ApexCyan,
    onPrimary = ApexBackground,
    primaryContainer = ApexCyanContainer,
    onPrimaryContainer = ApexCyan,
    secondary = ApexAmber,
    onSecondary = ApexBackground,
    secondaryContainer = ApexAmberContainer,
    onSecondaryContainer = ApexAmber,
    tertiary = ApexSuccess,
    onTertiary = ApexBackground,
    tertiaryContainer = ApexSuccessContainer,
    onTertiaryContainer = ApexSuccess,
    background = ApexBackground,
    onBackground = ApexTextPrimary,
    surface = ApexSurface,
    onSurface = ApexTextPrimary,
    surfaceVariant = ApexSurfaceElevated,
    onSurfaceVariant = ApexTextSecondary,
    outline = ApexBorder,
    outlineVariant = ApexBorderSubtle,
    error = ApexError,
    onError = ApexBackground,
    errorContainer = ApexErrorContainer,
    onErrorContainer = ApexError
)

val ApexShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(18.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

@Composable
fun ApexMathTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                WindowCompat.getInsetsController(window, view).apply {
                    isAppearanceLightStatusBars = false
                    isAppearanceLightNavigationBars = false
                }
            }
        }
    }

    MaterialTheme(
        colorScheme = ApexDarkColorScheme,
        typography = ApexTypography,
        shapes = ApexShapes,
        content = content
    )
}
