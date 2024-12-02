package com.example.workoutsolidproject

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.example.solid_annotation.SolidAuthAnnotation
import com.example.workoutsolidproject.healthdata.HealthConnectManager
import com.example.workoutsolidproject.healthdata.InputReadingsViewModel
import com.example.workoutsolidproject.healthdata.InputReadingsViewModelFactory
import com.example.workoutsolidproject.healthdata.showExceptionSnackbar
import com.example.workoutsolidproject.model.WorkoutItem
import com.example.workoutsolidproject.screens.AddEditWorkoutScreen
import com.example.workoutsolidproject.screens.AuthCompleteScreen
import com.example.workoutsolidproject.screens.HeartRateMonitor
import com.example.workoutsolidproject.screens.StartAuthScreen
import com.example.workoutsolidproject.screens.UnfetchableWebIdScreen
import com.solidannotations.AuthTokenStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// All apps screens
enum class SolidAuthFlowScreen {
    AddEditWorkoutScreen,
    WorkoutList,
    UnfetchableWebIdScreen,
    AuthCompleteScreen,
    StartAuthScreen,
    HeartRateMonitor
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
@SolidAuthAnnotation("WorkoutApplication")
fun WorkoutApp(
    healthConnectManager: HealthConnectManager,
) {
    Log.d("WORKOUTAPP", "Running WorkoutApp")
    val navController = rememberNavController()
    val applicationCtx = LocalContext.current.applicationContext
    val repository = (LocalContext.current.applicationContext as WorkoutItemSolidApplication).repository
    val viewModel = WorkoutItemViewModel(repository)
    val tokenStore = AuthTokenStore(LocalContext.current.applicationContext)
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }



    Scaffold {
        val context = LocalContext.current
        NavHost(
            navController = navController,
            startDestination = SolidAuthFlowScreen.StartAuthScreen.name,
        ) {

            Log.d("WORKOUTAPP", "Call Starting Screen")

            // SCREEN: Authentication (Starting screen)
            composable(route = SolidAuthFlowScreen.StartAuthScreen.name) {
                StartAuthScreen(
                    tokenStore = tokenStore,
                    onFailNavigation = {
                        coroutineScope.launch {
                            withContext(Dispatchers.Main) {
                                navController.navigate(SolidAuthFlowScreen.UnfetchableWebIdScreen.name)
                            }
                        }
                    },
                    onInvalidInput = { msg ->
                        Handler(Looper.getMainLooper()).post {
                            Toast.makeText(context, msg.toString(), Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }

            // SCREEN: The web id is unfetchable
            composable(route = SolidAuthFlowScreen.UnfetchableWebIdScreen.name) {
                UnfetchableWebIdScreen(tokenStore = tokenStore) { err ->
                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                    }
                }
            }

            // SCREEN: Authentication complete
            composable(
                route = SolidAuthFlowScreen.AuthCompleteScreen.name,
                deepLinks = listOf(navDeepLink { uriPattern = "app://www.solid-oidc.com/callback" })
            ) {

                AuthCompleteScreen(tokenStore = tokenStore) {
                    navController.navigate(SolidAuthFlowScreen.WorkoutList.name)
                }
            }

            // SCREEN: Add workout
            composable(route = SolidAuthFlowScreen.AddEditWorkoutScreen.name) {
                val coroutineScope = rememberCoroutineScope()
                AddEditWorkoutScreen(
                    onSaveWorkout = { _, name, calories, duration ->
                        coroutineScope.launch {
                            repository.insert(
                                WorkoutItem(
                                    id = "",
                                    name = name,
                                    caloriesBurned = calories,
                                    duration = duration,
                                )
                            )

                            saveWorkoutLog(context)

                            navController.navigate("WorkoutList")
                        }
                    },
                    onCancel = {
                        navController.navigate("WorkoutList")
                    }
                )
            }

            // SCREEN: displays the list of workouts
            composable(route = SolidAuthFlowScreen.WorkoutList.name) {
                val workouts by repository.allWorkoutItems.collectAsState(initial = emptyList())

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
                    floatingActionButton = {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 32.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            FloatingActionButton(
                                containerColor = Color.hsl(224f, 1f, 0.73f),
                                onClick = { navController.navigate(route = SolidAuthFlowScreen.HeartRateMonitor.name) },
                                shape = CircleShape,
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.vital_signs_24dp),
                                    contentDescription = "Heart Rate Monitor"
                                )
                            }
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
                                Log.d("Delete workout ID", workout.id)
                                coroutineScope.launch {
                                    repository.deleteByUri(workout.id)
                                }
                            },
                            onEditWorkout = { workout ->
                                Log.d("Edit workout ID", workout.id)
                                navController.navigate(route = "${SolidAuthFlowScreen.AddEditWorkoutScreen.name}/${workout.id}")
                            }
                        )
                    }
                }
            }

