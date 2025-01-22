package com.example.quickcook_project.components

import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.compose.ui.res.painterResource
import com.example.quickcook_project.R
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    BottomNavigation(
        backgroundColor = Color(0xFF7F3C3C),
        elevation = 8.dp
    ) {
        // Profile Button
        BottomNavigationItem(
            selected = currentRoute == "profile",
            onClick = { navController.navigate("profile") },
            icon = {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = if (currentRoute == "profile") Color.White else Color.Transparent,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(6.dp)
                ) {
                    Icon(
                        painter = painterResource(
                            id = if (currentRoute == "profile") R.drawable.ic_profile_dark
                            else R.drawable.ic_profile
                        ),
                        contentDescription = "Profile",
                        tint = if (currentRoute == "profile") Color(0xFF7F3C3C) else Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        )

        // Home Button
        BottomNavigationItem(
            selected = currentRoute == "home",
            onClick = { navController.navigate("home") },
            icon = {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = if (currentRoute == "home") Color.White else Color.Transparent,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(6.dp)
                ) {
                    Icon(
                        painter = painterResource(
                            id = if (currentRoute == "home") R.drawable.ic_home_dark
                            else R.drawable.ic_home
                        ),
                        contentDescription = "Home",
                        tint = if (currentRoute == "home") Color(0xFF7F3C3C) else Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        )

        // Statistics Button
        BottomNavigationItem(
            selected = currentRoute == "stats",
            onClick = { navController.navigate("stats") },
            icon = {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = if (currentRoute == "stats") Color.White else Color.Transparent,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(6.dp)
                ) {
                    Icon(
                        painter = painterResource(
                            id = if (currentRoute == "stats") R.drawable.ic_stats_dark
                            else R.drawable.ic_stats
                        ),
                        contentDescription = "Statistics",
                        tint = if (currentRoute == "stats") Color(0xFF7F3C3C) else Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        )
    }
}