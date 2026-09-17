package com.example.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.alarm.WorkoutAlarmHelper
import com.example.audio.AudioTrackItem
import com.example.audio.GymAudioPlayer
import com.example.audio.PlayerState
import com.example.data.local.AppDatabase
import com.example.data.model.AchievementEntity
import com.example.data.model.ExerciseCatalog
import com.example.data.model.ExerciseEntity
import com.example.data.model.GymConfigEntity
import com.example.data.model.GymMemberEntity
import com.example.data.model.SmartAlarmEntity
import com.example.data.model.UserProfileEntity
import com.example.data.model.WeightLogEntity
import com.example.data.model.WorkoutLogEntity
import com.example.data.model.WorkoutPlanEntity
import com.example.data.repository.GymRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class AppStep {
    SPLASH,
    PROFILE_SETUP,
    MAIN_APP
}

enum class MainNavTab {
    DASHBOARD,
    WORKOUT,
    PROGRESS,
    EXERCISES,
    ALARMS,
    HISTORY,
    ADMIN,
    SETTINGS
}

data class ActiveWorkoutSession(
    val isActive: Boolean = false,
    val dayOfWeek: String = "Monday",
    val workoutTitle: String = "Workout Session",
    val elapsedSeconds: Int = 0,
    val currentExerciseIndex: Int = 0,
    val completedExercises: Set<Long> = emptySet(),
    val isRestTimerRunning: Boolean = false,
    val restTimeRemaining: Int = 0,
    val totalRestTime: Int = 60
)

class GymViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: GymRepository
    val audioPlayer: GymAudioPlayer = GymAudioPlayer(application)
    val alarmHelper: WorkoutAlarmHelper = WorkoutAlarmHelper(application)

    init {
        val db = AppDatabase.getDatabase(application)
        repository = GymRepository(db.gymDao())
    }

    // App Navigation State
    private val _appStep = MutableStateFlow(AppStep.SPLASH)
    val appStep: StateFlow<AppStep> = _appStep.asStateFlow()

    private val _selectedTab = MutableStateFlow(MainNavTab.DASHBOARD)
    val selectedTab: StateFlow<MainNavTab> = _selectedTab.asStateFlow()
    val currentTab: StateFlow<MainNavTab> = _selectedTab.asStateFlow()

    fun setAppStep(step: AppStep) {
        _appStep.value = step
    }

    // Temporary onboarding states
    val setupGymName = MutableStateFlow("")
    val setupUserName = MutableStateFlow("")
    val setupAge = MutableStateFlow("24")
    val setupHeight = MutableStateFlow("178")
    val setupStartingWeight = MutableStateFlow("75")
    val setupTargetWeight = MutableStateFlow("70")
    val setupGoal = MutableStateFlow("Build Muscle & Strength")
    val setupGymTiming = MutableStateFlow("06:00 AM - 07:30 AM")
    val setupUnitPreference = MutableStateFlow("kg")
    val setupPhotoUri = MutableStateFlow<String?>(null)
    val setupRole = MutableStateFlow("MEMBER")

    // Data flows from Repository
    val gymConfig: StateFlow<GymConfigEntity?> = repository.gymConfig
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val userProfile: StateFlow<UserProfileEntity?> = repository.userProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val workoutPlans: StateFlow<List<WorkoutPlanEntity>> = repository.workoutPlans
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allExercises: StateFlow<List<ExerciseEntity>> = repository.allExercises
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val weightLogs: StateFlow<List<WeightLogEntity>> = repository.weightLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val workoutLogs: StateFlow<List<WorkoutLogEntity>> = repository.workoutLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val smartAlarms: StateFlow<List<SmartAlarmEntity>> = repository.smartAlarms
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val achievements: StateFlow<List<AchievementEntity>> = repository.achievements
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val gymMembers: StateFlow<List<GymMemberEntity>> = repository.gymMembers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val playerState: StateFlow<PlayerState> = audioPlayer.playerState

    // Active Day in Workout Screen (defaults to current day of week)
    private val _selectedDay = MutableStateFlow(getCurrentDayOfWeek())
    val selectedDay: StateFlow<String> = _selectedDay.asStateFlow()

    // Active Workout Session State
    private val _activeSession = MutableStateFlow(ActiveWorkoutSession())
    val activeSession: StateFlow<ActiveWorkoutSession> = _activeSession.asStateFlow()

    private var workoutTimerJob: Job? = null
    private var restTimerJob: Job? = null

    init {
        // Evaluate splash screen transition based on DB userProfile
        viewModelScope.launch {
            delay(1200)
            repository.userProfile.collect { profile ->
                if (_appStep.value == AppStep.SPLASH) {
                    if (profile != null && profile.isProfileConfigured) {
                        _appStep.value = AppStep.MAIN_APP
                    } else {
                        _appStep.value = AppStep.PROFILE_SETUP
                    }
                }
            }
        }
    }

    private fun getCurrentDayOfWeek(): String {
        return when (Calendar.getInstance().get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> "Monday"
            Calendar.TUESDAY -> "Tuesday"
            Calendar.WEDNESDAY -> "Wednesday"
            Calendar.THURSDAY -> "Thursday"
            Calendar.FRIDAY -> "Friday"
            Calendar.SATURDAY -> "Saturday"
            Calendar.SUNDAY -> "Sunday"
            else -> "Monday"
        }
    }

    // Step Transitions & Tab Navigation
    fun goToStep(step: AppStep) {
        _appStep.value = step
    }

    fun selectTab(tab: MainNavTab) {
        _selectedTab.value = tab
    }

    fun setSelectedDay(day: String) {
        _selectedDay.value = day
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            val startingW = setupStartingWeight.value.toFloatOrNull() ?: 75f
            val targetW = setupTargetWeight.value.toFloatOrNull() ?: 70f
            val ageVal = setupAge.value.toIntOrNull() ?: 24
            val heightVal = setupHeight.value.toFloatOrNull() ?: 175f

            val profile = UserProfileEntity(
                name = setupUserName.value.ifBlank { "Athlete" },
                age = ageVal,
                heightCm = heightVal,
                startingWeightKg = startingW,
                currentWeightKg = startingW,
                targetWeightKg = targetW,
                profilePictureUri = setupPhotoUri.value,
                fitnessGoal = setupGoal.value,
                gymTiming = setupGymTiming.value.ifBlank { "06:00 AM - 07:30 AM" },
                preferredWeightUnit = setupUnitPreference.value,
                currentRole = setupRole.value,
                currentStreak = 0,
                bestStreak = 0,
                totalWorkoutsCompleted = 0,
                isProfileConfigured = true
            )

            val finalGymName = setupGymName.value.ifBlank { "Titan Fitness Club" }
            repository.setupGymAndProfile(finalGymName, profile)
            _appStep.value = AppStep.MAIN_APP
        }
    }

    // Workout Plan Configuration (User-Controlled Weekly Schedule)
    fun saveOrUpdateWorkoutPlan(
        day: String,
        workoutName: String,
        muscleGroup: String,
        isRestDay: Boolean,
        targetCalories: Int = 450,
        estimatedMinutes: Int = 60,
        notes: String = ""
    ) {
        viewModelScope.launch {
            val plan = WorkoutPlanEntity(
                dayOfWeek = day,
                workoutName = workoutName,
                muscleGroup = muscleGroup,
                isRestDay = isRestDay,
                targetCalories = targetCalories,
                estimatedMinutes = estimatedMinutes,
                notes = notes
            )
            repository.saveWorkoutPlan(plan)
        }
    }

    fun deleteWorkoutPlan(day: String) {
        viewModelScope.launch {
            repository.deleteWorkoutPlanByDay(day)
        }
    }

    // Active Workout Controls
    fun startWorkoutSession(dayOfWeek: String, workoutTitle: String) {
        workoutTimerJob?.cancel()
        restTimerJob?.cancel()

        _activeSession.value = ActiveWorkoutSession(
            isActive = true,
            dayOfWeek = dayOfWeek,
            workoutTitle = workoutTitle.ifBlank { "$dayOfWeek Workout" },
            elapsedSeconds = 0,
            currentExerciseIndex = 0,
            completedExercises = emptySet()
        )

        workoutTimerJob = viewModelScope.launch {
            while (_activeSession.value.isActive) {
                delay(1000)
                _activeSession.value = _activeSession.value.copy(
                    elapsedSeconds = _activeSession.value.elapsedSeconds + 1
                )
            }
        }
    }

    fun toggleExerciseDoneInSession(exerciseId: Long, restSeconds: Int) {
        val current = _activeSession.value
        val newCompleted = current.completedExercises.toMutableSet()
        val isNowCompleted = if (newCompleted.contains(exerciseId)) {
            newCompleted.remove(exerciseId)
            false
        } else {
            newCompleted.add(exerciseId)
            true
        }

        _activeSession.value = current.copy(completedExercises = newCompleted)

        // If newly marked completed, start the rest timer!
        if (isNowCompleted && restSeconds > 0) {
            startRestTimer(restSeconds)
        }
    }

    fun startRestTimer(seconds: Int) {
        restTimerJob?.cancel()
        _activeSession.value = _activeSession.value.copy(
            isRestTimerRunning = true,
            restTimeRemaining = seconds,
            totalRestTime = seconds
        )

        restTimerJob = viewModelScope.launch {
            while (_activeSession.value.restTimeRemaining > 0) {
                delay(1000)
                val remaining = _activeSession.value.restTimeRemaining - 1
                _activeSession.value = _activeSession.value.copy(restTimeRemaining = remaining)
            }
            _activeSession.value = _activeSession.value.copy(isRestTimerRunning = false)
            alarmHelper.playRestCompleteChimeAndVibrate()
        }
    }

    fun stopRestTimer() {
        restTimerJob?.cancel()
        _activeSession.value = _activeSession.value.copy(
            isRestTimerRunning = false,
            restTimeRemaining = 0
        )
    }

    fun finishWorkoutSession(dayExercises: List<ExerciseEntity>, notes: String = "", rating: Int = 5) {
        viewModelScope.launch {
            val durationMin = maxOf(1, _activeSession.value.elapsedSeconds / 60)
            repository.recordWorkoutSession(
                dayOfWeek = _activeSession.value.dayOfWeek,
                workoutTitle = _activeSession.value.workoutTitle,
                durationMinutes = durationMin,
                exercises = dayExercises,
                notes = notes.ifBlank { "Completed ${_activeSession.value.workoutTitle} session!" },
                rating = rating
            )

            workoutTimerJob?.cancel()
            restTimerJob?.cancel()
            _activeSession.value = ActiveWorkoutSession(isActive = false)
            _selectedTab.value = MainNavTab.PROGRESS
        }
    }

    fun cancelWorkoutSession() {
        workoutTimerJob?.cancel()
        restTimerJob?.cancel()
        _activeSession.value = ActiveWorkoutSession(isActive = false)
    }

    // Exercise Management
    fun updateExerciseDetails(exercise: ExerciseEntity) {
        viewModelScope.launch {
            repository.updateExercise(exercise)
        }
    }

    fun addCustomExercise(
        dayOfWeek: String,
        name: String,
        muscle: String,
        equipment: String,
        sets: Int,
        reps: Int,
        weight: Float,
        restTimeSec: Int,
        instructions: String
    ) {
        viewModelScope.launch {
            val currentDayExercises = allExercises.value.filter { it.planDayOfWeek == dayOfWeek }
            val nextIndex = currentDayExercises.size
            val ex = ExerciseEntity(
                planDayOfWeek = dayOfWeek,
                name = name,
                category = "Custom",
                muscleGroup = muscle,
                equipment = equipment,
                sets = sets,
                reps = reps,
                weightKg = weight,
                restTimeSec = restTimeSec,
                instructions = instructions,
                orderIndex = nextIndex
            )
            repository.insertExercise(ex)
        }
    }

    fun addExerciseFromLibrary(
        dayOfWeek: String,
        libraryItem: ExerciseEntity,
        sets: Int = 3,
        reps: Int = 10,
        weight: Float = 20f,
        restTimeSec: Int = 60
    ) {
        viewModelScope.launch {
            val currentDayExercises = allExercises.value.filter { it.planDayOfWeek == dayOfWeek }
            val nextIndex = currentDayExercises.size
            val ex = ExerciseEntity(
                planDayOfWeek = dayOfWeek,
                name = libraryItem.name,
                category = libraryItem.category,
                muscleGroup = libraryItem.muscleGroup,
                equipment = libraryItem.equipment,
                sets = sets,
                reps = reps,
                weightKg = weight,
                restTimeSec = restTimeSec,
                instructions = libraryItem.instructions,
                orderIndex = nextIndex
            )
            repository.insertExercise(ex)
        }
    }

    fun deleteExercise(exercise: ExerciseEntity) {
        viewModelScope.launch {
            repository.deleteExercise(exercise)
        }
    }

    fun reorderExercise(exercise: ExerciseEntity, delta: Int) {
        viewModelScope.launch {
            repository.reorderExercise(exercise, delta)
        }
    }

    fun moveExerciseToDay(exercise: ExerciseEntity, newDay: String) {
        viewModelScope.launch {
            repository.moveExerciseToDay(exercise, newDay)
        }
    }

    // Weight Tracking
    fun logWeight(weightKg: Float, note: String) {
        viewModelScope.launch {
            repository.logNewWeight(weightKg, note)
        }
    }

    fun deleteWeightLog(log: WeightLogEntity) {
        viewModelScope.launch {
            repository.deleteWeightLog(log)
        }
    }

    // Workout History Logs
    fun deleteWorkoutLog(log: WorkoutLogEntity) {
        viewModelScope.launch {
            repository.deleteWorkoutLog(log)
        }
    }

    // Smart Alarms
    fun toggleAlarm(alarm: SmartAlarmEntity) {
        viewModelScope.launch {
            repository.toggleAlarm(alarm)
        }
    }

    fun addSmartAlarm(type: String, title: String, hour: Int, minute: Int, days: String, note: String) {
        viewModelScope.launch {
            val isAm = hour < 12
            val hour12 = if (hour % 12 == 0) 12 else hour % 12
            val timeFormatted = String.format(Locale.getDefault(), "%02d:%02d %s", hour12, minute, if (isAm) "AM" else "PM")

            val alarm = SmartAlarmEntity(
                type = type,
                title = title,
                timeFormatted = timeFormatted,
                hour = hour,
                minute = minute,
                repeatDays = days,
                isEnabled = true,
                note = note
            )
            repository.insertAlarm(alarm)
        }
    }

    fun updateSmartAlarm(alarm: SmartAlarmEntity) {
        viewModelScope.launch {
            repository.updateAlarm(alarm)
        }
    }

    fun deleteAlarm(alarm: SmartAlarmEntity) {
        viewModelScope.launch {
            repository.deleteAlarm(alarm)
        }
    }

    fun triggerAlarmTest(alarm: SmartAlarmEntity) {
        alarmHelper.sendAlarmNotification(alarm.title, "${alarm.timeFormatted} • ${alarm.note.ifBlank { "Time for your workout!" }}")
    }

    // Audio Player controls
    fun toggleMusic() = audioPlayer.togglePlayPause()
    fun nextTrack() = audioPlayer.playNext()
    fun prevTrack() = audioPlayer.playPrevious()
    fun seekMusic(posMs: Long) = audioPlayer.seekTo(posMs)
    fun toggleMusicLoop() = audioPlayer.toggleLoop()
    fun removeAudioTrack(trackId: String) = audioPlayer.removeTrack(trackId)

    fun addAudioTracksFromUris(uris: List<Uri>) {
        val tracks = uris.mapIndexed { index, uri ->
            val fileName = uri.lastPathSegment?.substringAfterLast('/') ?: "Gym Track ${index + 1}"
            AudioTrackItem(
                id = "doc_${System.currentTimeMillis()}_$index",
                title = fileName.replace(".mp3", "").replace(".m4a", "").replace(".wav", ""),
                artist = "My Workout Playlist",
                durationMs = 210_000,
                uri = uri
            )
        }
        audioPlayer.addTracks(tracks)
    }

    // Role Switching (Member vs Admin)
    fun switchRole(newRole: String) {
        viewModelScope.launch {
            repository.switchRole(newRole)
        }
    }

    // Admin Operations
    fun addGymMember(name: String, email: String, phone: String, plan: String) {
        viewModelScope.launch {
            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            val calendar = Calendar.getInstance()
            val joinDate = dateFormat.format(calendar.time)
            calendar.add(Calendar.MONTH, 6)
            val expiryDate = dateFormat.format(calendar.time)

            val member = GymMemberEntity(
                fullName = name,
                email = email,
                phone = phone,
                membershipPlan = plan,
                joinDate = joinDate,
                expiryDate = expiryDate,
                status = "Active",
                attendanceCount = 1,
                lastCheckIn = "Today, Just Joined"
            )
            repository.insertGymMember(member)
        }
    }

    fun updateGymMemberStatus(member: GymMemberEntity, newStatus: String) {
        viewModelScope.launch {
            repository.updateGymMember(member.copy(status = newStatus))
        }
    }

    fun deleteGymMember(member: GymMemberEntity) {
        viewModelScope.launch {
            repository.deleteGymMember(member)
        }
    }

    fun updateGymSettings(name: String, tagline: String, hours: String, fee: Double, announcement: String) {
        viewModelScope.launch {
            val current = gymConfig.value ?: GymConfigEntity()
            repository.updateGymConfig(
                current.copy(
                    gymName = name,
                    gymTagline = tagline,
                    openHours = hours,
                    monthlyFee = fee,
                    announcement = announcement
                )
            )
        }
    }

    fun updateFullProfile(
        name: String,
        age: Int,
        height: Float,
        startingWeight: Float,
        currentWeight: Float,
        targetWeight: Float,
        goal: String,
        gymTiming: String,
        unit: String,
        photoUri: String?
    ) {
        viewModelScope.launch {
            val current = userProfile.value ?: UserProfileEntity()
            repository.updateProfile(
                current.copy(
                    name = name,
                    age = age,
                    heightCm = height,
                    startingWeightKg = startingWeight,
                    currentWeightKg = currentWeight,
                    targetWeightKg = targetWeight,
                    fitnessGoal = goal,
                    gymTiming = gymTiming,
                    preferredWeightUnit = unit,
                    profilePictureUri = photoUri ?: current.profilePictureUri,
                    isProfileConfigured = true
                )
            )
        }
    }

    fun resetProgress() {
        viewModelScope.launch {
            repository.resetProgress()
        }
    }

    fun clearAllAppData() {
        viewModelScope.launch {
            repository.clearAllAppData()
            _appStep.value = AppStep.PROFILE_SETUP
        }
    }

    override fun onCleared() {
        super.onCleared()
        workoutTimerJob?.cancel()
        restTimerJob?.cancel()
        audioPlayer.release()
    }
}
