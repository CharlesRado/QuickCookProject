package com.example.quickcook_project

import android.annotation.SuppressLint
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue

@SuppressLint("StaticFieldLeak")
object FirestoreHelper {
    private val firestore = FirebaseFirestore.getInstance()

    // Add a user
    fun addUser(uid: String, username: String, email: String) {
        val user = mapOf(
            "username" to username,
            "email" to email,
            "recipes" to emptyList<Map<String, Any>>()
        )

        firestore.collection("users")
            .document(uid)
            .set(user)
            .addOnSuccessListener {
                println("User added successfully")
            }
            .addOnFailureListener { e ->
                println("Error adding user: ${e.message}")
            }
    }

    // Associer une recette à un utilisateur
    fun associateRecipeWithUser(uid: String, recipeId: String, recipeName: String) {
        val recipe = mapOf(
            "recipeId" to recipeId,
            "recipeName" to recipeName,
            "sharedWith" to emptyList<String>()
        )

        firestore.collection("users")
            .document(uid)
            .update("recipes", FieldValue.arrayUnion(recipe))
            .addOnSuccessListener {
                println("Recipe associated with user successfully")
            }
            .addOnFailureListener { e ->
                println("Error associating recipe with user: ${e.message}")
            }
    }

    // Récupérer les recettes d'un utilisateur
    fun getUserRecipes(uid: String, onResult: (List<Map<String, Any>>) -> Unit) {
        firestore.collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val recipes = document.get("recipes") as List<Map<String, Any>>
                    onResult(recipes)
                } else {
                    onResult(emptyList())
                }
            }
            .addOnFailureListener { e ->
                println("Error fetching user recipes: ${e.message}")
            }
    }

    fun addRecipe(recipeId: String, name: String, ingredients: List<String>, steps: List<String>, createdBy: String) {
        val recipe = mapOf(
            "recipeId" to recipeId,
            "name" to name,
            "ingredients" to ingredients,
            "steps" to steps,
            "createdBy" to createdBy,
            "sharedCount" to 0
        )

        firestore.collection("recipes")
            .document(recipeId)
            .set(recipe)
            .addOnSuccessListener {
                println("Recipe added successfully")
            }
            .addOnFailureListener { e ->
                println("Error adding recipe: ${e.message}")
            }
    }

    // Récupérer une recette spécifique
    fun getRecipe(recipeId: String, onResult: (Map<String, Any>?) -> Unit) {
        firestore.collection("recipes")
            .document(recipeId)
            .get()
            .addOnSuccessListener { document ->
                onResult(document.data)
            }
            .addOnFailureListener { e ->
                println("Error fetching recipe: ${e.message}")
            }
    }
}