package com.example.quickcook_project.screens

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.quickcook_project.R
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.foundation.background
import androidx.compose.ui.draw.clip
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth


class RecipesActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Récupération des paramètres envoyés à l'activité
        val filterType = intent.getStringExtra("filterType") ?: "Unknown"
        val filterValue = intent.getStringExtra("filterValue") ?: "Unknown"
        val username = intent.getStringExtra("username") ?: "User"
        val profileImageUrl = intent.getStringExtra("profileImageUrl") ?: ""

        // Contenu Jetpack Compose
        setContent {
            val navController = rememberNavController()

            NavHost(
                navController = navController,
                startDestination = "recipes"
            ) {
                // Route pour RecipesScreen
                composable("recipes") {
                    RecipesScreen(
                        filterType = filterType,
                        filterValue = filterValue,
                        username = username,
                        profileImageUrl = profileImageUrl,
                        onNavigateToProfile = {
                            navController.navigate("profile") // Naviguer vers le profil
                        },
                        onBack = { finish() }, // Revenir en arrière
                        onRecipeClick = { selectedRecipe ->
                            val ingredients = selectedRecipe.ingredients.joinToString(";")
                            val steps = selectedRecipe.steps.joinToString(";")

                            navController.navigate(
                                "recipeDetails/${selectedRecipe.name}/${selectedRecipe.category}/${selectedRecipe.meal}/${selectedRecipe.imageUrl}/${selectedRecipe.preparationTime}/${selectedRecipe.difficulty}/${selectedRecipe.calories}/${ingredients}/${steps}"
                            )
                        }
                    )
                }

                // Route pour ProfileScreen
                composable("profile") {
                    ProfileScreen(
                        onNavigateTo = { destination ->
                            navController.navigate(destination)
                        }
                    )
                }

                // Route pour RecipeDetailsScreen
                composable(
                    route = "recipeDetails/{name}/{category}/{meal}/{imageUrl}/{preparationTime}/{difficulty}/{calories}/{ingredients}/{steps}",
                    arguments = listOf(
                        navArgument("name") { defaultValue = "Unknown" },
                        navArgument("category") { defaultValue = "" },
                        navArgument("meal") { defaultValue = "" },
                        navArgument("imageUrl") { defaultValue = "" },
                        navArgument("preparationTime") { defaultValue = "N/A" },
                        navArgument("difficulty") { defaultValue = "N/A" },
                        navArgument("calories") { defaultValue = "0" },
                        navArgument("ingredients") { defaultValue = "" },
                        navArgument("steps") { defaultValue = "" }
                    )
                ) { backStackEntry ->
                    RecipeDetailsScreen(
                        name = backStackEntry.arguments?.getString("name") ?: "Unknown",
                        category = backStackEntry.arguments?.getString("category") ?: "",
                        meal = backStackEntry.arguments?.getString("meal") ?: "",
                        imageUrl = backStackEntry.arguments?.getString("imageUrl") ?: "",
                        preparationTime = backStackEntry.arguments?.getString("preparationTime") ?: "N/A",
                        difficulty = backStackEntry.arguments?.getString("difficulty") ?: "N/A",
                        calories = backStackEntry.arguments?.getString("calories") ?: "0",
                        ingredients = backStackEntry.arguments?.getString("ingredients")?.split(",") ?: emptyList(),
                        steps = backStackEntry.arguments?.getString("steps")?.split(",") ?: emptyList()
                    )
                }
            }
        }
    }
}

