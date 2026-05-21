package com.bevietnam.ui.screens.storyline

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

import com.bevietnam.core.model.Task
import com.bevietnam.core.model.TaskDifficulty
import com.bevietnam.ui.theme.BeVietnamTheme

// ── Color Tokens ──────────────────────────────────────────────────────────────

private val PrimaryRed = Color(0xFFC0392B)
private val Background = Color(0xFFFAF5E4)
private val CardBackground = Color(0xFFFFFFFF)
private val TextPrimary = Color(0xFF1A1A1A)
private val TextSecondary = Color(0xFF6B6B6B)
private val EasyGreen = Color(0xFF27AE60)
private val MediumOrange = Color(0xFFE67E22)
private val HardRed = Color(0xFFC0392B)
private val CulturalAmber = Color(0xFFF39C12)
private val CompletionBlue = Color(0xFF2980B9)
private val ShimmerLight = Color(0xFFE0E0E0)
private val ShimmerDark = Color(0xFFC8C8C8)
private val CompletedGray = Color(0xFFBDBDBD)

@Composable
fun StorylineScreen(
    viewModel: StorylineViewModel = hiltViewModel(),
    onTaskClick: (Task) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Background
    ) {
        Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
            StorylineTopBar()

            when (val state = uiState) {
                is StorylineUiState.Loading -> StorylineLoadingState()
                is StorylineUiState.Empty -> StorylineEmptyState(
                    onRetry = { viewModel.loadTasks() }
                )
                is StorylineUiState.Error -> StorylineEmptyState(
                    message = state.message,
                    onRetry = { viewModel.loadTasks() }
                )
                is StorylineUiState.Success -> StorylineSuccessContent(
                    state = state,
                    onTaskClick = onTaskClick,
                    onTaskCompleted = viewModel::onTaskCompleted
                )
            }
        }
    }
}

@Composable
private fun StorylineTopBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Background)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "Hành Trình",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "Khám phá văn hóa Việt Nam",
                fontSize = 13.sp,
                color = TextSecondary
            )
        }
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(PrimaryRed),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.EmojiEvents,
                contentDescription = "Achievements",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun StorylineSuccessContent(
    state: StorylineUiState.Success,
    onTaskClick: (Task) -> Unit,
    onTaskCompleted: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        state.nextTask?.let {
            item {
                NextTaskBanner(
                    task = it,
                    onTaskCompleted = onTaskCompleted
                )
            }
        }

        item {
            val completedCount = state.tasks.count { it.isCompleted }
            TaskProgressBar(
                completed = completedCount,
                total = state.tasks.size,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        item {
            Text(
                text = "Tất cả nhiệm vụ",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        itemsIndexed(
            items = state.tasks,
            key = { _, task -> task.id }
        ) { _, task ->
            TaskCard(
                task = task,
                onClick = { onTaskClick(task) },
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            )
        }
    }
}

@Composable
private fun NextTaskBanner(
    task: Task,
    onTaskCompleted: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(PrimaryRed, Color(0xFFE74C3C)),
                        start = Offset.Zero,
                        end = Offset(Float.POSITIVE_INFINITY, 0f)
                    )
                )
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "NHIỆM VỤ TIẾP THEO",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.9f),
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    DifficultyBadge(
                        difficulty = task.difficulty,
                        onDark = true
                    )
                }

                Text(
                    text = task.title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Text(
                    text = task.description,
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.85f),
                    lineHeight = 19.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .background(
                            color = Color.White.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.TaskAlt,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = task.completionRequirement,
                        fontSize = 12.sp,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Button(
                    onClick = { onTaskCompleted(task.id) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Bắt đầu nhiệm vụ",
                        color = PrimaryRed,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun TaskProgressBar(
    completed: Int,
    total: Int,
    modifier: Modifier = Modifier
) {
    val progress = if (total > 0) completed.toFloat() / total else 0f

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Tiến độ hành trình",
                fontSize = 13.sp,
                color = TextSecondary
            )
            Text(
                text = "$completed/$total nhiệm vụ",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = PrimaryRed
            )
        }
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = PrimaryRed,
            trackColor = Color(0xFFE0D5C5)
        )
    }
}

