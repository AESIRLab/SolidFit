package com.example.workoutsolidproject.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.workoutsolidproject.model.WorkoutItem


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditWorkoutScreen(
    workout: WorkoutItem? = null,
    onSaveWorkout: (String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String) -> Unit,
    onCancel: () -> Unit
) {
    var id by remember { mutableStateOf(workout?.id ?: "") }
    var userId by remember { mutableStateOf(workout?.userId ?: "") }
    var name by remember { mutableStateOf(workout?.name ?: "") }
    var age by remember { mutableStateOf(workout?.age ?: "") }
    var gender by remember { mutableStateOf(workout?.gender ?: "") }
    var height by remember { mutableStateOf(workout?.height ?: "") }
    var weight by remember { mutableStateOf(workout?.weight ?: "") }
    var stepsTaken by remember { mutableStateOf(workout?.stepsTaken ?: "") }
    var caloriesBurned by remember { mutableStateOf(workout?.caloriesBurned ?: "") }
    var hoursSlept by remember { mutableStateOf(workout?.hoursSlept ?: "") }
    var waterIntake by remember { mutableStateOf(workout?.waterIntake ?: "") }
    var activeMinutes by remember { mutableStateOf(workout?.activeMinutes ?: "") }
    var heartRate by remember { mutableStateOf(workout?.heartRate ?: "") }
    var workoutType by remember { mutableStateOf(workout?.workoutType ?: "") }
    var stressLevel by remember { mutableStateOf(workout?.stressLevel ?: "") }
    var mood by remember { mutableStateOf(workout?.mood ?: "") }
    var description by remember {mutableStateOf(workout?.description ?: "")}
    var mediaUri by remember(workout?.mediaUri) { mutableStateOf(workout?.mediaUri?.let(Uri::parse)?: "")}

    val context = LocalContext.current
    // Used to display image
    val mediaLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            context.contentResolver.takePersistableUriPermission(
                it,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            mediaUri = it
        }
    }
        Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 16.dp, end = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),

    ) {
        // User ID field
        OutlinedTextField(
            value = userId,
            onValueChange = { userId = it },
            label = { Text("User ID") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        // Name field
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Workout Name") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp)
        )
        // Age field
        OutlinedTextField(
            value = age,
            onValueChange = { age = it },
            label = { Text("Age") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        // Gender field
        OutlinedTextField(
            value = gender,
            onValueChange = { gender = it },
            label = { Text("Gender") },
            modifier = Modifier.fillMaxWidth()
        )
        // Height field
        OutlinedTextField(
            value = height,
            onValueChange = { height = it },
            label = { Text("Height (cm)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        // Weight field
        OutlinedTextField(
            value = weight,
            onValueChange = { weight = it },
            label = { Text("Weight (kgs)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        // Steps taken field
        OutlinedTextField(
            value = stepsTaken,
            onValueChange = { stepsTaken = it },
            label = { Text("Steps Taken") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        // Calories Burned field
        OutlinedTextField(
            value = caloriesBurned,
            onValueChange = { caloriesBurned = it },
            label = { Text("Calories Burned") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        // Hours Slept field
        OutlinedTextField(
            value = hoursSlept,
            onValueChange = { hoursSlept = it },
            label = { Text("Hours Slept") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        // Water Intake field
        OutlinedTextField(
            value = waterIntake,
            onValueChange = { waterIntake = it },
            label = { Text("Water Intake (Liters)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        // Active Minutes field
        OutlinedTextField(
            value = activeMinutes,
            onValueChange = { activeMinutes = it },
            label = { Text("Active Minutes") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        // Heart Rate field
        OutlinedTextField(
            value = heartRate,
            onValueChange = { heartRate = it },
            label = { Text("Heart Rate (BPM)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        // Workout Type field
        OutlinedTextField(
            value = workoutType,
            onValueChange = { workoutType = it },
            label = { Text("Workout Type") },
            modifier = Modifier.fillMaxWidth()
        )
        // Stress Level field
        OutlinedTextField(
            value = stressLevel,
            onValueChange = { stressLevel = it },
            label = { Text("Stress Level (1-10)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        // Mood field
        OutlinedTextField(
            value = mood,
            onValueChange = { mood = it },
            label = { Text("Mood (Happy, Sad, Stress, or Neutral)") },
            modifier = Modifier.fillMaxWidth()
        )
//        // Description field
//        OutlinedTextField(
//            value = description,
//            onValueChange = { description = it },
//            label = { Text("Description") },
//            modifier = Modifier.fillMaxWidth()
//        )
//        mediaUri?.let { uri ->
//            Image(
//                painter = rememberAsyncImagePainter(model = uri),
//                contentDescription = null,
//                contentScale = ContentScale.Crop,
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(400.dp)
//                    .clip(RoundedCornerShape(25.dp))
//                    .border(1.5.dp, Color.Gray, RoundedCornerShape(25.dp))
//                    .align(Alignment.CenterHorizontally)
//            )
//        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Cancel add/edit workout
            Button(
                colors = ButtonDefaults.buttonColors(containerColor = Color.hsl(
                    224f,
                    1f,
                    0.73f)),
                onClick = onCancel
            ) {
                Text("Cancel")
            }
            // Add/Change photo
            Button(
                colors = ButtonDefaults.buttonColors(containerColor = Color.hsl(
                    224f,
                    1f,
                    0.73f)),
                onClick = { mediaLauncher.launch(arrayOf("image/*")) }
            ) {
                Text(if (mediaUri.toString() == "") "Select Photo" else "Change Photo")
            }
            // Save workout
            Button(
                colors = ButtonDefaults.buttonColors(containerColor = Color.hsl(
                    224f,
                    1f,
                    0.73f)),
                onClick = {
                    // TODO: UNDO COMMENT
//                    if (name.isNotBlank() && caloriesBurned.isNotBlank() && activeMinutes.isNotBlank()) {
                    if (name.isNotBlank()) {

                        onSaveWorkout(id, userId, name, age, gender, height, weight, stepsTaken, caloriesBurned, hoursSlept, waterIntake, activeMinutes, heartRate, workoutType, stressLevel, mood, description, mediaUri?.toString().orEmpty())
                    }
                },
                // TODO: UNDO COMMENT
//                enabled = name.isNotBlank() && caloriesBurned.isNotBlank() && activeMinutes.isNotBlank()
                enabled = name.isNotBlank()

            ) {
                Text("Save")
            }
        }
    }
}
