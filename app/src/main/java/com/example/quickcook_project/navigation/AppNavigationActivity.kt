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
                    navController.navigate(
                        "recipeDetails/${selectedRecipe.name}/${selectedRecipe.description}/${selectedRecipe.imageUrl}/${selectedRecipe.time}/${selectedRecipe.difficulty}/${selectedRecipe.calories}"
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
                    navController.navigate(
                        "recipeDetails/${selectedRecipe.name}/${selectedRecipe.description}/${selectedRecipe.imageUrl}/${selectedRecipe.time}/${selectedRecipe.difficulty}/${selectedRecipe.calories}"
                    )
                }
            )
        }

        composable(
            route = "recipeDetails/{name}/{description}/{imageUrl}/{time}/{difficulty}/{calories}",
            arguments = listOf(
                navArgument("name") { defaultValue = "Unknown" },
                navArgument("description") { defaultValue = "No description available." },
                navArgument("imageUrl") { defaultValue = "" },
                navArgument("time") { defaultValue = "N/A" },
                navArgument("difficulty") { defaultValue = "N/A" },
                navArgument("calories") { defaultValue = "N/A" }
            )
        ) { backStackEntry ->
            RecipeDetailsScreen(
                name = backStackEntry.arguments?.getString("name") ?: "Unknown Recipe",
                description = backStackEntry.arguments?.getString("description") ?: "No description available.",
                imageUrl = backStackEntry.arguments?.getString("imageUrl") ?: "",
                time = backStackEntry.arguments?.getString("time") ?: "N/A",
                difficulty = backStackEntry.arguments?.getString("difficulty") ?: "N/A",
                calories = backStackEntry.arguments?.getString("calories") ?: "N/A"
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
        }*/

        composable("stats") {
            StatisticsScreen()
        }
    }
}
