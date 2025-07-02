package com.kubot.monhunsetselector.ui.viewmodel

import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kubot.monhunsetselector.data.models.UserSet
import com.kubot.monhunsetselector.data.models.*
import com.kubot.monhunsetselector.data.repository.GameDataRepository
import com.kubot.monhunsetselector.data.repository.UserSetsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.flatMapLatest

data class CumulativeStats(
    val numericStats: Map<String, Double> = emptyMap(),
    val skills: Map<String, Int> = emptyMap(),
    val materials: Map<String, Int> = emptyMap(),
    val slots: List<String> = emptyList()
)

class SetBuilderViewModel : ViewModel() {
    private val repository = UserSetsRepository

    private val gameDataRepository = GameDataRepository()

    private val _currentSet = MutableStateFlow(UserSet())
    val currentSet = _currentSet.asStateFlow()

    // A flow to signal when the set has been saved successfully
    private val _saveEvent = MutableSharedFlow<Unit>()
    val saveEvent = _saveEvent.asSharedFlow()

    private var loadedSetId: String? = null

    init {
        viewModelScope.launch {
            _currentSet.collect {
                println("DEBUG_STATE_CHANGE: _currentSet has been updated. New ID: ${it.id}, Head: ${it.headName}")
            }
        }
    }

    fun loadSet(setId: String?) {
        if (setId == loadedSetId) {
            println("DEBUG_VM: Ignoring redundant loadSet call for id: $setId")
            return
        }
        loadedSetId = setId
        println("DEBUG_VM: Preparing to load set with id: $setId")

        if (setId == null) {
            _currentSet.value = UserSet()
            return
        }

        viewModelScope.launch {
            repository.getSetById(setId)?.let {
                println("DEBUG_VM: Loaded set from repo: $it")
                _currentSet.value = it
            }
        }
    }

    fun onEquipmentSelected(equipment: Any) {
        // We will create the new state first, then update the flow once.
        val updatedSet = when (equipment) {
            is Weapon -> _currentSet.value.copy(
                weaponId = equipment.id,
                weaponName = equipment.name,
                weaponType = equipment.type
            )
            is ArmorPiece -> {
                when (equipment.type) {
                    ArmorType.HELMS -> _currentSet.value.copy(headId = equipment.id, headName = equipment.name)
                    ArmorType.CHEST -> _currentSet.value.copy(chestId = equipment.id, chestName = equipment.name)
                    ArmorType.ARMS -> _currentSet.value.copy(armsId = equipment.id, armsName = equipment.name)
                    ArmorType.WAIST -> _currentSet.value.copy(waistId = equipment.id, waistName = equipment.name)
                    ArmorType.LEGS -> _currentSet.value.copy(legsId = equipment.id, legsName = equipment.name)
                }
            }
            else -> _currentSet.value // Return the current state if type is unknown
        }

        println("DEBUG_VM: onEquipmentSelected is updating state to: $updatedSet")
        _currentSet.value = updatedSet
    }

    fun onSetNameChanged(newName: String) {
        _currentSet.update { it.copy(name = newName) }
    }

    fun saveSet() {
        viewModelScope.launch {
            repository.saveSet(_currentSet.value)
            _saveEvent.emit(Unit) // Signal that save is complete
        }
    }

    fun deleteSet() {
        // Ensure we have a valid set ID to delete
        val idToDelete = _currentSet.value.id
        if (idToDelete.isNotBlank()) {
            viewModelScope.launch {
                repository.deleteSet(idToDelete)
                _saveEvent.emit(Unit) // Re-use the save event to navigate back after delete
            }
        }
    }



