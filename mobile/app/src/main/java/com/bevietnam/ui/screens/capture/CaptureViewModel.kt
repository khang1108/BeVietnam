package com.bevietnam.ui.screens.capture

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.SavedStateHandle
import com.bevietnam.core.domain.session.SessionManager
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

/**
 * Trạng thái giao diện (UI State) cho màn hình Đăng bài viết khám phá (Capture).
 *
 * @property imageUri Địa chỉ Uri của ảnh chụp hoặc ảnh chọn từ thư viện.
 * @property isUploading Trạng thái đang tải lên dữ liệu bài viết (Loading).
 * @property uploadSuccess Trạng thái tải lên thành công (Success).
 * @property errorMessage Thông điệp lỗi chi tiết nếu có sự cố xảy ra.
 * @property latitude Vĩ độ vị trí GPS hiện tại của người dùng khi đăng bài.
 * @property longitude Kinh độ vị trí GPS hiện tại của người dùng khi đăng bài.
 * @property cameraPermissionGranted Trạng thái cấp quyền máy ảnh.
 * @property locationPermissionGranted Trạng thái cấp quyền truy cập vị trí địa lý.
 */
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

/**
 * ViewModel chịu trách nhiệm quản lý logic và trạng thái màn hình Đăng bài viết khám phá (Capture Screen).
 *
 * Lớp này sử dụng quy chuẩn Unidirectional Data Flow (UDF) để cập nhật trạng thái UI State
 * một cách thread-safe thông qua Hilt dependency injection.
 *
 * @property uploadCaptureUseCase UseCase thực thi nghiệp vụ đăng tải bài viết kèm ảnh ([UploadCaptureUseCase]).
 */
@HiltViewModel
class CaptureViewModel @Inject constructor(
    private val uploadCaptureUseCase: UploadCaptureUseCase,
    private val sessionManager: SessionManager,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val placeId: String? = savedStateHandle.get<String>("placeId")
    private val taskId: String? = savedStateHandle.get<String>("taskId")

    private val _uiState = MutableStateFlow(CaptureUiState())
    val uiState: StateFlow<CaptureUiState> = _uiState.asStateFlow()

    /**
     * Nhận sự kiện chọn hoặc chụp ảnh từ UI.
     * Cập nhật địa chỉ Uri của ảnh vào trạng thái màn hình.
     *
     * @param uri Địa chỉ Uri của hình ảnh đã chọn.
     */
    fun onImageCaptured(uri: Uri?) {
        _uiState.update { it.copy(imageUri = uri) }
    }

    /**
     * Nhận sự kiện cập nhật tọa độ GPS từ dịch vụ vị trí của thiết bị.
     * Cập nhật vĩ độ và kinh độ hiện tại vào trạng thái màn hình.
     *
     * @param lat Vĩ độ GPS.
     * @param lng Kinh độ GPS.
     */
    fun onLocationUpdated(lat: Double, lng: Double) {
        _uiState.update { it.copy(latitude = lat, longitude = lng) }
    }

    /**
     * Nhận kết quả yêu cầu cấp quyền từ người dùng trên giao diện.
     * Cập nhật trạng thái cấp quyền Camera hoặc Vị trí địa lý.
     *
     * @param permission Tên quyền hệ thống yêu cầu (ví dụ: Manifest.permission.CAMERA).
     * @param isGranted Trạng thái được người dùng cấp quyền hay từ chối.
     */
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

    /**
     * Thực hiện nghiệp vụ tải bài đăng kèm ảnh chụp lên máy chủ/giả lập.
     *
     * Kiểm tra tính hợp lệ của dữ liệu đầu vào (ảnh không được rỗng) trước khi gọi UseCase,
     * đồng thời lắng nghe luồng kết quả trạng thái từ UseCase để cập nhật UI State tương ứng.
     *
     * @param description Nội dung mô tả/cảm nghĩ bài đăng từ người dùng.
     */
    fun uploadCapture(description: String) {
        val currentState = _uiState.value
        if (currentState.imageUri == null) {
            _uiState.update { it.copy(errorMessage = "Vui lòng chọn hoặc chụp ảnh") }
            return
        }

        val userId = sessionManager.currentUser.value?.id
        if (userId == null) {
            _uiState.update { it.copy(errorMessage = "Vui lòng đăng nhập lại") }
            return
        }

        if (placeId == null) {
            _uiState.update { it.copy(errorMessage = "Thiếu thông tin địa điểm (placeId)") }
            return
        }

        viewModelScope.launch {
            val metadata = CaptureMetadata(
                mediaUrl = currentState.imageUri.toString(),
                latitude = currentState.latitude,
                longitude = currentState.longitude,
                note = description
            )

            uploadCaptureUseCase(metadata, userId, placeId, taskId).collect { resource ->
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

    /**
     * Đặt lại (reset) toàn bộ trạng thái của màn hình Capture về mặc định ban đầu.
     */
    fun resetState() {
        _uiState.value = CaptureUiState()
    }
}
