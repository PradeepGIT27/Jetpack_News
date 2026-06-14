package com.example.newsapp.data.remote

import retrofit2.http.GET

data class SourceDto(
    val id: String?,
    val name: String?
)

data class ArticleDto(
    val source: SourceDto?,
    val author: String?,
    val title: String?,
    val description: String?,
    val url: String?,
    val urlToImage: String?,
    val publishedAt: String?,
    val content: String?
)

data class NewsApiResponse(
    val status: String,
    val totalResults: Int,
    val articles: List<ArticleDto>
)

interface NewsApi {
    @GET("top-headlines/category/general/us.json")
    suspend fun getTopHeadlines(): NewsApiResponse

    // For everything source endpoint we query cnn.json
    @GET("everything/cnn.json")
    suspend fun getCnnNews(): NewsApiResponse
}
