package com.bevietnam.ui.screens.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bevietnam.core.domain.usecase.GetRecommendationsUseCase
import com.bevietnam.core.model.RecommendationItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Trạng thái giao diện (UI State) cho màn hình Bảng tin gợi ý (Recommendation Feed).
 *
 * @property isLoading Trạng thái đang tải dữ liệu gợi ý từ nguồn mạng/giả lập.
 * @property recommendations Danh sách gốc tất cả các gợi ý địa điểm đã xếp hạng.
 * @property filteredRecommendations Danh sách gợi ý đã được lọc theo từ khóa và danh mục.
 * @property errorMessage Thông điệp lỗi chi tiết nếu có sự cố xảy ra.
 * @property searchQuery Từ khóa tìm kiếm hiện tại do người dùng nhập.
 * @property categories Danh sách các thẻ danh mục để người dùng lọc gợi ý.
 * @property selectedCategory Danh mục đang được chọn để lọc (Mặc định là "Tất cả").
 */
data class FeedUiState(
    val isLoading: Boolean = false,
    val recommendations: List<RecommendationItem> = emptyList(),
    val filteredRecommendations: List<RecommendationItem> = emptyList(),
    val errorMessage: String? = null,
    val searchQuery: String = "",
    val categories: List<String> = listOf("Tất cả", "Lịch sử", "Văn hóa", "Thiên nhiên", "Ẩm thực"),
    val selectedCategory: String = "Tất cả"
)

/**
 * ViewModel quản lý logic nghiệp vụ và trạng thái dữ liệu cho màn hình Bảng tin gợi ý (Recommendation Feed).
 *
 * Lớp này sử dụng quy chuẩn Unidirectional Data Flow (UDF) và áp dụng cơ chế tối ưu hóa tìm kiếm
 * bằng Coroutines Flow Debounce để cải thiện hiệu năng xử lý UI.
 *
 * @property getRecommendationsUseCase UseCase lấy danh sách gợi ý địa điểm ([GetRecommendationsUseCase]).
 */
@OptIn(FlowPreview::class)
@HiltViewModel
class FeedViewModel @Inject constructor(
    private val getRecommendationsUseCase: GetRecommendationsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(FeedUiState())
    val uiState: StateFlow<FeedUiState> = _uiState.asStateFlow()

    /**
     * Luồng phát ra từ khóa tìm kiếm phục vụ tính năng trì hoãn lọc (Debounced search).
     */
    private val _searchQueryFlow = MutableStateFlow("")

    init {
        loadRecommendations()
        setupSearchDebounce()
    }

    /**
     * Khởi động luồng tải dữ liệu gợi ý địa điểm từ Domain layer.
     */
    fun loadRecommendations() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                getRecommendationsUseCase().collect { items ->
                    _uiState.update { currentState ->
                        currentState.copy(
                            isLoading = false,
                            recommendations = items,
                            filteredRecommendations = filterItems(
                                items,
                                currentState.searchQuery,
                                currentState.selectedCategory
                            )
                        )
                    }
                }
            } catch (e: CancellationException) {
                throw e // Không nuốt CancellationException — để coroutine hủy đúng cách
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message ?: "Đã xảy ra lỗi không xác định") }
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
                    _uiState.update { currentState ->
                        currentState.copy(
                            filteredRecommendations = filterItems(
                                currentState.recommendations,
                                query,
                                currentState.selectedCategory
                            )
                        )
                    }
                }
        }
    }

    /**
     * Nhận sự kiện thay đổi từ khóa tìm kiếm từ UI.
     * Cập nhật tức thời từ khóa lên giao diện để đảm bảo tính responsive, đồng thời đẩy
     * từ khóa vào luồng debounce để thực hiện lọc sau 300ms.
     *
     * @param query Từ khóa tìm kiếm mới do người dùng nhập.
     */
    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        _searchQueryFlow.value = query
    }

    /**
     * Nhận sự kiện chọn danh mục để lọc gợi ý.
     * Quá trình lọc theo danh mục được thực hiện tức thời không có độ trễ.
     *
     * @param category Danh mục được chọn.
     */
    fun onCategorySelect(category: String) {
        _uiState.update { currentState ->
            val filtered = filterItems(currentState.recommendations, currentState.searchQuery, category)
            currentState.copy(selectedCategory = category, filteredRecommendations = filtered)
        }
    }

    /**
     * Hàm phụ trợ thực hiện lọc danh sách gợi ý theo từ khóa và danh mục.
     *
     * @param items Danh sách gợi ý gốc.
     * @param query Từ khóa tìm kiếm.
     * @param category Danh mục phân loại.
     * @return Danh sách gợi ý đã được lọc thỏa mãn cả hai điều kiện.
     */
    private fun filterItems(
        items: List<RecommendationItem>,
        query: String,
        category: String
    ): List<RecommendationItem> {
        return items.filter { item ->
            val matchesQuery = item.name.contains(query, ignoreCase = true) ||
                    item.explanation.contains(query, ignoreCase = true)
            val matchesCategory = if (category == "Tất cả") true else item.category == category
            matchesQuery && matchesCategory
        }
    }
}
