package com.bevietnam.ui.screens.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bevietnam.core.domain.usecase.GetPlacesUseCase
import com.bevietnam.core.model.Place
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ExploreUiState {
    object Loading : ExploreUiState()
    data class Success(
        val places: List<Place>,
        val filteredPlaces: List<Place>,
        val selectedCategory: String = "Tất cả",
        val searchQuery: String = ""
    ) : ExploreUiState()
    object Empty : ExploreUiState()
    data class Error(val message: String) : ExploreUiState()
}

val categories = listOf("Tất cả", "Hành trình", "Lịch sử", "Địa điểm", "Văn hóa", "Thiên nhiên", "Nghỉ dưỡng")

@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val getPlacesUseCase: GetPlacesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<ExploreUiState>(ExploreUiState.Loading)
    val uiState: StateFlow<ExploreUiState> = _uiState.asStateFlow()

    init {
        loadPlaces()
    }

    fun loadPlaces() {
        viewModelScope.launch {
            _uiState.value = ExploreUiState.Loading
            delay(1200) // Simulate network delay
            try {
                getPlacesUseCase().collect { places ->
                    _uiState.value = if (places.isEmpty()) {
                        ExploreUiState.Empty
                    } else {
                        ExploreUiState.Success(places = places, filteredPlaces = places)
                    }
                }
            } catch (e: Exception) {
                _uiState.value = ExploreUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun onCategorySelected(category: String) {
        val current = _uiState.value as? ExploreUiState.Success ?: return
        val filtered = if (category == "Tất cả") {
            current.places
        } else {
            current.places.filter { it.category.contains(category, ignoreCase = true) }
        }
        _uiState.updateSuccessState(category = category, filtered = filtered)
    }

    fun onSearchQueryChanged(query: String) {
        val current = _uiState.value as? ExploreUiState.Success ?: return
        val filtered = if (query.isBlank()) {
            current.places
        } else {
            current.places.filter {
                it.name.contains(query, ignoreCase = true) ||
                        it.location.contains(query, ignoreCase = true) ||
                        it.category.contains(query, ignoreCase = true)
            }
        }
        _uiState.updateSuccessState(query = query, filtered = filtered)
    }

    private fun MutableStateFlow<ExploreUiState>.updateSuccessState(
        category: String? = null,
        query: String? = null,
        filtered: List<Place>
    ) {
        val current = value as? ExploreUiState.Success ?: return
        value = current.copy(
            selectedCategory = category ?: current.selectedCategory,
            searchQuery = query ?: current.searchQuery,
            filteredPlaces = filtered
        )
    }
}
