package com.bevietnam.ui.screens.profile

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.bevietnam.core.model.Gender
import com.bevietnam.core.model.User
import com.bevietnam.ui.theme.BeVietnamTheme
import com.bevietnam.ui.components.ErrorView
import com.bevietnam.ui.components.LoadingIndicator
import com.bevietnam.ui.navigation.BottomNavBar
import com.bevietnam.ui.components.DatePickerField
import com.bevietnam.ui.components.GenderSelector
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
                onBioChange = viewModel::onBioChange,
                onGenderChange = viewModel::onGenderChange,
                onDateOfBirthChange = viewModel::onDateOfBirthChange,
                onLocationChange = viewModel::onLocationChange,
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
 * Bố cục nội dung hiển thị chính (Content Layout) của màn hình Hồ sơ cá nhân.
 *
 * Chứa thẻ Avatar và các chỉ số cấp độ/điểm số, cùng form chỉnh sửa thông tin cá nhân khi được chuyển sang Chế độ chỉnh sửa (Edit Mode).
 */
@Composable
private fun ProfileContent(
    uiState: ProfileUiState,
    onNameChange: (String) -> Unit,
    onBioChange: (String) -> Unit,
    onGenderChange: (Gender) -> Unit,
    onDateOfBirthChange: (String) -> Unit,
    onLocationChange: (String) -> Unit,
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
                        .data(user.avatarUrl)
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
                    if (user.gender != null) {
                        Spacer(Modifier.width(6.dp))
                        Icon(
                            imageVector = if (user.gender == Gender.MALE)
                                Icons.Default.Male else Icons.Default.Female,
                            contentDescription = if (user.gender == Gender.MALE)
                                stringResource(R.string.gender_male_desc)
                            else
                                stringResource(R.string.gender_female_desc),
                            tint = if (user.gender == Gender.MALE)
                                MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = user.email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                if (user.dateOfBirth != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Cake,
                            contentDescription = stringResource(R.string.date_of_birth),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = user.dateOfBirth,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (user.bio.isNotBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = user.bio,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Chỉ số thành tựu trong game (Gamified Stats)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ProfileStatItem(
                        label = stringResource(R.string.stat_level),
                        value = user.level.toString(),
                        icon = Icons.Default.Star
                    )
                    ProfileStatItem(
                        label = stringResource(R.string.stat_points),
                        value = user.points.toString(),
                        icon = Icons.Default.EmojiEvents
                    )
                }

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

        // Thẻ thông tin địa lý và ngày tham gia
        if (!uiState.isEditMode) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (!user.location.isNullOrBlank()) ProfileInfoCard(stringResource(R.string.profile_location), user.location, Icons.Default.LocationOn)
                if (!user.createdAt.isNullOrBlank()) ProfileInfoCard(stringResource(R.string.profile_joined_date), user.createdAt, Icons.Default.CalendarToday)
            }
            Spacer(modifier = Modifier.height(16.dp))
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
                    OutlinedTextField(
                        value = uiState.editBio,
                        onValueChange = onBioChange,
                        label = { Text(stringResource(R.string.bio)) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        shape = RoundedCornerShape(10.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = uiState.editLocation,
                        onValueChange = onLocationChange,
                        label = { Text(stringResource(R.string.profile_location)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.LocationOn, null, Modifier.size(18.dp)) },
                        shape = RoundedCornerShape(10.dp)
                    )
                    Spacer(Modifier.height(12.dp))

                    GenderSelector(
                        selected = uiState.editGender,
                        onSelect = onGenderChange
                    )

                    Spacer(Modifier.height(12.dp))
                    DatePickerField(
                        label = stringResource(R.string.date_of_birth),
                        displayValue = uiState.editDateOfBirth,
                        onDateSelected = { date -> 
                            onDateOfBirthChange(date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))) 
                        }
                    )

                    Spacer(modifier = Modifier.height(20.dp))

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

        // Nút Đăng xuất (Logout Button)
        OutlinedButton(
            onClick = onLogoutClick,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
        ) {
            Icon(Icons.AutoMirrored.Filled.Logout, null, Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.logout))
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
                    bio = "Yêu du lịch và khám phá văn hóa Việt Nam",
                    gender = Gender.MALE,
                    dateOfBirth = "01/01/1990",
                    location = "Hà Nội, Việt Nam",
                    createdAt = "01/01/2023",
                    level = 5,
                    points = 1250
                )
            ),
            onNameChange = {},
            onBioChange = {},
            onGenderChange = {},
            onDateOfBirthChange = {},
            onLocationChange = {},
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
                    bio = "Yêu du lịch và khám phá văn hóa Việt Nam",
                    gender = Gender.MALE,
                    dateOfBirth = "01/01/1990",
                    location = "Hà Nội, Việt Nam",
                    createdAt = "01/01/2023",
                    level = 5,
                    points = 1250
                ),
                editName = "Nguyễn Văn A",
                editBio = "Yêu du lịch và khám phá văn hóa Việt Nam",
                editGender = Gender.MALE,
                editDateOfBirth = "01/01/1990",
                editLocation = "Hà Nội, Việt Nam"
            ),
            onNameChange = {},
            onBioChange = {},
            onGenderChange = {},
            onDateOfBirthChange = {},
            onLocationChange = {},
            onEditClick = {},
            onSaveClick = {},
            onCancelClick = {},
            onShareClick = {},
            onLogoutClick = {}
        )
    }
}
