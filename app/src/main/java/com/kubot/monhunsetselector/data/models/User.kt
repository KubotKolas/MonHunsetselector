package com.kubot.monhunsetselector.data.models

data class User(
    val uid: String,          // The unique Firebase Auth UID (e.g., "steam:7656...")
    val steamId: String,      // The 64-bit Steam ID
    val displayName: String,  // The user's public Steam name
    val avatarUrl: String     // The URL for their profile picture
)