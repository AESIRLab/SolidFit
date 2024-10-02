package com.example.workoutsolidproject

import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import com.example.workoutsolidproject.model.WorkoutItem

@Composable
fun WorkoutList(
    workouts: List<WorkoutItem>,
    onDeleteWorkout: (WorkoutItem) -> Unit,
    onEditWorkout: (WorkoutItem) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        items(workouts) { workout ->
            WorkoutItem(
                workout = workout,
                onDelete = onDeleteWorkout,
                onEdit = onEditWorkout
            )
        }
    }
}