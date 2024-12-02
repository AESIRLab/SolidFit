package com.example.workoutsolidproject.screens

import android.annotation.SuppressLint
import android.util.Log
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.workoutsolidproject.R
import com.example.workoutsolidproject.healthdata.InputReadingsViewModel
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun HeartRateMonitor(
    permissions: Set<String>,
    permissionsGranted: Boolean,
    uiState: InputReadingsViewModel.UiState,
    onError: (Throwable?) -> Unit = {},
    onPermissionsResult: () -> Unit = {},
    onPermissionsLaunch: (Set<String>) -> Unit = {},
    onCancel: () -> Unit
) {

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
                        Log.d("HEART RATE MONITOR", "Makes it past initialization")

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
                    onClick = onCancel,
                    shape = CircleShape,
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.exercise_black_24dp),
                        contentDescription = "Heart Rate Monitor",
                        modifier = Modifier.size(30.dp)
                    )
                }
            }
        }
    ) { innerPadding ->
        if (uiState != InputReadingsViewModel.UiState.Uninitialized) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (!permissionsGranted) {
                    item {
                        Button(
                            onClick = { onPermissionsLaunch(permissions) },
                            modifier = Modifier.padding(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.hsl(224f, 1f,0.73f))
                        ) {
                            Log.d("Button Heart", "BUTTON!")
                            Text(text = stringResource(R.string.permissions_button_label))
                        }
                    }
                }
                else {
                    item {
                        Text(text = "Permissions granted")
                    }
                }
            }
        }
    }
}


