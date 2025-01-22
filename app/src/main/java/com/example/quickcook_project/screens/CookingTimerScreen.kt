package com.example.quickcook_project.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import android.content.Intent
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.zIndex
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay

@Composable
fun CookingTimerScreen(
    navController: NavController,
    recipeName: String,
    preparationTime: String,
    steps: List<String>
) {
    val firestore = FirebaseFirestore.getInstance()

    // timer variables
    var timerRunning by remember { mutableStateOf(false) }
    var timeInMillis by remember { mutableStateOf(0L) }
    var username by remember { mutableStateOf("User") }
    var profileImageUrl by remember { mutableStateOf("") }

    // continue button
    var showContinueButton by remember { mutableStateOf(false) }

    // function to format the timer
    fun formatTime(timeInMillis: Long): String {
        val hours = timeInMillis / 3600000
        val minutes = (timeInMillis % 3600000) / 60000
        val seconds = (timeInMillis % 60000) / 1000
        val milliseconds = timeInMillis % 1000
        return String.format("%02d:%02d:%02d:%03d", hours, minutes, seconds, milliseconds)
    }

    LaunchedEffect(timerRunning) {
        while (timerRunning) {
            delay(10L)
            timeInMillis += 10L
        }
    }

    // principal structure
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE2D9D9))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // header
            HeaderForIngredientsScreen(username, profileImageUrl, navController, {
                navController.navigate("profile")
            })

            Spacer(modifier = Modifier.height(16.dp))

            // recipe name
            Text(
                text = "Cooking: $recipeName",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4A4A4A)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // timer display
            Text(
                text = " ${formatTime(timeInMillis)} ",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF737373)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // buttons (finish, timer, share)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Finish Cooking button
                FloatingActionButton(
                    onClick = {
                        firestore.collection("recipes").whereEqualTo("recipeName", recipeName)
                            .get()
                            .addOnSuccessListener { result ->
                                val ingredients = result.documents.firstOrNull()?.let { doc ->
                                    (1..4).mapNotNull { doc.getString("ingredient$it") }
                                } ?: emptyList()

                                firestore.collection("completed_recipes").add(
                                    mapOf(
                                        "recipeName" to recipeName,
                                        "timeTaken" to formatTime(timeInMillis),
                                        "ingredients" to ingredients,
                                        "steps" to steps
                                    )
                                ).addOnSuccessListener {
                                    // send local notification
                                    val notificationData = mapOf(
                                        "title" to "Recipe Completed!",
                                        "message" to "You finished $recipeName.",
                                        "timestamp" to System.currentTimeMillis(),
                                        "isRead" to false
                                    )

                                    val userId = FirebaseAuth.getInstance().currentUser?.uid
                                    if (userId != null) {
                                        firestore.collection("users").document(userId)
                                            .collection("notifications").add(notificationData)
                                    }

                                    navController.popBackStack()
                                }
                            }
                    },
                    backgroundColor = Color(0xFF7F3C3C),
                    shape = RoundedCornerShape(23.dp)
                ) {
                    Text(text = "Finish", color = Color.White, fontSize = 14.sp)
                }

                // timer control buttons
                if (!timerRunning && !showContinueButton) {
                    Button(
                        onClick = { timerRunning = true },
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = Color(
                                0xFFE48E8E
                            )
                        ),
                        shape = RoundedCornerShape(25.dp)
                    ) {
                        Text(text = "Start Timer", color = Color.White)
                    }
                }

                if (timerRunning) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            timerRunning = false
                            showContinueButton = true
                        },
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = Color(
                                0xFFE48E8E
                            )
                        ),
                        shape = RoundedCornerShape(25.dp)
                    ) {
                        Text(text = "Stop Timer", color = Color.White)
                    }
                }

                if (!timerRunning && showContinueButton) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { timerRunning = true },
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = Color(
                                0xFFE48E8E
                            )
                        ),
                        shape = RoundedCornerShape(25.dp)
                    ) {
                        Text(text = "Continue", color = Color.White)
                    }
                }

                // share recipe button
                FloatingActionButton(
                    onClick = {
                        val shareIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(
                                Intent.EXTRA_TEXT,
                                "I just cooked $recipeName! Here are the steps: ${
                                    steps.joinToString(
                                        "\n"
                                    )
                                }"
                            )
                            type = "text/plain"
                        }
                        navController.context.startActivity(
                            Intent.createChooser(
                                shareIntent,
                                "Share Recipe"
                            )
                        )
                    },
                    backgroundColor = Color(0xFFE48E8E),
                    shape = RoundedCornerShape(23.dp)
                ) {
                    Text(text = "Share", color = Color.White, fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    item {
                        Text(
                            text = "Steps:",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4A4A4A)
                        )
                    }
                    items(steps) { step ->
                        Text(
                            text = step,
                            fontSize = 16.sp,
                            color = Color(0xFF737373),
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
        }
    }
}
