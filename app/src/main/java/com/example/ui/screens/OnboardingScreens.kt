package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppStep
import com.example.ui.GymViewModel
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
fun SplashScreen(onGetStarted: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF141E28), DarkBackground, Color(0xFF090D12))
                )
            )
            .padding(28.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Glowing Dumbbell Logo
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .scale(scale)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(NeonLime.copy(alpha = 0.25f), Color.Transparent)
                        )
                    )
                    .border(2.dp, NeonLime, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.FitnessCenter,
                    contentDescription = "Gym Workout Logo",
                    tint = NeonLime,
                    modifier = Modifier.size(68.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "GYM WORKOUT",
                color = TextPrimary,
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp,
                textAlign = TextAlign.Center
            )
            Text(
                text = "MANAGER",
                color = NeonLime,
                fontSize = 30.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 4.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Track Every Rep • Monitor Weight • Elevate Power",
                color = TextSecondary,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))

            NeonButton(
                text = "GET STARTED",
                onClick = onGetStarted,
                icon = Icons.Default.ArrowForward,
                testTag = "splash_start_button"
            )
        }
    }
}

@Composable
fun GymSetupScreen(
    viewModel: GymViewModel,
    onNext: () -> Unit
) {
    val gymName by viewModel.setupGymName.collectAsState()
    val quickPicks = listOf("Titan Athletics", "Iron Core Gym", "Metro Fitness Club", "Apex Powerhouse", "Spartan Gym")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        // Step indicator
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            StepChip(number = "1", label = "Gym Name", isActive = true)
            Spacer(modifier = Modifier.width(16.dp))
            StepChip(number = "2", label = "Profile", isActive = false)
            Spacer(modifier = Modifier.width(16.dp))
            StepChip(number = "3", label = "Role", isActive = false)
        }

        Spacer(modifier = Modifier.height(36.dp))

        Text(
            text = "Set Up Your Gym",
            color = TextPrimary,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Enter your gym name or choose from popular suggestions below.",
            color = TextSecondary,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            borderColor = NeonLime.copy(alpha = 0.4f)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Gym / Club Name",
                    color = NeonLime,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = gymName,
                    onValueChange = { viewModel.setupGymName.value = it },
                    placeholder = { Text("e.g. Titan Athletics", color = TextMuted) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("gym_name_input"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonLime,
                        unfocusedBorderColor = DarkCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        cursorColor = NeonLime
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Quick Presets:",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    quickPicks.forEach { preset ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { viewModel.setupGymName.value = preset },
                            color = if (gymName == preset) NeonLime.copy(alpha = 0.18f) else DarkSurfaceVariant,
                            border = BorderStroke(1.dp, if (gymName == preset) NeonLime else Color.Transparent)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FitnessCenter,
                                    contentDescription = null,
                                    tint = if (gymName == preset) NeonLime else TextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = preset,
                                    color = if (gymName == preset) NeonLime else TextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = if (gymName == preset) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        NeonButton(
            text = "CONTINUE TO PROFILE",
            onClick = {
                if (gymName.isBlank()) {
                    viewModel.setupGymName.value = "Apex Fitness Club"
                }
                onNext()
            },
            icon = Icons.Default.ArrowForward,
            testTag = "gym_setup_next_button"
        )
    }
}

@Composable
fun ProfileSetupScreen(
    viewModel: GymViewModel,
    onNext: () -> Unit
) {
    val userName by viewModel.setupUserName.collectAsState()
    val age by viewModel.setupAge.collectAsState()
    val height by viewModel.setupHeight.collectAsState()
    val startingWeight by viewModel.setupStartingWeight.collectAsState()
    val targetWeight by viewModel.setupTargetWeight.collectAsState()
    val selectedGoal by viewModel.setupGoal.collectAsState()
    val photoUri by viewModel.setupPhotoUri.collectAsState()
    val gymTiming by viewModel.setupGymTiming.collectAsState()
    val unitPref by viewModel.setupUnitPreference.collectAsState()
    val gymName by viewModel.setupGymName.collectAsState()

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.setupPhotoUri.value = uri.toString()
        }
    }

    val goals = listOf(
        "Build Muscle & Hypertrophy",
        "Fat Loss & Lean Physique",
        "Strength & Powerlifting",
        "Endurance & General Fitness"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        // Badge
        Surface(
            color = NeonLime.copy(alpha = 0.12f),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, NeonLime.copy(alpha = 0.3f))
        ) {
            Text(
                text = "⚡ ONE-TIME PROFILE SETUP",
                color = NeonLime,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Create Your Profile",
            color = TextPrimary,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Set up your personal details to begin managing your workouts.",
            color = TextSecondary,
            fontSize = 13.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Profile Avatar Picker
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(DarkSurfaceVariant)
                .border(2.dp, ElectricCyan, CircleShape)
                .clickable {
                    photoPickerLauncher.launch(
                        androidx.activity.result.PickVisualMediaRequest(
                            ActivityResultContracts.PickVisualMedia.ImageOnly
                        )
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.PhotoCamera,
                    contentDescription = "Profile Photo",
                    tint = ElectricCyan,
                    modifier = Modifier.size(32.dp)
                )
                Text(
                    text = if (photoUri != null) "Photo Added" else "Add Photo",
                    color = ElectricCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(18.dp)) {
                OutlinedTextField(
                    value = userName,
                    onValueChange = { viewModel.setupUserName.value = it },
                    label = { Text("Your Full Name *") },
                    placeholder = { Text("e.g. Alex Johnson") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("profile_name_input"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonLime,
                        unfocusedBorderColor = DarkCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = age,
                        onValueChange = { viewModel.setupAge.value = it },
                        label = { Text("Age") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("profile_age_input"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonLime,
                            unfocusedBorderColor = DarkCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    OutlinedTextField(
                        value = height,
                        onValueChange = { viewModel.setupHeight.value = it },
                        label = { Text("Height (cm)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("profile_height_input"),
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

                // Unit Preference Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Weight Unit:",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("kg", "lbs").forEach { unit ->
                            val isSelected = unitPref == unit
                            Surface(
                                color = if (isSelected) NeonLime else DarkSurfaceVariant,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.clickable { viewModel.setupUnitPreference.value = unit }
                            ) {
                                Text(
                                    text = unit.uppercase(),
                                    color = if (isSelected) DarkBackground else TextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = startingWeight,
                        onValueChange = { viewModel.setupStartingWeight.value = it },
                        label = { Text("Starting Wt ($unitPref)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("profile_starting_weight_input"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonLime,
                            unfocusedBorderColor = DarkCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    OutlinedTextField(
                        value = targetWeight,
                        onValueChange = { viewModel.setupTargetWeight.value = it },
                        label = { Text("Target Wt ($unitPref)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("profile_target_weight_input"),
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

                OutlinedTextField(
                    value = gymTiming,
                    onValueChange = { viewModel.setupGymTiming.value = it },
                    label = { Text("Gym Timing / Preferred Hours") },
                    placeholder = { Text("e.g. 06:00 AM - 07:30 AM") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("profile_gym_timing_input"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonLime,
                        unfocusedBorderColor = DarkCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = gymName,
                    onValueChange = { viewModel.setupGymName.value = it },
                    label = { Text("Gym / Club Name (Optional)") },
                    placeholder = { Text("e.g. Iron Forge Gym") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("profile_gym_name_input"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonLime,
                        unfocusedBorderColor = DarkCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "Primary Fitness Goal",
                    color = NeonLime,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    goals.forEach { goal ->
                        val isSelected = selectedGoal == goal
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { viewModel.setupGoal.value = goal },
                            color = if (isSelected) NeonLime.copy(alpha = 0.15f) else DarkSurfaceVariant,
                            border = BorderStroke(1.dp, if (isSelected) NeonLime else Color.Transparent)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isSelected) Icons.Default.Check else Icons.Default.FitnessCenter,
                                    contentDescription = null,
                                    tint = if (isSelected) NeonLime else TextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = goal,
                                    color = if (isSelected) NeonLime else TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        NeonButton(
            text = "COMPLETE SETUP & OPEN GYM",
            onClick = {
                if (userName.isBlank()) {
                    viewModel.setupUserName.value = "Athlete"
                }
                onNext()
            },
            icon = Icons.Default.Check,
            testTag = "profile_setup_next_button"
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun RoleChoiceScreen(
    viewModel: GymViewModel,
    onComplete: () -> Unit
) {
    val selectedRole by viewModel.setupRole.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // Step indicator
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            StepChip(number = "1", label = "Gym Name", isActive = true)
            Spacer(modifier = Modifier.width(16.dp))
            StepChip(number = "2", label = "Profile", isActive = true)
            Spacer(modifier = Modifier.width(16.dp))
            StepChip(number = "3", label = "Role", isActive = true)
        }

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "Choose Your Experience",
            color = TextPrimary,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Select your primary mode. You can toggle between Member and Admin anytime from Settings!",
            color = TextSecondary,
            fontSize = 13.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Member App Option
        RoleOptionCard(
            title = "👤 Member / Athlete App",
            subtitle = "Workout Tracker, Weekly Split (Mon-Sun), Weight Journey, Smart Rest Alarms, Built-in MP3 Player, Achievements.",
            badge = "RECOMMENDED FOR WORKOUTS",
            isSelected = selectedRole == "MEMBER",
            accentColor = NeonLime,
            icon = Icons.Default.Person,
            onSelect = { viewModel.setupRole.value = "MEMBER" },
            testTag = "role_choice_member"
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Admin App Option
        RoleOptionCard(
            title = "🛡️ Gym Owner / Admin Side",
            subtitle = "Manage registered gym members, membership expiry alerts, gym timings, rules & announcements, attendance check-ins.",
            badge = "GYM MANAGEMENT SIDE",
            isSelected = selectedRole == "ADMIN",
            accentColor = ElectricCyan,
            icon = Icons.Default.AdminPanelSettings,
            onSelect = { viewModel.setupRole.value = "ADMIN" },
            testTag = "role_choice_admin"
        )

        Spacer(modifier = Modifier.height(36.dp))

        NeonButton(
            text = "ENTER GYM APP",
            onClick = {
                viewModel.completeOnboarding()
                onComplete()
            },
            icon = Icons.Default.Check,
            testTag = "role_choice_complete_button"
        )
    }
}

@Composable
private fun RoleOptionCard(
    title: String,
    subtitle: String,
    badge: String,
    isSelected: Boolean,
    accentColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onSelect: () -> Unit,
    testTag: String
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable { onSelect() }
            .testTag(testTag),
        color = if (isSelected) DarkSurfaceVariant else DarkSurface,
        border = BorderStroke(
            2.dp,
            if (isSelected) accentColor else DarkCardBorder
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        color = TextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = badge,
                        color = accentColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = accentColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = subtitle,
                color = TextSecondary,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
private fun StepChip(number: String, label: String, isActive: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isActive) NeonLime.copy(alpha = 0.18f) else DarkSurfaceVariant)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(18.dp)
                .clip(CircleShape)
                .background(if (isActive) NeonLime else TextMuted),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number,
                color = Color(0xFF0A0E13),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            color = if (isActive) NeonLime else TextSecondary,
            fontSize = 11.sp,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
        )
    }
}
