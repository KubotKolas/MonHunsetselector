package com.kubot.monhunsetselector

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.kubot.monhunsetselector.auth.AuthManager
import com.kubot.monhunsetselector.auth.AuthState
import com.kubot.monhunsetselector.navigation.AppNavigation
import com.kubot.monhunsetselector.ui.theme.MonHunSetSelectorTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val authManager = AuthManager()
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        var keepSplashOnScreen = true
        splashScreen.setKeepOnScreenCondition { keepSplashOnScreen }

        lifecycleScope.launch {
            authManager.authState.collect { authState ->

                if (authState != AuthState.UNKNOWN) {
                    keepSplashOnScreen = false
                }
            }
        }

        val uri = intent?.data
        if (uri != null) {
            lifecycleScope.launch {
                val success = authManager.handleAuthRedirect(uri)
                if (success) {
                    Log.d("LOGIN", "onCreate: Login passed")
                }
            }
        }

        setContent {
            MonHunSetSelectorTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(authManager)
                }
            }
        }
    }
}

