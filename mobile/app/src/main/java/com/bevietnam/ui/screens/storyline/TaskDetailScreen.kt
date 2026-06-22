package com.bevietnam.ui.screens.storyline

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.bevietnam.core.model.Task
import com.bevietnam.core.model.TaskDifficulty
import com.bevietnam.core.model.TaskStatus
import com.bevietnam.ui.theme.BeVietnamTheme
import com.bevietnam.ui.theme.LocalCulturalColors
import com.bevietnam.ui.components.CulturalBackground
import androidx.compose.ui.res.stringResource
import com.bevietnam.R

/**
 * Màn hình Chi tiết Nhiệm vụ (Task Detail Screen).
 *
 * Hiển thị đầy đủ thông tin về nhiệm vụ bao gồm:
 * - Ảnh capture lớn (hoặc placeholder nếu chưa chụp)
 * - Mô tả chi tiết
 * - Giải thích văn hóa lịch sử
 * - Yêu cầu hoàn thành
 * - Nút Check-in (nếu task ACTIVE)
 *
 * @param onBackClick Callback quay lại màn hình trước.
 * @param onCheckInTask Callback khi nhấn nút Check-in — navigate sang CaptureScreen.
 * @param viewModel ViewModel quản lý trạng thái ([TaskDetailViewModel]).
 * @param modifier [Modifier] dùng để định hình bố cục bên ngoài.
 */
@Composable
fun TaskDetailScreen(
    onBackClick: () -> Unit,
    onCheckInTask: (String, String?) -> Unit,
    viewModel: TaskDetailViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        CulturalBackground {
            when (uiState) {
                is TaskDetailUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
                is TaskDetailUiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "🏮", fontSize = 48.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = (uiState as TaskDetailUiState.Error).message,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                is TaskDetailUiState.Success -> {
                    TaskDetailContent(
                        task = (uiState as TaskDetailUiState.Success).task,
                        onBackClick = onBackClick,
                        onCheckInTask = onCheckInTask
                    )
                }
            }
        }
    }
}

@Composable
private fun TaskDetailContent(
    task: Task,
    onBackClick: () -> Unit,
    onCheckInTask: (String, String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val culturalColors = LocalCulturalColors.current
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Back Button
        IconButton(
            onClick = onBackClick,
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.task_detail_back)
            )
        }

        // Title with Gradient
        val gradientBrush = Brush.linearGradient(
            colors = listOf(
                MaterialTheme.colorScheme.primary,
                culturalColors.amberColor
            )
        )
        Text(
            text = task.title,
            style = MaterialTheme.typography.headlineMedium.copy(
                brush = gradientBrush
            ),
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.fillMaxWidth()
        )

        // Badges Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TaskStatusBadge(status = task.status)
            DifficultyBadge(difficulty = task.difficulty)
        }

        // Capture Photo — Large
        CapturePhotoSection(
            captureImageUrl = task.captureImageUrl,
            captureNote = task.captureNote,
            status = task.status
        )

        // Description Section
        DetailInfoCard(
            icon = Icons.Outlined.Description,
            title = stringResource(R.string.task_detail_description),
            content = task.description,
            accentColor = MaterialTheme.colorScheme.primary
        )

        // Cultural Explanation Section
        DetailInfoCard(
            icon = Icons.Outlined.Lightbulb,
            title = stringResource(R.string.task_detail_cultural),
            content = task.culturalExplanation,
            accentColor = culturalColors.amberColor
        )

        // Completion Requirement Section
        DetailInfoCard(
            icon = Icons.Outlined.TaskAlt,
            title = stringResource(R.string.task_detail_requirement),
            content = task.completionRequirement,
            accentColor = culturalColors.completionBlue
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Check-in Button (chỉ cho ACTIVE)
        when (task.status) {
            TaskStatus.ACTIVE -> {
                Button(
                    onClick = { onCheckInTask(task.id, task.placeId) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.storyline_checkin_button),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            TaskStatus.COMPLETED -> {
                // Đã hoàn thành — hiển thị badge
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(culturalColors.easyColor.copy(alpha = 0.1f))
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = culturalColors.easyColor,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.task_detail_completed_msg),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = culturalColors.easyColor
                        )
                    }
                }
            }
            TaskStatus.LOCKED -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(culturalColors.completedGray.copy(alpha = 0.1f))
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = culturalColors.completedGray,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.task_detail_locked_msg),
                            style = MaterialTheme.typography.titleSmall,
                            color = culturalColors.completedGray
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// ──────────────────────────────────────────────────────────────
// STATUS BADGE
// ──────────────────────────────────────────────────────────────

