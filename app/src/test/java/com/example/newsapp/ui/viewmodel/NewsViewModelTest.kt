package com.example.newsapp.ui.viewmodel

import app.cash.turbine.test
import com.example.newsapp.data.model.NewsArticle
import com.example.newsapp.data.repository.NewsRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NewsViewModelTest {

    private val newsRepository: NewsRepository = mockk(relaxed = true)
    private val testDispatcher = StandardTestDispatcher()

    private lateinit var viewModel: NewsViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `fetchTopHeadlines updates uiState from Loading to Success`() = runTest(testDispatcher) {
        val mockArticle =
            NewsArticle("1", "Title", "Desc", "Content", "Author", "Date", "ImgUrl", "Source")
        coEvery { newsRepository.getTopHeadlines() } returns flowOf(
            Result.success(
                listOf(
                    mockArticle
                )
            )
        )

        viewModel = NewsViewModel(newsRepository)

        viewModel.uiState.test {
            // First emit is Loading (initial state)
            assertEquals(NewsUiState.Loading, awaitItem())

            // Trigger load (init block runs it, but since we are running inside runTest, 
            // the init block coroutine will execute on testDispatcher)
            testDispatcher.scheduler.advanceUntilIdle()

            val successState = awaitItem()
            assert(successState is NewsUiState.Success)
            assertEquals(1, (successState as NewsUiState.Success).articles.size)
            assertEquals("Title", successState.articles[0].title)
        }
    }

    @Test
    fun `fetchArticleById updates detailUiState correctly`() = runTest(testDispatcher) {
        val mockArticle =
            NewsArticle("1", "Title", "Desc", "Content", "Author", "Date", "ImgUrl", "Source")
        coEvery { newsRepository.getArticleById("1") } returns flowOf(Result.success(mockArticle))

        viewModel = NewsViewModel(newsRepository)

        viewModel.fetchArticleById("1")

        viewModel.detailUiState.test {
            // Initial state in viewModel is Loading
            assertEquals(ArticleDetailUiState.Loading, awaitItem())

            testDispatcher.scheduler.advanceUntilIdle()

            val successState = awaitItem()
            assert(successState is ArticleDetailUiState.Success)
            assertEquals("Title", (successState as ArticleDetailUiState.Success).article?.title)
        }
    }

    @Test
    fun `clearDetailState resets state to Loading`() = runTest(testDispatcher) {
        viewModel = NewsViewModel(newsRepository)

        viewModel.clearDetailState()

        viewModel.detailUiState.test {
            assertEquals(ArticleDetailUiState.Loading, awaitItem())
        }
    }
}
// Trigger IDE analysis - updated
