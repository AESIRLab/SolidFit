package com.example.workoutsolidproject.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.workoutsolidproject.R
import com.example.workoutsolidproject.healthdata.HeartRateBleManager
import com.example.workoutsolidproject.healthdata.InputReadingsViewModel
import java.util.UUID

@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun HeartRateMonitor(
    permissions: Set<String>,
    permissionsGranted: Boolean,
    uiState: InputReadingsViewModel.UiState,
    onInsertClick: (Double) -> Unit = {},
    onError: (Throwable?) -> Unit = {},
    onPermissionsResult: () -> Unit = {},
    onPermissionsLaunch: (Set<String>) -> Unit = {},
) {
    val context = LocalContext.current

    // Current heart rate bpm
    val currentBpm = rememberSaveable { mutableStateOf<Int?>(null) }

    // Permissions for finding and connecting to bluetooth heart rate monitor
    val scanPerm = Manifest.permission.BLUETOOTH_SCAN
    val connectPerm = Manifest.permission.BLUETOOTH_CONNECT

    // Attempts to retrieve relevant bluetooth connections
    val bluetoothLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { }

    val scanGranted by remember {
        derivedStateOf {
            ContextCompat.checkSelfPermission(context, scanPerm) == PackageManager.PERMISSION_GRANTED
        }
    }
    val connectGranted by remember {
        derivedStateOf {
            ContextCompat.checkSelfPermission(context, connectPerm) == PackageManager.PERMISSION_GRANTED
        }
    }

    LaunchedEffect(Unit) {
        if (!scanGranted || !connectGranted) {
            bluetoothLauncher.launch(arrayOf(scanPerm, connectPerm))
        }
    }

    val bleManager = remember {
        HeartRateBleManager(context) { bpm ->
            currentBpm.value = bpm
            onInsertClick(bpm.toDouble())
        }
    }

    LaunchedEffect(permissionsGranted, scanGranted, connectGranted) {
        if (permissionsGranted && scanGranted && connectGranted) {
            bleManager.startScan()
        }
    }

    DisposableEffect(Unit) {
        onDispose { bleManager.stop() }
    }

    // Remember the last error ID, such that it is possible to avoid re-launching the error
    // notification for the same error when the screen is recomposed, or configuration changes etc.
    val errorId = rememberSaveable { mutableStateOf(UUID.randomUUID()) }

    LaunchedEffect(uiState) {
        // If the initial data load has not taken place, attempt to load the data.
        if (uiState is InputReadingsViewModel.UiState.Uninitialized) {
            onPermissionsResult()
        }
        // States whether action is successful or not
        else if (uiState is InputReadingsViewModel.UiState.Error && errorId.value != uiState.uuid) {
            onError(uiState.exception)
            errorId.value = uiState.uuid
        }
    }

    LaunchedEffect(permissionsGranted) {
        if (permissionsGranted) {
            bleManager.startScan()
        }
    }

    // Stop scanning when leaving screen
    DisposableEffect(Unit) {
        onDispose { bleManager.stop() }
    }

    if (uiState != InputReadingsViewModel.UiState.Uninitialized) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (!permissionsGranted) {
                item {
                    Button(
                        onClick = { onPermissionsLaunch(permissions) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.hsl(224f, 1f,0.73f)
                        )
                    ) {
                        Text(text = stringResource(R.string.permissions_button_label))
                    }
                }
            }
            else {
                // Current BPM
                item {
                    if (currentBpm.value == null) {
                        Text(
                            text = "Searching...",
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color.Black,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    }
                    else {
                        Text(
                            text = stringResource(id = R.string.current_bpm),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            modifier = Modifier.padding(top = 20.dp, bottom = 10.dp),
                        )
                        Text(
                            text = currentBpm.value.let { "$it BPM" },
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    }
                }
            }
        }
    }

}