            // SCREEN: Heart Rate Monitor
            composable(route = SolidAuthFlowScreen.HeartRateMonitor.name) {
                val viewModel: InputReadingsViewModel = viewModel(
                    factory = InputReadingsViewModelFactory(
                        healthConnectManager = healthConnectManager
                    )
                )
                val permissionsGranted by viewModel.permissionsGranted
                val readingsList by viewModel.readingsList
                val permissions = viewModel.permissions
                val weeklyAvg by viewModel.weeklyAvg
                val onPermissionsResult = { viewModel.initialLoad() }
                val permissionsLauncher =
                    rememberLauncherForActivityResult(viewModel.permissionsLauncher) {
                        onPermissionsResult()
                    }

                // Trigger `initialLoad` if the UI state is Uninitialized
                LaunchedEffect(viewModel.uiState) {
                    Log.d("HEART RATE MONITOR", "LaunchedEffect triggered with uiState: ${viewModel.uiState}")
                    if (viewModel.uiState is InputReadingsViewModel.UiState.Uninitialized) {
                        Log.d("HEART RATE MONITOR", "uiState is Uninitialized, calling initialLoad()")
                        viewModel.initialLoad()
                    }
                }

                HeartRateMonitor(
                    permissionsGranted = permissionsGranted,
                    permissions = permissions,

                    uiState = viewModel.uiState,
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
                        navController.navigate("WorkoutList")
                    }
                )
            }



            // SCREEN: Edit workout
            composable(
                route = "${SolidAuthFlowScreen.AddEditWorkoutScreen.name}/{workoutUri}",
                arguments = listOf(navArgument("workoutUri") { type = NavType.StringType })
            ) { backStackEntry ->
                val workoutUri = backStackEntry.arguments?.getString("workoutUri") ?: return@composable
                val workoutState = repository.getWorkoutItemLiveData(workoutUri).collectAsState(initial = null)
                val coroutineScope = rememberCoroutineScope()
                if (workoutState.value != null) {
                    AddEditWorkoutScreen(
                        workout = workoutState.value,
                        onSaveWorkout = {id, name, calories, duration ->
                            coroutineScope.launch {
                                repository.update(
                                    workoutState.value!!.copy(
                                        id = id,
                                        name = name,
                                        caloriesBurned = calories,
                                        duration = duration,
                                        date = System.currentTimeMillis()
                                    ), workoutUri
                                )
                            }

                            navController.navigate("WorkoutList")
                        },
                        onCancel = {
                            navController.navigate("WorkoutList")
                        }
                    )
                }
            }
        }
    }
}

fun saveWorkoutLog(context: Context) {
    val sharedPreferences = context.getSharedPreferences("workoutPrefs", Context.MODE_PRIVATE)
    val editor = sharedPreferences.edit()

    // Saves current date - used to see if user has already logged workout for the day -> wont notify again
    val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    editor.putString("lastWorkoutDate", todayDate)
    editor.apply()
}

