package com.example.newsapp.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.newsapp.ui.viewmodel.ArticleDetailUiState
import com.example.newsapp.ui.viewmodel.NewsViewModel
import com.example.newsapp.util.formatDate

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun NewsDetailScreen(
    articleId: String,
    viewModel: NewsViewModel,
    onBackClick: () -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    modifier: Modifier = Modifier
) {
    val detailState by viewModel.detailUiState.collectAsState()

    // Clear detail state when screen is disposed or when articleId changes
    DisposableEffect(articleId) {
        onDispose {
            viewModel.clearDetailState()
        }
    }

    // Fetch details when screen is created
    LaunchedEffect(articleId) {
        viewModel.fetchArticleById(articleId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Article Detail") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (val state = detailState) {
                is ArticleDetailUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                is ArticleDetailUiState.Success -> {
                    val article = state.article
                    if (article != null) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                        ) {
                            // Hero Image
                            with(sharedTransitionScope) {
                                AsyncImage(
                                    model = article.imageUrl,
                                    contentDescription = "Article Image Detail",
                                    modifier = Modifier
                                        .sharedElement(
                                            state = rememberSharedContentState(key = "image_${article.id}"),
                                            animatedVisibilityScope = animatedVisibilityScope
                                        )
                                        .fillMaxWidth()
                                        .height(240.dp)
                                        .clip(
                                            RoundedCornerShape(
                                                bottomStart = 24.dp,
                                                bottomEnd = 24.dp
                                            )
                                        )
                                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)),
                                    contentScale = ContentScale.Crop
                                )
                            }

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp)
                            ) {
                                // Source Name and Published Date
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = article.sourceName,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                    Text(
                                        text = "•",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                    )
                                    Text(
                                        text = formatDate(article.publishedAt),
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Title
                                Text(
                                    text = article.title,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    lineHeight = 28.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // Author
                                Text(
                                    text = "By ${article.author}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.onSurface.copy(
                                        alpha = 0.1f
                                    )
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                // Content
                                Text(
                                    text = article.content,
                                    fontSize = 16.sp,
                                    lineHeight = 24.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                                )
                            }
                        }
                    } else {
                        Text(
                            text = "Article not found",
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }

                is ArticleDetailUiState.Error -> {
                    ErrorState(
                        message = state.message,
                        onRetry = { viewModel.fetchArticleById(articleId) },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }
}
