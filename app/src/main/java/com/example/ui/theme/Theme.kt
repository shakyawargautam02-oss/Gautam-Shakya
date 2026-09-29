package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

enum class IdeTheme(val displayName: String, val bgHex: Long, val sidebarHex: Long, val statusHex: Long) {
    VS_CODE_DARK("VS Code Dark+", 0xFF1E1E1E, 0xFF252526, 0xFF007ACC),
    GITHUB_DARK("GitHub Dark", 0xFF0D1117, 0xFF161B22, 0xFF1F6FEB),
    MONOKAI_PRO("Monokai Pro", 0xFF272822, 0xFF21221D, 0xFF75715E),
    TOKYO_NIGHT("Tokyo Night", 0xFF1A1B26, 0xFF1F2335, 0xFF7AA2F7)
}

fun getIdeColorScheme(ideTheme: IdeTheme) = darkColorScheme(
    primary = Color(ideTheme.statusHex),
    onPrimary = Color.White,
    primaryContainer = Color(ideTheme.statusHex).copy(alpha = 0.3f),
    onPrimaryContainer = Color.White,
    secondary = Color(0xFF0E639C),
    onSecondary = Color.White,
    background = Color(ideTheme.bgHex),
    onBackground = Color(0xFFCCCCCC),
    surface = Color(ideTheme.sidebarHex),
    onSurface = Color(0xFFCCCCCC),
    surfaceVariant = Color(ideTheme.sidebarHex).copy(alpha = 0.85f),
    onSurfaceVariant = Color(0xFF858585),
    outline = Color(0xFF3C3C3C)
)

@Composable
fun CodeStudioTheme(
    ideTheme: IdeTheme = IdeTheme.VS_CODE_DARK,
    content: @Composable () -> Unit
) {
    val colorScheme = getIdeColorScheme(ideTheme)
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = Color(ideTheme.bgHex).toArgb()
                window.navigationBarColor = Color(ideTheme.statusHex).toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
