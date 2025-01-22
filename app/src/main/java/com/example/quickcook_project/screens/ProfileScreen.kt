package com.example.quickcook_project.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import com.example.quickcook_project.R
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

@Composable
fun ProfileScreen(onNavigateTo: (String) -> Unit) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    val storage = FirebaseStorage.getInstance()

    var username by remember { mutableStateOf("Loading...") }
    var email by remember { mutableStateOf("Loading...") }
    var totalTimeConnected by remember { mutableStateOf("Loading...") }
    var profileImageUrl by remember { mutableStateOf("") }
    var isEditingUsername by remember { mutableStateOf(false) } // indicator to activate or disactivate editing mode for username
    var newUsername by remember { mutableStateOf("") } // new username filled
    var isEditingEmail by remember { mutableStateOf(false) } // indicator to activate or disactivate editing mode for email
    var newEmail by remember { mutableStateOf("") } // new email filled

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            uploadProfileImage(storage, auth, firestore, uri) { newImageUrl ->
                profileImageUrl = newImageUrl
            }
        }
    }

    LaunchedEffect(Unit) {
        val user = auth.currentUser
        if (user != null) {
            // retrieve user email
            email = user.email ?: "No email"
            println("DEBUG: User UID is ${user.uid}")

            val loginTimestamp = System.currentTimeMillis() // Marquer le moment de connexion

            // retrieve user informations from Firestore
            firestore.collection("users").document(user.uid).get()
                .addOnSuccessListener { document ->
                    username = document.getString("username") ?: "No name"
                    profileImageUrl = document.getString("profileImageUrl") ?: ""

                    // retrieve and format the total connection time
                    val totalTime = document.getLong("totalTime") ?: 0L
                    totalTimeConnected = formatTime(totalTime)

                    println("DEBUG: Username retrieved: $username")
                    println("DEBUG: Total time connected: $totalTimeConnected")
                }
                .addOnFailureListener { exception ->
                    println("DEBUG: Failed to retrieve data: ${exception.message}")
                }

            // store the connection timestamp into Firestore
            firestore.collection("users").document(user.uid)
                .update("loginTimestamp", loginTimestamp)
                .addOnSuccessListener {
                    println("DEBUG: Login timestamp set successfully.")
                }
                .addOnFailureListener { e ->
                    println("DEBUG: Failed to set login timestamp: ${e.message}")
                }
        } else {
            println("DEBUG: No authenticated user found.")
        }
    }

    
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF7F3C3C)),
        color = Color(0xFF7F3C3C)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Titre
            Text(
                text = "Profile",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(16.dp))

            // profile picture
            Box(contentAlignment = Alignment.BottomEnd) {
                Card(
                    shape = CircleShape,
                    elevation = 4.dp,
                    modifier = Modifier.size(120.dp)
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
                            tint = Color.Unspecified,
                            modifier = Modifier
                                //.fillMaxSize()
                                .size(32.dp)
                                .clickable { imagePickerLauncher.launch("image/*") }
                        )
                    }
                }
                Icon(
                    painter = painterResource(id = R.drawable.ic_edit_profile),
                    contentDescription = "Edit Profile Picture",
                    tint = Color.Unspecified,
                    modifier = Modifier
                        .size(32.dp)
                        .clickable { imagePickerLauncher.launch("image/*") }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // username and email
            Text(
                text = username, // display username
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = email, // display email
                fontSize = 16.sp,
                color = Color(0xFFDDDDDD),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // statistics section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(totalTimeConnected, "Total time", R.drawable.ic_waste)
                StatItem("3", "Done", R.drawable.ic_recipe)
                StatItem("4", "Favorites", R.drawable.ic_difficulty)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // menu to edit username
            if (isEditingUsername) {
                Card(
                    backgroundColor = Color.White,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        TextField(
                            value = newUsername,
                            onValueChange = { newUsername = it },
                            label = { Text(text = "New Username") }, // Label "New Username" persistant
                            colors = TextFieldDefaults.textFieldColors(
                                textColor = Color.Black,
                                backgroundColor = Color(0xFFF1F1F1),
                                focusedIndicatorColor = Color(0xFF7F3C3C),
                                unfocusedIndicatorColor = Color(0xFFDADADA),
                                cursorColor = Color(0xFF7F3C3C),
                                focusedLabelColor = Color.Gray,
                                unfocusedLabelColor = Color.Gray
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF1F1F1), shape = RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                updateUsername(
                                    auth = auth,
                                    firestore = firestore,
                                    newUsername = newUsername,
                                    onUpdateSuccess = {
                                        username = newUsername
                                        isEditingUsername = false
                                    },
                                    onUpdateFail = {
                                        println("Failed to update username in Firestore.")
                                    }
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(24.dp),
                            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF7F3C3C))
                        ) {
                            Text(
                                text = "Confirm",
                                color = Color.White
                            )
                        }
                    }
                }
            }



            // menu
            ProfileMenuItem(
                label = if (isEditingUsername) "Cancel Editing" else "Edit Profile Name",
                iconRes = R.drawable.ic_edit,
                onNavigateTo = {
                    isEditingUsername = !isEditingUsername
                },
                destination = ""
            )
            ProfileMenuItem(
                label = "Change email",
                iconRes = R.drawable.ic_mail,
                onNavigateTo = {
                    isEditingEmail = !isEditingEmail
                },
                destination = ""
            )

            if (isEditingEmail) {
                Card(
                    backgroundColor = Color.White,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        TextField(
                            value = newEmail,
                            onValueChange = { newEmail = it },
                            label = { Text(text = "New Email", color = Color.Gray) },
                            colors = TextFieldDefaults.textFieldColors(
                                textColor = Color.Black,
                                backgroundColor = Color(0xFFF1F1F1),
                                focusedIndicatorColor = Color(0xFF7F3C3C),
                                unfocusedIndicatorColor = Color(0xFFDADADA),
                                cursorColor = Color(0xFF7F3C3C),
                                focusedLabelColor = Color.Gray,
                                unfocusedLabelColor = Color.Gray
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF1F1F1), shape = RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                updateEmail(
                                    auth = auth,
                                    firestore = firestore,
                                    newEmail = newEmail,
                                    onUpdateSuccess = {
                                        email = newEmail
                                        isEditingEmail = false
                                        Toast.makeText(context, "Email update successfully.", Toast.LENGTH_LONG).show()
                                        // println("Email updated successfully.")
                                    },
                                    onUpdateFail = { error ->
                                        println(error)
                                        if (error.contains("verify")) {

                                            Toast.makeText(context, "Please verify your email before updating.", Toast.LENGTH_LONG).show()
                                        } else {
                                            Toast.makeText(context, "Failed to update email: $error", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(24.dp),
                            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF7F3C3C))
                        ) {
                            Text(
                                text = "Confirm",
                                color = Color.White,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
            ProfileMenuItem("Change password", R.drawable.ic_padlock, onNavigateTo, "changePassword")
            ProfileMenuItem("Settings", R.drawable.ic_setting, onNavigateTo, "settings")
            ProfileMenuItem(
                label = "Log out",
                iconRes = R.drawable.ic_logout,
                onNavigateTo = {
                    onLogout(auth, firestore) {
                        onNavigateTo("login")
                    }
                },
                destination = "",
                iconTint = Color.Red
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun StatItem(value: String, label: String, iconRes: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier.size(36.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            color = Color(0xFFDDDDDD),
            fontSize = 12.sp
        )
    }
}

@Composable
fun ProfileMenuItem(
    label: String,
    iconRes: Int,
    onNavigateTo: (String) -> Unit,
    destination: String,
    iconTint: Color = Color.Black
) {
    Card(
        backgroundColor = Color.White,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onNavigateTo(destination) }
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = label,
                    color = if (iconTint == Color.Red) Color.Red else Color.Black,
                    fontSize = 16.sp
                )
            }
            Icon(
                painter = painterResource(id = R.drawable.ic_next_dark),
                contentDescription = "Arrow",
                tint = if (iconTint == Color.Red) Color.Red else Color.Gray,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// function to handle image uploading into firebase storage and update firestore
fun uploadProfileImage(
    storage: FirebaseStorage,
    auth: FirebaseAuth,
    firestore: FirebaseFirestore,
    uri: Uri,
    onUploadComplete: (String) -> Unit
) {
    val user = auth.currentUser ?: return
    val storageRef = storage.reference.child("profile_images/${user.uid}.jpg")

    storageRef.putFile(uri)
        .addOnSuccessListener {
            storageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                firestore.collection("users").document(user.uid)
                    .update("profileImageUrl", downloadUrl.toString())
                    .addOnSuccessListener {
                        onUploadComplete(downloadUrl.toString())
                    }
            }
        }
        .addOnFailureListener { e ->
            println("Failed to upload image: ${e.message}")
        }
}

// function to format the time for the connection
fun formatTime(totalMilliseconds: Long): String {
    val totalSeconds = totalMilliseconds / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    return "${hours}h ${minutes}m"
}

// function to update username
fun updateUsername(
    auth: FirebaseAuth,
    firestore: FirebaseFirestore,
    newUsername: String,
    onUpdateSuccess: () -> Unit,
    onUpdateFail: () -> Unit
) {
    val user = auth.currentUser ?: return
    firestore.collection("users").document(user.uid)
        .update("username", newUsername)
        .addOnSuccessListener { onUpdateSuccess() }
        .addOnFailureListener { onUpdateFail() }
}

// function to update email in Firebase Authentication and Firestore
fun updateEmail(
    auth: FirebaseAuth,
    firestore: FirebaseFirestore,
    newEmail: String,
    onUpdateSuccess: () -> Unit,
    onUpdateFail: (String) -> Unit
) {
    val user = auth.currentUser
    if (user != null) {
        user.updateEmail(newEmail) // update in Firebase Authentication
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    firestore.collection("users").document(user.uid)
                        .update("email", newEmail)
                        .addOnSuccessListener {
                            println("Email updated successfully in Firestore")
                            onUpdateSuccess()
                        }
                        .addOnFailureListener { e ->
                            println("Failed to update email in Firestore: ${e.message}")
                            onUpdateFail("Failed to update email in Firestore: ${e.message}")
                        }
                } else {
                    println("Failed to update email in Firebase Authentication: ${task.exception?.message}")
                    onUpdateFail("Firebase Authentication Error: ${task.exception?.message}")
                }
            }
            .addOnFailureListener { e ->
                println("Error updating email in Firebase Authentication: ${e.message}")
                onUpdateFail("Firebase Authentication Error: ${e.message}")
            }
    } else {
        onUpdateFail("No authenticated user found.")
    }
}

// function to send an email verification when the user try to change his email address
fun sendVerificationEmail(auth: FirebaseAuth, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
    val user = auth.currentUser
    user?.sendEmailVerification()
        ?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                println("Verification email sent.")
                onSuccess()
            } else {
                println("Failed to send verification email: ${task.exception?.message}")
                onFailure(task.exception?.message ?: "Unknown error")
            }
        }
}

// function to log out the application
fun onLogout(auth: FirebaseAuth, firestore: FirebaseFirestore, onSuccess: () -> Unit) {
    val user = auth.currentUser
    if (user != null) {
        val logoutTimestamp = System.currentTimeMillis()

        // retrieve the loginTimestamp and calculate session duration
        firestore.collection("users").document(user.uid).get()
            .addOnSuccessListener { document ->
                val loginTimestamp = document.getLong("loginTimestamp") ?: logoutTimestamp
                val sessionTime = logoutTimestamp - loginTimestamp // calculation of the session duration

                // add session time to totalTime in Firestore
                firestore.collection("users").document(user.uid)
                    .update("totalTime", FieldValue.increment(sessionTime))
                    .addOnSuccessListener {
                        println("DEBUG: Total time updated successfully.")
                        auth.signOut() // disconnect the user
                        onSuccess() // navigate or display a message after disconnection
                    }
                    .addOnFailureListener { e ->
                        println("DEBUG: Failed to update total time: ${e.message}")
                    }
            }
            .addOnFailureListener { e ->
                println("DEBUG: Failed to retrieve login timestamp: ${e.message}")
            }
    } else {
        println("DEBUG: No authenticated user found.")
        auth.signOut()
        onSuccess()
    }
}