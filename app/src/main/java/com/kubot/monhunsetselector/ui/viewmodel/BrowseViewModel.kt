package com.kubot.monhunsetselector.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kubot.monhunsetselector.data.models.ArmorPiece
import com.kubot.monhunsetselector.data.models.Skill
import com.kubot.monhunsetselector.data.models.Weapon
import com.kubot.monhunsetselector.data.repository.GameDataRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


val mainCategories = listOf("Armor", "Weapons", "Skills")


val subCategories = mapOf(
    "Armor" to listOf("Helms", "Chest", "Arms", "Waist", "Legs"),
    "Weapons" to listOf(
        "Great Sword",
        "Long Sword",
        "Sword & Shield",
        "Dual Blades",
        "Hammer",
        "Hunting Horn",
        "Lance",
        "Gunlance",
        "Switch Axe",
        "Charge Blade",
        "Insect Glaive",
        "Light Bowgun",
        "Heavy Bowgun",
        "Bow"
    ),
    "Skills" to emptyList()
)

class BrowseViewModel : ViewModel() {

    private var isInitialized = false

    fun initializeFromNavArgs(mainCat: String?, subCat: String?) {
        if (isInitialized) return

        val initialMain = mainCat ?: mainCategories.first()
        val initialSub = subCat ?: (subCategories[initialMain]?.firstOrNull() ?: "")

        _selectedMainCategory.value = initialMain
        _selectedSubCategory.value = initialSub


        fetchData(initialMain, initialSub)

        isInitialized = true
    }


    private val repository = GameDataRepository()

    private val _fullEquipmentList = MutableStateFlow<List<Any>>(emptyList())

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    val filteredEquipmentList: StateFlow<List<Any>> =
        combine(_fullEquipmentList, _searchQuery) { list, query ->
            if (query.isBlank()) {

                list
            } else {

                list.filter { equipment ->

                    equipment.matchesSearchQuery(query)
                }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )


    private fun Any.matchesSearchQuery(query: String): Boolean {
        val lowerCaseQuery = query.lowercase().trim()


        val statsBundle = when (this) {
            is Weapon -> this.allStats
            is ArmorPiece -> this.allStats
            is Skill -> this.details
            else -> return false
        }


        return statsBundle.keySet().any { key ->
            statsBundle.get(key)?.toString()?.lowercase()?.contains(lowerCaseQuery) == true
        }
    }


    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }


    private val _equipmentList = MutableStateFlow<List<Any>>(emptyList())
    val equipmentList = _equipmentList.asStateFlow()


    private val _selectedMainCategory = MutableStateFlow(mainCategories.first())
    val selectedMainCategory = _selectedMainCategory.asStateFlow()


    private val _selectedSubCategory =
        MutableStateFlow(subCategories[_selectedMainCategory.value]!!.first())
    val selectedSubCategory = _selectedSubCategory.asStateFlow()

    init {


    }

    fun onMainCategorySelected(category: String) {
        _selectedMainCategory.value = category

        val newSubCategories = subCategories[category] ?: emptyList()
        val newSubCategory = newSubCategories.firstOrNull() ?: ""
        _selectedSubCategory.value = newSubCategory


        fetchData(category, newSubCategory)
    }

    fun onSubCategorySelected(subCategory: String) {
        _selectedSubCategory.value = subCategory


        fetchData(_selectedMainCategory.value, subCategory)
    }


    private fun fetchData(mainCat: String, subCat: String) {
        viewModelScope.launch {

            _searchQuery.value = ""

            _fullEquipmentList.value = repository.getEquipment(mainCat, subCat)
        }
    }
}