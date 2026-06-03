package dev.mkao.weaver.features.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.window.core.layout.WindowSizeClass
import androidx.window.core.layout.WindowWidthSizeClass
import dev.mkao.weaver.R
import dev.mkao.weaver.domain.model.Article
import dev.mkao.weaver.presentation.common.components.cards.CardArticle
import dev.mkao.weaver.presentation.common.components.cards.WeaverCoverFlowCard
import dev.mkao.weaver.presentation.common.components.cards.WeaverMagazineCard
import dev.mkao.weaver.presentation.common.components.cards.WeaverMasonryCard
import dev.mkao.weaver.presentation.common.components.cards.WeaverTextUnderCard
import dev.mkao.weaver.presentation.common.components.feedback.ArticleCardShimmerEffect
import dev.mkao.weaver.util.Dimens

// ---------------------------------------------------------------------------
// TOP STORIES
// ---------------------------------------------------------------------------

private const val SHIMMER_PLACEHOLDER_COUNT = 3

/** How many items from the end of the feed trigger loading the next page. */
private const val LOAD_MORE_THRESHOLD = 3

internal data class CategoryStoriesState(
    val articles: List<Article>,
    val isLoading: Boolean,
    val error: String?,
    val preferredLayout: String,
    val isLoadingMore: Boolean,
    val canLoadMore: Boolean,
    val windowSizeClass: WindowSizeClass,
)

internal data class CategoryStoriesCallbacks(
    val onArticleClick: (Article) -> Unit,
    val onArticleLongClick: (Article) -> Unit,
    val onRetry: () -> Unit,
    val onLoadMore: () -> Unit,
)

@Composable
internal fun CategoryStoriesContent(
    state: CategoryStoriesState,
    callbacks: CategoryStoriesCallbacks,
) {
    val articles = state.articles
    val isLoading = state.isLoading
    val error = state.error
    val preferredLayout = state.preferredLayout
    val windowSizeClass = state.windowSizeClass
    val onArticleClick = callbacks.onArticleClick
    val onArticleLongClick = callbacks.onArticleLongClick
    val onRetry = callbacks.onRetry

    when {
        isLoading && articles.isEmpty() -> {
            repeat(SHIMMER_PLACEHOLDER_COUNT) {
                ArticleCardShimmerEffect(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Dimens.PaddingMedium, vertical = Dimens.PaddingSmall),
                )
            }
        }

        articles.isEmpty() -> {
            ErrorContent(
                message = error ?: stringResource(R.string.no_articles_available),
                onRetry = onRetry,
            )
        }

        else -> {
            CategoryStoriesGrid(
                articles = articles,
                preferredLayout = preferredLayout,
                isLoadingMore = state.isLoadingMore,
                windowSizeClass = windowSizeClass,
                onArticleClick = onArticleClick,
                onArticleLongClick = onArticleLongClick,
                onLoadMore = callbacks.onLoadMore,
            )
        }
    }
}

@Composable
private fun CategoryStoriesGrid(
    articles: List<Article>,
    preferredLayout: String,
    isLoadingMore: Boolean,
    windowSizeClass: WindowSizeClass,
    onArticleClick: (Article) -> Unit,
    onArticleLongClick: (Article) -> Unit,
    onLoadMore: () -> Unit,
) {
    val topStories = articles.take(TOP_STORIES_COUNT)
    val others = articles.drop(TOP_STORIES_COUNT)

    val columns = when (windowSizeClass.windowWidthSizeClass) {
        WindowWidthSizeClass.COMPACT -> 1
        else -> 2
    }

    val gridState = rememberLazyGridState()

    // Trigger the next page when the user scrolls near the end.
    val shouldLoadMore by remember {
        derivedStateOf {
            val layoutInfo = gridState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            totalItems > 0 && lastVisible >= totalItems - LOAD_MORE_THRESHOLD
        }
    }
    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) onLoadMore()
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        state = gridState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = Dimens.PaddingExtraLarge),
        horizontalArrangement = Arrangement.spacedBy(Dimens.PaddingMedium),
        verticalArrangement = Arrangement.spacedBy(Dimens.PaddingSmall),
    ) {
        item(key = "top_stories_pager", span = { GridItemSpan(maxLineSpan) }) {
            TopStoriesSection(
                topStories = topStories,
                preferredLayout = preferredLayout,
                onArticleClick = onArticleClick,
                onArticleLongClick = onArticleLongClick,
            )
        }
        item(key = "other_stories_header", span = { GridItemSpan(maxLineSpan) }) {
            Text(
                text = "Other Stories",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(
                    start = Dimens.PaddingMedium,
                    end = Dimens.PaddingMedium,
                    top = Dimens.PaddingExtraLarge,
                    bottom = Dimens.PaddingMedium,
                ),
            )
        }
        items(others, key = { it.url }) { article ->
            when (preferredLayout) {
                "compact" -> CardArticle(
                    article = article,
                    onReadFullStoryClicked = { onArticleClick(article) },
                )
                "text-under" -> WeaverTextUnderCard(
                    article = article,
                    category = article.source.name ?: "News",
                    onClick = { onArticleClick(article) },
                    onLongClick = { onArticleLongClick(article) },
                )
                "magazine" -> WeaverMagazineCard(
                    article = article,
                    category = article.source.name ?: "News",
                    onClick = { onArticleClick(article) },
                    onLongClick = { onArticleLongClick(article) },
                )
                "coverflow" -> WeaverCoverFlowCard(
                    article = article,
                    category = article.source.name ?: "News",
                    onClick = { onArticleClick(article) },
                    onLongClick = { onArticleLongClick(article) },
                )
                "masonry" -> WeaverMasonryCard(
                    article = article,
                    category = article.source.name ?: "News",
                    onClick = { onArticleClick(article) },
                    onLongClick = { onArticleLongClick(article) },
                )
                else -> OtherStoryRow(
                    article = article,
                    onClick = { onArticleClick(article) },
                    onLongClick = { onArticleLongClick(article) },
                )
            }
        }
        if (isLoadingMore) {
            item(key = "loading_more", span = { GridItemSpan(maxLineSpan) }) {
                LoadingMoreIndicator()
            }
        }
    }
}

/** Number of top-story articles shown in the home feed hero carousel. */
internal const val TOP_STORIES_COUNT = 3
