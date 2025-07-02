package com.kubot.monhunsetselector.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kubot.monhunsetselector.data.models.UserSet
import com.kubot.monhunsetselector.data.repository.UserSetsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MySetsViewModel : ViewModel() {
    // The repository is now an object, so no need to instantiate it.
    private val repository = UserSetsRepository

    private val _mySets = MutableStateFlow<List<UserSet>>(emptyList())
    val mySets = _mySets.asStateFlow()

    // --- NEW: State to track if we are currently refreshing ---
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()


    fun loadMySets() {
        viewModelScope.launch {
            // This will use the cache by default
            _mySets.value = repository.getMySets()
        }
    }

    // --- MODIFIED: The refresh function now controls the refreshing state ---
    fun refreshMySets() {
        viewModelScope.launch {
            // Show the loading indicator
            _isRefreshing.value = true
            // Force a new fetch from Firestore
            _mySets.value = repository.getMySets(forceRefresh = true)
            // Hide the loading indicator once the fetch is complete
            _isRefreshing.value = false
        }
    }
}