    @OptIn(ExperimentalCoroutinesApi::class)
    val fullEquipmentSet: StateFlow<List<Any>> = _currentSet.flatMapLatest { set ->
        // Create a new Flow for each updated UserSet
        flow {
            // Handle the initial empty case to avoid unnecessary work
            if (set.id.isBlank() && set.weaponId == null && set.headId == null) {
                emit(emptyList())
                return@flow
            }

            // --- THE KEY FIX: Use coroutineScope to launch async jobs ---
            val equipmentList = coroutineScope {
                // Launch all database fetches concurrently
                val weaponJob = async { set.weaponId?.let { gameDataRepository.getWeaponById(it, set.weaponType) } }
                val headJob = async { set.headId?.let { gameDataRepository.getArmorById(it, ArmorType.HELMS) } }
                val chestJob = async { set.chestId?.let { gameDataRepository.getArmorById(it, ArmorType.CHEST) } }
                val armsJob = async { set.armsId?.let { gameDataRepository.getArmorById(it, ArmorType.ARMS) } }
                val waistJob = async { set.waistId?.let { gameDataRepository.getArmorById(it, ArmorType.WAIST) } }
                val legsJob = async { set.legsId?.let { gameDataRepository.getArmorById(it, ArmorType.LEGS) } }

                // Create a list of all the Deferred jobs
                val jobs = listOf(weaponJob, headJob, chestJob, armsJob, waistJob, legsJob)

                // Await all jobs to complete and filter out any null results
                jobs.awaitAll().filterNotNull()
            }

            // Emit the final, complete list of equipment objects
            emit(equipmentList)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val cumulativeStats: StateFlow<CumulativeStats> = fullEquipmentSet.map { equipmentList ->
        // This 'map' block receives the List<Any> from the fullEquipmentSet flow.
        // The placeholder data is no longer needed.

        // 1. Initialize our accumulators
        val numericStats = mutableMapOf<String, Double>()
        val skills = mutableMapOf<String, Int>()
        val materials = mutableMapOf<String, Int>()
        val slotsList = mutableListOf<String>()

        println("DEBUG_SKILLS: --- Recalculating for ${equipmentList.size} items ---")

        // 2. Iterate through the real list of equipment
        equipmentList.forEach { equipment ->
            // Get the stats Bundle from the current piece of equipment
            val statsBundle = when (equipment) {
                is Weapon -> equipment.allStats
                is ArmorPiece -> equipment.allStats
                else -> Bundle() // Should not happen, but a safe fallback
            }

            // --- A. Process NUMERIC STATS ---
            statsBundle.keySet().forEach { key ->
                // This is a safe change to prevent Rarity from being summed.
                if (key in ignoredNumericStats) return@forEach

                val value = statsBundle.get(key)
                if (value is Number) {
                    numericStats[key] = (numericStats[key] ?: 0.0) + value.toDouble()
                }
            }

            // --- B. Process SKILLS ---
            // We assume skills are stored in a string field named "Skills"
            val skillsString = statsBundle.getString("Skills", "")

            println("DEBUG_SKILLS: Found 'Skills' key for '$equipment. Raw value: '$skillsString'")

            if (skillsString.isNotBlank()) {
                // Split by the "|" delimiter
                skillsString.split("|").forEach { skillPart ->
                    println("DEBUG_SKILLS: Parsing skill part: '$skillPart'");                    val trimmedPart = skillPart.trim()
                    // Split "Attack Boost Lv. 2" into "Attack Boost" and "2"
                    val parts = trimmedPart.split(" Lv ")
                    if (parts.size == 2) {
                        val skillName = parts[0]
                        val level = parts[1].toIntOrNull() ?: 0
                        println("DEBUG_SKILLS: Successfully parsed: Name='$skillName', Level=$level")
                        if (level > 0) {
                            // Add the level to the existing total for that skill
                            skills[skillName] = (skills[skillName] ?: 0) + level
                        }else {
                            println("DEBUG_SKILLS: !!! FAILED to parse part '$trimmedPart'. Split produced ${parts.size} parts, not 2.")
                        }
                    }

                }
            }


            // --- C. Process MATERIALS ---
            val materialsString = statsBundle.getString("Materials", "")
            if (materialsString.isNotBlank()) {
                materialsString.split("|").filter { it.isNotBlank() }.forEach { materialPart ->
                    val trimmedPart = materialPart.trim()

                    // --- THE MONEY FIX ---
                    // First, check if the part ends with 'z' for zenny.
                    if (trimmedPart.endsWith("z", ignoreCase = true)) {
                        // Try to parse the numeric part before the 'z'.
                        val quantity = trimmedPart.dropLast(1).toIntOrNull() ?: 0
                        if (quantity > 0) {
                            // Add to a special "Zenny" key.
                            materials["Zenny"] = (materials["Zenny"] ?: 0) + quantity
                        }
                    }
                    // If it's not zenny, use the original logic.
                    else {
                        val parts = trimmedPart.split(" x")
                        if (parts.size == 2) {
                            val materialName = parts[0]
                            val quantity = parts[1].toIntOrNull() ?: 0
                            if (quantity > 0) {
                                materials[materialName] = (materials[materialName] ?: 0) + quantity
                            }
                        }
                    }
                }
            }

            val slotsString = statsBundle.getString("Slots", "0-0-0")
            if (slotsString.isNotBlank() && slotsString != "0-0-0") {
                slotsList.add(slotsString)
            }
        }

        println("DEBUG_SKILLS: --- Calculation Finished. Final skills map: $skills ---")        // 3. Return the fully calculated data
        CumulativeStats(numericStats, skills, materials, slotsList)

    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CumulativeStats() // Start with empty stats
    )
    private val ignoredNumericStats = setOf("Rarity", "Price", "Sort Order", "id")

}