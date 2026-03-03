package com.example.impermanentrectangles

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleOut
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.EaseOut
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.drawscope.rotate;
import kotlinx.coroutines.delay
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.IntOffset
import androidx.compose.foundation.layout.offset
import kotlin.math.roundToInt
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.impermanentrectangles.ui.theme.ImpermanentRectanglesTheme
import java.util.UUID

data class Item(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val currentValue: Int = 0,
    val targetValue: Int = 3
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ImpermanentRectanglesTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    val items = remember {
        mutableStateListOf(
            Item(title = "Item 1", description = "Description for item 1", targetValue = 5),
            Item(title = "Item 2", description = "", targetValue = 3),
            Item(title = "Item 3", description = "Description for item 3", targetValue = 10)
        )
    }

    var expandedItemId by remember { mutableStateOf<String?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var itemToEdit by remember { mutableStateOf<Item?>(null) }
    var itemToDelete by remember { mutableStateOf<Item?>(null) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Item")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(items, key = { it.id }) { item ->
                    ListItem(
                        item = item,
                        isExpanded = item.id == expandedItemId,
                        onToggleExpand = {
                            expandedItemId = if (expandedItemId == item.id) null else item.id
                        },
                        onEditClick = { itemToEdit = item },
                        onDeleteClick = { itemToDelete = item },
                        onUpdateValue = { delta ->
                            val index = items.indexOfFirst { it.id == item.id }
                            if (index != -1) {
                                val updatedValue = (items[index].currentValue + delta).coerceAtLeast(0)
                                items[index] = items[index].copy(currentValue = updatedValue)
                            }
                        }
                    )
                    HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)
                }
            }
        }

        if (showAddDialog) {
            AddItemDialog(
                onDismiss = { showAddDialog = false },
                onConfirm = { title, description, targetValue ->
                    items.add(Item(title = title, description = description, targetValue = targetValue))
                    showAddDialog = false
                }
            )
        }

        itemToEdit?.let { item ->
            AddItemDialog(
                initialTitle = item.title,
                initialDescription = item.description,
                initialTargetValue = item.targetValue,
                onDismiss = { itemToEdit = null },
                onConfirm = { title, description, targetValue ->
                    val index = items.indexOfFirst { it.id == item.id }
                    if (index != -1) {
                        items[index] = item.copy(title = title, description = description, targetValue = targetValue)
                    }
                    itemToEdit = null
                }
            )
        }

        itemToDelete?.let { item ->
            DeleteConfirmationDialog(
                item = item,
                onDismiss = { itemToDelete = null },
                onConfirm = {
                    items.remove(item)
                    itemToDelete = null
                }
            )
        }
    }
}

private fun progressToColor(progress: Float): Color {
    val p = progress.coerceIn(0f, 2f)
    val red = Color(0xFFF44336)
    val amber = Color(0xFFFFC107)
    val green = Color(0xFF4CAF50)
    val overComplete = Color(0xFF00BCD4)

    return when {
        p < 0.5f -> lerp(red, amber, p / 0.5f)                  // 0.0..0.5
        p < 1.0f -> lerp(amber, green, (p - 0.5f) / 0.5f)       // 0.5..1.0
        else -> lerp(green, overComplete, (p - 1.0f) / 1.0f)            // 1.0..2.0
    }
}

