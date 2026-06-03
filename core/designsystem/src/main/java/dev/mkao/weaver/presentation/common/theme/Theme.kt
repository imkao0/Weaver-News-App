package dev.mkao.weaver.presentation.common.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = WeaverPrimary,
    onPrimary = WeaverSurface,
    secondary = WeaverSecondary,
    onSecondary = WeaverTextPrimary,
    tertiary = WeaverTertiary,
    background = Color(0xFF1A130E),
    onBackground = Color(0xFFF7F5F0),
    surface = Color(0xFF261D16),
    surfaceContainer = Color(0xFF261D16),
    surfaceContainerLow = Color(0xFF1A130E),
    surfaceContainerHigh = Color(0xFF261D16),
    surfaceContainerHighest = Color(0xFF261D16),
    onSurface = Color(0xFFF7F5F0),
    surfaceVariant = Color(0xFF33271E),
    onSurfaceVariant = Color(0xFFDFDACF),
    outline = Color(0xFF33271E),
)

private val LightColorScheme = lightColorScheme(
    primary = WeaverPrimary,
    onPrimary = WeaverSurface,
    secondary = WeaverSecondary,
    onSecondary = WeaverTextPrimary,
    tertiary = WeaverTertiary,
    background = WeaverBackground,
    onBackground = WeaverTextPrimary,
    surface = WeaverSurface,
    onSurface = WeaverTextPrimary,
    surfaceVariant = WeaverSecondary,
    onSurfaceVariant = WeaverTextSecondary,
    outline = WeaverOutline,
)

/** App theme. */
@Composable
fun WeaverTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
