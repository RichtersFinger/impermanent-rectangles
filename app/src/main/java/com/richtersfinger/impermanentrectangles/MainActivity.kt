package com.richtersfinger.impermanentrectangles

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
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.animation.core.EaseOut
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.zIndex
import androidx.compose.ui.graphics.drawscope.rotate;
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
import androidx.compose.ui.draw.alpha
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
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.unit.IntOffset
import androidx.compose.foundation.layout.offset
import kotlin.math.roundToInt
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material.icons.filled.Done
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.richtersfinger.impermanentrectangles.ui.theme.ImpermanentRectanglesTheme
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.richtersfinger.impermanentrectangles.data.db.AppDatabase
import com.richtersfinger.impermanentrectangles.data.repository.AppRepository
import com.richtersfinger.impermanentrectangles.ui.viewmodel.MainViewModel
import com.richtersfinger.impermanentrectangles.ui.viewmodel.MainViewModelFactory
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.layout.size
import androidx.compose.ui.res.stringResource
import com.richtersfinger.impermanentrectangles.ui.components.AboutDialog
import com.richtersfinger.impermanentrectangles.ui.components.AddItemDialog
import com.richtersfinger.impermanentrectangles.ui.components.AddListDialog
import com.richtersfinger.impermanentrectangles.ui.components.DeleteConfirmationDialog
import com.richtersfinger.impermanentrectangles.ui.components.DeleteListConfirmationDialog
import com.richtersfinger.impermanentrectangles.ui.components.ListInfoDialog
import com.richtersfinger.impermanentrectangles.ui.components.NewIterationConfirmationDialog
import com.richtersfinger.impermanentrectangles.ui.theme.progressToColor
import java.util.UUID

data class Item(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val currentValue: Int = 0,
    val targetValue: Int = 3,
    val position: Int = 0
)

