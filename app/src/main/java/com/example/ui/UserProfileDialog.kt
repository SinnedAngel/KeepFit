package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

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

                // Level Selector Dropdown using M3 style or basic custom Box selector
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
