package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.AchievementEntity
import com.example.data.model.ExerciseEntity
import com.example.data.model.GymConfigEntity
import com.example.data.model.GymMemberEntity
import com.example.data.model.SmartAlarmEntity
import com.example.data.model.UserProfileEntity
import com.example.data.model.WeightLogEntity
import com.example.data.model.WorkoutLogEntity
import com.example.data.model.WorkoutPlanEntity

@Database(
    entities = [
        GymConfigEntity::class,
        UserProfileEntity::class,
        WorkoutPlanEntity::class,
        ExerciseEntity::class,
        WeightLogEntity::class,
        WorkoutLogEntity::class,
        SmartAlarmEntity::class,
        AchievementEntity::class,
        GymMemberEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun gymDao(): GymDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "gym_workout_manager_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
