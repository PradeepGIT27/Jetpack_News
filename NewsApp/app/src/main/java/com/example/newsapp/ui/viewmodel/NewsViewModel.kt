package com.example.newsapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.newsapp.data.model.NewsArticle
import com.example.newsapp.data.repository.NewsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface NewsUiState {
    object Loading : NewsUiState
    data class Success(val articles: List<NewsArticle>) : NewsUiState
    data class Error(val message: String) : NewsUiState
}

sealed interface ArticleDetailUiState {
    object Loading : ArticleDetailUiState
    data class Success(val article: NewsArticle?) : ArticleDetailUiState
    data class Error(val message: String) : ArticleDetailUiState
}

@HiltViewModel
class NewsViewModel @Inject constructor(
    private val newsRepository: NewsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<NewsUiState>(NewsUiState.Loading)
    val uiState: StateFlow<NewsUiState> = _uiState.asStateFlow()

    private val _detailUiState =
        MutableStateFlow<ArticleDetailUiState>(ArticleDetailUiState.Loading)
    val detailUiState: StateFlow<ArticleDetailUiState> = _detailUiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    init {
        fetchTopHeadlines()
    }

    fun fetchTopHeadlines() {
        viewModelScope.launch {
            _uiState.value = NewsUiState.Loading
            newsRepository.getTopHeadlines()
                .catch { e ->
                    _uiState.value =
                        NewsUiState.Error(e.localizedMessage ?: "Unknown Error occurred")
                }
                .collectLatest { result ->
                    result.fold(
                        onSuccess = { articles ->
                            _uiState.value = NewsUiState.Success(articles)
                        },
                        onFailure = { error ->
                            _uiState.value = NewsUiState.Error(
                                error.localizedMessage ?: "Failed to fetch articles"
                            )
                        }
                    )
                }
        }
    }

    fun searchNews(query: String) {
        _searchQuery.value = query
        viewModelScope.launch {
            _uiState.value = NewsUiState.Loading
            newsRepository.searchNews(query)
                .catch { e ->
                    _uiState.value =
                        NewsUiState.Error(e.localizedMessage ?: "Unknown Error occurred")
                }
                .collectLatest { result ->
                    result.fold(
                        onSuccess = { articles ->
                            _uiState.value = NewsUiState.Success(articles)
                        },
                        onFailure = { error ->
                            _uiState.value = NewsUiState.Error(
                                error.localizedMessage ?: "Failed to find articles"
                            )
                        }
                    )
                }
        }
    }

    private var fetchArticleJob: Job? = null

    fun fetchArticleById(id: String) {
        fetchArticleJob?.cancel()
        fetchArticleJob = viewModelScope.launch {
            _detailUiState.value = ArticleDetailUiState.Loading
            newsRepository.getArticleById(id)
                .catch { e ->
                    _detailUiState.value =
                        ArticleDetailUiState.Error(e.localizedMessage ?: "Unknown Error occurred")
                }
                .collectLatest { result ->
                    result.fold(
                        onSuccess = { article ->
                            _detailUiState.value = ArticleDetailUiState.Success(article)
                        },
                        onFailure = { error ->
                            _detailUiState.value = ArticleDetailUiState.Error(
                                error.localizedMessage ?: "Failed to fetch article details"
                            )
                        }
                    )
                }
        }
    }

    fun clearDetailState() {
        fetchArticleJob?.cancel()
        _detailUiState.value = ArticleDetailUiState.Loading
    }
}
