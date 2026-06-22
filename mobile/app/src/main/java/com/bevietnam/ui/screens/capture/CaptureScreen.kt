package com.bevietnam.ui.screens.capture

import android.Manifest
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.bevietnam.R
import androidx.compose.ui.res.stringResource
import com.bevietnam.ui.components.BeVietnamTextField
import com.bevietnam.ui.components.CulturalBackground
import com.bevietnam.ui.components.PrimaryLoadingButton
import com.bevietnam.ui.theme.LocalCulturalColors

/**
 * Màn hình Đăng bài viết khám phá (Capture Screen) của ứng dụng BeVietnam.
 *
 * Cho phép người dùng chụp ảnh hoặc chọn ảnh phong cảnh từ thư viện, tự động kiểm tra và yêu cầu
 * cấp quyền hệ thống (Camera & GPS Location) và đăng bài viết chia sẻ kèm tọa độ thực tế.
 * Kết nối chặt chẽ với [CaptureViewModel] theo chuẩn Unidirectional Data Flow (UDF).
 *
 * @param onNavigateBack Callback điều hướng quay trở lại màn hình trước đó.
 * @param viewModel ViewModel quản lý trạng thái màn hình Đăng bài ([CaptureViewModel]). Mặc định là [hiltViewModel].
 * @param modifier [Modifier] dùng để định hình bố cục bên ngoài truyền vào.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaptureScreen(
    onNavigateBack: () -> Unit,
    viewModel: CaptureViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var description by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()
    var showCamera by remember { mutableStateOf(false) }

    var launchCameraAfterPermission by remember { mutableStateOf(false) }

    // Bệ phóng yêu cầu Cấp quyền Máy ảnh
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.onPermissionResult(Manifest.permission.CAMERA, isGranted)
        if (isGranted && launchCameraAfterPermission) {
            showCamera = true
        }
        launchCameraAfterPermission = false
    }

    val fusedLocationClient = remember { com.google.android.gms.location.LocationServices.getFusedLocationProviderClient(context) }

    // Bệ phóng yêu cầu Cấp quyền Truy cập Vị trí địa lý (GPS)
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.entries.any { it.value } // Only need one to be true (coarse or fine)
        viewModel.onPermissionResult(Manifest.permission.ACCESS_FINE_LOCATION, granted)
    }

    // Kiểm tra trạng thái cấp quyền ngay khi vào màn hình
    LaunchedEffect(Unit) {
        val hasCamera = androidx.core.content.ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == android.content.pm.PackageManager.PERMISSION_GRANTED
        viewModel.onPermissionResult(Manifest.permission.CAMERA, hasCamera)

        val hasFine = androidx.core.content.ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED
        val hasCoarse = androidx.core.content.ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED
        viewModel.onPermissionResult(Manifest.permission.ACCESS_FINE_LOCATION, hasFine || hasCoarse)
    }

    // Lắng nghe trạng thái quyền vị trí để lấy tọa độ thực
    LaunchedEffect(uiState.locationPermissionGranted) {
        if (uiState.locationPermissionGranted) {
            if (androidx.core.content.ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED ||
                androidx.core.content.ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                try {
                    fusedLocationClient.getCurrentLocation(com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY, null)
                        .addOnSuccessListener { location ->
                            if (location != null) {
                                viewModel.onLocationUpdated(location.latitude, location.longitude)
                            }
                        }
                } catch (e: SecurityException) {
                    // Ignore
                }
            }
        }
    }

    // Bệ phóng chọn tệp hình ảnh từ Thư viện (Gallery)
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        viewModel.onImageCaptured(uri)
    }

    // Lắng nghe trạng thái đăng bài thành công để điều hướng quay lại
    LaunchedEffect(uiState.uploadSuccess) {
        if (uiState.uploadSuccess) {
            Toast.makeText(context, context.getString(R.string.capture_success), Toast.LENGTH_SHORT).show()
            onNavigateBack()
            viewModel.resetState()
        }
    }

    if (showCamera) {
        CameraPreview(
            onPhotoCaptured = { uri ->
                viewModel.onImageCaptured(uri)
                showCamera = false
            },
            onCancel = {
                showCamera = false
            }
        )
        return
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.capture_title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    TextButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.minimumInteractiveComponentSize() // Đảm bảo Touch Target 48dp
                    ) {
                        Text(stringResource(R.string.capture_cancel))
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        CulturalBackground {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Khu vực xem trước hình ảnh (Image Preview Area) hoặc Mời gọi chụp ảnh
            if (uiState.imageUri != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
                ) {
                    // Tải ảnh mượt mà với Coil AsyncImage và crossfade
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(uiState.imageUri)
                            .crossfade(true)
                            .build(),
                        contentDescription = stringResource(R.string.capture_preview_desc),
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    
                    // Nút Xóa ảnh để chụp lại
                    SmallFloatingActionButton(
                        onClick = { viewModel.onImageCaptured(null) },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(12.dp),
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Bỏ ảnh này")
                    }
                }
            } else {
                // Dòng tùy chọn chọn nguồn ảnh (Thư viện / Máy ảnh) khi chưa có ảnh
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CaptureOptionButton(
                        icon = Icons.Default.AddPhotoAlternate,
                        label = stringResource(R.string.capture_gallery),
                        onClick = { galleryLauncher.launch("image/*") },
                        modifier = Modifier
                            .weight(1f)
                            .height(140.dp) // Nổi bật hơn
                    )
                    CaptureOptionButton(
                        icon = Icons.Default.CameraAlt,
                        label = stringResource(R.string.capture_camera),
                        onClick = {
                            if (uiState.cameraPermissionGranted) {
                                showCamera = true
                            } else {
                                launchCameraAfterPermission = true
                                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(140.dp)
                    )
                }
            }

            // Các mục trạng thái và yêu cầu cấp quyền hệ thống
            PermissionStatusItem(
                label = stringResource(R.string.capture_permission_camera),
                isGranted = uiState.cameraPermissionGranted,
                onRequest = { 
                    launchCameraAfterPermission = false
                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA) 
                }
            )

            PermissionStatusItem(
                label = stringResource(R.string.capture_permission_location),
                isGranted = uiState.locationPermissionGranted,
                onRequest = {
                    locationPermissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                }
            )

            // Trường nhập nội dung cảm nghĩ/mô tả bài viết
            BeVietnamTextField(
                label = stringResource(R.string.capture_note_label),
                value = description,
                onValueChange = { description = it },
                placeholder = stringResource(R.string.capture_note_hint),
                leadingIcon = Icons.Default.Create,
                modifier = Modifier.fillMaxWidth()
            )

            uiState.errorMessage?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Nút đăng bài viết
            PrimaryLoadingButton(
                text = stringResource(R.string.capture_submit),
                isLoading = uiState.isUploading,
                onClick = { viewModel.uploadCapture(description) },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(24.dp))
        }
        }
    }
}

/**
 * Nút nhấn chọn nguồn ảnh (Capture Option Button) như Thư viện hay Máy ảnh.
 *
 * Tối ưu hóa hiệu ứng Ripple và Touch Target bằng cách dùng `Card(onClick = ...)` trực tiếp.
 *
 * @param icon Biểu tượng vectơ hiển thị trên thẻ ([ImageVector]).
 * @param label Nhãn chuỗi hiển thị tên tùy chọn.
 * @param onClick Callback nhấn nút.
 * @param modifier [Modifier] dùng để định hình bố cục bên ngoài truyền vào.
 */
