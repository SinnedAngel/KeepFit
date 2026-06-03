package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class KeepFitRepository(private val dao: KeepFitDao) {

    // --- User Profile ---
    val userProfile: Flow<UserProfile> = dao.getUserProfileFlow().map { profile ->
        profile ?: UserProfile() // provide default if database is empty
    }

    suspend fun saveUserProfile(profile: UserProfile) {
        dao.insertUserProfile(profile)
    }

    suspend fun initializeDefaultProfileIfNeeded() {
        val current = dao.getUserProfile()
        if (current == null) {
            dao.insertUserProfile(UserProfile())
        }
    }

    // --- Daily Steps ---
    val allDailySteps: Flow<List<DailySteps>> = dao.getAllDailyStepsFlow()

    suspend fun recordStepsForDate(dateString: String, steps: Int) {
        dao.insertDailySteps(DailySteps(dateString = dateString, steps = steps))
    }

    suspend fun addStepsToDate(dateString: String, deltaSteps: Int) {
        val currentLog = dao.getDailyStepsForDate(dateString)
        val currentSteps = currentLog?.steps ?: 0
        dao.insertDailySteps(DailySteps(dateString = dateString, steps = currentSteps + deltaSteps))
    }

    // Helper to get today's date string
    fun getTodayDateString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    // --- BMI Records ---
    val allBmiRecords: Flow<List<BmiRecord>> = dao.getAllBmiRecordsFlow()

    suspend fun logBmi(heightCm: Double, weightKg: Double) {
        val bmi = if (heightCm > 0) weightKg / ((heightCm / 100.0) * (heightCm / 100.0)) else 0.0
        dao.insertBmiRecord(
            BmiRecord(
                heightCm = heightCm,
                weightKg = weightKg,
                bmiValue = bmi
            )
        )
    }

    suspend fun deleteBmiById(id: Int) {
        dao.deleteBmiRecordById(id)
    }

    // --- Completed Exercises ---
    val allCompletedExercises: Flow<List<CompletedExercise>> = dao.getAllCompletedExercisesFlow()

    suspend fun logCompletedExercise(name: String, level: String, durationMinutes: Int, caloriesBurned: Int) {
        dao.insertCompletedExercise(
            CompletedExercise(
                exerciseName = name,
                levelRequired = level,
                durationMinutes = durationMinutes,
                energyExpelledKcal = caloriesBurned
            )
        )
    }

    suspend fun deleteCompletedExerciseById(id: Int) {
        dao.deleteCompletedExerciseById(id)
    }
}
