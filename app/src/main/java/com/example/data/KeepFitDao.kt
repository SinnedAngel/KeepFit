package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface KeepFitDao {

    // --- User Profile ---
    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    fun getUserProfileFlow(): Flow<UserProfile?>

    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    suspend fun getUserProfile(): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProfile(profile: UserProfile)

    // --- Daily Steps Counter ---
    @Query("SELECT * FROM daily_steps ORDER BY dateString DESC")
    fun getAllDailyStepsFlow(): Flow<List<DailySteps>>

    @Query("SELECT * FROM daily_steps WHERE dateString = :date LIMIT 1")
    suspend fun getDailyStepsForDate(date: String): DailySteps?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailySteps(steps: DailySteps)

    // --- BMI Records ---
    @Query("SELECT * FROM bmi_records ORDER BY timestamp DESC")
    fun getAllBmiRecordsFlow(): Flow<List<BmiRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBmiRecord(record: BmiRecord)

    @Query("DELETE FROM bmi_records WHERE id = :id")
    suspend fun deleteBmiRecordById(id: Int)

    // --- Completed Exercises ---
    @Query("SELECT * FROM completed_exercises ORDER BY timestamp DESC")
    fun getAllCompletedExercisesFlow(): Flow<List<CompletedExercise>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompletedExercise(exercise: CompletedExercise)

    @Query("DELETE FROM completed_exercises WHERE id = :id")
    suspend fun deleteCompletedExerciseById(id: Int)
}
