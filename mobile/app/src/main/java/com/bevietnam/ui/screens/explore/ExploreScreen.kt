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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
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
import com.bevietnam.core.model.AreaWeather
import com.bevietnam.core.model.NearbyPlace
import com.bevietnam.core.model.Place
import com.bevietnam.ui.components.CulturalBackground
import com.bevietnam.ui.components.CulturalLoadingIndicator
import com.bevietnam.ui.components.ErrorView
import com.bevietnam.ui.components.PlaceCard
import com.bevietnam.ui.components.SearchBar
import com.bevietnam.ui.theme.BeVietnamTheme
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapLibreMapOptions
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.style.expressions.Expression
import org.maplibre.android.style.layers.CircleLayer
import org.maplibre.android.style.layers.Property
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.Point

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
    onPlaceClick: (Place) -> Unit = {}
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
        onUserLocated = viewModel::loadNearby,
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
    onUserLocated: (Double, Double) -> Unit,
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
                    onUserLocated = onUserLocated
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
    onUserLocated: (Double, Double) -> Unit
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
                onUserLocated = onUserLocated
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

// Dynamic markers — parity with the web Explore map. Three layers over one
// GeoJSON source, fed by live Foursquare POIs:
//   halo  – big translucent disc, size = distance-to-user (closer = bigger)
//   pin   – small solid category-coloured dot (the actual marker)
//   label – the POI's real name under the pin
private const val PLACES_SOURCE_ID = "bv-places"
private const val HALO_LAYER_ID = "bv-places-halo"
private const val PIN_LAYER_ID = "bv-places-pin"
private const val LABEL_LAYER_ID = "bv-places-label"

// Colour per coarse bucket returned by the backend. Default = gold.
private fun bubbleColorFor(bucket: String): String = when (bucket) {
    "history" -> "#B23A2E"   // lacquer red
    "culture" -> "#C69A3F"   // imperial gold
    "nature" -> "#3E7C5A"    // jade green
    "lodging" -> "#3B6EA5"   // blue
    "food" -> "#E07A3F"      // terracotta
    "place" -> "#8A6D3B"     // bronze
    else -> "#C69A3F"
}

private fun distanceMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val out = FloatArray(1)
    android.location.Location.distanceBetween(lat1, lon1, lat2, lon2, out)
    return out[0].toDouble()
}

private fun nearbyFeatureCollection(
    places: List<NearbyPlace>,
    userLat: Double?,
    userLng: Double?,
    focusedId: String?
): FeatureCollection {
    // Distance-to-user drives the halo radius. Without a fix, everything is mid-size.
    val dists = if (userLat != null && userLng != null) {
        places.associate { it.id to distanceMeters(userLat, userLng, it.latitude, it.longitude) }
    } else emptyMap()
    val maxDist = dists.values.maxOrNull() ?: 1.0

    val features = places.map { place ->
        val d = dists[place.id]
        // weight 1 = closest/biggest, 0 = farthest/smallest.
        val weight = if (d != null && maxDist > 0) (1.0 - d / maxDist) else 0.5
        val haloRadius = (16f + (weight * 24f)).toFloat() + if (place.id == focusedId) 8f else 0f
        Feature.fromGeometry(Point.fromLngLat(place.longitude, place.latitude)).apply {
            addStringProperty("id", place.id)
            addStringProperty("name", place.name)
            addStringProperty("color", bubbleColorFor(place.category))
            addNumberProperty("haloRadius", haloRadius)
            addBooleanProperty("selected", place.id == focusedId)
        }
    }
    return FeatureCollection.fromFeatures(features)
}

