package com.example.workoutsolidproject.model

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zybooks.sksolidannotations.SolidAnnotation
//import com.example.solid_annotation.SolidAnnotation
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@SolidAnnotation(
        "https://solidev.me/AndroidApplication/WorkoutApp",
    "http://www.w3.org/2024/ci/core#"
)
data class WorkoutItem(
    var id: String,
    var name: String = "",
    var caloriesBurned: String,
    var duration: String,
    var date: Long = System.currentTimeMillis(),
    var description: String = ""
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
                .padding(start = 16.dp, end = 12.dp, top = 16.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column (
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 16.dp)
            ){
                Text(text = workout.name, fontSize = 17.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 5.dp))
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
                Text(
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Medium)) {
                            // Medium weight
                            append("Duration: ")
                        }
                        // Normal weight
                        append("${workout.duration} minutes")
                    }
                )
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
                if (workout.description != "") {
                    Text(
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
                                    fontWeight = FontWeight.Normal
                                )
                            ) {
                                // Smaller, Italicized, Normal-weight font
                                append(workout.description)
                            }
                        }
                    )
                }
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