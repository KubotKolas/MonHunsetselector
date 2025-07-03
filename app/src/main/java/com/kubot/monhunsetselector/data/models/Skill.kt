package com.kubot.monhunsetselector.data.models

import android.os.Bundle
import android.os.Parcelable
import com.google.firebase.firestore.PropertyName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Skill(
    var id: String = "",


    @get:PropertyName("Name") @set:PropertyName("Name")
    var name: String = "",


    @get:PropertyName("Description") @set:PropertyName("Description")
    var description: String = "",


    var details: Bundle = Bundle()

) : Parcelable