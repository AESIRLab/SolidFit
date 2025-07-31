package com.example.workoutsolidproject.model

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.zybooks.sksolidannotations.SolidAnnotation
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@SolidAnnotation(
    "http://www.w3.org/2024/ci/core#",
    "AndroidApplication/WorkoutApp"
)
data class WorkoutItem(
    var id: String,
    var userId: String,
    var name: String = "",
    var date: Long = System.currentTimeMillis(),
    var age: String,
    var gender: String = "",
    var height: String,
    var weight: String,
//    var duration: String,
    var stepsTaken: String,
    var caloriesBurned: String,
    var hoursSlept: String,
    var waterIntake: String,
    var activeMinutes: String,
    var heartRate: String,
    var workoutType: String = "",
    var stressLevel: String,
    var mood: String = "",
    var description: String = "",
    var mediaUri: String = ""
)

@Composable
fun WorkoutItem(
    workout: WorkoutItem,
    onDelete: (WorkoutItem) -> Unit,
    onEdit: (WorkoutItem) -> Unit,
    onSelect: (WorkoutItem) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onSelect(workout) },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 12.dp, top = 16.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column (
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 16.dp)
            ){
                // NAME
                Text(
                    text = workout.name,
                    fontSize = 17.sp, fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 5.dp),
                    maxLines = 1,
                )
                // USER ID
                Text(
                    text = buildAnnotatedString {
                        // Doing this style allows for part of the text to be in the 'Medium' bold style while the data text is normal weight
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Medium)) {
                            // Medium weight
                            append("User ID: ")
                        }
                        // Normal weight
                        append(workout.userId)
                    }
                )

                // GENDER
                Text(
                    text = buildAnnotatedString {
                        // Doing this style allows for part of the text to be in the 'Medium' bold style while the data text is normal weight
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Medium)) {
                            // Medium weight
                            append("Gender: ")
                        }
                        // Normal weight
                        append(workout.gender)
                    }
                )

                // HEIGHT
                Text(
                    text = buildAnnotatedString {
                        // Doing this style allows for part of the text to be in the 'Medium' bold style while the data text is normal weight
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Medium)) {
                            // Medium weight
                            append("Height: ")
                        }
                        // Normal weight
                        append("${workout.height} cm")
                    }
                )

                // WEIGHT
                Text(
                    text = buildAnnotatedString {
                        // Doing this style allows for part of the text to be in the 'Medium' bold style while the data text is normal weight
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Medium)) {
                            // Medium weight
                            append("Weight: ")
                        }
                        // Normal weight
                        append("${workout.weight} kgs")
                    }
                )
                // STEPS TAKEN
                Text(
                    text = buildAnnotatedString {
                        // Doing this style allows for part of the text to be in the 'Medium' bold style while the data text is normal weight
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Medium)) {
                            // Medium weight
                            append("Steps Taken: ")
                        }
                        // Normal weight
                        append(workout.stepsTaken)
                    }
                )

                // CALORIES
                Text(
                    text = buildAnnotatedString {
                        // Doing this style allows for part of the text to be in the 'Medium' bold style while the data text is normal weight
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Medium)) {
                            // Medium weight
                            append("Calories: ")
                        }
                        // Normal weight
                        append(workout.caloriesBurned)
                    }
                )

                // HOURS SLEPT
                Text(
                    text = buildAnnotatedString {
                        // Doing this style allows for part of the text to be in the 'Medium' bold style while the data text is normal weight
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Medium)) {
                            // Medium weight
                            append("Hours Slept: ")
                        }
                        // Normal weight
                        append(workout.hoursSlept)
                    }
                )

                // WATER INTAKE
                Text(
                    text = buildAnnotatedString {
                        // Doing this style allows for part of the text to be in the 'Medium' bold style while the data text is normal weight
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Medium)) {
                            // Medium weight
                            append("Water Intake: ")
                        }
                        // Normal weight
                        append(workout.waterIntake)
                    }
                )

                // ACTIVE MINUTES
                Text(
                    text = buildAnnotatedString {
                        // Doing this style allows for part of the text to be in the 'Medium' bold style while the data text is normal weight
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Medium)) {
                            // Medium weight
                            append("Active Minutes: ")
                        }
                        // Normal weight
                        append(workout.activeMinutes)
                    }
                )

                // HEART RATE
                Text(
                    text = buildAnnotatedString {
                        // Doing this style allows for part of the text to be in the 'Medium' bold style while the data text is normal weight
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Medium)) {
                            // Medium weight
                            append("Heart Rate: ")
                        }
                        // Normal weight
                        append("${workout.heartRate} BPM")
                    }
                )

                // WORKOUT TYPE
                Text(
                    text = buildAnnotatedString {
                        // Doing this style allows for part of the text to be in the 'Medium' bold style while the data text is normal weight
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Medium)) {
                            // Medium weight
                            append("Workout Type: ")
                        }
                        // Normal weight
                        append(workout.workoutType)
                    }
                )

                // STRESS LEVEL
                Text(
                    text = buildAnnotatedString {
                        // Doing this style allows for part of the text to be in the 'Medium' bold style while the data text is normal weight
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Medium)) {
                            // Medium weight
                            append("Stress Level: ")
                        }
                        // Normal weight
                        append(workout.stressLevel)
                    }
                )

                // MOOD
                Text(
                    text = buildAnnotatedString {
                        // Doing this style allows for part of the text to be in the 'Medium' bold style while the data text is normal weight
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Medium)) {
                            // Medium weight
                            append("Mood: ")
                        }
                        // Normal weight
                        append(workout.mood)
                    }
                )

                // DATE
                Text(
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Medium)) {
                            // Medium weight
                            append("Date: ")
                        }
                        // Normal weight
                        append(SimpleDateFormat("MM/dd/yyyy: hh:mm a", Locale.getDefault()).format(
                            Date(workout.date)
                        ))
                    }
                )

                // DESCRIPTION
                if (workout.description.isNotBlank()) {
                    Text(
                        // Truncates the description if it's too long
                        maxLines = 3,
                        text = buildAnnotatedString {
                            // Doing this style allows for part of the text to be in the 'Medium' bold style while the data text is normal weight
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Medium)) {
                                // Medium weight
                                append("Description: ")
                            }
                            withStyle(
                                style = SpanStyle(
                                    fontSize = 16.sp,
                                    fontStyle = FontStyle.Italic,
                                    fontWeight = FontWeight.Normal,

                                    )
                            ) {
                                // Smaller, Italicized, Normal-weight font
                                append(workout.description)
                            }
                        }
                    )
                }
            }
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // THUMBNAIL
                if (workout.mediaUri.isNotBlank()) {
                    Image(
                        painter = rememberAsyncImagePainter(model = Uri.parse(workout.mediaUri)),
                        contentDescription = "Workout photo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(70.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                }
                Row(modifier = Modifier.padding(top = 6.dp)) {
                    // EDIT BUTTON
                    IconButton(onClick = { onEdit(workout) }) {
                        Icon(
                            Icons.Filled.Edit,
                            contentDescription = "Edit workout",
                            tint = Color.Black
                        )
                    }

                    // DELETE BUTTON
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
    }
}