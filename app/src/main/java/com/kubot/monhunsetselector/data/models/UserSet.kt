package com.kubot.monhunsetselector.data.models

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class UserSet(


    @DocumentId
    var id: String = "",


    var name: String = "My New Set",


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


    var weaponName: String? = null,
    var weaponType: String? = null,


    @ServerTimestamp
    val lastModified: Date? = null
)