package com.example.quickcook_project.screens

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.rememberNavController
import com.example.quickcook_project.components.BottomNavigationBar
import com.example.quickcook_project.components.BottomNavigationBar
import com.example.quickcook_project.navigation.AppNavigationActivity

class MainActivity : ComponentActivity() {
    @SuppressLint("UnusedMaterialScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()

            Scaffold(
                bottomBar = { BottomNavigationBar(navController) },
                backgroundColor = Color(0xFF7F3C3C)
            ) {
                AppNavigationActivity(navController)
            }
        }
    }
}