package com.richtersfinger.impermanentrectangles

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.material.icons.filled.Done
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
import com.richtersfinger.impermanentrectangles.ui.components.ListItem
import com.richtersfinger.impermanentrectangles.ui.components.NewIterationConfirmationDialog
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

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    ImpermanentRectanglesTheme {
        // Mock data or use a different preview composable that doesn't rely on ViewModel with DB
        Text("Preview is disabled because it requires a Database instance.")
    }
}