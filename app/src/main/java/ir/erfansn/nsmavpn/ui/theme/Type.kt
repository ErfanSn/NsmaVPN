package ir.erfansn.nsmavpn.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import ir.erfansn.nsmavpn.R

private val fontFamily = FontFamily(
    Font(R.font.baloo_bhaijaan2, FontWeight.Normal)
)

val Typography = Typography().run {
    copy(
        displayLarge = displayLarge.copy(
            fontFamily = fontFamily
        ),
        displayMedium = displayMedium.copy(
            fontFamily = fontFamily
        ),
        displaySmall = displaySmall.copy(
            fontFamily = fontFamily
        ),
        headlineLarge = headlineLarge.copy(
            fontFamily = fontFamily
        ),
        headlineMedium = headlineMedium.copy(
            fontFamily = fontFamily
        ),
        headlineSmall = headlineSmall.copy(
            fontFamily = fontFamily
        ),
        titleLarge = titleLarge.copy(
            fontFamily = fontFamily
        ),
        titleMedium = titleMedium.copy(
            fontFamily = fontFamily
        ),
        titleSmall = titleSmall.copy(
            fontFamily = fontFamily
        ),
        bodyLarge = bodyLarge.copy(
            fontFamily = fontFamily
        ),
        bodyMedium = bodyMedium.copy(
            fontFamily = fontFamily
        ),
        bodySmall = bodySmall.copy(
            fontFamily = fontFamily
        ),
        labelLarge = labelLarge.copy(
            fontFamily = fontFamily
        ),
        labelMedium = labelMedium.copy(
            fontFamily = fontFamily
        ),
        labelSmall = labelSmall.copy(
            fontFamily = fontFamily
        )
    )
}