private fun addBubbleLayer(style: Style) {
    if (style.getSource(PLACES_SOURCE_ID) != null) return
    style.addSource(GeoJsonSource(PLACES_SOURCE_ID, FeatureCollection.fromFeatures(emptyList())))

    // 1) Translucent gold halo sized by distance-to-user.
    style.addLayer(
        CircleLayer(HALO_LAYER_ID, PLACES_SOURCE_ID).withProperties(
            PropertyFactory.circleRadius(Expression.get("haloRadius")),
            PropertyFactory.circleColor("#C69A3F"),
            PropertyFactory.circleOpacity(0.18f),
            PropertyFactory.circleStrokeColor("#C69A3F"),
            PropertyFactory.circleStrokeWidth(
                Expression.switchCase(
                    Expression.eq(Expression.get("selected"), Expression.literal(true)),
                    Expression.literal(2.5f),
                    Expression.literal(1.2f)
                )
            ),
            PropertyFactory.circleBlur(0.15f)
        )
    )

    // 2) Solid category-coloured pin dot (the marker itself).
    style.addLayer(
        CircleLayer(PIN_LAYER_ID, PLACES_SOURCE_ID).withProperties(
            PropertyFactory.circleRadius(
                Expression.switchCase(
                    Expression.eq(Expression.get("selected"), Expression.literal(true)),
                    Expression.literal(8f),
                    Expression.literal(6f)
                )
            ),
            PropertyFactory.circleColor(Expression.toColor(Expression.get("color"))),
            PropertyFactory.circleStrokeColor("#FFFAF0"),
            PropertyFactory.circleStrokeWidth(
                Expression.switchCase(
                    Expression.eq(Expression.get("selected"), Expression.literal(true)),
                    Expression.literal(3f),
                    Expression.literal(2f)
                )
            )
        )
    )

    // 3) Real POI name label under the pin.
    style.addLayer(
        SymbolLayer(LABEL_LAYER_ID, PLACES_SOURCE_ID).withProperties(
            PropertyFactory.textField(Expression.get("name")),
            PropertyFactory.textSize(11f),
            PropertyFactory.textOffset(arrayOf(0f, 1.3f)),
            PropertyFactory.textAnchor(Property.TEXT_ANCHOR_TOP),
            PropertyFactory.textAllowOverlap(false),
            PropertyFactory.textOptional(true),
            PropertyFactory.textColor("#2A2118"),
            PropertyFactory.textHaloColor("#FFF8EC"),
            PropertyFactory.textHaloWidth(1.4f)
        )
    )
}

