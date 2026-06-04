package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.viewmodel.KeepFitViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
                            text = String.format(Locale.getDefault(), if (userProfile.languageCode == "ID") "%,d langkah" else "%,d steps", step.steps),
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
                                text = String.format(Locale.getDefault(), "BMI: %.1f", record.bmiValue),
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
