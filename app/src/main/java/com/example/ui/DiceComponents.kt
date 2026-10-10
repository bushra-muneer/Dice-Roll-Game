package com.example.ui

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.rounded.Smartphone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DiceTheme
import com.example.model.DiceType
import com.example.model.RollHistoryEntry
import com.example.model.ShakeSensitivity
import com.example.model.SingleDieState
import androidx.compose.ui.unit.min
import kotlin.math.cos
import kotlin.math.sin

/**
 * High-contrast, scalable D6 die face rendered strictly on Canvas with indented pips.
 */
@Composable
fun D6DieCanvas(
    value: Int,
    theme: DiceTheme,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val sizePx = size.minDimension
        val cornerRadius = sizePx * 0.18f
        val pipRadius = sizePx * 0.088f

        // Base 3D Die Body with bevel gradient
        val bodyBrush = Brush.linearGradient(
            colors = listOf(
                theme.bodyColor,
                theme.bodyColor.copy(alpha = 0.92f)
            ),
            start = Offset(0f, 0f),
            end = Offset(sizePx, sizePx)
        )

        // Subtle drop border
        drawRoundRect(
            color = theme.borderColor,
            topLeft = Offset(0f, 0f),
            size = Size(sizePx, sizePx),
            cornerRadius = CornerRadius(cornerRadius, cornerRadius),
            style = Fill
        )

        // Main die face
        val inset = sizePx * 0.035f
        drawRoundRect(
            brush = bodyBrush,
            topLeft = Offset(inset, inset),
            size = Size(sizePx - inset * 2, sizePx - inset * 2),
            cornerRadius = CornerRadius(cornerRadius * 0.85f, cornerRadius * 0.85f),
            style = Fill
        )

        // Inner subtle specular highlight
        drawRoundRect(
            color = Color.White.copy(alpha = if (theme.isDarkDie) 0.12f else 0.4f),
            topLeft = Offset(inset * 1.5f, inset * 1.5f),
            size = Size(sizePx - inset * 3, sizePx - inset * 3),
            cornerRadius = CornerRadius(cornerRadius * 0.75f, cornerRadius * 0.75f),
            style = Stroke(width = sizePx * 0.015f)
        )

        // Draw Pips (dots)
        val cx = sizePx / 2f
        val cy = sizePx / 2f
        val offset = sizePx * 0.25f

        fun drawPip(x: Float, y: Float) {
            // Indented concave shadow
            drawCircle(
                color = if (theme.isDarkDie) Color.Black.copy(alpha = 0.45f) else Color.Black.copy(alpha = 0.15f),
                radius = pipRadius * 1.08f,
                center = Offset(x, y + pipRadius * 0.15f)
            )
            // Main pip dot
            drawCircle(
                color = theme.pipColor,
                radius = pipRadius,
                center = Offset(x, y)
            )
        }

        when (value.coerceIn(1, 6)) {
            1 -> {
                drawPip(cx, cy)
            }
            2 -> {
                drawPip(cx - offset, cy - offset)
                drawPip(cx + offset, cy + offset)
            }
            3 -> {
                drawPip(cx - offset, cy - offset)
                drawPip(cx, cy)
                drawPip(cx + offset, cy + offset)
            }
            4 -> {
                drawPip(cx - offset, cy - offset)
                drawPip(cx + offset, cy - offset)
                drawPip(cx - offset, cy + offset)
                drawPip(cx + offset, cy + offset)
            }
            5 -> {
                drawPip(cx - offset, cy - offset)
                drawPip(cx + offset, cy - offset)
                drawPip(cx, cy)
                drawPip(cx - offset, cy + offset)
                drawPip(cx + offset, cy + offset)
            }
            6 -> {
                drawPip(cx - offset, cy - offset)
                drawPip(cx - offset, cy)
                drawPip(cx - offset, cy + offset)
                drawPip(cx + offset, cy - offset)
                drawPip(cx + offset, cy)
                drawPip(cx + offset, cy + offset)
            }
        }
    }
}

/**
 * Geometric, faceted RPG Dice (D4, D8, D10, D12, D20) drawn cleanly on Canvas.
 */
