package com.example.quickcook_project.authentications

import android.annotation.SuppressLint
import android.content.Intent
import androidx.activity.ComponentActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import at.favre.lib.crypto.bcrypt.BCrypt
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.GoogleAuthProvider
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import androidx.compose.ui.res.painterResource
import com.example.quickcook_project.R
import com.example.quickcook_project.screens.MainActivity


class LoginActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance() // initialize firebase auth
        firestore = FirebaseFirestore.getInstance() // initialize firestore

        // configure Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.client_id)) // ID client Web OAuth 2.0
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        setContent {
            LoginScreen(
                onForgotPasswordClick = {
                    // handle redirection to Forgot Password screen
                    val intent = Intent(this@LoginActivity, ForgotPasswordActivity::class.java)
                    startActivity(intent)
                },
                onSignUpClick = {
                    // redirect to Sign In page
                    val intent = Intent(this@LoginActivity, SigninActivity::class.java)
                    startActivity(intent)
                },
                onGoogleSignIn = { signInWithGoogle() }, // call the Google Signin method
                auth = auth // auth passed as parameter
            )
        }
    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
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
                    val user = auth.currentUser
                    val userId = user?.uid
                    if (userId != null) {
                        firestore.collection("users").document(userId).get()
                            .addOnSuccessListener { document ->
                                if (!document.exists()) {
                                    // add user into firestore if doesn't exist
                                    val userData = mapOf(
                                        "username" to (user.displayName ?: "Google User"),
                                        "email" to user.email
                                    )
                                    firestore.collection("users").document(userId).set(userData)
                                        .addOnSuccessListener {
                                            Toast.makeText(
                                                this,
                                                "Google user added to Firestore",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            val intent = Intent(this, MainActivity::class.java)
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
                                        "Welcome back, ${user.displayName}!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    val intent = Intent(this, MainActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                }
                            }
                    }
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
fun LoginScreen(
    onForgotPasswordClick: () -> Unit,
    onSignUpClick: () -> Unit,
    onGoogleSignIn: () -> Unit,
    auth: FirebaseAuth
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val firestore = FirebaseFirestore.getInstance()
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
            // log in title
            Text(
                text = "Log In",
                color = Color(0xFF7F3C3C),
                fontSize = 32.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // email field
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
                TextField(
                    value = email,
                    onValueChange = { email = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = TextFieldDefaults.textFieldColors(
                        backgroundColor = Color.Transparent,
                        textColor = Color.White,
                        cursorColor = Color.White
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // password field
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
                TextField(
                    value = password,
                    onValueChange = { password = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    colors = TextFieldDefaults.textFieldColors(
                        backgroundColor = Color.Transparent,
                        textColor = Color.White,
                        cursorColor = Color.White
                    )
                )
            }

            // forgot password field
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Forgot password?",
                color = Color.White,
                fontSize = 14.sp,
                modifier = Modifier.clickable(onClick = onForgotPasswordClick)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // login button
            Button(
                onClick = {
                    // user verification into firebase authentication
                    auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val userId = task.result?.user?.uid
                                if (userId != null) {
                                    // retrieve other user data form firestore (username...)
                                    firestore.collection("users").document(userId).get()
                                        .addOnSuccessListener { document ->
                                            if (document != null && document.exists()) {
                                                val username = document.getString("username") ?: "User"
                                                Toast.makeText(
                                                    context,
                                                    "Welcome back, $username!",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                // redirect to MainActivity
                                                val intent = Intent(context, MainActivity::class.java)
                                                context.startActivity(intent)
                                            } else {
                                                Toast.makeText(
                                                    context,
                                                    "User data not found in Firestore!",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                        .addOnFailureListener { e ->
                                            Toast.makeText(
                                                context,
                                                "Error retrieving user data: ${e.message}",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                }
                            } else {
                                Toast.makeText(context, "Invalid email or password", Toast.LENGTH_SHORT).show()
                            }
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                },
                modifier = Modifier.width(200.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF7F3C3C))
            ) {
                Text(text = "Log In", color = Color.White)
            }

            // sign Up link
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Don't have an account? Sign Up",
                color = Color.White,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.clickable {
                    val intent = Intent(context, SigninActivity::class.java)
                    context.startActivity(intent)
                }
            )

            // google Sign-In Button
            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                IconButton(
                    onClick = onGoogleSignIn,
                    modifier = Modifier
                        .size(72.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_google_logo),
                        contentDescription = "Google Log-In",
                        tint = Color.Unspecified
                    )
                }
            }
        }
    }
}
