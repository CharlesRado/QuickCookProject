package com.example.quickcook_project.screens

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class StatisticsScreenActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            StatisticsScreen()
        }
    }
}

@Composable
fun StatisticsScreen() {
    val statsData = remember { mutableStateOf(emptyMap<String, String>()) }

    // Simulation des données statistiques
    LaunchedEffect(Unit) {
        statsData.value = mapOf(
            "Total Recipes" to "42",
            "Saved Recipes" to "8",
            "Categories Explored" to "5",
            "Last Recipe Viewed" to "Salad Nicoise"
        )
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF7F3C3C))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Statistics",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Affichage des statistiques sous forme de cartes
            statsData.value.forEach { (label, value) ->
                StatsItem(label = label, value = value)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun StatsItem(label: String, value: String) {
    Card(
        shape = RoundedCornerShape(12.dp),
        backgroundColor = Color.White,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, color = Color.Black, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(text = value, color = Color.Gray, fontSize = 18.sp)
        }
    }
}