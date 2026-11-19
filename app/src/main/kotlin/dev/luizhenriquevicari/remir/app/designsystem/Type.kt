package dev.luizhenriquevicari.remir.app.designsystem

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.luizhenriquevicari.remir.app.R

val Display =
    FontFamily(
        Font(R.font.barlow_semi_condensed_medium, FontWeight.Medium),
        Font(R.font.barlow_semi_condensed_semibold, FontWeight.SemiBold),
    )

// Inter ships as a single variable font; each weight picks its own axis value (minSdk 26 supports it).
val Body =
    FontFamily(
        Font(
            R.font.inter,
            FontWeight.Normal,
            variationSettings = FontVariation.Settings(FontWeight.Normal, FontStyle.Normal),
        ),
        Font(
            R.font.inter,
            FontWeight.Medium,
            variationSettings = FontVariation.Settings(FontWeight.Medium, FontStyle.Normal),
        ),
    )

private val base = Typography()

val RemirTypography =
    Typography(
        displayLarge = base.displayLarge.copy(fontFamily = Display, fontWeight = FontWeight.SemiBold),
        displayMedium = base.displayMedium.copy(fontFamily = Display, fontWeight = FontWeight.SemiBold),
        displaySmall = base.displaySmall.copy(fontFamily = Display, fontWeight = FontWeight.SemiBold),
        headlineLarge = base.headlineLarge.copy(fontFamily = Display, fontWeight = FontWeight.Medium),
        headlineMedium = base.headlineMedium.copy(fontFamily = Display, fontWeight = FontWeight.Medium),
        headlineSmall = base.headlineSmall.copy(fontFamily = Display, fontWeight = FontWeight.Medium),
        titleLarge = base.titleLarge.copy(fontFamily = Display, fontWeight = FontWeight.Medium),
        titleMedium = base.titleMedium.copy(fontFamily = Body),
        titleSmall = base.titleSmall.copy(fontFamily = Body),
        bodyLarge = base.bodyLarge.copy(fontFamily = Body),
        bodyMedium = base.bodyMedium.copy(fontFamily = Body),
        bodySmall = base.bodySmall.copy(fontFamily = Body),
        labelLarge = base.labelLarge.copy(fontFamily = Body, letterSpacing = 0.2.sp),
        labelMedium = base.labelMedium.copy(fontFamily = Body),
        labelSmall = base.labelSmall.copy(fontFamily = Body),
    )

// Slightly tighter than the M3 default: reads firmer, less "bubbly".
val RemirShapes =
    Shapes(
        extraSmall = RoundedCornerShape(4.dp),
        small = RoundedCornerShape(6.dp),
        medium = RoundedCornerShape(10.dp),
        large = RoundedCornerShape(14.dp),
        extraLarge = RoundedCornerShape(24.dp),
    )