@Composable
fun RecipesScreen(
    filterType: String,
    filterValue: String,
    username: String,
    profileImageUrl: String,
    onNavigateToProfile: () -> Unit,
    onBack: () -> Unit,
    onRecipeClick: (Recipe) -> Unit
) {
    val firestore = FirebaseFirestore.getInstance()
    var recipes by remember { mutableStateOf(listOf<Recipe>()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
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

    // Récupération des recettes depuis Firestore
    LaunchedEffect(filterType, filterValue) {
        val query = when (filterType) {
            "category" -> firestore.collection("recipes").whereEqualTo("category", filterValue)
            "meal" -> firestore.collection("recipes").whereEqualTo("meal", filterValue)
            else -> firestore.collection("recipes")
        }

        query.get()
            .addOnSuccessListener { documents ->
                val fetchedRecipes = mutableListOf<Recipe>()
                documents.forEach { doc ->
                    val ingredientsReferences = doc.data
                        .filterKeys { it.startsWith("ingredient") }
                        .mapNotNull { (_, value) -> value as? com.google.firebase.firestore.DocumentReference }

                    val recipe = Recipe(
                        name = doc.getString("name") ?: "",
                        category = doc.getString("category") ?: "",
                        calories = doc.getString("calories") ?: "0",
                        difficulty = doc.getString("difficulty") ?: "",
                        imageUrl = doc.getString("imageUrl") ?: "",
                        meal = doc.getString("meal") ?: "",
                        preparationTime = doc.getString("preparation_time") ?: "",
                        ingredients = emptyList(), // On chargera les ingrédients après
                        steps = doc.get("steps") as? List<String> ?: emptyList()
                    )

                    // Charger les données des ingrédients
                    val ingredientNames = mutableListOf<String>()
                    val ingredientTasks = ingredientsReferences.map { ref ->
                        ref.get().addOnSuccessListener { ingredientDoc ->
                            ingredientNames.add(ingredientDoc.getString("name") ?: "Unknown")
                        }
                    }

                    // Attendre que tous les ingrédients soient chargés
                    Tasks.whenAllSuccess<Void>(ingredientTasks).addOnCompleteListener {
                        recipe.ingredients = ingredientNames
                        fetchedRecipes.add(recipe)
                        recipes = fetchedRecipes
                        isLoading = false
                    }
                }
            }
            .addOnFailureListener { exception ->
                errorMessage = exception.message
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
            // En-tête
            HeaderForRecipesScreen(
                username = currentUsername,
                profileImageUrl = currentProfileImageUrl,
                onNavigateToProfile = onNavigateToProfile
            )

            Spacer(modifier = Modifier.height(16.dp))

            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color.White)
                    }
                }
                errorMessage != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = errorMessage ?: "An error occurred.",
                            color = Color.White
                        )
                    }
                }
                else -> {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            text = "$filterType: $filterValue",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        LazyColumn(
                            modifier = Modifier
                                .weight(1f) // Assure que la liste occupe tout l'espace restant
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(bottom = 16.dp) // espace avant le bouton
                        ) {
                            items(recipes) { recipe ->
                                RecipeCard(recipe = recipe, onClick = onRecipeClick)
                            }
                        }

                        Button(
                            onClick = onBack,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .height(48.dp), // Taille du bouton
                            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFE48E8E)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement  = Arrangement.Center
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_previous),
                                    contentDescription = "Back",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                Text(
                                    text = "Back",
                                    color = Color(0xFF7F3C3C),
                                    style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                )


                            }
                        }
                        Spacer(modifier = Modifier.width(64.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun RecipeCard(recipe: Recipe, onClick: (Recipe) -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().clickable { onClick(recipe) },
        elevation = 4.dp
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = recipe.imageUrl,
                contentDescription = recipe.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(60.dp).background(Color.White).clip(CircleShape)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = recipe.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(text = "Ingredients: ${recipe.ingredients.joinToString(", ")}", fontSize = 14.sp, color = Color.Gray)
            }
        }
    }
}

// Recipe data class
data class Recipe(
    val name: String,
    val category: String,
    val meal: String,
    val imageUrl: String,
    val preparationTime: String,
    val difficulty: String,
    val calories: String,
    var ingredients: List<String>,
    val steps: List<String>
)

@Composable
fun FloatingBackButton(onBack: () -> Unit, modifier: Modifier = Modifier) {
    FloatingActionButton(
        onClick = onBack,
        backgroundColor = Color.White,
        modifier = Modifier.size(48.dp) // Taille réduite du bouton
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_previous),
            contentDescription = "Back",
            tint = Color(0xFF7F3C3C),
            modifier = Modifier.size(24.dp) // Taille de l'icône ajustée
        )
    }
    /*Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.BottomStart
    ) {
        FloatingActionButton(
            onClick = onBack,
            backgroundColor = Color.White,
            modifier = Modifier.size(48.dp) // Taille réduite du bouton
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_previous),
                contentDescription = "Back",
                tint = Color(0xFF7F3C3C),
                modifier = Modifier.size(24.dp) // Taille de l'icône ajustée
            )
        }
    }*/
}

@Composable
fun HeaderForRecipesScreen(
    username: String,
    profileImageUrl: String,
    onNavigateToProfile: () -> Unit
) {
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
                    color = Color.White
                )
            )
        }
        Icon(
            painter = painterResource(id = R.drawable.ic_notification),
            contentDescription = "Notification",
            modifier = Modifier.size(30.dp),
            tint = Color.White
        )
    }
}