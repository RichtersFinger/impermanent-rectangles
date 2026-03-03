package com.richtersfinger.impermanentrectangles.data.repository

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.richtersfinger.impermanentrectangles.data.db.AppDao
import com.richtersfinger.impermanentrectangles.data.db.AppDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppRepositoryTest {
    private lateinit var database: AppDatabase
    private lateinit var dao: AppDao
    private lateinit var repository: AppRepository

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = database.appDao()
        repository = AppRepository(dao, ApplicationProvider.getApplicationContext())
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun addAndGetLists() = runBlocking {
        val listId = repository.addList("My List", "My Description")
        val allLists = repository.getAllLists().first()

        assertEquals(1, allLists.size)
        assertEquals("My List", allLists[0].name)
        assertEquals("My Description", allLists[0].description)
        assertEquals(listId, allLists[0].id)
    }

    @Test
    fun addAndGetItems() = runBlocking {
        val listId = repository.addList("My List")
        repository.addItem(listId, "Item 1", "Desc 1", 10, 0)

        val items = repository.getItemsForList(listId).first()
        assertEquals(1, items.size)
        assertEquals("Item 1", items[0].title)
        assertEquals(10, items[0].targetValue)
    }

    @Test
    fun updateItem() = runBlocking {
        val listId = repository.addList("My List")
        repository.addItem(listId, "Item 1", "Desc 1", 10, 0)
        val items = repository.getItemsForList(listId).first()
        val item = items[0]

        val updatedItem = item.copy(currentValue = 5)
        repository.updateItem(listId, updatedItem)

        val newItems = repository.getItemsForList(listId).first()
        assertEquals(5, newItems[0].currentValue)
    }

    @Test
    fun deleteItem() = runBlocking {
        val listId = repository.addList("My List")
        repository.addItem(listId, "Item 1", "Desc 1", 10, 0)
        val items = repository.getItemsForList(listId).first()

        repository.deleteItem(items[0].id)

        val newItems = repository.getItemsForList(listId).first()
        assertTrue(newItems.isEmpty())
    }

    @Test
    fun historyOperations() = runBlocking {
        val listId = repository.addList("My List")
        repository.addItem(listId, "Item 1", "Desc 1", 10, 0)
        val items = repository.getItemsForList(listId).first()

        val historyMap = mapOf(items[0].id to 0.75f)
        repository.addHistoryEntry(listId, historyMap)

        val history = repository.getHistoryForList(listId).first()
        assertEquals(1, history.size)
        assertEquals(0.75f, history[0][items[0].id])
    }
}
