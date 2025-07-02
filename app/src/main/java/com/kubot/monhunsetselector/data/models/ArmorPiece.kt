package com.kubot.monhunsetselector.data.models

import android.os.Bundle
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/// Enum to represent the armor slot. This helps with logic in the app.
@Parcelize
data class ArmorPiece(
    val id: String = "",
    var type: ArmorType = ArmorType.HELMS, // Set this programmatically.
    val name: String = "",
    // This map holds ALL other columns.
    var allStats: Bundle = Bundle()
) : Parcelable

enum class ArmorType { HELMS, CHEST, ARMS, WAIST, LEGS }
