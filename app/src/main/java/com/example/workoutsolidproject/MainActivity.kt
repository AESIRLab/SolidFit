package com.example.workoutsolidproject

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.workoutsolidproject.healthdata.HealthConnectManager
import com.example.workoutsolidproject.ui.theme.WorkoutSolidProjectTheme
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.WeightRecord
import com.example.workoutsolidproject.healthdata.BaseApplication


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d("MAIN ACTIVITY", "Before healthConnectManager initialization")

        val healthConnectManager = (application as WorkoutItemSolidApplication).healthConnectManager

        requestNotificationPermission()

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