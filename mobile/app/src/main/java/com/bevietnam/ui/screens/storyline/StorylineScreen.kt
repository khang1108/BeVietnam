package com.bevietnam.ui.screens.storyline

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.bevietnam.core.model.QuestChain
import com.bevietnam.core.model.Task
import com.bevietnam.core.model.TaskDifficulty
import com.bevietnam.core.model.TaskStatus
import com.bevietnam.ui.components.CulturalBackground
import com.bevietnam.ui.theme.BeVietnamTheme
import com.bevietnam.ui.theme.LocalCulturalColors
import androidx.compose.ui.res.stringResource
import com.bevietnam.R

/**
 * Màn hình Hành trình cốt truyện & Nhiệm vụ (Storyline Screen) — Duolingo-style.
 *
 * Hiển thị chuỗi nhiệm vụ hành trình (quest chain) dạng danh sách dọc với:
 * - Progress header (thanh tiến độ)
 * - Horizontal path thumbnails (mini-map hành trình)
 * - Vertical task cards với connector lines và capture thumbnails
 *
 * @param viewModel ViewModel quản lý trạng thái dữ liệu ([StorylineViewModel]).
 * @param onTaskClick Callback khi nhấn vào thẻ nhiệm vụ — navigate sang TaskDetailScreen.
 * @param onCheckInTask Callback khi nhấn nút Check-in — navigate sang CaptureScreen.
 * @param modifier [Modifier] dùng để định hình bố cục bên ngoài.
 */
@Composable
fun StorylineScreen(
    viewModel: StorylineViewModel = hiltViewModel(),
    onTaskClick: (String) -> Unit = {},
    onCheckInTask: (String, String?) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    StorylineScreenContent(
        uiState = uiState,
        onRetry = viewModel::loadQuestChain,
        onTaskClick = onTaskClick,
        onCheckInTask = onCheckInTask,
        modifier = modifier
    )
}

