package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1,
    val name: String = "Kateda Practitioner",
    val KatedaLevel: String = "White Belt", // Default to the first belt level from API
    val heightCm: Double = 170.0,
    val weightKg: Double = 70.0,
    val dailyStepGoal: Int = 8000,
    val reminderHour: Int = 8,
    val reminderMinute: Int = 0,
    val reminderEnabled: Boolean = true,
    val languageCode: String = "EN"
) {
    val bmi: Double
        get() = if (heightCm > 0) weightKg / ((heightCm / 100.0) * (heightCm / 100.0)) else 0.0
}

@Entity(tableName = "daily_steps")
data class DailySteps(
    @PrimaryKey val dateString: String, // format YYYY-MM-DD
    val steps: Int
)

@Entity(tableName = "bmi_records")
data class BmiRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val heightCm: Double,
    val weightKg: Double,
    val bmiValue: Double
)

@Entity(tableName = "completed_exercises")
data class CompletedExercise(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val exerciseName: String,
    val levelRequired: String,
    val durationMinutes: Int,
    val energyExpelledKcal: Int
)
