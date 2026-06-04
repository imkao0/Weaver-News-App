package dev.mkao.weaver.features.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.window.core.layout.WindowSizeClass
import androidx.window.core.layout.WindowWidthSizeClass
import dev.mkao.weaver.R
import dev.mkao.weaver.domain.model.Article
import dev.mkao.weaver.presentation.common.components.cards.CardArticle
import dev.mkao.weaver.presentation.common.components.cards.WeaverCoverFlowCard
import dev.mkao.weaver.presentation.common.components.cards.WeaverMagazineCard
import dev.mkao.weaver.presentation.common.components.cards.WeaverMasonryCard
import dev.mkao.weaver.presentation.common.components.cards.WeaverNewsCard
import dev.mkao.weaver.presentation.common.components.cards.WeaverTextUnderCard
import dev.mkao.weaver.presentation.common.components.feedback.ArticleCardShimmerEffect
import dev.mkao.weaver.presentation.common.components.feedback.BottomDialog
import dev.mkao.weaver.presentation.common.components.feedback.WeaverHeader
import dev.mkao.weaver.presentation.common.theme.WeaverPrimary
import dev.mkao.weaver.features.home.ArticleEvent
import dev.mkao.weaver.features.home.ArticleState
import dev.mkao.weaver.features.home.ArticleUiEvent
import dev.mkao.weaver.util.AppConstants
import dev.mkao.weaver.util.Dimens
import dev.mkao.weaver.util.NewsCategories
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlin.time.Duration.Companion.milliseconds

private const val SHIMMER_PLACEHOLDER_COUNT = 3
private const val SINGLE_COLUMN_COUNT = 1
private const val DOUBLE_COLUMN_COUNT = 2

/** How many items from the end of the feed trigger loading the next page. */
private const val LOAD_MORE_THRESHOLD = 3

private data class DashBoardCallbacks(
    val onArticleClick: (Article) -> Unit,
    val onSettingsClick: () -> Unit,
    val onEditionClick: () -> Unit,
    val onCategoryToggled: (NewsCategories) -> Unit,
    val onRefresh: () -> Unit,
    val onLoadMore: () -> Unit,
)

private class DashBoardUiState {
    var articleToBlock by mutableStateOf<Article?>(null)
    var showBottomSheet by mutableStateOf(false)
    var selectedArticle by mutableStateOf<Article?>(null)
    var showShimmer by mutableStateOf(true)
}

@Composable
private fun rememberDashBoardUiState(): DashBoardUiState {
    return remember { DashBoardUiState() }
}