@Composable
private fun TaskStatusBadge(status: TaskStatus) {
    val culturalColors = LocalCulturalColors.current

    val (icon, label, color) = when (status) {
        TaskStatus.COMPLETED -> Triple(Icons.Default.CheckCircle, stringResource(R.string.task_detail_status_completed), culturalColors.easyColor)
        TaskStatus.ACTIVE -> Triple(Icons.Default.PlayCircle, stringResource(R.string.task_detail_status_active), MaterialTheme.colorScheme.primary)
        TaskStatus.LOCKED -> Triple(Icons.Default.Lock, stringResource(R.string.storyline_locked), culturalColors.completedGray)
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier
            .background(color.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
    }
}

// ──────────────────────────────────────────────────────────────
// CAPTURE PHOTO SECTION (large, full-width)
// ──────────────────────────────────────────────────────────────

@Composable
private fun CapturePhotoSection(
    captureImageUrl: String?,
    captureNote: String?,
    status: TaskStatus
) {
    val culturalColors = LocalCulturalColors.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (captureImageUrl != null)
                Color.Transparent
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        border = if (captureImageUrl == null) {
            BorderStroke(
                width = 2.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        culturalColors.completedGray.copy(alpha = 0.4f),
                        culturalColors.completedGray.copy(alpha = 0.2f)
                    )
                )
            )
        } else null
    ) {
        if (captureImageUrl != null) {
            Box(modifier = Modifier.fillMaxSize()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(captureImageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = stringResource(R.string.task_detail_image_desc),
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )

                // Overlay gradient at bottom to ensure text readability
                val hasNote = !captureNote.isNullOrBlank()
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (hasNote) 120.dp else 48.dp) // Tăng chiều cao lớp phủ mờ để dễ đọc chữ hơn
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
                            )
                        )
                )

                // Note overlay text
                if (hasNote) {
                    Text(
                        text = captureNote!!,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(start = 16.dp, end = 56.dp, bottom = 16.dp) // Avoid overlapping with camera icon
                            .heightIn(max = 90.dp) // Giới hạn chiều cao
                            .verticalScroll(rememberScrollState()) // Cho phép cuộn
                    )
                }

                // Camera icon badge
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp)
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(culturalColors.easyColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        } else {
            // Placeholder — chưa chụp ảnh
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(culturalColors.completedGray.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.QuestionMark,
                        contentDescription = null,
                        tint = culturalColors.completedGray,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = if (status == TaskStatus.ACTIVE) stringResource(R.string.task_detail_capture_hint)
                    else stringResource(R.string.task_detail_capture_locked),
                    style = MaterialTheme.typography.bodyMedium,
                    color = culturalColors.completedGray
                )
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────
// DETAIL INFO CARD
// ──────────────────────────────────────────────────────────────

@Composable
private fun DetailInfoCard(
    icon: ImageVector,
    title: String,
    content: String,
    accentColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(accentColor.copy(alpha = 0.08f))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = accentColor
                )
            }

            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 22.sp
            )
        }
    }
}

// ──────────────────────────────────────────────────────────────
// PREVIEWS
// ──────────────────────────────────────────────────────────────

@Preview(showBackground = true)
@Composable
fun TaskDetailCompletedPreview() {
    BeVietnamTheme {
        TaskDetailContent(
            task = Task(
                id = "1", title = "Thử thách Phở Hà Nội",
                description = "Tìm và thưởng thức một bát phở truyền thống tại một quán phở nổi tiếng ở phố cổ Hà Nội.",
                culturalExplanation = "Phở là món ăn quốc hồn quốc túy của Việt Nam, xuất hiện từ đầu thế kỷ 20.",
                completionRequirement = "Chụp ảnh bát phở truyền thống.",
                difficulty = TaskDifficulty.EASY, isCompleted = true,
                captureImageUrl = "https://example.com/pho.jpg", status = TaskStatus.COMPLETED
            ),
            onBackClick = {},
            onCheckInTask = { _, _ -> }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TaskDetailActivePreview() {
    BeVietnamTheme {
        TaskDetailContent(
            task = Task(
                id = "2", title = "Đi dạo Phố cổ 36 phố phường",
                description = "Khám phá khu phố cổ Hà Nội với 36 phố phường.",
                culturalExplanation = "Khu phố cổ hình thành từ thế kỷ 15.",
                completionRequirement = "Chụp ảnh tại một con phố cổ đặc trưng.",
                difficulty = TaskDifficulty.MEDIUM, status = TaskStatus.ACTIVE
            ),
            onBackClick = {},
            onCheckInTask = { _, _ -> }
        )
    }
}
