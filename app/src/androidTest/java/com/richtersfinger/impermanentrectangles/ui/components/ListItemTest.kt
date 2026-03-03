package com.richtersfinger.impermanentrectangles.ui.components

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeRight
import com.richtersfinger.impermanentrectangles.Item
import com.richtersfinger.impermanentrectangles.ui.theme.ImpermanentRectanglesTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class ListItemTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun listItem_displaysTitleAndDescription() {
        val item = Item(
            title = "Test Title",
            description = "Test Description",
            currentValue = 1,
            targetValue = 3
        )

        composeTestRule.setContent {
            ImpermanentRectanglesTheme {
                ListItem(
                    item = item,
                    isExpanded = false,
                    onToggleExpand = {},
                    onEditClick = {},
                    onDeleteClick = {},
                    onUpdateValue = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Test Title").assertExists()
        composeTestRule.onNodeWithText("Test Description").assertDoesNotExist()
    }

    @Test
    fun listItem_displaysTitleAndDescriptionExpanded() {
        val item = Item(
            title = "Test Title",
            description = "Test Description",
            currentValue = 1,
            targetValue = 3
        )

        composeTestRule.setContent {
            ImpermanentRectanglesTheme {
                ListItem(
                    item = item,
                    isExpanded = true,
                    onToggleExpand = {},
                    onEditClick = {},
                    onDeleteClick = {},
                    onUpdateValue = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Test Title").assertExists()
        composeTestRule.onNodeWithText("Test Description").assertExists()
    }

    @Test
    fun listItem_swipeUpdatesValue() {
        var updatedDelta = 0
        val item = Item(
            title = "Test Item",
            description = "Test Description",
            currentValue = 1,
            targetValue = 5
        )

        composeTestRule.setContent {
            ImpermanentRectanglesTheme {
                ListItem(
                    item = item,
                    isExpanded = false,
                    onToggleExpand = {},
                    onEditClick = {},
                    onDeleteClick = {},
                    onUpdateValue = { updatedDelta = it }
                )
            }
        }

        // ListItem handles horizontal drag gestures. 
        // We need to swipe enough to pass the threshold (150f).
        composeTestRule.onNodeWithText("Test Item").performTouchInput {
            swipeRight(startX = 0f, endX = 500f)
        }
        // Wait for state update if necessary, though it should be immediate in this test setup
        assertEquals(1, updatedDelta)
    }

    @Test
    fun listItem_clickTogglesExpand() {
        var expandToggled = false
        val item = Item(
            title = "Test Item",
            description = "Test Description",
            currentValue = 1,
            targetValue = 5
        )

        composeTestRule.setContent {
            ImpermanentRectanglesTheme {
                ListItem(
                    item = item,
                    isExpanded = false,
                    onToggleExpand = { expandToggled = true },
                    onEditClick = {},
                    onDeleteClick = {},
                    onUpdateValue = {}
                )
            }
        }

        // Use a more specific matcher if possible, or just click the text
        composeTestRule.onNodeWithText("Test Item").performClick()
        assertEquals(true, expandToggled)
    }

    @Test
    fun listItem_expandShowsActions() {
        val item = Item(title = "Test Item", description = "Test Description")

        composeTestRule.setContent {
            ImpermanentRectanglesTheme {
                ListItem(
                    item = item,
                    isExpanded = true,
                    onToggleExpand = {},
                    onEditClick = {},
                    onDeleteClick = {},
                    onUpdateValue = {}
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("More Options").performClick()
        composeTestRule.onNodeWithText("Edit").assertExists()
        composeTestRule.onNodeWithText("Remove").assertExists()
        // Check for More options menu if it exists
    }
}
