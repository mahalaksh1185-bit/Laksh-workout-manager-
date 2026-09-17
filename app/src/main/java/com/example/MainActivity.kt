package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.example.alarm.WorkoutAlarmHelper
import com.example.audio.GymAudioPlayer
import com.example.data.local.AppDatabase
import com.example.data.repository.GymRepository
import com.example.ui.AppStep
import com.example.ui.GymViewModel
import com.example.ui.MainAppScreen
import com.example.ui.screens.GymSetupScreen
import com.example.ui.screens.ProfileSetupScreen
import com.example.ui.screens.RoleChoiceScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

  private lateinit var audioPlayer: GymAudioPlayer

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    val viewModel = GymViewModel(application)
    audioPlayer = viewModel.audioPlayer

    setContent {
      MyApplicationTheme {
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = DarkBackground
        ) {
          GymWorkoutApp(
            viewModel = viewModel,
            audioPlayer = audioPlayer
          )
        }
      }
    }
  }

  override fun onDestroy() {
    super.onDestroy()
    if (::audioPlayer.isInitialized) {
      audioPlayer.release()
    }
  }
}

@Composable
fun GymWorkoutApp(
  viewModel: GymViewModel,
  audioPlayer: GymAudioPlayer
) {
  val appStep by viewModel.appStep.collectAsState()

  Crossfade(
    targetState = appStep,
    animationSpec = tween(400),
    label = "app_step_crossfade"
  ) { step ->
    when (step) {
      AppStep.SPLASH -> SplashScreen(
        onGetStarted = { viewModel.setAppStep(AppStep.PROFILE_SETUP) }
      )
      AppStep.PROFILE_SETUP -> ProfileSetupScreen(
        viewModel = viewModel,
        onNext = { viewModel.completeOnboarding() }
      )
      AppStep.MAIN_APP -> MainAppScreen(
        viewModel = viewModel,
        audioPlayer = audioPlayer
      )
    }
  }
}

