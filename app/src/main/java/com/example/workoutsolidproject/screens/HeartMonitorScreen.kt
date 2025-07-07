package com.example.workoutsolidproject.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.health.connect.client.records.HeartRateRecord
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.workoutsolidproject.BottomNavItem
import com.example.workoutsolidproject.R
import com.example.workoutsolidproject.SolidAuthFlowScreen
import com.example.workoutsolidproject.healthdata.InputReadingsViewModel
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun HeartRateMonitor(
    navController: NavHostController,
    permissions: Set<String>,
    permissionsGranted: Boolean,
    readingsList: List<HeartRateRecord>,
    uiState: InputReadingsViewModel.UiState,
    onInsertClick: (Double) -> Unit = {},
    onError: (Throwable?) -> Unit = {},
    onPermissionsResult: () -> Unit = {},
    // TODO: Likely need to change datatype of weeklyAvg
    weeklyAvg: Double?,
    onPermissionsLaunch: (Set<String>) -> Unit = {},
) {

    val navBarItems = listOf(
        BottomNavItem.WorkoutList,
        BottomNavItem.HeartMonitor,
        BottomNavItem.WeightMonitor,
    )

    // Remember the last error ID, such that it is possible to avoid re-launching the error
    // notification for the same error when the screen is recomposed, or configuration changes etc.
    val errorId = rememberSaveable { mutableStateOf(UUID.randomUUID()) }

    LaunchedEffect(uiState) {
        // If the initial data load has not taken place, attempt to load the data.
        if (uiState is InputReadingsViewModel.UiState.Uninitialized) {
            onPermissionsResult()
        }

        // The [InputReadingsScreenViewModel.UiState] provides details of whether the last action
        // was a success or resulted in an error. Where an error occurred, for example in reading
        // and writing to Health Connect, the user is notified, and where the error is one that can
        // be recovered from, an attempt to do so is made.
        if (uiState is InputReadingsViewModel.UiState.Error && errorId.value != uiState.uuid) {
            onError(uiState.exception)
            errorId.value = uiState.uuid
        }
    }

    var heartInput by remember { mutableStateOf("") }

// TODO: Implement auto reading of heart rate from sensor

    Scaffold(
        // Bar at the top of the screen
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
                            "Heart Rate Monitor",
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
                        selected = currentDestination
                            ?.hierarchy
                            ?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }

    ) { innerPadding ->
        if (uiState != InputReadingsViewModel.UiState.Uninitialized) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(20.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (!permissionsGranted) {
                    item {
                        Button(
                            onClick = { onPermissionsLaunch(permissions) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.hsl(224f, 1f,0.73f))
                        ) {
                            Text(text = stringResource(R.string.permissions_button_label))
                        }
                    }
                }
                else {
//                    items(readingsList) { reading ->
//                        Row(
//                            horizontalArrangement = Arrangement.SpaceEvenly,
//                            verticalAlignment = Alignment.CenterVertically
//                        ) {
//                            // show local date and time
//                            val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
//                            val zonedDateTime =
//                                dateTimeWithOffsetOrDefault(reading.time,  reading.zoneOffset)
//                            Text(
//                                text = "${"%.1f".format(reading.weight.inPounds)} lbs" + " - ",
//                                fontWeight = FontWeight.Medium,
//                            )
//                            Text(text = formatter.format(zonedDateTime))
//                        }
//                    }
                    item {
                        Text(
                            text = stringResource(id = R.string.weekly_avg), fontSize = 24.sp,
                            color = Color.Black,
                            modifier = Modifier.padding(top = 20.dp, bottom = 10.dp),
                        )
                        if (weeklyAvg == null || weeklyAvg == 0.0) {
                            Text(text = "0.0 BPM")
                        } else {
                            Text(text = "%.1f BPM".format(weeklyAvg))
                        }
                    }
                }
            }
        }
    }
}


