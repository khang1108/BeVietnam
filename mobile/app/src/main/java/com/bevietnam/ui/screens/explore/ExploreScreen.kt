package com.bevietnam.ui.screens.explore

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.pm.PackageManager
import androidx.hilt.navigation.compose.hiltViewModel
import com.bevietnam.R
import com.bevietnam.core.model.Place
import com.bevietnam.ui.components.CategoryChip
import com.bevietnam.ui.components.CulturalBackground
import com.bevietnam.ui.components.CulturalLoadingIndicator
import com.bevietnam.ui.components.ErrorView
import com.bevietnam.ui.components.PlaceCard
import com.bevietnam.ui.components.SearchBar
import com.bevietnam.ui.theme.BeVietnamTheme
import com.bevietnam.ui.theme.LocalCulturalColors
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch

/**
 * Màn hình Khám phá địa điểm du lịch văn hóa (Explore Screen) của ứng dụng BeVietnam.
 *
 * Hiển thị danh sách các danh lam thắng cảnh, di sản lịch sử của Việt Nam kèm bộ lọc nhanh theo danh mục.
 * Kết nối dữ liệu dạng luồng quan sát từ [ExploreViewModel] và tuân thủ quy chuẩn Unidirectional Data Flow (UDF).
 *
 * @param viewModel ViewModel quản lý trạng thái dữ liệu màn hình Khám phá ([ExploreViewModel]). Mặc định là [hiltViewModel].
 * @param onPlaceClick Sự kiện click chọn xem chi tiết một địa điểm cụ thể ([Place]).
 * @param modifier [Modifier] dùng để định hình bố cục bên ngoài truyền vào.
 */
@Composable
fun ExploreScreen(
    viewModel: ExploreViewModel = hiltViewModel(),
    onPlaceClick: (Place) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Cập nhật trạng thái quyền vị trí ban đầu cho ViewModel
    LaunchedEffect(Unit) {
        val hasPerm = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                      ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        viewModel.updateLocationPermission(hasPerm)
    }

    ExploreScreenContent(
        uiState = uiState,
        onRetry = viewModel::loadPlaces,
        onCategorySelected = viewModel::onCategorySelected,
        onSearchQueryChanged = viewModel::onSearchQueryChanged,
        onPlaceClick = onPlaceClick,
        onToggleViewMode = viewModel::toggleViewMode,
        onPlaceFocused = viewModel::onPlaceFocused,
        onPermissionResult = viewModel::updateLocationPermission,
        modifier = modifier
    )
}

/**
 * Triển khai phân phối giao diện chính của màn hình Khám phá.
 *
 * Tự động chuyển mạch hiển thị giữa trạng thái đang tải (Shimmer loading), trạng thái rỗng,
 * trạng thái báo lỗi mạng và trạng thái tải dữ liệu thành công chứa danh sách địa điểm.
 *
 * @param uiState Đối tượng đại diện cho các trạng thái màn hình ([ExploreUiState]).
 * @param onRetry Callback nhấn thử lại khi tải dữ liệu gặp sự cố.
 * @param onCategorySelected Callback nhấn chọn danh mục bộ lọc nhanh.
 * @param onSearchQueryChanged Callback gõ tìm kiếm từ khóa địa điểm.
 * @param onPlaceClick Callback xem chi tiết địa điểm.
 * @param modifier [Modifier] dùng để định hình bố cục bên ngoài truyền vào.
 */
@Composable
fun ExploreScreenContent(
    uiState: ExploreUiState,
    onRetry: () -> Unit,
    onCategorySelected: (String) -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onPlaceClick: (Place) -> Unit,
    onToggleViewMode: () -> Unit,
    onPlaceFocused: (String?) -> Unit,
    onPermissionResult: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        CulturalBackground {
            when (uiState) {
                is ExploreUiState.Loading -> ExploreLoadingState()
                is ExploreUiState.Empty -> ExploreEmptyState(
                    onRetry = onRetry
                )
                is ExploreUiState.Error -> ErrorView(
                    message = uiState.message,
                    onRetry = onRetry
                )
                is ExploreUiState.Success -> ExploreSuccessContent(
                    state = uiState,
                    onCategorySelected = onCategorySelected,
                    onSearchQueryChanged = onSearchQueryChanged,
                    onPlaceClick = onPlaceClick,
                    onToggleViewMode = onToggleViewMode,
                    onPlaceFocused = onPlaceFocused,
                    onPermissionResult = onPermissionResult
                )
            }
        }
    }
}

/**
 * Giao diện khi tải dữ liệu thành công (Success State) của màn hình Khám phá.
 *
 * Chứa thanh tìm kiếm địa danh, thanh trượt ngang bộ lọc danh mục và danh sách cuộn các địa điểm phù hợp.
 */