@Composable
fun RpgDieCanvas(
    value: Int,
    type: DiceType,
    theme: DiceTheme,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val sizePx = size.minDimension
        val cx = sizePx / 2f
        val cy = sizePx / 2f
        val radius = sizePx * 0.44f

        val strokeColor = theme.borderColor
        val baseFillColor = theme.bodyColor
        val facetHighlight = if (theme.isDarkDie) Color.White.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.08f)

        when (type) {
            DiceType.D4 -> {
                // Equilateral triangle
                val p1 = Offset(cx, cy - radius * 1.05f)
                val p2 = Offset(cx - radius * 0.95f, cy + radius * 0.7f)
                val p3 = Offset(cx + radius * 0.95f, cy + radius * 0.7f)

                val path = Path().apply {
                    moveTo(p1.x, p1.y)
                    lineTo(p2.x, p2.y)
                    lineTo(p3.x, p3.y)
                    close()
                }
                drawPath(path, baseFillColor)

                // Facet bevel lines to center
                drawLine(strokeColor, p1, Offset(cx, cy + radius * 0.1f), strokeWidth = 3f)
                drawLine(strokeColor, p2, Offset(cx, cy + radius * 0.1f), strokeWidth = 3f)
                drawLine(strokeColor, p3, Offset(cx, cy + radius * 0.1f), strokeWidth = 3f)
                drawPath(path, strokeColor, style = Stroke(width = 4f))
            }
            DiceType.D8 -> {
                // Diamond / Octahedron silhouette
                val pTop = Offset(cx, cy - radius)
                val pBottom = Offset(cx, cy + radius)
                val pLeft = Offset(cx - radius * 0.85f, cy)
                val pRight = Offset(cx + radius * 0.85f, cy)

                val path = Path().apply {
                    moveTo(pTop.x, pTop.y)
                    lineTo(pRight.x, pRight.y)
                    lineTo(pBottom.x, pBottom.y)
                    lineTo(pLeft.x, pLeft.y)
                    close()
                }
                drawPath(path, baseFillColor)

                // Cross facet lines
                drawLine(strokeColor, pTop, pBottom, strokeWidth = 3f)
                drawLine(strokeColor, pLeft, pRight, strokeWidth = 3f)
                drawPath(path, strokeColor, style = Stroke(width = 4f))
            }
            DiceType.D10 -> {
                // Decahedron kite silhouette
                val pTop = Offset(cx, cy - radius)
                val pRightTop = Offset(cx + radius * 0.82f, cy - radius * 0.2f)
                val pRightBottom = Offset(cx + radius * 0.5f, cy + radius * 0.85f)
                val pLeftBottom = Offset(cx - radius * 0.5f, cy + radius * 0.85f)
                val pLeftTop = Offset(cx - radius * 0.82f, cy - radius * 0.2f)

                val path = Path().apply {
                    moveTo(pTop.x, pTop.y)
                    lineTo(pRightTop.x, pRightTop.y)
                    lineTo(pRightBottom.x, pRightBottom.y)
                    lineTo(pLeftBottom.x, pLeftBottom.y)
                    lineTo(pLeftTop.x, pLeftTop.y)
                    close()
                }
                drawPath(path, baseFillColor)

                // Facet spine lines
                drawLine(strokeColor, pTop, Offset(cx, cy + radius * 0.3f), strokeWidth = 3f)
                drawLine(strokeColor, pLeftTop, Offset(cx, cy + radius * 0.3f), strokeWidth = 3f)
                drawLine(strokeColor, pRightTop, Offset(cx, cy + radius * 0.3f), strokeWidth = 3f)
                drawPath(path, strokeColor, style = Stroke(width = 4f))
            }
            DiceType.D12 -> {
                // Pentagon silhouette
                val path = Path()
                for (i in 0 until 5) {
                    val angle = (i * 72 - 90) * (Math.PI / 180.0)
                    val px = (cx + radius * cos(angle)).toFloat()
                    val py = (cy + radius * sin(angle)).toFloat()
                    if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
                }
                path.close()
                drawPath(path, baseFillColor)

                // Internal pentagram facet lines
                for (i in 0 until 5) {
                    val angle = (i * 72 - 90) * (Math.PI / 180.0)
                    val px = (cx + radius * cos(angle)).toFloat()
                    val py = (cy + radius * sin(angle)).toFloat()
                    drawLine(strokeColor, Offset(px, py), Offset(cx, cy), strokeWidth = 2.5f)
                }
                drawPath(path, strokeColor, style = Stroke(width = 4f))
            }
            DiceType.D20 -> {
                // Hexagonal icosahedral silhouette
                val path = Path()
                for (i in 0 until 6) {
                    val angle = (i * 60 - 90) * (Math.PI / 180.0)
                    val px = (cx + radius * cos(angle)).toFloat()
                    val py = (cy + radius * sin(angle)).toFloat()
                    if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
                }
                path.close()
                drawPath(path, baseFillColor)

                // Inner triangle face
                val innerR = radius * 0.58f
                val innerPath = Path()
                for (i in 0 until 3) {
                    val angle = (i * 120 - 90) * (Math.PI / 180.0)
                    val px = (cx + innerR * cos(angle)).toFloat()
                    val py = (cy + innerR * sin(angle)).toFloat()
                    if (i == 0) innerPath.moveTo(px, py) else innerPath.lineTo(px, py)
                }
                innerPath.close()
                drawPath(innerPath, facetHighlight)
                drawPath(innerPath, strokeColor, style = Stroke(width = 2.5f))

                drawPath(path, strokeColor, style = Stroke(width = 4f))
            }
            DiceType.D6 -> {
                // Fallback (handled by D6DieCanvas)
            }
        }

        // Draw crisp numeric value in center
        drawIntoCanvas { canvas ->
            val paint = Paint().apply {
                color = theme.pipColor.toArgb()
                textSize = sizePx * 0.36f
                isAntiAlias = true
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
            }
            val text = value.toString()
            val textBounds = android.graphics.Rect()
            paint.getTextBounds(text, 0, text.length, textBounds)
            val textY = cy - textBounds.exactCenterY()
            canvas.nativeCanvas.drawText(text, cx, textY, paint)
        }
    }
}

