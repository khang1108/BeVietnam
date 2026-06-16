package com.bevietnam.ui.screens.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bevietnam.core.domain.usecase.GetPlacesUseCase
import com.bevietnam.core.model.Place
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Trạng thái giao diện (UI State) cho màn hình Khám phá địa điểm du lịch (Explore).
 */
sealed class ExploreUiState {
    /** Trạng thái đang tải danh sách địa điểm */
    object Loading : ExploreUiState()
    
    /** 
     * Trạng thái tải danh sách địa điểm thành công.
     * 
     * @property places Danh sách địa điểm gốc từ dữ liệu nguồn.
     * @property filteredPlaces Danh sách địa điểm sau khi đã lọc theo từ khóa và danh mục.
     * @property selectedCategory Thẻ danh mục đang được lựa chọn để lọc (Mặc định là "Tất cả").
     * @property searchQuery Từ khóa tìm kiếm hiện tại do người dùng nhập.
     * @property isMapView Trạng thái hiển thị giao diện Bản đồ hay Danh sách.
     * @property focusedPlaceId ID của địa điểm đang được focus/chọn trên bản đồ.
     * @property hasLocationPermission Trạng thái cấp quyền vị trí của người dùng.
     */
    data class Success(
        val places: List<Place>,
        val filteredPlaces: List<Place>,
        val selectedCategory: String = "Tất cả",
        val searchQuery: String = "",
        val isMapView: Boolean = true,
        val focusedPlaceId: String? = null,
        val hasLocationPermission: Boolean = false
    ) : ExploreUiState()
    
    /** Trạng thái danh sách địa điểm trống */
    object Empty : ExploreUiState()
    
    /** 
     * Trạng thái gặp sự cố/lỗi khi tải danh sách địa điểm.
     * 
     * @property message Thông tin mô tả chi tiết lỗi xảy ra.
     */
    data class Error(val message: String) : ExploreUiState()
}

/**
 * Danh sách các bộ lọc thẻ danh mục mặc định trên màn hình Khám phá.
 */
val categories = listOf("Tất cả", "Hành trình", "Lịch sử", "Địa điểm", "Văn hóa", "Thiên nhiên", "Nghỉ dưỡng")

/**
 * ViewModel quản lý logic nghiệp vụ và trạng thái dữ liệu cho màn hình Khám phá địa điểm (Explore).
 *
 * Lớp này áp dụng cơ chế tối ưu hóa lọc tìm kiếm bằng Coroutines Flow Debounce để bảo vệ
 * hiệu năng Main Thread, loại bỏ delay giả lập tại UI, và sử dụng `.update` đảm bảo an toàn bất đồng bộ.
 *
 * @property getPlacesUseCase UseCase lấy danh sách địa điểm văn hóa ([GetPlacesUseCase]).
 */
