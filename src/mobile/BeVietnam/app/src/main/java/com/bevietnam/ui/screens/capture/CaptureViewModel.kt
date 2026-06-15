package com.bevietnam.ui.screens.capture

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bevietnam.core.domain.usecase.UploadCaptureUseCase
import com.bevietnam.core.model.CaptureMetadata
import com.bevietnam.core.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CaptureUiState(
    val imageUri: Uri? = null,
    val isUploading: Boolean = false,
    val uploadSuccess: Boolean = false,
    val errorMessage: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val cameraPermissionGranted: Boolean = false,
    val locationPermissionGranted: Boolean = false
)

@HiltViewModel
class CaptureViewModel @Inject constructor(
    private val uploadCaptureUseCase: UploadCaptureUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CaptureUiState())
    val uiState: StateFlow<CaptureUiState> = _uiState.asStateFlow()

    fun onImageCaptured(uri: Uri?) {
        _uiState.update { it.copy(imageUri = uri) }
    }

    fun onLocationUpdated(lat: Double, lng: Double) {
        _uiState.update { it.copy(latitude = lat, longitude = lng) }
    }

    fun onPermissionResult(permission: String, isGranted: Boolean) {
        when (permission) {
            android.Manifest.permission.CAMERA -> {
                _uiState.update { it.copy(cameraPermissionGranted = isGranted) }
            }
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION -> {
                _uiState.update { it.copy(locationPermissionGranted = isGranted) }
            }
        }
    }

    fun uploadCapture(description: String) {
        val currentState = _uiState.value
        if (currentState.imageUri == null) {
            _uiState.update { it.copy(errorMessage = "Vui lòng chọn hoặc chụp ảnh") }
            return
        }

        viewModelScope.launch {
            val metadata = CaptureMetadata(
                imageUrl = currentState.imageUri.toString(),
                latitude = currentState.latitude,
                longitude = currentState.longitude,
                description = description
            )

            uploadCaptureUseCase(metadata).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isUploading = true, errorMessage = null) }
                    }
                    is Resource.Success -> {
                        _uiState.update { it.copy(isUploading = false, uploadSuccess = true) }
                    }
                    is Resource.Error -> {
                        _uiState.update { it.copy(isUploading = false, errorMessage = resource.message) }
                    }
                }
            }
        }
    }

    fun resetState() {
        _uiState.value = CaptureUiState()
    }
}
