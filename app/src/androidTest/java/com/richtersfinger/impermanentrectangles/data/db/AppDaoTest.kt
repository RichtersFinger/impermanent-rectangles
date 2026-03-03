package com.richtersfinger.impermanentrectangles.data.db

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppDaoTest {
    private lateinit var database: AppDatabase
    private lateinit var dao: AppDao

    @Before
    fun createDb() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = database.appDao()
    }

    @After
    fun closeDb() {
        database.close()
    }

    @Test
    fun insertAndGetList() = runBlocking {
        val list = ItemListEntity(name = "Test List", description = "Test Description")
        dao.insertList(list)
        val allLists = dao.getAllLists().first()
        assertEquals(1, allLists.size)
        assertEquals(list.name, allLists[0].name)
        assertEquals(list.description, allLists[0].description)
    }

    @Test
    fun insertAndGetItems() = runBlocking {
        val list = ItemListEntity(name = "Test List")
        dao.insertList(list)

        val item1 = ItemEntity(
            listId = list.id,
            title = "Item 1",
            description = "Desc 1",
            currentValue = 0,
            targetValue = 5,
            position = 0
        )
        val item2 = ItemEntity(
            listId = list.id,
            title = "Item 2",
            description = "Desc 2",
            currentValue = 1,
            targetValue = 3,
            position = 1
        )
        dao.insertItems(listOf(item1, item2))

        val items = dao.getItemsForList(list.id).first()
        assertEquals(2, items.size)
        assertEquals("Item 1", items[0].title)
        assertEquals("Item 2", items[1].title)
    }

    @Test
    fun updateItem() = runBlocking {
        val list = ItemListEntity(name = "Test List")
        dao.insertList(list)
        val item = ItemEntity(
            listId = list.id,
            title = "Item",
            description = "Desc",
            currentValue = 0,
            targetValue = 5
        )
        dao.insertItem(item)

        val updatedItem = item.copy(currentValue = 3)
        dao.updateItem(updatedItem)

        val items = dao.getItemsForList(list.id).first()
        assertEquals(3, items[0].currentValue)
    }

    @Test
    fun deleteItem() = runBlocking {
        val list = ItemListEntity(name = "Test List")
        dao.insertList(list)
        val item = ItemEntity(
            listId = list.id,
            title = "Item",
            description = "Desc",
            currentValue = 0,
            targetValue = 5
        )
        dao.insertItem(item)

        dao.deleteItem(item)
        val items = dao.getItemsForList(list.id).first()
        assertEquals(0, items.size)
    }

    @Test
    fun historyOperations() = runBlocking {
        val list = ItemListEntity(name = "Test List")
        dao.insertList(list)
        val item = ItemEntity(
            listId = list.id,
            title = "Item",
            description = "Desc",
            currentValue = 0,
            targetValue = 5
        )
        dao.insertItem(item)

        val historyMap = mapOf(item.id to 0.5f)
        dao.addHistoryEntry(list.id, historyMap)

        val history = dao.getHistoryWithValuesForList(list.id).first()
        assertEquals(1, history.size)
        assertEquals(1, history[0].values.size)
        assertEquals(0.5f, history[0].values[0].value)
        assertEquals(item.id, history[0].values[0].itemId)
    }

    @Test
    fun metadataOperations() = runBlocking {
        dao.setMetadata("testKey", "testValue")
        val metadata = dao.getMetadata("testKey")
        assertNotNull(metadata)
        assertEquals("testValue", metadata?.value)

        dao.setMetadata("testKey", "newValue")
        val updatedMetadata = dao.getMetadata("testKey")
        assertEquals("newValue", updatedMetadata?.value)
    }

    @Test
    fun clearAllData() = runBlocking {
        val list = ItemListEntity(name = "Test List")
        dao.insertList(list)
        val item = ItemEntity(
            listId = list.id,
            title = "Item",
            description = "Desc",
            currentValue = 0,
            targetValue = 5
        )
        dao.insertItem(item)
        dao.addHistoryEntry(list.id, mapOf(item.id to 1.0f))

        dao.clearAllData()

        assertEquals(0, dao.getAllListsRaw().size)
        assertEquals(0, dao.getAllItemsRaw().size)
        assertEquals(0, dao.getAllHistoryEntriesRaw().size)
        assertEquals(0, dao.getAllHistoryValuesRaw().size)
    }
}
