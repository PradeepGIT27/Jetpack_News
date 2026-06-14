package com.example.newsapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "articles")
data class ArticleEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val content: String,
    val author: String,
    val publishedAt: String,
    val imageUrl: String,
    val sourceName: String
)

fun ArticleEntity.toDomain() = NewsArticle(
    id = id,
    title = title,
    description = description,
    content = content,
    author = author,
    publishedAt = publishedAt,
    imageUrl = imageUrl,
    sourceName = sourceName
)

fun NewsArticle.toEntity() = ArticleEntity(
    id = id,
    title = title,
    description = description,
    content = content,
    author = author,
    publishedAt = publishedAt,
    imageUrl = imageUrl,
    sourceName = sourceName
)
