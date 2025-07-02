package com.kubot.monhunsetselector.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.kubot.monhunsetselector.R
import com.kubot.monhunsetselector.auth.AuthManager

@Composable
fun LoginScreen(navController: NavController, aM: AuthManager) {
    // We will need the context to launch the browser tab
    val context = LocalContext.current
    val authManager = aM

    // In a real app, you'd add a check here:
    // if (authManager.isUserLoggedIn()) {
    //     navController.navigate(Screen.MySets.route) { popUpTo(Screen.Login.route) { inclusive = true } }
    // }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.mhwilds_logo),
            contentDescription = "App Logo",
            modifier = Modifier.size(240.dp) // A slightly smaller logo for this screen
        )

        Spacer(modifier = Modifier.height(24.dp))
        Text("Welcome to MonHun Wilds Set Selector", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = {
                // This is where the magic starts
                authManager.startSteamLogin(context)
            }
        ) {
            Text("Login with Steam")
        }
    }
}