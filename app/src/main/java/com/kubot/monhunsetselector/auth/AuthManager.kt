package com.kubot.monhunsetselector.auth

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.kubot.monhunsetselector.data.models.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await

enum class AuthState {
    UNKNOWN, // Still checking
    AUTHENTICATED,
    UNAUTHENTICATED
}

class AuthManager {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val presistSteamLogin: Boolean = true

    // This StateFlow will hold the current user state and notify the UI of changes.
    // It's nullable because the user can be logged out.
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    // --- NEW: State to track the auth check progress ---
    private val _authState = MutableStateFlow(AuthState.UNKNOWN)
    val authState: StateFlow<AuthState> = _authState

    init {
        val initialUser = auth.currentUser
        if (initialUser != null) {
            // A user is cached! Immediately parse them and set the state.
            // This happens instantly on app start, before any listeners fire.
            println("AUTH_CACHE: Found cached user on init: ${initialUser.uid}")
            parseUserFromFirebase(initialUser)
            _authState.value = AuthState.AUTHENTICATED
        } else {
            // No user is cached. We know for sure they are unauthenticated.
            println("AUTH_CACHE: No cached user found on init.")
            _authState.value = AuthState.UNAUTHENTICATED
        }

        // 2. The listener is still crucial for handling REAL-TIME changes
        //    (like a successful login, a logout, or an automatic token refresh).
        auth.addIdTokenListener { firebaseAuth :FirebaseAuth ->
            val firebaseUser = firebaseAuth.currentUser
            println("AUTH_CACHE: IdTokenListener fired. User is: ${firebaseUser?.uid}")
            if (firebaseUser == null) {
                // This handles logout.
                if (_authState.value != AuthState.UNAUTHENTICATED) {
                    _currentUser.value = null
                    _authState.value = AuthState.UNAUTHENTICATED
                }
            } else {
                // This handles login and token refreshes.
                if (_authState.value != AuthState.AUTHENTICATED) {
                    parseUserFromFirebase(firebaseUser)
                    _authState.value = AuthState.AUTHENTICATED
                }
            }
        }
    }

    private fun parseUserFromFirebase(firebaseUser: FirebaseUser) {
        // This function now needs to be slightly more robust for offline use.
        // It should prioritize claims already present in the cached token.
        firebaseUser.getIdToken(false) // Use 'false' to not force a network refresh
            .addOnSuccessListener { result ->
                val claims = result.claims
                _currentUser.value = User(
                    uid = firebaseUser.uid,
                    steamId = claims["steam_id"] as? String ?: "",
                    displayName = claims["name"] as? String ?: "Cached User",
                    avatarUrl = claims["picture"] as? String ?: ""
                )
            }.addOnFailureListener {
                // This might happen offline if the token is very old.
                // It's often okay to just let the user stay logged in with stale data.
                println("AUTH_CACHE: Failed to get token claims, user may be offline. Uid: ${firebaseUser.uid}")
            }
    }

    fun startSteamLogin(context: Context) {
        // IMPORTANT: This URL MUST point to your deployed Firebase Cloud Function.
        // For local testing, you might use a tool like ngrok, but for production,
        // it will be the URL provided by Firebase after you deploy the function.
        val returnToUrl = "https://verifysteam-o7vztoop6q-ew.a.run.app/verifySteam/"

        val steamLoginUrl = "https://steamcommunity.com/openid/login?" +
                "openid.claimed_id=http://specs.openid.net/auth/2.0/identifier_select&" +
                "openid.identity=http://specs.openid.net/auth/2.0/identifier_select&" +
                "openid.mode=checkid_setup&" +
                "openid.ns=http://specs.openid.net/auth/2.0&" +
                "openid.realm=$returnToUrl&" + // Realm and return_to should match for security
                "openid.return_to=$returnToUrl"

        // Use Chrome Custom Tabs for a better, more secure login experience
        val builder = CustomTabsIntent.Builder()
        val customTabsIntent = builder.build()

        if(!presistSteamLogin){
            customTabsIntent.intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
        }
        customTabsIntent.launchUrl(context, Uri.parse(steamLoginUrl))
    }

    suspend fun handleAuthRedirect(uri: Uri): Boolean {
        // Check for an error first
        val error = uri.getQueryParameter("error")
        if (error != null) {
            println("Auth Error: $error")
            return false
        }

        // Get the token
        val token = uri.getQueryParameter("token")
        return if (token != null) {
            signInWithCustomToken(token)
        } else {
            false
        }
    }

    private suspend fun signInWithCustomToken(token: String): Boolean {
        return try {
            auth.signInWithCustomToken(token).await()
            println("Successfully signed in with custom token. User: ${auth.currentUser?.uid}")
            true
        } catch (e: Exception) {
            println("Error signing in with custom token: $e")
            false
        }
    }

    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    fun logout() {
        auth.signOut()
    }

    // You will add more functions here later, like:
    // fun signInWithCustomToken(token: String) { ... }
    // fun logout() { ... }
}