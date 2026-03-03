package com.richtersfinger.impermanentrectangles.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.richtersfinger.impermanentrectangles.Item
import com.richtersfinger.impermanentrectangles.ui.theme.progressToColor
import kotlin.math.roundToInt

@Composable
fun ListItem(
    item: Item,
    isExpanded: Boolean,
    history: List<Float> = emptyList(),
    onToggleExpand: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onUpdateValue: (Int) -> Unit,
    onDragStart: () -> Unit = {},
    onDrag: (Float) -> Unit = {},
    onDragEnd: () -> Unit = {},
    isDragging: Boolean = false,
    isReorderMode: Boolean = false
) {
    if (isDragging && !isReorderMode) {
        // Safety check to reset dragging if mode changes unexpectedly
        LaunchedEffect(Unit) { onDragEnd() }
    }
    var showMenu by remember { mutableStateOf(false) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    val threshold = 150f

    val isComplete = item.targetValue > 0 && item.currentValue >= item.targetValue
    var wasComplete by remember(item.id) { mutableStateOf(isComplete) }
    var confettiTrigger by remember(item.id) { mutableIntStateOf(0) }

    LaunchedEffect(item.currentValue, item.targetValue) {
        val nowComplete = item.targetValue > 0 && item.currentValue >= item.targetValue
        if (!wasComplete && nowComplete) {
            confettiTrigger++
        }
        wasComplete = nowComplete
    }

    val dragElevation by animateDpAsState(if (isDragging) 8.dp else 0.dp, label = "dragElevation")
    val dragAlpha by animateFloatAsState(if (isDragging) 0.8f else 1f, label = "dragAlpha")
    val dragScale by animateFloatAsState(if (isDragging) 1.05f else 1f, label = "dragScale")
    val backgroundColor by animateColorAsState(
        if (isDragging) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent,
        label = "dragColor"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .zIndex(if (isDragging) 1f else 0f)
            .graphicsLayer {
                scaleX = dragScale
                scaleY = dragScale
                alpha = dragAlpha
                shadowElevation = dragElevation.toPx()
                shape = RoundedCornerShape(if (isDragging) 8.dp else 0.dp)
                clip = isDragging
            }
            .background(backgroundColor)
            .pointerInput(item.id, isReorderMode) {
                if (isReorderMode) {
                    detectDragGesturesAfterLongPress(
                        onDragStart = { onDragStart() },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            onDrag(dragAmount.y)
                        },
                        onDragEnd = { onDragEnd() },
                        onDragCancel = { onDragEnd() }
                    )
                }
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .pointerInput(item, isReorderMode) {
                    if (!isReorderMode) {
                        detectHorizontalDragGestures(
                            onDragEnd = {
                                if (offsetX > threshold) {
                                    onUpdateValue(+1)
                                } else if (offsetX < -threshold) {
                                    onUpdateValue(-1)
                                }
                                offsetX = 0f
                            },
                            onHorizontalDrag = { change, dragAmount ->
                                change.consume()
                                offsetX += dragAmount
                            }
                        )
                    }
                }
                .offset { IntOffset(offsetX.roundToInt(), 0) }
                .clickable(enabled = !isReorderMode) { onToggleExpand() }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                val rawProgress = if (item.targetValue > 0) {
                    item.currentValue.toFloat() / item.targetValue
                } else {
                    0f
                }
                val barProgress = rawProgress.coerceIn(0f, 1f)

                val animatedProgress by animateFloatAsState(
                    targetValue = barProgress,
                    label = "progressAnimation"
                )
                val progressColor by animateColorAsState(
                    targetValue = progressToColor(rawProgress),
                    label = "colorAnimation"
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        LinearProgressIndicator(
                            progress = { animatedProgress },
                            modifier = Modifier.fillMaxSize(),
                            color = progressColor,
                            trackColor = progressColor.copy(alpha = 0.2f),
                            gapSize = 0.dp,
                            strokeCap = StrokeCap.Butt,
                            drawStopIndicator = {}
                        )
                        Text(
                            text = "${item.currentValue} / ${item.targetValue}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.78f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }

                    ConfettiBurst(
                        triggerKey = confettiTrigger,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                if (isExpanded) {
                    Spacer(modifier = Modifier.height(8.dp))
                    if (item.description.isNotBlank()) {
                        Text(
                            text = item.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                    if (history.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Column {
                            Text(
                                text = "Previous iterations",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                history.forEach { progress ->
                                    val color = progressToColor(progress)
                                    Box(
                                        modifier = Modifier
                                            .height(8.dp)
                                            .weight(1f)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(color.copy(alpha = 0.2f))
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxHeight()
                                                .fillMaxWidth(progress.coerceIn(0f, 1f))
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(color)
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No history available yet",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.alpha(0.6f)
                        )
                    }
                }
            }
            if (!isReorderMode) {
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More Options"
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit") },
                            onClick = {
                                showMenu = false
                                onEditClick()
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = null
                                )
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Remove") },
                            onClick = {
                                showMenu = false
                                onDeleteClick()
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        )
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = item.currentValue >= item.targetValue && item.targetValue > 0,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut(),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = -4.dp, y = 4.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = "Achieved",
                tint = Color(0xFF4CAF50),
                modifier = Modifier.padding(2.dp)
            )
        }
    }
}
