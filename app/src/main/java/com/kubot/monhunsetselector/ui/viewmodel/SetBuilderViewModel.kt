package com.kubot.monhunsetselector.ui.viewmodel

import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kubot.monhunsetselector.data.models.ArmorPiece
import com.kubot.monhunsetselector.data.models.ArmorType
import com.kubot.monhunsetselector.data.models.UserSet
import com.kubot.monhunsetselector.data.models.Weapon
import com.kubot.monhunsetselector.data.repository.GameDataRepository
import com.kubot.monhunsetselector.data.repository.UserSetsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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

        val updatedSet = when (equipment) {
            is Weapon -> _currentSet.value.copy(
                weaponId = equipment.id,
                weaponName = equipment.name,
                weaponType = equipment.type
            )

            is ArmorPiece -> {
                when (equipment.type) {
                    ArmorType.HELMS -> _currentSet.value.copy(
                        headId = equipment.id,
                        headName = equipment.name
                    )

                    ArmorType.CHEST -> _currentSet.value.copy(
                        chestId = equipment.id,
                        chestName = equipment.name
                    )

                    ArmorType.ARMS -> _currentSet.value.copy(
                        armsId = equipment.id,
                        armsName = equipment.name
                    )

                    ArmorType.WAIST -> _currentSet.value.copy(
                        waistId = equipment.id,
                        waistName = equipment.name
                    )

                    ArmorType.LEGS -> _currentSet.value.copy(
                        legsId = equipment.id,
                        legsName = equipment.name
                    )
                }
            }

            else -> _currentSet.value
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
            _saveEvent.emit(Unit)
        }
    }

    fun deleteSet() {

        val idToDelete = _currentSet.value.id
        if (idToDelete.isNotBlank()) {
            viewModelScope.launch {
                repository.deleteSet(idToDelete)
                _saveEvent.emit(Unit)
            }
        }
    }


    @OptIn(ExperimentalCoroutinesApi::class)
    val fullEquipmentSet: StateFlow<List<Any>> = _currentSet.flatMapLatest { set ->

        flow {

            if (set.id.isBlank() && set.weaponId == null && set.headId == null) {
                emit(emptyList())
                return@flow
            }


            val equipmentList = coroutineScope {

                val weaponJob = async {
                    set.weaponId?.let {
                        gameDataRepository.getWeaponById(
                            it,
                            set.weaponType
                        )
                    }
                }
                val headJob = async {
                    set.headId?.let {
                        gameDataRepository.getArmorById(
                            it,
                            ArmorType.HELMS
                        )
                    }
                }
                val chestJob = async {
                    set.chestId?.let {
                        gameDataRepository.getArmorById(
                            it,
                            ArmorType.CHEST
                        )
                    }
                }
                val armsJob = async {
                    set.armsId?.let {
                        gameDataRepository.getArmorById(
                            it,
                            ArmorType.ARMS
                        )
                    }
                }
                val waistJob = async {
                    set.waistId?.let {
                        gameDataRepository.getArmorById(
                            it,
                            ArmorType.WAIST
                        )
                    }
                }
                val legsJob = async {
                    set.legsId?.let {
                        gameDataRepository.getArmorById(
                            it,
                            ArmorType.LEGS
                        )
                    }
                }


                val jobs = listOf(weaponJob, headJob, chestJob, armsJob, waistJob, legsJob)


                jobs.awaitAll().filterNotNull()
            }


            emit(equipmentList)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val cumulativeStats: StateFlow<CumulativeStats> = fullEquipmentSet.map { equipmentList ->


        val numericStats = mutableMapOf<String, Double>()
        val skills = mutableMapOf<String, Int>()
        val materials = mutableMapOf<String, Int>()
        val slotsList = mutableListOf<String>()

        println("DEBUG_SKILLS: --- Recalculating for ${equipmentList.size} items ---")


        equipmentList.forEach { equipment ->

            val statsBundle = when (equipment) {
                is Weapon -> equipment.allStats
                is ArmorPiece -> equipment.allStats
                else -> Bundle()
            }


            statsBundle.keySet().forEach { key ->

                if (key in ignoredNumericStats) return@forEach

                val value = statsBundle.get(key)
                if (value is Number) {
                    numericStats[key] = (numericStats[key] ?: 0.0) + value.toDouble()
                }
            }


            val skillsString = statsBundle.getString("Skills", "")

            println("DEBUG_SKILLS: Found 'Skills' key for '$equipment. Raw value: '$skillsString'")

            if (skillsString.isNotBlank()) {

                skillsString.split("|").forEach { skillPart ->
                    println("DEBUG_SKILLS: Parsing skill part: '$skillPart'")
                    val trimmedPart = skillPart.trim()

                    val parts = trimmedPart.split(" Lv ")
                    if (parts.size == 2) {
                        val skillName = parts[0]
                        val level = parts[1].toIntOrNull() ?: 0
                        println("DEBUG_SKILLS: Successfully parsed: Name='$skillName', Level=$level")
                        if (level > 0) {

                            skills[skillName] = (skills[skillName] ?: 0) + level
                        } else {
                            println("DEBUG_SKILLS: !!! FAILED to parse part '$trimmedPart'. Split produced ${parts.size} parts, not 2.")
                        }
                    }

                }
            }


            val materialsString = statsBundle.getString("Materials", "")
            if (materialsString.isNotBlank()) {
                materialsString.split("|").filter { it.isNotBlank() }.forEach { materialPart ->
                    val trimmedPart = materialPart.trim()



                    if (trimmedPart.endsWith("z", ignoreCase = true)) {

                        val quantity = trimmedPart.dropLast(1).toIntOrNull() ?: 0
                        if (quantity > 0) {

                            materials["Zenny"] = (materials["Zenny"] ?: 0) + quantity
                        }
                    } else {
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

        println("DEBUG_SKILLS: --- Calculation Finished. Final skills map: $skills ---")
        CumulativeStats(numericStats, skills, materials, slotsList)

    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CumulativeStats()
    )
    private val ignoredNumericStats = setOf("Rarity", "Price", "Sort Order", "id")

}