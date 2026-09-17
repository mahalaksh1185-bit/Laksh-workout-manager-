package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.FlameOrange
import com.example.ui.theme.NeonLime
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningYellow
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

data class Particle(
    val initialX: Float,
    val initialY: Float,
    val angle: Float,
    val speed: Float,
    val color: Color,
    val size: Float,
    val rotationSpeed: Float
)

@Composable
fun CelebrationOverlay(
    visible: Boolean,
    title: String = "WORKOUT CRUSHED!",
    subtitle: String = "New Personal Best & Streak Milestone",
    metric1Label: String = "Volume Lifted",
    metric1Value: String = "1,450 kg",
    metric2Label: String = "Current Streak",
    metric2Value: String = "🔥 4 Days",
    onDismiss: () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(300)) + scaleIn(spring(dampingRatio = 0.65f)),
        exit = fadeOut(tween(250)) + scaleOut(tween(250))
    ) {
        val animProgress = remember { Animatable(0f) }
        val infiniteTransition = rememberInfiniteTransition(label = "halo")
        val haloRotation by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(4000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "halo_rotate"
        )
        val pulseScale by infiniteTransition.animateFloat(
            initialValue = 0.96f,
            targetValue = 1.04f,
            animationSpec = infiniteRepeatable(
                animation = tween(800, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulse_scale"
        )

        // Generate dynamic energetic particles
        val particles = remember {
            val colors = listOf(NeonLime, ElectricCyan, WarningYellow, FlameOrange, Color.White)
            List(40) {
                Particle(
                    initialX = 0f,
                    initialY = 0f,
                    angle = (it * (360f / 40f)) + Random.nextFloat() * 8f,
                    speed = Random.nextFloat() * 260f + 120f,
                    color = colors[it % colors.size],
                    size = Random.nextFloat() * 6f + 4f,
                    rotationSpeed = Random.nextFloat() * 180f
                )
            }
        }

        LaunchedEffect(visible) {
            if (visible) {
                animProgress.snapTo(0f)
                animProgress.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(1200, easing = FastOutSlowInEasing)
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.85f))
                .testTag("celebration_overlay"),
            contentAlignment = Alignment.Center
        ) {
            // Particle burst Canvas
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2f, size.height / 2f - 40f)
                val progress = animProgress.value

                particles.forEach { p ->
                    val radians = Math.toRadians(p.angle.toDouble())
                    val dist = p.speed * progress
                    val x = center.x + (cos(radians) * dist).toFloat()
                    val y = center.y + (sin(radians) * dist).toFloat() + (progress * progress * 80f) // gravity pull
                    val alpha = (1f - progress).coerceIn(0f, 1f)

                    drawCircle(
                        color = p.color.copy(alpha = alpha),
                        radius = p.size * (1f - progress * 0.4f),
                        center = Offset(x, y)
                    )
                }
            }

            // Glassmorphic Victory Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .clip(RoundedCornerShape(28.dp))
                    .border(
                        2.dp,
                        Brush.sweepGradient(listOf(NeonLime, ElectricCyan, WarningYellow, NeonLime)),
                        RoundedCornerShape(28.dp)
                    )
                    .scale(pulseScale),
                color = DarkBackground,
                tonalElevation = 12.dp
            ) {
                Column(
                    modifier = Modifier.padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Trophy / Fire Icon with glowing halo
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .rotate(haloRotation)
                            .background(
                                Brush.radialGradient(
                                    listOf(NeonLime.copy(alpha = 0.35f), Color.Transparent)
                                ),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(76.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(listOf(WarningYellow, FlameOrange))
                                )
                                .border(2.dp, NeonLime, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = "Trophy",
                                tint = Color(0xFF0A0E13),
                                modifier = Modifier.size(44.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = title,
                        color = NeonLime,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = subtitle,
                        color = TextSecondary,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Metrics Comparison Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(DarkSurfaceVariant)
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = metric1Label,
                                color = TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = metric1Value,
                                color = ElectricCyan,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Box(
                            modifier = Modifier
                                .height(36.dp)
                                .width(1.dp)
                                .background(Color.White.copy(alpha = 0.15f))
                        )

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = metric2Label,
                                color = TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = metric2Value,
                                color = FlameOrange,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(26.dp))

                    NeonButton(
                        text = "CLAIM PROGRESS",
                        onClick = onDismiss,
                        icon = Icons.Default.Check,
                        testTag = "celebration_claim_button"
                    )
                }
            }
        }
    }
}
