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


                bundle.putString(key, "")
                continue
            }
            when (value) {
                is String -> bundle.putString(key, value)
                is Long -> bundle.putLong(key, value)
                is Int -> bundle.putInt(key, value)
                is Double -> bundle.putDouble(key, value)
                is Boolean -> bundle.putBoolean(key, value)

                else -> bundle.putString(key, value.toString())
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

            println("Error fetching equipment: $e")
            emptyList()
        }
    }

    private suspend fun fetchArmor(armorType: String): List<ArmorPiece> {
        val collectionName = armorType.uppercase()


        val snapshot = db.collection(collectionName).get().await()



        return snapshot.documents.mapNotNull { document ->
            document.data?.let { data ->
                val armorName = data["Name"] as? String ?: "Unknown"
                val mappedType = mapStringToArmorType(armorType)
                if (armorName == "G Doshaguma Helm Alpha") {
                    println("DEBUG_REPO: Creating ArmorPiece: name='$armorName', type='$mappedType'")
                }
                ArmorPiece(
                    id = document.id,
                    name = data["Name"] as? String ?: "Unknown",
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
                    type = weaponType,
                    allStats = mapToBundle(data)
                )
            }
        }
    }

    private suspend fun fetchSkills(): List<Skill> {
        val snapshot = db.collection("SKILLS").get().await()
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


    private fun mapStringToArmorType(type: String): ArmorType {
        return when (type) {
            "Helms" -> ArmorType.HELMS
            "Chest" -> ArmorType.CHEST
            "Arms" -> ArmorType.ARMS
            "Waist" -> ArmorType.WAIST
            "Legs" -> ArmorType.LEGS
            else -> ArmorType.HELMS
        }
    }


    /**
     * Fetches a single weapon document by its ID from the correct collection.
     */
    suspend fun getWeaponById(weaponId: String, weaponType: String?): Weapon? {

        val collectionName = weaponCollectionMap[weaponType] ?: return null

        return try {
            val document = db.collection(collectionName).document(weaponId).get().await()
            document.toObject(Weapon::class.java)?.apply {

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

        val collectionName = armorType.name

        return try {
            val document = db.collection(collectionName).document(armorId).get().await()
            document.toObject(ArmorPiece::class.java)?.apply {

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
                .whereEqualTo("Name", skillName)
                .limit(1)
                .get()
                .await()

            if (snapshot.isEmpty) return null

            val document = snapshot.documents.first()
            val skill = document.toObject(Skill::class.java) ?: return null
            val fullDataMap = document.data
            if (fullDataMap != null) {

                val remainingDataMap = fullDataMap.toMutableMap()
                remainingDataMap.remove("Name")
                remainingDataMap.remove("Description")



                skill.details = mapToBundle(remainingDataMap)
            }



            return skill
        } catch (e: Exception) {
            println("Error fetching skill by name '$skillName': $e")
            return null
        }

    }
}