package com.example.workoutsolidproject.screens

import androidx.compose.foundation.Image
import androidx.compose.material3.Button
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.workoutsolidproject.R
import com.example.workoutsolidproject.model.WorkoutItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditWorkoutScreen(
    workout: WorkoutItem? = null,
    onSaveWorkout: (String, String, String, String) -> Unit,
    onCancel: () -> Unit
) {
    var id by remember { mutableStateOf(workout?.id ?: "") }
    var name by remember { mutableStateOf(workout?.name ?: "") }
    var caloriesBurned by remember { mutableStateOf(workout?.caloriesBurned ?: "") }
    var duration by remember { mutableStateOf(workout?.duration  ?: "") }

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
                        "Add/Edit Workout",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Image(
                        painter = painterResource(id = R.drawable.exercise_white_34dp),
                        contentDescription = "App logo",
                        )
                }
            }
        )
    }
    )
    { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Name field
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Workout Name") },
                modifier = Modifier.fillMaxWidth()
            )
            // Calories field
            OutlinedTextField(
                value = caloriesBurned,
                onValueChange = { caloriesBurned = it },
                label = { Text("Calories Burned") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            // Duration field
            OutlinedTextField(
                value = duration,
                onValueChange = { duration = it },
                label = { Text("Duration (minutes)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Cancel add/edit workout
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = Color.hsl(
                        224f,
                        1f,
                        0.73f)),
                    onClick = onCancel,

                ) {
                    Text("Cancel")
                }
                // Save workout
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = Color.hsl(
                        224f,
                        1f,
                        0.73f)),
                    onClick = {
                        if (name.isNotBlank() && caloriesBurned.isNotBlank() && duration.isNotBlank()) {
                            onSaveWorkout(id, name, caloriesBurned, duration)
                        }
                    },
                    enabled = name.isNotBlank() && caloriesBurned.isNotBlank() && duration.isNotBlank()
                ) {
                    Text("Save")
                }
            }
        }
    }
}
