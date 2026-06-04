package com.bevietnam.ui.screens.feed

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bevietnam.core.model.FeedItem
import com.bevietnam.ui.components.*
import com.bevietnam.ui.theme.BeVietnamTheme

@Composable
fun FeedScreen(
    viewModel: FeedViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    FeedScreenContent(
        uiState = uiState,
        onRetry = viewModel::loadFeed,
        onLikeClick = viewModel::onLikeClick,
        onSearchQueryChange = viewModel::onSearchQueryChange,
        onCategorySelect = viewModel::onCategorySelect
    )
}

@Composable
fun FeedScreenContent(
    uiState: FeedUiState,
    onRetry: () -> Unit,
    onLikeClick: (String) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onCategorySelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = Color(0xFFFAF5E4)
    ) {
        when {
            uiState.isLoading -> LoadingIndicator()
            uiState.errorMessage != null -> ErrorView(
                message = uiState.errorMessage!!,
                onRetry = onRetry
            )
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        SearchBar(
                            query = uiState.searchQuery,
                            onQueryChanged = onSearchQueryChange,
                            placeholder = "Khám phá Việt Nam..."
                        )
                    }

                    item {
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            items(uiState.categories) { category ->
                                CategoryChip(
                                    category = category,
                                    isSelected = uiState.selectedCategory == category,
                                    onSelected = { onCategorySelect(category) }
                                )
                            }
                        }
                    }

                    if (uiState.filteredItems.isEmpty() && !uiState.isLoading) {
                        item {
                            FeedEmptyState()
                        }
                    } else {
                        items(uiState.filteredItems) { item ->
                            FeedCard(
                                feedItem = item,
                                onLikeClick = { onLikeClick(item.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FeedEmptyState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 64.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "📭", fontSize = 64.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Không tìm thấy bài đăng nào",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(showBackground = true)
@Composable
fun FeedScreenPreview() {
    val mockFeedItems = listOf(
        FeedItem(
            id = "1",
            userId = "u1",
            userName = "Fabian",
            userAvatarUrl = "",
            content = "Hạ Long thật tuyệt!",
            imageUrl = null,
            timestamp = "2 giờ trước",
            likesCount = 1250,
            commentsCount = 45,
            location = "Hà Nội, Việt Nam",
            category = "Travel"
        )
    )
    BeVietnamTheme {
        FeedScreenContent(
            uiState = FeedUiState(
                feedItems = mockFeedItems,
                filteredItems = mockFeedItems
            ),
            onRetry = {},
            onLikeClick = {},
            onSearchQueryChange = {},
            onCategorySelect = {}
        )
    }
}
