package com.bevietnam.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.unit.dp

/**
 * Hiệu ứng Loading hình Hoa Sen (Lotus) được vẽ hoàn toàn bằng Vector (Canvas) của Jetpack Compose.
 * Không sử dụng ảnh, không dùng Lottie, siêu nhẹ và siêu mượt.
 */
@Composable
fun CulturalLoadingIndicator(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary
) {
    val infiniteTransition = rememberInfiniteTransition(label = "LotusTransition")
    
    // Animate độ xòe của các cánh hoa (tạo cảm giác hoa nở)
    val spread by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "SpreadAnimation"
    )

    // Animate độ lớn tổng thể tạo hiệu ứng "thở" (breathing)
    val breathingScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "BreathingAnimation"
    )

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(72.dp)) {
            val baseCenter = Offset(size.width / 2, size.height * 0.8f)
            val petalWidth = size.width * 0.35f
            val petalHeight = size.height * 0.6f

            scale(breathingScale, pivot = baseCenter) {
                // Cánh hoa số 1 (Ngoài cùng bên trái)
                rotate(degrees = -55f * spread, pivot = baseCenter) {
                    drawPetal(baseCenter, petalWidth * 0.8f, petalHeight * 0.7f, color.copy(alpha = 0.4f))
                }
                
                // Cánh hoa số 2 (Trái)
                rotate(degrees = -30f * spread, pivot = baseCenter) {
                    drawPetal(baseCenter, petalWidth * 0.9f, petalHeight * 0.85f, color.copy(alpha = 0.7f))
                }

                // Cánh hoa số 3 (Ngoài cùng bên phải)
                rotate(degrees = 55f * spread, pivot = baseCenter) {
                    drawPetal(baseCenter, petalWidth * 0.8f, petalHeight * 0.7f, color.copy(alpha = 0.4f))
                }
                
                // Cánh hoa số 4 (Phải)
                rotate(degrees = 30f * spread, pivot = baseCenter) {
                    drawPetal(baseCenter, petalWidth * 0.9f, petalHeight * 0.85f, color.copy(alpha = 0.7f))
                }

                // Cánh hoa trung tâm (Chính giữa)
                drawPetal(baseCenter, petalWidth, petalHeight, color)
                
                // Đế hoa (Đài sen phía dưới)
                val baseWidth = size.width * 0.45f
                val baseHeight = size.height * 0.15f
                val basePath = Path().apply {
                    moveTo(baseCenter.x - baseWidth / 2, baseCenter.y - baseHeight / 2)
                    quadraticBezierTo(baseCenter.x, baseCenter.y + baseHeight, baseCenter.x + baseWidth / 2, baseCenter.y - baseHeight / 2)
                    quadraticBezierTo(baseCenter.x, baseCenter.y, baseCenter.x - baseWidth / 2, baseCenter.y - baseHeight / 2)
                    close()
                }
                drawPath(path = basePath, color = color.copy(alpha = 0.8f))
            }
        }
    }
}

/**
 * Hàm vẽ một cánh hoa sen
 */
private fun DrawScope.drawPetal(baseCenter: Offset, width: Float, height: Float, color: Color) {
    val path = Path().apply {
        // Điểm nhọn dưới cùng (cuống cánh hoa)
        moveTo(baseCenter.x, baseCenter.y)
        // Uốn lượn sang trái rồi lên đỉnh cánh hoa
        quadraticBezierTo(
            baseCenter.x - width, baseCenter.y - height / 2, // Control point
            baseCenter.x, baseCenter.y - height              // Đỉnh cánh
        )
        // Uốn lượn sang phải rồi về lại cuống
        quadraticBezierTo(
            baseCenter.x + width, baseCenter.y - height / 2, // Control point
            baseCenter.x, baseCenter.y                       // Cuống cánh
        )
        close()
    }
    drawPath(path = path, color = color)
}
