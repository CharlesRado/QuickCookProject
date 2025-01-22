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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.quickcook_project.R
import com.example.quickcook_project.screens.Recipe
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CookingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                val navController = rememberNavController()
                val ingredients = intent.getStringExtra("ingredients")?.split(",") ?: emptyList()
                CookingScreen(navController = navController, selectedIngredients = ingredients, onBack = { finish() } )
            }
        }
    }
}

@Composable
fun CookingScreen(
    navController: NavController,
    selectedIngredients: List<String>,
    onBack: () -> Unit
) {
    val firestore = FirebaseFirestore.getInstance()
    var recipes by remember { mutableStateOf<List<Recipe>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val selectedRecipe = remember { mutableStateOf<Recipe?>(null) }
    var username by remember { mutableStateOf("User") }
    var profileImageUrl by remember { mutableStateOf("") }
    var currentUsername by remember { mutableStateOf(username) } // to retrieve current user
    var currentProfileImageUrl by remember { mutableStateOf(profileImageUrl) }

    LaunchedEffect(Unit) {
        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser
        if (user != null) {
            firestore.collection("users").document(user.uid).get()
                .addOnSuccessListener { document ->
                    currentUsername = document.getString("username") ?: "User"
                    currentProfileImageUrl = document.getString("profileImageUrl") ?: ""
                }
                .addOnFailureListener {
                    currentUsername = "User"
                }
        }
    }

    // ingredient loading depending on selected ingredients
    LaunchedEffect(selectedIngredients) {
        val query = firestore.collection("recipes")

        query.get()
            .addOnSuccessListener { documents ->
                val fetchedRecipes = mutableListOf<Recipe>()
                val allTasks = mutableListOf<Task<Void>>()

                documents.forEach { doc ->
                    val recipe = Recipe(
                        name = doc.getString("name") ?: "",
                        category = doc.getString("category") ?: "",
                        calories = doc.get("calories")?.toString() ?: "0",
                        difficulty = doc.getString("difficulty") ?: "",
                        imageUrl = doc.getString("imageUrl") ?: "",
                        meal = doc.getString("meal") ?: "",
                        preparationTime = doc.getString("preparationTime") ?: "",
                        ingredients = emptyList(),
                        steps = doc.get("steps") as? List<String> ?: emptyList()
                    )

                    val ingredientNames = mutableListOf<String>()

                    val ingredientsReferences = doc.data
                        .filterKeys { it.startsWith("ingredient") }
                        .mapNotNull { (_, value) -> value as? com.google.firebase.firestore.DocumentReference }

                    if (ingredientsReferences.isNotEmpty()) {
                        val ingredientTasks = ingredientsReferences.map { ref ->
                            ref.get().continueWithTask { task ->
                                if (task.isSuccessful) {
                                    val ingredientDoc = task.result
                                    val name = ingredientDoc?.getString("name") ?: "Unknown"
                                    ingredientNames.add(name)
                                }
                                Tasks.forResult(null)
                            } as Task<Void>
                        }
                        allTasks.addAll(ingredientTasks)
                    } else {
                        ingredientNames.addAll(doc.get("ingredients") as? List<String> ?: emptyList())
                    }

                    // wait until all ingredients are retrieve
                    Tasks.whenAllComplete(allTasks).addOnCompleteListener {
                        recipe.ingredients = ingredientNames

                        // verify if recipe matched with selected ingredients
                        if (selectedIngredients.all { it in recipe.ingredients }) {
                            fetchedRecipes.add(recipe)
                        }

                        recipes = fetchedRecipes
                        isLoading = false
                    }
                }
            }
            .addOnFailureListener {
                isLoading = false
            }
    }

    // principal structure
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
            HeaderForIngredientsScreen(username, profileImageUrl, navController, onNavigateToProfile = {
                navController.navigate("profile")
            })

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Recipes matching your ingredient(s)",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(32.dp))

            if (isLoading) {
                CircularProgressIndicator(color = Color.White)
            } else {
                LazyColumn (
                    modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(recipes) { recipe ->
                        RecipeCard(
                            recipe = recipe,
                            isSelected = selectedRecipe.value == recipe,
                            onClick = {
                                selectedRecipe.value =
                                    if (selectedRecipe.value == recipe) null else recipe
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
        FloatingActionButton(
            onClick = onBack,
            modifier = Modifier.align(Alignment.BottomEnd).offset(y = (-32).dp).padding(end = 16.dp, bottom = 32.dp),
            backgroundColor = Color(0xFFE48E8E),
            shape = CircleShape
        ) {
            Icon(painter = painterResource(id = R.drawable.ic_previous), contentDescription = "Back", tint = Color.Unspecified, modifier = Modifier.size(32.dp))
        }

        selectedRecipe.value?.let {
            FloatingActionButton(
                onClick = {
                    navController.navigate("cooking_timer/${it.name}/${it.preparationTime}/${it.steps.joinToString(";")}")
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = (-32).dp)
                    .padding(bottom = 32.dp),
                backgroundColor = Color(0xFFE48E8E),
                shape = CircleShape
            ) {
                Text(text = "Start", color = Color.White, fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun RecipeCard(recipe: Recipe, isSelected: Boolean, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        backgroundColor = if (isSelected) Color(0xFFE48E8E) else Color.White,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(4.dp),
        elevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = recipe.imageUrl,
                contentDescription = recipe.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = recipe.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = if (isSelected) Color(0xFF4A4A4A) else Color.Black
                )
                Text(
                    text = "Ingredients: ${recipe.ingredients.joinToString(", ")}",
                    fontSize = 14.sp,
                    color = if (isSelected) Color(0xFF737373) else Color.Gray
                )
            }
        }
    }
}
