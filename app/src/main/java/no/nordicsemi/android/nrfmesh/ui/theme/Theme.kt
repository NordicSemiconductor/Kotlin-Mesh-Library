package no.nordicsemi.android.nrfmesh.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import com.google.accompanist.systemuicontroller.rememberSystemUiController

private val DarkColorPalette = darkColors(
    primary = NordicBlue,
    primaryVariant = NordicBlueDark,
    secondary = NordicLake,
    background = Color.Black,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onSurface = Color.White
)

private val LightColorPalette = lightColors(
    primary = NordicBlue,
    primaryVariant = NordicBlueDark,
    secondary = NordicLake,
    secondaryVariant = NordicBlueSlate,
    background = NordicBackground,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color.Black,
    onSurface = Color.Black,
)

@Composable
fun NordicTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }

    MaterialTheme(
        colors = colors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )

    /**
     * There was a problem with navigation bar color on Samsung 8 with Android 9.
     * Because Dark Theme was introduced in Android 10, which may be possible cause of the problem,
     * the navigation bar color is set programmatically for older versions of Android.
     */
    if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.Q) {
        val systemUiController = rememberSystemUiController()

        SideEffect {
            systemUiController.setNavigationBarColor(color = colors.background)
        }
    }
}