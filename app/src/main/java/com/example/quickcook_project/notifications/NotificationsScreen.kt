package com.example.quickcook_project.notifications

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth

@Composable
fun NotificationsScreen(navController: NavController) {
    val firestore = FirebaseFirestore.getInstance()
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    var notifications by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }

    LaunchedEffect(userId) {
        if (userId != null) {
            firestore.collection("users").document(userId)
                .collection("notifications")
                .orderBy("timestamp")
                .addSnapshotListener { snapshot, _ ->
                    val newNotifications = snapshot?.documents?.mapNotNull { it.data } ?: emptyList()
                    notifications = newNotifications

                    // mark new notifications as read
                    for (doc in snapshot?.documents ?: emptyList()) {
                        if (doc.getBoolean("isRead") == false) {
                            doc.reference.update("isRead", true)
                        }
                    }
                }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

        Spacer(modifier = Modifier.height(32.dp))

        Text(text = "Notifications", style = MaterialTheme.typography.h5, fontWeight = FontWeight.Bold, color = Color.White)

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(notifications) { notification ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    elevation = 4.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = notification["title"].toString(), fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = notification["message"].toString(), color = Color.Gray)
                    }
                }
            }
        }
    }
}