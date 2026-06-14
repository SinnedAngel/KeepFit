package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.viewmodel.HealthRoutine
import com.example.viewmodel.KeepFitViewModel

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
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
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