@Composable
fun TaskCard(
    task: Task,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        onClick = {
            expanded = !expanded
            onClick()
        },
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (task.isCompleted) Color(0xFFF5F5F5) else CardBackground
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (task.isCompleted) 0.dp else 3.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = if (task.isCompleted)
                        Icons.Default.CheckCircle
                    else
                        Icons.Default.RadioButtonUnchecked,
                    contentDescription = null,
                    tint = if (task.isCompleted) EasyGreen else CompletedGray,
                    modifier = Modifier.size(22.dp)
                )

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (task.isCompleted) TextSecondary else TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                DifficultyBadge(difficulty = task.difficulty)
            }

            Text(
                text = task.description,
                fontSize = 13.sp,
                color = if (task.isCompleted) CompletedGray else TextSecondary,
                lineHeight = 18.sp,
                maxLines = if (expanded) Int.MAX_VALUE else 2,
                overflow = if (expanded) TextOverflow.Visible else TextOverflow.Ellipsis
            )

            InfoSection(
                icon = Icons.Outlined.Lightbulb,
                label = "Giải thích văn hóa",
                content = task.culturalExplanation,
                accentColor = CulturalAmber,
                isCompleted = task.isCompleted
            )

            InfoSection(
                icon = Icons.Outlined.TaskAlt,
                label = "Yêu cầu hoàn thành",
                content = task.completionRequirement,
                accentColor = CompletionBlue,
                isCompleted = task.isCompleted
            )
        }
    }
}

@Composable
private fun InfoSection(
    icon: ImageVector,
    label: String,
    content: String,
    accentColor: Color,
    isCompleted: Boolean
) {
    val effectiveAccent = if (isCompleted) CompletedGray else accentColor

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = effectiveAccent.copy(alpha = 0.08f),
                shape = RoundedCornerShape(10.dp)
            )
            .padding(10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = effectiveAccent,
            modifier = Modifier
                .size(16.dp)
                .padding(top = 1.dp)
        )
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = effectiveAccent,
                letterSpacing = 0.3.sp
            )
            Text(
                text = content,
                fontSize = 12.sp,
                color = if (isCompleted) CompletedGray else TextSecondary,
                lineHeight = 17.sp
            )
        }
    }
}

@Composable
fun DifficultyBadge(
    difficulty: TaskDifficulty,
    onDark: Boolean = false
) {
    val (label, color) = when (difficulty) {
        TaskDifficulty.EASY -> "Dễ" to EasyGreen
        TaskDifficulty.MEDIUM -> "Trung bình" to MediumOrange
        TaskDifficulty.HARD -> "Khó" to HardRed
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

    val brush = Brush.linearGradient(
        colors = listOf(ShimmerLight, ShimmerDark, ShimmerLight),
        start = Offset.Zero,
        end = Offset(translateAnim, translateAnim)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(brush)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(brush)
        )
        repeat(3) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(brush)
            )
        }
    }
}

@Composable
private fun StorylineEmptyState(
    message: String = "Chưa có nhiệm vụ nào",
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
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Hành trình của bạn sắp bắt đầu. Hãy thử lại sau!",
            fontSize = 14.sp,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(text = "Thử lại", color = Color.White)
        }
    }
}

// ── Preview ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true)
@Composable
fun StorylineScreenPreview() {
    BeVietnamTheme {
        StorylineScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun TaskCardPreview() {
    BeVietnamTheme {
        Surface(color = Background) {
            TaskCard(
                task = Task(
                    id = "preview",
                    title = "Nhiệm vụ mẫu",
                    description = "Mô tả nhiệm vụ mẫu cho preview.",
                    culturalExplanation = "Giải thích văn hóa.",
                    completionRequirement = "Yêu cầu hoàn thành.",
                    difficulty = TaskDifficulty.MEDIUM
                ),
                onClick = {},
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}
