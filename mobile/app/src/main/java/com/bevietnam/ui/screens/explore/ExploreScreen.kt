package com.bevietnam.ui.screens.explore

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Restaurant
import com.bevietnam.ui.content.CultureContent
import com.bevietnam.ui.content.CultureStory
import com.bevietnam.ui.content.FoodItem
import com.bevietnam.ui.theme.LocalCulturalColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.bevietnam.BuildConfig
import com.bevietnam.R
import com.bevietnam.core.model.Place
import com.bevietnam.ui.components.CulturalBackground
import com.bevietnam.ui.components.CulturalLoadingIndicator
import com.bevietnam.ui.components.ErrorView
import com.bevietnam.ui.components.PlaceCard
import com.bevietnam.ui.components.SearchBar
import com.bevietnam.ui.theme.BeVietnamTheme
import org.maplibre.android.MapLibre
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapLibreMapOptions
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style

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
    modifier: Modifier = Modifier,
    viewModel: ExploreViewModel = hiltViewModel(),
    onPlaceClick: (Place) -> Unit = {},
    onFoodClick: (String) -> Unit = {},
    onStoryClick: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    ExploreScreenContent(
        uiState = uiState,
        onRetry = viewModel::loadPlaces,
        onCategorySelected = viewModel::onCategorySelected,
        onSearchQueryChanged = viewModel::onSearchQueryChanged,
        onPlaceClick = onPlaceClick,
        onToggleViewMode = viewModel::toggleViewMode,
        onPlaceFocused = viewModel::onPlaceFocused,
        onFoodClick = onFoodClick,
        onStoryClick = onStoryClick,
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
    onFoodClick: (String) -> Unit = {},
    onStoryClick: (String) -> Unit = {},
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
                    onFoodClick = onFoodClick,
                    onStoryClick = onStoryClick
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
    onFoodClick: (String) -> Unit,
    onStoryClick: (String) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        if (state.isMapView) {
            ExploreMapView(
                state = state,
                onCategorySelected = onCategorySelected,
                onSearchQueryChanged = onSearchQueryChanged,
                onPlaceClick = onPlaceClick,
                onToggleViewMode = onToggleViewMode,
                onPlaceFocused = onPlaceFocused
            )
        } else {
            ExploreListView(
                state = state,
                onCategorySelected = onCategorySelected,
                onSearchQueryChanged = onSearchQueryChanged,
                onPlaceClick = onPlaceClick,
                onToggleViewMode = onToggleViewMode,
                onFoodClick = onFoodClick,
                onStoryClick = onStoryClick
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
    onPlaceFocused: (String?) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val listState = rememberLazyListState()
    var mapState by remember { mutableStateOf<MapLibreMap?>(null) }

    // Sync Carousel to Map selection
    LaunchedEffect(state.focusedPlaceId) {
        val index = state.filteredPlaces.indexOfFirst { it.id == state.focusedPlaceId }
        if (index >= 0) {
            listState.animateScrollToItem(index)
        }
    }

    val mapView = remember {
        MapLibre.getInstance(context)
        val options = MapLibreMapOptions.createFromAttributes(context, null).textureMode(true)
        MapView(context, options).apply {
            onCreate(null)
            getMapAsync { map ->
                mapState = map
                map.setStyle(Style.Builder().fromUri("https://tiles.goong.io/assets/goong_map_web.json?api_key=${BuildConfig.GOONG_MAPTILES_KEY}"))
                
                val daNangLocation = LatLng(16.047079, 108.206230)
                map.cameraPosition = CameraPosition.Builder()
                    .target(daNangLocation)
                    .zoom(5.5)
                    .build()
                
                map.uiSettings.isCompassEnabled = false
                map.uiSettings.isLogoEnabled = false
                map.uiSettings.isAttributionEnabled = false

                map.addOnMapClickListener {
                    onPlaceFocused(null)
                    true
                }

                map.setOnMarkerClickListener { marker ->
                    val placeId = marker.snippet
                    if (placeId != null) {
                        onPlaceFocused(placeId)
                    }
                    true
                }
            }
        }
    }


    // Sync markers
    LaunchedEffect(state.filteredPlaces) {
        mapView.getMapAsync { map ->
            map.clear()
            state.filteredPlaces.forEach { place ->
                map.addMarker(
                    MarkerOptions()
                        .position(LatLng(place.latitude, place.longitude))
                        .title(place.name)
                        .snippet(place.id) // Use snippet to store the place ID
                )
            }
        }
    }

    // Handle MapView Lifecycle
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(factory = { mapView }, modifier = Modifier.fillMaxSize())

        // Map Reset Button
        FloatingActionButton(
            onClick = {
                mapState?.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(16.047079, 108.206230),
                        5.5
                    )
                )
            },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 130.dp, end = 16.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(
                imageVector = Icons.Filled.Map,
                contentDescription = stringResource(R.string.map_reset_view)
            )
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
                        onLocateClick = {
                            mapState?.animateCamera(
                                CameraUpdateFactory.newLatLngZoom(
                                    LatLng(place.latitude, place.longitude),
                                    14.0
                                )
                            )
                        },
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
    onToggleViewMode: () -> Unit,
    onFoodClick: (String) -> Unit,
    onStoryClick: (String) -> Unit
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

        // Ẩm thực Việt (carousel)
        item { FoodSection(onFoodClick = onFoodClick) }

        // Bạn có biết? (fun fact)
        item { FactCard() }

        // Tiêu đề mục địa điểm + tổng số kết quả
        item {
            SectionHeader(title = "Địa điểm nổi bật", icon = Icons.Default.Place)
        }
        item {
            Text(
                text = stringResource(R.string.places_count, state.filteredPlaces.size),
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
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

        // Câu chuyện văn hóa
        item { StorySection(onStoryClick = onStoryClick) }
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

/**
 * Tiêu đề một mục nội dung (icon + chữ).
 */
@Composable
private fun SectionHeader(title: String, icon: ImageVector) {
    Row(
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(6.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
    }
}

/**
 * Mục Ẩm thực Việt: carousel cuộn ngang các món ăn, bấm vào mở chi tiết.
 */
@Composable
private fun FoodSection(onFoodClick: (String) -> Unit) {
    Column {
        SectionHeader(title = "Ẩm thực Việt", icon = Icons.Default.Restaurant)
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(items = CultureContent.foods, key = { it.id }) { food ->
                FoodCard(food = food, onClick = { onFoodClick(food.id) })
            }
        }
    }
}

@Composable
private fun FoodCard(food: FoodItem, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .width(112.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(74.dp)
                    .background(Color(food.colorHex)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Restaurant,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.55f),
                    modifier = Modifier.size(28.dp)
                )
            }
            Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 7.dp)) {
                Text(food.name, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                Text(food.region, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

/**
 * Mục "Bạn có biết?": thẻ fun fact văn hóa.
 */
@Composable
private fun FactCard() {
    val culturalColors = LocalCulturalColors.current
    val fact = "Áo dài Việt Nam có nguồn gốc từ thế kỷ 18, được cách tân thành dáng hiện đại vào những năm 1930."
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        color = culturalColors.amberColor.copy(alpha = 0.12f)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Lightbulb, contentDescription = null, tint = culturalColors.amberColor, modifier = Modifier.size(17.dp))
                Spacer(Modifier.width(6.dp))
                Text("Bạn có biết?", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = culturalColors.amberColor)
            }
            Spacer(Modifier.height(5.dp))
            Text(fact, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface, lineHeight = 17.sp)
        }
    }
}

/**
 * Mục Câu chuyện văn hóa: thẻ truyện, bấm vào mở trang đọc đầy đủ.
 */
@Composable
private fun StorySection(onStoryClick: (String) -> Unit) {
    val story: CultureStory = CultureContent.stories.first()
    Column {
        SectionHeader(title = "Câu chuyện văn hóa", icon = Icons.Default.MenuBook)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .clip(RoundedCornerShape(12.dp))
                .clickable { onStoryClick(story.id) },
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 1.dp
        ) {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .background(Color(story.colorHex)),
                    contentAlignment = Alignment.BottomStart
                ) {
                    Surface(
                        modifier = Modifier.padding(9.dp),
                        shape = RoundedCornerShape(20.dp),
                        color = Color.Black.copy(alpha = 0.4f)
                    ) {
                        Text(story.tag, color = Color.White, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                    }
                }
                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                    Text(story.title, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(Modifier.height(3.dp))
                    Text(story.excerpt, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 16.sp)
                    Spacer(Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Đọc tiếp", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(3.dp))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(13.dp))
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
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
            onPlaceFocused = {}
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
            onPlaceFocused = {}
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
            onPlaceFocused = {}
        )
    }
}
