package com.bevietnam.ui.screens.storyline

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
 * Trạng thái của một mắt xích trên lộ trình Hành trình.
 */
private enum class NodeState { COMPLETED, ACTIVE, LOCKED }

/**
 * Màn hình Hành trình văn hóa (Storyline) dạng lộ trình chuỗi.
 *
 * Các nhiệm vụ được xâu chuỗi tuần tự: hoàn thành nhiệm vụ đang mở khóa sẽ tự
 * mở khóa nhiệm vụ kế tiếp. Nhiệm vụ chưa tới hiển thị trạng thái "Khóa".
 */
@Composable
fun StorylineScreen(
    viewModel: StorylineViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        when (val state = uiState) {
            is StorylineUiState.Loading -> StorylineLoadingState()
            is StorylineUiState.Empty -> StorylineEmptyState(onRetry = viewModel::loadTasks)
            is StorylineUiState.Error -> StorylineEmptyState(
                message = state.message,
                onRetry = viewModel::loadTasks
            )
            is StorylineUiState.Success -> StorylineChain(
                tasks = state.tasks,
                onTaskCompleted = viewModel::onTaskCompleted
            )
        }
    }
}

/**
 * Lộ trình chuỗi các nhiệm vụ với trạng thái hoàn thành / đang mở khóa / khóa.
 */
@Composable
private fun StorylineChain(
    tasks: List<Task>,
    onTaskCompleted: (String) -> Unit
) {
    val activeIndex = tasks.indexOfFirst { !it.isCompleted }
    val completedCount = tasks.count { it.isCompleted }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        item {
            ChainHeader(completed = completedCount, total = tasks.size)
            Spacer(Modifier.height(18.dp))
        }

        itemsIndexed(items = tasks, key = { _, task -> task.id }) { index, task ->
            val nodeState = when {
                task.isCompleted -> NodeState.COMPLETED
                index == activeIndex -> NodeState.ACTIVE
                else -> NodeState.LOCKED
            }
            ChainItem(
                task = task,
                number = index + 1,
                state = nodeState,
                isLast = index == tasks.lastIndex,
                onComplete = { onTaskCompleted(task.id) }
            )
        }
    }
}

/**
 * Phần đầu trang: tiêu đề hành trình và thanh tiến độ.
 */
@Composable
private fun ChainHeader(completed: Int, total: Int) {
    val culturalColors = LocalCulturalColors.current
    val progress = if (total > 0) completed.toFloat() / total else 0f

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary)
            .padding(horizontal = 18.dp, vertical = 16.dp)
    ) {
        Column {
            Text(
                text = "Hành trình văn hóa",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = "Hoàn thành để mở khóa chặng tiếp theo",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
            )
            Spacer(Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(7.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White.copy(alpha = 0.25f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(20.dp))
                        .background(culturalColors.goldColor)
                )
            }
            Spacer(Modifier.height(5.dp))
            Text(
                text = "$completed/$total nhiệm vụ",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
            )
        }
    }
}

/**
 * Một mắt xích trên lộ trình: cột nút tròn + đường nối, và thẻ nội dung nhiệm vụ.
 */
@Composable
private fun ChainItem(
    task: Task,
    number: Int,
    state: NodeState,
    isLast: Boolean,
    onComplete: () -> Unit
) {
    val culturalColors = LocalCulturalColors.current
    val connectorColor = if (state == NodeState.COMPLETED)
        culturalColors.goldColor
    else
        MaterialTheme.colorScheme.outlineVariant

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(IntrinsicSize.Min)
    ) {
        // Cột nút tròn + đường nối dọc
        Column(
            modifier = Modifier
                .width(40.dp)
                .fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            NodeCircle(state = state, number = number)
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .weight(1f)
                        .background(connectorColor)
                )
            }
        }

        Spacer(Modifier.width(6.dp))

        // Thẻ nội dung nhiệm vụ
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 18.dp)
        ) {
            when (state) {
                NodeState.COMPLETED -> CompletedCard(task)
                NodeState.ACTIVE -> ActiveCard(task = task, onComplete = onComplete)
                NodeState.LOCKED -> LockedCard(task)
            }
        }
    }
}

