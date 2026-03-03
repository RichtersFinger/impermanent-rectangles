package com.example.impermanentrectangles

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
    val description: String
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
            Item(title = "Item 1", description = "Description for item 1"),
            Item(title = "Item 2", description = "Description for item 2"),
            Item(title = "Item 3", description = "Description for item 3")
        )
    }

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
                        onEditClick = { itemToEdit = item },
                        onDeleteClick = { itemToDelete = item }
                    )
                    HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)
                }
            }
        }

        if (showAddDialog) {
            AddItemDialog(
                onDismiss = { showAddDialog = false },
                onConfirm = { title, description ->
                    items.add(Item(title = title, description = description))
                    showAddDialog = false
                }
            )
        }

        itemToEdit?.let { item ->
            AddItemDialog(
                initialTitle = item.title,
                initialDescription = item.description,
                onDismiss = { itemToEdit = null },
                onConfirm = { title, description ->
                    val index = items.indexOfFirst { it.id == item.id }
                    if (index != -1) {
                        items[index] = item.copy(title = title, description = description)
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
fun ListItem(item: Item, onEditClick: () -> Unit, onDeleteClick: () -> Unit) {
    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
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
            Text(
                text = item.description,
                style = MaterialTheme.typography.bodyMedium
            )
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
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var title by remember { mutableStateOf(initialTitle) }
    var description by remember { mutableStateOf(initialDescription) }

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
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && description.isNotBlank()) {
                        onConfirm(title, description)
                    }
                },
                enabled = title.isNotBlank() && description.isNotBlank()
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