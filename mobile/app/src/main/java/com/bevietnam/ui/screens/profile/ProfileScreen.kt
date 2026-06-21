package com.bevietnam.ui.screens.profile

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
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
import com.bevietnam.ui.components.ErrorView
import com.bevietnam.ui.components.PrimaryLoadingButton
import com.bevietnam.ui.theme.BeVietnamTheme

/**
 * Màn hình Hồ sơ cá nhân (Profile Screen) của ứng dụng BeVietnam.
 *
 * Hiển thị banner văn hóa (hoa văn trống đồng), thông tin cá nhân, và lưới bài viết
 * check-in của người dùng. Cho phép chỉnh sửa hồ sơ và đăng xuất.
 *
 * @param onNavigateToLogin Callback điều hướng sang màn hình Đăng nhập khi đăng xuất.
 * @param viewModel ViewModel quản lý thông tin hồ sơ ([ProfileViewModel]).
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
            uiState.isLoading -> com.bevietnam.ui.components.CulturalLoadingIndicator(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
            errorMessage != null -> ErrorView(
                message = errorMessage,
                modifier = Modifier.padding(paddingValues)
            )
            user != null -> ProfileContent(
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

/**
 * Bố cục nội dung chính của màn hình Hồ sơ: banner văn hóa, avatar, thông tin, và lưới bài viết.
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
            .verticalScroll(rememberScrollState())
    ) {
        // Banner văn hóa (hoa văn trống đồng) + avatar nổi lên trên
        Box(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(MaterialTheme.colorScheme.primary)
            ) {
                Image(
                    painter = painterResource(R.drawable.dong_son_pattern),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    alpha = 0.12f,
                    modifier = Modifier.fillMaxSize()
                )
            }

            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(user.avatarUrl ?: R.drawable.default_avt)
                    .crossfade(true)
                    .build(),
                contentDescription = stringResource(R.string.avatar),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 76.dp)
                    .size(92.dp)
                    .clip(CircleShape)
                    .border(4.dp, MaterialTheme.colorScheme.background, CircleShape)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = user.name,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = user.email,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (!uiState.isEditMode) {
            // Nút Chỉnh sửa + Chia sẻ
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onEditClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    shape = RoundedCornerShape(11.dp)
                ) {
                    Icon(Icons.Default.Edit, null, Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(stringResource(R.string.edit_profile))
                }
                OutlinedButton(
                    onClick = onShareClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    shape = RoundedCornerShape(11.dp)
                ) {
                    Icon(Icons.Default.Share, null, Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(stringResource(R.string.share))
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Lưới bài viết của người dùng (Instagram-style)
            ProfilePostsSection()
        } else {
            // Form chỉnh sửa thông tin
            Surface(
                modifier = Modifier.padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 4.dp
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        stringResource(R.string.edit_profile_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
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
        }

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedButton(
            onClick = onLogoutClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
        ) {
            Icon(Icons.AutoMirrored.Filled.Logout, null, Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.logout))
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            stringResource(R.string.app_version),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(120.dp)) // Chừa chỗ cho BottomNavBar
    }
}

/**
 * Khu vực lưới bài viết check-in của người dùng.
 *
 * TODO(Backend): hiện dùng dữ liệu mẫu cố định để demo. Khi nối API "bài viết của tôi",
 * thay [sampleProfilePosts] bằng danh sách thật từ ViewModel/UseCase.
 */
@Composable
private fun ProfilePostsSection() {
    val posts = sampleProfilePosts

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.PhotoCamera,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = "Bài viết của tôi",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
        Text(
            text = "${posts.size} bài",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Lưới 2 cột: chia danh sách thành từng cặp để tránh xung đột cuộn lồng nhau
    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        posts.chunked(2).forEach { rowPosts ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                rowPosts.forEach { post ->
                    PostCard(post = post, modifier = Modifier.weight(1f))
                }
                if (rowPosts.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

/**
 * Thẻ hiển thị một bài viết check-in: ảnh (placeholder), địa danh, tiêu đề, lượt thích.
 */
@Composable
private fun PostCard(post: ProfilePost, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Ảnh placeholder (sẽ thay bằng AsyncImage khi có URL thật)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp)
                    .background(post.color),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    post.icon,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.size(30.dp)
                )
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(6.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.Black.copy(alpha = 0.42f))
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(11.dp)
                    )
                    Spacer(Modifier.width(3.dp))
                    Text(post.placeName, color = Color.White, fontSize = 10.sp)
                }
            }

            Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
                Text(
                    text = post.title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.FavoriteBorder,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "${post.likes} · ${post.timeAgo}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Mô hình hiển thị một bài viết trên hồ sơ (chỉ dùng cho UI/demo).
 */
private data class ProfilePost(
    val title: String,
    val placeName: String,
    val likes: Int,
    val timeAgo: String,
    val color: Color,
    val icon: ImageVector
)

/** Dữ liệu mẫu cố định để demo lưới bài viết. */
private val sampleProfilePosts = listOf(
    ProfilePost("Một ngày ở Văn Miếu", "Văn Miếu", 24, "2 ngày trước", Color(0xFFA6471F), Icons.Default.AccountBalance),
    ProfilePost("Đèn lồng phố cổ", "Hội An", 58, "5 ngày trước", Color(0xFFC9A227), Icons.Default.Lightbulb),
    ProfilePost("Hang động Tràng An", "Tràng An", 41, "1 tuần trước", Color(0xFF6E7B3E), Icons.Default.Terrain),
    ProfilePost("Hoàng thành Huế", "Huế", 37, "2 tuần trước", Color(0xFF8A5A2B), Icons.Default.AccountBalance)
)

@Preview(showBackground = true, name = "View Mode")
@Composable
fun ProfileScreenViewModePreview() {
    BeVietnamTheme {
        ProfileContent(
            uiState = ProfileUiState(
                user = User(
                    id = "1",
                    name = "Hoàng Phi",
                    email = "hoangphipay@example.com",
                    avatarUrl = null,
                    createdAt = "01/01/2024"
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
                    name = "Hoàng Phi",
                    email = "hoangphipay@example.com",
                    avatarUrl = null,
                    createdAt = "01/01/2024"
                ),
                editName = "Hoàng Phi"
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
