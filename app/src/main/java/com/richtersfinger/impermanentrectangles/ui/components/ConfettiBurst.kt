package com.richtersfinger.impermanentrectangles.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate


@Composable
fun ConfettiBurst(
    triggerKey: Int,
    modifier: Modifier = Modifier
) {
    data class Particle(
        val angleRad: Float,
        val speed: Float,
        val size: Float,
        val color: Color,
        val drift: Float,
        val spin: Float
    )

    val progress = remember { Animatable(0f) }
    var visible by remember { mutableStateOf(false) }

    val brightColors = listOf(
        Color(0xFFFF1744), // bright red
        Color(0xFFFFEA00), // bright yellow
        Color(0xFF00E676), // bright green
        Color(0xFF00E5FF), // bright cyan
        Color(0xFFD500F9), // bright purple
        Color(0xFFFF9100)  // bright orange
    )

    val particles = remember(triggerKey) {
        val random = kotlin.random.Random(System.currentTimeMillis())
        List(random.nextInt(6, 12)) {
            // launch mostly upward and left, to fly from the right end
            val angleDeg = random.nextInt(95, 145).toFloat()
            val angleRad = Math.toRadians(angleDeg.toDouble()).toFloat()
            Particle(
                angleRad = angleRad,
                speed = random.nextInt(400, 800).toFloat(),   // initial shoot speed
                size = random.nextInt(8, 16).toFloat(),       // random size
                color = brightColors.random(random),           // random bright color
                drift = random.nextFloat() * 36f - 18f,       // side drift
                spin = random.nextFloat() * 540f - 270f       // rotation variety
            )
        }
    }

    LaunchedEffect(triggerKey) {
        if (triggerKey > 0) {
            visible = true
            progress.snapTo(0f)
            progress.animateTo(1f, animationSpec = tween(durationMillis = 1500, easing = EaseOut))
            visible = false
        }
    }

    if (visible) {
        androidx.compose.foundation.Canvas(modifier = modifier) {
            val startX = size.width
            val startY = size.height * 0.5f
            val gravity = 1000f // low-ish gravity => slower drop after apex

            particles.forEachIndexed { i, p ->
                val t = progress.value * 1.8f // seconds mapped from animation progress

                // physics: y-axis positive downward in canvas
                val vx = kotlin.math.cos(p.angleRad) * p.speed
                val vy = -kotlin.math.sin(p.angleRad) * p.speed

                val x = startX + vx * t + p.drift * t * t
                val y = startY + vy * t + 0.5f * gravity * t * t

                // keep bright at launch, then fade mostly during descent
                val fadeStart = 0.40f
                val alpha = when {
                    progress.value < fadeStart -> 1f
                    else -> (1f - (progress.value - fadeStart) / (1f - fadeStart)).coerceIn(0f, 1f)
                }

                if (alpha > 0f) {
                    rotate(
                        degrees = p.spin * progress.value + i * 12f,
                        pivot = Offset(x, y)
                    ) {
                        drawRect(
                            color = p.color,
                            topLeft = Offset(x, y),
                            size = Size(p.size, p.size * (1.2f + (i % 3) * 0.25f)),
                            alpha = alpha
                        )
                    }
                }
            }
        }
    }
}