@OptIn(FlowPreview::class)
@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val getPlacesUseCase: GetPlacesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<ExploreUiState>(ExploreUiState.Loading)
    val uiState: StateFlow<ExploreUiState> = _uiState.asStateFlow()

    /**
     * Luồng phát ra từ khóa tìm kiếm phục vụ tính năng trì hoãn lọc (Debounced search).
     */
    private val _searchQueryFlow = MutableStateFlow("")

    init {
        loadPlaces()
        setupSearchDebounce()
    }

    /**
     * Tải danh sách địa điểm khám phá từ tầng Domain.
     */
    fun loadPlaces() {
        viewModelScope.launch {
            _uiState.value = ExploreUiState.Loading
            try {
                getPlacesUseCase().collect { places ->
                    _uiState.value = if (places.isEmpty()) {
                        ExploreUiState.Empty
                    } else {
                        ExploreUiState.Success(places = places, filteredPlaces = places)
                    }
                }
            } catch (e: Exception) {
                _uiState.value = ExploreUiState.Error(e.message ?: "Đã xảy ra lỗi không xác định")
            }
        }
    }

    /**
     * Thiết lập cơ chế lọc tìm kiếm trì hoãn (Debounce).
     *
     * Giúp giảm tải xử lý lọc đồng bộ liên tục trên Main Thread bằng cách chỉ kích hoạt
     * hàm lọc khi người dùng đã dừng gõ phím được 300ms.
     */
    private fun setupSearchDebounce() {
        viewModelScope.launch {
            _searchQueryFlow
                .debounce(300)
                .collect { query ->
                    val current = _uiState.value as? ExploreUiState.Success ?: return@collect
                    val filtered = filterPlaces(current.places, query, current.selectedCategory)
                    updateSuccessState(query = query, filtered = filtered)
                }
        }
    }

    /**
     * Nhận sự kiện chọn danh mục để lọc địa danh.
     * Quá trình lọc theo danh mục được thực hiện tức thời không có độ trễ.
     *
     * @param category Danh mục được chọn.
     */
    fun onCategorySelected(category: String) {
        val current = _uiState.value as? ExploreUiState.Success ?: return
        val filtered = filterPlaces(current.places, current.searchQuery, category)
        updateSuccessState(category = category, filtered = filtered)
    }

    /**
     * Nhận sự kiện thay đổi từ khóa tìm kiếm từ UI.
     * Cập nhật tức thời từ khóa lên giao diện để đảm bảo tính responsive, đồng thời đẩy
     * từ khóa vào luồng debounce để thực hiện lọc sau 300ms.
     *
     * @param query Từ khóa tìm kiếm mới do người dùng nhập.
     */
    fun onSearchQueryChanged(query: String) {
        val current = _uiState.value as? ExploreUiState.Success ?: return
        updateSuccessState(query = query, filtered = current.filteredPlaces)
        _searchQueryFlow.value = query
    }

    /**
     * Chuyển đổi qua lại giữa chế độ xem Bản đồ (Map View) và xem Danh sách (List View).
     */
    fun toggleViewMode() {
        val current = _uiState.value as? ExploreUiState.Success ?: return
        _uiState.update { currentState ->
            if (currentState is ExploreUiState.Success) {
                currentState.copy(isMapView = !currentState.isMapView)
            } else currentState
        }
    }

    /**
     * Đặt ID của địa điểm đang được chọn hoặc lướt tới trên Carousel để highlight Marker trên bản đồ.
     *
     * @param id ID của địa điểm (hoặc null nếu bỏ chọn).
     */
    fun onPlaceFocused(id: String?) {
        val current = _uiState.value as? ExploreUiState.Success ?: return
        if (current.focusedPlaceId != id) {
            _uiState.update { currentState ->
                if (currentState is ExploreUiState.Success) {
                    currentState.copy(focusedPlaceId = id)
                } else currentState
            }
        }
    }

    /**
     * Cập nhật trạng thái cấp quyền vị trí từ UI.
     *
     * @param isGranted Người dùng đã cấp quyền vị trí chưa.
     */
    fun updateLocationPermission(isGranted: Boolean) {
        val current = _uiState.value as? ExploreUiState.Success ?: return
        if (current.hasLocationPermission != isGranted) {
            _uiState.update { currentState ->
                if (currentState is ExploreUiState.Success) {
                    currentState.copy(hasLocationPermission = isGranted)
                } else currentState
            }
        }
    }

    /**
     * Hàm phụ trợ thực hiện lọc danh sách địa điểm theo từ khóa và danh mục.
     *
     * @param places Danh sách địa điểm gốc.
     * @param query Từ khóa tìm kiếm.
     * @param category Danh mục phân loại.
     * @return Danh sách địa điểm đã được lọc thỏa mãn cả hai điều kiện.
     */
    private fun filterPlaces(places: List<Place>, query: String, category: String): List<Place> {
        return places.filter { place ->
            val matchesQuery = query.isBlank() || 
                               place.name.contains(query, ignoreCase = true) ||
                               place.description.contains(query, ignoreCase = true) ||
                               place.category.contains(query, ignoreCase = true)
            val matchesCategory = if (category == "Tất cả") true else place.category.contains(category, ignoreCase = true)
            matchesQuery && matchesCategory
        }
    }

    /**
     * Cập nhật trạng thái thành công của UI một cách an toàn đa luồng (Thread-safe).
     *
     * @param category Danh mục chọn lọc.
     * @param query Từ khóa tìm kiếm.
     * @param filtered Danh sách địa danh đã lọc thành công.
     */
    private fun updateSuccessState(
        category: String? = null,
        query: String? = null,
        filtered: List<Place>
    ) {
        _uiState.update { currentState ->
            if (currentState is ExploreUiState.Success) {
                currentState.copy(
                    selectedCategory = category ?: currentState.selectedCategory,
                    searchQuery = query ?: currentState.searchQuery,
                    filteredPlaces = filtered
                )
            } else {
                currentState
            }
        }
    }
}
