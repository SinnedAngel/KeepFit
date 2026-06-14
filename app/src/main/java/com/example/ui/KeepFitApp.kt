package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.viewmodel.KeepFitViewModel

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
    val katedaLevelNames by viewModel.KatedaLevelNames.collectAsStateWithLifecycle()

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
                    levels = katedaLevelNames
                )
            }
        }
    }
}