@Composable
private fun ExploreSuccessContent(
    state: ExploreUiState.Success,
    onCategorySelected: (String) -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onPlaceClick: (Place) -> Unit,
    onToggleViewMode: () -> Unit,
    onPlaceFocused: (String?) -> Unit,
    onPermissionResult: (Boolean) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        if (state.isMapView) {
            ExploreMapView(
                state = state,
                onCategorySelected = onCategorySelected,
                onSearchQueryChanged = onSearchQueryChanged,
                onPlaceClick = onPlaceClick,
                onToggleViewMode = onToggleViewMode,
                onPlaceFocused = onPlaceFocused,
                onPermissionResult = onPermissionResult
            )
        } else {
            ExploreListView(
                state = state,
                onCategorySelected = onCategorySelected,
                onSearchQueryChanged = onSearchQueryChanged,
                onPlaceClick = onPlaceClick,
                onToggleViewMode = onToggleViewMode
            )
        }
    }
}

@Composable
private fun ExploreMapView(
    state: ExploreUiState.Success,
    onCategorySelected: (String) -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onPlaceClick: (Place) -> Unit,
    onToggleViewMode: () -> Unit,
    onPlaceFocused: (String?) -> Unit,
    onPermissionResult: (Boolean) -> Unit
) {
    val daNangLocation = LatLng(16.047079, 108.206230)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(daNangLocation, 5.5f)
    }
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    // Launcher yêu cầu quyền vị trí
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                          permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
            onPermissionResult(granted)
        }
    )

    // Tự động yêu cầu quyền khi mở map nếu chưa có
    LaunchedEffect(Unit) {
        if (!state.hasLocationPermission) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    // Sync Carousel to Map selection
    LaunchedEffect(state.focusedPlaceId) {
        val index = state.filteredPlaces.indexOfFirst { it.id == state.focusedPlaceId }
        if (index >= 0) {
            listState.animateScrollToItem(index)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = state.hasLocationPermission),
            uiSettings = MapUiSettings(
                zoomControlsEnabled = false, 
                compassEnabled = false,
                myLocationButtonEnabled = state.hasLocationPermission
            ),
            contentPadding = PaddingValues(top = 130.dp), // Đẩy nút My Location xuống dưới thanh SearchBar và CategoryFilter
            onMapClick = { onPlaceFocused(null) }
        ) {
            state.filteredPlaces.forEach { place ->
                val position = LatLng(place.latitude, place.longitude)
                Marker(
                    state = MarkerState(position = position),
                    title = place.name,
                    onClick = {
                        onPlaceFocused(place.id)
                        true // Consume click to prevent default InfoWindow from automatically centering if desired
                    }
                )
            }
        }

        // Overlay Search and Filters (Glassmorphism effect is on the components)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(bottom = 12.dp)
        ) {
            SearchBar(
                query = state.searchQuery,
                onQueryChanged = onSearchQueryChanged,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                trailingIcon = {
                    IconButton(onClick = onToggleViewMode) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.List,
                            contentDescription = stringResource(R.string.list_view),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
            CategoryFilterRow(
                categories = categories,
                selectedCategory = state.selectedCategory,
                onCategorySelected = onCategorySelected
            )
        }

        // Overlay PlaceCards (Carousel) at bottom
        if (state.filteredPlaces.isNotEmpty()) {
            LazyRow(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 120.dp), // padding to clear FAB and BottomNavBar
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(
                    items = state.filteredPlaces,
                    key = { _, place -> place.id }
                ) { index, place ->
                    PlaceCard(
                        place = place,
                        index = index + 1,
                        onClick = { onPlaceClick(place) },
                        modifier = Modifier
                            .width(280.dp)
                            .height(130.dp)
                            .padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ExploreListView(
    state: ExploreUiState.Success,
    onCategorySelected: (String) -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onPlaceClick: (Place) -> Unit,
    onToggleViewMode: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        // Ô Tìm kiếm (Search Bar)
        item {
            SearchBar(
                query = state.searchQuery,
                onQueryChanged = onSearchQueryChanged,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                trailingIcon = {
                    IconButton(onClick = onToggleViewMode) {
                        Icon(
                            imageVector = Icons.Default.Map,
                            contentDescription = stringResource(R.string.map_view),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        }

        // Bộ lọc nhanh danh mục (Category filter row)
        item {
            CategoryFilterRow(
                categories = categories,
                selectedCategory = state.selectedCategory,
                onCategorySelected = onCategorySelected
            )
        }

        // Dòng hiển thị tổng số kết quả
        item {
            Text(
                text = stringResource(R.string.places_count, state.filteredPlaces.size),
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        // Kết quả tìm kiếm hoặc trạng thái trống
        if (state.filteredPlaces.isEmpty()) {
            item {
                SearchEmptyState(query = state.searchQuery)
            }
        } else {
            // Sử dụng itemsIndexed có tích hợp stable key để tối ưu hóa hiệu năng recomposition
            itemsIndexed(
                items = state.filteredPlaces,
                key = { _, place -> place.id }
            ) { index, place ->
                PlaceCard(
                    place = place,
                    index = index + 1,
                    onClick = { onPlaceClick(place) },
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }
        }
    }
}

/**
 * Thanh cuộn ngang các mục danh mục bộ lọc nhanh.
 *
 * Tích hợp stable key cho hiệu năng render danh sách mượt mà nhất.
 */
@Composable
private fun CategoryFilterRow(
    categories: List<String>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Tối ưu hóa Recomposition bằng cách cung cấp stable key là tên danh mục duy nhất
        items(
            items = categories,
            key = { it }
        ) { category ->
            val isSelected = category == selectedCategory
            com.bevietnam.ui.components.CategoryChip(
                category = category,
                isSelected = isSelected,
                onSelected = { onCategorySelected(category) },
                modifier = Modifier.animateItem(
                    fadeInSpec = androidx.compose.animation.core.tween(300)
                )
            )
        }
    }
}

/**
 * Trạng thái đang tải dữ liệu (Loading State) hiển thị hiệu ứng Shimmer mượt mà.
 */
@Composable
private fun ExploreLoadingState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CulturalLoadingIndicator()
    }
}



/**
 * Trạng thái trống (Empty State) hiển thị khi chưa có bất kỳ địa điểm nào trên hệ thống.
 */
@Composable
private fun ExploreEmptyState(message: String = stringResource(R.string.empty_places), onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "🗺️", fontSize = 64.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = message, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onRetry, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) {
            Text(text = stringResource(R.string.retry), color = Color.White)
        }
    }
}

/**
 * Trạng thái trống của kết quả tìm kiếm (Search Empty State) hiển thị khi không có địa danh nào khớp từ khóa.
 */
@Composable
private fun SearchEmptyState(query: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "🔍", fontSize = 48.sp)
        Text(text = stringResource(R.string.search_not_found, query), fontWeight = FontWeight.SemiBold)
    }
}

@Preview(showBackground = true)
@Composable
fun ExploreScreenLoadingPreview() {
    BeVietnamTheme {
        ExploreScreenContent(
            uiState = ExploreUiState.Loading,
            onRetry = {},
            onCategorySelected = {},
            onSearchQueryChanged = {},
            onPlaceClick = {},
            onToggleViewMode = {},
            onPlaceFocused = {},
            onPermissionResult = {}
        )
    }
}

@Preview(showBackground = true, name = "Map View Mode")
@Composable
fun ExploreScreenMapViewPreview() {
    // Trigger preview refresh for new colors
    val mockPlaces = listOf(
        Place(id = "1", name = "Vịnh Hạ Long", category = "Thiên nhiên", description = "Di sản thiên nhiên thế giới", latitude = 20.9101, longitude = 107.1839, imageUrl = "", referenceUrl = "https://example.com"),
        Place(id = "2", name = "Phố cổ Hội An", category = "Lịch sử", description = "Thành phố cổ kính bên sông Hoài", latitude = 15.8801, longitude = 108.3380, imageUrl = "", referenceUrl = "https://example.com")
    )
    BeVietnamTheme {
        ExploreScreenContent(
            uiState = ExploreUiState.Success(places = mockPlaces, filteredPlaces = mockPlaces, isMapView = true),
            onRetry = {},
            onCategorySelected = {},
            onSearchQueryChanged = {},
            onPlaceClick = {},
            onToggleViewMode = {},
            onPlaceFocused = {},
            onPermissionResult = {}
        )
    }
}

@Preview(showBackground = true, name = "List View Mode")
@Composable
fun ExploreScreenListViewPreview() {
    val mockPlaces = listOf(
        Place(id = "1", name = "Vịnh Hạ Long", category = "Thiên nhiên", description = "Di sản thiên nhiên thế giới", latitude = 20.9101, longitude = 107.1839, imageUrl = "", referenceUrl = "https://example.com"),
        Place(id = "2", name = "Phố cổ Hội An", category = "Lịch sử", description = "Thành phố cổ kính bên sông Hoài", latitude = 15.8801, longitude = 108.3380, imageUrl = "", referenceUrl = "https://example.com")
    )
    BeVietnamTheme {
        ExploreScreenContent(
            uiState = ExploreUiState.Success(places = mockPlaces, filteredPlaces = mockPlaces, isMapView = false),
            onRetry = {},
            onCategorySelected = {},
            onSearchQueryChanged = {},
            onPlaceClick = {},
            onToggleViewMode = {},
            onPlaceFocused = {},
            onPermissionResult = {}
        )
    }
}
