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
    val durationMinutes: Int,
    val caloriesBurned: Int,
    val detailsSteps: List<String>
)

class KeepFitViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: KeepFitRepository

    init {
        val database = KeepFitDatabase.getDatabase(application)
        repository = KeepFitRepository(database.keepFitDao())
        
        // Asynchronously scaffold default user profile data so the user never sees empty screens
        viewModelScope.launch {
            repository.initializeDefaultProfileIfNeeded()
            // Add a mock step record for today if absent, so we have fresh metrics
            val today = repository.getTodayDateString()
            repository.addStepsToDate(today, 1250) // Starting seed
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

    val KatedaLevels = listOf(
        "Kateda Basic (Healthy Movement)",
        "Kateda Level 1 (Self-Defense Fundamentals)",
        "Kateda Level 2 (Central Energy Breath)",
        "Kateda Level 3 (Healing & Oxygenation)",
        "Kateda Master (Power Synthesis)"
    )

    // Complete list of health exercises aligned with martial arts / healing breathing
    val KatedaWorkoutCatalog = listOf(
        HealthRoutine(
            id = "sikap_basic",
            name = "Sikap Balance Pose & Stance",
            levelRequired = "Kateda Basic (Healthy Movement)",
            description = "Centering your mass, aligning spinal posture, and dynamic joint mobilization to stimulate organic balance.",
            durationMinutes = 5,
            caloriesBurned = 35,
            detailsSteps = listOf("Place feet shoulder-width apart.", "Gently bend knees, keeping spine strictly erect.", "Inhale slow, raising hands to chest level.", "Exhale, pushing hands down slowly while sinking weight.")
        ),
        HealthRoutine(
            id = "harmonizing_breath",
            name = "Harmonizing Abdominal Breath",
            levelRequired = "Kateda Basic (Healthy Movement)",
            description = "Gentle abdominal expansions to enrich blood oxygen, soothe core nervous tension, and condition lungs.",
            durationMinutes = 8,
            caloriesBurned = 50,
            detailsSteps = listOf("Sit cross-legged or stand comfortably.", "Place hands on lower abdomen (Dan Tian equivalent).", "Slowly draw breath through nostrils for 4 seconds, inflating abdomen.", "Hold gently for 2 seconds.", "Exhale slow and empty chest and stomach completely over 6 seconds.")
        ),
        HealthRoutine(
            id = "vital_joint_circuits",
            name = "Vital Energy Joint Circuits",
            levelRequired = "Kateda Basic (Healthy Movement)",
            description = "Coordinated full-body mobilization focusing on neck, wrists, shoulders and leg sockets to remove stagnant tension.",
            durationMinutes = 10,
            caloriesBurned = 60,
            detailsSteps = listOf("Begin neck rotations, synchronized with deep breathing.", "Extend hands and perform circular wrist, elbow, and shoulder patterns.", "Lift alternate knees gently to rotate hip sockets outward.", "Maintain continuous relaxed breathing throughout.")
        ),
        HealthRoutine(
            id = "self_defense_blocks",
            name = "Protective Kinetic Guarding",
            levelRequired = "Kateda Level 1 (Self-Defense Fundamentals)",
            description = "Dynamic upper and lower limb defensive block series that tests core alignment and strengthens physical stability.",
            durationMinutes = 12,
            caloriesBurned = 110,
            detailsSteps = listOf("Adopt solid left lead stance.", "Execute repetitive high, middle, and low guarding patterns with full tension.", "Switch lead stance and repeat.", "Combine posture changes with quick, focused exhales.")
        ),
        HealthRoutine(
            id = "central_energy_ignition",
            name = "Central Energy Core Ignition",
            levelRequired = "Kateda Level 1 (Self-Defense Fundamentals)",
            description = "Brief, highly-concentrated dynamic contractions of the core muscle bands, storing kinetic potential.",
            durationMinutes = 10,
            caloriesBurned = 95,
            detailsSteps = listOf("Stand with deep visual focus.", "Draw a short, sharp in-breath.", "Tense core muscles simultaneously for 5 seconds.", "Relax completely, breathing freely for 10 seconds before replicating.")
        ),
        HealthRoutine(
            id = "abdominal_compaction",
            name = "Tense Abdominal Compaction",
            levelRequired = "Kateda Level 2 (Central Energy Breath)",
            description = "Kateda's core breathing methodology. Teaches deep compression and voluntary control of the abdominal wall.",
            durationMinutes = 15,
            caloriesBurned = 140,
            detailsSteps = listOf("Inhale completely while expanding the torso.", "Exhale firmly, pulling your navel toward the spine.", "Contract and hold abdominal wall tightly for 4-8 seconds while performing slow hand movements.", "Release with a long, satisfying breath restoration.")
        ),
        HealthRoutine(
            id = "inner_power_flow",
            name = "Central Power Flow Form",
            levelRequired = "Kateda Level 2 (Central Energy Breath)",
            description = "Continuous, slow, and deep physical sequences mirroring defense arts with full contraction-coordination cycles.",
            durationMinutes = 20,
            caloriesBurned = 185,
            detailsSteps = listOf("Step into a wide horse stance (Kuda-kuda).", "Perform slow, resistive pushing/pulling hand forms.", "Sync every dynamic press with hard abdominal contraction and empty-lung breath cycles.", "Recover stance slowly.")
        ),
        HealthRoutine(
            id = "therapeutic_breath_wave",
            name = "Therapeutic Healing Wave",
            levelRequired = "Kateda Level 3 (Healing & Oxygenation)",
            description = "Gentle, continuous healing movements design to replenish organ vitality and flush out toxic lactic residues.",
            durationMinutes = 15,
            caloriesBurned = 80,
            detailsSteps = listOf("Unclench all jaw and facial muscles.", "Utilize graceful hand strokes that emulate incoming waves standard to Kateda healing.", "Maintain long, ultra-slow respiratory loops without any pause or compaction.", "Focus mind solely on warm energy circulating to limbs.")
        ),
        HealthRoutine(
            id = "cell_oxygenation",
            name = "Hyper-Oxygenation Sequence",
            levelRequired = "Kateda Level 3 (Healing & Oxygenation)",
            description = "Controlled breathing speed transitions to optimize cellular gas-exchange and promote micro-muscle repair.",
            durationMinutes = 12,
            caloriesBurned = 75,
            detailsSteps = listOf("Assume comfortable sitting position.", "Take 10 quick deep breathing cycles, filling lungs fully and emptying quickly.", "Pause holding breath out for 10 seconds.", "Follow with 2 minutes of ultra-slow soothing therapeutic wave breathing.")
        ),
        HealthRoutine(
            id = "power_breath_synthesis",
            name = "Universal Core Energy Integration",
            levelRequired = "Kateda Master (Power Synthesis)",
            description = "Full integration of high tensional self defense stances, fast compaction blocks, and slow restorative wave states.",
            durationMinutes = 30,
            caloriesBurned = 280,
            detailsSteps = listOf("Conduct 5 minutes of high speed reactive blockers with compaction bursts.", "Transition immediately to 10 minutes of horse-stance energy flow.", "Finish with 15 minutes of deep healing wave oxygenation.", "Rest seated silently for 2 minutes to settle internal energy.")
        )
    )

    // Filtered routines based on the user's level
    val recommendedRoutines: StateFlow<List<HealthRoutine>> = userProfile.map { profile ->
        KatedaWorkoutCatalog.filter { routine ->
            routine.levelRequired == profile.KatedaLevel || 
            // Also recommend basic if they are higher level
            (profile.KatedaLevel != "Kateda Basic (Healthy Movement)" && routine.levelRequired == "Kateda Basic (Healthy Movement)")
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Actions
    fun updateProfile(name: String, level: String, height: Double, weight: Double, stepGoal: Int) {
        viewModelScope.launch {
            val current = userProfile.value
            val updated = current.copy(
                name = name,
                KatedaLevel = level,
                heightCm = height,
                weightKg = weight,
                dailyStepGoal = stepGoal
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
