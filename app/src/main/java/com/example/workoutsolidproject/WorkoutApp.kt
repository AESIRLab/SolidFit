package com.example.workoutsolidproject

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.compose.runtime.getValue
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.compose.composable
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import com.example.solid_annotation.SolidAuthAnnotation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.example.workoutsolidproject.model.WorkoutItem
import com.solidannotations.AuthTokenStore
import kotlinx.coroutines.launch

// All apps screens
enum class SolidAuthFlowScreen {
    AddEditWorkoutScreen,
    WorkoutList,
    UnfetchableWebIdScreen,
    AuthCompleteScreen,
    StartAuthScreen
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
@SolidAuthAnnotation("WorkoutApplication")
fun WorkoutApp() {
    val navController = rememberNavController()
    val applicationCtx = LocalContext.current.applicationContext
    val repository = (LocalContext.current.applicationContext as WorkoutItemSolidApplication).repository
    val viewModel = WorkoutItemViewModel(repository)
    val tokenStore = AuthTokenStore(LocalContext.current.applicationContext)
    val coroutineScope = rememberCoroutineScope()

    Scaffold {
        val context = LocalContext.current
        NavHost(
            navController = navController,
            startDestination = SolidAuthFlowScreen.StartAuthScreen.name,
//            modifier = Modifier.padding(top = innerPadding.calculateTopPadding())
        ) {

            // Authentication screen (Starting screen)
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

            // Screen if the web id is unfetchable
            composable(route = SolidAuthFlowScreen.UnfetchableWebIdScreen.name) {
                UnfetchableWebIdScreen(tokenStore = tokenStore) { err ->
                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                    }
                }
            }

            // Authentication complete screen
            composable(
                route = SolidAuthFlowScreen.AuthCompleteScreen.name,
                deepLinks = listOf(navDeepLink { uriPattern = "app://www.solid-oidc.com/callback" })) {

                AuthCompleteScreen(tokenStore = tokenStore) {
                    navController.navigate(SolidAuthFlowScreen.WorkoutList.name)
                }
            }

            // Add/edit workout screen
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
                                    date = System.currentTimeMillis()
                                )
                            )

                            navController.navigate("WorkoutList")
                        }
                    },
                    onCancel = {
                        navController.popBackStack()
                    }
                )
            }
            // Screen that displays the list of workouts
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
                                        "Workout Trainer",
                                        fontSize = 26.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Image(
                                        painter = painterResource(id = R.drawable.exercise_24px),
                                        contentDescription = "App logo"
                                    )
                                }
                            }
                        )
                    },
                    floatingActionButton = {
                        FloatingActionButton(
                            containerColor = Color.hsl(224f, 1f,0.73f),
                            onClick = { navController.navigate(route = SolidAuthFlowScreen.AddEditWorkoutScreen.name) }
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = "Add workout")
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = innerPadding.calculateTopPadding(), bottom = innerPadding.calculateBottomPadding())
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


