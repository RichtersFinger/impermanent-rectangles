package com.richtersfinger.impermanentrectangles.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

fun progressToColor(progress: Float): Color {
    val p = progress.coerceIn(0f, 2f)
    val red = Color(0xFFF44336)
    val amber = Color(0xFFFFC107)
    val green = Color(0xFF4CAF50)
    val overComplete = Color(0xFF00BCD4)

    return when {
        p < 0.5f -> lerp(red, amber, p / 0.5f)                  // 0.0..0.5
        p < 1.0f -> lerp(amber, green, (p - 0.5f) / 0.5f)       // 0.5..1.0
        else -> lerp(green, overComplete, (p - 1.0f) / 1.0f)            // 1.0..2.0
    }
}
