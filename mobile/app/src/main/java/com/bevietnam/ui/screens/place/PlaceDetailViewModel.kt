package com.bevietnam.ui.screens.place

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.bevietnam.core.domain.usecase.GetPlaceDetailUseCase
import com.bevietnam.core.model.Place
import com.bevietnam.ui.navigation.PlaceDetailRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class PlaceDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    getPlaceDetailUseCase: GetPlaceDetailUseCase
) : ViewModel() {

    private val route: PlaceDetailRoute = savedStateHandle.toRoute()

    val place: StateFlow<Place?> = getPlaceDetailUseCase(route.placeId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
}
