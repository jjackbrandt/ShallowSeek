package com.example.shallowseek.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * Enum class representing all available themes in the application.
 */
enum class AppTheme(val displayName: String) {
    SYSTEM("System Default"),
    LIGHT("Light"),
    DARK("Dark"),
    DRACULA("Dracula"),
    MATERIAL_DEEP_OCEAN("Material Deep Ocean"),
    ROOTBEER("Jacob Mode"),
    FOREST("Forest"),
    SUNSET("Sunset");

    companion object {
        fun fromOrdinal(ordinal: Int): AppTheme {
            return values().firstOrNull { it.ordinal == ordinal } ?: SYSTEM
        }
    }
}

/**
 * Function to get the appropriate color scheme for the selected theme.
 */
fun getColorScheme(theme: AppTheme, isDarkTheme: Boolean): ColorScheme {
    return when (theme) {
        AppTheme.SYSTEM -> if (isDarkTheme) DarkColorScheme else LightColorScheme
        AppTheme.LIGHT -> LightColorScheme
        AppTheme.DARK -> DarkColorScheme
        AppTheme.DRACULA -> DraculaColorScheme
        AppTheme.MATERIAL_DEEP_OCEAN -> MaterialDeepOceanColorScheme
        AppTheme.ROOTBEER -> RootbeerColorScheme
        AppTheme.FOREST -> ForestColorScheme
        AppTheme.SUNSET -> SunsetColorScheme
    }
}

// Default light and dark color schemes
val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40,
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F)
)

val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80,
    background = Color(0xFF1C1B1F),
    surface = Color(0xFF1C1B1F),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFFE6E1E5),
    onSurface = Color(0xFFE6E1E5)
)

// Dracula theme color scheme
val DraculaColorScheme = darkColorScheme(
    primary = Color(0xFF8BE9FD),
    secondary = Color(0xFFFF79C6),
    tertiary = Color(0xFFFFB86C),
    background = Color(0xFF282A36),
    surface = Color(0xFF282A36),
    onPrimary = Color(0xFF282A36),
    onSecondary = Color(0xFF282A36),
    onTertiary = Color(0xFF282A36),
    onBackground = Color(0xFFF8F8F2),
    onSurface = Color(0xFFF8F8F2),
    surfaceVariant = Color(0xFF44475A),
    onSurfaceVariant = Color(0xFFF8F8F2)
)

// Material Deep Ocean theme color scheme
val MaterialDeepOceanColorScheme = darkColorScheme(
    primary = Color(0xFF84FFFF),
    secondary = Color(0xFFB0BEC5),
    tertiary = Color(0xFFEAFF8F),
    background = Color(0xFF0F111A),
    surface = Color(0xFF0F111A),
    onPrimary = Color(0xFF0F111A),
    onSecondary = Color(0xFF0F111A),
    onTertiary = Color(0xFF0F111A),
    onBackground = Color(0xFFEEEEEE),
    onSurface = Color(0xFFEEEEEE),
    surfaceVariant = Color(0xFF1F2233),
    onSurfaceVariant = Color(0xFFEEEEEE)
)

// Rootbeer theme color scheme (Jacob Mode)
val RootbeerColorScheme = lightColorScheme(
    primary = Color(0xFF5F4B32),
    secondary = Color(0xFF8B5A2B),
    tertiary = Color(0xFFD2691E),
    background = Color(0xFFFDF5E6),
    surface = Color(0xFFFDF5E6),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF3E2723),
    onSurface = Color(0xFF3E2723),
    surfaceVariant = Color(0xFFECDCC3),
    onSurfaceVariant = Color(0xFF3E2723)
)

// Forest theme color scheme
val ForestColorScheme = darkColorScheme(
    primary = Color(0xFF81C784),
    secondary = Color(0xFFA5D6A7),
    tertiary = Color(0xFFFFCC80),
    background = Color(0xFF2E4736),
    surface = Color(0xFF2E4736),
    onPrimary = Color(0xFF1B5E20),
    onSecondary = Color(0xFF1B5E20),
    onTertiary = Color(0xFF1B5E20),
    onBackground = Color(0xFFE8F5E9),
    onSurface = Color(0xFFE8F5E9),
    surfaceVariant = Color(0xFF3E684A),
    onSurfaceVariant = Color(0xFFE8F5E9)
)

// Sunset theme color scheme
val SunsetColorScheme = lightColorScheme(
    primary = Color(0xFFFF7043),
    secondary = Color(0xFFFFB74D),
    tertiary = Color(0xFF9575CD),
    background = Color(0xFFFFF5F2),
    surface = Color(0xFFFFF5F2),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF3E2723),
    onSurface = Color(0xFF3E2723),
    surfaceVariant = Color(0xFFFFE0B2),
    onSurfaceVariant = Color(0xFF3E2723)
)