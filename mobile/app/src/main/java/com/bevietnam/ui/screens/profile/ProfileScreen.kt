package com.bevietnam.ui.screens.profile

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.bevietnam.R
import com.bevietnam.core.model.User
import com.bevietnam.ui.theme.BeVietnamTheme
import com.bevietnam.ui.components.CulturalBackground
import com.bevietnam.ui.components.CulturalLoadingIndicator
import com.bevietnam.ui.components.ErrorView
import com.bevietnam.ui.components.LoadingIndicator
import androidx.compose.foundation.lazy.items
import com.bevietnam.ui.navigation.BottomNavBar
import com.bevietnam.ui.components.PrimaryLoadingButton
import java.time.format.DateTimeFormatter
import java.util.Calendar

/**
 * Màn hình Hồ sơ cá nhân (Profile Screen) của ứng dụng BeVietnam.
 *
 * Cho phép người dùng theo dõi cấp độ, điểm số hành trình, chỉnh sửa thông tin cá nhân (Họ tên, Bio, Giới tính, Ngày sinh, Vị trí)
 * và thực hiện đăng xuất khỏi hệ thống.
 * Kết nối luồng dữ liệu từ [ProfileViewModel] và phát ra các sự kiện một lần an toàn thông qua LaunchedEffect.
 *
 * @param onNavigateToLogin Callback điều hướng người dùng sang màn hình Đăng nhập khi đăng xuất tài khoản.
 * @param viewModel ViewModel quản lý thông tin hồ sơ người dùng ([ProfileViewModel]). Mặc định là [hiltViewModel].
 * @param modifier [Modifier] dùng để định hình bố cục bên ngoài truyền vào.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateToLogin: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // Lắng nghe và xử lý các sự kiện giao diện diễn ra một lần (One-off Events)
    LaunchedEffect(viewModel.uiEvent) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is ProfileUiEvent.ShowSnackbar ->
                    snackbarHostState.showSnackbar(event.message)
                is ProfileUiEvent.NavigateToLogin ->
                    onNavigateToLogin()
            }
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0.dp)
    ) { paddingValues ->
        val errorMessage = uiState.errorMessage
        val user = uiState.user

        when {
            uiState.isLoading -> CulturalLoadingIndicator(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
            errorMessage != null -> ErrorView(
                message = errorMessage,
                modifier = Modifier.padding(paddingValues)
            )
            user != null -> {
                CulturalBackground {
                    ProfileContent(
                        uiState = uiState,
                        onNameChange = viewModel::onNameChange,
                        onEditClick = viewModel::toggleEditMode,
                        onSaveClick = viewModel::saveProfile,
                        onCancelClick = viewModel::toggleEditMode,
                        onShareClick = {
                            Toast.makeText(context, context.getString(R.string.share_coming_soon), Toast.LENGTH_SHORT).show()
                        },
                        onLogoutClick = viewModel::logout,
                        modifier = Modifier.padding(paddingValues)
                    )
                }
            }
        }
    }
}

/**
 * Bố cục nội dung hiển thị chính (Content Layout) của màn hình Hồ sơ cá nhân.
 *
 * Chứa thẻ Avatar và các chỉ số cấp độ/điểm số, cùng form chỉnh sửa thông tin cá nhân khi được chuyển sang Chế độ chỉnh sửa (Edit Mode).
 */
