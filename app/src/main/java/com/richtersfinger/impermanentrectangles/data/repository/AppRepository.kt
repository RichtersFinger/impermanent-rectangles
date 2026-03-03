package com.richtersfinger.impermanentrectangles.data.repository

import android.content.Context
import android.net.Uri
import com.richtersfinger.impermanentrectangles.BuildConfig
import com.richtersfinger.impermanentrectangles.Item
import com.richtersfinger.impermanentrectangles.ItemList
import com.richtersfinger.impermanentrectangles.data.db.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class AppRepository(private val appDao: AppDao, private val context: Context) {

    suspend fun ensureAppVersion() {
        appDao.setMetadata("versionCode", BuildConfig.VERSION_CODE.toString())
        appDao.setMetadata("versionName", BuildConfig.VERSION_NAME)
    }

    suspend fun exportDatabase(uri: Uri) = withContext(Dispatchers.IO) {
        val dbFile = context.getDatabasePath(AppDatabase.DATABASE_NAME)
        // Ensure WAL is checkpointed
        appDao.checkpoint()
        
        context.contentResolver.openOutputStream(uri)?.use { output ->
            FileInputStream(dbFile).use { input ->
                input.copyTo(output)
            }
        }
    }

    suspend fun importDatabase(uri: Uri) = withContext(Dispatchers.IO) {
        android.util.Log.d("AppRepository", "Starting import from $uri")
        // Close database before replacement to avoid issues
        AppDatabase.closeDatabase()

        val dbFile = context.getDatabasePath(AppDatabase.DATABASE_NAME)
        val shmFile = File(dbFile.path + "-shm")
        val walFile = File(dbFile.path + "-wal")

        android.util.Log.d("AppRepository", "Replacing database file: ${dbFile.path}")
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(dbFile).use { output ->
                input.copyTo(output)
            }
        }
        
        // Delete WAL/SHM files to ensure consistency
        if (shmFile.exists()) {
            android.util.Log.d("AppRepository", "Deleting SHM file")
            shmFile.delete()
        }
        if (walFile.exists()) {
            android.util.Log.d("AppRepository", "Deleting WAL file")
            walFile.delete()
        }
        android.util.Log.d("AppRepository", "Import finished")
    }

    fun getAllLists(): Flow<List<ItemList>> {
        return appDao.getAllLists().map { listEntities ->
            listEntities.map { entity ->
                ItemList(
                    id = entity.id,
                    name = entity.name,
                    description = entity.description,
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

    suspend fun addList(name: String, description: String = ""): String {
        val list = ItemListEntity(name = name, description = description)
        appDao.insertList(list)
        return list.id
    }

    suspend fun updateList(list: ItemList) {
        appDao.updateList(
            ItemListEntity(
                id = list.id,
                name = list.name,
                description = list.description,
                iterationStartTime = list.iterationStartTime
            )
        )
    }

    suspend fun deleteList(list: ItemList) {
        appDao.deleteList(
            ItemListEntity(
                id = list.id,
                name = list.name,
                description = list.description,
                iterationStartTime = list.iterationStartTime
            )
        )
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
