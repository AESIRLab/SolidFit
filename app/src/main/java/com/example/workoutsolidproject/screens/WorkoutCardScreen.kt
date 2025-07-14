package com.example.workoutsolidproject.screens

import android.annotation.SuppressLint
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.example.workoutsolidproject.BottomNavItem
import com.example.workoutsolidproject.R
import com.example.workoutsolidproject.model.WorkoutItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import coil.compose.AsyncImagePainter

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutCard(
    workout: WorkoutItem,
    navController: NavHostController
) {
    val rawUri  = Uri.parse(workout.mediaUri)
    val painter = rememberAsyncImagePainter(model = rawUri)

    Column (
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp)
            .verticalScroll(rememberScrollState())
    ){
        if (workout.mediaUri.isNotBlank()) {
            Image(
                painter = painter,
                contentDescription = "Workout photo",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .padding(top = 16.dp)
                    .fillMaxWidth(0.6f)
                    .height(200.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .align(Alignment.CenterHorizontally)
            )
        }
        Text(
            modifier = Modifier.padding(top = 18.dp, bottom = 5.dp),
            text = buildAnnotatedString {
                // Doing this style allows for part of the text to be in the 'Medium' bold style while the data text is normal weight
                withStyle(style = SpanStyle(fontSize = 19.sp, fontWeight = FontWeight.Medium)) {
                    // Medium weight
                    append("Name:\t\t\t\t\t\t")
                }
                withStyle(
                    style = SpanStyle(
                        fontSize = 18.sp
                    )
                ) {
                    // Normal weight
                    append(workout.name)
                }
            }
        )
        Text(
            modifier = Modifier.padding(bottom = 5.dp),
            text = buildAnnotatedString {
                // Doing this style allows for part of the text to be in the 'Medium' bold style while the data text is normal weight
                withStyle(style = SpanStyle(fontSize = 19.sp, fontWeight = FontWeight.Medium)) {
                    // Medium weight
                    append("Calories:\t\t\t")
                }
                withStyle(
                    style = SpanStyle(
                        fontSize = 18.sp
                    )
                ) {
                    // Normal weight
                    append(workout.caloriesBurned)
                }
            }
        )
        Text(
            modifier = Modifier.padding(bottom = 5.dp),
            text = buildAnnotatedString {
                withStyle(style = SpanStyle(fontSize = 19.sp, fontWeight = FontWeight.Medium)) {
                    // Medium weight
                    append("Duration:\t\t\t")
                }
                withStyle(
                    style = SpanStyle(
                        fontSize = 18.sp
                    )
                ) {
                    // Normal weight
                    append("${workout.duration} minutes")
                }
            }
        )
        Text(
            modifier = Modifier.padding(bottom = 5.dp),
            text = buildAnnotatedString {
                withStyle(style = SpanStyle(fontSize = 19.sp, fontWeight = FontWeight.Medium)) {
                    // Medium weight
                    append("Date:\t\t\t\t\t\t\t")
                }
                withStyle(
                    style = SpanStyle(
                        fontSize = 18.sp,
                    )
                ) {
                    // Normal weight
                    append(
                        SimpleDateFormat("MM/dd/yyyy: hh:mm a", Locale.getDefault()).format(
                            Date(workout.date)
                        )
                    )
                }
            }
        )
        if (workout.description != "") {
            Text(
                modifier = Modifier.padding(bottom = 5.dp),
                text = buildAnnotatedString {
                    // Doing this style allows for part of the text to be in the 'Medium' bold style while the data text is normal weight
                    withStyle(style = SpanStyle(fontSize = 19.sp, fontWeight = FontWeight.Medium)) {
                        // Medium weight
                        append("Description:\n\t\t\t")
                    }
                    withStyle(
                        style = SpanStyle(
                            fontSize = 18.sp,
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
}