@Composable
private fun ProfileContent(
    uiState: ProfileUiState,
    onNameChange: (String) -> Unit,
    onEditClick: () -> Unit,
    onSaveClick: () -> Unit,
    onCancelClick: () -> Unit,
    onShareClick: () -> Unit,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val user = uiState.user!!
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Thẻ thông tin cá nhân cốt lõi (Core Card)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 4.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Tải ảnh đại diện mượt mà với Coil AsyncImage và crossfade
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(user.avatarUrl ?: R.drawable.default_avt)
                        .crossfade(true)
                        .build(),
                    contentDescription = stringResource(R.string.avatar),
                    modifier = Modifier
                        .size(88.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = user.name,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = user.email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(18.dp))



                // Nút chỉnh sửa và chia sẻ (ở View Mode)
                if (!uiState.isEditMode) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onEditClick,
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(stringResource(R.string.edit_profile))
                        }
                        OutlinedButton(
                            onClick = onShareClick,
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Share, null, Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(stringResource(R.string.share))
                        }
                    }
                }
            }
        }

        // Gamification Stats & Journey Diary (View Mode)
        if (!uiState.isEditMode) {
            ProfileStatsSection(
                completedTasks = uiState.completedTasks,
                totalTasks = uiState.totalTasks,
                photoCount = uiState.journeyImages.size
            )
            Spacer(modifier = Modifier.height(20.dp))

            JourneyDiarySection(images = uiState.journeyImages)
            Spacer(modifier = Modifier.height(20.dp))

            SettingsList(onLogoutClick = onLogoutClick)
        }

        // Form Chỉnh sửa thông tin hồ sơ (Edit Mode)
        if (uiState.isEditMode) {
            Surface(
                modifier = Modifier.padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 4.dp
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(stringResource(R.string.edit_profile_title), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = uiState.editName,
                        onValueChange = onNameChange,
                        label = { Text(stringResource(R.string.display_name)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(onClick = onCancelClick, modifier = Modifier.weight(1f), enabled = !uiState.isSaving) {
                            Text(stringResource(R.string.cancel))
                        }
                        PrimaryLoadingButton(
                            text = stringResource(R.string.save),
                            isLoading = uiState.isSaving,
                            onClick = onSaveClick,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(stringResource(R.string.app_version), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f))
        Spacer(modifier = Modifier.height(120.dp)) // Không gian bù trừ cho BottomNavBar
    }
}

/**
 * Chỉ số thành tựu riêng lẻ (Stat Item).
 */
@Composable
private fun ProfileStatItem(label: String, value: String, icon: ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
        Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/**
 * Thẻ hiển thị thông tin tĩnh (Info Card) như Vị trí địa lý hay Ngày tham gia.
 */
@Composable
private fun ProfileInfoCard(title: String, content: String, icon: ImageVector) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(12.dp))
            Column {
                Text(title, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(content, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun ProfileStatsSection(completedTasks: Int, totalTasks: Int, photoCount: Int) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(stringResource(R.string.profile_gamification_title), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ProfileStatItem(
                    label = stringResource(R.string.profile_stat_tasks),
                    value = "$completedTasks/$totalTasks",
                    icon = Icons.Default.Flag
                )
                ProfileStatItem(
                    label = stringResource(R.string.profile_stat_photos),
                    value = "$photoCount",
                    icon = Icons.Default.PhotoCamera
                )
            }
        }
    }
}

@Composable
private fun JourneyDiarySection(images: List<JourneyImage>) {
    var showDialog by remember { mutableStateOf(false) }
    var selectedImage by remember { mutableStateOf<JourneyImage?>(null) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable { showDialog = true },
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.PhotoAlbum, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = stringResource(R.string.profile_journey_diary),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (images.isEmpty()) stringResource(R.string.profile_journey_empty) else "${images.size} ảnh check-in",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }

    if (showDialog) {
        Dialog(onDismissRequest = { showDialog = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 200.dp, max = 500.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.profile_journey_diary),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    if (images.isEmpty()) {
                        Text(
                            text = stringResource(R.string.profile_journey_empty),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f, fill = false)
                        ) {
                            items(images.size) { index ->
                                val journeyImage = images[index]
                                Box(
                                    modifier = Modifier
                                        .aspectRatio(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { selectedImage = journeyImage }
                                ) {
                                    AsyncImage(
                                        model = coil3.request.ImageRequest.Builder(LocalContext.current)
                                            .data(journeyImage.url)
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                    
                                    if (!journeyImage.note.isNullOrBlank()) {
                                        // Overlay gradient
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(40.dp)
                                                .align(Alignment.BottomCenter)
                                                .background(
                                                    Brush.verticalGradient(
                                                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
                                                    )
                                                )
                                        )
                                        // Note text
                                        Text(
                                            text = journeyImage.note,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier
                                                .align(Alignment.BottomStart)
                                                .padding(6.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    TextButton(
                        onClick = { showDialog = false },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text(stringResource(R.string.task_detail_back))
                    }
                }
            }
        }
    }

    if (selectedImage != null) {
        Dialog(
            onDismissRequest = { selectedImage = null },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                dismissOnBackPress = true,
                dismissOnClickOutside = true
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                AsyncImage(
                    model = coil3.request.ImageRequest.Builder(LocalContext.current)
                        .data(selectedImage!!.url)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )

                // Close Button
                IconButton(
                    onClick = { selectedImage = null },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }
            }
        }
    }
}

@Composable
private fun SettingsList(onLogoutClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(R.string.profile_settings),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
            )
            
            SettingsItem(
                icon = Icons.Default.Language,
                title = stringResource(R.string.profile_settings_language),
                onClick = { /* TODO */ }
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.surfaceVariant)
            SettingsItem(
                icon = Icons.Default.Security,
                title = stringResource(R.string.profile_settings_terms),
                onClick = { /* TODO */ }
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.surfaceVariant)
            // Logout
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onLogoutClick() }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, null, tint = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.width(16.dp))
                Text(stringResource(R.string.logout), color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun SettingsItem(icon: ImageVector, title: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.width(16.dp))
        Text(title, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Preview(showBackground = true, name = "View Mode")
@Composable
fun ProfileScreenViewModePreview() {
    BeVietnamTheme {
        ProfileContent(
            uiState = ProfileUiState(
                user = User(
                    id = "1",
                    name = "Nguyễn Văn A",
                    email = "nguyenvana@example.com",
                    avatarUrl = null,
                    createdAt = "01/01/2023"
                )
            ),
            onNameChange = {},
            onEditClick = {},
            onSaveClick = {},
            onCancelClick = {},
            onShareClick = {},
            onLogoutClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Edit Mode")
@Composable
fun ProfileScreenEditModePreview() {
    BeVietnamTheme {
        ProfileContent(
            uiState = ProfileUiState(
                isEditMode = true,
                user = User(
                    id = "1",
                    name = "Nguyễn Văn A",
                    email = "nguyenvana@example.com",
                    avatarUrl = null,
                    createdAt = "01/01/2023"
                ),
                editName = "Nguyễn Văn A"
            ),
            onNameChange = {},
            onEditClick = {},
            onSaveClick = {},
            onCancelClick = {},
            onShareClick = {},
            onLogoutClick = {}
        )
    }
}
