package com.example.impermanentrectangles.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.impermanentrectangles.Item
import com.example.impermanentrectangles.ItemList
import com.example.impermanentrectangles.data.repository.AppRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(private val repository: AppRepository) : ViewModel() {

    val allLists: StateFlow<List<ItemList>> = repository.getAllLists()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedListIndex = MutableStateFlow(0)
    val selectedListIndex: StateFlow<Int> = _selectedListIndex.asStateFlow()

    private val _isReorderMode = MutableStateFlow(false)
    val isReorderMode: StateFlow<Boolean> = _isReorderMode.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val currentList: StateFlow<ItemList?> = combine(allLists, _selectedListIndex) { lists, index ->
        lists.getOrNull(index)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val currentItems: StateFlow<List<Item>> = currentList
        .filterNotNull()
        .flatMapLatest { list -> repository.getItemsForList(list.id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val currentHistory: StateFlow<List<Map<String, Float>>> = currentList
        .filterNotNull()
        .flatMapLatest { list -> repository.getHistoryForList(list.id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectList(index: Int) {
        _selectedListIndex.value = index
    }

    fun toggleReorderMode() {
        _isReorderMode.value = !_isReorderMode.value
    }

    fun addList(name: String, description: String = "") {
        viewModelScope.launch {
            val newId = repository.addList(name, description)
            // Wait for allLists to be updated with the new ID
            allLists.first { lists -> lists.any { it.id == newId } }
            // Now find the index and select it
            val index = allLists.value.indexOfFirst { it.id == newId }
            if (index != -1) {
                _selectedListIndex.value = index
            }
        }
    }

    fun updateList(list: ItemList) {
        viewModelScope.launch {
            repository.updateList(list)
        }
    }

    fun deleteList(list: ItemList) {
        viewModelScope.launch {
            repository.deleteList(list)
            if (_selectedListIndex.value >= allLists.value.size - 1) {
                _selectedListIndex.value = (allLists.value.size - 2).coerceAtLeast(0)
            }
        }
    }

    fun addItem(listId: String, title: String, description: String, targetValue: Int) {
        viewModelScope.launch {
            val maxPosition = currentItems.value.maxOfOrNull { it.position } ?: -1
            repository.addItem(listId, title, description, targetValue, maxPosition + 1)
        }
    }

    fun updateItem(listId: String, item: Item) {
        viewModelScope.launch {
            repository.updateItem(listId, item)
        }
    }

    fun deleteItem(itemId: String) {
        viewModelScope.launch {
            repository.deleteItem(itemId)
        }
    }

    fun moveItem(listId: String, fromIndex: Int, toIndex: Int) {
        val items = currentItems.value.toMutableList()
        if (fromIndex !in items.indices || toIndex !in items.indices) return

        val item = items.removeAt(fromIndex)
        items.add(toIndex, item)

        // Update positions
        val updatedItems = items.mapIndexed { index, it ->
            it.copy(position = index)
        }

        viewModelScope.launch {
            repository.updateItems(listId, updatedItems)
        }
    }

    fun startNewIteration(listId: String, items: List<Item>) {
        viewModelScope.launch {
            val historyMap = items.associate { it.id to (it.currentValue.toFloat() / it.targetValue.coerceAtLeast(1)) }
            repository.addHistoryEntry(listId, historyMap)

            // Reset items
            items.forEach { item ->
                repository.updateItem(listId, item.copy(currentValue = 0))
            }

            // Update iteration start time
            val list = allLists.value.find { it.id == listId }
            list?.let {
                repository.updateList(it.copy(iterationStartTime = System.currentTimeMillis()))
            }
        }
    }
}

class MainViewModelFactory(private val repository: AppRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
