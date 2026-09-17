package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.GymViewModel
import com.example.ui.MainNavTab
import com.example.ui.components.GlassCard
import com.example.ui.components.NeonButton
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.FlameOrange
import com.example.ui.theme.NeonLime
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun DashboardScreen(
    viewModel: GymViewModel,
    onOpenMusicPlayer: () -> Unit
) {
    val gymConfig by viewModel.gymConfig.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val today = viewModel.selectedDay.collectAsState().value
    val weeklyPlans by viewModel.workoutPlans.collectAsState()
    val alarms by viewModel.smartAlarms.collectAsState()
    val todayPlan = weeklyPlans.firstOrNull { it.dayOfWeek == today }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 20.dp)
            .testTag("dashboard_screen"),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(12.dp))

            // Gym Branding & Athlete Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = gymConfig?.gymName?.ifBlank { "Titan Gym" } ?: "Titan Gym",
                        color = NeonLime,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Hello, ${userProfile?.name?.ifBlank { "Athlete" } ?: "Athlete"} 👋",
                        color = TextPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                // Switch Role / Mode Badge
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            val newRole = if (userProfile?.currentRole == "ADMIN") "MEMBER" else "ADMIN"
                            viewModel.switchRole(newRole)
                        }
                        .testTag("dashboard_role_switch"),
                    color = DarkSurfaceVariant,
                    border = BorderStroke(1.dp, NeonLime.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = null,
                            tint = NeonLime,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (userProfile?.currentRole == "ADMIN") "Admin" else "Member",
                            color = TextPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Today's Main Workout Card (Large Workout Button!)
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = NeonLime.copy(alpha = 0.6f),
                backgroundColor = Color(0xFF141D26)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(NeonLime.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FitnessCenter,
                                    contentDescription = null,
                                    tint = NeonLime,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "TODAY'S WORKOUT",
                                    color = NeonLime,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = today,
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        // Target calories badge
                        if (todayPlan != null && !todayPlan.isRestDay && todayPlan.targetCalories > 0) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = FlameOrange.copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, FlameOrange.copy(alpha = 0.4f))
                            ) {
                                Text(
                                    text = "🔥 ~${todayPlan.targetCalories} kcal",
                                    color = FlameOrange,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        } else if (todayPlan?.isRestDay == true) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = ElectricCyan.copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, ElectricCyan.copy(alpha = 0.4f))
                            ) {
                                Text(
                                    text = "💤 REST DAY",
                                    color = ElectricCyan,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    val displayTitle = when {
                        todayPlan == null -> "No Workout Planned"
                        todayPlan.isRestDay -> "Rest & Recovery Day"
                        else -> todayPlan.workoutName.ifBlank { todayPlan.muscleGroup }
                    }

                    val displaySub = when {
                        todayPlan == null -> "Configure your schedule or add exercises to get started."
                        todayPlan.isRestDay -> "Hydrate, stretch, and let your body recover for optimal growth."
                        else -> todayPlan.notes.ifBlank { "Ready to train ${todayPlan.muscleGroup} today!" }
                    }

                    Text(
                        text = displayTitle,
                        color = TextPrimary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = displaySub,
                        color = TextSecondary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Main Action Button
                    Button(
                        onClick = {
                            viewModel.setSelectedDay(today)
                            viewModel.selectTab(MainNavTab.WORKOUT)
                            if (todayPlan != null && !todayPlan.isRestDay) {
                                viewModel.startWorkoutSession(
                                    dayOfWeek = today,
                                    workoutTitle = todayPlan.workoutName.ifBlank { todayPlan.muscleGroup }
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(58.dp)
                            .testTag("start_today_workout_button"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonLime,
                            contentColor = Color(0xFF0A0E13)
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = if (todayPlan == null || todayPlan.isRestDay) Icons.Default.FitnessCenter else Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            val buttonText = when {
                                todayPlan == null -> "PLAN TODAY'S WORKOUT"
                                todayPlan.isRestDay -> "VIEW WEEKLY SCHEDULE"
                                else -> "START TODAY'S WORKOUT"
                            }
                            Text(
                                text = buttonText,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }
            }
        }

        // Quick Stats Row (Starting -> Current Weight, Streaks, Workouts Completed)
        item {
            Text(
                text = "Performance & Metrics",
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Weight Tracking Stat Card
                val starting = userProfile?.startingWeightKg ?: 75f
                val current = userProfile?.currentWeightKg ?: 75f
                val diff = current - starting

                GlassCard(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { viewModel.selectTab(MainNavTab.PROGRESS) }
                        .testTag("dashboard_weight_stat_card")
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.MonitorWeight,
                                contentDescription = null,
                                tint = ElectricCyan,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = if (diff <= 0f) "${String.format("%.1f", diff)} kg" else "+${String.format("%.1f", diff)} kg",
                                color = if (diff <= 0f) NeonLime else FlameOrange,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${current} kg",
                            color = TextPrimary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "Start: ${starting} kg",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                // Workouts Completed Stat Card
                GlassCard(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { viewModel.selectTab(MainNavTab.PROGRESS) }
                        .testTag("dashboard_workouts_stat_card")
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = WarningYellowCustom,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${userProfile?.totalWorkoutsCompleted ?: 0}",
                            color = TextPrimary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "Workouts Done",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                // Streak Stat Card
                GlassCard(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { viewModel.selectTab(MainNavTab.PROGRESS) }
                        .testTag("dashboard_streak_stat_card")
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = null,
                            tint = FlameOrange,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${userProfile?.currentStreak ?: 0} 🔥",
                            color = TextPrimary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "Day Streak",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        // Smart Alarm & Next Schedule Card
        item {
            val enabledAlarm = alarms.firstOrNull { it.isEnabled }
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.selectTab(MainNavTab.ALARMS) }
                    .testTag("dashboard_alarm_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(ElectricCyan.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Alarm,
                                contentDescription = null,
                                tint = ElectricCyan,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Next Smart Alarm",
                                color = TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = enabledAlarm?.title ?: "No Alarms Configured",
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (enabledAlarm != null) "${enabledAlarm.timeFormatted} • ${enabledAlarm.repeatDays}" else "Tap to set a workout alarm & reminder",
                                color = if (enabledAlarm != null) ElectricCyan else TextMuted,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Open Alarms",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Quick MP3 Workout Tunes Player Banner
        item {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenMusicPlayer() }
                    .testTag("dashboard_music_card"),
                borderColor = ElectricCyan.copy(alpha = 0.4f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(listOf(NeonLime, ElectricCyan))
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color(0xFF0A0E13),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Workout Music Engine 🎵",
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Play pump-up beats or load songs from storage",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = NeonLime.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, NeonLime.copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = "PLAYER",
                            color = NeonLime,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

val WarningYellowCustom = Color(0xFFFBBF24)
