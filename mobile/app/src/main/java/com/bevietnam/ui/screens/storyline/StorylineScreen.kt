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
import com.bevietnam.ui.theme.LocalCulturalColors

/**
 * Màn hình Hành trình cốt truyện & Nhiệm vụ (Storyline Screen) của ứng dụng BeVietnam.
 *
 * Cung cấp hành trình trò chơi hóa (gamification) tìm hiểu văn hóa Việt Nam của người dùng.
 * Cho phép xem nhiệm vụ hiện tại, theo dõi tiến độ tổng thể và mở rộng xem giải thích chi tiết về văn hóa lịch sử Việt Nam.
 *
 * @param viewModel ViewModel quản lý trạng thái dữ liệu màn hình cốt truyện ([StorylineViewModel]). Mặc định là [hiltViewModel].
 * @param onTaskClick Callback kích hoạt khi người dùng nhấn chọn một thẻ nhiệm vụ ([Task]).
 * @param modifier [Modifier] dùng để định hình bố cục bên ngoài truyền vào.
 */
@Composable
fun StorylineScreen(
    viewModel: StorylineViewModel = hiltViewModel(),
    onTaskClick: (Task) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    StorylineScreenContent(
        uiState = uiState,
        onRetry = viewModel::loadTasks,
        onTaskClick = onTaskClick,
        onTaskCompleted = viewModel::onTaskCompleted,
        modifier = modifier
    )
}

/**
 * Triển khai phân phối giao diện chính của màn hình Cốt truyện & Nhiệm vụ.
 *
 * Tự động chuyển đổi giữa các giao diện đang tải (Shimmer), giao diện lỗi/rỗng và giao diện danh sách nhiệm vụ thành công.
 *
 * @param uiState Đối tượng đại diện cho trạng thái màn hình ([StorylineUiState]).
 * @param onRetry Callback nhấn thử lại khi tải dữ liệu gặp sự cố.
 * @param onTaskClick Callback nhấn xem thông tin thẻ nhiệm vụ.
 * @param onTaskCompleted Callback nhấn xác nhận hoàn thành nhiệm vụ.
 * @param modifier [Modifier] dùng để định hình bố cục bên ngoài truyền vào.
 */
@Composable
fun StorylineScreenContent(
    uiState: StorylineUiState,
    onRetry: () -> Unit,
    onTaskClick: (Task) -> Unit,
    onTaskCompleted: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        when (uiState) {
            is StorylineUiState.Loading -> StorylineLoadingState()
            is StorylineUiState.Empty -> StorylineEmptyState(onRetry = onRetry)
            is StorylineUiState.Error -> StorylineEmptyState(
                message = uiState.message,
                onRetry = onRetry
            )
            is StorylineUiState.Success -> StorylineSuccessContent(
                state = uiState,
                onTaskClick = onTaskClick,
                onTaskCompleted = onTaskCompleted
            )
        }
    }
}

/**
 * Giao diện khi tải dữ liệu thành công (Success State) của màn hình Cốt truyện.
 *
 * Hiển thị Banner nhiệm vụ tiếp theo nổi bật trên cùng, thanh tiến độ tổng thể,
 * và danh sách cuộn mượt mà tất cả nhiệm vụ đã được giao.
 */
