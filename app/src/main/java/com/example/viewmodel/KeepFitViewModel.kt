package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Data structures for workout routines based on Martial Arts health level
data class HealthRoutine(
    val id: String,
    val name: String,
    val levelRequired: String,
    val description: String,
    val descriptionEN: String = "",
    val descriptionID: String = "",
    val durationMinutes: Int,
    val caloriesBurned: Int,
    val detailsSteps: List<String>,
    val stepDetailsEN: List<StepDetail> = emptyList(),
    val stepDetailsID: List<StepDetail> = emptyList(),
    val loops: Int = 1,
    val tutorialUrl: String = "", //"https://www.youtube.com/results?search_query=kateda+martial+art+health+breath",
    val slidesUrl: List<String> = emptyList()
)

class KeepFitViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: KeepFitRepository

    private val _katedaWorkoutCatalog = MutableStateFlow<List<HealthRoutine>>(emptyList())
    val KatedaWorkoutCatalog: StateFlow<List<HealthRoutine>> = _katedaWorkoutCatalog.asStateFlow()

    private val _beltLevels = MutableStateFlow<List<BeltLevel>>(emptyList())
    val beltLevels: StateFlow<List<BeltLevel>> = _beltLevels.asStateFlow()

    val KatedaLevelNames: StateFlow<List<String>> = _beltLevels.map { levels ->
        levels.map { it.nameEN }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        val database = KeepFitDatabase.getDatabase(application)
        repository = KeepFitRepository(database.keepFitDao())
        
        // Asynchronously scaffold default user profile data so the user never sees empty screens
        viewModelScope.launch {
            repository.initializeDefaultProfileIfNeeded()
            
            fetchSupabaseData()
        }
    }

    private suspend fun fetchSupabaseData() {
        try {
            val apiKey = com.example.BuildConfig.SUPABASE_ANON_KEY
            val auth = "Bearer $apiKey"

            // 1. Fetch belt levels first so we can map exercises and profile correctly
            val levels = SupabaseClient.api.getBeltLevels(apiKey, auth)
            _beltLevels.value = levels

            // 2. Fetch member profile for mem-1
            val members = SupabaseClient.api.getMemberById(apiKey, auth, "eq.mem-1")
            members.firstOrNull()?.let { member ->
                val currentProfile = userProfile.value
                val updatedProfile = currentProfile.copy(
                    name = member.fullName,
                    KatedaLevel = levels.find { it.id == member.beltLevel }?.nameEN ?: currentProfile.KatedaLevel,
                    heightCm = member.height ?: currentProfile.heightCm,
                    weightKg = member.weight ?: currentProfile.weightKg
                )
                repository.saveUserProfile(updatedProfile)
                // Also log the BMI if height and weight are provided
                if (member.height != null && member.weight != null) {
                    repository.logBmi(member.height, member.weight)
                }
            }

            // 3. Fetch exercises
            val exercises = SupabaseClient.api.getExercises(apiKey, auth)
            
            val routines = exercises.map { ex ->
                val level = levels.find { it.id == ex.difficulty }?.nameEN ?: "White Belt"
                
                HealthRoutine(
                    id = ex.id,
                    name = ex.titleEN,
                    levelRequired = level,
                    description = ex.descriptionEN ?: "",
                    descriptionEN = ex.descriptionEN ?: "",
                    descriptionID = ex.descriptionID ?: "",
                    durationMinutes = if (ex.duration > 0) ex.duration else 10,
                    caloriesBurned = if (ex.calories > 0) ex.calories else 50,
                    detailsSteps = ex.stepsEN ?: emptyList(),
                    stepDetailsEN = ex.stepDetailsEN ?: emptyList(),
                    stepDetailsID = ex.stepDetailsID ?: emptyList(),
                    loops = ex.loops ?: 1,
                    tutorialUrl = ex.videoUrl.orEmpty(), //?: "https://www.youtube.com/results?search_query=kateda+martial+art+health+breath",
                    slidesUrl = ex.slidesUrl ?: emptyList()
                )
            }
            _katedaWorkoutCatalog.value = routines
        } catch (e: Exception) {
            e.printStackTrace()
            // Leave empty or maintain current value if fetch fails
        }
    }

    val userProfile: StateFlow<UserProfile> = repository.userProfile
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserProfile()
        )

    val allDailySteps: StateFlow<List<DailySteps>> = repository.allDailySteps
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allBmiRecords: StateFlow<List<BmiRecord>> = repository.allBmiRecords
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allCompletedExercises: StateFlow<List<CompletedExercise>> = repository.allCompletedExercises
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Derived states
    val todaySteps: StateFlow<Int> = allDailySteps.map { stepsList ->
        val todayStr = repository.getTodayDateString()
        stepsList.find { it.dateString == todayStr }?.steps ?: 0
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val currentBmiSymbolicRating: StateFlow<String> = userProfile.map { profile ->
        val bmi = profile.bmi
        when {
            bmi <= 0.0 -> "N/A"
            bmi < 18.5 -> "Underweight (Need Central Energy Nourishment)"
            bmi < 25.0 -> "Optimal Balance (Harmonious Health)"
            bmi < 30.0 -> "Overweight (Requires Kinetic Compaction)"
            else -> "Obese (Focused Qi Circulation Required)"
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Optimal Balance")

    // Filtered routines based on the user's level
    val recommendedRoutines: StateFlow<List<HealthRoutine>> = combine(userProfile, KatedaWorkoutCatalog, beltLevels) { profile, catalog, levels ->
        val basicLevelName = levels.find { it.id == 1 }?.nameEN ?: "White Belt"
        catalog.filter { routine ->
            routine.levelRequired == profile.KatedaLevel || 
            // Also recommend basic if they are higher level
            (profile.KatedaLevel != basicLevelName && routine.levelRequired == basicLevelName)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Actions
    fun updateProfile(name: String, level: String, height: Double, weight: Double, stepGoal: Int, languageCode: String) {
        viewModelScope.launch {
            val current = userProfile.value
            val updated = current.copy(
                name = name,
                KatedaLevel = level,
                heightCm = height,
                weightKg = weight,
                dailyStepGoal = stepGoal,
                languageCode = languageCode
            )
            repository.saveUserProfile(updated)
            // Automatically record an updated BMI log entry as well
            repository.logBmi(height, weight)
        }
    }

    fun updateReminderSettings(enabled: Boolean, hour: Int, minute: Int) {
        viewModelScope.launch {
            val current = userProfile.value
            val updated = current.copy(
                reminderEnabled = enabled,
                reminderHour = hour,
                reminderMinute = minute
            )
            repository.saveUserProfile(updated)
        }
    }

    fun addStepsToday(steps: Int) {
        viewModelScope.launch {
            val todayStr = repository.getTodayDateString()
            repository.addStepsToDate(todayStr, steps)
        }
    }

    fun setStepsToday(steps: Int) {
        viewModelScope.launch {
            val todayStr = repository.getTodayDateString()
            repository.recordStepsForDate(todayStr, steps)
        }
    }

    fun logBmiManually(height: Double, weight: Double) {
        viewModelScope.launch {
            // Also update the profile values
            val current = userProfile.value
            val updatedProfile = current.copy(heightCm = height, weightKg = weight)
            repository.saveUserProfile(updatedProfile)
            
            repository.logBmi(height, weight)
        }
    }

    fun deleteBmiRecord(id: Int) {
        viewModelScope.launch {
            repository.deleteBmiById(id)
        }
    }

    fun trackCompletedWorkout(routine: HealthRoutine) {
        viewModelScope.launch {
            repository.logCompletedExercise(
                name = routine.name,
                level = routine.levelRequired,
                durationMinutes = routine.durationMinutes,
                caloriesBurned = routine.caloriesBurned
            )
        }
    }

    fun trackCustomCompletedWorkout(name: String, level: String, durationMinutes: Int, caloriesBurned: Int) {
        viewModelScope.launch {
            repository.logCompletedExercise(
                name = name,
                level = level,
                durationMinutes = durationMinutes,
                caloriesBurned = caloriesBurned
            )
        }
    }

    fun deleteCompletedExercise(id: Int) {
        viewModelScope.launch {
            repository.deleteCompletedExerciseById(id)
        }
    }
}

class KeepFitViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(KeepFitViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return KeepFitViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
