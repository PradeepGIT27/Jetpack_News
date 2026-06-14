package com.example.newsapp.data.repository

import com.example.newsapp.data.local.ArticleDao
import com.example.newsapp.data.model.ArticleEntity
import com.example.newsapp.data.remote.ArticleDto
import com.example.newsapp.data.remote.NewsApi
import com.example.newsapp.data.remote.NewsApiResponse
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NewsRepositoryTest {

    private val newsApi: NewsApi = mockk(relaxed = true)
    private val articleDao: ArticleDao = mockk(relaxed = true)
    private val testDispatcher = StandardTestDispatcher()

    private lateinit var newsRepository: NewsRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        newsRepository = NewsRepositoryImpl(newsApi, articleDao, testDispatcher)
    }

    @Test
    fun `getTopHeadlines emits cached data first then updates from remote`() =
        runTest(testDispatcher) {
            val cachedEntity = ArticleEntity(
                "1",
                "Cached Title",
                "Desc",
                "Content",
                "Author",
                "Date",
                "ImgUrl",
                "Source"
            )
            val remoteDto = ArticleDto(
                source = null,
                author = "Remote Author",
                title = "Remote Title",
                description = "Remote Desc",
                url = "http://remote.com",
                urlToImage = "RemoteImgUrl",
                publishedAt = "RemoteDate",
                content = "RemoteContent"
            )
            val apiResponse = NewsApiResponse("ok", 1, listOf(remoteDto))

            every { articleDao.getArticles() } returns flowOf(listOf(cachedEntity))
            coEvery { newsApi.getTopHeadlines() } returns apiResponse

            val results = newsRepository.getTopHeadlines().toList()

            // Verify cached was emitted
            assertEquals(2, results.size)
            val firstResult = results[0].getOrNull()
            assertEquals(1, firstResult?.size)
            assertEquals("Cached Title", firstResult?.get(0)?.title)

            // Verify remote was emitted
            val secondResult = results[1].getOrNull()
            assertEquals(1, secondResult?.size)
            assertEquals("Remote Title", secondResult?.get(0)?.title)

            // Verify cache operations
            coVerify(exactly = 1) { articleDao.clearArticles() }
            coVerify(exactly = 1) { articleDao.insertArticles(any()) }
        }

    @Test
    fun `getTopHeadlines falls back to cache on API error`() = runTest(testDispatcher) {
        val cachedEntity = ArticleEntity(
            "1",
            "Cached Title",
            "Desc",
            "Content",
            "Author",
            "Date",
            "ImgUrl",
            "Source"
        )
        every { articleDao.getArticles() } returns flowOf(listOf(cachedEntity))
        coEvery { newsApi.getTopHeadlines() } throws RuntimeException("Network Error")

        val results = newsRepository.getTopHeadlines().toList()

        // Emits cache first, then tries remote and falls back to cache or error
        // In our implementation, if remote throws but cache was present, we fallback to cache.
        assertEquals(2, results.size)
        assertEquals("Cached Title", results[0].getOrNull()?.get(0)?.title)
        assertEquals("Cached Title", results[1].getOrNull()?.get(0)?.title)
    }
}
// Trigger IDE analysis - updated
