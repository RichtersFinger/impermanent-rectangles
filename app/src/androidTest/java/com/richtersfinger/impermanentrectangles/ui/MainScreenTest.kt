package com.richtersfinger.impermanentrectangles.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.richtersfinger.impermanentrectangles.MainScreen
import com.richtersfinger.impermanentrectangles.data.db.AppDatabase
import com.richtersfinger.impermanentrectangles.data.repository.AppRepository
import com.richtersfinger.impermanentrectangles.ui.theme.ImpermanentRectanglesTheme
import com.richtersfinger.impermanentrectangles.ui.viewmodel.MainViewModel
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private fun createViewModel(): MainViewModel {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val repository = AppRepository(database.appDao(), context)
        return MainViewModel(repository)
    }

    @Test
    fun mainScreen_initialState_showsAppName() {
        val viewModel = createViewModel()
        composeTestRule.setContent {
            ImpermanentRectanglesTheme {
                MainScreen(viewModel = viewModel)
            }
        }

        // Initially no lists, should show app name in top bar
        composeTestRule.onNodeWithText("Impermanent Rectangles").assertIsDisplayed()
        // Should show "Create your first list" button when empty
        composeTestRule.onNodeWithText("Create your first list").assertIsDisplayed()
    }

    @Test
    fun mainScreen_addList_showsListTitle() {
        val viewModel = createViewModel()
        composeTestRule.setContent {
            ImpermanentRectanglesTheme {
                MainScreen(viewModel = viewModel)
            }
        }

        // Wait for the list creation button to appear
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodesWithText("Create your first list").fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeTestRule.onNodeWithText("Create your first list").performClick()

        // In AddListDialog, the label is "List Name"
        composeTestRule.onNodeWithText("List Name").performTextInput("My New List")
        composeTestRule.onNodeWithText("Add").performClick()

        // Wait for the new list to be displayed in the top bar
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodesWithText("My New List").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("My New List").assertIsDisplayed()
    }

    @Test
    fun mainScreen_addItem_showsItem() {
        val viewModel = createViewModel()
        composeTestRule.setContent {
            ImpermanentRectanglesTheme {
                MainScreen(viewModel = viewModel)
            }
        }

        // Add a list first
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodesWithText("Create your first list").fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeTestRule.onNodeWithText("Create your first list").performClick()
        composeTestRule.onNodeWithText("List Name").performTextInput("List 1")
        composeTestRule.onNodeWithText("Add").performClick()

        // Wait for list to be active and "Add Item" FAB to appear
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodesWithContentDescription("Add Item").fetchSemanticsNodes()
                .isNotEmpty()
        }

        // Add an item
        composeTestRule.onNodeWithContentDescription("Add Item").performClick()
        // In AddItemDialog, the label is "Title"
        composeTestRule.onNodeWithText("Title").performTextInput("First Item")
        composeTestRule.onNodeWithText("Add").performClick()

        // Wait for the new item to be displayed in the list
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodesWithText("First Item").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("First Item").assertIsDisplayed()
    }

    @Test
    fun mainScreen_toggleReorderMode() {
        val viewModel = createViewModel()
        composeTestRule.setContent {
            ImpermanentRectanglesTheme {
                MainScreen(viewModel = viewModel)
            }
        }

        // Add a list to enable reorder button
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodesWithText("Create your first list").fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeTestRule.onNodeWithText("Create your first list").performClick()
        composeTestRule.onNodeWithText("List Name").performTextInput("List 1")
        composeTestRule.onNodeWithText("Add").performClick()

        // Wait for list to be active and reorder button to appear
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodesWithContentDescription("Enter Reorder Mode")
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithContentDescription("Enter Reorder Mode").performClick()
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodesWithContentDescription("Exit Reorder Mode")
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithContentDescription("Exit Reorder Mode").assertIsDisplayed()
    }
}
