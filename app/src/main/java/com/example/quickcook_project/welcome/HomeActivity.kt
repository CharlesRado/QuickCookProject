package com.example.quickcook_project.welcome

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import com.example.quickcook_project.authentications.LoginActivity
import com.example.quickcook_project.R

class HomeActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HomeScreen()
        }
    }
}

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun HomeScreen() {
    val context = LocalContext.current // Pour rediriger vers une autre activité

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFE2D9D9)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo (Assure-toi d'avoir un logo dans le dossier drawable avec le nom "ic_logo")
            Image(
                painter = painterResource(id = R.drawable.ic_quickcook_logo), // Remplace avec le nom réel de ton logo
                contentDescription = "Logo",
                modifier = Modifier
                    .size(300.dp) // Taille du logo
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Bouton Get Started
            Button(
                onClick = {
                    val intent = Intent(context, LoginActivity::class.java)
                    context.startActivity(intent)
                },
                modifier = Modifier
                    .width(200.dp)
                    .height(48.dp),
                shape = RoundedCornerShape(24.dp), // Boutons arrondis
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF7F3C3C)) // Couleur du bouton : #7F3C3C
            ) {
                Text(
                    text = "Get started !",
                    color = Color.White, // Texte en blanc
                    style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
