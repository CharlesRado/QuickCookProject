package com.example.quickcook_project.screens

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale


class RecipeDetailsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val name = intent.getStringExtra("name") ?: "Unknown Recipe"
        val category = intent.getStringExtra("category") ?: ""
        val meal = intent.getStringExtra("meal") ?: ""
        val imageUrl = intent.getStringExtra("imageUrl") ?: ""
        val preparationTime = intent.getStringExtra("preparationTime") ?: "N/A"
        val difficulty = intent.getStringExtra("difficulty") ?: "N/A"
        val calories = intent.getStringExtra("calories") ?: "0"
        val ingredients = intent.getStringExtra("ingredients")?.split(",") ?: emptyList()
        val steps = intent.getStringExtra("steps")?.split(",") ?: emptyList()

        setContent {
            RecipeDetailsScreen(
                name = name,
                category = category,
                meal = meal,
                imageUrl = imageUrl,
                preparationTime = preparationTime,
                difficulty = difficulty,
                calories = calories,
                ingredients = ingredients,
                steps = steps,
                onBack = { finish() }
            )
        }
    }
}

@Composable
fun RecipeDetailsScreen(
    name: String,
    category: String,
    meal: String,
    imageUrl: String,
    preparationTime: String,
    difficulty: String,
    calories: String,
    ingredients: List<String>,
    steps: List<String>,
    onBack: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFE2D9D9)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                // image and back button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                ) {
                    // principal image
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    // back button
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(64.dp)
                            .align(Alignment.TopStart)
                            .offset(x = 20.dp, y = 30.dp)
                            .clip(CircleShape)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_previous),
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // recipe title
                Text(
                    text = name,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4A4A4A),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // recipe details
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text(
                        text = "Category: $category",
                        fontSize = 16.sp,
                        color = Color(0xFF737373),
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    Text(
                        text = "Meal: $meal",
                        fontSize = 16.sp,
                        color = Color(0xFF737373)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // recipe information
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    RecipeInfoItem(icon = R.drawable.ic_time, label = preparationTime)
                    RecipeInfoItem(icon = R.drawable.ic_difficulty, label = difficulty)
                    RecipeInfoItem(icon = R.drawable.ic_calories, label = calories)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ingredients
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text(
                        text = "Ingredients",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color(0xFF4A4A4A),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    ingredients.forEach { ingredient ->
                        Text(
                            text = "• $ingredient",
                            fontSize = 16.sp,
                            color = Color(0xFF737373),
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // steps
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text(
                        text = "Steps:",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4A4A4A),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    steps.forEachIndexed { index, step ->
                        Text(
                            text = "${index + 1}. $step",
                            fontSize = 16.sp,
                            color = Color(0xFF737373),
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(64.dp))
            }
        }
    }
}

@Composable
fun RecipeInfoItem(icon: Int, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = label,
            modifier = Modifier.size(24.dp),
            tint = Color.Unspecified
        )
        Text(
            fontWeight = FontWeight.Bold,
            text = label,
            fontSize = 14.sp,
            color = Color(0xFF4A4A4A)
        )
    }
}