/**
 * Home screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashBoard(
    onArticleClick: (Article) -> Unit,
    onSettingsClick: () -> Unit,
    onEditionClick: () -> Unit,
    windowSizeClass: WindowSizeClass,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val uiState = rememberDashBoardUiState()
    val snackbarHostState = remember { SnackbarHostState() }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is ArticleUiEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        if (state.articles.isEmpty() &&
            state.category.lowercase() !in listOf("sports", "entertainment")
        ) {
            viewModel.onEvent(ArticleEvent.CategorySelected(state.category))
        }
    }

    LaunchedEffect(state.articles) {
        uiState.showShimmer = state.articles.isEmpty()
        if (uiState.showShimmer) {
            delay(AppConstants.SHIMMER_TIMEOUT_MS.milliseconds)
            uiState.showShimmer = false
        }
    }

    val callbacks = DashBoardCallbacks(
        onArticleClick = onArticleClick,
        onSettingsClick = onSettingsClick,
        onEditionClick = onEditionClick,
        onCategoryToggled = { viewModel.onEvent(ArticleEvent.CategoryToggled(it.apiValue)) },
        onRefresh = { viewModel.onEvent(ArticleEvent.RefreshArticles) },
        onLoadMore = { viewModel.onEvent(ArticleEvent.LoadMoreArticles) },
    )

    DashBoardScaffold(
        state = state,
        uiState = uiState,
        callbacks = callbacks,
        windowSizeClass = windowSizeClass,
        snackbarHostState = snackbarHostState,
    )

    BlockSourceDialog(
        article = uiState.articleToBlock,
        onBlock = { viewModel.onEvent(ArticleEvent.BlockSource(it)) },
        onDismiss = { uiState.articleToBlock = null },
    )

    if (uiState.showBottomSheet && uiState.selectedArticle != null) {
        ModalBottomSheet(
            onDismissRequest = { uiState.showBottomSheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            dragHandle = null,
        ) {
            BottomDialog(
                article = uiState.selectedArticle!!,
                onReadFullStoryButtonClicked = {
                    uiState.showBottomSheet = false
                    onArticleClick(uiState.selectedArticle!!)
                },
            )
        }
    }
}

@Composable
private fun DashBoardScaffold(
    state: ArticleState,
    uiState: DashBoardUiState,
    callbacks: DashBoardCallbacks,
    windowSizeClass: WindowSizeClass,
    snackbarHostState: SnackbarHostState,
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            WeaverHomeTopBar(
                editionName = state.editionName,
                onMenuClick = callbacks.onSettingsClick,
                onGlobeClick = callbacks.onEditionClick,
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            CategoryTabStrip(
                categories = NewsCategories.entries.toList(),
                selectedCategories = state.categories,
                onCategoryToggled = callbacks.onCategoryToggled,
            )
            DashBoardFeed(
                state = state,
                uiState = uiState,
                onRefresh = callbacks.onRefresh,
                onLoadMore = callbacks.onLoadMore,
                windowSizeClass = windowSizeClass,
            )
        }
    }
}

@Composable
private fun DashBoardFeed(
    state: ArticleState,
    uiState: DashBoardUiState,
    onRefresh: () -> Unit,
    onLoadMore: () -> Unit,
    windowSizeClass: WindowSizeClass,
) {
    val onArticleSelect: (Article) -> Unit = {
        uiState.selectedArticle = it
        uiState.showBottomSheet = true
    }
    val onArticleLongClick: (Article) -> Unit = { uiState.articleToBlock = it }

    if (state.categories.contains("general")) {
        TopNewsFeed(
            state = state,
            showShimmer = uiState.showShimmer,
            onRefresh = onRefresh,
            onArticleSelect = onArticleSelect,
            onArticleLongClick = onArticleLongClick,
            onLoadMore = onLoadMore,
            windowSizeClass = windowSizeClass,
        )
    } else {
        CategoryStoriesContent(
            state = CategoryStoriesState(
                articles = state.articles,
                isLoading = state.isLoading || uiState.showShimmer,
                error = state.error,
                preferredLayout = state.preferredLayout,
                isLoadingMore = state.isLoadingMore,
                canLoadMore = state.canLoadMore,
                windowSizeClass = windowSizeClass,
            ),
            callbacks = CategoryStoriesCallbacks(
                onArticleClick = onArticleSelect,
                onArticleLongClick = onArticleLongClick,
                onRetry = onRefresh,
                onLoadMore = onLoadMore,
            ),
        )
    }
}

@Composable
private fun TopNewsFeed(
    state: ArticleState,
    showShimmer: Boolean,
    onRefresh: () -> Unit,
    onArticleSelect: (Article) -> Unit,
    onArticleLongClick: (Article) -> Unit,
    onLoadMore: () -> Unit,
    windowSizeClass: WindowSizeClass,
) {
    when {
        showShimmer -> {
            repeat(SHIMMER_PLACEHOLDER_COUNT) {
                ArticleCardShimmerEffect(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Dimens.PaddingSmall, vertical = Dimens.PaddingSmall),
                )
            }
        }
        state.articles.isEmpty() -> {
            ErrorContent(
                message = state.error ?: stringResource(R.string.no_articles_available),
                onRetry = onRefresh,
            )
        }
        else -> {
            val columns = when (windowSizeClass.windowWidthSizeClass) {
                WindowWidthSizeClass.COMPACT -> SINGLE_COLUMN_COUNT
                else -> DOUBLE_COLUMN_COUNT
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
                contentPadding = PaddingValues(
                    start = Dimens.PaddingSmall,
                    end = Dimens.PaddingSmall,
                    top = Dimens.PaddingMicro,
                    bottom = Dimens.PaddingMedium,
                ),
                horizontalArrangement = Arrangement.spacedBy(Dimens.PaddingMedium),
                verticalArrangement = Arrangement.spacedBy(Dimens.PaddingMedium),
            ) {
                items(state.articles, key = { it.url }) { article ->
                    FeedArticleCard(
                        article = article,
                        preferredLayout = state.preferredLayout,
                        category = state.category.replaceFirstChar { it.uppercase() },
                        onClick = { onArticleSelect(article) },
                        onLongClick = { onArticleLongClick(article) },
                    )
                }
                if (state.isLoadingMore) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        LoadingMoreIndicator()
                    }
                }
            }
        }
    }
}

/** Centered spinner shown at the bottom of the feed while paging in more articles. */
@Composable
internal fun LoadingMoreIndicator() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Dimens.PaddingMedium),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            color = WeaverPrimary,
            modifier = Modifier.size(Dimens.PaddingTripleExtraLarge),
        )
    }
}

