package dev.mkao.weaver.features.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import dev.mkao.weaver.R
import dev.mkao.weaver.domain.model.Article
import dev.mkao.weaver.presentation.common.components.cards.CardArticle
import dev.mkao.weaver.presentation.common.components.cards.WeaverCoverFlowCard
import dev.mkao.weaver.presentation.common.components.cards.WeaverMagazineCard
import dev.mkao.weaver.presentation.common.components.cards.WeaverMasonryCard
import dev.mkao.weaver.presentation.common.components.cards.WeaverTextUnderCard
import dev.mkao.weaver.presentation.common.theme.WeaverPrimary
import dev.mkao.weaver.presentation.common.theme.WeaverTextSecondary
import dev.mkao.weaver.util.AppConstants
import dev.mkao.weaver.util.Dimens
import dev.mkao.weaver.util.shortDateFormat
import kotlinx.coroutines.delay

private const val HERO_AUTO_ADVANCE_DELAY_MS = 3000L
private const val ERROR_TEXT_ARGB = 0xFF8E8E93

private val ErrorTextColor
    get() = Color(ERROR_TEXT_ARGB)

/**
 * Top Stories section: a horizontally swipeable [HorizontalPager] carousel of
 * [TOP_STORIES_COUNT] hero cards with a dot indicator highlighting the
 * current page.
 */
@Composable
internal fun TopStoriesSection(
    topStories: List<Article>,
    preferredLayout: String,
    onArticleClick: (Article) -> Unit,
    onArticleLongClick: (Article) -> Unit,
) {
    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { topStories.size.coerceAtMost(TOP_STORIES_COUNT) },
    )

    // Auto-advance the hero carousel except in dense list-style layouts.
    if (preferredLayout != "compact" && preferredLayout != "text-under") {
        LaunchedEffect(pagerState) {
            while (true) {
                delay(HERO_AUTO_ADVANCE_DELAY_MS)
                if (pagerState.pageCount > 0) {
                    val nextPage = (pagerState.currentPage + 1) % pagerState.pageCount
                    pagerState.animateScrollToPage(nextPage)
                }
            }
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth(),
        ) { page ->
            val article = topStories[page]
            when (preferredLayout) {
                "compact" -> CardArticle(
                    article = article,
                    onReadFullStoryClicked = { onArticleClick(article) },
                )
                "text-under" -> WeaverTextUnderCard(
                    article = article,
                    category = "Top Stories",
                    onClick = { onArticleClick(article) },
                    onLongClick = { onArticleLongClick(article) },
                )
                "magazine" -> WeaverMagazineCard(
                    article = article,
                    category = "Top Stories",
                    onClick = { onArticleClick(article) },
                    onLongClick = { onArticleLongClick(article) },
                )
                "coverflow" -> WeaverCoverFlowCard(
                    article = article,
                    category = "Top Stories",
                    onClick = { onArticleClick(article) },
                    onLongClick = { onArticleLongClick(article) },
                    scrollOffset = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction,
                )
                "masonry" -> WeaverMasonryCard(
                    article = article,
                    category = "Top Stories",
                    onClick = { onArticleClick(article) },
                    onLongClick = { onArticleLongClick(article) },
                    index = page,
                )
                else -> HeroTopStoryCard(
                    article = article,
                    onClick = { onArticleClick(article) },
                    onLongClick = { onArticleLongClick(article) },
                )
            }
        }

        Spacer(modifier = Modifier.height(Dimens.PaddingMediumSmall))

        PagerIndicator(
            pageCount = pagerState.pageCount,
            currentPage = pagerState.currentPage,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )
    }
}

/**
 * Row of dots (one per top story). The dot for the currently visible story is
 * highlighted with the accent color; the rest stay dimmed.
 */
@Composable
private fun PagerIndicator(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(Dimens.PaddingSmall),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(pageCount) { index ->
            val isSelected = index == currentPage
            Box(
                modifier = Modifier
                    .size(if (isSelected) Dimens.PaddingMediumSmall else Dimens.PaddingSmall)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) {
                            WeaverPrimary
                        } else {
                            WeaverTextSecondary.copy(alpha = AppConstants.AlphaMedium)
                        },
                    ),
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HeroTopStoryCard(
    article: Article,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .padding(horizontal = Dimens.PaddingMedium)
            .fillMaxWidth()
            .height(Dimens.HeroCardHeight)
            .clip(RoundedCornerShape(Dimens.RadiusExtraLarge))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
            ),
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(article.image)
                .crossfade(true)
                .build(),
            contentDescription = article.title,
            contentScale = ContentScale.Crop,
            placeholder = painterResource(R.drawable.ic_logo),
            error = painterResource(R.drawable.ic_logo),
            modifier = Modifier.fillMaxSize(),
        )
        // Bottom scrim so the white overlay text stays readable.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = AppConstants.AlphaLow),
                            Color.Black.copy(alpha = AppConstants.AlphaScrim),
                        ),
                        startY = 220f,
                    ),
                ),
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(horizontal = Dimens.PaddingExtraLarge, vertical = Dimens.PaddingExtraLarge),
        ) {
            Text(
                text = "Top Stories",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )
            Spacer(modifier = Modifier.height(Dimens.PaddingSmall))
            Box(
                modifier = Modifier
                    .width(Dimens.RadiusCircular)
                    .height(Dimens.PaddingMicro + 1.dp)
                    .clip(RoundedCornerShape(Dimens.RadiusTiny))
                    .background(Color.White),
            )
            Spacer(modifier = Modifier.height(Dimens.PaddingMediumSmall))
            Text(
                text = article.title,
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                maxLines = AppConstants.MaxLinesTitle,
                overflow = TextOverflow.Ellipsis,
            )
            article.description?.takeIf { it.isNotBlank() }?.let { excerpt ->
                Spacer(modifier = Modifier.height(Dimens.PaddingMediumSmall))
                Text(
                    text = excerpt,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = AppConstants.AlphaHigh),
                    maxLines = AppConstants.MaxLinesDescription,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun OtherStoryRow(
    article: Article,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    val date = shortDateFormat(article.publishedAt)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
            )
            .padding(horizontal = Dimens.PaddingMedium, vertical = Dimens.PaddingMediumSmall),
        verticalAlignment = Alignment.Top,
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(article.image)
                .crossfade(true)
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            placeholder = painterResource(R.drawable.ic_logo),
            error = painterResource(R.drawable.ic_logo),
            modifier = Modifier
                .size(Dimens.RadiusFull)
                .clip(RoundedCornerShape(Dimens.RadiusSmall)),
        )

        Spacer(modifier = Modifier.width(Dimens.PaddingMedium))

        Column(
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = article.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = AppConstants.MaxLinesTitle,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(Dimens.PaddingSmall))
            Text(
                text = article.description ?: article.source.name.orEmpty(),
                style = MaterialTheme.typography.bodySmall,
                color = WeaverTextSecondary,
                maxLines = AppConstants.MaxLinesTitle,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(Dimens.PaddingStandard))
            Text(
                text = "— $date",
                style = MaterialTheme.typography.labelMedium,
                color = WeaverTextSecondary.copy(alpha = AppConstants.AlphaMedium),
            )
        }
    }
}

@Composable
internal fun ErrorContent(
    message: String,
    onRetry: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Dimens.PaddingDoubleExtraLarge),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = message,
            color = ErrorTextColor,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(Dimens.PaddingMedium))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = WeaverPrimary),
        ) {
            Text(text = "Retry", color = Color.White)
        }
    }
}
