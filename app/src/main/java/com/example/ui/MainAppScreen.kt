package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.audio.GymAudioPlayer
import com.example.ui.components.CelebrationOverlay
import com.example.ui.components.MiniPlayerDock
import com.example.ui.screens.AdminScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.ExerciseLibraryScreen
import com.example.ui.screens.MusicPlayerScreen
import com.example.ui.screens.ProgressScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SmartAlarmsScreen
import com.example.ui.screens.WorkoutHistoryScreen
import com.example.ui.screens.WorkoutScreen
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.NeonLime
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

data class NavItem(
    val tab: MainNavTab,
    val label: String,
    val icon: ImageVector
)

@Composable
fun MainAppScreen(
    viewModel: GymViewModel,
    audioPlayer: GymAudioPlayer
) {
    val currentTab by viewModel.currentTab.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val playerState by audioPlayer.playerState.collectAsState()

    var showMusicPlayerDialog by remember { mutableStateOf(false) }

    // Celebration state
    var showCelebration by remember { mutableStateOf(false) }
    var celebrationTitle by remember { mutableStateOf("WORKOUT CRUSHED!") }
    var celebrationSubtitle by remember { mutableStateOf("New Personal Best & Streak Milestone") }
    var celebrationV1Label by remember { mutableStateOf("Volume Lifted") }
    var celebrationV1Value by remember { mutableStateOf("1,250 kg") }
    var celebrationV2Label by remember { mutableStateOf("Current Streak") }
    var celebrationV2Value by remember { mutableStateOf("🔥 1 Day") }

    val isAdminMode = userProfile?.currentRole == "ADMIN"

    val memberNavItems = listOf(
        NavItem(MainNavTab.DASHBOARD, "Home", Icons.Default.Home),
        NavItem(MainNavTab.WORKOUT, "Workout", Icons.Default.FitnessCenter),
        NavItem(MainNavTab.PROGRESS, "Progress", Icons.Default.Timeline),
        NavItem(MainNavTab.EXERCISES, "Library", Icons.Default.MenuBook),
        NavItem(MainNavTab.ALARMS, "Alarms", Icons.Default.Alarm),
        NavItem(MainNavTab.SETTINGS, "Profile", Icons.Default.Settings)
    )

    val adminNavItems = listOf(
        NavItem(MainNavTab.ADMIN, "Console", Icons.Default.AdminPanelSettings),
        NavItem(MainNavTab.ALARMS, "Alarms", Icons.Default.Alarm),
        NavItem(MainNavTab.SETTINGS, "Settings", Icons.Default.Settings)
    )

    val navItems = if (isAdminMode) adminNavItems else memberNavItems

    Scaffold(
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkBackground)
            ) {
                // Mini Audio Player Bar (only visible when a track is chosen)
                if (playerState.currentTrack != null) {
                    MiniPlayerDock(
                        playerState = playerState,
                        onTogglePlay = { audioPlayer.togglePlayPause() },
                        onNext = { audioPlayer.playNext() },
                        onOpenFullPlayer = { showMusicPlayerDialog = true }
                    )
                }

                // Bottom Navigation Bar
                NavigationBar(
                    containerColor = DarkSurface,
                    contentColor = TextPrimary,
                    tonalElevation = 8.dp,
                    modifier = Modifier.testTag("bottom_nav_bar")
                ) {
                    navItems.forEach { item ->
                        val isSelected = currentTab == item.tab
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { viewModel.selectTab(item.tab) },
                            icon = {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.label,
                                    tint = if (isSelected) NeonLime else TextMuted
                                )
                            },
                            label = {
                                Text(
                                    text = item.label,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) NeonLime else TextSecondary
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = NeonLime.copy(alpha = 0.15f),
                                selectedIconColor = NeonLime,
                                unselectedIconColor = TextMuted
                            ),
                            modifier = Modifier.testTag("nav_tab_${item.tab.name.lowercase()}")
                        )
                    }
                }
            }
        },
        containerColor = DarkBackground
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                MainNavTab.DASHBOARD -> DashboardScreen(
                    viewModel = viewModel,
                    onOpenMusicPlayer = { showMusicPlayerDialog = true }
                )
                MainNavTab.WORKOUT -> WorkoutScreen(
                    viewModel = viewModel,
                    onShowCelebration = { volume, streak ->
                        celebrationTitle = "WORKOUT CRUSHED!"
                        celebrationSubtitle = "Workout completed & logged to your history!"
                        celebrationV1Label = "Volume Lifted"
                        celebrationV1Value = "${volume.toInt()} kg"
                        celebrationV2Label = "Current Streak"
                        celebrationV2Value = "🔥 $streak Days"
                        showCelebration = true
                    }
                )
                MainNavTab.PROGRESS -> ProgressScreen(
                    viewModel = viewModel,
                    onTriggerCelebration = { title, sub, v1, v2 ->
                        celebrationTitle = title
                        celebrationSubtitle = sub
                        celebrationV1Label = "Achievement"
                        celebrationV1Value = v1
                        celebrationV2Label = "Milestone"
                        celebrationV2Value = v2
                        showCelebration = true
                    }
                )
                MainNavTab.EXERCISES -> ExerciseLibraryScreen(viewModel = viewModel)
                MainNavTab.ALARMS -> SmartAlarmsScreen(viewModel = viewModel)
                MainNavTab.HISTORY -> WorkoutHistoryScreen(viewModel = viewModel)
                MainNavTab.ADMIN -> AdminScreen(viewModel = viewModel)
                MainNavTab.SETTINGS -> SettingsScreen(viewModel = viewModel)
            }

            // Confetti & Victory Overlay Animation
            CelebrationOverlay(
                visible = showCelebration,
                title = celebrationTitle,
                subtitle = celebrationSubtitle,
                metric1Label = celebrationV1Label,
                metric1Value = celebrationV1Value,
                metric2Label = celebrationV2Label,
                metric2Value = celebrationV2Value,
                onDismiss = { showCelebration = false }
            )
        }
    }

    // Full Built-in MP3 Music Player Dialog
    if (showMusicPlayerDialog) {
        Dialog(
            onDismissRequest = { showMusicPlayerDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            MusicPlayerScreen(
                audioPlayer = audioPlayer,
                onClose = { showMusicPlayerDialog = false }
            )
        }
    }
}
