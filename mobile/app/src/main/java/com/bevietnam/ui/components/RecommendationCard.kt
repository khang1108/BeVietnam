package com.bevietnam.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.bevietnam.R
import com.bevietnam.core.model.RecommendationItem
import com.bevietnam.ui.theme.CulturalAmber
import com.bevietnam.ui.theme.CulturalGold
import com.bevietnam.ui.theme.Dimens

/**
 * Thẻ gợi ý địa điểm (Recommendation Card) trong bảng tin gợi ý BeVietnam.
 *
 * Thiết kế theo phong cách Hybrid: hiển thị 2 dòng đầu của giải thích (explanation)
 * và cho phép mở rộng xem toàn bộ bằng nút "Xem thêm". Nhấn mạnh phần explanation
 * với background riêng biệt để xây dựng niềm tin gợi ý (recommendation trust).
 *
 * @param item Đối tượng dữ liệu chứa thông tin gợi ý địa điểm ([RecommendationItem]).
 * @param onClick Sự kiện click kích hoạt khi người dùng nhấn vào thẻ (điều hướng chi tiết).
 * @param modifier [Modifier] dùng để căn chỉnh, định hình kích thước layout bên ngoài truyền vào.
 * @param rank Thứ hạng hiển thị trên thẻ (truyền từ index danh sách). Mặc định là 0.
 */
@Composable
fun RecommendationCard(
    item: RecommendationItem,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    rank: Int = 0
) {
    var isExplanationExpanded by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // ── Phần hình ảnh với Rank Badge và Category ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(item.thumbnailUrl?.toIntOrNull() ?: item.thumbnailUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = stringResource(R.string.recommendation_image_desc, item.name),
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Gradient overlay phía dưới ảnh
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.4f)
                                )
                            )
                        )
                )

                // Rank Badge (góc trên trái)
                if (rank > 0) {
                    Surface(
                        modifier = Modifier
                            .padding(Dimens.stackMd)
                            .align(Alignment.TopStart),
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(Dimens.spacingBase)
                    ) {
                        Text(
                            text = "#$rank",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Category chip (góc trên phải)
                Surface(
                    modifier = Modifier
                        .padding(Dimens.stackMd)
                        .align(Alignment.TopEnd),
                    color = Color.Black.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(Dimens.spacingBase)
                ) {
                    Text(
                        text = item.category,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // ── Phần nội dung thông tin ──
            Column(
                modifier = Modifier.padding(Dimens.gutterCard),
                verticalArrangement = Arrangement.spacedBy(Dimens.stackMd)
            ) {
                // Tên địa điểm
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // ── Score Badge ──
                ScoreBadge(score = item.score)

                // ── Explanation Section (Hybrid: 2 dòng + Xem thêm) ──
                ExplanationSection(
                    explanation = item.explanation,
                    isExpanded = isExplanationExpanded,
                    onToggle = { isExplanationExpanded = !isExplanationExpanded }
                )
            }
        }
    }
}

/**
 * Hiển thị Điểm phù hợp dạng phần trăm.
 *
 * Điểm số hiển thị dạng phần trăm (0.0–1.0 → 0%–100%) với vòng tròn tiến trình.
 *
 * @param score Điểm phù hợp (0.0–1.0).
 */
@Composable
private fun ScoreBadge(
    score: Float
) {
    val scorePercent = (score * 100).toInt()

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.stackMd),
        modifier = Modifier.fillMaxWidth()
    ) {
        // Score chip với gradient
        Surface(
            shape = RoundedCornerShape(Dimens.stackMd),
            color = Color.Transparent,
            modifier = Modifier
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            CulturalAmber.copy(alpha = 0.15f),
                            CulturalGold.copy(alpha = 0.1f)
                        )
                    ),
                    shape = RoundedCornerShape(Dimens.stackMd)
                )
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Vòng tròn nhỏ hiển thị điểm số
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(CulturalAmber, CulturalGold)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$scorePercent",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        lineHeight = 11.sp
                    )
                }
                Text(
                    text = stringResource(R.string.recommendation_score_unit),
                    style = MaterialTheme.typography.labelSmall,
                    color = CulturalAmber,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

/**
 * Phần giải thích chi tiết (Explanation Section) — trái tim của recommendation trust.
 *
 * Thiết kế theo phương án Hybrid:
 * - Mặc định hiển thị 2 dòng đầu của giải thích
 * - Nút "Xem thêm" / "Thu gọn" cho phép mở rộng xem toàn bộ
 * - Background gradient nhẹ (amber warm tone) để nổi bật khỏi các section khác
 *
 * @param explanation Đoạn giải thích chi tiết 2-3 câu.
 * @param isExpanded Trạng thái mở rộng hiện tại.
 * @param onToggle Callback khi nhấn nút "Xem thêm" / "Thu gọn".
 */
@Composable
private fun ExplanationSection(
    explanation: String,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = Color.Transparent,
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        CulturalAmber.copy(alpha = 0.08f),
                        CulturalGold.copy(alpha = 0.04f)
                    )
                ),
                shape = MaterialTheme.shapes.medium
            )
    ) {
        Column(
            modifier = Modifier
                .padding(Dimens.stackMd)
                .animateContentSize(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
        ) {
            // Header "Tại sao gợi ý này?"
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimens.stackSm)
            ) {
                Icon(
                    imageVector = Icons.Outlined.AutoAwesome,
                    contentDescription = null,
                    tint = CulturalAmber,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = stringResource(R.string.recommendation_why_title),
                    style = MaterialTheme.typography.labelLarge,
                    color = CulturalAmber,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(Dimens.spacingBase))

            // Nội dung giải thích (2 dòng hoặc full)
            Text(
                text = explanation,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 22.sp,
                maxLines = if (isExpanded) Int.MAX_VALUE else 2,
                overflow = if (isExpanded) TextOverflow.Clip else TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(Dimens.stackSm))

            // Nút "Xem thêm" / "Thu gọn"
            Row(
                modifier = Modifier
                    .clickable(onClick = onToggle)
                    .padding(vertical = Dimens.stackSm),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = if (isExpanded) stringResource(R.string.recommendation_collapse)
                           else stringResource(R.string.recommendation_expand),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
                Icon(
                    imageVector = if (isExpanded)
                        Icons.Outlined.KeyboardArrowUp
                    else
                        Icons.Outlined.KeyboardArrowDown,
                    contentDescription = if (isExpanded)
                        stringResource(R.string.recommendation_collapse_desc)
                    else
                        stringResource(R.string.recommendation_expand_desc),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
