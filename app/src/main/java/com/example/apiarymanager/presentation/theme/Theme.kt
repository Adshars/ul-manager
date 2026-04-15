package com.example.apiarymanager.presentation.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary          = Amber40,
    onPrimary        = Neutral99,
    primaryContainer = Amber80,
    secondary        = Brown40,
    onSecondary      = Neutral99,
    secondaryContainer = Brown80,
    tertiary         = Green40,
    onTertiary       = Neutral99,
    tertiaryContainer = Green80,
    error            = ErrorRed,
    errorContainer   = ErrorRedContainer,
    background       = Neutral99,
    onBackground     = Neutral10,
    surface          = Neutral99,
    onSurface        = Neutral10,
)

private val DarkColorScheme = darkColorScheme(
    primary          = Amber80,
    onPrimary        = Neutral10,
    primaryContainer = Amber40,
    secondary        = Brown80,
    onSecondary      = Neutral10,
    secondaryContainer = Brown40,
    tertiary         = Green80,
    onTertiary       = Neutral10,
    tertiaryContainer = Green40,
    error            = ErrorRedContainer,
    background       = Neutral10,
    onBackground     = Neutral99,
    surface          = Neutral10,
    onSurface        = Neutral99,
)

@Composable
fun ApiaryManagerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else      -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = ApiaryTypography,
        content     = content
    )
}
