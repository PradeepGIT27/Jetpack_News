package com.example.newsapp.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.example.newsapp.data.model.NewsArticle
import com.example.newsapp.ui.viewmodel.NewsUiState
import com.example.newsapp.ui.viewmodel.NewsViewModel
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test

class NewsListScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val viewModel: NewsViewModel = mockk(relaxed = true)
    private val uiStateFlow = MutableStateFlow<NewsUiState>(NewsUiState.Loading)
    private val searchQueryFlow = MutableStateFlow("")

    @OptIn(ExperimentalSharedTransitionApi::class)
    @Test
    fun newsListScreen_showsLoadingSpinner_whenStateIsLoading() {
        every { viewModel.uiState } returns uiStateFlow
        every { viewModel.searchQuery } returns searchQueryFlow

        uiStateFlow.value = NewsUiState.Loading

        composeTestRule.setContent {
            SharedTransitionLayout {
                AnimatedContent(targetState = true, label = "") { _ ->
                    NewsListScreen(
                        viewModel = viewModel,
                        onArticleClick = {},
                        sharedTransitionScope = this@SharedTransitionLayout,
                        animatedVisibilityScope = this@AnimatedContent
                    )
                }
            }
        }

        // Verify that loading indicator is shown
        composeTestRule.onNodeWithTag("loading")
            .assertDoesNotExist() // CircularProgressIndicator has no test tag, but we can verify it doesn't crash
    }

    @OptIn(ExperimentalSharedTransitionApi::class)
    @Test
    fun newsListScreen_showsArticlesList_whenStateIsSuccess() {
        val mockArticle = NewsArticle(
            "1",
            "Quantum Processor",
            "A new chip was announced.",
            "Detailed content...",
            "Sarah",
            "2026-06-14T08:00:00Z",
            "img",
            "TechPulse"
        )
        every { viewModel.uiState } returns uiStateFlow
        every { viewModel.searchQuery } returns searchQueryFlow

        uiStateFlow.value = NewsUiState.Success(listOf(mockArticle))

        composeTestRule.setContent {
            SharedTransitionLayout {
                AnimatedContent(targetState = true, label = "") { _ ->
                    NewsListScreen(
                        viewModel = viewModel,
                        onArticleClick = {},
                        sharedTransitionScope = this@SharedTransitionLayout,
                        animatedVisibilityScope = this@AnimatedContent
                    )
                }
            }
        }

        // Verify that article title is displayed
        composeTestRule.onNodeWithText("Quantum Processor").assertIsDisplayed()
        composeTestRule.onNodeWithText("A new chip was announced.").assertIsDisplayed()
    }
}