@Composable
fun CaptureOptionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick, // Ripple mượt mà ôm sát bo góc Card theo đúng chuẩn Material 3
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon, 
                contentDescription = null, 
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = label, fontWeight = FontWeight.Medium, fontSize = 14.sp)
        }
    }
}

/**
 * Mục hiển thị trạng thái cấp quyền hệ thống (Permission Status Item).
 *
 * @param label Tên quyền cần hiển thị (ví dụ: Quyền vị trí).
 * @param isGranted Trạng thái quyền đã được cấp hay chưa.
 * @param onRequest Callback kích hoạt yêu cầu cấp quyền khi nhấn nút.
 */
@Composable
fun PermissionStatusItem(
    label: String,
    isGranted: Boolean,
    onRequest: () -> Unit
) {
    val culturalColors = LocalCulturalColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (isGranted) culturalColors.permissionGreenBg else culturalColors.permissionOrangeBg)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (isGranted) Icons.Default.CheckCircle else Icons.Default.LocationOn,
                contentDescription = null,
                tint = if (isGranted) culturalColors.permissionGreenText else culturalColors.permissionOrangeText,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isGranted) culturalColors.permissionGreenText else culturalColors.permissionOrangeText
            )
        }
        if (!isGranted) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(
                    onClick = onRequest,
                    modifier = Modifier.minimumInteractiveComponentSize()
                ) {
                    Text(stringResource(R.string.capture_grant_permission), fontSize = 12.sp)
                }
                val context = LocalContext.current
                TextButton(
                    onClick = {
                        val intent = android.content.Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = android.net.Uri.fromParts("package", context.packageName, null)
                        }
                        context.startActivity(intent)
                    },
                    modifier = Modifier.minimumInteractiveComponentSize()
                ) {
                    Text(stringResource(R.string.capture_settings), fontSize = 12.sp)
                }
            }
        } else {
            Text(stringResource(R.string.capture_granted), fontSize = 12.sp, color = culturalColors.permissionGreenText)
        }
    }
}
