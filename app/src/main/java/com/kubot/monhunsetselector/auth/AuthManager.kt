package com.kubot.monhunsetselector.auth

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.kubot.monhunsetselector.data.models.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await

enum class AuthState {
    UNKNOWN,
    AUTHENTICATED,
    UNAUTHENTICATED
}

class AuthManager {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val presistSteamLogin: Boolean = true


    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser


    private val _authState = MutableStateFlow(AuthState.UNKNOWN)
    val authState: StateFlow<AuthState> = _authState

    init {
        val initialUser = auth.currentUser
        if (initialUser != null) {


            println("AUTH_CACHE: Found cached user on init: ${initialUser.uid}")
            parseUserFromFirebase(initialUser)
            _authState.value = AuthState.AUTHENTICATED
        } else {

            println("AUTH_CACHE: No cached user found on init.")
            _authState.value = AuthState.UNAUTHENTICATED
        }



        auth.addIdTokenListener { firebaseAuth: FirebaseAuth ->
            val firebaseUser = firebaseAuth.currentUser
            println("AUTH_CACHE: IdTokenListener fired. User is: ${firebaseUser?.uid}")
            if (firebaseUser == null) {

                if (_authState.value != AuthState.UNAUTHENTICATED) {
                    _currentUser.value = null
                    _authState.value = AuthState.UNAUTHENTICATED
                }
            } else {

                if (_authState.value != AuthState.AUTHENTICATED) {
                    parseUserFromFirebase(firebaseUser)
                    _authState.value = AuthState.AUTHENTICATED
                }
            }
        }
    }

    private fun parseUserFromFirebase(firebaseUser: FirebaseUser) {


        firebaseUser.getIdToken(false)
            .addOnSuccessListener { result ->
                val claims = result.claims
                _currentUser.value = User(
                    uid = firebaseUser.uid,
                    steamId = claims["steam_id"] as? String ?: "",
                    displayName = claims["name"] as? String ?: "Cached User",
                    avatarUrl = claims["picture"] as? String ?: ""
                )
            }.addOnFailureListener {


                println("AUTH_CACHE: Failed to get token claims, user may be offline. Uid: ${firebaseUser.uid}")
            }
    }

    fun startSteamLogin(context: Context) {


        val returnToUrl = "https://verifysteam-o7vztoop6q-ew.a.run.app/verifySteam/"

        val steamLoginUrl = "https://steamcommunity.com/openid/login?" +
                "openid.claimed_id=http://specs.openid.net/auth/2.0/identifier_select&" +
                "openid.identity=http://specs.openid.net/auth/2.0/identifier_select&" +
                "openid.mode=checkid_setup&" +
                "openid.ns=http://specs.openid.net/auth/2.0&" +
                "openid.realm=$returnToUrl&" +
                "openid.return_to=$returnToUrl"


        val builder = CustomTabsIntent.Builder()
        val customTabsIntent = builder.build()

        if (!presistSteamLogin) {
            customTabsIntent.intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
        }
        customTabsIntent.launchUrl(context, Uri.parse(steamLoginUrl))
    }

    suspend fun handleAuthRedirect(uri: Uri): Boolean {

        val error = uri.getQueryParameter("error")
        if (error != null) {
            println("Auth Error: $error")
            return false
        }


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


}