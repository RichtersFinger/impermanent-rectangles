package com.richtersfinger.impermanentrectangles.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import androidx.room.Delete
import androidx.room.RawQuery
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
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

    @Query("SELECT * FROM metadata WHERE `key` = :key")
    suspend fun getMetadata(key: String): MetadataEntity?

    @Insert
    suspend fun insertMetadata(metadata: MetadataEntity)

    @Update
    suspend fun updateMetadata(metadata: MetadataEntity)

    @Transaction
    suspend fun setMetadata(key: String, value: String) {
        val existing = getMetadata(key)
        if (existing == null) {
            insertMetadata(MetadataEntity(key, value))
        } else {
            updateMetadata(existing.copy(value = value))
        }
    }

    @Query("SELECT * FROM items")
    suspend fun getAllItemsRaw(): List<ItemEntity>

    @Query("SELECT * FROM history_entries")
    suspend fun getAllHistoryEntriesRaw(): List<HistoryEntryEntity>

    @Query("SELECT * FROM history_values")
    suspend fun getAllHistoryValuesRaw(): List<HistoryValueEntity>

    @Query("SELECT * FROM item_lists")
    suspend fun getAllListsRaw(): List<ItemListEntity>

    @Transaction
    suspend fun clearAllData() {
        deleteAllHistoryValues()
        deleteAllHistoryEntries()
        deleteAllItems()
        deleteAllLists()
    }

    @Query("DELETE FROM history_values")
    suspend fun deleteAllHistoryValues()

    @Query("DELETE FROM history_entries")
    suspend fun deleteAllHistoryEntries()

    @Query("DELETE FROM items")
    suspend fun deleteAllItems()

    @Query("DELETE FROM item_lists")
    suspend fun deleteAllLists()

    @RawQuery
    suspend fun checkpoint(supportSQLiteQuery: SupportSQLiteQuery = SimpleSQLiteQuery("PRAGMA wal_checkpoint(FULL)")): Int

    @Insert
    suspend fun insertLists(lists: List<ItemListEntity>)
}
