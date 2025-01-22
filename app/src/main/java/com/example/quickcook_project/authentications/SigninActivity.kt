package com.example.quickcook_project.authentications

import android.annotation.SuppressLint
import androidx.activity.ComponentActivity
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.ui.platform.LocalContext
import at.favre.lib.crypto.bcrypt.BCrypt
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import com.example.quickcook_project.R
import com.example.quickcook_project.screens.MainActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class SigninActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance() // initialize firebase auth
        firestore = FirebaseFirestore.getInstance() // initialize firestore

        // configure Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.client_id)) // ID of client Web OAuth 2.0
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        setContent {
            SignInScreen(onLoginClick = {
                val intent = Intent(this@SigninActivity, LoginActivity::class.java)
                startActivity(intent)
            },
            onConfirmClick = {
                val intent = Intent(this@SigninActivity, LoginActivity::class.java)
                startActivity(intent)
            },
            onGoogleSignIn = { signInWithGoogle() }, // call the Google Signin method
                auth = auth, // auth passed as parameter
                firestore = firestore // firestore passed as parameter
            )
        }
    }

    private fun createUser(email: String, password: String, username: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = task.result?.user?.uid ?: return@addOnCompleteListener

                    // Hash the password
                    val hashedPassword = BCrypt.withDefaults().hashToString(12, password.toCharArray())

                    // Save user data in Firestore
                    val userData = mapOf(
                        "username" to username,
                        "email" to email,
                        "password" to hashedPassword // Storing hashed password
                    )
                    firestore.collection("users").document(uid).set(userData)
                        .addOnSuccessListener {
                            Toast.makeText(
                                this,
                                "User registered successfully!",
                                Toast.LENGTH_SHORT
                            ).show()
                            val intent = Intent(this, LoginActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(
                                this,
                                "Failed to save user in Firestore: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                } else {
                    Toast.makeText(
                        this,
                        "Authentication failed: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private  fun signInWithGoogle() {
        googleSignInClient.signOut().addOnCompleteListener {
            // end of disconnection / google Sign-In proposed
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account.idToken
                firebaseAuthWithGoogle(idToken)
            } catch (e: ApiException) {
                Toast.makeText(this, "Google sign-in failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String?) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Sign-up successful with Google!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Authentication failed", Toast.LENGTH_SHORT).show()
                }
            }
    }

    companion object {
        private const val RC_SIGN_IN = 9001
    }
}

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun SignInScreen(
    onLoginClick: () -> Unit,
    onConfirmClick: () -> Unit,
    onGoogleSignIn: () -> Unit,
    auth: FirebaseAuth,
    firestore: FirebaseFirestore
) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    val context = LocalContext.current

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Black
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Sign Up",
                color = Color(0xFF7F3C3C),
                fontSize = 32.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(Color.Gray, shape = RoundedCornerShape(24.dp))
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (username.isEmpty()) {
                    Text(text = "Username", color = Color.White.copy(alpha = 0.6f))
                }
                // Username Field
                TextField(
                    value = username,
                    onValueChange = {username = it},
                    modifier = Modifier .fillMaxWidth(),
                    singleLine = true,
                    colors = TextFieldDefaults.textFieldColors(
                        backgroundColor = Color.Transparent,
                        textColor = Color.White,
                        cursorColor = Color.White
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(Color.Gray, shape = RoundedCornerShape(24.dp))
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (email.isEmpty()) {
                    Text(text = "Email", color = Color.White.copy(alpha = 0.6f))
                }

                // Email Field
                TextField(
                    value = email,
                    onValueChange = { email = it },
                    modifier = Modifier .fillMaxWidth(),
                    singleLine = true,
                    colors = TextFieldDefaults.textFieldColors(
                        backgroundColor = Color.Transparent,
                        textColor = Color.White,
                        cursorColor = Color.White
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(Color.Gray, shape = RoundedCornerShape(24.dp))
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (password.isEmpty()) {
                    Text(text = "Password", color = Color.White.copy(alpha = 0.6f))
                }

                // Password Field
                TextField(
                    value = password,
                    onValueChange = {password = it},
                    modifier = Modifier .fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    colors = TextFieldDefaults.textFieldColors(
                        backgroundColor = Color.Transparent,
                        textColor = Color.White,
                        cursorColor = Color.White
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(Color.Gray, shape = RoundedCornerShape(24.dp))
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (confirmPassword.isEmpty()) {
                    Text(text = "Confirm Password", color = Color.White.copy(alpha = 0.6f))
                }

                // Confirm Password Field
                TextField(
                    value = confirmPassword,
                    onValueChange = {confirmPassword = it},
                    modifier = Modifier .fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    colors = TextFieldDefaults.textFieldColors(
                        backgroundColor = Color.Transparent,
                        textColor = Color.White,
                        cursorColor = Color.White
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Confirm button
            Button(
                onClick = {
                    if (password.length < 6){
                        Toast.makeText(context, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                    } else if (password == confirmPassword) {
                        // Hash the password
                        val hashedPassword = BCrypt.withDefaults().hashToString(12, password.toCharArray())

                        // Create user in Firebase Authentication
                        auth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    val uid = task.result?.user?.uid ?: return@addOnCompleteListener

                                    // Add user to Firestore
                                    val user = mapOf(
                                        "username" to username,
                                        "email" to email,
                                        "password" to hashedPassword // Save hashed password
                                    )
                                    firestore.collection("users").document(uid)
                                        .set(user)
                                        .addOnSuccessListener {
                                            Toast.makeText(context, "User registered successfully!", Toast.LENGTH_SHORT).show()
                                            // Redirect to LoginActivity
                                            val intent = Intent(context, LoginActivity::class.java)
                                            context.startActivity(intent)
                                        }
                                        .addOnFailureListener { e ->
                                            Toast.makeText(context, "Error saving user to Firestore: ${e.message}", Toast.LENGTH_SHORT).show()
                                        }
                                } else {
                                    Toast.makeText(context, "Error creating user: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                    } else {
                        Toast.makeText(context, "Passwords do not match!", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.width(200.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF7F3C3C))
            ) {
                Text(text = "Confirm", color = Color.White)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Redirect to Log In
            TextButton(onClick = {
                val intent = Intent(context, LoginActivity::class.java)
                context.startActivity(intent)
            }) {
                Text(
                    text = "You already have an account? Log In",
                    color = Color.White
                )
            }

            // Google Sign-In Button
            Spacer(modifier = Modifier.height(16.dp))

            IconButton(
                onClick = onGoogleSignIn,
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_google_logo),
                    contentDescription = "Google Sign-Up",
                    tint = Color.Unspecified
                )
            }
        }
    }
}
