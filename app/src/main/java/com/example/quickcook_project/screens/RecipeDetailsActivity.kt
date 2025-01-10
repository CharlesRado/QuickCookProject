package com.example.quickcook_project.screens

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import com.example.quickcook_project.R


class RecipeDetailsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val name = intent.getStringExtra("name") ?: "Unknown Recipe"
        val description = intent.getStringExtra("description") ?: "No description available."
        val imageUrl = intent.getStringExtra("imageUrl") ?: ""
        val time = intent.getStringExtra("time") ?: "N/A"
        val difficulty = intent.getStringExtra("difficulty") ?: "N/A"
        val calories = intent.getStringExtra("calories") ?: "N/A"

        setContent {
            RecipeDetailsScreen(
                name = name,
                description = description,
                imageUrl = imageUrl,
                time = time,
                difficulty = difficulty,
                calories = calories
            )
        }
    }
}

@Composable
fun RecipeDetailsScreen(
    name: String,
    description: String,
    imageUrl: String,
    time: String,
    difficulty: String,
    calories: String
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF7F3C3C)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Back button
            IconButton(onClick = { /* Handle back navigation */ }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_previous),
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            // Recipe image
            AsyncImage(
                model = imageUrl,
                contentDescription = name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                alignment = Alignment.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Recipe details
            Text(
                text = name,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = description,
                fontSize = 16.sp,
                color = Color.LightGray,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                RecipeInfoItem(icon = R.drawable.ic_time, label = time)
                RecipeInfoItem(icon = R.drawable.ic_difficulty, label = difficulty)
                RecipeInfoItem(icon = R.drawable.ic_calories, label = calories)
            }
        }
    }
}

@Composable
fun RecipeInfoItem(icon: Int, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = label,
            modifier = Modifier.size(24.dp),
            tint = Color.White
        )
        Text(text = label, fontSize = 14.sp, color = Color.White)
    }
}