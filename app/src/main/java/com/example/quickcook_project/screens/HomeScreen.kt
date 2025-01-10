package com.example.quickcook_project.screens

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import com.example.quickcook_project.R
import androidx.compose.foundation.clickable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import coil.compose.AsyncImage

@Composable
fun HomeScreen(onCategorySelected: (String) -> Unit,
               onMealSelected: (String) -> Unit,
               onNavigateToProfile: () -> Unit
) {

    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()

    var username by remember { mutableStateOf("Loading...") }
    var profileImageUrl by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val user = auth.currentUser
        if (user != null) {
            firestore.collection("users").document(user.uid).get()
                .addOnSuccessListener { document ->
                    username = document.getString("username") ?: "User"
                    profileImageUrl = document.getString("profileImageUrl") ?: ""
                }
                .addOnFailureListener {
                    username = "User"
                }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFE2D9D9)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Section supérieure avec la photo de profil et le message "Hey !"
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
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
                    Text(
                        text = "Hey ! $username",
                        style = TextStyle(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4A4A4A)
                        )
                    )
                }
                Icon(
                    painter = painterResource(id = R.drawable.ic_notification),
                    contentDescription = "Notification",
                    modifier = Modifier.size(30.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Barre de recherche
            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by ingredient, recipe...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .height(55.dp),
                shape = RoundedCornerShape(25.dp),
                colors = TextFieldDefaults.textFieldColors(
                    backgroundColor = Color.White,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Section "Choose by categories..."
            Text(
                text = "Choose by categories...",
                style = TextStyle(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4A4A4A)
                ),
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    CategoryItem(R.drawable.ic_salad, "Salad") { onCategorySelected("Salad") }
                    CategoryItem(R.drawable.ic_pasta, "Pasta") { onCategorySelected("Pasta") }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    CategoryItem(R.drawable.ic_soup, "Soup") { onCategorySelected("Soup") }
                    CategoryItem(R.drawable.ic_balanced, "Balanced") { onCategorySelected("Balanced") }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Section "Choose by meal..."
            Text(
                text = "Choose by meal...",
                style = TextStyle(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4A4A4A)
                ),
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    MealItem(R.drawable.ic_breakfast, "Breakfast") { onMealSelected("Breakfast") }
                    MealItem(R.drawable.ic_lunch, "Lunch") { onMealSelected("Lunch") }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    MealItem(R.drawable.ic_snack, "Snack") { onMealSelected("Snack") }
                    MealItem(R.drawable.ic_diner, "Diner") { onMealSelected("Diner") }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Bouton "Start Cooking !"
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Button(
                    onClick = {
                        val intent = Intent(context, CookingActivity::class.java)
                        context.startActivity(intent)
                    },
                    modifier = Modifier
                        .width(200.dp)
                        .align(Alignment.Center)
                        .height(50.dp),
                    shape = RoundedCornerShape(25.dp),
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFE48E8E))
                ) {
                    Text(
                        text = "Start cooking !",
                        color = Color.White,
                        style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryItem(iconRes: Int, label: String, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .size(80.dp)
            .clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = label,
            modifier = Modifier.size(50.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = TextStyle(
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4A4A4A)
            )
        )
    }
}

@Composable
fun MealItem(iconRes: Int, label: String, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .size(80.dp)
            .clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = label,
            modifier = Modifier.size(50.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = TextStyle(
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4A4A4A) // Applique la couleur demandée
            )
        )
    }
}
