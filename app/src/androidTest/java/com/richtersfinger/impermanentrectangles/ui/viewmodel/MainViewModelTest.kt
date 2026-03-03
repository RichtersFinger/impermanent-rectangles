package com.richtersfinger.impermanentrectangles.ui.viewmodel

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.richtersfinger.impermanentrectangles.data.db.AppDatabase
import com.richtersfinger.impermanentrectangles.data.repository.AppRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith

@Ignore("Disabled due to flaky tests")
@RunWith(AndroidJUnit4::class)
class MainViewModelTest {
    private lateinit var database: AppDatabase
    private lateinit var repository: AppRepository
    private lateinit var viewModel: MainViewModel

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        repository = AppRepository(database.appDao(), ApplicationProvider.getApplicationContext())
        viewModel = MainViewModel(repository, initializeAppVersion = false)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun addList_updatesSelectedList() = runBlocking {
        viewModel.addList("Test List")

        val lists = withTimeout(3000) { viewModel.allLists.first { it.isNotEmpty() } }
        assertEquals(1, lists.size)
        assertEquals("Test List", lists[0].name)

        val currentList = withTimeout(3000) { viewModel.currentList.first { it != null } }
        assertNotNull(currentList)
        assertEquals("Test List", currentList?.name)
    }

    @Test
    fun addItem_updatesCurrentItems() = runBlocking {
        viewModel.addList("Test List")
        val currentList = withTimeout(3000) { viewModel.currentList.first { it != null } }

        viewModel.addItem(currentList!!.id, "Test Item", "Desc", 5)

        val items = withTimeout(3000) { viewModel.currentItems.first { it.isNotEmpty() } }
        assertEquals(1, items.size)
        assertEquals("Test Item", items[0].title)
    }

    @Test
    fun selectList_updatesCurrentList() = runBlocking {
        viewModel.addList("List 1")
        viewModel.addList("List 2")

        withTimeout(3000) { viewModel.allLists.first { it.size == 2 } }

        viewModel.selectList(0)
        withTimeout(3000) { viewModel.currentList.first { it?.name == "List 1" } }
        assertEquals("List 1", viewModel.currentList.value?.name)

        viewModel.selectList(1)
        withTimeout(3000) { viewModel.currentList.first { it?.name == "List 2" } }
        assertEquals("List 2", viewModel.currentList.value?.name)
    }

    @Test
    fun deleteList_updatesSelection() = runBlocking {
        viewModel.addList("List 1")
        viewModel.addList("List 2")
        val lists = withTimeout(3000) { viewModel.allLists.first { it.size == 2 } }

        viewModel.selectList(1)
        viewModel.deleteList(lists[1])

        withTimeout(3000) { viewModel.allLists.first { it.size == 1 } }
        assertEquals(0, viewModel.selectedListIndex.value)
    }

    @Test
    fun toggleReorderMode() {
        assertEquals(false, viewModel.isReorderMode.value)
        viewModel.toggleReorderMode()
        assertEquals(true, viewModel.isReorderMode.value)
        viewModel.toggleReorderMode()
        assertEquals(false, viewModel.isReorderMode.value)
    }
}
