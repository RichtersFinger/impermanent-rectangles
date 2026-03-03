package com.example.impermanentrectangles.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import androidx.room.Delete
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    @Query("SELECT * FROM item_lists")
    fun getAllLists(): Flow<List<ItemListEntity>>

    @Query("SELECT * FROM items WHERE listId = :listId ORDER BY position ASC")
    fun getItemsForList(listId: String): Flow<List<ItemEntity>>

    @Insert
    suspend fun insertItems(items: List<ItemEntity>)

    @Query("SELECT * FROM history_entries WHERE listId = :listId ORDER BY timestamp ASC")
    fun getHistoryEntriesForList(listId: String): Flow<List<HistoryEntryEntity>>

    @Transaction
    @Query("SELECT * FROM history_entries WHERE listId = :listId ORDER BY timestamp ASC")
    fun getHistoryWithValuesForList(listId: String): Flow<List<HistoryEntryWithValues>>

    @Query("SELECT * FROM history_values WHERE historyEntryId = :historyEntryId")
    suspend fun getHistoryValuesForEntry(historyEntryId: Long): List<HistoryValueEntity>

    @Insert
    suspend fun insertList(list: ItemListEntity)

    @Update
    suspend fun updateList(list: ItemListEntity)

    @Delete
    suspend fun deleteList(list: ItemListEntity)

    @Insert
    suspend fun insertItem(item: ItemEntity)

    @Update
    suspend fun updateItem(item: ItemEntity)

    @Update
    suspend fun updateItems(items: List<ItemEntity>)

    @Delete
    suspend fun deleteItem(item: ItemEntity)

    @Query("DELETE FROM items WHERE id = :itemId")
    suspend fun deleteItemById(itemId: String)

    @Insert
    suspend fun insertHistoryEntry(entry: HistoryEntryEntity): Long

    @Insert
    suspend fun insertHistoryValues(values: List<HistoryValueEntity>)

    @Transaction
    suspend fun addHistoryEntry(listId: String, historyMap: Map<String, Float>) {
        val entryId = insertHistoryEntry(HistoryEntryEntity(listId = listId))
        val values = historyMap.map { (itemId, value) ->
            HistoryValueEntity(historyEntryId = entryId, itemId = itemId, value = value)
        }
        insertHistoryValues(values)
    }
}
