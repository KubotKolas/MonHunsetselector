package com.kubot.monhunsetselector.data.repository

import android.os.Bundle
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.kubot.monhunsetselector.data.models.ArmorPiece
import com.kubot.monhunsetselector.data.models.ArmorType
import com.kubot.monhunsetselector.data.models.Skill
import com.kubot.monhunsetselector.data.models.Weapon
import kotlinx.coroutines.tasks.await

class GameDataRepository {

    private val db = Firebase.firestore

    // Maps the user-friendly dropdown names to the actual Firestore collection names
    private val weaponCollectionMap = mapOf(
        "Great Sword" to "GS",
        "Long Sword" to "LS",
        "Sword & Shield" to "SNS",
        "Dual Blades" to "DB",
        "Hammer" to "HM",
        "Hunting Horn" to "HH",
        "Lance" to "LA",
        "Gunlance" to "GL",
        "Switch Axe" to "SA",
        "Charge Blade" to "CB",
        "Insect Glaive" to "IG",
        "Light Bowgun" to "LBG",
        "Heavy Bowgun" to "HBG",
        "Bow" to "BO"
    )

    private fun mapToBundle(map: Map<String, Any?>): Bundle {
        val bundle = Bundle()
        for ((key, value) in map) {
            if (value == null) {
                // If the value is null, you can either skip it or add it as an empty string.
                // Adding an empty string is often safer for the UI layer.
                bundle.putString(key, "")
                continue // Go to the next item in the loop
            }
            when (value) {
                is String -> bundle.putString(key, value)
                is Long -> bundle.putLong(key, value) // Firestore often uses Long for integers
                is Int -> bundle.putInt(key, value)
                is Double -> bundle.putDouble(key, value)
                is Boolean -> bundle.putBoolean(key, value)
                // Add other types as needed
                else -> bundle.putString(key, value.toString()) // Fallback to String
            }
        }
        return bundle
    }

    suspend fun getEquipment(mainCategory: String, subCategory: String): List<Any> {
        return try {
            when (mainCategory) {
                "Armor" -> fetchArmor(subCategory)
                "Weapons" -> fetchWeapons(subCategory)
                "Skills" -> fetchSkills()
                else -> emptyList()
            }
        } catch (e: Exception) {
            // Log the error and return an empty list so the app doesn't crash
            println("Error fetching equipment: $e")
            emptyList()
        }
    }

    private suspend fun fetchArmor(armorType: String): List<ArmorPiece> {
        val collectionName = armorType.uppercase()

//        println("Attempting to fetch armor from collection: '$collectionName'")

        val snapshot = db.collection(collectionName).get().await()

//        println("Query for '$collectionName' returned ${snapshot.size()} documents.")

        return snapshot.documents.mapNotNull { document ->
            document.data?.let { data ->
                val armorName = data["Name"] as? String ?: "Unknown"
                val mappedType = mapStringToArmorType(armorType)
                if (armorName == "G Doshaguma Helm Alpha") { // Pick a specific armor piece to check
                    println("DEBUG_REPO: Creating ArmorPiece: name='$armorName', type='$mappedType'")
                }
                ArmorPiece(
                    id = document.id,
                    name = data["Name"] as? String ?: "Unknown", // IMPORTANT: Match capitalization
                    type = mapStringToArmorType(armorType),
                    allStats = mapToBundle(data)
                )
            }
        }
    }

    private suspend fun fetchWeapons(weaponType: String): List<Weapon> {
        val collectionName = weaponCollectionMap[weaponType] ?: return emptyList()
        val snapshot = db.collection(collectionName).get().await()

        return snapshot.documents.mapNotNull { document ->
            document.data?.let { data ->
                Weapon(
                    id = document.id,
                    name = data["Name"] as? String ?: "Unknown",
                    type = weaponType, // Use the user-friendly name
                    allStats = mapToBundle(data)
                )
            }
        }
    }

    private suspend fun fetchSkills(): List<Skill> {
        val snapshot = db.collection("SKILLS").get().await() // Direct collection name
        return snapshot.documents.mapNotNull { document ->
            document.data?.let { data ->
                Skill(
                    id = document.id,
                    name = data["Name"] as? String ?: "Unknown",
                    description = data["Description"] as? String ?: "",
                    details = mapToBundle(data)
                )
            }
        }
    }

    // Helper to convert the dropdown string to the ArmorType enum
    private fun mapStringToArmorType(type: String): ArmorType {
        return when (type) {
            "Helms" -> ArmorType.HELMS
            "Chest" -> ArmorType.CHEST
            "Arms" -> ArmorType.ARMS
            "Waist" -> ArmorType.WAIST
            "Legs" -> ArmorType.LEGS
            else -> ArmorType.HELMS // A sensible default
        }
    }

    // --- NEW METHODS ---

    /**
     * Fetches a single weapon document by its ID from the correct collection.
     */
    suspend fun getWeaponById(weaponId: String, weaponType: String?): Weapon? {
        // Find the correct Firestore collection name from the weapon's type
        val collectionName = weaponCollectionMap[weaponType] ?: return null

        return try {
            val document = db.collection(collectionName).document(weaponId).get().await()
            document.toObject(Weapon::class.java)?.apply {
                // Manually set the type and map stats, since toObject doesn't handle Maps well
                this.type = weaponType ?: ""
                this.allStats = mapToBundle(document.data ?: emptyMap())
            }
        } catch (e: Exception) {
            println("Error fetching weapon by ID $weaponId: $e")
            null
        }
    }

    /**
     * Fetches a single armor document by its ID from the correct collection.
     */
    suspend fun getArmorById(armorId: String, armorType: ArmorType): ArmorPiece? {
        // Get the collection name from the enum's name (e.g., HELMS, CHEST)
        val collectionName = armorType.name

        return try {
            val document = db.collection(collectionName).document(armorId).get().await()
            document.toObject(ArmorPiece::class.java)?.apply {
                // Manually set the type and map stats
                this.type = armorType
                this.allStats = mapToBundle(document.data ?: emptyMap())
            }
        } catch (e: Exception) {
            println("Error fetching armor by ID $armorId: $e")
            null
        }
    }

    /**
     * Fetches a single skill by its name. This requires a query.
     */
    suspend fun getSkillByName(skillName: String): Skill? {
        try {
            val snapshot = db.collection("SKILLS")
                .whereEqualTo("Name", skillName) // Query for the document with the matching name
                .limit(1)
                .get()
                .await()

            if (snapshot.isEmpty) return null

            val document = snapshot.documents.first()
            val skill = document.toObject(Skill::class.java) ?: return null
            val fullDataMap = document.data
            if (fullDataMap != null) {
                // Step 3: Create a mutable copy and remove keys that are already mapped
                val remainingDataMap = fullDataMap.toMutableMap()
                remainingDataMap.remove("Name")
                remainingDataMap.remove("Description")
                // Add any other @PropertyName-mapped fields here if you add more later

                // Step 4: Convert the REST of the data into the 'details' bundle
                skill.details = mapToBundle(remainingDataMap)
            }
//            document.toObject(Skill::class.java)?.apply {
//                this.details = mapToBundle(document.data ?: emptyMap())
//            }
            return skill
        } catch (e: Exception) {
            println("Error fetching skill by name '$skillName': $e")
            return null
        }

    }
}