@Composable
private fun ExploreMapView(
    state: ExploreUiState.Success,
    onCategorySelected: (String) -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onPlaceClick: (Place) -> Unit,
    onToggleViewMode: () -> Unit,
    onPlaceFocused: (String?) -> Unit,
    onUserLocated: (Double, Double) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val listState = rememberLazyListState()
    var mapState by remember { mutableStateOf<MapLibreMap?>(null) }

    // Get the real user position, then pull live POIs + weather around it.
    val fusedClient = remember {
        com.google.android.gms.location.LocationServices.getFusedLocationProviderClient(context)
    }

    fun deliverLocation(loc: android.location.Location?) {
        if (loc == null) return
        onUserLocated(loc.latitude, loc.longitude)
        mapState?.animateCamera(
            CameraUpdateFactory.newLatLngZoom(LatLng(loc.latitude, loc.longitude), 14.0)
        )
    }

    // Active fix (getCurrentLocation) is reliable even when no cached fix exists;
    // fall back to lastLocation if the active request yields nothing.
    fun requestFix() {
        try {
            val cts = com.google.android.gms.tasks.CancellationTokenSource()
            fusedClient.getCurrentLocation(
                com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY, cts.token
            ).addOnSuccessListener { fresh ->
                if (fresh != null) deliverLocation(fresh)
                else fusedClient.lastLocation.addOnSuccessListener { deliverLocation(it) }
            }.addOnFailureListener {
                fusedClient.lastLocation.addOnSuccessListener { deliverLocation(it) }
            }
        } catch (_: SecurityException) {
            // Permission revoked between check and call — leave map at default.
        }
    }

    val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        if (result.values.any { it }) requestFix()
    }

    LaunchedEffect(Unit) {
        val fineGranted = androidx.core.content.ContextCompat.checkSelfPermission(
            context, android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        val coarseGranted = androidx.core.content.ContextCompat.checkSelfPermission(
            context, android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        if (fineGranted || coarseGranted) {
            requestFix()
        } else {
            permissionLauncher.launch(
                arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
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

    val mapView = remember {
        MapLibre.getInstance(context)
        val options = MapLibreMapOptions.createFromAttributes(context, null).textureMode(true)
        MapView(context, options).apply {
            onCreate(null)
            getMapAsync { map ->
                mapState = map
                map.setStyle(
                    Style.Builder().fromUri("https://tiles.goong.io/assets/goong_map_web.json?api_key=${BuildConfig.GOONG_MAPTILES_KEY}")
                ) { style ->
                    addBubbleLayer(style)
                }

                val daNangLocation = LatLng(16.047079, 108.206230)
                map.cameraPosition = CameraPosition.Builder()
                    .target(daNangLocation)
                    .zoom(5.5)
                    .build()

                map.uiSettings.isCompassEnabled = false
                map.uiSettings.isLogoEnabled = false
                map.uiSettings.isAttributionEnabled = false

                // Tap a bubble to focus it; tap empty map to clear selection.
                map.addOnMapClickListener { latLng ->
                    val screenPoint = map.projection.toScreenLocation(latLng)
                    val hits = map.queryRenderedFeatures(screenPoint, PIN_LAYER_ID, HALO_LAYER_ID)
                    val placeId = hits.firstOrNull()
                        ?.getStringProperty("id")
                    onPlaceFocused(placeId) // null when tapping empty space
                    true
                }
            }
        }
    }


    // Sync bubbles: rebuild the GeoJSON source from the live nearby POIs whenever
    // the data, the user position, or the selection changes.
    LaunchedEffect(state.nearbyPlaces, state.userLatitude, state.userLongitude, state.focusedPlaceId) {
        mapView.getMapAsync { map ->
            map.style?.let { style ->
                addBubbleLayer(style) // no-op if already present
                (style.getSource(PLACES_SOURCE_ID) as? GeoJsonSource)?.setGeoJson(
                    nearbyFeatureCollection(
                        state.nearbyPlaces,
                        state.userLatitude,
                        state.userLongitude,
                        state.focusedPlaceId
                    )
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

        // Live area weather (temp / UV / rain) around the user.
        state.areaWeather?.let { weather ->
            AreaWeatherChip(
                weather = weather,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(top = 130.dp, start = 16.dp)
            )
        }

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

        // My-location button: re-fetch GPS and recenter the camera on the user.
        FloatingActionButton(
            onClick = { requestFix() },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 196.dp, end = 16.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(
                imageVector = Icons.Filled.MyLocation,
                contentDescription = stringResource(R.string.map_my_location)
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

/**
 * Chip nổi hiển thị điều kiện thời tiết khu vực (nhiệt độ, UV, lượng mưa) quanh người dùng.
 */
@Composable
private fun AreaWeatherChip(weather: AreaWeather, modifier: Modifier = Modifier) {
    val emoji = when (weather.condition) {
        "rainy" -> "🌧️"
        "cloudy" -> "☁️"
        "hot" -> "🔥"
        "sunny" -> "☀️"
        else -> "🌤️"
    }
    val parts = buildList {
        weather.temp?.let { add("${it.toInt()}°C") }
        weather.uvi?.let { add("UV ${it.toInt()}") }
        weather.rainMm?.let { if (it > 0) add("☔ ${"%.1f".format(it)}mm") }
    }
    Surface(
        modifier = modifier,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 2.dp
    ) {
        Text(
            text = "$emoji  ${parts.joinToString(" · ")}",
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
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
            onUserLocated = { _, _ -> }
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
            onUserLocated = { _, _ -> }
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
            onUserLocated = { _, _ -> }
        )
    }
}
