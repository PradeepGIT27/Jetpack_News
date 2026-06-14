package com.example.newsapp.data.model

data class NewsArticle(
    val id: String,
    val title: String,
    val description: String,
    val content: String,
    val author: String,
    val publishedAt: String,
    val imageUrl: String,
    val sourceName: String
)