package com.example.quickcook_project.authentications

import androidx.activity.ComponentActivity
import android.content.Intent
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.VisualTransformation
import at.favre.lib.crypto.bcrypt.BCrypt
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ForgotPasswordActivity : ComponentActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private var generatedCode: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ForgotPasswordScreen()
        }
    }

    @Composable
    fun ForgotPasswordScreen() {
        val context = LocalContext.current

        var email by remember { mutableStateOf("") }
        var code by remember { mutableStateOf("") }
        var newPassword by remember { mutableStateOf("") }
        var confirmPassword by remember { mutableStateOf("") }

        var step by remember { mutableStateOf(1) }

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
                    text = "Change Password",
                    color = Color(0xFF7F3C3C),
                    fontSize = 32.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // step 1 : ask for the email
                if (step == 1) {
                    CustomInputField(
                        label = "Email",
                        value = email,
                        onValueChange = { email = it }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    CustomButton("Confirm") {
                        if (email.isNotEmpty()) {
                            val verificationCode = (100000..999999).random().toString() // generate a random code
                            generatedCode = verificationCode

                            // Debugging : Log to verify the state of the code
                            println("Generated Code: $generatedCode")

                            // send the code by email
                            CoroutineScope(Dispatchers.IO).launch {
                                val emailSent =
                                    EmailService.sendVerificationCode(email, verificationCode)
                                withContext(Dispatchers.Main) {
                                    if (emailSent) {
                                        Toast.makeText(context, "Code sent to $email", Toast.LENGTH_SHORT).show()
                                        step = 2
                                    } else {
                                        Toast.makeText(context, "Failed to send code. Try again.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        } else {
                            Toast.makeText(context, "Please enter a valid email address.", Toast.LENGTH_SHORT).show()
                        }

                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    ReturnToLogin()
                }

                // step 2 : code verification
                if (step == 2) {
                    CustomInputField(
                        label = "Code",
                        value = code,
                        onValueChange = { code = it }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    CustomButton("Confirm") {
                        if (code == generatedCode) {
                            Toast.makeText(context, "Code verified!", Toast.LENGTH_SHORT).show()
                            step = 3
                        } else {
                            Toast.makeText(context, "Invalid code!", Toast.LENGTH_SHORT).show()
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    ReturnToLogin()
                }

                // step 3 : change password
                if (step == 3) {
                    CustomInputField(
                        label = "New Password",
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        isPassword = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    CustomInputField(
                        label = "Confirm Password",
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        isPassword = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    CustomButton("Confirm") {
                        if (newPassword == confirmPassword) {
                            updatePassword(email, newPassword) {
                                Toast.makeText(context, "Password updated!", Toast.LENGTH_SHORT).show()
                                val intent = Intent(context, LoginActivity::class.java)
                                context.startActivity(intent)
                            }
                        } else {
                            Toast.makeText(context, "Passwords do not match!", Toast.LENGTH_SHORT).show()
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    ReturnToLogin()
                }
            }
        }
    }

    @Composable
    fun ReturnToLogin() {
        val context = LocalContext.current
        Text(
            text = "Do you remember your password?",
            color = Color.White,
            fontSize = 14.sp,
            modifier = Modifier.clickable {
                val intent = Intent(context, LoginActivity::class.java)
                context.startActivity(intent)
            }
        )
    }

    @Composable
    fun CustomInputField(
        label: String,
        value: String,
        onValueChange: (String) -> Unit,
        isPassword: Boolean = false
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(Color.Gray, shape = RoundedCornerShape(24.dp))
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            if (value.isEmpty()) {
                Text(text = label, color = Color.White.copy(alpha = 0.6f))
            }
            TextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
                colors = TextFieldDefaults.textFieldColors(
                    backgroundColor = Color.Transparent,
                    textColor = Color.White,
                    cursorColor = Color.White
                )
            )
        }
    }

    @Composable
    fun CustomButton(text: String, onClick: () -> Unit) {
        Button(
            onClick = onClick,
            modifier = Modifier.width(200.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF7F3C3C))
        ) {
            Text(text = text, color = Color.White)
        }
    }

    // update password
    private fun updatePassword(email: String, newPassword: String, onComplete: () -> Unit) {
        firestore.collection("users").whereEqualTo("email", email).get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    firestore.collection("users").document(document.id)
                        .update("password", BCrypt.withDefaults().hashToString(12, newPassword.toCharArray()))
                        .addOnSuccessListener { onComplete() }
                }
            }
    }
}