@Composable
fun StorylineScreenContent(
    uiState: StorylineUiState,
    onRetry: () -> Unit,
    onTaskClick: (String) -> Unit,
    onCheckInTask: (String, String?) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        CulturalBackground {
            when (uiState) {
                is StorylineUiState.Loading -> StorylineLoadingState()
                is StorylineUiState.Empty -> StorylineEmptyState(onRetry = onRetry)
                is StorylineUiState.Error -> StorylineEmptyState(
                    message = uiState.message,
                    onRetry = onRetry
                )
                is StorylineUiState.Success -> StorylineSuccessContent(
                    questChain = uiState.questChain,
                    onTaskClick = onTaskClick,
                    onCheckInTask = onCheckInTask
                )
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────
// SUCCESS CONTENT
// ──────────────────────────────────────────────────────────────

@Composable
private fun StorylineSuccessContent(
    questChain: QuestChain,
    onTaskClick: (String) -> Unit,
    onCheckInTask: (String, String?) -> Unit
) {
    val completedCount = questChain.tasks.count { it.status == TaskStatus.COMPLETED }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        // Progress Header
        item {
            QuestProgressHeader(
                completed = completedCount,
                total = questChain.totalTasks
            )
        }

        // Task Cards with Connector Lines
        itemsIndexed(
            items = questChain.tasks,
            key = { _, task -> task.id }
        ) { index, task ->
            Column {
                // Connector line (giữa các card, không vẽ trước card đầu tiên)
                if (index > 0) {
                    TaskConnectorLine(
                        previousStatus = questChain.tasks[index - 1].status,
                        currentStatus = task.status
                    )
                }
                // Task Card
                QuestTaskCard(
                    task = task,
                    stepNumber = index + 1,
                    onCardClick = { onTaskClick(task.id) },
                    onCheckInClick = { onCheckInTask(task.id, task.placeId) },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────
// PROGRESS HEADER
// ──────────────────────────────────────────────────────────────

@Composable
private fun QuestProgressHeader(
    completed: Int,
    total: Int
) {
    val progress = if (total > 0) completed.toFloat() / total else 0f
    val percentage = (progress * 100).toInt()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Progress Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Flag,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        text = stringResource(R.string.storyline_journey),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = "$percentage%",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Progress Bar text
            Text(
                text = stringResource(R.string.storyline_progress, completed, total),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
        }
    }
}

// ──────────────────────────────────────────────────────────────
// HORIZONTAL PATH THUMBNAILS
// ──────────────────────────────────────────────────────────────

@Composable
private fun HorizontalPathThumbnails(
    tasks: List<Task>,
    modifier: Modifier = Modifier
) {
    val culturalColors = LocalCulturalColors.current

    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(0.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        itemsIndexed(tasks) { index, task ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Connector line (giữa các node)
                if (index > 0) {
                    val lineColor = when {
                        task.status == TaskStatus.COMPLETED -> culturalColors.easyColor
                        task.status == TaskStatus.ACTIVE -> MaterialTheme.colorScheme.primary
                        else -> culturalColors.completedGray
                    }
                    Canvas(
                        modifier = Modifier
                            .width(24.dp)
                            .height(2.dp)
                    ) {
                        val dashEffect = if (task.status == TaskStatus.LOCKED) {
                            PathEffect.dashPathEffect(floatArrayOf(6f, 4f), 0f)
                        } else null

                        drawLine(
                            color = lineColor,
                            start = Offset(0f, size.height / 2),
                            end = Offset(size.width, size.height / 2),
                            strokeWidth = 2.dp.toPx(),
                            pathEffect = dashEffect,
                            cap = StrokeCap.Round
                        )
                    }
                }

                // Node circle
                val nodeColor = when (task.status) {
                    TaskStatus.COMPLETED -> culturalColors.easyColor
                    TaskStatus.ACTIVE -> MaterialTheme.colorScheme.primary
                    TaskStatus.LOCKED -> culturalColors.completedGray
                }

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(nodeColor.copy(alpha = 0.15f))
                        .border(2.dp, nodeColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    when (task.status) {
                        TaskStatus.COMPLETED -> Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = stringResource(R.string.storyline_completed),
                            tint = culturalColors.easyColor,
                            modifier = Modifier.size(16.dp)
                        )
                        TaskStatus.ACTIVE -> Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                        )
                        TaskStatus.LOCKED -> Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = stringResource(R.string.storyline_locked_status),
                            tint = culturalColors.completedGray,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────
// CONNECTOR LINE
// ──────────────────────────────────────────────────────────────

@Composable
private fun TaskConnectorLine(
    previousStatus: TaskStatus,
    currentStatus: TaskStatus
) {
    val culturalColors = LocalCulturalColors.current

    val lineColor = when {
        previousStatus == TaskStatus.COMPLETED && currentStatus == TaskStatus.COMPLETED ->
            culturalColors.easyColor
        previousStatus == TaskStatus.COMPLETED && currentStatus == TaskStatus.ACTIVE ->
            MaterialTheme.colorScheme.primary
        else -> culturalColors.completedGray
    }

    val isDashed = currentStatus == TaskStatus.LOCKED

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(32.dp)
    ) {
        val centerX = size.width / 2
        val dashEffect = if (isDashed) {
            PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
        } else null

        drawLine(
            color = lineColor,
            start = Offset(centerX, 0f),
            end = Offset(centerX, size.height),
            strokeWidth = 2.5.dp.toPx(),
            pathEffect = dashEffect,
            cap = StrokeCap.Round
        )
    }
}

// ──────────────────────────────────────────────────────────────
// QUEST TASK CARD
// ──────────────────────────────────────────────────────────────

@Composable
fun QuestTaskCard(
    task: Task,
    stepNumber: Int,
    onCardClick: () -> Unit,
    onCheckInClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val culturalColors = LocalCulturalColors.current
    val isLocked = task.status == TaskStatus.LOCKED
    val isCompleted = task.status == TaskStatus.COMPLETED
    val isActive = task.status == TaskStatus.ACTIVE

    val cardAlpha = if (isLocked) 0.55f else 1f
    val borderColor = when (task.status) {
        TaskStatus.COMPLETED -> culturalColors.easyColor.copy(alpha = 0.5f)
        TaskStatus.ACTIVE -> MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        TaskStatus.LOCKED -> Color.Transparent
    }
    val cardElevation = when (task.status) {
        TaskStatus.ACTIVE -> 6.dp
        TaskStatus.COMPLETED -> 2.dp
        TaskStatus.LOCKED -> 0.dp
    }

    Box(modifier = modifier.alpha(cardAlpha)) {
        Card(
            onClick = { if (!isLocked) onCardClick() },
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (borderColor != Color.Transparent) {
                        Modifier.border(1.5.dp, borderColor, RoundedCornerShape(16.dp))
                    } else Modifier
                ),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = cardElevation),
            enabled = !isLocked
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Row 1: Step badge + Title & Difficulty
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(end = 24.dp) // Avoid overlapping with Thumbnail
                ) {
                    // Step Number Badge
                    StepBadge(
                        stepNumber = stepNumber,
                        status = task.status
                    )

                    // Title and Difficulty
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = task.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isLocked) MaterialTheme.colorScheme.onSurfaceVariant
                            else MaterialTheme.colorScheme.onSurface,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        DifficultyBadge(difficulty = task.difficulty)
                    }
                }

                // Description
                Text(
                    text = task.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isLocked) culturalColors.completedGray
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 18.sp
                )

                // Check-in Button (chỉ cho ACTIVE) hoặc Status Badge
                when (task.status) {
                    TaskStatus.ACTIVE -> {
                        Button(
                            onClick = onCheckInClick,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.storyline_checkin_button),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    TaskStatus.COMPLETED -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier
                                .background(
                                    culturalColors.easyColor.copy(alpha = 0.1f),
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = culturalColors.easyColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = stringResource(R.string.storyline_checkin_success),
                                style = MaterialTheme.typography.labelMedium,
                                color = culturalColors.easyColor,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    TaskStatus.LOCKED -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier
                                .background(
                                    culturalColors.completedGray.copy(alpha = 0.1f),
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = culturalColors.completedGray,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = stringResource(R.string.storyline_locked),
                                style = MaterialTheme.typography.labelMedium,
                                color = culturalColors.completedGray
                            )
                        }
                    }
                }
            }
        }

        // Capture Thumbnail — góc trên phải card
        CaptureThumbnail(
            captureImageUrl = task.captureImageUrl,
            status = task.status,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (-8).dp, y = (-6).dp)
        )

        // Lock Icon Overlay cho thẻ bị khóa
        if (isLocked) {
            Box(
                modifier = Modifier.matchParentSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = stringResource(R.string.storyline_locked),
                    modifier = Modifier.size(56.dp),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                )
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────
// STEP BADGE
// ──────────────────────────────────────────────────────────────

@Composable
private fun StepBadge(
    stepNumber: Int,
    status: TaskStatus
) {
    val culturalColors = LocalCulturalColors.current

    val bgColor = when (status) {
        TaskStatus.COMPLETED -> culturalColors.easyColor
        TaskStatus.ACTIVE -> MaterialTheme.colorScheme.primary
        TaskStatus.LOCKED -> culturalColors.completedGray
    }

    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        when (status) {
            TaskStatus.COMPLETED -> Icon(
                imageVector = Icons.Default.Check,
                contentDescription = stringResource(R.string.storyline_completed),
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
            else -> Text(
                text = "$stepNumber",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

// ──────────────────────────────────────────────────────────────
// CAPTURE THUMBNAIL (góc trên phải card)
// ──────────────────────────────────────────────────────────────

@Composable
private fun CaptureThumbnail(
    captureImageUrl: String?,
    status: TaskStatus,
    modifier: Modifier = Modifier
) {
    // Không hiển thị thumbnail cho task LOCKED
    if (status == TaskStatus.LOCKED) return

    val culturalColors = LocalCulturalColors.current

    Box(
        modifier = modifier
            .size(48.dp)
            .shadow(4.dp, RoundedCornerShape(10.dp))
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surface)
            .then(
                if (captureImageUrl != null) {
                    Modifier.border(
                        2.dp,
                        culturalColors.easyColor,
                        RoundedCornerShape(10.dp)
                    )
                } else {
                    Modifier.border(
                        1.5.dp,
                        culturalColors.completedGray,
                        RoundedCornerShape(10.dp)
                    )
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        if (captureImageUrl != null) {
            // Ảnh đã chụp
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(captureImageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "Ảnh đã chụp",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            // Checkmark overlay
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 2.dp, y = 2.dp)
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(culturalColors.easyColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(12.dp)
                )
            }
        } else {
            // Chưa chụp — icon dấu chấm hỏi
            Icon(
                imageVector = Icons.Outlined.QuestionMark,
                contentDescription = "Chưa chụp ảnh",
                tint = culturalColors.completedGray,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

// ──────────────────────────────────────────────────────────────
// DIFFICULTY BADGE (giữ nguyên logic từ phiên bản cũ)
// ──────────────────────────────────────────────────────────────

@Composable
fun DifficultyBadge(
    difficulty: TaskDifficulty,
    onDark: Boolean = false
) {
    val culturalColors = LocalCulturalColors.current
    val (label, color) = when (difficulty) {
        TaskDifficulty.EASY -> "Dễ" to culturalColors.easyColor
        TaskDifficulty.MEDIUM -> "Trung bình" to culturalColors.mediumColor
        TaskDifficulty.HARD -> "Khó" to culturalColors.hardColor
    }

    val bgColor = if (onDark) Color.White.copy(alpha = 0.2f) else color.copy(alpha = 0.12f)
    val textColor = if (onDark) Color.White else color

    Box(
        modifier = Modifier
            .background(color = bgColor, shape = RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

// ──────────────────────────────────────────────────────────────
// LOADING STATE
// ──────────────────────────────────────────────────────────────

@Composable
private fun StorylineLoadingState() {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate"
    )

    val culturalColors = LocalCulturalColors.current
    val brush = Brush.linearGradient(
        colors = listOf(culturalColors.shimmerLight, culturalColors.shimmerDark, culturalColors.shimmerLight),
        start = Offset.Zero,
        end = Offset(translateAnim, translateAnim)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header shimmer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(brush)
        )
        // Path shimmer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(brush)
        )
        // Card shimmers
        repeat(3) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(brush)
            )
        }
    }
}

// ──────────────────────────────────────────────────────────────
// EMPTY STATE
// ──────────────────────────────────────────────────────────────

@Composable
private fun StorylineEmptyState(
    message: String = stringResource(R.string.storyline_empty_title),
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "🏮", fontSize = 64.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.storyline_empty_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(text = stringResource(R.string.storyline_retry), color = Color.White)
        }
    }
}

// ──────────────────────────────────────────────────────────────
// PREVIEWS
// ──────────────────────────────────────────────────────────────

@Preview(showBackground = true)
@Composable
fun StorylineScreenSuccessPreview() {
    val mockTasks = listOf(
        Task(
            id = "1", title = "Thử thách Phở", description = "Tìm và thưởng thức một bát phở truyền thống.",
            culturalExplanation = "Phở là món ăn quốc hồn quốc túy.", completionRequirement = "Chụp ảnh bát phở.",
            difficulty = TaskDifficulty.EASY, isCompleted = true,
            captureImageUrl = "https://example.com/pho.jpg", status = TaskStatus.COMPLETED
        ),
        Task(
            id = "2", title = "Khám phá Văn Miếu", description = "Ghé thăm trường đại học đầu tiên.",
            culturalExplanation = "Nơi thờ Khổng Tử.", completionRequirement = "Check-in tại Văn Miếu.",
            difficulty = TaskDifficulty.MEDIUM, status = TaskStatus.ACTIVE
        ),
        Task(
            id = "3", title = "Hoàng thành Thăng Long", description = "Di sản UNESCO.",
            culturalExplanation = "Trung tâm quyền lực.", completionRequirement = "Chụp ảnh Cột cờ.",
            difficulty = TaskDifficulty.HARD, status = TaskStatus.LOCKED
        )
    )
    val mockChain = QuestChain("q1", "Di Sản Việt Nam", "Khám phá hành trình.", 3, 2, mockTasks)
    BeVietnamTheme {
        StorylineScreenContent(
            uiState = StorylineUiState.Success(questChain = mockChain),
            onRetry = {}, onTaskClick = {}, onCheckInTask = { _, _ -> }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun StorylineScreenLoadingPreview() {
    BeVietnamTheme {
        StorylineScreenContent(
            uiState = StorylineUiState.Loading,
            onRetry = {}, onTaskClick = {}, onCheckInTask = { _, _ -> }
        )
    }
}
