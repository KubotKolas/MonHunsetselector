package com.kubot.monhunsetselector.data.models

import android.os.Bundle
import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class ArmorPiece(
    val id: String = "",
    var type: ArmorType = ArmorType.HELMS,
    val name: String = "",

    var allStats: Bundle = Bundle()
) : Parcelable

enum class ArmorType { HELMS, CHEST, ARMS, WAIST, LEGS }
