package dev.luizhenriquevicari.remir.app.designsystem

import androidx.compose.ui.unit.dp
import com.clerk.api.ui.ClerkColors
import com.clerk.api.ui.ClerkDesign
import com.clerk.api.ui.ClerkTheme
import com.clerk.api.ui.ClerkTypography

private val LightClerkColors = ClerkColors(
    primary = primaryLight,
    primaryForeground = onPrimaryLight,
    background = backgroundLight,
    foreground = onSurfaceLight,
    mutedForeground = onSurfaceVariantLight,
    input = surfaceContainerLowestLight,
    inputForeground = onSurfaceLight,
    muted = surfaceContainerHighLight,
    neutral = outlineVariantLight,
    border = outlineVariantLight,
    ring = LimeText,
    secondaryButtonBackground = surfaceContainerLowestLight,
    secondaryButtonForeground = onSurfaceLight,
    danger = errorLight,
    success = successLight,
    warning = warningLight,
    shadow = scrimLight,
)

private val DarkClerkColors = ClerkColors(
    primary = primaryDark,
    primaryForeground = onPrimaryDark,
    background = backgroundDark,
    foreground = onSurfaceDark,
    mutedForeground = onSurfaceVariantDark,
    input = surfaceContainerDark,
    inputForeground = onSurfaceDark,
    muted = surfaceContainerHighDark,
    neutral = outlineVariantDark,
    border = outlineVariantDark,
    ring = Lime,
    secondaryButtonBackground = surfaceContainerHighDark,
    secondaryButtonForeground = onSurfaceDark,
    danger = errorDark,
    success = successDark,
    warning = warningDark,
    shadow = scrimDark,
)

private val defaultTypography = ClerkTypography()

private val RemirClerkTypography = defaultTypography.copy(
    displaySmall = defaultTypography.displaySmall?.copy(fontFamily = Display),
    headlineLarge = defaultTypography.headlineLarge?.copy(fontFamily = Display),
    headlineMedium = defaultTypography.headlineMedium?.copy(fontFamily = Display),
    headlineSmall = defaultTypography.headlineSmall?.copy(fontFamily = Display),
    titleMedium = defaultTypography.titleMedium?.copy(fontFamily = Body),
    titleSmall = defaultTypography.titleSmall?.copy(fontFamily = Body),
    bodyLarge = defaultTypography.bodyLarge?.copy(fontFamily = Body),
    bodyMedium = defaultTypography.bodyMedium?.copy(fontFamily = Body),
    bodySmall = defaultTypography.bodySmall?.copy(fontFamily = Body),
    labelMedium = defaultTypography.labelMedium?.copy(fontFamily = Body),
    labelSmall = defaultTypography.labelSmall?.copy(fontFamily = Body),
)

/** Skins Clerk's prebuilt UI (AuthView) with the Remir palette, fonts and corner radius. */
val RemirClerkTheme = ClerkTheme(
    lightColors = LightClerkColors,
    darkColors = DarkClerkColors,
    typography = RemirClerkTypography,
    design = ClerkDesign(borderRadius = 10.dp),
)
