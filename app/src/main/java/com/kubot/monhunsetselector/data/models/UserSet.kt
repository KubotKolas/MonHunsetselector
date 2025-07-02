package com.kubot.monhunsetselector.data.models

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class UserSet(
    // @DocumentId tells Firestore to automatically fill this with the document's ID
    // when you fetch the data. This is the best way to handle the 'id'.
    @DocumentId
    var id: String = "",

    // The set needs a name for the user to identify it.
    var name: String = "My New Set",

    // --- All equipment IDs are nullable to allow for incomplete sets ---
    var weaponId: String? = null,
    var headId: String? = null,
    var chestId: String? = null,
    var armsId: String? = null,
    var waistId: String? = null,
    var legsId: String? = null,

    var headName: String? = null,
    var chestName: String? = null,
    var armsName: String? = null,
    var waistName: String? = null,
    var legsName: String? = null,

    // --- Denormalized data for fast display on the list screen ---
    var weaponName: String? = null,
    var weaponType: String? = null,

    // @ServerTimestamp tells Firestore to automatically set this field
    // to the server's time on every write. Perfect for sorting.
    @ServerTimestamp
    val lastModified: Date? = null
) {
    // A no-argument constructor is required by Firestore for deserialization.
    // Kotlin data classes with default values for all properties get this for free,
    // but it's good practice to be aware of it.
}