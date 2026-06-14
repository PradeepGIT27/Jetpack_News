package com.example.newsapp.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.example.newsapp.data.model.NewsArticle
import com.example.newsapp.ui.viewmodel.ArticleDetailUiState
import com.example.newsapp.ui.viewmodel.NewsViewModel
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test

class NewsDetailScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val viewModel: NewsViewModel = mockk(relaxed = true)
    private val detailUiStateFlow =
        MutableStateFlow<ArticleDetailUiState>(ArticleDetailUiState.Loading)

    @OptIn(ExperimentalSharedTransitionApi::class)
    @Test
    fun newsDetailScreen_showsArticleDetails_whenStateIsSuccess() {
        val mockArticle = NewsArticle(
            "1",
            "Quantum Processor",
            "A new chip.",
            "Detailed content about quantum chip.",
            "Sarah",
            "2026-06-14T08:00:00Z",
            "img",
            "TechPulse"
        )
        every { viewModel.detailUiState } returns detailUiStateFlow

        detailUiStateFlow.value = ArticleDetailUiState.Success(mockArticle)

        composeTestRule.setContent {
            SharedTransitionLayout {
                AnimatedContent(targetState = true, label = "") { _ ->
                    NewsDetailScreen(
                        articleId = "1",
                        viewModel = viewModel,
                        onBackClick = {},
                        sharedTransitionScope = this@SharedTransitionLayout,
                        animatedVisibilityScope = this@AnimatedContent
                    )
                }
            }
        }

        // Verify that the details are rendered
        composeTestRule.onNodeWithText("Quantum Processor").assertIsDisplayed()
        composeTestRule.onNodeWithText("Detailed content about quantum chip.").assertIsDisplayed()
        composeTestRule.onNodeWithText("By Sarah").assertIsDisplayed()
    }

    @OptIn(ExperimentalSharedTransitionApi::class)
    @Test
    fun newsDetailScreen_showsErrorState_whenStateIsError() {
        every { viewModel.detailUiState } returns detailUiStateFlow

        detailUiStateFlow.value = ArticleDetailUiState.Error("Network Failed")

        composeTestRule.setContent {
            SharedTransitionLayout {
                AnimatedContent(targetState = true, label = "") { _ ->
                    NewsDetailScreen(
                        articleId = "1",
                        viewModel = viewModel,
                        onBackClick = {},
                        sharedTransitionScope = this@SharedTransitionLayout,
                        animatedVisibilityScope = this@AnimatedContent
                    )
                }
            }
        }

        // Verify that the error and retry button are displayed
        composeTestRule.onNodeWithText("Oops! Something went wrong").assertIsDisplayed()
        composeTestRule.onNodeWithText("Network Failed").assertIsDisplayed()
        composeTestRule.onNodeWithText("Retry").assertIsDisplayed()
    }
}
