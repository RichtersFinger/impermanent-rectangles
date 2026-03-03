package com.example.impermanentrectangles.data.repository

import androidx.compose.runtime.mutableStateListOf
import com.example.impermanentrectangles.Item
import com.example.impermanentrectangles.ItemList
import com.example.impermanentrectangles.data.db.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

class AppRepository(private val appDao: AppDao) {

    fun getAllLists(): Flow<List<ItemList>> {
        return appDao.getAllLists().map { listEntities ->
            listEntities.map { entity ->
                ItemList(
                    id = entity.id,
                    name = entity.name,
                    iterationStartTime = entity.iterationStartTime
                )
            }
        }
    }

    fun getItemsForList(listId: String): Flow<List<Item>> {
        return appDao.getItemsForList(listId).map { itemEntities ->
            itemEntities.map { entity ->
                Item(
                    id = entity.id,
                    title = entity.title,
                    description = entity.description,
                    currentValue = entity.currentValue,
                    targetValue = entity.targetValue,
                    position = entity.position
                )
            }
        }
    }

    fun getHistoryForList(listId: String): Flow<List<Map<String, Float>>> {
        return appDao.getHistoryWithValuesForList(listId).map { entriesWithValues ->
            entriesWithValues.map { entryWithValues ->
                entryWithValues.values.associate { it.itemId to it.value }
            }
        }
    }

    suspend fun addList(name: String) {
        appDao.insertList(ItemListEntity(name = name))
    }

    suspend fun updateList(list: ItemList) {
        appDao.updateList(ItemListEntity(id = list.id, name = list.name, iterationStartTime = list.iterationStartTime))
    }

    suspend fun deleteList(list: ItemList) {
        appDao.deleteList(ItemListEntity(id = list.id, name = list.name, iterationStartTime = list.iterationStartTime))
    }

    suspend fun addItem(listId: String, title: String, description: String, targetValue: Int, position: Int) {
        appDao.insertItem(
            ItemEntity(
                listId = listId,
                title = title,
                description = description,
                currentValue = 0,
                targetValue = targetValue,
                position = position
            )
        )
    }

    suspend fun updateItem(listId: String, item: Item) {
        appDao.updateItem(
            ItemEntity(
                id = item.id,
                listId = listId,
                title = item.title,
                description = item.description,
                currentValue = item.currentValue,
                targetValue = item.targetValue,
                position = item.position
            )
        )
    }

    suspend fun updateItems(listId: String, items: List<Item>) {
        appDao.updateItems(
            items.map { item ->
                ItemEntity(
                    id = item.id,
                    listId = listId,
                    title = item.title,
                    description = item.description,
                    currentValue = item.currentValue,
                    targetValue = item.targetValue,
                    position = item.position
                )
            }
        )
    }

    suspend fun deleteItem(itemId: String) {
        appDao.deleteItemById(itemId)
    }

    suspend fun addHistoryEntry(listId: String, historyMap: Map<String, Float>) {
        appDao.addHistoryEntry(listId, historyMap)
    }
}
