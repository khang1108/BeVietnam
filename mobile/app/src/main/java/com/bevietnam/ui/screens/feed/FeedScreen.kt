package com.bevietnam.ui.screens.feed

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bevietnam.core.model.RecommendationItem
import com.bevietnam.ui.components.*
import com.bevietnam.ui.theme.BeVietnamTheme
import com.bevietnam.ui.theme.CulturalAmber
import com.bevietnam.ui.theme.Dimens

/**
 * Màn hình Bảng tin gợi ý (Recommendation Feed Screen) của ứng dụng BeVietnam.
 *
 * Hiển thị danh sách gợi ý địa điểm du lịch được xếp hạng dựa trên thuật toán recommendation,
 * kèm điểm phù hợp, lý do gợi ý, và giải thích chi tiết giúp xây dựng niềm tin gợi ý.
 * Kết nối trực tiếp với [FeedViewModel] để nhận luồng dữ liệu an toàn theo mô hình UDF.
 *
 * @param viewModel ViewModel quản lý trạng thái dữ liệu màn hình ([FeedViewModel]). Mặc định là [hiltViewModel].
 */
@Composable
fun FeedScreen(
    viewModel: FeedViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    FeedScreenContent(
        uiState = uiState,
        onRetry = viewModel::loadRecommendations,
        onSearchQueryChange = viewModel::onSearchQueryChange,
        onCategorySelect = viewModel::onCategorySelect
    )
}

/**
 * Triển khai giao diện hiển thị chính (Content Layout) của màn hình Bảng tin gợi ý.
 *
 * Hiển thị thanh tìm kiếm, bộ lọc nhanh danh mục dạng LazyRow, tiêu đề "Gợi ý cho bạn"
 * và danh sách cuộn mượt mà các thẻ gợi ý [RecommendationCard] với animation xuất hiện.
 * Tự động chuyển đổi giữa các trạng thái tải dữ liệu, thông báo lỗi và trạng thái trống.
 *
 * @param uiState Đối tượng chứa toàn bộ trạng thái dữ liệu giao diện màn hình ([FeedUiState]).
 * @param onRetry Callback kích hoạt khi người dùng nhấn nút thử lại tải dữ liệu khi có lỗi.
 * @param onSearchQueryChange Callback kích hoạt khi từ khóa tìm kiếm thay đổi.
 * @param onCategorySelect Callback kích hoạt khi người dùng nhấn chọn lọc theo một danh mục.
 * @param modifier [Modifier] dùng để định hình bố cục bên ngoài truyền vào.
 */
@Composable
fun FeedScreenContent(
    uiState: FeedUiState,
    onRetry: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onCategorySelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        when {
            uiState.isLoading -> LoadingIndicator(modifier = Modifier.fillMaxSize())
            uiState.errorMessage != null -> ErrorView(
                message = uiState.errorMessage,
                onRetry = onRetry
            )
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = Dimens.gutterCard,
                        end = Dimens.gutterCard,
                        top = Dimens.gutterCard,
                        bottom = 80.dp // Padding cho Bottom Navigation Bar
                    ),
                    verticalArrangement = Arrangement.spacedBy(Dimens.gutterCard)
                ) {
                    // Thanh Tìm kiếm (Search Bar)
                    item(key = "search_bar") {
                        SearchBar(
                            query = uiState.searchQuery,
                            onQueryChanged = onSearchQueryChange,
                            placeholder = "Tìm địa điểm gợi ý...",
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Danh mục bộ lọc nhanh (LazyRow với Stable Key)
                    item(key = "category_filter") {
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(Dimens.spacingBase),
                            contentPadding = PaddingValues(vertical = Dimens.spacingBase)
                        ) {
                            items(
                                items = uiState.categories,
                                key = { it }
                            ) { category ->
                                CategoryChip(
                                    category = category,
                                    isSelected = uiState.selectedCategory == category,
                                    onSelected = { onCategorySelect(category) }
                                )
                            }
                        }
                    }

                    // Header "Gợi ý cho bạn"
                    item(key = "section_header") {
                        RecommendationSectionHeader(
                            itemCount = uiState.filteredRecommendations.size
                        )
                    }

                    // Hiển thị danh sách gợi ý hoặc màn hình trống
                    if (uiState.filteredRecommendations.isEmpty() && !uiState.isLoading) {
                        item(key = "empty_state") {
                            FeedEmptyState()
                        }
                    } else {
                        // Tối ưu hóa Recomposition bằng stable key
                        itemsIndexed(
                            items = uiState.filteredRecommendations,
                            key = { _, item -> item.id }
                        ) { index, item ->
                            RecommendationCard(
                                item = item,
                                rank = index + 1,
                                modifier = Modifier.animateItem(
                                    fadeInSpec = tween(durationMillis = 300),
                                    fadeOutSpec = tween(durationMillis = 300)
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Header section "Gợi ý cho bạn" với icon AutoAwesome và số lượng kết quả.
 *
 * @param itemCount Số lượng gợi ý hiện tại sau khi lọc.
 */
@Composable
private fun RecommendationSectionHeader(
    itemCount: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.spacingBase)
        ) {
            Icon(
                imageVector = Icons.Outlined.AutoAwesome,
                contentDescription = null,
                tint = CulturalAmber,
                modifier = Modifier.size(22.dp)
            )
            Text(
                text = "Gợi ý cho bạn",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        Text(
            text = "$itemCount địa điểm",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Trạng thái trống (Empty State) hiển thị khi không có gợi ý nào khớp với bộ lọc đã chọn.
 */
@Composable
private fun FeedEmptyState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 64.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "🔍", fontSize = 64.sp)
        Spacer(modifier = Modifier.height(Dimens.gutterCard))
        Text(
            text = "Không tìm thấy gợi ý phù hợp",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(Dimens.spacingBase))
        Text(
            text = "Thử thay đổi từ khóa hoặc danh mục",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline
        )
    }
}

@Preview(showBackground = true)
@Composable
fun FeedScreenPreview() {
    val mockItems = listOf(
        RecommendationItem(
            id = "rec_1",
            placeId = "place-hue-001",
            name = "Hoàng Thành Huế",
            category = "Lịch sử",
            thumbnailUrl = "",
            score = 0.97f,
            explanation = "Bạn đã ghé thăm 3 di tích lịch sử trong tháng qua. " +
                    "Hoàng Thành Huế là Di sản Thế giới UNESCO rất phù hợp với sở thích của bạn.",
            createdAt = "2026-06-14T08:00:00Z"
        ),
        RecommendationItem(
            id = "rec_2",
            placeId = "place-hoian-001",
            name = "Phố cổ Hội An",
            category = "Văn hóa",
            thumbnailUrl = "",
            score = 0.94f,
            explanation = "85% du khách yêu thích di tích lịch sử cũng đánh giá cao Phố cổ Hội An.",
            createdAt = "2026-06-14T08:00:00Z"
        )
    )
    BeVietnamTheme {
        FeedScreenContent(
            uiState = FeedUiState(
                recommendations = mockItems,
                filteredRecommendations = mockItems
            ),
            onRetry = {},
            onSearchQueryChange = {},
            onCategorySelect = {}
        )
    }
}
