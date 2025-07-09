package com.example.workoutsolidproject

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
import com.example.workoutsolidproject.healthdata.HealthConnectManager
import com.example.workoutsolidproject.screens.AuthCompleteScreen
import com.example.workoutsolidproject.screens.StartAuthScreen
import com.example.workoutsolidproject.screens.UnfetchableWebIdScreen
import com.example.workoutsolidproject.screens.UpdateWorkouts
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.skCompiler.generatedModel.AuthTokenStore


// All apps screens
enum class SolidAuthFlowScreen {
    AddEditWorkoutScreen,
    WorkoutList,
    UnfetchableWebIdScreen,
    AuthCompleteScreen,
    StartAuthScreen,
    HeartRateMonitor,
    WeightMonitor
}


// Used for navbar
sealed class BottomNavItem(val route: String, val title: String, val icon: ImageVector) {
    data object WorkoutList : BottomNavItem(
        route = SolidAuthFlowScreen.WorkoutList.name,
        title = SolidAuthFlowScreen.WorkoutList.name,
        icon = Icons.AutoMirrored.Filled.List
    )
    data object HeartMonitor : BottomNavItem(
        route = SolidAuthFlowScreen.HeartRateMonitor.name,
        title = SolidAuthFlowScreen.HeartRateMonitor.name,
        icon = Icons.Default.Favorite
    )
    data object WeightMonitor: BottomNavItem(
        route = SolidAuthFlowScreen.WeightMonitor.name,
        title = SolidAuthFlowScreen.WeightMonitor.name,
        icon = Icons.Default.Person
    )
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter", "NewApi")
@Composable
fun WorkoutApp(
    healthConnectManager: HealthConnectManager,
) {
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
                deepLinks = listOf(navDeepLink { uriPattern = "app://www.solid-oidc.com/callback"})
            ) {
                AuthCompleteScreen(tokenStore = tokenStore) {
                    UpdateWorkouts(healthConnectManager = healthConnectManager)
                }
            }
        }
    }
}

