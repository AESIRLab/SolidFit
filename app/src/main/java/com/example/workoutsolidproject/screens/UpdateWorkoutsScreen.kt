package com.example.workoutsolidproject.screens

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.workoutsolidproject.BottomNavItem
import com.example.workoutsolidproject.R
import com.example.workoutsolidproject.SolidAuthFlowScreen
import com.example.workoutsolidproject.WorkoutItemViewModel
import com.example.workoutsolidproject.WorkoutList
import com.example.workoutsolidproject.healthdata.HealthConnectManager
import com.example.workoutsolidproject.healthdata.InputReadingsViewModel
import com.example.workoutsolidproject.healthdata.InputReadingsViewModelFactory
import com.example.workoutsolidproject.healthdata.showExceptionSnackbar
import com.example.workoutsolidproject.model.WorkoutItem
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.skCompiler.generatedModel.AuthTokenStore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun UpdateWorkouts(
    healthConnectManager: HealthConnectManager,
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val viewModel: WorkoutItemViewModel = viewModel(
        factory = WorkoutItemViewModel.Factory
    )
    val coroutineScope = rememberCoroutineScope()
    val store = AuthTokenStore(LocalContext.current.applicationContext)
    val snackbarHostState = remember { SnackbarHostState() }

    val navBarItems = listOf(
        BottomNavItem.WorkoutList,
        BottomNavItem.HeartMonitor,
        BottomNavItem.WeightMonitor,
    )

    LifecycleEventEffect(event = Lifecycle.Event.ON_RESUME) {
        runBlocking {
            val webId = store.getWebId().first()
            viewModel.updateWebId(webId)
            if (!viewModel.remoteIsAvailable()) {
                val accessToken = store.getAccessToken().first()
                val signingJwk = store.getSigner().first()
                // TODO: need add expiration time
                val expirationTime = 2301220800000// store.get
                viewModel.setRemoteRepositoryData(
                    accessToken,
                    signingJwk,
                    webId,
                    expirationTime
                )
            }
        }
    }

    LifecycleEventEffect(event = Lifecycle.Event.ON_STOP) {
        coroutineScope.launch {
            viewModel.updateRemote()
        }
    }

    Scaffold {
        val context = LocalContext.current
        NavHost(
            navController = navController,
            startDestination = SolidAuthFlowScreen.WorkoutList.name,
        ) {
            // SCREEN: displays the list of workouts
            composable(route = SolidAuthFlowScreen.WorkoutList.name) {
                // Fetches all workout items from repo
                //TODO: Is this okay?: repository.allWorkoutItems -> viewModel.allItems
                val workouts by viewModel.allItems.collectAsState(initial = emptyList())

                Scaffold(
                    topBar = {
                        TopAppBar(
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = Color.hsl(
                                    224f,
                                    1f,
                                    0.73f
                                ),
                                titleContentColor = MaterialTheme.colorScheme.primary,
                            ),
                            title = {
                                Row (modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(end = 30.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween)
                                {
                                    Text(
                                        "Workout Tracker",
                                        fontSize = 26.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Image(
                                        painter = painterResource(id = R.drawable.exercise_white_34dp),
                                        contentDescription = "App logo"
                                    )
                                }
                            }
                        )
                    },
                    bottomBar = {
                        NavigationBar {
                            val navBackStackEntry by navController.currentBackStackEntryAsState()
                            val currentDestination = navBackStackEntry?.destination
                            navBarItems.forEach { screen ->
                                NavigationBarItem(
                                    icon = { Icon(screen.icon, contentDescription = screen.title) },
                                    label = { Text(screen.title) },
                                    selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                                    onClick = {
                                        navController.navigate(screen.route) {
                                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                )
                            }
                        }
                    },
                    floatingActionButton = {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 32.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            // This will only show the FAB on the WorkoutList screen
                            if (currentDestination?.route == SolidAuthFlowScreen.WorkoutList.name) {
                                FloatingActionButton(
                                    containerColor = Color.hsl(224f, 1f, 0.73f),
                                    onClick = { navController.navigate(route = SolidAuthFlowScreen.AddEditWorkoutScreen.name) },
                                    shape = CircleShape,
                                ) {
                                    Icon(
                                        Icons.Filled.Add,
                                        contentDescription = "Add workout",
                                        modifier = Modifier.size(25.dp)
                                    )
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(
                                top = innerPadding.calculateTopPadding(),
                                bottom = innerPadding.calculateBottomPadding()
                            )
//                            .navigationBarsPadding()
                    ) {
                        WorkoutList(
                            workouts = workouts,
                            onDeleteWorkout = { workout ->
                                coroutineScope.launch {
                                    viewModel.delete(workout)
                                }
                            },
                            onEditWorkout = { workout ->
                                navController.navigate(route = "${SolidAuthFlowScreen.AddEditWorkoutScreen.name}/${workout.id}")
                            }
                        )
                    }
                }
            }

            // SCREEN: Add workout
            composable(route = SolidAuthFlowScreen.AddEditWorkoutScreen.name) {
                val coroutineScope = rememberCoroutineScope()
                AddEditWorkoutScreen(
                    onSaveWorkout = { _, name, calories, duration ->
                        coroutineScope.launch {
                            viewModel.insert(
                                WorkoutItem(
                                    id = "",
                                    name = name,
                                    caloriesBurned = calories,
                                    duration = duration,
                                )
                            )
                            saveWorkoutLog(context)
                            navController.navigate(SolidAuthFlowScreen.WorkoutList.name)
                        }
                    },
                    onCancel = {
                        navController.navigate(SolidAuthFlowScreen.WorkoutList.name)
                    }
                )
            }

            // SCREEN: Edit workout
            composable(
                route = "${SolidAuthFlowScreen.AddEditWorkoutScreen.name}/{workoutUri}",
                arguments = listOf(navArgument("workoutUri") { type = NavType.StringType })
            ) { backStackEntry ->
                val workoutUri = backStackEntry.arguments?.getString("workoutUri") ?: return@composable
                LaunchedEffect(workoutUri) {
                    viewModel.loadWorkoutByUri(workoutUri)
                }
                val workoutState by viewModel.workoutItem.collectAsState()
                val coroutineScope = rememberCoroutineScope()
                if (workoutState != null) {
                    AddEditWorkoutScreen(
                        workout = workoutState,
                        onSaveWorkout = { _, name, calories, duration ->
                            coroutineScope.launch {
                                viewModel.update(
                                    workoutState!!.copy(
                                        name = name,
                                        caloriesBurned = calories,
                                        duration = duration
                                    )
                                )
                                saveWorkoutLog(context)
                                navController.navigate(SolidAuthFlowScreen.WorkoutList.name)
                            }
                        },
                        onCancel = {
                            navController.navigate(SolidAuthFlowScreen.WorkoutList.name)
                        }
                    )
                }
            }


            // SCREEN: Weight Monitor
            composable(route = SolidAuthFlowScreen.WeightMonitor.name) {
                val viewModel: InputReadingsViewModel = viewModel(
                    factory = InputReadingsViewModelFactory(
                        healthConnectManager = healthConnectManager
                    )
                )
                val permissionsGranted by viewModel.permissionsGranted
                val readingsList by viewModel.weightReadingsList
                val permissions = viewModel.permissions
                val weeklyAvg by viewModel.weightWeeklyAvg
                val onPermissionsResult = { viewModel.initialLoad() }
                val permissionsLauncher =
                    rememberLauncherForActivityResult(viewModel.permissionsLauncher) {
                        onPermissionsResult()
                    }

                // Trigger `initialLoad` if the UI state is Uninitialized
                LaunchedEffect(viewModel.uiState) {
                    Log.d("WEIGHT MONITOR", "LaunchedEffect triggered with uiState: ${viewModel.uiState}")
                    if (viewModel.uiState is InputReadingsViewModel.UiState.Uninitialized) {
                        Log.d("WEIGHT MONITOR", "uiState is Uninitialized, calling initialLoad()")
                        viewModel.initialLoad()
                    }
                }

                WeightMonitor(
                    permissionsGranted = permissionsGranted,
                    permissions = permissions,

                    uiState = viewModel.uiState,
                    onInsertClick = { weightInput ->
                        viewModel.inputReadings(weightInput)
                    },
                    weeklyAvg = weeklyAvg,
                    readingsList = readingsList,
                    onError = { exception ->
                        showExceptionSnackbar(snackbarHostState, coroutineScope, exception)
                    },
                    onPermissionsResult = {
                        viewModel.initialLoad()
                    },
                    onPermissionsLaunch = { values ->
                        permissionsLauncher.launch(values)
                    },
                    onCancel = {
                        navController.popBackStack()
                    }
                )
            }

            // SCREEN: Heart Rate Monitor
            composable(route = SolidAuthFlowScreen.HeartRateMonitor.name) {
                val viewModel: InputReadingsViewModel = viewModel(
                    factory = InputReadingsViewModelFactory(
                        healthConnectManager = healthConnectManager
                    )
                )
                val permissionsGranted by viewModel.permissionsGranted
                val readingsList by viewModel.heartReadingsList
                val permissions = viewModel.permissions
                val weeklyAvg by viewModel.heartWeeklyAvg
                val onPermissionsResult = { viewModel.initialLoad() }
                val permissionsLauncher =
                    rememberLauncherForActivityResult(viewModel.permissionsLauncher) {
                        onPermissionsResult()
                    }

                // Trigger `initialLoad` if the UI state is Uninitialized
                LaunchedEffect(viewModel.uiState) {
                    if (viewModel.uiState is InputReadingsViewModel.UiState.Uninitialized) {
                        viewModel.initialLoad()
                    }
                }

                HeartRateMonitor(
                    permissionsGranted = permissionsGranted,
                    permissions = permissions,

                    uiState = viewModel.uiState,
                    onInsertClick = { weightInput ->
                        viewModel.inputReadings(weightInput)
                    },
                    weeklyAvg = weeklyAvg,
                    readingsList = readingsList,
                    onError = { exception ->
                        showExceptionSnackbar(snackbarHostState, coroutineScope, exception)
                    },
                    onPermissionsResult = {
                        viewModel.initialLoad()
                    },
                    onPermissionsLaunch = { values ->
                        permissionsLauncher.launch(values)
                    },
                    onCancel = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}

// Saves current date - used to see if user has already logged workout for the day -> wont notify again
fun saveWorkoutLog(context: Context) {
    val sharedPreferences = context.getSharedPreferences("workoutPrefs", Context.MODE_PRIVATE)
    val editor = sharedPreferences.edit()

    val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    editor.putString("lastWorkoutDate", todayDate)
    editor.apply()
}