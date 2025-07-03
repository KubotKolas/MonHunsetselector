package com.kubot.monhunsetselector.data.models

import android.os.Bundle
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Weapon(
    val id: String = "",
    var type: String = "",
    val name: String = "",
    var allStats: Bundle = Bundle()
) : Parcelable