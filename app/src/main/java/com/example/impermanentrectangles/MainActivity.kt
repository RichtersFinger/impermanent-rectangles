package com.example.impermanentrectangles

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.runtime.mutableIntStateOf
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
            val progress = if (item.targetValue > 0) {
                (item.currentValue.toFloat() / item.targetValue).coerceIn(0f, 1f)
            } else {
                0f
            }
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = "Progress: ${item.currentValue} / ${item.targetValue}",
                style = MaterialTheme.typography.bodySmall
            )
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