/**
 * Animated Die wrapper with full 3D tumbling rotation, bounce scale, and tap interaction.
 */
@Composable
fun InteractiveDie(
    die: SingleDieState,
    theme: DiceTheme,
    isRolling: Boolean,
    size: Dp,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val rotZ by animateFloatAsState(
        targetValue = die.rotationZ,
        animationSpec = tween(durationMillis = 65, easing = FastOutSlowInEasing),
        label = "rotZ"
    )
    val scale by animateFloatAsState(
        targetValue = die.scale,
        animationSpec = tween(durationMillis = 65, easing = FastOutSlowInEasing),
        label = "scale"
    )

    Box(
        modifier = modifier
            .size(size)
            .graphicsLayer {
                rotationZ = rotZ
                rotationX = die.rotationX
                rotationY = die.rotationY
                scaleX = scale
                scaleY = scale
                translationX = die.offsetX
                translationY = die.offsetY
                cameraDistance = 12f * density
            }
            .shadow(
                elevation = if (isRolling) 12.dp else 6.dp,
                shape = RoundedCornerShape(size * 0.2f),
                ambientColor = Color.Black.copy(alpha = 0.35f),
                spotColor = Color.Black.copy(alpha = 0.5f)
            )
            .clip(RoundedCornerShape(size * 0.2f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .testTag("die_item_${die.id}"),
        contentAlignment = Alignment.Center
    ) {
        if (die.type == DiceType.D6) {
            D6DieCanvas(
                value = die.displayValue,
                theme = theme,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            RpgDieCanvas(
                value = die.displayValue,
                type = die.type,
                theme = theme,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

/**
 * Responsive Dice Tray arranging 1 to 6 dice cleanly.
 */
@Composable
fun DiceTray(
    diceList: List<SingleDieState>,
    theme: DiceTheme,
    isRolling: Boolean,
    onRollTriggered: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("dice_tray_card")
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onRollTriggered
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
        ),
        border = CardDefaults.outlinedCardBorder()
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            val count = diceList.size
            val availableWidth = maxWidth
            val availableHeight = maxHeight

            // Adaptive sizing based on count
            val dieSize: Dp = when (count) {
                1 -> min(availableWidth * 0.52f, availableHeight * 0.52f)
                2 -> min(availableWidth * 0.38f, availableHeight * 0.38f)
                3 -> min(availableWidth * 0.30f, availableHeight * 0.30f)
                4 -> min(availableWidth * 0.32f, availableHeight * 0.32f)
                else -> min(availableWidth * 0.26f, availableHeight * 0.26f)
            }.coerceIn(72.dp, 160.dp)

            when (count) {
                1 -> {
                    InteractiveDie(
                        die = diceList[0],
                        theme = theme,
                        isRolling = isRolling,
                        size = dieSize,
                        onClick = onRollTriggered
                    )
                }
                2 -> {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        diceList.forEach { die ->
                            InteractiveDie(
                                die = die,
                                theme = theme,
                                isRolling = isRolling,
                                size = dieSize,
                                onClick = onRollTriggered
                            )
                        }
                    }
                }
                3 -> {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(14.dp, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        diceList.forEach { die ->
                            InteractiveDie(
                                die = die,
                                theme = theme,
                                isRolling = isRolling,
                                size = dieSize,
                                onClick = onRollTriggered
                            )
                        }
                    }
                }
                4 -> {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            InteractiveDie(diceList[0], theme, isRolling, dieSize, onRollTriggered)
                            InteractiveDie(diceList[1], theme, isRolling, dieSize, onRollTriggered)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            InteractiveDie(diceList[2], theme, isRolling, dieSize, onRollTriggered)
                            InteractiveDie(diceList[3], theme, isRolling, dieSize, onRollTriggered)
                        }
                    }
                }
                5 -> {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            InteractiveDie(diceList[0], theme, isRolling, dieSize, onRollTriggered)
                            InteractiveDie(diceList[1], theme, isRolling, dieSize, onRollTriggered)
                            InteractiveDie(diceList[2], theme, isRolling, dieSize, onRollTriggered)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            InteractiveDie(diceList[3], theme, isRolling, dieSize, onRollTriggered)
                            InteractiveDie(diceList[4], theme, isRolling, dieSize, onRollTriggered)
                        }
                    }
                }
                6 -> {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            InteractiveDie(diceList[0], theme, isRolling, dieSize, onRollTriggered)
                            InteractiveDie(diceList[1], theme, isRolling, dieSize, onRollTriggered)
                            InteractiveDie(diceList[2], theme, isRolling, dieSize, onRollTriggered)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            InteractiveDie(diceList[3], theme, isRolling, dieSize, onRollTriggered)
                            InteractiveDie(diceList[4], theme, isRolling, dieSize, onRollTriggered)
                            InteractiveDie(diceList[5], theme, isRolling, dieSize, onRollTriggered)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Real-time accelerometer meter showing live shake force compared to trigger threshold.
 */
@Composable
fun ShakeForceMeter(
    currentForce: Float,
    threshold: Float,
    isEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    if (!isEnabled) return

    val progress = (currentForce / threshold).coerceIn(0f, 1f)
    val isThresholdReached = currentForce >= threshold

    val meterColor by animateColorAsState(
        targetValue = if (isThresholdReached) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary,
        label = "meterColor"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.Smartphone,
                    contentDescription = "Shake gesture status",
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isThresholdReached) "Shake threshold hit!" else "Shake intensity",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isThresholdReached) meterColor else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (isThresholdReached) FontWeight.Bold else FontWeight.Normal
                )
            }
            Text(
                text = "${"%.1f".format(currentForce)} / ${threshold.toInt()} m/s²",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = meterColor,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

/**
 * Total counter & breakdown banner.
 */
@Composable
fun RollSummaryBanner(
    total: Int,
    breakdown: String,
    diceType: DiceType,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("roll_summary_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "TOTAL SUM",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = total.toString(),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Text(
                        text = diceType.label,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = breakdown.ifEmpty { "Roll to begin" },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f),
                    maxLines = 1
                )
            }
        }
    }
}

