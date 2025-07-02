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

// Define the options for the first dropdown
val mainCategories = listOf("Armor", "Weapons", "Skills")

// Define the options for the second dropdown, mapped to the first
val subCategories = mapOf(
    "Armor" to listOf("Helms", "Chest", "Arms", "Waist", "Legs"),
    "Weapons" to listOf("Great Sword", "Long Sword", "Sword & Shield", "Dual Blades", "Hammer", "Hunting Horn", "Lance", "Gunlance", "Switch Axe", "Charge Blade", "Insect Glaive", "Light Bowgun", "Heavy Bowgun", "Bow"),
    "Skills" to emptyList() // No sub-category for skills
)

class BrowseViewModel : ViewModel() {

    private var isInitialized = false

    fun initializeFromNavArgs(mainCat: String?, subCat: String?) {
        if (isInitialized) return

        val initialMain = mainCat ?: mainCategories.first()
        val initialSub = subCat ?: (subCategories[initialMain]?.firstOrNull() ?: "")

        _selectedMainCategory.value = initialMain
        _selectedSubCategory.value = initialSub

        // Pass the calculated initial values directly to fetchData
        fetchData(initialMain, initialSub)

        isInitialized = true
    }

    // You would inject this repository in a real app
    private val repository = GameDataRepository()

    private val _fullEquipmentList = MutableStateFlow<List<Any>>(emptyList())

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    val filteredEquipmentList: StateFlow<List<Any>> =
        combine(_fullEquipmentList, _searchQuery) { list, query ->
            if (query.isBlank()) {
                // If the query is empty, return the full list
                list
            } else {
                // Otherwise, filter the list
                list.filter { equipment ->
                    // This function will check if the equipment matches the query
                    equipment.matchesSearchQuery(query)
                }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // --- NEW: A helper extension function to perform the search logic ---
    private fun Any.matchesSearchQuery(query: String): Boolean {
        val lowerCaseQuery = query.lowercase().trim()

        // Get the bundle of stats for the current item
        val statsBundle = when (this) {
            is Weapon -> this.allStats
            is ArmorPiece -> this.allStats
            is Skill -> this.details
            else -> return false
        }

        // Check if any value in the bundle contains the query text
        return statsBundle.keySet().any { key ->
            statsBundle.get(key)?.toString()?.lowercase()?.contains(lowerCaseQuery) == true
        }
    }

    // --- NEW: A function for the UI to call when the search text changes ---
    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    // State for the currently displayed list of equipment
    private val _equipmentList = MutableStateFlow<List<Any>>(emptyList())
    val equipmentList = _equipmentList.asStateFlow()

    // State for the selected main category
    private val _selectedMainCategory = MutableStateFlow(mainCategories.first())
    val selectedMainCategory = _selectedMainCategory.asStateFlow()

    // State for the selected sub category
    private val _selectedSubCategory = MutableStateFlow(subCategories[_selectedMainCategory.value]!!.first())
    val selectedSubCategory = _selectedSubCategory.asStateFlow()

    init {
        // Load the initial data when the ViewModel is created
//        if(!isInitialized){
//            fetchData()
//        }
    }

    fun onMainCategorySelected(category: String) {
        _selectedMainCategory.value = category

        val newSubCategories = subCategories[category] ?: emptyList()
        val newSubCategory = newSubCategories.firstOrNull() ?: ""
        _selectedSubCategory.value = newSubCategory

        // Pass the new values directly to fetchData
        fetchData(category, newSubCategory)
    }

    fun onSubCategorySelected(subCategory: String) {
        _selectedSubCategory.value = subCategory

        // Pass the new sub-category and the CURRENT main category to fetchData
        fetchData(_selectedMainCategory.value, subCategory)
    }

    // --- KEY CHANGE: fetchData now accepts parameters ---
    private fun fetchData(mainCat: String, subCat: String) {
        viewModelScope.launch {
            // Reset search query when categories change
            _searchQuery.value = ""
            // Fetch the full list and store it
            _fullEquipmentList.value = repository.getEquipment(mainCat, subCat)
        }
    }
}