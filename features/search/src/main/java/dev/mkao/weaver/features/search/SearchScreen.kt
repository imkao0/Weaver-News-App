package dev.mkao.weaver.features.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import dev.mkao.weaver.R
import dev.mkao.weaver.domain.model.Article
import dev.mkao.weaver.presentation.common.components.feedback.ArticleCardShimmerEffect
import dev.mkao.weaver.presentation.common.components.feedback.WeaverHeader
import dev.mkao.weaver.presentation.common.theme.WeaverTertiary
import dev.mkao.weaver.util.Dimens
import dev.mkao.weaver.util.calculateElapsedTime

/**
 * Search tab: centered logo header, a rounded search field, then a vertical
 * list of article rows.
 */
private const val SHIMMER_PLACEHOLDER_COUNT = 5

@Composable
fun SearchScreen(
    onArticleClick: (Article) -> Unit,
    viewModel: SearchViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val recentSearches by viewModel.recentSearches.collectAsStateWithLifecycle()
    val keyboardController = LocalSoftwareKeyboardController.current

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { WeaverHeader(modifier = Modifier.statusBarsPadding()) },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            WeaverSearchBar(
                value = state.searchQuery,
                onValueChange = viewModel::onSearchQueryChanged,
                onSearch = {
                    viewModel.onSearchClicked()
                    keyboardController?.hide()
                },
            )

            val showRecentSearches =
                state.searchQuery.isBlank() && state.articles.isEmpty() && recentSearches.isNotEmpty()

            if (state.isLoading) {
                repeat(SHIMMER_PLACEHOLDER_COUNT) {
                    ArticleCardShimmerEffect(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Dimens.PaddingLarge, vertical = Dimens.PaddingMediumSmall),
                    )
                }
            } else if (showRecentSearches) {
                RecentSearchHistory(
                    recentSearches = recentSearches,
                    onQueryClick = {
                        viewModel.onRecentSearchClicked(it)
                        keyboardController?.hide()
                    },
                    onClearAll = viewModel::clearRecentSearches,
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = Dimens.PaddingLarge),
                ) {
                    itemsIndexed(state.articles, key = { _, article -> article.url }) { index, article ->
                        SearchResultRow(
                            article = article,
                            onClick = { onArticleClick(article) },
                        )
                        if (index < state.articles.lastIndex) {
                            HorizontalDivider(
                                modifier = Modifier.padding(start = 136.dp),
                                thickness = 0.5.dp,
                                color = Color.Gray.copy(alpha = 0.3f),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WeaverSearchBar(
    value: String,
    onValueChange: (String) -> Unit,
    onSearch: () -> Unit,
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.PaddingLarge, vertical = Dimens.PaddingMediumSmall)
            .height(Dimens.SearchFieldHeight)
            .clip(RoundedCornerShape(Dimens.RadiusMediumLarge)),
        placeholder = {
            Text(
                text = stringResource(R.string.search_for_articles),
                color = Color.LightGray,
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        leadingIcon = {
            Icon(
                painter = painterResource(id = R.drawable.ic_search),
                contentDescription = stringResource(R.string.nav_search),
                tint = WeaverTertiary,
                modifier = Modifier.size(Dimens.RadiusCircular),
            )
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSearch() }),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
            cursorColor = WeaverTertiary,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
        ),
    )
}

@Composable
private fun SearchResultRow(
    article: Article,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = Dimens.PaddingSmall),
        verticalAlignment = Alignment.Top,
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(article.image)
                .crossfade(true)
                .build(),
            contentDescription = article.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(120.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            placeholder = painterResource(R.drawable.placeholder_image),
            error = painterResource(R.drawable.placeholder_image),
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.PaddingMedium),
        ) {
            Text(
                text = article.title,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )

            article.description?.let {
                Spacer(modifier = Modifier.height(Dimens.PaddingTiny))
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Spacer(modifier = Modifier.height(Dimens.PaddingSmall))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "— ${calculateElapsedTime(article.publishedAt)}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
    }
}

/**
 * Recent-search history rows, shown when the field is empty. Tapping a row
 * re-runs that query; the trailing action clears the history.
 */
@Composable
private fun RecentSearchHistory(
    recentSearches: List<dev.mkao.weaver.domain.model.RecentSearch>,
    onQueryClick: (String) -> Unit,
    onClearAll: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = Dimens.PaddingLarge, vertical = Dimens.PaddingTiny),
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = Dimens.PaddingMediumSmall),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.recent_searches),
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = stringResource(R.string.clear),
                    color = WeaverTertiary,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.clickable(onClick = onClearAll),
                )
            }
        }
        items(recentSearches, key = { it.id }) { recent ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onQueryClick(recent.query) }
                    .padding(vertical = Dimens.PaddingMediumSmall),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Outlined.History,
                    contentDescription = null,
                    tint = Color.LightGray,
                    modifier = Modifier.size(Dimens.RadiusCircular),
                )
                Spacer(modifier = Modifier.width(Dimens.PaddingMedium))
                Text(
                    text = recent.query,
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
fun WeaverChip(
    text: String,
    backgroundColor: Color,
    textColor: Color,
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(Dimens.PaddingSmall))
            .background(backgroundColor)
            .padding(horizontal = Dimens.PaddingMediumSmall, vertical = Dimens.ExtraSmallPadding),
    ) {
        Text(
            text = text,
            color = textColor,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
