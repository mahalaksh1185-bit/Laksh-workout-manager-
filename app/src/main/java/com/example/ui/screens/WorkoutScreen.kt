package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DriveFileMove
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ExerciseCatalog
import com.example.data.model.ExerciseEntity
import com.example.data.model.WorkoutPlanEntity
import com.example.ui.GymViewModel
import com.example.ui.components.GlassCard
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.FlameOrange
import com.example.ui.theme.NeonLime
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.util.Locale

@Composable
fun WorkoutScreen(
    viewModel: GymViewModel,
    onShowCelebration: (volume: Float, streak: Int) -> Unit
) {
    val selectedDay by viewModel.selectedDay.collectAsState()
    val weeklyPlans by viewModel.workoutPlans.collectAsState()
    val allExercises by viewModel.allExercises.collectAsState()
    val activeSession by viewModel.activeSession.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()

    val dayExercises = remember(selectedDay, allExercises) {
        allExercises.filter { it.planDayOfWeek == selectedDay }
            .sortedBy { it.orderIndex }
    }
    val currentPlan = weeklyPlans.firstOrNull { it.dayOfWeek == selectedDay }

    val daysOfWeek = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")

    var showConfigureDayDialog by remember { mutableStateOf(false) }
    var showAddExerciseDialog by remember { mutableStateOf(false) }
    var editingExercise by remember { mutableStateOf<ExerciseEntity?>(null) }
    var movingExercise by remember { mutableStateOf<ExerciseEntity?>(null) }
    var showInstructionsExercise by remember { mutableStateOf<ExerciseEntity?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .testTag("workout_screen")
    ) {
        // 1. Top Weekly Schedule Tabs (Mon - Sun)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurface)
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            daysOfWeek.forEach { day ->
                val isSelected = day == selectedDay
                val plan = weeklyPlans.firstOrNull { it.dayOfWeek == day }
                val daySubtitle = when {
                    plan == null -> "Unset"
                    plan.isRestDay -> "Rest"
                    else -> plan.workoutName.ifBlank { plan.muscleGroup }.take(7)
                }

                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { viewModel.setSelectedDay(day) }
                        .testTag("day_tab_$day"),
                    color = if (isSelected) NeonLime else DarkSurfaceVariant,
                    border = BorderStroke(
                        1.dp,
                        if (isSelected) NeonLime else DarkCardBorder
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = day.take(3).uppercase(Locale.getDefault()),
                            color = if (isSelected) Color(0xFF0A0E13) else TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = daySubtitle,
                            color = if (isSelected) Color(0xFF0A0E13) else TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }

        // 2. Active Rest Timer Banner
        AnimatedVisibility(visible = activeSession.isRestTimerRunning) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .testTag("rest_timer_banner"),
                color = Color(0xFF2E1700),
                border = BorderStroke(1.dp, FlameOrange)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = FlameOrange,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "REST TIME: ${activeSession.restTimeRemaining}s",
                                color = FlameOrange,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = "Catch your breath & hydrate for next set",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    TextButton(onClick = { viewModel.stopRestTimer() }) {
                        Text("SKIP", color = TextPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 3. Live Workout Session In-Progress Bar
        if (activeSession.isActive && activeSession.dayOfWeek == selectedDay) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .testTag("active_workout_header"),
                color = DarkSurfaceVariant,
                border = BorderStroke(1.dp, NeonLime.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(NeonLime)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        val minutes = activeSession.elapsedSeconds / 60
                        val seconds = activeSession.elapsedSeconds % 60
                        Text(
                            text = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds),
                            color = NeonLime,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "(${activeSession.completedExercises.size}/${dayExercises.size} Completed)",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }

                    Row {
                        TextButton(onClick = { viewModel.cancelWorkoutSession() }) {
                            Text("Cancel", color = ErrorRed, fontSize = 12.sp)
                        }
                        Button(
                            onClick = {
                                var totalVol = 0f
                                for (ex in dayExercises) {
                                    totalVol += (ex.sets * ex.reps * ex.weightKg)
                                }
                                val streak = (userProfile?.currentStreak ?: 0) + 1
                                viewModel.finishWorkoutSession(dayExercises)
                                onShowCelebration(totalVol, streak)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonLime, contentColor = Color(0xFF0A0E13)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("finish_workout_button")
                        ) {
                            Text("FINISH", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // 4. Main Scrollable Content
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(10.dp))

                // Day Plan Header & Configuration Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = selectedDay,
                                color = NeonLime,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            if (currentPlan?.isRestDay == true) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = ElectricCyan.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "REST DAY",
                                        color = ElectricCyan,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        val titleText = when {
                            currentPlan == null -> "Schedule Not Set"
                            currentPlan.isRestDay -> "Rest & Recovery Day"
                            else -> currentPlan.workoutName.ifBlank { currentPlan.muscleGroup }
                        }

                        Text(
                            text = titleText,
                            color = TextPrimary,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black
                        )

                        val subText = when {
                            currentPlan == null -> "Tap 'Configure Day' to assign workout or set rest day"
                            currentPlan.isRestDay -> "Muscle recovery & hydration. No intense lifts today."
                            else -> "${dayExercises.size} Exercises • ~${currentPlan.estimatedMinutes} mins • ~${currentPlan.targetCalories} kcal"
                        }

                        Text(
                            text = subText,
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Configure Day Button
                        IconButton(
                            onClick = { showConfigureDayDialog = true },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(DarkSurfaceVariant)
                                .testTag("configure_day_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Configure Day",
                                tint = NeonLime,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Add Exercise Button (Option A & Option B)
                        IconButton(
                            onClick = { showAddExerciseDialog = true },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(NeonLime)
                                .testTag("add_exercise_icon_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Exercise",
                                tint = Color(0xFF0A0E13),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }

            // Start Workout Session Button (Only shown when not resting and has exercises)
            if (!activeSession.isActive && currentPlan?.isRestDay != true && dayExercises.isNotEmpty()) {
                item {
                    Button(
                        onClick = {
                            viewModel.startWorkoutSession(
                                dayOfWeek = selectedDay,
                                workoutTitle = currentPlan?.workoutName?.ifBlank { currentPlan.muscleGroup } ?: "$selectedDay Workout"
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("start_workout_session_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonLime,
                            contentColor = Color(0xFF0A0E13)
                        )
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "START $selectedDay.uppercase() WORKOUT",
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }
            }

            // Zero-State Display for Day
            if (currentPlan?.isRestDay == true) {
                item {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(30.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Hotel,
                                contentDescription = null,
                                tint = ElectricCyan,
                                modifier = Modifier.size(46.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "$selectedDay is a Rest Day",
                                color = TextPrimary,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Allow muscles to rebuild and glycogen stores to replenish. You can configure this day as a workout day anytime.",
                                color = TextSecondary,
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { showConfigureDayDialog = true },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ElectricCyan.copy(alpha = 0.2f),
                                    contentColor = ElectricCyan
                                ),
                                border = BorderStroke(1.dp, ElectricCyan.copy(alpha = 0.4f)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("CHANGE TO WORKOUT DAY", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            } else if (dayExercises.isEmpty()) {
                item {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.FitnessCenter,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(46.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No Exercises Added for $selectedDay",
                                color = TextPrimary,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Choose from the Exercise Library or create custom exercises to start building your workout.",
                                color = TextSecondary,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Button(
                                    onClick = { showAddExerciseDialog = true },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = NeonLime,
                                        contentColor = Color(0xFF0A0E13)
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("ADD EXERCISE", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }

                                Button(
                                    onClick = { showConfigureDayDialog = true },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = DarkSurfaceVariant,
                                        contentColor = TextPrimary
                                    ),
                                    border = BorderStroke(1.dp, DarkCardBorder),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("SET AS REST DAY", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Exercise Cards with Reordering, Move to Day, Edit, Delete
            itemsIndexed(dayExercises, key = { _, it -> it.id }) { index, exercise ->
                val isDoneInActive = activeSession.completedExercises.contains(exercise.id)

                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("exercise_item_${exercise.id}"),
                    borderColor = if (isDoneInActive) SuccessGreen.copy(alpha = 0.6f) else DarkCardBorder
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                if (activeSession.isActive) {
                                    IconButton(
                                        onClick = {
                                            viewModel.toggleExerciseDoneInSession(
                                                exercise.id,
                                                exercise.restTimeSec
                                            )
                                        },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isDoneInActive) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                            contentDescription = "Complete Set",
                                            tint = if (isDoneInActive) SuccessGreen else TextSecondary,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                }

                                Column {
                                    Text(
                                        text = exercise.name,
                                        color = if (isDoneInActive) SuccessGreen else TextPrimary,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = ElectricCyan.copy(alpha = 0.15f)
                                        ) {
                                            Text(
                                                text = exercise.muscleGroup,
                                                color = ElectricCyan,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "• ${exercise.equipment}",
                                            color = TextSecondary,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }

                            // Exercise Actions Toolbar: Up, Down, Move Day, Info, Edit, Delete
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Reorder Up
                                IconButton(
                                    onClick = { viewModel.reorderExercise(exercise, -1) },
                                    enabled = index > 0,
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowUpward,
                                        contentDescription = "Move Up",
                                        tint = if (index > 0) TextSecondary else TextMuted.copy(alpha = 0.3f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                // Reorder Down
                                IconButton(
                                    onClick = { viewModel.reorderExercise(exercise, 1) },
                                    enabled = index < dayExercises.size - 1,
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowDownward,
                                        contentDescription = "Move Down",
                                        tint = if (index < dayExercises.size - 1) TextSecondary else TextMuted.copy(alpha = 0.3f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                // Move to Another Day
                                IconButton(
                                    onClick = { movingExercise = exercise },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.SwapHoriz,
                                        contentDescription = "Move to Day",
                                        tint = ElectricCyan,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                // Info / Technique
                                IconButton(
                                    onClick = { showInstructionsExercise = exercise },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = "Instructions",
                                        tint = TextSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                // Edit Details
                                IconButton(
                                    onClick = { editingExercise = exercise },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit",
                                        tint = NeonLime,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                // Delete
                                IconButton(
                                    onClick = { viewModel.deleteExercise(exercise) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = ErrorRed,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Exercise Specs: Sets, Reps, Weight, Rest
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(DarkSurfaceVariant)
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SpecItem(label = "SETS", value = "${exercise.sets}")
                            SpecDivider()
                            SpecItem(label = "REPS", value = "${exercise.reps}")
                            SpecDivider()
                            SpecItem(label = "WEIGHT", value = "${exercise.weightKg} kg")
                            SpecDivider()
                            SpecItem(label = "REST", value = "${exercise.restTimeSec}s")
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }

    // --- DIALOG 1: Configure Day (Workout Day vs Rest Day) ---
    if (showConfigureDayDialog) {
        ConfigureDayDialog(
            dayOfWeek = selectedDay,
            currentPlan = currentPlan,
            onDismiss = { showConfigureDayDialog = false },
            onSave = { name, muscle, isRest, duration, calories, notes ->
                viewModel.saveOrUpdateWorkoutPlan(
                    day = selectedDay,
                    workoutName = name,
                    muscleGroup = muscle,
                    isRestDay = isRest,
                    targetCalories = calories,
                    estimatedMinutes = duration,
                    notes = notes
                )
                showConfigureDayDialog = false
            },
            onClear = {
                viewModel.deleteWorkoutPlan(selectedDay)
                showConfigureDayDialog = false
            }
        )
    }

    // --- DIALOG 2: Add Exercise (Option A: Library, Option B: Custom) ---
    if (showAddExerciseDialog) {
        AddExerciseDialogWithOptions(
            dayOfWeek = selectedDay,
            onDismiss = { showAddExerciseDialog = false },
            onAddFromLibrary = { libraryItem, sets, reps, weight, rest ->
                viewModel.addExerciseFromLibrary(
                    dayOfWeek = selectedDay,
                    libraryItem = libraryItem,
                    sets = sets,
                    reps = reps,
                    weight = weight,
                    restTimeSec = rest
                )
                showAddExerciseDialog = false
            },
            onAddCustom = { name, muscle, equipment, sets, reps, weight, rest, instructions ->
                viewModel.addCustomExercise(
                    dayOfWeek = selectedDay,
                    name = name,
                    muscle = muscle,
                    equipment = equipment,
                    sets = sets,
                    reps = reps,
                    weight = weight,
                    restTimeSec = rest,
                    instructions = instructions
                )
                showAddExerciseDialog = false
            }
        )
    }

    // --- DIALOG 3: Move Exercise to Another Day ---
    movingExercise?.let { ex ->
        MoveExerciseDayDialog(
            exercise = ex,
            currentDay = selectedDay,
            allDays = daysOfWeek,
            onDismiss = { movingExercise = null },
            onMove = { targetDay ->
                viewModel.moveExerciseToDay(ex, targetDay)
                movingExercise = null
            }
        )
    }

    // --- DIALOG 4: Edit Exercise Details ---
    editingExercise?.let { ex ->
        EditExerciseDialog(
            exercise = ex,
            onDismiss = { editingExercise = null },
            onSave = { name, muscle, equipment, sets, reps, weight, rest, instructions ->
                viewModel.updateExerciseDetails(
                    ex.copy(
                        name = name,
                        muscleGroup = muscle,
                        equipment = equipment,
                        sets = sets,
                        reps = reps,
                        weightKg = weight,
                        restTimeSec = rest,
                        instructions = instructions
                    )
                )
                editingExercise = null
            },
            onDelete = {
                viewModel.deleteExercise(ex)
                editingExercise = null
            }
        )
    }

    // --- DIALOG 5: Instructions Modal ---
    showInstructionsExercise?.let { ex ->
        AlertDialog(
            onDismissRequest = { showInstructionsExercise = null },
            title = {
                Text(text = ex.name, color = TextPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text(
                        text = "Target Muscle: ${ex.muscleGroup} • ${ex.equipment}",
                        color = NeonLime,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = ex.instructions.ifBlank { "Perform with strict form, controlled tempo and squeeze the target muscle at peak contraction." },
                        color = TextSecondary,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showInstructionsExercise = null }) {
                    Text("CLOSE", color = NeonLime, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = DarkSurface
        )
    }
}

// -------------------------------------------------------------
// Helper UI Components and Dialogs
// -------------------------------------------------------------

@Composable
private fun SpecItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
        Text(text = value, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun SpecDivider() {
    Box(
        modifier = Modifier
            .height(24.dp)
            .width(1.dp)
            .background(Color.White.copy(alpha = 0.1f))
    )
}

/**
 * Configure Day Dialog: Allows setting Workout Day vs Rest Day, workout title, muscle, target calories, duration.
 */
@Composable
fun ConfigureDayDialog(
    dayOfWeek: String,
    currentPlan: WorkoutPlanEntity?,
    onDismiss: () -> Unit,
    onSave: (name: String, muscle: String, isRest: Boolean, duration: Int, calories: Int, notes: String) -> Unit,
    onClear: () -> Unit
) {
    var isRestDay by remember { mutableStateOf(currentPlan?.isRestDay ?: false) }
    var workoutName by remember { mutableStateOf(currentPlan?.workoutName ?: "") }
    var muscleGroup by remember { mutableStateOf(currentPlan?.muscleGroup ?: "Chest & Triceps") }
    var durationText by remember { mutableStateOf((currentPlan?.estimatedMinutes ?: 60).toString()) }
    var caloriesText by remember { mutableStateOf((currentPlan?.targetCalories ?: 450).toString()) }
    var notes by remember { mutableStateOf(currentPlan?.notes ?: "") }

    val presetNames = listOf(
        "Chest & Triceps", "Back & Biceps", "Legs & Abs",
        "Shoulders & Arms", "Push Day", "Pull Day", "Leg Day", "Full Body", "Cardio & Core"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Configure $dayOfWeek",
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Workout vs Rest Day Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(DarkSurfaceVariant)
                        .padding(4.dp)
                ) {
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { isRestDay = false },
                        color = if (!isRestDay) NeonLime else Color.Transparent
                    ) {
                        Text(
                            text = "WORKOUT DAY",
                            color = if (!isRestDay) Color(0xFF0A0E13) else TextSecondary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(vertical = 10.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { isRestDay = true },
                        color = if (isRestDay) ElectricCyan else Color.Transparent
                    ) {
                        Text(
                            text = "REST DAY",
                            color = if (isRestDay) Color(0xFF0A0E13) else TextSecondary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(vertical = 10.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }

                if (isRestDay) {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "Rest & Muscle Repair",
                                color = ElectricCyan,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Marking $dayOfWeek as a rest day allows muscle synthesis and central nervous system recovery.",
                                color = TextSecondary,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                } else {
                    // Workout Day Settings
                    Text(
                        text = "Quick Presets:",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        presetNames.forEach { preset ->
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        workoutName = preset
                                        muscleGroup = preset
                                    },
                                color = if (workoutName == preset) NeonLime.copy(alpha = 0.2f) else DarkSurfaceVariant,
                                border = BorderStroke(1.dp, if (workoutName == preset) NeonLime else DarkCardBorder)
                            ) {
                                Text(
                                    text = preset,
                                    color = if (workoutName == preset) NeonLime else TextPrimary,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = workoutName,
                        onValueChange = { workoutName = it },
                        label = { Text("Workout Name (e.g. Push Hypertrophy)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonLime,
                            unfocusedBorderColor = DarkCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    OutlinedTextField(
                        value = muscleGroup,
                        onValueChange = { muscleGroup = it },
                        label = { Text("Target Muscle Groups") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonLime,
                            unfocusedBorderColor = DarkCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    Row(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = durationText,
                            onValueChange = { durationText = it },
                            label = { Text("Est. Minutes") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonLime,
                                unfocusedBorderColor = DarkCardBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedTextField(
                            value = caloriesText,
                            onValueChange = { caloriesText = it },
                            label = { Text("Target Calories") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonLime,
                                unfocusedBorderColor = DarkCardBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )
                    }

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Session Notes / Focus") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonLime,
                            unfocusedBorderColor = DarkCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalName = if (isRestDay) "Rest Day" else workoutName.ifBlank { muscleGroup }
                    val dur = durationText.toIntOrNull() ?: 60
                    val cal = caloriesText.toIntOrNull() ?: 450
                    onSave(finalName, muscleGroup, isRestDay, dur, cal, notes)
                },
                colors = ButtonDefaults.buttonColors(containerColor = NeonLime, contentColor = Color(0xFF0A0E13))
            ) {
                Text("SAVE SCHEDULE", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            Row {
                if (currentPlan != null) {
                    TextButton(onClick = onClear) {
                        Text("RESET", color = ErrorRed)
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text("CANCEL", color = TextSecondary)
                }
            }
        },
        containerColor = DarkSurface
    )
}

/**
 * Add Exercise Dialog with Option A: Choose from Library & Option B: Create Custom Exercise.
 */
@Composable
fun AddExerciseDialogWithOptions(
    dayOfWeek: String,
    onDismiss: () -> Unit,
    onAddFromLibrary: (libraryItem: ExerciseEntity, sets: Int, reps: Int, weight: Float, rest: Int) -> Unit,
    onAddCustom: (name: String, muscle: String, equipment: String, sets: Int, reps: Int, weight: Float, rest: Int, instructions: String) -> Unit
) {
    var selectedOptionTab by remember { mutableStateOf(0) } // 0 = Option A (Library), 1 = Option B (Custom)

    // Option A State
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var selectedLibraryItem by remember { mutableStateOf<ExerciseEntity?>(null) }
    var libSets by remember { mutableStateOf("3") }
    var libReps by remember { mutableStateOf("10") }
    var libWeight by remember { mutableStateOf("20") }
    var libRest by remember { mutableStateOf("60") }

    // Option B State
    var customName by remember { mutableStateOf("") }
    var customMuscle by remember { mutableStateOf("Chest") }
    var customEquipment by remember { mutableStateOf("Dumbbells") }
    var customSets by remember { mutableStateOf("3") }
    var customReps by remember { mutableStateOf("10") }
    var customWeight by remember { mutableStateOf("20") }
    var customRest by remember { mutableStateOf("60") }
    var customInstructions by remember { mutableStateOf("") }

    val categories = listOf("All", "Chest", "Back", "Legs", "Shoulders", "Triceps", "Biceps", "Core", "Cardio")

    val filteredLibrary = remember(searchQuery, selectedCategory) {
        ExerciseCatalog.defaultExercises.filter { ex ->
            val matchCat = if (selectedCategory == "All") true else ex.muscleGroup.contains(selectedCategory, ignoreCase = true)
            val matchQuery = if (searchQuery.isBlank()) true else {
                ex.name.contains(searchQuery, ignoreCase = true) ||
                    ex.muscleGroup.contains(searchQuery, ignoreCase = true) ||
                    ex.equipment.contains(searchQuery, ignoreCase = true)
            }
            matchCat && matchQuery
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Add Exercise to $dayOfWeek",
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(480.dp)
            ) {
                // Tab Selection for Option A vs Option B
                TabRow(
                    selectedTabIndex = selectedOptionTab,
                    containerColor = DarkSurfaceVariant,
                    contentColor = NeonLime,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedOptionTab]),
                            color = NeonLime
                        )
                    }
                ) {
                    Tab(
                        selected = selectedOptionTab == 0,
                        onClick = { selectedOptionTab = 0 },
                        text = {
                            Text(
                                "Option A: Library",
                                fontWeight = if (selectedOptionTab == 0) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp
                            )
                        }
                    )
                    Tab(
                        selected = selectedOptionTab == 1,
                        onClick = { selectedOptionTab = 1 },
                        text = {
                            Text(
                                "Option B: Custom",
                                fontWeight = if (selectedOptionTab == 1) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp
                            )
                        }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (selectedOptionTab == 0) {
                    // ------------------ OPTION A: LIBRARY ------------------
                    if (selectedLibraryItem == null) {
                        // Search & category chips
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search library...", color = TextMuted, fontSize = 12.sp) },
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = TextSecondary)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonLime,
                                unfocusedBorderColor = DarkCardBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            categories.forEach { cat ->
                                val isSel = selectedCategory == cat
                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .clickable { selectedCategory = cat },
                                    color = if (isSel) NeonLime else DarkSurfaceVariant,
                                    border = BorderStroke(1.dp, if (isSel) NeonLime else DarkCardBorder)
                                ) {
                                    Text(
                                        text = cat,
                                        color = if (isSel) Color(0xFF0A0E13) else TextPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(filteredLibrary, key = { it.name }) { ex ->
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable {
                                            selectedLibraryItem = ex
                                            libSets = ex.sets.toString()
                                            libReps = ex.reps.toString()
                                            libWeight = ex.weightKg.toInt().toString()
                                            libRest = ex.restTimeSec.toString()
                                        },
                                    color = DarkSurfaceVariant,
                                    border = BorderStroke(1.dp, DarkCardBorder)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = ex.name,
                                                color = TextPrimary,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "${ex.muscleGroup} • ${ex.equipment}",
                                                color = ElectricCyan,
                                                fontSize = 11.sp
                                            )
                                        }
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                            contentDescription = "Select",
                                            tint = NeonLime,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        // Adjust sets, reps, weight, rest for chosen exercise
                        val item = selectedLibraryItem!!
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.name,
                                        color = NeonLime,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                    Text(
                                        text = "${item.muscleGroup} • ${item.equipment}",
                                        color = TextSecondary,
                                        fontSize = 12.sp
                                    )
                                }
                                TextButton(onClick = { selectedLibraryItem = null }) {
                                    Text("Change", color = ElectricCyan, fontSize = 12.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Text("Adjust Sets & Reps:", color = TextSecondary, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(6.dp))

                            Row(modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = libSets,
                                    onValueChange = { libSets = it },
                                    label = { Text("Sets") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = NeonLime,
                                        unfocusedBorderColor = DarkCardBorder,
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary
                                    )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                OutlinedTextField(
                                    value = libReps,
                                    onValueChange = { libReps = it },
                                    label = { Text("Reps") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = NeonLime,
                                        unfocusedBorderColor = DarkCardBorder,
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = libWeight,
                                    onValueChange = { libWeight = it },
                                    label = { Text("Weight (kg)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = NeonLime,
                                        unfocusedBorderColor = DarkCardBorder,
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary
                                    )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                OutlinedTextField(
                                    value = libRest,
                                    onValueChange = { libRest = it },
                                    label = { Text("Rest (s)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = NeonLime,
                                        unfocusedBorderColor = DarkCardBorder,
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Button(
                                onClick = {
                                    val s = libSets.toIntOrNull() ?: item.sets
                                    val r = libReps.toIntOrNull() ?: item.reps
                                    val w = libWeight.toFloatOrNull() ?: item.weightKg
                                    val rest = libRest.toIntOrNull() ?: item.restTimeSec
                                    onAddFromLibrary(item, s, r, w, rest)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = NeonLime, contentColor = Color(0xFF0A0E13)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("ADD TO $dayOfWeek.uppercase()", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
                    // ------------------ OPTION B: CUSTOM ------------------
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = customName,
                            onValueChange = { customName = it },
                            label = { Text("Custom Exercise Name *") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonLime,
                                unfocusedBorderColor = DarkCardBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )

                        Row(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = customMuscle,
                                onValueChange = { customMuscle = it },
                                label = { Text("Muscle (e.g. Chest)") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NeonLime,
                                    unfocusedBorderColor = DarkCardBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedTextField(
                                value = customEquipment,
                                onValueChange = { customEquipment = it },
                                label = { Text("Equipment (e.g. Barbell)") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NeonLime,
                                    unfocusedBorderColor = DarkCardBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                )
                            )
                        }

                        Row(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = customSets,
                                onValueChange = { customSets = it },
                                label = { Text("Sets") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NeonLime,
                                    unfocusedBorderColor = DarkCardBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedTextField(
                                value = customReps,
                                onValueChange = { customReps = it },
                                label = { Text("Reps") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NeonLime,
                                    unfocusedBorderColor = DarkCardBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                )
                            )
                        }

                        Row(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = customWeight,
                                onValueChange = { customWeight = it },
                                label = { Text("Weight (kg)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NeonLime,
                                    unfocusedBorderColor = DarkCardBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedTextField(
                                value = customRest,
                                onValueChange = { customRest = it },
                                label = { Text("Rest (s)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NeonLime,
                                    unfocusedBorderColor = DarkCardBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                )
                            )
                        }

                        OutlinedTextField(
                            value = customInstructions,
                            onValueChange = { customInstructions = it },
                            label = { Text("Technique & Execution Notes") },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 2,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonLime,
                                unfocusedBorderColor = DarkCardBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Button(
                            onClick = {
                                if (customName.isNotBlank()) {
                                    val s = customSets.toIntOrNull() ?: 3
                                    val r = customReps.toIntOrNull() ?: 10
                                    val w = customWeight.toFloatOrNull() ?: 20f
                                    val rest = customRest.toIntOrNull() ?: 60
                                    onAddCustom(
                                        customName,
                                        customMuscle.ifBlank { "Full Body" },
                                        customEquipment.ifBlank { "Bodyweight" },
                                        s,
                                        r,
                                        w,
                                        rest,
                                        customInstructions
                                    )
                                }
                            },
                            enabled = customName.isNotBlank(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NeonLime, contentColor = Color(0xFF0A0E13)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("CREATE & ADD EXERCISE", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CLOSE", color = TextSecondary)
            }
        },
        containerColor = DarkSurface
    )
}

/**
 * Move Exercise to Another Day Dialog.
 */
@Composable
fun MoveExerciseDayDialog(
    exercise: ExerciseEntity,
    currentDay: String,
    allDays: List<String>,
    onDismiss: () -> Unit,
    onMove: (targetDay: String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Move Exercise", color = TextPrimary, fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                Text(
                    text = "Move \"${exercise.name}\" from $currentDay to another day:",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(14.dp))

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    allDays.forEach { day ->
                        val isCurrent = day == currentDay
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable(enabled = !isCurrent) { onMove(day) },
                            color = if (isCurrent) DarkSurfaceVariant.copy(alpha = 0.4f) else DarkSurfaceVariant,
                            border = BorderStroke(1.dp, if (isCurrent) Color.Transparent else DarkCardBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = day,
                                    color = if (isCurrent) TextMuted else TextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = if (isCurrent) FontWeight.Normal else FontWeight.SemiBold
                                )
                                if (isCurrent) {
                                    Text("(Current)", color = TextMuted, fontSize = 12.sp)
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.DriveFileMove,
                                        contentDescription = "Move here",
                                        tint = NeonLime,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", color = TextSecondary)
            }
        },
        containerColor = DarkSurface
    )
}

/**
 * Edit Exercise Dialog.
 */
@Composable
fun EditExerciseDialog(
    exercise: ExerciseEntity,
    onDismiss: () -> Unit,
    onSave: (name: String, muscle: String, equipment: String, sets: Int, reps: Int, weight: Float, rest: Int, instructions: String) -> Unit,
    onDelete: () -> Unit
) {
    var name by remember { mutableStateOf(exercise.name) }
    var muscle by remember { mutableStateOf(exercise.muscleGroup) }
    var equipment by remember { mutableStateOf(exercise.equipment) }
    var sets by remember { mutableStateOf(exercise.sets.toString()) }
    var reps by remember { mutableStateOf(exercise.reps.toString()) }
    var weight by remember { mutableStateOf(exercise.weightKg.toString()) }
    var rest by remember { mutableStateOf(exercise.restTimeSec.toString()) }
    var instructions by remember { mutableStateOf(exercise.instructions) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Edit Exercise",
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Exercise Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonLime,
                        unfocusedBorderColor = DarkCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = muscle,
                        onValueChange = { muscle = it },
                        label = { Text("Muscle Group") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonLime,
                            unfocusedBorderColor = DarkCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = equipment,
                        onValueChange = { equipment = it },
                        label = { Text("Equipment") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonLime,
                            unfocusedBorderColor = DarkCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                }

                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = sets,
                        onValueChange = { sets = it },
                        label = { Text("Sets") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonLime,
                            unfocusedBorderColor = DarkCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = reps,
                        onValueChange = { reps = it },
                        label = { Text("Reps") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonLime,
                            unfocusedBorderColor = DarkCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                }

                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = weight,
                        onValueChange = { weight = it },
                        label = { Text("Weight (kg)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonLime,
                            unfocusedBorderColor = DarkCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = rest,
                        onValueChange = { rest = it },
                        label = { Text("Rest (sec)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonLime,
                            unfocusedBorderColor = DarkCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                }

                OutlinedTextField(
                    value = instructions,
                    onValueChange = { instructions = it },
                    label = { Text("Technique & Instructions") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonLime,
                        unfocusedBorderColor = DarkCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(
                            name,
                            muscle,
                            equipment,
                            sets.toIntOrNull() ?: 3,
                            reps.toIntOrNull() ?: 10,
                            weight.toFloatOrNull() ?: 20f,
                            rest.toIntOrNull() ?: 60,
                            instructions
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = NeonLime, contentColor = Color(0xFF0A0E13))
            ) {
                Text("SAVE", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onDelete) {
                    Text("DELETE", color = ErrorRed)
                }
                TextButton(onClick = onDismiss) {
                    Text("CANCEL", color = TextSecondary)
                }
            }
        },
        containerColor = DarkSurface
    )
}
