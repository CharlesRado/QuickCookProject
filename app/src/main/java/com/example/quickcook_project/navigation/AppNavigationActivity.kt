package com.example.quickcook_project.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.quickcook_project.screens.HomeScreen
import com.example.quickcook_project.screens.ProfileScreen
import com.example.quickcook_project.screens.RecipeDetailsScreen
import com.example.quickcook_project.screens.RecipesScreen
import com.example.quickcook_project.screens.StatisticsScreen
import java.net.URLEncoder
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

@Composable
fun AppNavigationActivity(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "home") {

        // Home Screen
        composable("home") {
            HomeScreen(
                onCategorySelected = { category ->
                    // Naviguer vers la page des recettes pour cette catégorie
                    navController.navigate("recipes/category/$category")
                },
                onMealSelected = { meal ->
                    // Naviguer vers la page des recettes pour ce type de repas
                    navController.navigate("recipes/meal/$meal")
                },
                onNavigateToProfile = {
                    navController.navigate("profile") // Navigation vers la page de profil
                }
            )
        }

        composable(
            route = "recipes/category/{category}",
            arguments = listOf(navArgument("category") { defaultValue = "Unknown" })
        ) { backStackEntry ->
            val category = backStackEntry.arguments?.getString("category") ?: "Unknown"
            RecipesScreen(
                filterType = "category",
                filterValue = category,
                username = "Mickael", // Passez les données nécessaires
                profileImageUrl = "", // Ajoutez l'URL de l'image de profil si disponible
                onNavigateToProfile = {
                    navController.navigate("profile")
                },
                onBack = {
                    navController.popBackStack() // Revenir en arrière
                },
                
                onRecipeClick = { selectedRecipe ->
                    val encodedIngredients = encodeForNavigation(selectedRecipe.ingredients.joinToString(";"))
                    val encodedSteps = encodeForNavigation(selectedRecipe.steps.joinToString(";"))
                    val encodedName = encodeForNavigation(selectedRecipe.name)
                    val encodedCategory = encodeForNavigation(selectedRecipe.category)
                    val encodedMeal = encodeForNavigation(selectedRecipe.meal)
                    val encodedImageUrl = encodeForNavigation(selectedRecipe.imageUrl)
                    val encodedPreparationTime = encodeForNavigation(selectedRecipe.preparationTime)
                    val encodedDifficulty = encodeForNavigation(selectedRecipe.difficulty)
                    val encodedCalories = encodeForNavigation(selectedRecipe.calories)

                    navController.navigate(
                        "recipeDetails/$encodedName/$encodedCategory/$encodedMeal/$encodedImageUrl/$encodedPreparationTime/$encodedDifficulty/$encodedCalories/$encodedIngredients/$encodedSteps"
                    )
                }
            )
        }

        composable(
            route = "recipes/meal/{meal}",
            arguments = listOf(navArgument("meal") { defaultValue = "Unknown" })
        ) { backStackEntry ->
            val meal = backStackEntry.arguments?.getString("meal") ?: "Unknown"
            RecipesScreen(
                filterType = "meal",
                filterValue = meal,
                username = "Mickael", // Passez les données nécessaires
                profileImageUrl = "", // Ajoutez l'URL de l'image de profil si disponible
                onNavigateToProfile = {
                    navController.navigate("profile")
                },
                onBack = {
                    navController.popBackStack() // Revenir en arrière
                },
                onRecipeClick = { selectedRecipe ->
                    val encodedIngredients = encodeForNavigation(selectedRecipe.ingredients.joinToString(";"))
                    val encodedSteps = encodeForNavigation(selectedRecipe.steps.joinToString(";"))
                    val encodedName = encodeForNavigation(selectedRecipe.name)
                    val encodedCategory = encodeForNavigation(selectedRecipe.category)
                    val encodedMeal = encodeForNavigation(selectedRecipe.meal)
                    val encodedImageUrl = encodeForNavigation(selectedRecipe.imageUrl)
                    val encodedPreparationTime = encodeForNavigation(selectedRecipe.preparationTime)
                    val encodedDifficulty = encodeForNavigation(selectedRecipe.difficulty)
                    val encodedCalories = encodeForNavigation(selectedRecipe.calories)

                    navController.navigate(
                        "recipeDetails/$encodedName/$encodedCategory/$encodedMeal/$encodedImageUrl/$encodedPreparationTime/$encodedDifficulty/$encodedCalories/$encodedIngredients/$encodedSteps"
                    )
                }
            )
        }

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
                name = decodeFromNavigation(backStackEntry.arguments?.getString("name") ?: "Unknown"),
                category = decodeFromNavigation(backStackEntry.arguments?.getString("category") ?: ""),
                meal = decodeFromNavigation(backStackEntry.arguments?.getString("meal") ?: ""),
                imageUrl = decodeFromNavigation(backStackEntry.arguments?.getString("imageUrl") ?: ""),
                preparationTime = decodeFromNavigation(backStackEntry.arguments?.getString("preparationTime") ?: "N/A"),
                difficulty = decodeFromNavigation(backStackEntry.arguments?.getString("difficulty") ?: "N/A"),
                calories = decodeFromNavigation(backStackEntry.arguments?.getString("calories") ?: "0"),
                ingredients = decodeFromNavigation(backStackEntry.arguments?.getString("ingredients") ?: "").split(";"),
                steps = decodeFromNavigation(backStackEntry.arguments?.getString("steps") ?: "").split(";")
            )
        }

        // Profile Screen
        composable("profile") {
            ProfileScreen(
                onNavigateTo = { destination ->
                    navController.navigate(destination)
                }
            )
        }

        // Statistics Screen
        composable("stats") {
            StatisticsScreen()
        }

        // Edit Profile Name Screen
        /*composable("edit_profile_name") {
            // Appel de l'écran pour modifier le nom de profil
            EditProfileNameScreen()
        }

        // Change Email Screen
        composable("change_email") {
            ChangeEmailScreen()
        }

        // Change Password Screen
        composable("change_password") {
            ChangePasswordScreen()
        }

        // Settings Screen
        composable("settings") {
            SettingsScreen()
        }

        composable("stats") {
            StatisticsScreen()
        }*/
    }
}

// Encodage de l'URL pour éviter les problèmes avec les caractères spéciaux
fun encodeForNavigation(input: String): String {
    return URLEncoder.encode(input, StandardCharsets.UTF_8.toString())
}

// Decodage de l'URL pour éviter les problèmes avec les caractères spéciaux
fun decodeFromNavigation(input: String): String {
    return URLDecoder.decode(input, StandardCharsets.UTF_8.toString())
}