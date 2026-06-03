package com.example.ui

import android.content.Intent
import android.net.Uri
import android.speech.tts.TextToSpeech
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.isActive
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.draw.scale
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.viewmodel.HealthRoutine
import com.example.viewmodel.KeepFitViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KeepFitApp(
    modifier: Modifier = Modifier,
    viewModel: KeepFitViewModel = viewModel()
) {
    var selectedTab by remember { mutableStateOf(0) }
    var showEditProfileDialog by remember { mutableStateOf(false) }

    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val todaySteps by viewModel.todaySteps.collectAsStateWithLifecycle()
    val currentBmiRating by viewModel.currentBmiSymbolicRating.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "KEEP FIT",
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 20.sp
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .padding(end = 4.dp)
                                    .size(6.pngToDp()) // decorative small indicator
                                    .background(MaterialTheme.colorScheme.secondary, CircleShape)
                            )
                            Text(
                                text = KatedaL10n.get("app_subtitle", userProfile.languageCode),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showEditProfileDialog = true },
                        modifier = Modifier.testTag("edit_profile_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Edit Profile",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                modifier = Modifier.testTag("main_navigation_bar")
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(imageVector = Icons.Default.Home, contentDescription = "Dashboard") },
                    label = { Text(KatedaL10n.get("tab_dashboard", userProfile.languageCode)) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.testTag("nav_tab_dashboard")
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Workouts") },
                    label = { Text(KatedaL10n.get("tab_exercises", userProfile.languageCode)) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.testTag("nav_tab_exercises")
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(imageVector = Icons.Default.Notifications, contentDescription = "History & Settings") },
                    label = { Text(KatedaL10n.get("tab_reminder_logs", userProfile.languageCode)) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.testTag("nav_tab_history")
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            when (selectedTab) {
                0 -> DashboardScreen(
                    viewModel = viewModel,
                    userProfile = userProfile,
                    todaySteps = todaySteps,
                    currentBmiRating = currentBmiRating
                )
                1 -> WorkoutsScreen(
                    viewModel = viewModel,
                    userProfile = userProfile
                )
                2 -> RemindersAndHistoryScreen(
                    viewModel = viewModel,
                    userProfile = userProfile
                )
            }

            if (showEditProfileDialog) {
                UserProfileDialog(
                    userProfile = userProfile,
                    onDismiss = { showEditProfileDialog = false },
                    onSave = { name, level, height, weight, stepGoal, languageCode ->
                        viewModel.updateProfile(name, level, height, weight, stepGoal, languageCode)
                        showEditProfileDialog = false
                    },
                    levels = viewModel.KatedaLevels
                )
            }
        }
    }
}

// Helper int to dp to prevent issues in standard compilation
private fun Int.pngToDp(): androidx.compose.ui.unit.Dp {
    return this.dp
}