/**
 * Nút tròn đại diện trạng thái mắt xích.
 */
@Composable
private fun NodeCircle(state: NodeState, number: Int) {
    val culturalColors = LocalCulturalColors.current
    val bgColor = when (state) {
        NodeState.COMPLETED -> culturalColors.goldColor
        NodeState.ACTIVE -> MaterialTheme.colorScheme.primary
        NodeState.LOCKED -> MaterialTheme.colorScheme.surfaceVariant
    }

    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        when (state) {
            NodeState.COMPLETED -> Icon(
                Icons.Default.Check,
                contentDescription = "Đã hoàn thành",
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
            NodeState.ACTIVE -> Text(
                text = "$number",
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            NodeState.LOCKED -> Icon(
                Icons.Default.Lock,
                contentDescription = "Đang khóa",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

/**
 * Thẻ nhiệm vụ đã hoàn thành.
 */
@Composable
private fun CompletedCard(task: Task) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(modifier = Modifier.padding(horizontal = 13.dp, vertical = 11.dp)) {
            Text(
                text = task.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(3.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = LocalCulturalColors.current.easyColor,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "Đã hoàn thành",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Thẻ nhiệm vụ đang mở khóa (nổi bật, có nút bắt đầu).
 */
@Composable
private fun ActiveCard(task: Task, onComplete: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
    ) {
        Column(modifier = Modifier.padding(13.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ĐANG MỞ KHÓA",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                DifficultyBadge(task.difficulty)
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = task.title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = task.description,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 17.sp
            )
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 9.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.PhotoCamera,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = task.completionRequirement,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.height(11.dp))
            Button(
                onClick = onComplete,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("Bắt đầu nhiệm vụ", fontWeight = FontWeight.Medium)
            }
        }
    }
}

/**
 * Thẻ nhiệm vụ đang khóa.
 */
@Composable
private fun LockedCard(task: Task) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 13.dp, vertical = 11.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = task.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
            )
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = "Khóa",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Nhãn mức độ khó của nhiệm vụ.
 */
@Composable
fun DifficultyBadge(difficulty: TaskDifficulty) {
    val culturalColors = LocalCulturalColors.current
    val (label, color) = when (difficulty) {
        TaskDifficulty.EASY -> "Dễ" to culturalColors.easyColor
        TaskDifficulty.MEDIUM -> "Trung bình" to culturalColors.mediumColor
        TaskDifficulty.HARD -> "Khó" to culturalColors.hardColor
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = color)
    }
}

/**
 * Trạng thái đang tải.
 */
@Composable
private fun StorylineLoadingState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
    }
}

/**
 * Trạng thái rỗng / lỗi.
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
private fun StorylineChainPreview() {
    val sample = listOf(
        Task("1", "Khám phá Văn Miếu", "Thăm trường đại học đầu tiên.", "Giải thích văn hóa.", "Chụp ảnh", TaskDifficulty.EASY, isCompleted = true),
        Task("2", "Học làm đèn lồng Hội An", "Làm đèn lồng truyền thống phố cổ.", "Giải thích văn hóa.", "Chụp ảnh đèn lồng", TaskDifficulty.EASY),
        Task("3", "Hang động Tràng An", "Đi thuyền khám phá hang động.", "Giải thích văn hóa.", "Chụp ảnh hang", TaskDifficulty.MEDIUM),
        Task("4", "Leo núi Bà Nà Hills", "Chinh phục Cầu Vàng.", "Giải thích văn hóa.", "Chụp ảnh Cầu Vàng", TaskDifficulty.HARD)
    )
    BeVietnamTheme {
        StorylineChain(tasks = sample, onTaskCompleted = {})
    }
}
