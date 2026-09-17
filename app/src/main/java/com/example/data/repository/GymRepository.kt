package com.example.data.repository

import com.example.data.local.GymDao
import com.example.data.model.AchievementEntity
import com.example.data.model.ExerciseEntity
import com.example.data.model.GymConfigEntity
import com.example.data.model.GymMemberEntity
import com.example.data.model.SmartAlarmEntity
import com.example.data.model.UserProfileEntity
import com.example.data.model.WeightLogEntity
import com.example.data.model.WorkoutLogEntity
import com.example.data.model.WorkoutPlanEntity
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class GymRepository(private val dao: GymDao) {

    val gymConfig: Flow<GymConfigEntity?> = dao.getGymConfig()
    val userProfile: Flow<UserProfileEntity?> = dao.getUserProfile()
    val workoutPlans: Flow<List<WorkoutPlanEntity>> = dao.getAllWorkoutPlans()
    val allExercises: Flow<List<ExerciseEntity>> = dao.getAllExercises()
    val weightLogs: Flow<List<WeightLogEntity>> = dao.getAllWeightLogs()
    val workoutLogs: Flow<List<WorkoutLogEntity>> = dao.getAllWorkoutLogs()
    val smartAlarms: Flow<List<SmartAlarmEntity>> = dao.getAllAlarms()
    val achievements: Flow<List<AchievementEntity>> = dao.getAllAchievements()
    val gymMembers: Flow<List<GymMemberEntity>> = dao.getAllMembers()

    fun getExercisesForDay(day: String): Flow<List<ExerciseEntity>> {
        return dao.getExercisesForDay(day)
    }

    suspend fun setupGymAndProfile(gymName: String, profile: UserProfileEntity) {
        val config = GymConfigEntity(
            id = 1,
            gymName = gymName.ifBlank { "Apex Fitness Club" },
            gymTagline = "Strength • Consistency • Progress",
            openHours = "05:30 AM - 10:30 PM",
            address = "Central Athletics Complex",
            monthlyFee = 45.0,
            announcement = "Gym open 7 days a week. Free locker access & trainer consultations.",
            isConfigured = true
        )
        dao.saveGymConfig(config)

        val updatedProfile = profile.copy(
            id = 1,
            startingWeightKg = profile.startingWeightKg,
            currentWeightKg = profile.startingWeightKg,
            currentStreak = 0,
            bestStreak = 0,
            totalWorkoutsCompleted = 0,
            isProfileConfigured = true
        )
        dao.saveUserProfile(updatedProfile)

        // Initial starting weight log if user provided starting weight
        if (profile.startingWeightKg > 0f) {
            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            dao.insertWeightLog(
                WeightLogEntity(
                    weightKg = profile.startingWeightKg,
                    dateString = dateFormat.format(Date()),
                    timestamp = System.currentTimeMillis(),
                    note = "Starting Weight Initial Entry"
                )
            )
        }

        // Initialize achievement definitions at zero progress (all locked)
        initializeAchievements()
    }

    private suspend fun initializeAchievements() {
        val achievementsList = listOf(
            AchievementEntity(key = "first_workout", title = "First Rep Down", description = "Complete your first recorded gym workout session", category = "WORKOUTS", currentProgress = 0, targetProgress = 1, isUnlocked = false),
            AchievementEntity(key = "streak_3", title = "Momentum Builder", description = "Maintain a 3-day workout consistency streak", category = "STREAK", currentProgress = 0, targetProgress = 3, isUnlocked = false),
            AchievementEntity(key = "streak_7", title = "Iron Discipline", description = "Crush a 7-day workout streak", category = "STREAK", currentProgress = 0, targetProgress = 7, isUnlocked = false),
            AchievementEntity(key = "workout_10", title = "Gym Regular", description = "Complete 10 total workout sessions", category = "WORKOUTS", currentProgress = 0, targetProgress = 10, isUnlocked = false),
            AchievementEntity(key = "volume_1000", title = "One Ton Lifter", description = "Lift over 1,000 kg total cumulative workout volume", category = "VOLUME", currentProgress = 0, targetProgress = 1000, isUnlocked = false),
            AchievementEntity(key = "weight_progress", title = "Target Locked", description = "Record 3 weight check-ins toward your fitness goal", category = "WEIGHT", currentProgress = 1, targetProgress = 3, isUnlocked = false)
        )
        dao.insertAchievements(achievementsList)
    }

    suspend fun saveWorkoutPlan(plan: WorkoutPlanEntity) = dao.saveWorkoutPlan(plan)
    suspend fun updateWorkoutPlan(plan: WorkoutPlanEntity) = dao.updateWorkoutPlan(plan)
    suspend fun deleteWorkoutPlan(plan: WorkoutPlanEntity) {
        dao.deleteWorkoutPlan(plan)
        dao.deleteExercisesForDay(plan.dayOfWeek)
    }
    suspend fun deleteWorkoutPlanByDay(day: String) {
        dao.deleteWorkoutPlanByDay(day)
        dao.deleteExercisesForDay(day)
    }

    suspend fun recordWorkoutSession(
        dayOfWeek: String,
        workoutTitle: String,
        durationMinutes: Int,
        exercises: List<ExerciseEntity>,
        notes: String,
        rating: Int
    ): WorkoutLogEntity {
        var totalVol = 0f
        var completedCount = 0
        val exerciseSummaryParts = mutableListOf<String>()

        for (ex in exercises) {
            val vol = ex.sets * ex.reps * ex.weightKg
            totalVol += vol
            if (ex.isCompleted) completedCount++
            val weightStr = if (ex.weightKg > 0) " @ ${ex.weightKg.toInt()}kg" else ""
            exerciseSummaryParts.add("${ex.name} (${ex.sets}x${ex.reps}$weightStr)")
        }
        if (completedCount == 0) completedCount = exercises.size

        val summary = exerciseSummaryParts.joinToString(", ")
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

        val log = WorkoutLogEntity(
            workoutTitle = workoutTitle.ifBlank { "Workout Session" },
            dayOfWeek = dayOfWeek,
            durationMinutes = durationMinutes,
            exercisesCompleted = completedCount,
            totalVolumeKg = totalVol,
            timestamp = System.currentTimeMillis(),
            dateFormatted = dateFormat.format(Date()),
            notes = notes,
            intensityRating = rating,
            exerciseSummary = summary
        )
        dao.insertWorkoutLog(log)

        // Update profile streak and workout count
        val profile = dao.getUserProfileDirect()
        if (profile != null) {
            val newTotal = profile.totalWorkoutsCompleted + 1
            val newStreak = profile.currentStreak + 1
            val bestStreak = maxOf(newStreak, profile.bestStreak)
            dao.saveUserProfile(
                profile.copy(
                    totalWorkoutsCompleted = newTotal,
                    currentStreak = newStreak,
                    bestStreak = bestStreak
                )
            )

            // Update achievement progress
            checkAndUnlockAchievements(newTotal, newStreak, totalVol.toInt())
        }

        // Reset day exercises completed state for future sessions
        dao.resetDayExerciseCompletion(dayOfWeek, false)

        return log
    }

    private suspend fun checkAndUnlockAchievements(totalWorkouts: Int, streak: Int, volume: Int) {
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        val dateStr = dateFormat.format(Date())

        if (totalWorkouts >= 1) {
            dao.updateAchievement(
                AchievementEntity(
                    id = 1,
                    key = "first_workout",
                    title = "First Rep Down",
                    description = "Complete your first recorded gym workout session",
                    category = "WORKOUTS",
                    currentProgress = 1,
                    targetProgress = 1,
                    isUnlocked = true,
                    unlockedDate = dateStr
                )
            )
        }
        if (streak >= 3) {
            dao.updateAchievement(
                AchievementEntity(
                    id = 2,
                    key = "streak_3",
                    title = "Momentum Builder",
                    description = "Maintain a 3-day workout consistency streak",
                    category = "STREAK",
                    currentProgress = minOf(streak, 3),
                    targetProgress = 3,
                    isUnlocked = true,
                    unlockedDate = dateStr
                )
            )
        }
        if (streak >= 7) {
            dao.updateAchievement(
                AchievementEntity(
                    id = 3,
                    key = "streak_7",
                    title = "Iron Discipline",
                    description = "Crush a 7-day workout streak",
                    category = "STREAK",
                    currentProgress = minOf(streak, 7),
                    targetProgress = 7,
                    isUnlocked = true,
                    unlockedDate = dateStr
                )
            )
        }
        if (totalWorkouts >= 10) {
            dao.updateAchievement(
                AchievementEntity(
                    id = 4,
                    key = "workout_10",
                    title = "Gym Regular",
                    description = "Complete 10 total workout sessions",
                    category = "WORKOUTS",
                    currentProgress = minOf(totalWorkouts, 10),
                    targetProgress = 10,
                    isUnlocked = true,
                    unlockedDate = dateStr
                )
            )
        }
    }

    suspend fun logNewWeight(weightKg: Float, note: String) {
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        dao.insertWeightLog(
            WeightLogEntity(
                weightKg = weightKg,
                dateString = dateFormat.format(Date()),
                timestamp = System.currentTimeMillis(),
                note = note
            )
        )
        val profile = dao.getUserProfileDirect()
        if (profile != null) {
            dao.saveUserProfile(profile.copy(currentWeightKg = weightKg))
        }
    }

    suspend fun updateExercise(exercise: ExerciseEntity) = dao.updateExercise(exercise)
    suspend fun insertExercise(exercise: ExerciseEntity): Long = dao.insertExercise(exercise)
    suspend fun deleteExercise(exercise: ExerciseEntity) = dao.deleteExercise(exercise)

    suspend fun reorderExercise(exercise: ExerciseEntity, delta: Int) {
        val updated = exercise.copy(orderIndex = (exercise.orderIndex + delta).coerceAtLeast(0))
        dao.updateExercise(updated)
    }

    suspend fun moveExerciseToDay(exercise: ExerciseEntity, newDay: String) {
        val updated = exercise.copy(planDayOfWeek = newDay)
        dao.updateExercise(updated)
    }

    suspend fun toggleAlarm(alarm: SmartAlarmEntity) {
        dao.updateAlarm(alarm.copy(isEnabled = !alarm.isEnabled))
    }
    suspend fun insertAlarm(alarm: SmartAlarmEntity) = dao.insertAlarm(alarm)
    suspend fun updateAlarm(alarm: SmartAlarmEntity) = dao.updateAlarm(alarm)
    suspend fun deleteAlarm(alarm: SmartAlarmEntity) = dao.deleteAlarm(alarm)

    suspend fun deleteWorkoutLog(log: WorkoutLogEntity) = dao.deleteWorkoutLog(log)
    suspend fun deleteWeightLog(log: WeightLogEntity) = dao.deleteWeightLog(log)

    suspend fun switchRole(newRole: String) {
        val profile = dao.getUserProfileDirect()
        if (profile != null) {
            dao.saveUserProfile(profile.copy(currentRole = newRole))
        }
    }

    suspend fun updateProfile(profile: UserProfileEntity) = dao.saveUserProfile(profile)
    suspend fun updateGymConfig(config: GymConfigEntity) = dao.saveGymConfig(config)

    suspend fun insertGymMember(member: GymMemberEntity) = dao.insertMember(member)
    suspend fun updateGymMember(member: GymMemberEntity) = dao.updateMember(member)
    suspend fun deleteGymMember(member: GymMemberEntity) = dao.deleteMember(member)

    suspend fun resetProgress() {
        dao.clearWeightLogs()
        dao.clearWorkoutLogs()
        val profile = dao.getUserProfileDirect()
        if (profile != null) {
            dao.saveUserProfile(
                profile.copy(
                    currentStreak = 0,
                    bestStreak = 0,
                    totalWorkoutsCompleted = 0,
                    currentWeightKg = profile.startingWeightKg
                )
            )
        }
    }

    suspend fun clearAllAppData() {
        dao.clearWeightLogs()
        dao.clearWorkoutLogs()
        dao.clearWorkoutPlans()
        dao.clearExercises()
        dao.clearSmartAlarms()
        dao.clearAchievements()
        dao.deleteAllMembers()
        dao.clearUserProfile()
        dao.clearGymConfig()
    }
}
