package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
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

@Dao
interface GymDao {

    // --- Gym Config ---
    @Query("SELECT * FROM gym_config WHERE id = 1 LIMIT 1")
    fun getGymConfig(): Flow<GymConfigEntity?>

    @Query("SELECT * FROM gym_config WHERE id = 1 LIMIT 1")
    suspend fun getGymConfigDirect(): GymConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveGymConfig(config: GymConfigEntity)

    // --- User Profile ---
    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    fun getUserProfile(): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    suspend fun getUserProfileDirect(): UserProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUserProfile(profile: UserProfileEntity)

    // --- Weekly Workout Plans ---
    @Query("SELECT * FROM workout_plans")
    fun getAllWorkoutPlans(): Flow<List<WorkoutPlanEntity>>

    @Query("SELECT * FROM workout_plans WHERE dayOfWeek = :day LIMIT 1")
    fun getWorkoutPlanForDay(day: String): Flow<WorkoutPlanEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkoutPlans(plans: List<WorkoutPlanEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveWorkoutPlan(plan: WorkoutPlanEntity)

    @Update
    suspend fun updateWorkoutPlan(plan: WorkoutPlanEntity)

    @Delete
    suspend fun deleteWorkoutPlan(plan: WorkoutPlanEntity)

    @Query("DELETE FROM workout_plans WHERE dayOfWeek = :day")
    suspend fun deleteWorkoutPlanByDay(day: String)

    // --- Exercises ---
    @Query("SELECT * FROM exercises WHERE planDayOfWeek = :day ORDER BY orderIndex ASC, id ASC")
    fun getExercisesForDay(day: String): Flow<List<ExerciseEntity>>

    @Query("SELECT * FROM exercises ORDER BY muscleGroup ASC, name ASC")
    fun getAllExercises(): Flow<List<ExerciseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercise(exercise: ExerciseEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercises(exercises: List<ExerciseEntity>)

    @Update
    suspend fun updateExercise(exercise: ExerciseEntity)

    @Delete
    suspend fun deleteExercise(exercise: ExerciseEntity)

    @Query("DELETE FROM exercises WHERE planDayOfWeek = :day")
    suspend fun deleteExercisesForDay(day: String)

    @Query("UPDATE exercises SET isCompleted = :completed WHERE planDayOfWeek = :day")
    suspend fun resetDayExerciseCompletion(day: String, completed: Boolean = false)

    // --- Weight Logs ---
    @Query("SELECT * FROM weight_logs ORDER BY timestamp ASC")
    fun getAllWeightLogs(): Flow<List<WeightLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeightLog(log: WeightLogEntity)

    @Delete
    suspend fun deleteWeightLog(log: WeightLogEntity)

    // --- Workout Logs (History) ---
    @Query("SELECT * FROM workout_logs ORDER BY timestamp DESC")
    fun getAllWorkoutLogs(): Flow<List<WorkoutLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkoutLog(log: WorkoutLogEntity)

    @Delete
    suspend fun deleteWorkoutLog(log: WorkoutLogEntity)

    // --- Smart Alarms ---
    @Query("SELECT * FROM smart_alarms ORDER BY hour ASC, minute ASC")
    fun getAllAlarms(): Flow<List<SmartAlarmEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlarm(alarm: SmartAlarmEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlarms(alarms: List<SmartAlarmEntity>)

    @Update
    suspend fun updateAlarm(alarm: SmartAlarmEntity)

    @Delete
    suspend fun deleteAlarm(alarm: SmartAlarmEntity)

    // --- Achievements ---
    @Query("SELECT * FROM achievements ORDER BY isUnlocked DESC, id ASC")
    fun getAllAchievements(): Flow<List<AchievementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAchievements(achievements: List<AchievementEntity>)

    @Update
    suspend fun updateAchievement(achievement: AchievementEntity)

    // --- Gym Members (Admin Side) ---
    @Query("SELECT * FROM gym_members ORDER BY id DESC")
    fun getAllMembers(): Flow<List<GymMemberEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(member: GymMemberEntity)

    @Update
    suspend fun updateMember(member: GymMemberEntity)

    @Delete
    suspend fun deleteMember(member: GymMemberEntity)

    @Query("DELETE FROM weight_logs")
    suspend fun clearWeightLogs()

    @Query("DELETE FROM workout_logs")
    suspend fun clearWorkoutLogs()

    @Query("DELETE FROM workout_plans")
    suspend fun clearWorkoutPlans()

    @Query("DELETE FROM exercises")
    suspend fun clearExercises()

    @Query("DELETE FROM smart_alarms")
    suspend fun clearSmartAlarms()

    @Query("DELETE FROM achievements")
    suspend fun clearAchievements()

    @Query("DELETE FROM gym_members")
    suspend fun deleteAllMembers()

    @Query("DELETE FROM user_profile")
    suspend fun clearUserProfile()

    @Query("DELETE FROM gym_config")
    suspend fun clearGymConfig()
}
