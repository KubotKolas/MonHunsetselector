package com.kubot.monhunsetselector.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = MHW_Highlight_YellowGreen,
    onPrimary = MHW_Background_Slate,
    primaryContainer = Color(0xFF3D4A02),
    onPrimaryContainer = MHW_Highlight_YellowGreen,

    secondary = MHW_Accent_Gold,
    onSecondary = MHW_Background_Slate,
    secondaryContainer = Color(0xFF4F4231),
    onSecondaryContainer = MHW_Accent_Gold,

    tertiary = MHW_Accent_Gold,
    onTertiary = MHW_Background_Slate,
    tertiaryContainer = Color(0xFF4F4231),
    onTertiaryContainer = MHW_Accent_Gold,

    background = MHW_Background_Slate,
    onBackground = MHW_Text_Cream,

    surface = MHW_Surface_Dark,
    onSurface = MHW_Text_Cream,

    surfaceVariant = MHW_Surface_Variant,
    onSurfaceVariant = MHW_Text_Muted,

    outline = MHW_Surface_Variant,

    error = MHW_Error_Red,
    onError = MHW_Text_Cream,
    errorContainer = Color(0xFF93000A),
    onErrorContainer = MHW_Error_Red
)


private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF5A6600),
    onPrimary = Color.White,
    background = Color(0xFFFDFCF5),
    surface = Color(0xFFFDFCF5),
    onBackground = Color(0xFF1B1C18),
    onSurface = Color(0xFF1B1C18)

)

@Composable
fun MonHunSetSelectorTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),


    content: @Composable () -> Unit
) {


    val colorScheme = when {

        else -> DarkColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window

            window.statusBarColor = Color.Transparent.toArgb()

            window.navigationBarColor = Color.Transparent.toArgb()


            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false

            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}