package com.example.workoutsolidproject.model

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.solid_annotation.SolidAnnotation
import com.example.workoutsolidproject.SolidAuthFlowScreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@SolidAnnotation(
    "WorkoutItem",
    "AndroidApplication/WorkoutApp",
    "http://www.w3.org/2024/ci/core#"
)
data class WorkoutItem(
    var id: String,
    var name: String = "",
    var caloriesBurned: String,
    var duration: String,
    var date: Long = System.currentTimeMillis()
)

@Composable
fun WorkoutItem(
    workout: WorkoutItem,
    onDelete: (WorkoutItem) -> Unit,
    onEdit: (WorkoutItem) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column (
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 16.dp)
            ){
                Text(text = workout.name, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 5.dp))
                Text(text = "Calories: ${workout.caloriesBurned}")
                Text(text = "Duration: ${workout.duration} minutes")
                Text(text = "Date: " +
                        SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(
                    Date(workout.date)
                )
                )
            }

            IconButton(onClick = { onEdit(workout) }) {
                Icon(
                    Icons.Filled.Edit,
                    contentDescription = "Edit workout",
                    tint = Color.Black
                )
            }
            IconButton(onClick = { onDelete(workout) }) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = "Delete workout",
                    tint = Color.Black
                )
            }
        }
    }
}