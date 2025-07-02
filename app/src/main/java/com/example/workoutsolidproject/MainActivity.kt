package com.example.workoutsolidproject

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.workoutsolidproject.ui.theme.WorkoutSolidProjectTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d("MAIN ACTIVITY", "Before healthConnectManager initialization")

        // Used to connect health connect object throughout the app
        val healthConnectManager = (application as WorkoutItemSolidApplication).healthConnectManager

        // Requests notification permissions (shocker)
        requestNotificationPermission()

        // Allows content to display behind device's status and navigation bar
        enableEdgeToEdge()
        setContent {
            WorkoutSolidProjectTheme {
                Log.d("MAIN ACTIVITY", "Calling WorkoutApp(..)")
                WorkoutApp(healthConnectManager = healthConnectManager)
            }
        }
    }

    private fun requestNotificationPermission() {
        // Check and request POST_NOTIFICATIONS permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 100)
            }
        }
    }
}