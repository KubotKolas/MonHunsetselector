package com.kubot.monhunsetselector.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kubot.monhunsetselector.data.models.UserSet
import com.kubot.monhunsetselector.data.repository.UserSetsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MySetsViewModel : ViewModel() {

    private val repository = UserSetsRepository

    private val _mySets = MutableStateFlow<List<UserSet>>(emptyList())
    val mySets = _mySets.asStateFlow()


    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()


    fun loadMySets() {
        viewModelScope.launch {

            _mySets.value = repository.getMySets()
        }
    }


    fun refreshMySets() {
        viewModelScope.launch {

            _isRefreshing.value = true

            _mySets.value = repository.getMySets(forceRefresh = true)

            _isRefreshing.value = false
        }
    }
}