data class ItemList(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String = "",
    val items: List<Item> = emptyList(),
    val iterationStartTime: Long = System.currentTimeMillis(),
    val history: List<Map<String, Float>> = emptyList()
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel = viewModel(
        factory = MainViewModelFactory(
            AppRepository(
                AppDatabase.getDatabase(
                    androidx.compose.ui.platform.LocalContext.current
                ).appDao()
            )
        )
    )
) {
    val lists by viewModel.allLists.collectAsStateWithLifecycle()
    val currentList by viewModel.currentList.collectAsStateWithLifecycle()
    val items by viewModel.currentItems.collectAsStateWithLifecycle()
    val currentHistory by viewModel.currentHistory.collectAsStateWithLifecycle()
    val isReorderMode by viewModel.isReorderMode.collectAsStateWithLifecycle()

    var expandedItemId by remember { mutableStateOf<String?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var itemToEdit by remember { mutableStateOf<Item?>(null) }
    var itemToDelete by remember { mutableStateOf<Item?>(null) }

    var showAddListDialog by remember { mutableStateOf(false) }
    var showListInfoDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var listToEdit by remember { mutableStateOf<ItemList?>(null) }
    var listToDelete by remember { mutableStateOf<ItemList?>(null) }
    var listForNewIteration by remember { mutableStateOf<ItemList?>(null) }

    var showListSelectionMenu by remember { mutableStateOf(false) }
    var showContextMenu by remember { mutableStateOf(false) }

    // Reorder state
    var draggingItemIndex by remember { mutableStateOf<Int?>(null) }
    var dragOffset by remember { mutableFloatStateOf(0f) }
    val lazyListState = rememberLazyListState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = { showAboutDialog = true }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_logo),
                            contentDescription = "About",
                            modifier = Modifier.size(32.dp),
                            tint = Color.Unspecified
                        )
                    }
                },
                title = {
                    if (lists.isNotEmpty()) {
                        Box {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable { showListSelectionMenu = true }
                            ) {
                                Text(currentList?.name ?: "No List Selected")
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Select List")
                            }
                            DropdownMenu(
                                expanded = showListSelectionMenu,
                                onDismissRequest = { showListSelectionMenu = false }
                            ) {
                                lists.forEachIndexed { index, itemList ->
                                    DropdownMenuItem(
                                        text = { Text(itemList.name) },
                                        onClick = {
                                            viewModel.selectList(index)
                                            showListSelectionMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    } else {
                        Text(stringResource(id = R.string.app_name))
                    }
                },
                actions = {
                    if (currentList != null) {
                        IconButton(onClick = { viewModel.toggleReorderMode() }) {
                            Icon(
                                if (isReorderMode) Icons.Default.Done else Icons.Default.List,
                                contentDescription = if (isReorderMode) "Exit Reorder Mode" else "Enter Reorder Mode"
                            )
                        }
                    }
                    Box {
                        IconButton(onClick = { showContextMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "List Options")
                        }
                        DropdownMenu(
                            expanded = showContextMenu,
                            onDismissRequest = { showContextMenu = false }
                        ) {
                            currentList?.let {
                                DropdownMenuItem(
                                    text = { Text("Show info") },
                                    onClick = {
                                        showContextMenu = false
                                        showListInfoDialog = true
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("New iteration") },
                                    onClick = {
                                        showContextMenu = false
                                        listForNewIteration = it
                                    }
                                )
                            }
                            DropdownMenuItem(
                                text = { Text("Add a new list") },
                                onClick = {
                                    showContextMenu = false
                                    showAddListDialog = true
                                }
                            )
                            currentList?.let {
                                DropdownMenuItem(
                                    text = { Text("Edit current list") },
                                    onClick = {
                                        showContextMenu = false
                                        listToEdit = it
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Delete current list") },
                                    onClick = {
                                        showContextMenu = false
                                        listToDelete = it
                                    }
                                )
                            }
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (currentList != null && !isReorderMode) {
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    modifier = Modifier.alpha(0.75f),
                    containerColor = Color.LightGray,
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Item")
                }
            }
        },
        floatingActionButtonPosition = androidx.compose.material3.FabPosition.Start
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            if (lists.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("No lists found", style = MaterialTheme.typography.headlineSmall)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { showAddListDialog = true }) {
                            Text("Create your first list")
                        }
                    }
                }
            } else if (items.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("This list is empty", style = MaterialTheme.typography.headlineSmall)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { showAddDialog = true }) {
                            Text("Add an item")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    state = lazyListState
                ) {
                    items(items, key = { it.id }) { item ->
                        val index = items.indexOfFirst { it.id == item.id }
                        Box(modifier = Modifier.animateItem()) {
                            ListItem(
                                item = item,
                                isExpanded = item.id == expandedItemId,
                                history = currentHistory.takeLast(5).map { it[item.id] ?: 0f },
                                isDragging = index == draggingItemIndex,
                                isReorderMode = isReorderMode,
                                onDragStart = {
                                    draggingItemIndex = index
                                    dragOffset = 0f
                                },
                                onDrag = { delta ->
                                    dragOffset += delta
                                    val currentDraggingIndex = draggingItemIndex ?: return@ListItem

                                    val itemLayoutInfo =
                                        lazyListState.layoutInfo.visibleItemsInfo.firstOrNull { it.key == item.id }
                                    val itemHeight = itemLayoutInfo?.size ?: 100
                                    val threshold = itemHeight / 2

                                    if (dragOffset > threshold && currentDraggingIndex < items.size - 1) {
                                        currentList?.let { list ->
                                            viewModel.moveItem(list.id, currentDraggingIndex, currentDraggingIndex + 1)
                                            draggingItemIndex = currentDraggingIndex + 1
                                            dragOffset -= itemHeight
                                        }
                                    } else if (dragOffset < -threshold && currentDraggingIndex > 0) {
                                        currentList?.let { list ->
                                            viewModel.moveItem(list.id, currentDraggingIndex, currentDraggingIndex - 1)
                                            draggingItemIndex = currentDraggingIndex - 1
                                            dragOffset += itemHeight
                                        }
                                    }
                                },
                                onDragEnd = {
                                    draggingItemIndex = null
                                    dragOffset = 0f
                                },
                                onToggleExpand = {
                                    expandedItemId = if (expandedItemId == item.id) null else item.id
                                },
                                onEditClick = { itemToEdit = item },
                                onDeleteClick = { itemToDelete = item },
                                onUpdateValue = { delta ->
                                    currentList?.let { list ->
                                        val updatedValue = (item.currentValue + delta).coerceAtLeast(0)
                                        viewModel.updateItem(list.id, item.copy(currentValue = updatedValue))
                                    }
                                }
                            )
                        }
                        HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)
                    }
                }
            }
        }

        if (showAboutDialog) {
            val context = androidx.compose.ui.platform.LocalContext.current
            val packageInfo = try {
                context.packageManager.getPackageInfo(context.packageName, 0)
            } catch (e: Exception) {
                null
            }
            val versionName = packageInfo?.versionName ?: "Unknown"
            val versionCode = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                packageInfo?.longVersionCode?.toInt() ?: 0
            } else {
                @Suppress("DEPRECATION")
                packageInfo?.versionCode ?: 0
            }
            AboutDialog(
                onDismiss = { showAboutDialog = false },
                versionName = versionName,
                versionCode = versionCode
            )
        }

        if (showAddDialog) {
            AddItemDialog(
                onDismiss = { showAddDialog = false },
                onConfirm = { title, description, targetValue ->
                    currentList?.let { list ->
                        viewModel.addItem(list.id, title, description, targetValue)
                    }
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
                    currentList?.let { list ->
                        viewModel.updateItem(
                            list.id,
                            item.copy(title = title, description = description, targetValue = targetValue)
                        )
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
                    viewModel.deleteItem(item.id)
                    itemToDelete = null
                }
            )
        }

        if (showAddListDialog) {
            AddListDialog(
                onDismiss = { showAddListDialog = false },
                onConfirm = { name, description ->
                    viewModel.addList(name, description)
                    showAddListDialog = false
                }
            )
        }

        listToEdit?.let { list ->
            AddListDialog(
                initialName = list.name,
                initialDescription = list.description,
                onDismiss = { listToEdit = null },
                onConfirm = { newName, newDescription ->
                    viewModel.updateList(list.copy(name = newName, description = newDescription))
                    listToEdit = null
                }
            )
        }

        if (showListInfoDialog) {
            currentList?.let { list ->
                ListInfoDialog(
                    itemList = list,
                    totalIterations = currentHistory.size,
                    onDismiss = { showListInfoDialog = false }
                )
            }
        }

        listToDelete?.let { list ->
            DeleteListConfirmationDialog(
                listName = list.name,
                onDismiss = { listToDelete = null },
                onConfirm = {
                    viewModel.deleteList(list)
                    listToDelete = null
                }
            )
        }

        listForNewIteration?.let { list ->
            NewIterationConfirmationDialog(
                listName = list.name,
                onDismiss = { listForNewIteration = null },
                onConfirm = {
                    viewModel.startNewIteration(list.id, items)
                    listForNewIteration = null
                }
            )
        }
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

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    ImpermanentRectanglesTheme {
        // Mock data or use a different preview composable that doesn't rely on ViewModel with DB
        Text("Preview is disabled because it requires a Database instance.")
    }
}