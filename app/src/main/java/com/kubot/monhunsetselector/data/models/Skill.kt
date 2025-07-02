package com.kubot.monhunsetselector.data.models

import android.os.Bundle
import android.os.Parcelable
import com.google.firebase.firestore.PropertyName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Skill(
    var id: String = "",

    // Use the annotation to map the Firestore field "Name" to this property.
    @get:PropertyName("Name") @set:PropertyName("Name")
    var name: String = "",

    // Do the same for all other fields that have a case mismatch.
    @get:PropertyName("Description") @set:PropertyName("Description")
    var description: String = "",

    // The 'details' bundle will still hold everything else,
    // but it's good practice to map the primary fields you use often.
    var details: Bundle = Bundle()

) : Parcelable {
    // A no-argument constructor is needed for Firestore, which data classes provide.
    // If you add other properties without default values, you might need to add it explicitly:
    // constructor() : this("", "", "", Bundle())
}