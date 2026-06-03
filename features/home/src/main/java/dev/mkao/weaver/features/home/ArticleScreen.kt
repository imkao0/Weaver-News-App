package dev.mkao.weaver.features.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.mkao.weaver.R
import dev.mkao.weaver.domain.model.Article
import dev.mkao.weaver.presentation.common.components.cards.CardArticle
import dev.mkao.weaver.presentation.common.components.feedback.ArticleCardShimmerEffect
import dev.mkao.weaver.presentation.common.components.feedback.BottomDialog
import dev.mkao.weaver.presentation.common.components.feedback.TintedTextButton
import dev.mkao.weaver.features.home.ArticleEvent
import dev.mkao.weaver.features.home.ArticleState
import dev.mkao.weaver.features.shared.SearchAppBar
import dev.mkao.weaver.util.NewsCategories
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleScreen(
    onArticleClick: (Article) -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val screenState = rememberArticleScreenState()

    LaunchedEffect(Unit) {
        if (state.category.isEmpty()) {
            viewModel.onEvent(ArticleEvent.CategorySelected(NewsCategories.General.apiValue))
        }
    }

    Scaffold(
        topBar = { ArticleScreenTopBar() },
        content = { paddingValues ->
            ArticleScreenContent(
                state = state,
                screenState = screenState,
                paddingValues = paddingValues,
                onEvent = viewModel::onEvent,
            )
        },
    )

    if (screenState.shouldBottomSheetShow) {
        ArticleBottomSheet(
            article = state.selectedArticle,
            sheetState = screenState.sheetState,
            onDismiss = { screenState.shouldBottomSheetShow = false },
            onReadFullStoryButtonClick = { article ->
                screenState.hideBottomSheet()
                onArticleClick(article)
            },
        )
    }
}

@Composable
private fun ArticleScreenTopBar() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.Start,
    ) {
        Text(
            text = stringResource(R.string.discover),
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = stringResource(R.string.fresh_stories_and_bold_ideas_to_help_you_live_curiously),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
        )
    }
}

@Composable
private fun ArticleScreenContent(
    state: ArticleState,
    screenState: ArticleScreenState,
    paddingValues: PaddingValues,
    onEvent: (ArticleEvent) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
    ) {
        SearchAppBar(
            modifier = Modifier.focusRequester(screenState.focusRequester),
            value = state.searchQuery,
            onValueChange = { newQuery ->
                onEvent(ArticleEvent.SearchQueryChanged(newQuery))
            },
            onCloseIconClicked = {
                onEvent(ArticleEvent.CloseSearch)
            },
            onSearchClicked = screenState::hideKeyboardAndClearFocus,
        )

        Spacer(modifier = Modifier.height(2.dp))

        CategorySelector(
            categories = screenState.categories,
            onEvent = onEvent,
            currentCategory = state.category,
        )

        if (state.isLoading) {
            ArticleLoadingShimmer()
        } else {
            ArticleList(
                articles = state.articles,
                isLoadingMore = state.isLoadingMore,
                onArticleClicked = { article ->
                    onEvent(ArticleEvent.ArticleSelected(article))
                    screenState.shouldBottomSheetShow = true
                },
                onLoadMore = { onEvent(ArticleEvent.LoadMoreArticles) },
            )
        }
    }
}

@Composable
private fun CategorySelector(
    categories: List<NewsCategories>,
    onEvent: (ArticleEvent) -> Unit,
    currentCategory: String,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(categories) { category ->
            TintedTextButton(
                isSelected = category.apiValue == currentCategory,
                category = category.displayName,
                onClick = {
                    onEvent(ArticleEvent.CategorySelected(category.apiValue))
                },
            )
        }
    }
}

private const val SHIMMER_PLACEHOLDER_COUNT = 5

/** How many items from the end of the feed trigger loading the next page. */
private const val LOAD_MORE_THRESHOLD = 3

@Composable
private fun ArticleLoadingShimmer() {
    repeat(SHIMMER_PLACEHOLDER_COUNT) {
        ArticleCardShimmerEffect(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
        )
    }
}

@Composable
private fun ArticleList(
    articles: List<Article>,
    isLoadingMore: Boolean,
    onArticleClicked: (Article) -> Unit,
    onLoadMore: () -> Unit,
) {
    val listState = rememberLazyListState()

    // Trigger the next page.
    val shouldLoadMore by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            totalItems > 0 && lastVisible >= totalItems - LOAD_MORE_THRESHOLD
        }
    }
    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) onLoadMore()
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(2.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        items(articles) { article ->
            CardArticle(
                article = article,
                onReadFullStoryClicked = { onArticleClicked(article) },
            )
            Spacer(modifier = Modifier.height(0.5.dp))
        }
        if (isLoadingMore) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp),
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ArticleBottomSheet(
    article: Article?,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onReadFullStoryButtonClick: (Article) -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = null,
    ) {
        article?.let {
            BottomDialog(
                article = it,
                onReadFullStoryButtonClicked = {
                    onReadFullStoryButtonClick(it)
                },
            )
        }
    }
}

@Composable
fun rememberArticleScreenState(
    categories: List<NewsCategories> = NewsCategories.values().toList(),
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    focusManager: FocusManager = LocalFocusManager.current,
    focusRequester: FocusRequester = remember { FocusRequester() },
    keyboardController: SoftwareKeyboardController? = LocalSoftwareKeyboardController.current,
): ArticleScreenState {
    val density = LocalDensity.current
    return remember {
        ArticleScreenState(
            categories = categories,
            coroutineScope = coroutineScope,
            focusManager = focusManager,
            focusRequester = focusRequester,
            keyboardController = keyboardController,
            density = density,
        )
    }
}

class ArticleScreenState(
    val categories: List<NewsCategories>,
    private val coroutineScope: CoroutineScope,
    private val focusManager: FocusManager,
    val focusRequester: FocusRequester,
    private val keyboardController: SoftwareKeyboardController?,
    private val density: Density,
) {
    var shouldBottomSheetShow by mutableStateOf(false)

    @OptIn(ExperimentalMaterial3Api::class)
    val sheetState = SheetState(skipPartiallyExpanded = true, density = density)

    fun hideKeyboardAndClearFocus() {
        keyboardController?.hide()
        focusManager.clearFocus()
    }

    @OptIn(ExperimentalMaterial3Api::class)
    fun hideBottomSheet() {
        coroutineScope.launch {
            sheetState.hide()
        }.invokeOnCompletion {
            if (!sheetState.isVisible) shouldBottomSheetShow = false
        }
    }
}