@Composable
private fun StorylineSuccessContent(
    state: StorylineUiState.Success,
    onTaskClick: (Task) -> Unit,
    onTaskCompleted: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        // Banner hiển thị nổi bật Nhiệm vụ tiếp theo cần làm
        state.nextTask?.let {
            item {
                NextTaskBanner(
                    task = it,
                    onTaskCompleted = onTaskCompleted
                )
            }
        }

        // Thanh tiến độ tổng thể hành trình
        item {
            val completedCount = state.tasks.count { it.isCompleted }
            TaskProgressBar(
                completed = completedCount,
                total = state.tasks.size,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        // Tiêu đề danh sách
        item {
            Text(
                text = "Tất cả nhiệm vụ",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        // Danh sách tất cả các nhiệm vụ với stable key đảm bảo hiệu năng
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

/**
 * Banner hiển thị nổi bật Nhiệm vụ tiếp theo cần làm (Next Task Banner).
 *
 * Sử dụng hình nền dải màu chuyển sắc (gradient) đỏ nổi bật mang bản sắc văn hóa Việt Nam.
 */
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
                        colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primaryContainer),
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
                    val culturalColors = LocalCulturalColors.current
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null, // Icon trang trí
                        tint = culturalColors.goldColor,
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
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * Thanh chỉ báo tiến độ tổng thể của Hành trình (Task Progress Bar).
 */
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
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "$completed/$total nhiệm vụ",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.outlineVariant
        )
    }
}

/**
 * Thẻ hiển thị một nhiệm vụ cụ thể (Task Card).
 *
 * Cho phép nhấn để mở rộng/thu gọn hiển thị phần giải thích văn hóa bổ sung.
 *
 * @param task Đối tượng dữ liệu chứa thông tin chi tiết nhiệm vụ ([Task]).
 * @param onClick Callback kích hoạt khi người dùng nhấn chọn thẻ.
 * @param modifier [Modifier] dùng để định hình bố cục bên ngoài truyền vào.
 */
@Composable
fun TaskCard(
    task: Task,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    val culturalColors = LocalCulturalColors.current

    Card(
        onClick = {
            expanded = !expanded
            onClick()
        },
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (task.isCompleted) culturalColors.shimmerLight else MaterialTheme.colorScheme.surface
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
                    contentDescription = if (task.isCompleted) "Nhiệm vụ đã hoàn thành" else "Nhiệm vụ chưa hoàn thành",
                    tint = if (task.isCompleted) culturalColors.easyColor else culturalColors.completedGray,
                    modifier = Modifier.size(22.dp)
                )

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                DifficultyBadge(difficulty = task.difficulty)
            }

            Text(
                text = task.description,
                fontSize = 13.sp,
                color = if (task.isCompleted) culturalColors.completedGray else MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp,
                maxLines = if (expanded) Int.MAX_VALUE else 2,
                overflow = if (expanded) TextOverflow.Visible else TextOverflow.Ellipsis
            )

            // Phần giải thích ý nghĩa văn hóa hấp dẫn
            InfoSection(
                icon = Icons.Outlined.Lightbulb,
                label = "Giải thích văn hóa",
                content = task.culturalExplanation,
                accentColor = culturalColors.amberColor,
                isCompleted = task.isCompleted
            )

            // Phần yêu cầu chụp ảnh check-in
            InfoSection(
                icon = Icons.Outlined.TaskAlt,
                label = "Yêu cầu hoàn thành",
                content = task.completionRequirement,
                accentColor = culturalColors.completionBlue,
                isCompleted = task.isCompleted
            )
        }
    }
}

/**
 * Mục thông tin bổ sung (Info Section) nằm bên trong thẻ nhiệm vụ.
 *
 * Phân tách màu sắc trực quan tùy chọn (ví dụ màu vàng hổ phách cho Giải thích văn hóa).
 */
@Composable
private fun InfoSection(
    icon: ImageVector,
    label: String,
    content: String,
    accentColor: Color,
    isCompleted: Boolean
) {
    val culturalColors = LocalCulturalColors.current
    val effectiveAccent = if (isCompleted) culturalColors.completedGray else accentColor

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
                color = if (isCompleted) culturalColors.completedGray else MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 17.sp
            )
        }
    }
}

/**
 * Nhãn mức độ khó (Difficulty Badge) của từng thử thách nhiệm vụ.
 *
 * @param difficulty Mức độ khó của nhiệm vụ ([TaskDifficulty]).
 * @param onDark Xác định xem có vẽ trên nền dải màu tối hay không để căn chỉnh tương phản. Mặc định là `false`.
 */
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

/**
 * Trạng thái đang tải hành trình nhiệm vụ (Shimmer Loading State).
 */
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

/**
 * Trạng thái rỗng (Empty State) hiển thị khi chưa có nhiệm vụ nào được giao.
 */
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
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Hành trình của bạn sắp bắt đầu. Hãy thử lại sau!",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(text = "Thử lại", color = Color.White)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun StorylineScreenSuccessPreview() {
    val mockTasks = listOf(
        Task(
            id = "1",
            title = "Thử thách Phở",
            description = "Tìm và thưởng thức một bát phở truyền thống.",
            culturalExplanation = "Phở là món ăn quốc hồn quốc túy của Việt Nam.",
            completionRequirement = "Chụp ảnh bát phở của bạn.",
            difficulty = TaskDifficulty.EASY,
            isCompleted = true
        ),
        Task(
            id = "2",
            title = "Khám phá Văn Miếu",
            description = "Ghé thăm trường đại học đầu tiên của Việt Nam.",
            culturalExplanation = "Nơi thờ Khổng Tử và các bậc hiền triết.",
            completionRequirement = "Check-in tại cổng Văn Miếu.",
            difficulty = TaskDifficulty.MEDIUM,
            isCompleted = false
        )
    )
    BeVietnamTheme {
        StorylineScreenContent(
            uiState = StorylineUiState.Success(tasks = mockTasks, nextTask = mockTasks[1]),
            onRetry = {},
            onTaskClick = {},
            onTaskCompleted = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun StorylineScreenLoadingPreview() {
    BeVietnamTheme {
        StorylineScreenContent(
            uiState = StorylineUiState.Loading,
            onRetry = {},
            onTaskClick = {},
            onTaskCompleted = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TaskCardPreview() {
    BeVietnamTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
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
