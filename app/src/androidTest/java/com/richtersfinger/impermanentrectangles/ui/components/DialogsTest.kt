package com.richtersfinger.impermanentrectangles.ui.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import com.richtersfinger.impermanentrectangles.ui.theme.ImpermanentRectanglesTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class DialogsTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun addListDialog_submitsData() {
        var submittedName = ""
        var submittedDesc = ""

        composeTestRule.setContent {
            ImpermanentRectanglesTheme {
                AddListDialog(
                    onDismiss = {},
                    onConfirm = { name, desc ->
                        submittedName = name
                        submittedDesc = desc
                    }
                )
            }
        }

        composeTestRule.onNodeWithText("Add New List").assertIsDisplayed()
        composeTestRule.onNodeWithText("List Name").performTextInput("Test List")
        composeTestRule.onNodeWithText("Description").performTextInput("Test Description")
        composeTestRule.onNodeWithText("Add").performClick()

        assertEquals("Test List", submittedName)
        assertEquals("Test Description", submittedDesc)
    }

    @Test
    fun addItemDialog_submitsData() {
        var submittedTitle = ""
        var submittedTarget = 0

        composeTestRule.setContent {
            ImpermanentRectanglesTheme {
                AddItemDialog(
                    onDismiss = {},
                    onConfirm = { title, _, target ->
                        submittedTitle = title
                        submittedTarget = target
                    }
                )
            }
        }

        composeTestRule.onNodeWithText("Add New Item").assertIsDisplayed()
        composeTestRule.onNodeWithText("Title").performTextInput("New Item")
        composeTestRule.onNodeWithText("Target Value").performTextClearance()
        composeTestRule.onNodeWithText("Target Value").performTextInput("5")
        composeTestRule.onNodeWithText("Add").performClick()

        assertEquals("New Item", submittedTitle)
        assertEquals(5, submittedTarget)
    }

    @Test
    fun deleteConfirmationDialog_callsConfirm() {
        var confirmCalled = false

        composeTestRule.setContent {
            ImpermanentRectanglesTheme {
                DeleteListConfirmationDialog(
                    listName = "Test List",
                    onDismiss = {},
                    onConfirm = { confirmCalled = true }
                )
            }
        }

        composeTestRule.onNodeWithText("Delete List").assertIsDisplayed()
        composeTestRule.onNodeWithText("Are you sure you want to delete the list 'Test List'?")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Delete").performClick()

        assertEquals(true, confirmCalled)
    }

    @Test
    fun aboutDialog_displaysVersion() {
        composeTestRule.setContent {
            ImpermanentRectanglesTheme {
                AboutDialog(
                    onDismiss = {},
                    versionName = "1.2.3",
                    versionCode = 42
                )
            }
        }

        composeTestRule.onNodeWithText("Impermanent Rectangles").assertIsDisplayed()
        composeTestRule.onNodeWithText("Version: 1.2.3").assertIsDisplayed()
    }
}
