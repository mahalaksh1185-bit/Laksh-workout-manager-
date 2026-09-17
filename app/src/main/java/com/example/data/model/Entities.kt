package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "gym_config")
data class GymConfigEntity(
    @PrimaryKey val id: Int = 1,
    val gymName: String = "",
    val gymTagline: String = "Train Hard. Stay Consistent.",
    val openHours: String = "05:00 AM - 11:00 PM",
    val address: String = "Downtown Fitness Center",
    val monthlyFee: Double = 49.99,
    val announcement: String = "Welcome to the gym! Clean towels and water dispensers available.",
    val isConfigured: Boolean = false
)

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Int = 1,
    val name: String = "",
    val age: Int = 24,
    val heightCm: Float = 175f,
    val startingWeightKg: Float = 75f,
    val currentWeightKg: Float = 75f,
    val targetWeightKg: Float = 70f,
    val profilePictureUri: String? = null,
    val fitnessGoal: String = "Build Muscle & Strength",
    val gymTiming: String = "06:00 AM - 07:30 AM",
    val preferredWeightUnit: String = "kg",
    val currentRole: String = "MEMBER", // "MEMBER" or "ADMIN"
    val membershipPlan: String = "Gold Annual Membership",
    val membershipExpiryDays: Int = 180, // days remaining
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val totalWorkoutsCompleted: Int = 0,
    val isProfileConfigured: Boolean = false
)

@Entity(tableName = "workout_plans")
data class WorkoutPlanEntity(
    @PrimaryKey val dayOfWeek: String, // Monday, Tuesday, etc.
    val workoutName: String = "", // e.g. "Chest Blaster", "Leg Day", etc.
    val muscleGroup: String = "", // Chest, Back, Legs, etc.
    val isRestDay: Boolean = false,
    val targetCalories: Int = 450,
    val estimatedMinutes: Int = 60,
    val notes: String = ""
)

@Entity(tableName = "exercises")
data class ExerciseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val planDayOfWeek: String, // "Monday", "Tuesday", etc.
    val name: String,
    val category: String, // "Strength", "Hypertrophy", "Cardio"
    val muscleGroup: String, // "Chest", "Triceps", "Back", etc.
    val equipment: String, // "Barbell", "Dumbbell", "Cable", "Machine", "Bodyweight"
    val sets: Int = 3,
    val reps: Int = 10,
    val weightKg: Float = 20f,
    val restTimeSec: Int = 60,
    val instructions: String = "",
    val isCompleted: Boolean = false,
    val orderIndex: Int = 0
)

@Entity(tableName = "weight_logs")
data class WeightLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val weightKg: Float,
    val dateString: String,
    val timestamp: Long = System.currentTimeMillis(),
    val note: String = ""
)

@Entity(tableName = "workout_logs")
data class WorkoutLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val workoutTitle: String,
    val dayOfWeek: String,
    val durationMinutes: Int,
    val exercisesCompleted: Int,
    val totalVolumeKg: Float,
    val timestamp: Long = System.currentTimeMillis(),
    val dateFormatted: String,
    val notes: String = "",
    val intensityRating: Int = 5, // 1 to 5
    val exerciseSummary: String = "" // e.g. "Bench Press: 4x8 @ 60kg, Incline DB: 3x10 @ 22kg"
)

@Entity(tableName = "smart_alarms")
data class SmartAlarmEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String, // "GYM_TIMING", "WORKOUT", "REST_TIME", "MEMBERSHIP_EXPIRY", "WATER_REMINDER"
    val title: String,
    val timeFormatted: String,
    val hour: Int,
    val minute: Int,
    val repeatDays: String, // "Mon, Tue, Wed, Thu, Fri"
    val isEnabled: Boolean = true,
    val note: String = ""
)

@Entity(tableName = "achievements")
data class AchievementEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val key: String,
    val title: String,
    val description: String,
    val category: String, // "STREAK", "WORKOUTS", "WEIGHT", "VOLUME"
    val currentProgress: Int = 0,
    val targetProgress: Int = 1,
    val isUnlocked: Boolean = false,
    val unlockedDate: String? = null
)

@Entity(tableName = "gym_members")
data class GymMemberEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fullName: String,
    val email: String,
    val phone: String,
    val membershipPlan: String,
    val joinDate: String,
    val expiryDate: String,
    val status: String = "Active", // "Active", "Expiring Soon", "Expired"
    val attendanceCount: Int = 0,
    val lastCheckIn: String = "Never"
)