// --- SCREEN 1: DASHBOARD ---
@Composable
fun DashboardScreen(
    viewModel: KeepFitViewModel,
    userProfile: com.example.data.UserProfile,
    todaySteps: Int,
    currentBmiRating: String
) {
    var stepDeltaInput by remember { mutableStateOf("") }
    var showQuickInputError by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)
    ) {
        // Welcome and Rank Banner
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("welcome_banner_card")
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.secondary
                                    )
                                ),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Rank Icon",
                            tint = Color.Black,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = KatedaL10n.get("greetings", userProfile.languageCode) + ", ${userProfile.name}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = KatedaL10n.get("level", userProfile.languageCode) + ": ${userProfile.KatedaLevel}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            text = KatedaL10n.get("sub_agency", userProfile.languageCode),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }

        // Circular Step Progress
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = KatedaL10n.get("daily_steps", userProfile.languageCode),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))

                    // Progress Ring
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(180.dp)
                    ) {
                        val percentage = if (userProfile.dailyStepGoal > 0) {
                            todaySteps.toFloat() / userProfile.dailyStepGoal.toFloat()
                        } else 0f

                        val coercedPercentage = percentage.coerceIn(0f, 1.25f)
                        val primaryColor = MaterialTheme.colorScheme.primary
                        val trackColor = MaterialTheme.colorScheme.surfaceVariant

                        Canvas(modifier = Modifier.size(160.dp)) {
                            // Track outline
                            drawArc(
                                color = trackColor,
                                startAngle = -220f,
                                sweepAngle = 260f,
                                useCenter = false,
                                style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round)
                            )
                            // Filled progression
                            drawArc(
                                color = primaryColor,
                                startAngle = -220f,
                                sweepAngle = coercedPercentage * 260f,
                                useCenter = false,
                                style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = String.format("%,d", todaySteps),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = KatedaL10n.get("goal", userProfile.languageCode) + ": ${userProfile.dailyStepGoal}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${(percentage * 100).toInt()}% " + KatedaL10n.get("completed_percentage", userProfile.languageCode),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Quick Steps Increment Panel
                    Text(
                        text = KatedaL10n.get("calibrate", userProfile.languageCode),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { viewModel.addStepsToday(500) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("add_500_steps_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            )
                        ) {
                            Text("+500", fontSize = 13.sp)
                        }
                        
                        Button(
                            onClick = { viewModel.addStepsToday(1000) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("add_1000_steps_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            )
                        ) {
                            Text("+1k Steps", fontSize = 13.sp)
                        }

                        // Direct Input
                        OutlinedTextField(
                            value = stepDeltaInput,
                            onValueChange = { input ->
                                if (input.all { it.isDigit() }) stepDeltaInput = input
                            },
                            placeholder = { Text("Custom", fontSize = 12.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .width(90.dp)
                                .testTag("custom_steps_input"),
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                            )
                        )

                        IconButton(
                            onClick = {
                                val delta = stepDeltaInput.toIntOrNull()
                                if (delta != null && delta > 0) {
                                    viewModel.addStepsToday(delta)
                                    stepDeltaInput = ""
                                    showQuickInputError = false
                                } else {
                                    showQuickInputError = true
                                }
                            },
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
                                .size(40.dp)
                                .testTag("save_custom_steps_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Add custom steps",
                                tint = Color.Black
                            )
                        }
                    }
                    if (showQuickInputError) {
                        Text(
                            text = "Please enter standard positive digits for steps",
                            color = MaterialTheme.colorScheme.secondary,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }

        // BMI Card and Dial
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = KatedaL10n.get("bmi_alignment", userProfile.languageCode),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = KatedaL10n.get("current_weight", userProfile.languageCode),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${userProfile.weightKg} kg",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Column {
                            Text(
                                text = KatedaL10n.get("current_height", userProfile.languageCode),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${userProfile.heightCm} cm",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = KatedaL10n.get("bmi_index", userProfile.languageCode),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = String.format(Locale.getDefault(), "%.1f", userProfile.bmi),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Bio-Energy Status Callout Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Diagnostic",
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = KatedaL10n.get("diagnostic", userProfile.languageCode),
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                val resolvedBmiRating = when {
                                    currentBmiRating.startsWith("Underweight") -> if (userProfile.languageCode == "ID") "Kurang Berat Badan (Butuh Asupan Energi Inti)" else currentBmiRating
                                    currentBmiRating.startsWith("Optimal") -> if (userProfile.languageCode == "ID") "Keseimbangan Optimal (Kesehatan Harmonis)" else currentBmiRating
                                    currentBmiRating.startsWith("Overweight") -> if (userProfile.languageCode == "ID") "Kelebihan Berat Badan (Butuh Pemadatan Kinetik)" else currentBmiRating
                                    currentBmiRating.startsWith("Obese") -> if (userProfile.languageCode == "ID") "Obesitas (Butuh Sirkulasi Energi Terfokus)" else currentBmiRating
                                    else -> currentBmiRating
                                }
                                Text(
                                    text = resolvedBmiRating,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Quick Weight Adjuster for dynamic changes!
                    var weightInputText by remember { mutableStateOf(userProfile.weightKg.toString()) }
                    var showWeightError by remember { mutableStateOf(false) }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = weightInputText,
                            onValueChange = { weightInputText = it },
                            label = { Text("Log Weight (kg)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("weight_update_input")
                        )
                        Button(
                            onClick = {
                                val w = weightInputText.toDoubleOrNull()
                                if (w != null && w > 20.0 && w < 300.0) {
                                    viewModel.logBmiManually(userProfile.heightCm, w)
                                    showWeightError = false
                                } else {
                                    showWeightError = true
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                            modifier = Modifier
                                .height(56.dp)
                                .testTag("update_weight_button")
                        ) {
                            Text("Log Weight", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                    if (showWeightError) {
                        Text(
                            text = "Please enter valid weight between 20kg and 300kg",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

// --- SCREEN 2: WORKOUTS/EXERCISES ---
@Composable
fun WorkoutsScreen(
    viewModel: KeepFitViewModel,
    userProfile: com.example.data.UserProfile
) {
    val recommendedRoutines by viewModel.recommendedRoutines.collectAsStateWithLifecycle()
    var activeExpandedRoutineId by remember { mutableStateOf<String?>(null) }
    var celebrationMessage by remember { mutableStateOf<String?>(null) }
    var activeTimerRoutine by remember { mutableStateOf<HealthRoutine?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp)
        ) {
            item {
                Column {
                    Text(
                        text = KatedaL10n.get("fitness_flows", userProfile.languageCode),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                    val exerciseDesc = if (userProfile.languageCode == "ID") {
                        "Pola gerakan khusus ini diselaraskan untuk tingkat ${userProfile.KatedaLevel} dan pengkondisian dasar fisik. Berlatih setiap hari meningkatkan kapasitas oksigen perut bagian dalam."
                    } else {
                        "These tailored forms are specialized for ${userProfile.KatedaLevel} and foundational conditioning. Practicing daily promotes deep abdominal oxygenation."
                    }
                    Text(
                        text = exerciseDesc,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            if (recommendedRoutines.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = KatedaL10n.get("no_exercise_level", userProfile.languageCode),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(recommendedRoutines) { routine ->
                    val isExpanded = activeExpandedRoutineId == routine.id
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                activeExpandedRoutineId = if (isExpanded) null else routine.id
                            }
                            .testTag("routine_card_${routine.id}"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isExpanded) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = routine.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isExpanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(top = 4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = "Required level",
                                            tint = MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = routine.levelRequired,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.secondary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "${routine.durationMinutes} mins",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "~${routine.caloriesBurned} kcal",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Text(
                                text = routine.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 10.dp)
                            )

                            AnimatedVisibility(
                                visible = isExpanded,
                                enter = expandVertically() + fadeIn(),
                                exit = shrinkVertically() + fadeOut()
                            ) {
                                Column(
                                    modifier = Modifier.padding(top = 16.dp)
                                ) {
                                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "Coordinated Movements:",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    
                                    routine.detailsSteps.forEachIndexed { index, step ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 6.dp),
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(22.dp)
                                                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "${index + 1}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = Color.Black,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(
                                                text = step,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Interactive Simulated Visual Tutorial & Real-time Web Search redirection
                                    ExerciseTutorialCard(
                                        routine = routine,
                                        modifier = Modifier.fillMaxWidth().testTag("exercise_tutorial_card_${routine.id}")
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))

                                    Button(
                                        onClick = {
                                            activeTimerRoutine = routine
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("start_timer_workout_${routine.id}"),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primary
                                        )
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = "Guided session icon",
                                            tint = Color.Black,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = KatedaL10n.get("start_guided", userProfile.languageCode),
                                            color = Color.Black,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    OutlinedButton(
                                        onClick = {
                                            viewModel.trackCompletedWorkout(routine)
                                            // trigger a delight feedback
                                            celebrationMessage = if (userProfile.languageCode == "ID") {
                                                "Energi Inti Tercatat! Anda telah menyelesaikan '${routine.name}' (+${routine.caloriesBurned} kkal)"
                                            } else {
                                                "Central Energy Logged! You've accomplished '${routine.name}' (+${routine.caloriesBurned} kcal)"
                                            }
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("complete_workout_${routine.id}"),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                                    ) {
                                        Text(
                                            text = KatedaL10n.get("quick_log", userProfile.languageCode),
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Celebration dialog Toast-alternative
        celebrationMessage?.let { msg ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp, start = 16.dp, end = 16.dp)
                    .shadow(12.dp)
                    .testTag("celebration_toast")
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Done",
                        tint = Color.Black
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = msg,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "DISMISS",
                        fontWeight = FontWeight.Black,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier
                            .clickable { celebrationMessage = null }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        if (activeTimerRoutine != null) {
            WorkoutTimerDialog(
                routine = activeTimerRoutine!!,
                languageCode = userProfile.languageCode,
                onDismiss = { activeTimerRoutine = null },
                onComplete = { customDuration, customCalories ->
                    viewModel.trackCustomCompletedWorkout(
                        name = activeTimerRoutine!!.name,
                        level = activeTimerRoutine!!.levelRequired,
                        durationMinutes = customDuration,
                        caloriesBurned = customCalories
                    )
                    celebrationMessage = "Guided Flow Finished! Logged '${activeTimerRoutine!!.name}' (+${customCalories} kcal, ${customDuration}m)"
                    activeTimerRoutine = null
                }
            )
        }
    }
}

// --- SCREEN 3: REMINDERS & HISTORY LOGS ---
@Composable
fun RemindersAndHistoryScreen(
    viewModel: KeepFitViewModel,
    userProfile: com.example.data.UserProfile
) {
    val allCompletedExercises by viewModel.allCompletedExercises.collectAsStateWithLifecycle()
    val allDailySteps by viewModel.allDailySteps.collectAsStateWithLifecycle()
    val allBmiRecords by viewModel.allBmiRecords.collectAsStateWithLifecycle()

    var showTimeEdit by remember { mutableStateOf(false) }
    var hourInput by remember { mutableStateOf(userProfile.reminderHour.toString()) }
    var minuteInput by remember { mutableStateOf(String.format("%02d", userProfile.reminderMinute)) }
    var scheduleFeedback by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)
    ) {
        // Exercise Reminder Setup Section
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = KatedaL10n.get("daily_reminder", userProfile.languageCode),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = KatedaL10n.get("set_alerts", userProfile.languageCode),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = userProfile.reminderEnabled,
                            onCheckedChange = { enabled ->
                                viewModel.updateReminderSettings(enabled, userProfile.reminderHour, userProfile.reminderMinute)
                                scheduleFeedback = if (enabled) "Reminders Activated!" else "Reminders Silenced"
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.primary,
                                checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier.testTag("reminder_toggle_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (userProfile.reminderEnabled) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "Alert Time",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = KatedaL10n.get("current_slot", userProfile.languageCode),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = String.format(Locale.getDefault(), "%02d:%02d AM/PM", userProfile.reminderHour, userProfile.reminderMinute),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            Button(
                                onClick = { showTimeEdit = !showTimeEdit },
                                modifier = Modifier.testTag("change_reminder_time_button"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = MaterialTheme.colorScheme.onSurface
                                )
                            ) {
                                Text(if (showTimeEdit) (if (userProfile.languageCode == "ID") "Tutup" else "Collapse") else (if (userProfile.languageCode == "ID") "Ubah Waktu" else "Modify Time"))
                            }
                        }

                        AnimatedVisibility(visible = showTimeEdit) {
                            Column(
                                modifier = Modifier
                                    .padding(top = 16.dp)
                                    .fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = hourInput,
                                        onValueChange = { if (it.length <= 2 && it.all { c -> c.isDigit() }) hourInput = it },
                                        label = { Text("Hour (0-23)") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("reminder_hour_input"),
                                        singleLine = true
                                    )

                                    OutlinedTextField(
                                        value = minuteInput,
                                        onValueChange = { if (it.length <= 2 && it.all { c -> c.isDigit() }) minuteInput = it },
                                        label = { Text("Minute (0-59)") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("reminder_minute_input"),
                                        singleLine = true
                                    )

                                    Button(
                                        onClick = {
                                            val h = hourInput.toIntOrNull()
                                            val m = minuteInput.toIntOrNull()
                                            if (h != null && h in 0..23 && m != null && m in 0..59) {
                                                viewModel.updateReminderSettings(true, h, m)
                                                showTimeEdit = false
                                                scheduleFeedback = String.format("Daily reminder set to %02d:%02d!", h, m)
                                            } else {
                                                scheduleFeedback = "Error: Invalid input hours (0-23) or minutes (0-59)!"
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                        modifier = Modifier
                                            .height(56.dp)
                                            .testTag("save_reminder_time_button")
                                    ) {
                                        Text("Save", color = Color.Black, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    } else {
                        Text(
                            text = KatedaL10n.get("reminders_silent", userProfile.languageCode),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    scheduleFeedback?.let { feedback ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = feedback,
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.testTag("reminder_feedback_msg")
                        )
                    }
                }
            }
        }

        // Section 2: Exercise Completion Log History
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = KatedaL10n.get("exercise_log", userProfile.languageCode),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "${allCompletedExercises.size} " + KatedaL10n.get("logged", userProfile.languageCode),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (allCompletedExercises.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = KatedaL10n.get("no_routines_logged", userProfile.languageCode),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(allCompletedExercises) { log ->
                val dateFormatted = remember(log.timestamp) {
                    val sdf = SimpleDateFormat("MMM d, yyyy - h:mm a", Locale.getDefault())
                    sdf.format(Date(log.timestamp))
                }
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("exercise_log_item_${log.id}"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(14.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = log.exerciseName,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "$dateFormatted | ${log.durationMinutes} " + KatedaL10n.get("mins", userProfile.languageCode) + " | ${log.levelRequired}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "+${log.energyExpelledKcal} kcal",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            IconButton(
                                onClick = { viewModel.deleteCompletedExercise(log.id) },
                                modifier = Modifier.testTag("delete_log_${log.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete record",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section 3: Daily Steps History
        item {
            Text(
                text = KatedaL10n.get("steps_log", userProfile.languageCode),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (allDailySteps.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = KatedaL10n.get("no_history_logged", userProfile.languageCode),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(allDailySteps) { step ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Active",
                                tint = if (step.steps >= userProfile.dailyStepGoal) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = step.dateString,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = String.format(if (userProfile.languageCode == "ID") "%,d langkah" else "%,d steps", step.steps),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (step.steps >= userProfile.dailyStepGoal) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // Section 4: BMI Track Records
        item {
            Text(
                text = KatedaL10n.get("historic_bmi", userProfile.languageCode),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (allBmiRecords.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = KatedaL10n.get("no_weight_log", userProfile.languageCode),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(allBmiRecords) { record ->
                val dateFormatted = remember(record.timestamp) {
                    val sdf = SimpleDateFormat("MMM d, yyyy - h:mm a", Locale.getDefault())
                    sdf.format(Date(record.timestamp))
                }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = dateFormatted,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = KatedaL10n.get("stats", userProfile.languageCode) + ": ${record.weightKg} kg / ${record.heightCm} cm",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = String.format("BMI: %.1f", record.bmiValue),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            IconButton(
                                onClick = { viewModel.deleteBmiRecord(record.id) },
                                modifier = Modifier.testTag("delete_bmi_${record.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- SYSTEM COMPONENT: PROFILE DIALOG ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileDialog(
    userProfile: com.example.data.UserProfile,
    onDismiss: () -> Unit,
    onSave: (name: String, level: String, height: Double, weight: Double, stepGoal: Int, languageCode: String) -> Unit,
    levels: List<String>
) {
    var name by remember { mutableStateOf(userProfile.name) }
    var expandedDropdown by remember { mutableStateOf(false) }
    var selectedLevel by remember { mutableStateOf(userProfile.KatedaLevel) }
    
    var heightInput by remember { mutableStateOf(userProfile.heightCm.toString()) }
    var weightInput by remember { mutableStateOf(userProfile.weightKg.toString()) }
    var stepGoalInput by remember { mutableStateOf(userProfile.dailyStepGoal.toString()) }
    var selectedLanguageCode by remember { mutableStateOf(userProfile.languageCode) }

    var validationErrorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .testTag("edit_profile_dialog"),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            LazyColumn(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        text = KatedaL10n.get("edit_profile_title", userProfile.languageCode),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text(if (userProfile.languageCode == "ID") "Nama Praktisi" else "Practitioner Name") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("profile_name_input")
                    )
                }

                // Level Selector Dropdown using M3 ExposedDropdownMenu style or basic custom Box selector
                item {
                    Text(
                        text = KatedaL10n.get("select_level", userProfile.languageCode),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { expandedDropdown = !expandedDropdown }
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(14.dp)
                            .testTag("profile_level_spinner_trigger")
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = selectedLevel,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.SemiBold
                            )
                            Icon(
                                imageVector = if (expandedDropdown) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = "Dropdown Indicator",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    if (expandedDropdown) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                        ) {
                            Column {
                                levels.forEach { level ->
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                selectedLevel = level
                                                expandedDropdown = false
                                            }
                                            .padding(14.dp)
                                            .testTag("level_option_$level")
                                    ) {
                                        Text(
                                            text = level,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = if (level == selectedLevel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                            fontWeight = if (level == selectedLevel) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = heightInput,
                            onValueChange = { heightInput = it },
                            label = { Text(if (userProfile.languageCode == "ID") "Tinggi Badan (cm)" else "Height (cm)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("profile_height_input")
                        )
                        OutlinedTextField(
                            value = weightInput,
                            onValueChange = { weightInput = it },
                            label = { Text(if (userProfile.languageCode == "ID") "Berat Badan (kg)" else "Weight (kg)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("profile_weight_input")
                        )
                    }
                }

                item {
                    OutlinedTextField(
                        value = stepGoalInput,
                        onValueChange = { stepGoalInput = it },
                        label = { Text(if (userProfile.languageCode == "ID") "Target Sirkulasi Langkah Harian" else "Daily Step Circulation Goal") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("profile_steps_goal_input")
                    )
                }

                item {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = KatedaL10n.get("voice_lang", userProfile.languageCode),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // English Selection Card
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedLanguageCode = "EN" }
                                    .testTag("language_option_en"),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (selectedLanguageCode == "EN") MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
                                ),
                                border = BorderStroke(
                                    1.dp,
                                    if (selectedLanguageCode == "EN") MaterialTheme.colorScheme.primary.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp, horizontal = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "🇬🇧 English (EN)",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (selectedLanguageCode == "EN") FontWeight.Bold else FontWeight.Normal,
                                        color = if (selectedLanguageCode == "EN") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            // Indonesian Selection Card
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedLanguageCode = "ID" }
                                    .testTag("language_option_id"),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (selectedLanguageCode == "ID") MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
                                ),
                                border = BorderStroke(
                                    1.dp,
                                    if (selectedLanguageCode == "ID") MaterialTheme.colorScheme.primary.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp, horizontal = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "🇮🇩 Indonesia (ID)",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (selectedLanguageCode == "ID") FontWeight.Bold else FontWeight.Normal,
                                        color = if (selectedLanguageCode == "ID") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }

                validationErrorMessage?.let { err ->
                    item {
                        Text(
                            text = err,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = onDismiss,
                            modifier = Modifier.testTag("dismiss_profile_button")
                        ) {
                            Text(if (userProfile.languageCode == "ID") "Batal" else "Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val h = heightInput.toDoubleOrNull()
                                val w = weightInput.toDoubleOrNull()
                                val goal = stepGoalInput.toIntOrNull()

                                if (name.trim().isEmpty()) {
                                    validationErrorMessage = if (userProfile.languageCode == "ID") "Nama praktisi tidak boleh kosong." else "Practitioner name cannot be empty."
                                } else if (h == null || h <= 50.0 || h >= 260.0) {
                                    validationErrorMessage = if (userProfile.languageCode == "ID") "Harap masukkan tinggi badan antara 50cm dan 260cm." else "Please enter height between 50cm and 260cm."
                                } else if (w == null || w <= 10.0 || w >= 300.0) {
                                    validationErrorMessage = if (userProfile.languageCode == "ID") "Harap masukkan berat badan antara 10kg dan 300kg." else "Please enter weight between 10kg and 300kg."
                                } else if (goal == null || goal <= 0) {
                                    validationErrorMessage = if (userProfile.languageCode == "ID") "Harap masukkan target jumlah langkah kaki harian yang valid." else "Please enter valid walk step goal."
                                } else {
                                    onSave(name.trim(), selectedLevel, h, w, goal, selectedLanguageCode)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier.testTag("save_profile_button")
                        ) {
                            Text(if (userProfile.languageCode == "ID") "Simpan" else "Apply Changes", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ExerciseTutorialCard(
    routine: HealthRoutine,
    modifier: Modifier = Modifier
) {
    var isPlaying by remember { mutableStateOf(false) }
    var playbackProgress by remember { mutableFloatStateOf(0f) }
    var breathePhase by remember { mutableStateOf("Ready") }
    var timerCounter by remember { mutableStateOf(0) }
    val context = LocalContext.current

    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            while (isPlaying) {
                kotlinx.coroutines.delay(1000)
                timerCounter++
                playbackProgress = (playbackProgress + 0.083f) // Loop simulation block roughly every 12 seconds
                if (playbackProgress > 1f) {
                    playbackProgress = 0f
                }
                breathePhase = when (timerCounter % 12) {
                    in 0..3 -> "INHALE (Abdomen Expanding 4s)"
                    in 4..5 -> "HOLD (Oxygen Consolidating 2s)"
                    else -> "EXHALE (Tension Discharging 6s)"
                }
            }
        } else {
            breathePhase = "Ready"
            timerCounter = 0
            playbackProgress = 0f
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Tutorial Icon",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Technique Video & Alignment Guide",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Simulated Video Playback Screen
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF1E1E1E)),
                contentAlignment = Alignment.Center
            ) {
                if (!isPlaying) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxSize().padding(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = when (routine.imageUrl) {
                                    "stance" -> Icons.Default.Home
                                    "breath" -> Icons.Default.Favorite
                                    "joint" -> Icons.Default.Refresh
                                    "guard" -> Icons.Default.Lock
                                    "ignition" -> Icons.Default.Warning
                                    "compaction" -> Icons.Default.PlayArrow
                                    "power" -> Icons.Default.Star
                                    "healing" -> Icons.Default.FavoriteBorder
                                    "oxygen" -> Icons.Default.Face
                                    else -> Icons.Default.PlayArrow
                                },
                                contentDescription = "Concept Illustration",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "Reference Photo/Video: ${routine.name}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.85f),
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Estimated Flow: ${routine.durationMinutes} mins | Target: ${routine.levelRequired}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.5f),
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp)
                    ) {
                        // Core breathing cycle pulse
                        val pulseFactor = when (breathePhase) {
                            "INHALE (Abdomen Expanding 4s)" -> 1.4f
                            "HOLD (Oxygen Consolidating 2s)" -> 1.1f
                            else -> 0.8f
                        }
                        val animatedScale by animateFloatAsState(
                            targetValue = pulseFactor,
                            animationSpec = spring(dampingRatio = 0.6f),
                            label = "Pulse"
                        )

                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .scale(animatedScale)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = "Lungs pulsing",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = breathePhase,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Progress indication
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Simulated Loop:",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            LinearProgressIndicator(
                                progress = { playbackProgress },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp)),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = Color.White.copy(alpha = 0.15f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = String.format("%02d:00", (playbackProgress * routine.durationMinutes).toInt()),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.8f),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Play/Pause Simulated Demo Button
                Button(
                    onClick = { isPlaying = !isPlaying },
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .testTag("simulate_play_button_${routine.id}"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isPlaying) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (isPlaying) Color.Black else MaterialTheme.colorScheme.onSurface
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Close else Icons.Default.PlayArrow,
                        contentDescription = "Simulate Playback Button",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isPlaying) "Stop Demo" else "Demo Trainer",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Launch External Tutorial Web Link
                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(routine.tutorialUrl))
                        context.startActivity(intent)
                    },
                    modifier = Modifier
                        .weight(1.2f)
                        .height(38.dp)
                        .testTag("launch_tutorial_link_${routine.id}"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Watch video tutorial web link Icon",
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Watch Tutorial",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutTimerDialog(
    routine: HealthRoutine,
    languageCode: String,
    onDismiss: () -> Unit,
    onComplete: (durationMinutes: Int, caloriesBurned: Int) -> Unit
) {
    val isBreathing = routine.id.contains("breath") || 
            routine.id.contains("oxygen") || 
            routine.id.contains("healing") || 
            routine.id.contains("compaction") ||
            routine.name.lowercase().contains("breath")

    // Configuration States
    var inhaleSec by remember { mutableIntStateOf(if (routine.id == "harmonizing_breath") 4 else 5) }
    var holdSec by remember { mutableIntStateOf(if (routine.id == "harmonizing_breath") 2 else if (routine.id == "cell_oxygenation") 10 else 3) }
    var exhaleSec by remember { mutableIntStateOf(if (routine.id == "harmonizing_breath") 6 else 5) }
    var restSec by remember { mutableIntStateOf(if (isBreathing) 2 else 5) }
    
    // For posture/stances
    var holdPostureSec by remember { mutableIntStateOf(if (routine.id == "sikap_basic") 30 else 45) }
    
    var totalLoops by remember { mutableIntStateOf(if (isBreathing) 5 else 3) }

    // Sound customization states
    var enableVoice by remember { mutableStateOf(true) }
    var enableAmbientSound by remember { mutableStateOf(true) }
    var isVoiceSpeaking by remember { mutableStateOf(false) }
    var voiceStartMillis by remember { mutableLongStateOf(0L) }

    // Dialog state: "CONFIG", "RUNNING", "PAUSED", "COMPLETED"
    var dialogState by remember { mutableStateOf("CONFIG") }
    
    // Active Timer States
    var currentLoop by remember { mutableIntStateOf(1) }
    var currentPhase by remember { mutableStateOf(if (isBreathing) "INHALE" else "HOLD_POSTURE") }
    var phaseSecondsRemaining by remember { mutableIntStateOf(0) }
    var totalSecondsElapsed by remember { mutableIntStateOf(0) }
    var isTimerRunning by remember { mutableStateOf(false) }

    // Setup TTS Engine and Dispose on Exit
    val context = LocalContext.current
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    var isTtsReady by remember { mutableStateOf(false) }

    DisposableEffect(context) {
        var instance: TextToSpeech? = null
        instance = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isTtsReady = true
                try {
                    val locale = if (languageCode == "ID") Locale("id", "ID") else Locale.ENGLISH
                    val result = instance?.setLanguage(locale)
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("KeepFitTTS", "Selected locale not supported, backing up to English")
                        instance?.language = Locale.ENGLISH
                    }
                } catch (e: Exception) {
                    Log.e("KeepFitTTS", "Failed to set TTS Locale: ${e.message}")
                }
                instance?.setOnUtteranceProgressListener(object : android.speech.tts.UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        isVoiceSpeaking = true
                    }
                    override fun onDone(utteranceId: String?) {
                        isVoiceSpeaking = false
                    }
                    @Deprecated("Deprecated in Java", ReplaceWith("isVoiceSpeaking = false"))
                    override fun onError(utteranceId: String?) {
                        isVoiceSpeaking = false
                    }
                })
            }
        }
        tts = instance
        onDispose {
            instance?.stop()
            instance?.shutdown()
        }
    }

    // Speech delivery function
    fun speakText(text: String) {
        if (enableVoice && isTtsReady) {
            try {
                isVoiceSpeaking = true
                voiceStartMillis = System.currentTimeMillis()
                val utteranceId = java.util.UUID.randomUUID().toString()
                tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
            } catch (e: Exception) {
                Log.e("KeepFitTTS", "Speech delivery error: ${e.message}")
                isVoiceSpeaking = false
            }
        } else {
            isVoiceSpeaking = false
        }
    }

    // Voice announcement triggers on phase transitions
    LaunchedEffect(currentPhase, currentLoop, isTimerRunning) {
        if (isTimerRunning) {
            val announcement = when (currentPhase) {
                "INHALE" -> {
                    if (languageCode == "ID") {
                        if (currentLoop > 1) "Putaran $currentLoop. Tarik napas." else "Tarik napas."
                    } else {
                        if (currentLoop > 1) "Cycle $currentLoop. Inhale." else "Inhale."
                    }
                }
                "HOLD" -> {
                    if (languageCode == "ID") "Tahan." else "Hold."
                }
                "EXHALE" -> {
                    if (languageCode == "ID") "Hembuskan." else "Exhale."
                }
                "REST" -> {
                    if (languageCode == "ID") "Istirahat." else "Rest."
                }
                "HOLD_POSTURE" -> {
                    if (languageCode == "ID") {
                        if (currentLoop > 1) "Putaran $currentLoop. Tahan." else "Tahan."
                    } else {
                        if (currentLoop > 1) "Cycle $currentLoop. Hold." else "Hold."
                    }
                }
                else -> ""
            }
            if (announcement.isNotEmpty()) {
                speakText(announcement)
            }
        }
    }

    // Voice announcement trigger when session finishes or when starting
    LaunchedEffect(dialogState) {
        if (dialogState == "COMPLETED") {
            val endMsg = if (languageCode == "ID") "Latihan selesai. Kerja bagus!" else "Practice complete. Outstanding work!"
            speakText(endMsg)
        }
    }

    // Real-time Breathing audio flow synthesis
    LaunchedEffect(isTimerRunning, currentPhase, enableAmbientSound, isBreathing) {
        if (!isBreathing || !isTimerRunning || !enableAmbientSound || (currentPhase != "INHALE" && currentPhase != "EXHALE")) {
            return@LaunchedEffect
        }

        withContext(Dispatchers.Default) {
            var track: AudioTrack? = null
            try {
                val sampleRate = 22050
                val minBufSize = AudioTrack.getMinBufferSize(
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                ).coerceAtLeast(4096)

                track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                            .build()
                    )
                    .setBufferSizeInBytes(minBufSize)
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()

                track.play()

                val random = java.util.Random()
                val chunk = ShortArray(1024)
                var lastValue1 = 0f
                var lastValue2 = 0f

                while (isActive) {
                    val maxPhaseSeconds = if (currentPhase == "INHALE") inhaleSec else exhaleSec
                    val remaining = phaseSecondsRemaining.toFloat()

                    // Shape a bell-curve envelope for inhale and exhale breaths
                    val progressRatio = if (currentPhase == "INHALE") {
                        (maxPhaseSeconds - remaining) / maxPhaseSeconds.coerceAtLeast(1)
                    } else {
                        remaining / maxPhaseSeconds.coerceAtLeast(1)
                    }

                    val envelope = if (enableVoice && isVoiceSpeaking) {
                        0f
                    } else {
                        kotlin.math.sin(progressRatio.coerceIn(0f, 1f) * Math.PI.toFloat())
                    }

                    // Low-pass filter coefficient for airflow sound (Inhale is sharper, Exhale is deeper/warmer)
                    val fc = if (currentPhase == "INHALE") 0.35f else 0.22f
                    val targetVolume = envelope * 0.3f // Safe, comforting volume range

                    for (i in chunk.indices) {
                        val rawNoise = random.nextFloat() * 2f - 1f
                        val valIn = rawNoise * targetVolume
                        
                        // Simple 2-pole lowpass filter for an organic wind/breathing effect
                        val filtered = (valIn + lastValue1 + lastValue2) * fc
                        lastValue2 = lastValue1
                        lastValue1 = valIn

                        chunk[i] = (filtered * 32767f).toInt().coerceIn(-32768, 32767).toShort()
                    }

                    track.write(chunk, 0, chunk.size)
                    kotlinx.coroutines.delay(10)
                }
            } catch (e: Exception) {
                Log.e("KeepFitAudio", "Dynamic synth error: ${e.message}")
            } finally {
                try {
                    track?.stop()
                    track?.release()
                } catch (e: Exception) {
                    // Fail-safe cleanup
                }
            }
        }
    }

    // Calculate dynamic values for completion screen
    val totalEstimatedSeconds = remember(isBreathing, inhaleSec, holdSec, exhaleSec, restSec, holdPostureSec, totalLoops) {
        if (isBreathing) {
            (inhaleSec + holdSec + exhaleSec + restSec) * totalLoops
        } else {
            (holdPostureSec + restSec) * totalLoops
        }
    }

    // Handle stopping voice/TTS if timer is paused
    LaunchedEffect(isTimerRunning) {
        if (!isTimerRunning) {
            try {
                tts?.stop()
                isVoiceSpeaking = false
            } catch (e: Exception) {
                // Fail-safe
            }
        }
    }

    // Timer Effect
    LaunchedEffect(isTimerRunning) {
        if (isTimerRunning) {
            while (isTimerRunning && phaseSecondsRemaining > 0) {
                val now = System.currentTimeMillis()
                if (enableVoice && isVoiceSpeaking && (now - voiceStartMillis < 10000L)) {
                    kotlinx.coroutines.delay(100)
                    continue
                }
                kotlinx.coroutines.delay(1000)
                val nowAfterDelay = System.currentTimeMillis()
                if (enableVoice && isVoiceSpeaking && (nowAfterDelay - voiceStartMillis < 10000L)) {
                    continue
                }
                totalSecondsElapsed++
                phaseSecondsRemaining--
                
                if (phaseSecondsRemaining == 0) {
                    // Transition phase
                    if (isBreathing) {
                        when (currentPhase) {
                            "INHALE" -> {
                                if (holdSec > 0) {
                                    currentPhase = "HOLD"
                                    phaseSecondsRemaining = holdSec
                                } else {
                                    currentPhase = "EXHALE"
                                    phaseSecondsRemaining = exhaleSec
                                }
                            }
                            "HOLD" -> {
                                currentPhase = "EXHALE"
                                phaseSecondsRemaining = exhaleSec
                            }
                            "EXHALE" -> {
                                if (restSec > 0) {
                                    currentPhase = "REST"
                                    phaseSecondsRemaining = restSec
                                } else {
                                    if (currentLoop < totalLoops) {
                                        currentLoop++
                                        currentPhase = "INHALE"
                                        phaseSecondsRemaining = inhaleSec
                                    } else {
                                        isTimerRunning = false
                                        dialogState = "COMPLETED"
                                    }
                                }
                            }
                            "REST" -> {
                                if (currentLoop < totalLoops) {
                                    currentLoop++
                                    currentPhase = "INHALE"
                                    phaseSecondsRemaining = inhaleSec
                                } else {
                                    isTimerRunning = false
                                    dialogState = "COMPLETED"
                                }
                            }
                        }
                    } else {
                        // Posture holds
                        when (currentPhase) {
                            "HOLD_POSTURE" -> {
                                if (restSec > 0) {
                                    currentPhase = "REST"
                                    phaseSecondsRemaining = restSec
                                } else {
                                    if (currentLoop < totalLoops) {
                                        currentLoop++
                                        currentPhase = "HOLD_POSTURE"
                                        phaseSecondsRemaining = holdPostureSec
                                    } else {
                                        isTimerRunning = false
                                        dialogState = "COMPLETED"
                                    }
                                }
                            }
                            "REST" -> {
                                if (currentLoop < totalLoops) {
                                    currentLoop++
                                    currentPhase = "HOLD_POSTURE"
                                    phaseSecondsRemaining = holdPostureSec
                                } else {
                                    isTimerRunning = false
                                    dialogState = "COMPLETED"
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    Dialog(onDismissRequest = {
        isTimerRunning = false
        onDismiss()
    }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .testTag("workout_timer_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = routine.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (languageCode == "ID") {
                                if (isBreathing) "Pengukur Waktu Pernapasan Dinamis Kateda" else "Pengukur Waktu Sikap Tubuh Kateda"
                            } else {
                                if (isBreathing) "Kateda Dynamic Breathwork Timer" else "Kateda Posture Hold Timer"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = {
                        isTimerRunning = false
                        onDismiss()
                    }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close Dialog")
                    }
                }

                Divider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )

                when (dialogState) {
                    "CONFIG" -> {
                        Text(
                            text = if (languageCode == "ID") "Konfigurasi Sesi Latihan" else "Configure Practice Flow",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier
                                .weight(1f, fill = false)
                                .fillMaxWidth()
                        ) {
                            if (isBreathing) {
                                TimerSettingRow(
                                    label = if (languageCode == "ID") "Tarik Napas" else "Inhale (Breathe In)",
                                    seconds = inhaleSec,
                                    onSecondsChanged = { inhaleSec = it.coerceIn(2, 12) },
                                    color = Color(0xFF64B5F6)
                                )
                                TimerSettingRow(
                                    label = if (languageCode == "ID") "Tahan Napas (Tekan Perut)" else "Hold (Abs Compacted)",
                                    seconds = holdSec,
                                    onSecondsChanged = { holdSec = it.coerceIn(0, 10) },
                                    color = Color(0xFFFFD54F)
                                )
                                TimerSettingRow(
                                    label = if (languageCode == "ID") "Hembuskan Napas" else "Exhale (Breathe Out)",
                                    seconds = exhaleSec,
                                    onSecondsChanged = { exhaleSec = it.coerceIn(2, 12) },
                                    color = Color(0xFFFF8A65)
                                )
                                TimerSettingRow(
                                    label = if (languageCode == "ID") "Fase Istirahat" else "Rest/Recovery Phase",
                                    seconds = restSec,
                                    onSecondsChanged = { restSec = it.coerceIn(0, 10) },
                                    color = Color(0xFF80CBC4)
                                )
                            } else {
                                TimerSettingRow(
                                    label = if (languageCode == "ID") "Tahan Sikap / Kuda-Kuda" else "Hold Posture / Stance",
                                    seconds = holdPostureSec,
                                    onSecondsChanged = { holdPostureSec = it.coerceIn(5, 120) },
                                    color = Color(0xFFE57373)
                                )
                                TimerSettingRow(
                                    label = if (languageCode == "ID") "Istirahat Antara Sikap" else "Rest/Recovery Between Holds",
                                    seconds = restSec,
                                    onSecondsChanged = { restSec = it.coerceIn(0, 30) },
                                    color = Color(0xFF80CBC4)
                                )
                            }

                            TimerSettingRow(
                                label = if (languageCode == "ID") "Jumlah Putaran Latihan" else "Cycles/Loops Sequence",
                                seconds = totalLoops,
                                onSecondsChanged = { totalLoops = it.coerceIn(1, 15) },
                                color = MaterialTheme.colorScheme.primary,
                                unitLabel = if (languageCode == "ID") "Putaran" else "Loops"
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Audio Configuration Controls (Vocal guidance & breathing loop wind synthesis)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Voice Guidance Card Trigger
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { enableVoice = !enableVoice }
                                    .testTag("voice_guidance_toggle_card"),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (enableVoice) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
                                ),
                                border = BorderStroke(
                                    1.dp,
                                    if (enableVoice) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = if (languageCode == "ID") "Panduan Suara" else "Vocal Guide",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = if (languageCode == "ID") "Perintah suara TTS" else "TTS voice commands",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontSize = 9.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Switch(
                                        checked = enableVoice,
                                        onCheckedChange = { enableVoice = it },
                                        modifier = Modifier.scale(0.7f)
                                    )
                                }
                            }

                            if (isBreathing) {
                                // Dynamic AirWave sound synthesizer Trigger
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { enableAmbientSound = !enableAmbientSound }
                                        .testTag("ambient_sound_toggle_card"),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (enableAmbientSound) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
                                    ),
                                    border = BorderStroke(
                                        1.dp,
                                        if (enableAmbientSound) MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = if (languageCode == "ID") "Gelombang Paru" else "Lung Wave",
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = if (languageCode == "ID") "Deru angin pernapasan" else "Breathing audio wind",
                                                style = MaterialTheme.typography.bodySmall,
                                                fontSize = 9.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Switch(
                                            checked = enableAmbientSound,
                                            onCheckedChange = { enableAmbientSound = it },
                                            modifier = Modifier.scale(0.7f)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Duration Summary
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Clock",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (languageCode == "ID") "Total Durasi Sesi" else "Total Session Duration",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Text(
                                    text = formatDuration(totalEstimatedSeconds),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                dialogState = "RUNNING"
                                currentLoop = 1
                                currentPhase = if (isBreathing) "INHALE" else "HOLD_POSTURE"
                                phaseSecondsRemaining = if (isBreathing) inhaleSec else holdPostureSec
                                totalSecondsElapsed = 0
                                isTimerRunning = true
                                val startMsg = if (languageCode == "ID") "Mulai latihan. Regangkan tubuh Anda." else "Begin practice. Expand your body."
                                speakText(startMsg)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                               .testTag("begin_practice_timer_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = if (languageCode == "ID") "MULAI SESI LATIHAN" else "BEGIN GUIDED PRACTICE",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }
                    }

                    "RUNNING", "PAUSED" -> {
                        // Current Phase Settings for Color scheme
                        val (phaseColor, phaseDescription) = when (currentPhase) {
                            "INHALE" -> Color(0xFF64B5F6) to (if (languageCode == "ID") "Tarik napas dalam-dalam lewat hidung, kembangkan perut bagian bawah." else "Deeply inhale through nostrils, expanding lower abdomen.")
                            "HOLD" -> Color(0xFFFFD54F) to (if (languageCode == "ID") "Tahan napas. Kunci pemadatan perut inti." else "Hold breath. Secure core abdominal compaction.")
                            "EXHALE" -> Color(0xFFFF8A65) to (if (languageCode == "ID") "Hembuskan napas perlahan, tekan ketegangan ke bawah dan luar." else "Slowly exhale, pushing tension down and out.")
                            "REST" -> Color(0xFF80CBC4) to (if (languageCode == "ID") "Rilekskan semua otot. Kalibrasi kembali aliran napas alami." else "Relax all muscles. Recalibrating natural breathing wave.")
                            "HOLD_POSTURE" -> Color(0xFFE57373) to (if (languageCode == "ID") "Sikap tegak dengan keseimbangan kokoh. Diam jangan bergerak." else "Engage posture with rigid balance. Hold still.")
                            else -> MaterialTheme.colorScheme.primary to (if (languageCode == "ID") "Pertahankan konsentrasi." else "Maintain concentration.")
                        }

                        val maxPhaseSeconds = when (currentPhase) {
                            "INHALE" -> inhaleSec
                            "HOLD" -> holdSec
                            "EXHALE" -> exhaleSec
                            "REST" -> restSec
                            "HOLD_POSTURE" -> holdPostureSec
                            else -> 5
                        }

                        // Progress fraction
                        val fraction = if (maxPhaseSeconds > 0) {
                            phaseSecondsRemaining.toFloat() / maxPhaseSeconds
                        } else 1f

                        // Pulsing radius factor based on breathing phases
                        val pulseTarget = when (currentPhase) {
                            "INHALE" -> 0.8f + 0.6f * (1f - fraction)
                            "HOLD" -> 1.4f + 0.05f * kotlin.math.sin(totalSecondsElapsed * 3f)
                            "EXHALE" -> 0.8f + 0.6f * fraction
                            "REST" -> 0.82f + 0.02f * kotlin.math.sin(totalSecondsElapsed * 1.5f)
                            "HOLD_POSTURE" -> 1.0f + 0.04f * kotlin.math.sin(totalSecondsElapsed * 4f)
                            else -> 1f
                        }

                        val animatedPulseScale by animateFloatAsState(
                            targetValue = pulseTarget,
                            animationSpec = spring(stiffness = Spring.StiffnessLow),
                            label = "BreathingCorePulse"
                        )

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Cycle status
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f))
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Loop",
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "CYCLE $currentLoop OF $totalLoops",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }

                            // Active Practice Screen Sound & Speech Quick Controls
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 8.dp)
                            ) {
                                // Live Speech guide toggle mini trigger
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .clickable { 
                                            enableVoice = !enableVoice 
                                            if (enableVoice) {
                                                val guideMsg = if (languageCode == "ID") "Panduan suara aktif." else "Speech guide active."
                                                speakText(guideMsg)
                                            }
                                        }
                                        .background(if (enableVoice) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else Color.Gray.copy(alpha = 0.1f))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = if (enableVoice) Icons.Default.PlayArrow else Icons.Default.Close,
                                        contentDescription = "Voice Guide Activator",
                                        tint = if (enableVoice) MaterialTheme.colorScheme.primary else Color.Gray,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (enableVoice) "Voice: ON" else "Voice: OFF",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (enableVoice) MaterialTheme.colorScheme.primary else Color.Gray
                                    )
                                }

                                if (isBreathing) {
                                    // Live Breathing wind synthesizer toggle mini trigger
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .clickable { enableAmbientSound = !enableAmbientSound }
                                            .background(if (enableAmbientSound) MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f) else Color.Gray.copy(alpha = 0.1f))
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (enableAmbientSound) Icons.Default.Favorite else Icons.Default.Close,
                                            contentDescription = "Auditory Wave Activator",
                                            tint = if (enableAmbientSound) MaterialTheme.colorScheme.secondary else Color.Gray,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (enableAmbientSound) "Synth: ON" else "Synth: OFF",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (enableAmbientSound) MaterialTheme.colorScheme.secondary else Color.Gray
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Interactive Canvas Circular Tracker & Custom Breathing ball
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(170.dp)
                                    .padding(8.dp)
                            ) {
                                // Background Arch
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    drawArc(
                                        color = Color.Gray.copy(alpha = 0.1f),
                                        startAngle = -90f,
                                        sweepAngle = 360f,
                                        useCenter = false,
                                        style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
                                    )
                                }

                                // Foreground remaining duration arc
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    drawArc(
                                        color = phaseColor,
                                        startAngle = -90f,
                                        sweepAngle = 360f * fraction,
                                        useCenter = false,
                                        style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
                                    )
                                }

                                // Simulated organic lungs/energy core pulsing ball
                                Box(
                                    modifier = Modifier
                                        .size(90.dp)
                                        .scale(animatedPulseScale)
                                        .background(
                                            Brush.radialGradient(
                                                colors = listOf(
                                                    phaseColor,
                                                    phaseColor.copy(alpha = 0.4f),
                                                    Color.Transparent
                                                )
                                            ),
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isBreathing) Icons.Default.Favorite else Icons.Default.Star,
                                        contentDescription = "Action Symbol",
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                // Phase countdown text block superimposed
                                Text(
                                    text = "$phaseSecondsRemaining",
                                    style = MaterialTheme.typography.headlineLarge,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White,
                                    modifier = Modifier.offset(y = 52.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Current phase name
                            Text(
                                text = when (currentPhase) {
                                    "INHALE" -> "INHALE"
                                    "HOLD" -> "HOLD BREATH"
                                    "EXHALE" -> "EXHALE"
                                    "REST" -> "REST & DETOX"
                                    "HOLD_POSTURE" -> "HOLD STANCE"
                                    else -> "PRACTICING"
                                },
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = phaseColor,
                                letterSpacing = 1.5.sp
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            // Interactive phase descriptive tutorial instruction
                            Text(
                                text = phaseDescription,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp)
                                    .height(34.dp)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Control Bar
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Quit/Stop Button
                                OutlinedIconButton(
                                    onClick = {
                                        isTimerRunning = false
                                        dialogState = "CONFIG"
                                    },
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                    modifier = Modifier
                                        .padding(horizontal = 6.dp)
                                        .size(44.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Stop",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }

                                // Play Pause central toggle
                                FilledIconButton(
                                    onClick = {
                                        isTimerRunning = !isTimerRunning
                                        dialogState = if (isTimerRunning) "RUNNING" else "PAUSED"
                                    },
                                    colors = IconButtonDefaults.filledIconButtonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = Color.Black
                                    ),
                                    modifier = Modifier
                                        .padding(horizontal = 6.dp)
                                        .size(52.dp)
                                        .testTag("play_pause_timer_toggle")
                                ) {
                                    Icon(
                                        imageVector = if (isTimerRunning) Icons.Default.Refresh else Icons.Default.PlayArrow,
                                        contentDescription = if (isTimerRunning) "Pause" else "Play",
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                // Skip Phase Button
                                OutlinedIconButton(
                                    onClick = {
                                        phaseSecondsRemaining = 1
                                    },
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                    modifier = Modifier
                                        .padding(horizontal = 6.dp)
                                        .size(44.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Share, 
                                        contentDescription = "Skip Phase",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Overall Elapsed
                            Text(
                                text = "Total elapsed: ${formatDuration(totalSecondsElapsed)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                    }

                    "COMPLETED" -> {
                        val finalDurationMinutes = maxOf(1, (totalSecondsElapsed / 60))
                        val calculatedCaloriesBurned = remember(finalDurationMinutes, routine) {
                            val progressRatio = totalSecondsElapsed.toFloat() / totalEstimatedSeconds.coerceAtLeast(1)
                            val k = (routine.caloriesBurned * progressRatio).toInt()
                            maxOf(10, k)
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("timer_complete_screen")
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Finished",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(36.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Kateda Flow Completed!",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Text(
                                text = "Your physical vessels are oxygenated and central energy has been harmonized.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                            )

                            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                            // Report Stats
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "TIME SPENT",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = formatDuration(totalSecondsElapsed),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "ESTIMATED BURN",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "$calculatedCaloriesBurned kcal",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "CYCLES DONE",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "$totalLoops / $totalLoops",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = {
                                    onComplete(finalDurationMinutes, calculatedCaloriesBurned)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("save_practice_history_btn"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = "SAVE PRACTICE TO DIARY",
                                    color = Color.Black,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            TextButton(
                                onClick = { onDismiss() },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Close without logging",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TimerSettingRow(
    label: String,
    seconds: Int,
    onSecondsChanged: (Int) -> Unit,
    color: Color,
    unitLabel: String = "Sec"
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.08f))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "$seconds $unitLabel",
                fontSize = 11.sp,
                color = color,
                fontWeight = FontWeight.SemiBold
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            // Minus Button
            OutlinedIconButton(
                onClick = { onSecondsChanged(seconds - if (unitLabel == "Loops") 1 else if (seconds <= 10) 1 else 5) },
                border = BorderStroke(1.dp, color.copy(alpha = 0.4f)),
                modifier = Modifier.size(28.dp)
            ) {
                Text("-", fontWeight = FontWeight.Bold, color = color, fontSize = 12.sp)
            }

            // Slider in center
            Slider(
                value = seconds.toFloat(),
                onValueChange = { onSecondsChanged(it.toInt()) },
                valueRange = if (unitLabel == "Loops") 1f..15f else if (label.contains("Post")) 5f..120f else 0f..12f,
                modifier = Modifier.width(80.dp),
                colors = SliderDefaults.colors(
                    activeTrackColor = color,
                    inactiveTrackColor = color.copy(alpha = 0.2f),
                    thumbColor = color
                )
            )

            // Plus Button
            OutlinedIconButton(
                onClick = { onSecondsChanged(seconds + if (unitLabel == "Loops") 1 else if (seconds < 10) 1 else 5) },
                border = BorderStroke(1.dp, color.copy(alpha = 0.4f)),
                modifier = Modifier.size(28.dp)
            ) {
                Text("+", fontWeight = FontWeight.Bold, color = color, fontSize = 12.sp)
            }
        }
    }
}

fun formatDuration(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return if (m > 0) "${m}m ${s}s" else "${s}s"
}

object KatedaL10n {
    fun get(key: String, lang: String): String {
        val entry = translations[key] ?: return key
        return entry[lang] ?: entry["EN"] ?: key
    }

    private val translations = mapOf(
        "app_subtitle" to mapOf("EN" to "A subsidiary of Kateda", "ID" to "Anak perusahaan Kateda"),
        "tab_dashboard" to mapOf("EN" to "Dashboard", "ID" to "Dasbor"),
        "tab_exercises" to mapOf("EN" to "Exercises", "ID" to "Latihan"),
        "tab_reminder_logs" to mapOf("EN" to "Reminder & Logs", "ID" to "Alarm & Catatan"),
        "greetings" to mapOf("EN" to "Greetings", "ID" to "Salam"),
        "level" to mapOf("EN" to "Level", "ID" to "Tingkatan"),
        "sub_agency" to mapOf("EN" to "Subsidiary of Kateda Central Energy", "ID" to "Afiliasi Energi Pusat Kateda"),
        "daily_steps" to mapOf("EN" to "Daily Steps Circulation", "ID" to "Sirkulasi Langkah Harian"),
        "goal" to mapOf("EN" to "Goal", "ID" to "Target"),
        "completed_percentage" to mapOf("EN" to "completed", "ID" to "selesai"),
        "calibrate" to mapOf("EN" to "Calibrate Walk Steps Log", "ID" to "Kalibrasi Langkah"),
        "valid_steps_err" to mapOf("EN" to "Please enter standard positive digits for steps", "ID" to "Harap masukkan langkah yang valid"),
        "bmi_alignment" to mapOf("EN" to "BMI & Weight Alignment", "ID" to "Penyelarasan Berat Badan & BMI"),
        "current_weight" to mapOf("EN" to "Current Weight", "ID" to "Berat Badan"),
        "current_height" to mapOf("EN" to "Current Height", "ID" to "Tinggi Badan"),
        "bmi_index" to mapOf("EN" to "Body Mass Index (BMI)", "ID" to "Indeks Massa Tubuh (BMI)"),
        "diagnostic" to mapOf("EN" to "Kateda Energy Diagnostic", "ID" to "Diagnosis Energi Kateda"),
        "valid_weight_err" to mapOf("EN" to "Please enter valid weight between 20kg and 300kg", "ID" to "Harap masukkan berat antara 20kg & 300kg"),
        "fitness_flows" to mapOf("EN" to "Kateda Subsidiary Fitness Flows", "ID" to "Sirkulasi Aliran Latihan Kateda"),
        "no_exercise_level" to mapOf("EN" to "No customized physical exercises found for this level.\nTry choosing a different martial art level in your profile settings.", "ID" to "Tidak ada latihan fisik yang disesuaikan untuk tingkat ini.\nCoba pilih tingkat seni bela diri yang berbeda di pengaturan profil."),
        "mins" to mapOf("EN" to "mins", "ID" to "menit"),
        "coordinated_movs" to mapOf("EN" to "Coordinated Movements:", "ID" to "Kombinasi Gerakan:"),
        "start_guided" to mapOf("EN" to "Start Guided Practice Timer", "ID" to "Mulai Sesi Latihan"),
        "quick_log" to mapOf("EN" to "Quick Log Practice Finish", "ID" to "Catat Selesai Cepat"),
        "dismiss" to mapOf("EN" to "DISMISS", "ID" to "BATAL"),
        "daily_reminder" to mapOf("EN" to "Daily Physical Reminder", "ID" to "Pengingat Latihan Harian"),
        "set_alerts" to mapOf("EN" to "Set alerts for martial breathing routines", "ID" to "Atur waktu pengingat latihan pernapasan harian"),
        "current_slot" to mapOf("EN" to "Current Alert Slot", "ID" to "Jadwal Alarm Aktif"),
        "reminders_silent" to mapOf("EN" to "Daily reminders are currently silent.", "ID" to "Jadwal pengingat harian saat ini kosong."),
        "exercise_log" to mapOf("EN" to "Exercise Circulation Log", "ID" to "Catatan Distribusi Latihan"),
        "logged" to mapOf("EN" to "logged", "ID" to "tercatat"),
        "no_routines_logged" to mapOf("EN" to "No completed martial routines logged yet.\nHead over to the Exercises tab and finish a session!", "ID" to "Kamu belum mencatat latihan apa pun.\nMulai latihan Anda dari tab Latihan!"),
        "steps_log" to mapOf("EN" to "Steps Distribution Log", "ID" to "Catatan Sirkulasi Langkah"),
        "no_history_logged" to mapOf("EN" to "No history logged yet.", "ID" to "Belum ada catatan."),
        "historic_bmi" to mapOf("EN" to "Historic Body Mass Index checks", "ID" to "Pemeriksaan Indeks Massa Tubuh"),
        "no_weight_log" to mapOf("EN" to "No weight log entries yet.", "ID" to "Belum ada catatan berat."),
        "stats" to mapOf("EN" to "Stats", "ID" to "Statistik"),
        "edit_profile_title" to mapOf("EN" to "Edit Keep Fit Profile", "ID" to "Ubah Profil Keep Fit"),
        "select_level" to mapOf("EN" to "Select Kateda Health Level:", "ID" to "Pilih Tingkatan Kesehatan Kateda:"),
        "voice_lang" to mapOf("EN" to "Vocal Command Language (TTS):", "ID" to "Bahasa Panduan Suara (TTS):"),
        "tech_guide_title" to mapOf("EN" to "Technique Video & Alignment Guide", "ID" to "Panduan Aliran & Gerakan"),
        "ref_photo" to mapOf("EN" to "Reference Photo/Video", "ID" to "Referensi Teknik/Gerakan"),
        "est_flow" to mapOf("EN" to "Estimated Flow", "ID" to "Estimasi Durasi"),
        "target_level" to mapOf("EN" to "Target Level", "ID" to "Tingkatan Target"),
        "sim_loop" to mapOf("EN" to "Simulated Loop:", "ID" to "Animasi Gerakan:"),
        "watch_tutorial" to mapOf("EN" to "Watch Tutorial", "ID" to "Tonton Tutorial"),
        "config_practice" to mapOf("EN" to "Configure Practice Flow", "ID" to "Konfigurasi Sesi Latihan"),
        "vocal_guide" to mapOf("EN" to "Vocal Guide", "ID" to "Panduan Suara"),
        "tts_commands" to mapOf("EN" to "TTS voice commands", "ID" to "Perintah suara"),
        "lung_wave" to mapOf("EN" to "Lung Wave", "ID" to "Gelombang Paru"),
        "breathing_wind" to mapOf("EN" to "Breathing audio wind", "ID" to "Efek napas udara"),
        "tot_duration" to mapOf("EN" to "Total Session Duration", "ID" to "Total Durasi Latihan"),
        "begin_guided" to mapOf("EN" to "BEGIN GUIDED PRACTICE", "ID" to "MULAI LATIHAN"),
        "cycle_of" to mapOf("EN" to "CYCLE", "ID" to "PUTARAN"),
        "cycle_of_connector" to mapOf("EN" to "OF", "ID" to "DARI"),
        "total_elapsed" to mapOf("EN" to "Total elapsed", "ID" to "Total waktu"),
        "flow_completed" to mapOf("EN" to "Kateda Flow Completed!", "ID" to "Latihan Kateda Selesai!"),
        "flow_completed_desc" to mapOf("EN" to "Your physical vessels are oxygenated and central energy has been harmonized.", "ID" to "Aliran darah Anda telah teroksigenasi, serta energi inti tubuh Anda telah selaras."),
        "time_spent" to mapOf("EN" to "TIME SPENT", "ID" to "DURASI"),
        "est_burn" to mapOf("EN" to "ESTIMATED BURN", "ID" to "ESTIMASI KALORI"),
        "cycles_done" to mapOf("EN" to "CYCLES DONE", "ID" to "PUTARAN SELESAI"),
        "save_practice" to mapOf("EN" to "SAVE PRACTICE TO DIARY", "ID" to "SIMPAN LATIHAN"),
        "close_no_log" to mapOf("EN" to "Close without logging", "ID" to "Tutup tanpa menyimpan")
    )
}
