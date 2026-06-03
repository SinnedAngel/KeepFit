package com.example.ui

import androidx.compose.animation.*
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
                                text = "A subsidiary of Kateda",
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
                    label = { Text("Dashboard") },
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
                    label = { Text("Exercises") },
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
                    label = { Text("Reminder & Logs") },
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
                    onSave = { name, level, height, weight, stepGoal ->
                        viewModel.updateProfile(name, level, height, weight, stepGoal)
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
                            text = "Greetings, ${userProfile.name}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Level: ${userProfile.KatedaLevel}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            text = "Subsidiary of Kateda Central Energy",
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
                        text = "Daily Steps Circulation",
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
                                text = "Goal: ${userProfile.dailyStepGoal}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${(percentage * 100).toInt()}% completed",
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
                        text = "Calibrate Walk Steps Log",
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
                        text = "BMI & Weight Alignment",
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
                                text = "Current Weight",
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
                                text = "Current Height",
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
                                text = "Body Mass Index (BMI)",
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
                                    text = "Kateda Energy Diagnostic",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                Text(
                                    text = currentBmiRating,
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
                        text = "Kateda Subsidiary Fitness Flows",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "These tailored forms are specialized for ${userProfile.KatedaLevel} and foundational conditioning. Practicing daily promotes deep abdominal oxygenation.",
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
                            text = "No customized physical exercises found for this level.\nTry choosing a different martial art level in your profile settings.",
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

                                    Spacer(modifier = Modifier.height(16.dp))

                                    Button(
                                        onClick = {
                                            viewModel.trackCompletedWorkout(routine)
                                            // trigger a delight feedback
                                            celebrationMessage = "Central Energy Logged! You've accomplished '${routine.name}' (+${routine.caloriesBurned} kcal)"
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("complete_workout_${routine.id}"),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primary
                                        )
                                    ) {
                                        Text(
                                            text = "Complete This Exercise Flow",
                                            color = Color.Black,
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
                                text = "Daily Physical Reminder",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Set alerts for martial breathing routines",
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
                                        text = "Current Alert Slot",
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
                                Text(if (showTimeEdit) "Collapse" else "Modify Time")
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
                            text = "Daily reminders are currently silent.",
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
                    text = "Exercise Circulation Log",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "${allCompletedExercises.size} logged",
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
                            text = "No completed martial routines logged yet.\nHead over to the Exercises tab and finish a session!",
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
                                text = "$dateFormatted | ${log.durationMinutes} mins | ${log.levelRequired}",
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
                text = "Steps Distribution Log",
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
                            text = "No history logged yet.",
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
                            text = String.format("%,d steps", step.steps),
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
                text = "Historic Body Mass Index checks",
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
                            text = "No weight log entries yet.",
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
                                text = "Stats: ${record.weightKg} kg / ${record.heightCm} cm",
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
    onSave: (name: String, level: String, height: Double, weight: Double, stepGoal: Int) -> Unit,
    levels: List<String>
) {
    var name by remember { mutableStateOf(userProfile.name) }
    var expandedDropdown by remember { mutableStateOf(false) }
    var selectedLevel by remember { mutableStateOf(userProfile.KatedaLevel) }
    
    var heightInput by remember { mutableStateOf(userProfile.heightCm.toString()) }
    var weightInput by remember { mutableStateOf(userProfile.weightKg.toString()) }
    var stepGoalInput by remember { mutableStateOf(userProfile.dailyStepGoal.toString()) }

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
                        text = "Edit Keep Fit Profile",
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
                        label = { Text("Practitioner Name") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("profile_name_input")
                    )
                }

                // Level Selector Dropdown using M3 ExposedDropdownMenu style or basic custom Box selector
                item {
                    Text(
                        text = "Select Kateda Health Level:",
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
                            label = { Text("Height (cm)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("profile_height_input")
                        )
                        OutlinedTextField(
                            value = weightInput,
                            onValueChange = { weightInput = it },
                            label = { Text("Weight (kg)") },
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
                        label = { Text("Daily Step Circulation Goal") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("profile_steps_goal_input")
                    )
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
                            Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val h = heightInput.toDoubleOrNull()
                                val w = weightInput.toDoubleOrNull()
                                val goal = stepGoalInput.toIntOrNull()

                                if (name.trim().isEmpty()) {
                                    validationErrorMessage = "Practitioner name cannot be empty."
                                } else if (h == null || h <= 50.0 || h >= 260.0) {
                                    validationErrorMessage = "Please enter height between 50cm and 260cm."
                                } else if (w == null || w <= 10.0 || w >= 300.0) {
                                    validationErrorMessage = "Please enter weight between 10kg and 300kg."
                                } else if (goal == null || goal <= 0) {
                                    validationErrorMessage = "Please enter valid walk step goal."
                                } else {
                                    onSave(name.trim(), selectedLevel, h, w, goal)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier.testTag("save_profile_button")
                        ) {
                            Text("Apply Changes", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
