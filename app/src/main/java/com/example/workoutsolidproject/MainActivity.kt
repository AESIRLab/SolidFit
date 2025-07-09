package com.example.workoutsolidproject

import android.Manifest.permission.BLUETOOTH_CONNECT
import android.Manifest.permission.BLUETOOTH_SCAN
import android.Manifest.permission.POST_NOTIFICATIONS
import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.READ_MEDIA_IMAGES
import android.Manifest.permission.READ_MEDIA_VIDEO
import android.Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import com.example.workoutsolidproject.ui.theme.WorkoutSolidProjectTheme

class MainActivity : ComponentActivity() {
    private lateinit var notifcationLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var bluetoothLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var mediaLauncher: ActivityResultLauncher<Array<String>>

    @SuppressLint("InlinedApi")
    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d("MAIN ACTIVITY", "Before healthConnectManager initialization")

        // Start of permissions request chain definition.
        // Notification -> Bluetooth
        notifcationLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { results ->
            bluetoothLauncher.launch(arrayOf(
                BLUETOOTH_SCAN,
                BLUETOOTH_CONNECT
            ))
        }

        // Permissions request chain:
        // Bluetooth -> Media
        bluetoothLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { results ->
            val mediaPerms = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                arrayOf(
                    READ_MEDIA_IMAGES,
                    READ_MEDIA_VIDEO,
                    READ_MEDIA_VISUAL_USER_SELECTED
                )
            } else arrayOf(READ_EXTERNAL_STORAGE)
            mediaLauncher.launch(mediaPerms)
        }

        // Permissions request chain:
        // Media
        mediaLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) {}

        // Launches permissions request chain
        notifcationLauncher.launch(arrayOf(POST_NOTIFICATIONS))

        // Used to connect health connect object throughout the app
        val healthConnectManager = (application as WorkoutItemSolidApplication).healthConnectManager

        // Allows content to display behind device's status and navigation bar
        enableEdgeToEdge()
        setContent {
            WorkoutSolidProjectTheme {
                Log.d("MAIN ACTIVITY", "Calling WorkoutApp(..)")
                WorkoutApp(healthConnectManager = healthConnectManager)
            }
        }
    }
}