@Composable
fun ConfettiBurst(
    triggerKey: Int,
    modifier: Modifier = Modifier
) {
    data class Particle(
        val angleRad: Float,
        val speed: Float,
        val size: Float,
        val color: Color,
        val drift: Float,
        val spin: Float
    )

    val progress = remember { Animatable(0f) }
    var visible by remember { mutableStateOf(false) }

    val brightColors = listOf(
        Color(0xFFFF1744), // bright red
        Color(0xFFFFEA00), // bright yellow
        Color(0xFF00E676), // bright green
        Color(0xFF00E5FF), // bright cyan
        Color(0xFFD500F9), // bright purple
        Color(0xFFFF9100)  // bright orange
    )

    val particles = remember(triggerKey) {
        val random = kotlin.random.Random(System.currentTimeMillis())
        List(random.nextInt(6, 12)) {
            // launch mostly upward and left, to fly from the right end
            val angleDeg = random.nextInt(95, 145).toFloat()
            val angleRad = Math.toRadians(angleDeg.toDouble()).toFloat()
            Particle(
                angleRad = angleRad,
                speed = random.nextInt(400, 800).toFloat(),   // initial shoot speed
                size = random.nextInt(8, 16).toFloat(),       // random size
                color = brightColors.random(random),           // random bright color
                drift = random.nextFloat() * 36f - 18f,       // side drift
                spin = random.nextFloat() * 540f - 270f       // rotation variety
            )
        }
    }

    LaunchedEffect(triggerKey) {
        if (triggerKey > 0) {
            visible = true
            progress.snapTo(0f)
            progress.animateTo(1f, animationSpec = tween(durationMillis = 1500, easing = EaseOut))
            visible = false
        }
    }

    if (visible) {
        androidx.compose.foundation.Canvas(modifier = modifier) {
            val startX = size.width
            val startY = size.height * 0.5f
            val gravity = 1000f // low-ish gravity => slower drop after apex

            particles.forEachIndexed { i, p ->
                val t = progress.value * 1.8f // seconds mapped from animation progress

                // physics: y-axis positive downward in canvas
                val vx = kotlin.math.cos(p.angleRad) * p.speed
                val vy = -kotlin.math.sin(p.angleRad) * p.speed

                val x = startX + vx * t + p.drift * t * t
                val y = startY + vy * t + 0.5f * gravity * t * t

                // keep bright at launch, then fade mostly during descent
                val fadeStart = 0.40f
                val alpha = when {
                    progress.value < fadeStart -> 1f
                    else -> (1f - (progress.value - fadeStart) / (1f - fadeStart)).coerceIn(0f, 1f)
                }

                if (alpha > 0f) {
                    rotate(
                        degrees = p.spin * progress.value + i * 12f,
                        pivot = Offset(x, y)
                    ) {
                        drawRect(
                            color = p.color,
                            topLeft = Offset(x, y),
                            size = Size(p.size, p.size * (1.2f + (i % 3) * 0.25f)),
                            alpha = alpha
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ListItem(
    item: Item,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onUpdateValue: (Int) -> Unit
) {
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

    Box(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .pointerInput(Unit) {
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
                .offset { IntOffset(offsetX.roundToInt(), 0) }
                .clickable { onToggleExpand() }
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
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (item.description.isNotBlank()) item.description else "No description provided",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (item.description.isNotBlank()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline
                    )
                }
            }
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

@Composable
fun AddItemDialog(
    initialTitle: String = "",
    initialDescription: String = "",
    initialTargetValue: Int = 3,
    onDismiss: () -> Unit,
    onConfirm: (String, String, Int) -> Unit
) {
    var title by remember { mutableStateOf(initialTitle) }
    var description by remember { mutableStateOf(initialDescription) }
    var targetValue by remember { mutableStateOf(initialTargetValue.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialTitle.isEmpty()) "Add New Item" else "Edit Item") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = targetValue,
                    onValueChange = {
                        if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                            targetValue = it
                        }
                    },
                    label = { Text("Target Value") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        val target = targetValue.toIntOrNull() ?: 3
                        onConfirm(title, description.trim(), target)
                    }
                },
                enabled = title.isNotBlank()
            ) {
                Text(if (initialTitle.isEmpty()) "Add" else "Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun DeleteConfirmationDialog(item: Item, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Item") },
        text = { Text("Are you sure you want to delete '${item.title}'?") },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    ImpermanentRectanglesTheme {
        MainScreen()
    }
}