@Composable
private fun FeedArticleCard(
    article: Article,
    preferredLayout: String,
    category: String,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    when (preferredLayout) {
        "compact" -> CardArticle(
            article = article,
            onReadFullStoryClicked = onClick,
        )
        "text-under" -> WeaverTextUnderCard(
            article = article,
            category = category,
            onClick = onClick,
            onLongClick = onLongClick,
        )
        "magazine" -> WeaverMagazineCard(
            article = article,
            category = category,
            onClick = onClick,
            onLongClick = onLongClick,
        )
        "coverflow" -> WeaverCoverFlowCard(
            article = article,
            category = category,
            onClick = onClick,
            onLongClick = onLongClick,
        )
        "masonry" -> WeaverMasonryCard(
            article = article,
            category = category,
            onClick = onClick,
            onLongClick = onLongClick,
        )
        else -> WeaverNewsCard(
            article = article,
            category = category,
            onClick = onClick,
            onLongClick = onLongClick,
        )
    }
}

@Composable
private fun BlockSourceDialog(
    article: Article?,
    onBlock: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    article?.let {
        val sourceName = it.source.name ?: "this source"
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(text = "Block Source") },
            text = { Text(text = "Are you sure you want to block all news from \"$sourceName\"?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        it.source.name?.let(onBlock)
                        onDismiss()
                    },
                ) {
                    Text(text = "BLOCK", color = WeaverPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(text = "CANCEL")
                }
            },
        )
    }
}

/**
 * Home top app bar: menu icon (left, opens Settings) — centered wordmark —
 * current edition label + globe icon (right, opens Select edition).
 */
@Composable
private fun WeaverHomeTopBar(
    editionName: String?,
    onMenuClick: () -> Unit,
    onGlobeClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 4.dp),
    ) {
        IconButton(
            onClick = onMenuClick,
            modifier = Modifier.align(Alignment.CenterStart),
        ) {
            Icon(
                imageVector = Icons.Outlined.Menu,
                contentDescription = stringResource(R.string.menu),
                tint = Color.DarkGray,
                modifier = Modifier.size(Dimens.StandardIconSize),
            )
        }

        WeaverHeader(
            modifier = Modifier.align(Alignment.Center),
            logoSize = Dimens.HeaderLogoSize,
        )

        Row(
            modifier = Modifier.align(Alignment.CenterEnd),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (!editionName.isNullOrBlank()) {
                Text(
                    text = editionName,
                    color = WeaverPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    modifier = Modifier
                        .clickable(onClick = onGlobeClick)
                        .padding(horizontal = Dimens.PaddingSmall),
                )
            }
            IconButton(onClick = onGlobeClick) {
                Icon(
                    painter = painterResource(R.drawable.ic_globe),
                    contentDescription = stringResource(R.string.select_edition),
                    tint = Color.DarkGray,
                    modifier = Modifier.size(Dimens.MediumIconSize),
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// CATEGORY TAB STRIP
// ---------------------------------------------------------------------------

@Composable
private fun CategoryTabStrip(
    categories: List<NewsCategories>,
    selectedCategories: Set<String>,
    onCategoryToggled: (NewsCategories) -> Unit,
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Dimens.PaddingMedium),
        contentPadding = PaddingValues(horizontal = Dimens.PaddingMedium),
        horizontalArrangement = Arrangement.spacedBy(Dimens.PaddingStandard),
    ) {
        items(categories, key = { it.apiValue }) { category ->
            CategoryTab(
                label = category.displayName,
                isSelected = selectedCategories.contains(category.apiValue.lowercase()),
                onClick = { onCategoryToggled(category) },
            )
        }
    }
}

@Composable
private fun CategoryTab(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = Dimens.PaddingMedium, vertical = Dimens.PaddingTiny),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            maxLines = 1,
        )
        if (isSelected) {
            Spacer(modifier = Modifier.height(Dimens.PaddingSmall))
            Box(
                modifier = Modifier
                    .width(Dimens.PaddingDoubleExtraLarge + 8.dp)
                    .height(Dimens.PaddingMicro + 1.dp)
                    .clip(RoundedCornerShape(Dimens.RadiusTiny))
                    .background(WeaverPrimary),
            )
        }
    }
}
