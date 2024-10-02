package com.example.workoutsolidproject

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.workoutsolidproject.ui.theme.WorkoutSolidProjectTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WorkoutSolidProjectTheme {
                WorkoutApp()
            }
        }
    }
}