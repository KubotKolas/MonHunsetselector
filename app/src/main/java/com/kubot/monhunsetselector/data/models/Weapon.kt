package com.kubot.monhunsetselector.data.models

import android.os.Bundle
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Weapon(
    val id: String = "",
    var type: String = "", // e.g., "GS", "CB". Set this programmatically.
    val name: String = "",
    // This map holds ALL other columns from the Firestore document.
    // Using <String, Any> handles both String and Number types from Firestore.
//    val allStats: Map<String, Any> = emptyMap()
    var allStats: Bundle = Bundle()
) : Parcelable