package com.example.newsapp.data.repository

import com.example.newsapp.data.local.ArticleDao
import com.example.newsapp.data.model.NewsArticle
import com.example.newsapp.data.model.toDomain
import com.example.newsapp.data.model.toEntity
import com.example.newsapp.data.remote.ArticleDto
import com.example.newsapp.data.remote.NewsApi
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import javax.inject.Singleton

interface NewsRepository {
    fun getTopHeadlines(): Flow<Result<List<NewsArticle>>>
    fun searchNews(query: String): Flow<Result<List<NewsArticle>>>
    fun getArticleById(id: String): Flow<Result<NewsArticle?>>
}

@Singleton
class NewsRepositoryImpl @Inject constructor(
    private val newsApi: NewsApi,
    private val articleDao: ArticleDao,
    private val ioDispatcher: CoroutineDispatcher
) : NewsRepository {

    override fun getTopHeadlines(): Flow<Result<List<NewsArticle>>> = flow {
        // Emit cached data first (instant load)
        val cachedArticles = articleDao.getArticles().first()
        if (cachedArticles.isNotEmpty()) {
            emit(Result.success(cachedArticles.map { it.toDomain() }))
        }

        try {
            // Fetch remote data
            val response = newsApi.getTopHeadlines()
            val domainArticles = response.articles.map { it.toDomain() }

            // Clear old cache and insert fresh data
            articleDao.clearArticles()
            articleDao.insertArticles(domainArticles.map { it.toEntity() })

            // Emit fresh data
            emit(Result.success(domainArticles))
        } catch (e: Exception) {
            // Fallback to cache if network call fails
            val fallback = articleDao.getArticles().first()
            if (fallback.isNotEmpty()) {
                emit(Result.success(fallback.map { it.toDomain() }))
            } else {
                emit(Result.failure(e))
            }
        }
    }.flowOn(ioDispatcher)

    override fun searchNews(query: String): Flow<Result<List<NewsArticle>>> = flow {
        try {
            if (query.isBlank()) {
                // If query is blank, return top headlines from cache
                val cached = articleDao.getArticles().first().map { it.toDomain() }
                emit(Result.success(cached))
            } else {
                // SQL search via Room for local/offline-first searching
                val sqlQuery = "%$query%"
                val searchResults = articleDao.searchArticles(sqlQuery).map { it.toDomain() }
                emit(Result.success(searchResults))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.flowOn(ioDispatcher)

    override fun getArticleById(id: String): Flow<Result<NewsArticle?>> = flow {
        try {
            // Read from Room cache
            val localArticle = articleDao.getArticleById(id)?.toDomain()
            emit(Result.success(localArticle))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.flowOn(ioDispatcher)
}

// Mapper extension function for ArticleDto to Domain Model
fun ArticleDto.toDomain(): NewsArticle {
    // Generate unique ID based on hash of URL or title
    val generatedId = url?.hashCode()?.toString() ?: title?.hashCode()?.toString() ?: ""
    return NewsArticle(
        id = generatedId,
        title = title ?: "",
        description = description ?: "",
        content = content ?: "",
        author = author ?: "Unknown Author",
        publishedAt = publishedAt ?: "",
        imageUrl = urlToImage ?: "",
        sourceName = source?.name ?: "Unknown Source"
    )
}
