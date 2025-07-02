package com.example.workoutsolidproject

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
import com.example.workoutsolidproject.healthdata.HealthConnectManager
import com.example.workoutsolidproject.healthdata.InputReadingsViewModel
import com.example.workoutsolidproject.healthdata.InputReadingsViewModelFactory
import com.example.workoutsolidproject.healthdata.showExceptionSnackbar
import com.example.workoutsolidproject.screens.AuthCompleteScreen
import com.example.workoutsolidproject.screens.StartAuthScreen
import com.example.workoutsolidproject.screens.UnfetchableWebIdScreen
import com.example.workoutsolidproject.screens.UpdateWorkouts
import com.example.workoutsolidproject.screens.WeightMonitor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.skCompiler.generatedModel.AuthTokenStore
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
    WeightMonitor
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun WorkoutApp(
    healthConnectManager: HealthConnectManager,
) {
    Log.d("WORKOUTAPP", "Running WorkoutApp")

    val navController = rememberNavController()
    val tokenStore = AuthTokenStore(LocalContext.current.applicationContext)
    val coroutineScope = rememberCoroutineScope()

    Scaffold {
        val context = LocalContext.current
        NavHost(
            navController = navController,
            startDestination = SolidAuthFlowScreen.StartAuthScreen.name,
        ) {

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

            // SCREEN: UnfetchableWebID
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
                    UpdateWorkouts(healthConnectManager = healthConnectManager)
                }
            }
        }
    }
}

