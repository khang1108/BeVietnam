package com.bevietnam.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.bevietnam.R
import com.bevietnam.core.model.Place
import com.bevietnam.ui.theme.BeVietnamTheme

/**
 * Thẻ hiển thị địa điểm du lịch văn hóa (Place Card) của ứng dụng BeVietnam.
 *
 * Hiển thị tóm tắt thông tin của địa điểm bao gồm hình ảnh thu nhỏ (thumbnail), tên địa điểm,
 * danh mục phân loại, mô tả ngắn và nút nhấn lưu trữ nhanh.
 *
 * @param place Đối tượng chứa dữ liệu thông tin địa điểm ([Place]).
 * @param onClick Sự kiện click kích hoạt khi người dùng nhấn chọn thẻ địa điểm (để điều hướng sang chi tiết).
 * @param modifier [Modifier] dùng để căn chỉnh, định hình kích thước layout bên ngoài truyền vào.
 * @param index Chỉ số thứ tự tùy chọn (ví dụ: hiển thị số thứ tự trong bảng xếp hạng). Mặc định là `null`.
 */
@Composable
fun PlaceCard(
    place: Place,
    onClick: () -> Unit,
    onLocateClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    index: Int? = null
) {
    val context = LocalContext.current

    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = Color.Black.copy(alpha = 0.08f)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                // Tải ảnh mượt mà với Coil AsyncImage và hiệu ứng crossfade
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(place.imageUrl?.toIntOrNull() ?: place.imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = stringResource(R.string.place_image_desc, place.name),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                if (index != null) {
                    Text(
                        text = "$index",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(6.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = place.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Danh mục phân loại dạng nhãn (Badge)
                androidx.compose.material3.Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = place.category.uppercase(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Mô tả ngắn
                Text(
                    text = place.description,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (onLocateClick != null) {
                    IconButton(
                        onClick = onLocateClick,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.MyLocation,
                            contentDescription = stringResource(R.string.place_locate),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                
                val referenceUrl = place.referenceUrl
                if (!referenceUrl.isNullOrEmpty()) {
                    val uriHandler = LocalUriHandler.current
                    IconButton(
                        onClick = { uriHandler.openUri(referenceUrl) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Public,
                            contentDescription = stringResource(R.string.place_learn_more),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

    @Preview(showBackground = true)
    @Composable
    private fun PlaceCardPreview() {
        BeVietnamTheme {
            Box(modifier = Modifier.padding(16.dp)) {
                PlaceCard(
                    place = Place(
                        id = "place-001",
                        name = "Văn Miếu - Quốc Tử Giám",
                        category = "temple",
                        description = "Quần thể di tích đa dạng và phong phú hàng đầu của thành phố Hà Nội.",
                        latitude = 21.0275,
                        longitude = 105.8357,
                        imageUrl = null,
                        referenceUrl = "https://vanmieu.gov.vn"
                    ),
                    onClick = {}
                )
            }
        }
    }

