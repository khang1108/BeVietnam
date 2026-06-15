package com.bevietnam.ui.screens.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bevietnam.core.domain.usecase.GetFeedUseCase
import com.bevietnam.core.model.FeedItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FeedUiState(
    val isLoading: Boolean = false,
    val feedItems: List<FeedItem> = emptyList(),
    val filteredItems: List<FeedItem> = emptyList(),
    val errorMessage: String? = null,
    val searchQuery: String = "",
    val categories: List<String> = listOf("Tất cả", "Travel", "Football", "Food", "Culture", "Music"),
    val selectedCategory: String = "Tất cả"
)

@HiltViewModel
class FeedViewModel @Inject constructor(
    private val getFeedUseCase: GetFeedUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(FeedUiState())
    val uiState: StateFlow<FeedUiState> = _uiState.asStateFlow()

    init {
        loadFeed()
    }

    fun loadFeed() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                getFeedUseCase().collect { items ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false, 
                            feedItems = items,
                            filteredItems = filterItems(items, it.searchQuery, it.selectedCategory)
                        ) 
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { 
            val filtered = filterItems(it.feedItems, query, it.selectedCategory)
            it.copy(searchQuery = query, filteredItems = filtered) 
        }
    }

    fun onCategorySelect(category: String) {
        _uiState.update { 
            val filtered = filterItems(it.feedItems, it.searchQuery, category)
            it.copy(selectedCategory = category, filteredItems = filtered) 
        }
    }

    private fun filterItems(items: List<FeedItem>, query: String, category: String): List<FeedItem> {
        return items.filter { item ->
            val matchesQuery = item.content.contains(query, ignoreCase = true) || 
                               item.userName.contains(query, ignoreCase = true)
            val matchesCategory = if (category == "Tất cả") true else item.category == category
            matchesQuery && matchesCategory
        }
    }

    fun onLikeClick(feedId: String) {
        _uiState.update { currentState ->
            val updatedItems = currentState.feedItems.map { item ->
                if (item.id == feedId) {
                    val newIsLiked = !item.isLiked
                    item.copy(
                        isLiked = newIsLiked,
                        likesCount = if (newIsLiked) item.likesCount + 1 else item.likesCount - 1
                    )
                } else {
                    item
                }
            }
            currentState.copy(
                feedItems = updatedItems,
                filteredItems = filterItems(updatedItems, currentState.searchQuery, currentState.selectedCategory)
            )
        }
    }
}
