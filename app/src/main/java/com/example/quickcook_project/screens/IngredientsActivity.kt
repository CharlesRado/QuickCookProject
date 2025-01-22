package com.example.quickcook_project.screens

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import coil.compose.AsyncImage
import com.example.quickcook_project.R
import com.example.quickcook_project.navigation.encodeForNavigation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class IngredientsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                val navController = rememberNavController()
                IngredientsScreen(navController = navController, onNavigateToProfile = {
                    navController.navigate("profile")
                },)
            }
        }
    }
}

@Composable
fun IngredientsScreen(
    navController: NavController,
    onNavigateToProfile: () -> Unit,
) {
    val firestore = FirebaseFirestore.getInstance()
    var ingredientsMap by remember { mutableStateOf(mapOf<String, List<String>>()) }
    val selectedIngredients = remember { mutableStateListOf<String>() }
    var username by remember { mutableStateOf("User") }
    var profileImageUrl by remember { mutableStateOf("") }

    // retrieve user and profile image
    LaunchedEffect(Unit) {
        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser
        if (user != null) {
            firestore.collection("users").document(user.uid).get()
                .addOnSuccessListener { document ->
                    username = document.getString("username") ?: "User"
                    profileImageUrl = document.getString("profileImageUrl") ?: ""
                }
        }

        // load ingredients ordered by categories
        firestore.collection("ingredients").get().addOnSuccessListener { documents ->
            val tempMap = mutableMapOf<String, MutableList<String>>()
            documents.forEach { doc ->
                val name = doc.getString("name") ?: ""
                val category = doc.getString("category") ?: "Other"
                tempMap.getOrPut(category) { mutableListOf() }.add(name)
            }
            ingredientsMap = tempMap
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF7F3C3C))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // header
            HeaderForIngredientsScreen(username, profileImageUrl, navController, onNavigateToProfile = onNavigateToProfile)

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Choose your ingredients :",
                fontSize = 20.sp,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn (
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                ingredientsMap.forEach { (category, ingredients) ->
                    item {
                        Text(
                            text = category,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    items(ingredients.chunked(2)) { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            row.forEach { ingredient ->
                                IngredientButton(
                                    ingredient = ingredient,
                                    isSelected = selectedIngredients.contains(ingredient),
                                    onClick = {
                                        if (selectedIngredients.contains(ingredient)) {
                                            selectedIngredients.remove(ingredient)
                                        } else {
                                            selectedIngredients.add(ingredient)
                                        }
                                    }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .offset(y = (-16).dp)
                .padding(bottom = 48.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            FloatingActionButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier
                    .padding(start = 16.dp),
                backgroundColor = Color(0xFFE48E8E),
                shape = CircleShape
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_previous),
                    contentDescription = "Back",
                    tint = Color.Unspecified,
                    modifier = Modifier.size(32.dp)
                )
            }

            FloatingActionButton(
                onClick = {
                    val ingredientsString = selectedIngredients.joinToString(";")
                    navController.navigate("cooking/$ingredientsString")
                },
                modifier = Modifier
                    .padding(end = 16.dp),
                backgroundColor = Color(0xFFE48E8E),
                shape = CircleShape
            ) {
                Text(
                    text = "Cook !",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
fun IngredientButton(ingredient: String, isSelected: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.width(140.dp).height(40.dp),
        shape = RoundedCornerShape(25.dp),
        colors = ButtonDefaults.buttonColors(backgroundColor = if (isSelected) Color(0xFFE48E8E) else Color.White)
    ) {
        Text(text = ingredient, color = if (isSelected) Color.White else Color.Black)
    }
}

@Composable
fun HeaderForIngredientsScreen(
    username: String,
    profileImageUrl: String,
    navController: NavController,
    onNavigateToProfile: () -> Unit
) {
    val firestore = FirebaseFirestore.getInstance()
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    var hasUnreadNotifications by remember { mutableStateOf(false) }

    LaunchedEffect(userId) {
        if (userId != null) {
            firestore.collection("users").document(userId)
                .collection("notifications")
                .whereEqualTo("isRead", false)
                .addSnapshotListener { snapshot, _ ->
                    hasUnreadNotifications = snapshot?.documents?.isNotEmpty() == true
                }
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Card(
                shape = CircleShape,
                modifier = Modifier
                    .size(50.dp)
                    .clickable {
                        onNavigateToProfile()
                    }
            ) {
                if (profileImageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = profileImageUrl,
                        contentDescription = "Profile Picture",
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_profile_dark),
                        contentDescription = "Default Profile Picture",
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(text = "Hey ! $username", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        }

        IconButton(
            onClick = { navController.navigate("notifications") }
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_notification),
                contentDescription = "Notifications",
                tint = if (hasUnreadNotifications) Color.Red else Color.Gray,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}