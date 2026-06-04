package dev.mkao.weaver.features.bookmarks

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.mkao.weaver.R
import dev.mkao.weaver.domain.model.Article
import dev.mkao.weaver.presentation.common.components.cards.CardArticle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.min

/**
 * Bookmarks screen — observes the reactive bookmark stream, so a bookmark
 * toggled on the detail screen updates this list without a manual refetch.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarkScreen(
    onArticleClick: (Article) -> Unit,
    viewModel: BookmarkViewModel = androidx.hilt.navigation.compose.hiltViewModel(),
) {
    val bookmarkedArticles by viewModel.bookmarkedArticles.collectAsStateWithLifecycle()
    var isMenuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        stringResource(R.string.Favourites),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                },
                actions = {
                    IconButton(onClick = { isMenuExpanded = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = stringResource(R.string.more_options),
                            tint = MaterialTheme.colorScheme.onBackground,
                        )
                    }
                    DropdownMenu(
                        expanded = isMenuExpanded,
                        onDismissRequest = { isMenuExpanded = false },
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.clear_all_bookmarks)) },
                            onClick = {
                                isMenuExpanded = false
                                viewModel.clearAllBookmarks()
                            },
                        )
                    }
                },
            )
        },
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            if (bookmarkedArticles.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(R.string.no_bookmarks_yet),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            } else {
                ArticleList(
                    articles = bookmarkedArticles,
                    onToggleBookmark = viewModel::toggleBookmark,
                    onArticleClick = onArticleClick,
                )
            }
        }
    }
}

@Composable
fun ArticleList(
    articles: List<Article>,
    onToggleBookmark: (Article) -> Unit,
    onArticleClick: (Article) -> Unit,
) {
    LazyColumn {
        items(
            items = articles,
            key = { it.url },
        ) { article ->
            SwipeableArticleItem(
                article = article,
                onDismiss = { onToggleBookmark(article) },
                onArticleClick = { onArticleClick(article) },
            )
        }
    }
}

private const val DISMISS_THRESHOLD_PX = 250f
private const val MAX_DRAG_ALPHA_REDUCTION = 0.5f
private const val BASE_DRAG_RESISTANCE = 0.5f
private const val DRAG_RESISTANCE_DISTANCE_PX = 500f
private const val MAX_PROGRESSIVE_RESISTANCE = 0.7f
private const val DISMISS_ANIMATION_DELAY_MS = 150L

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeableArticleItem(
    article: Article,
    onDismiss: () -> Unit,
    onArticleClick: () -> Unit,
) {
    var offsetX by remember { mutableStateOf(0f) }
    val dismissThreshold = DISMISS_THRESHOLD_PX

    val animatedOffsetX by animateFloatAsState(
        targetValue = offsetX,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow,
        ),
        label = "offsetX",
    )

    val alpha by animateFloatAsState(
        targetValue = 1f - (min(abs(offsetX) / dismissThreshold, MAX_DRAG_ALPHA_REDUCTION)),
        label = "alpha",
    )

    val scope = rememberCoroutineScope()
    val baseDragResistanceFactor = BASE_DRAG_RESISTANCE

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .alpha(alpha)
            .offset(x = animatedOffsetX.dp)
            .draggable(
                orientation = Orientation.Horizontal,
                state = rememberDraggableState { delta ->
                    val dragProgress = min(abs(offsetX) / DRAG_RESISTANCE_DISTANCE_PX, MAX_PROGRESSIVE_RESISTANCE)
                    val progressiveResistance = 1f - dragProgress
                    val effectiveResistance = baseDragResistanceFactor * progressiveResistance
                    offsetX += delta * effectiveResistance
                },
                onDragStopped = { velocity ->
                    scope.launch {
                        if (abs(offsetX) > dismissThreshold) {
                            delay(DISMISS_ANIMATION_DELAY_MS)
                            onDismiss()
                        } else {
                            offsetX = 0f
                        }
                    }
                },
            ),
    ) {
        ArticleCard(article, onArticleClick)
    }
}

@Composable
fun ArticleCard(
    article: Article,
    onArticleClick: () -> Unit,
) {
    CardArticle(
        article = article,
        onReadFullStoryClicked = onArticleClick,
    )
}
