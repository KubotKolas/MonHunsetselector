package com.kubot.monhunsetselector.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

//private val DarkColorScheme = darkColorScheme(
//    primary = Purple80,
//    secondary = PurpleGrey80,
//    tertiary = Pink80
//)
//
//private val LightColorScheme = lightColorScheme(
//    primary = Purple40,
//    secondary = PurpleGrey40,
//    tertiary = Pink40
//
//    /* Other default colors to override
//    background = Color(0xFFFFFBFE),
//    surface = Color(0xFFFFFBFE),
//    onPrimary = Color.White,
//    onSecondary = Color.White,
//    onTertiary = Color.White,
//    onBackground = Color(0xFF1C1B1F),
//    onSurface = Color(0xFF1C1B1F),
//    */
//)

// Define the Dark Color Scheme based on the screenshot
private val DarkColorScheme = darkColorScheme(
    primary = MHW_Highlight_YellowGreen,
    onPrimary = MHW_Background_Slate, // Dark text on the bright highlight
    primaryContainer = Color(0xFF3D4A02), // A darker, less intense version of the primary
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

// A fallback Light Color Scheme (you can customize this later if needed)
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF5A6600),
    onPrimary = Color.White,
    background = Color(0xFFFDFCF5),
    surface = Color(0xFFFDFCF5),
    onBackground = Color(0xFF1B1C18),
    onSurface = Color(0xFF1B1C18)
    // Other default values
)

@Composable
fun MonHunSetSelectorTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
//    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
//    val colorScheme = when {
//        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
//            val context = LocalContext.current
//            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
//        }
//
//        darkTheme -> DarkColorScheme
//        else -> LightColorScheme
//    }
//
//    MaterialTheme(
//        colorScheme = colorScheme,
//        typography = Typography,
//        content = content
//    )
    val colorScheme = when {
        // We will force dark theme for now to match the game's aesthetic
        else -> DarkColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Set status bar to be transparent
            window.statusBarColor = Color.Transparent.toArgb()
            // Set navigation bar to be transparent
            window.navigationBarColor = Color.Transparent.toArgb()

            // This handles the color of the icons in the status bar (e.g., time, battery)
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            // This handles the color of the icons in the navigation bar (e.g., back, home buttons)
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // Assumes you have a Typography.kt file
        content = content
    )
}