/**
 * Dice count selector pills (1 to 6).
 */
@Composable
fun DiceCountSelector(
    currentCount: Int,
    onSelectCount: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        (1..6).forEach { count ->
            val isSelected = count == currentCount
            FilterChip(
                selected = isSelected,
                onClick = { onSelectCount(count) },
                label = {
                    Text(
                        text = "$count🎲",
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                modifier = Modifier.testTag("count_chip_$count"),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    }
}

/**
 * RPG Dice Type Selector Chips (D4, D6, D8, D10, D12, D20).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DiceTypeSelector(
    selectedType: DiceType,
    onSelectType: (DiceType) -> Unit,
    modifier: Modifier = Modifier
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        DiceType.values().forEach { type ->
            val isSelected = type == selectedType
            FilterChip(
                selected = isSelected,
                onClick = { onSelectType(type) },
                label = { Text(type.label) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Casino,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                },
                modifier = Modifier.testTag("type_chip_${type.name}"),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.secondary,
                    selectedLabelColor = MaterialTheme.colorScheme.onSecondary
                )
            )
        }
    }
}

/**
 * Roll History Bottom Sheet.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RollHistoryBottomSheet(
    history: List<RollHistoryEntry>,
    onClearHistory: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = "Roll History",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Roll History",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                if (history.isNotEmpty()) {
                    TextButton(
                        onClick = onClearHistory,
                        modifier = Modifier.testTag("clear_history_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Clear History",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Clear")
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (history.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No rolls recorded yet.\nShake or tap to roll your first set!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                // Quick stats summary
                val totals = history.map { it.total }
                val maxRoll = totals.maxOrNull() ?: 0
                val minRoll = totals.minOrNull() ?: 0
                val avgRoll = totals.average()

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatPill("Total Rolls", history.size.toString())
                        StatPill("Highest", maxRoll.toString())
                        StatPill("Lowest", minRoll.toString())
                        StatPill("Average", "%.1f".format(avgRoll))
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(history, key = { it.id }) { entry ->
                        HistoryRowItem(entry)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatPill(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun HistoryRowItem(entry: RollHistoryEntry) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (entry.isShakeTriggered) Icons.Rounded.Smartphone else Icons.Default.TouchApp,
                        contentDescription = if (entry.isShakeTriggered) "Shake roll" else "Tap roll",
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${entry.values.size}${entry.diceType.label} • ${entry.formattedTime}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "[ ${entry.values.joinToString(", ")} ]" +
                        if (entry.modifier != 0) " (${if (entry.modifier > 0) "+${entry.modifier}" else entry.modifier})" else "",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }

            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(38.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = entry.total.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

/**
 * Settings & Sensitivity Sheet.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsBottomSheet(
    currentSensitivity: ShakeSensitivity,
    onSensitivityChanged: (ShakeSensitivity) -> Unit,
    isShakeEnabled: Boolean,
    onToggleShake: () -> Unit,
    isHapticsEnabled: Boolean,
    onToggleHaptics: () -> Unit,
    isSoundEnabled: Boolean,
    onToggleSound: () -> Unit,
    currentTheme: DiceTheme,
    onThemeChanged: (DiceTheme) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Preferences & Sensors",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Shake-to-Roll Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Shake to Roll",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Use accelerometer motion to trigger dice rolls",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = isShakeEnabled,
                    onCheckedChange = { onToggleShake() },
                    modifier = Modifier.testTag("shake_switch")
                )
            }

            AnimatedVisibility(visible = isShakeEnabled) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    Text(
                        text = "Shake Sensitivity Threshold",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ShakeSensitivity.values().forEach { sensitivity ->
                            val isSelected = sensitivity == currentSensitivity
                            FilterChip(
                                selected = isSelected,
                                onClick = { onSensitivityChanged(sensitivity) },
                                label = { Text(sensitivity.label) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("sensitivity_${sensitivity.name}"),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                )
                            )
                        }
                    }
                    Text(
                        text = currentSensitivity.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            // Haptics Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Haptic Vibration",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Tactile ticks during rolling & impact feedback",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = isHapticsEnabled,
                    onCheckedChange = { onToggleHaptics() },
                    modifier = Modifier.testTag("haptics_switch")
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Sound Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Rolling Audio Clatter",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Physical sound effects for tumbling and settling",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = isSoundEnabled,
                    onCheckedChange = { onToggleSound() },
                    modifier = Modifier.testTag("sound_switch")
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            // Dice Theme Swatches
            Text(
                text = "Dice Visual Theme",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                DiceTheme.values().forEach { theme ->
                    val isSelected = theme == currentTheme
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(theme.bodyColor)
                            .border(
                                width = if (isSelected) 3.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else theme.borderColor,
                                shape = CircleShape
                            )
                            .clickable { onThemeChanged(theme) }
                            .testTag("theme_${theme.name}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(theme.pipColor)
                        )
                    }
                }
            }
        }
    }
}
