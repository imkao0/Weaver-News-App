package dev.mkao.weaver.features.details

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.rememberAsyncImagePainter
import dev.mkao.weaver.presentation.common.components.feedback.sourceChipColor
import dev.mkao.weaver.features.details.DetailEvent
import dev.mkao.weaver.util.dashedDateFormat
import timber.log.Timber

private val BookmarkedTint
    get() = Color(BOOKMARKED_TINT_ARGB)

private const val BOOKMARKED_TINT_ARGB = 0xFF2EE6A8

/** Fraction of the hero image the reader must scroll before the header pins. */
private const val STICKY_HEADER_SCROLL_THRESHOLD = 0.9f

@Composable
fun DetailScreen(
    articleUrl: String,
    onBackPressed: () -> Unit,
    viewModel: DetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(articleUrl) {
        viewModel.loadArticle(articleUrl)
    }

    CollectShareEvents(viewModel, context)

    val article = state.selectedArticle

    when {
        state.isLoading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        article == null -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(state.error ?: "Article not found")
            }
        }

        else -> {
            val isBookmarked by viewModel.isBookmarked(article.url)
                .collectAsStateWithLifecycle(initialValue = article.isBookedMarked)

            // API-provided body: full text -> summary -> description.
            val body = listOfNotNull(article.content, article.description)
                .firstOrNull { it.isNotBlank() }
                .orEmpty()

            val scrollState = rememberScrollState()
            val density = LocalDensity.current

            val headerHeightPx = with(density) { 340.dp.toPx() }
            val imageAlpha = if (headerHeightPx > 0) {
                (1f - (scrollState.value / (headerHeightPx / 2f))).coerceIn(0f, 1f)
            } else {
                1f
            }

            /**
             * Once the reader has scrolled through ~90% of the hero image, pin the
             * article title + source to the top of the screen so they stay visible
             * while the description keeps scrolling underneath.
             */
            val stickyThresholdPx = headerHeightPx * STICKY_HEADER_SCROLL_THRESHOLD
            val stickyHeaderProgress by animateFloatAsState(
                targetValue = if (scrollState.value >= stickyThresholdPx) 1f else 0f,
                label = "stickyHeaderProgress",
            )

            Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(bottom = 80.dp),
                ) {
                    DetailHeader(
                        imageUrl = article.image,
                        imageAlpha = imageAlpha,
                        onBackPressed = onBackPressed,
                    )

                    DetailBody(
                        article = article,
                        body = body,
                        contentFadeAlpha = imageAlpha,
                        onReadFullArticle = { openInCustomTab(context, article.url) },
                    )
                }

                // Pinned title + source — slides in from the top at 90% scroll and
                // stays put for the rest of the article.
                StickyArticleHeader(
                    title = article.title,
                    sourceName = article.source.name,
                    progress = stickyHeaderProgress,
                    onBackPressed = onBackPressed,
                    modifier = Modifier.align(Alignment.TopCenter),
                )

                DetailActionBar(
                    isBookmarked = isBookmarked,
                    onBlockSource = {
                        article.source.name?.let {
                            viewModel.onEvent(DetailEvent.BlockSource(it))
                            onBackPressed()
                        }
                    },
                    onShare = { viewModel.onEvent(DetailEvent.ShareArticle(article)) },
                    onToggleBookmark = { viewModel.onEvent(DetailEvent.ToggleBookmark(article)) },
                    modifier = Modifier.align(Alignment.BottomCenter),
                )
            }
        }
    }
}

@Composable
private fun CollectShareEvents(viewModel: DetailViewModel, context: android.content.Context) {
    // Collect one-shot share events from the ViewModel.
    LaunchedEffect(Unit) {
        viewModel.uiEvents.collect { event ->
            when (event) {
                is DetailUiEvent.ShareArticle -> {
                    val shareText = "Have you read this? ${event.article.title}\n${event.article.url}"
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_SUBJECT, event.article.title)
                        putExtra(Intent.EXTRA_TEXT, shareText)
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Share Article"))
                }
            }
        }
    }
}

/**
 * Opens the original article in a Chrome Custom Tab, falling back to a plain
 * ACTION_VIEW intent when no browser handles Custom Tabs.
 */
private fun openInCustomTab(context: android.content.Context, url: String) {
    val uri = Uri.parse(url)
    try {
        CustomTabsIntent.Builder().build().launchUrl(context, uri)
    } catch (e: ActivityNotFoundException) {
        Timber.tag("DetailScreen").w(e, "No Custom Tab handler; falling back to ACTION_VIEW")
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, uri))
        } catch (fallbackError: ActivityNotFoundException) {
            Timber.tag("DetailScreen").e(fallbackError, "No browser available for %s", url)
        }
    }
}

@Composable
private fun DetailHeader(imageUrl: String?, imageAlpha: Float, onBackPressed: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(340.dp),
    ) {
        val imagePainter = rememberAsyncImagePainter(imageUrl)
        Image(
            painter = imagePainter,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = imageAlpha,
        )

        // Minimal translucent back capsule
        Row(
            modifier = Modifier
                .statusBarsPadding()
                .padding(16.dp)
                .clip(RoundedCornerShape(50.dp))
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f))
                .padding(horizontal = 14.dp, vertical = 8.dp)
                .clickable { onBackPressed() },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(18.dp),
            )
            Text(
                text = "Back",
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

/**
 * Pinned reading header — appears once the reader has scrolled 90% of the hero
 * image and keeps the article title and source on screen while the description
 * scrolls underneath. [progress] animates the slide/fade-in and the header is
 * fully hidden (and non-interactive) when [progress] is 0.
 */
@Composable
private fun StickyArticleHeader(
    title: String,
    sourceName: String?,
    progress: Float,
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (progress <= 0f) return

    val density = LocalDensity.current
    val hiddenOffsetPx = with(density) { 96.dp.toPx() }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                alpha = progress
                translationY = -hiddenOffsetPx * (1f - progress)
            }
            .shadow(6.dp),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBackPressed) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
            ) {
                Text(
                    text = title,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 16.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                if (!sourceName.isNullOrBlank()) {
                    Box(
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .clip(RoundedCornerShape(30.dp))
                            .background(sourceChipColor(sourceName))
                            .padding(horizontal = 10.dp, vertical = 3.dp),
                    ) {
                        Text(
                            text = sourceName,
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailBody(
    article: dev.mkao.weaver.domain.model.Article,
    body: String,
    contentFadeAlpha: Float,
    onReadFullArticle: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 24.dp, vertical = 24.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // Source chip keeps its varied per-source color; the tag chip fades
            // with the hero image as before.
            article.source.name?.let { sourceName ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(30.dp))
                        .background(sourceChipColor(sourceName))
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                ) {
                    Text(
                        text = sourceName,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(30.dp))
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = contentFadeAlpha),
                    )
                    .padding(horizontal = 14.dp, vertical = 6.dp),
            ) {
                Text(
                    text = "News",
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                        alpha = contentFadeAlpha,
                    ),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                )
            }
        }

        Text(
            text = article.title,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 28.sp,
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.ExtraBold,
            lineHeight = 36.sp,
            modifier = Modifier.padding(bottom = 20.dp),
        )

        DetailAuthorRow(article = article)

        Text(
            text = body,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 16.sp,
            fontFamily = FontFamily.SansSerif,
            lineHeight = 26.sp,
            textAlign = TextAlign.Start,
            modifier = Modifier.padding(bottom = 24.dp),
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 1.dp)
        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = onReadFullArticle,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
            shape = RoundedCornerShape(12.dp),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text(
                text = "Read Full Article",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
            )
        }
    }
}

@Composable
private fun DetailAuthorRow(article: dev.mkao.weaver.domain.model.Article) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = 28.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onPrimary),
            )
        }
        Column {
            Text(
                text = article.author ?: article.source.name ?: "Publisher",
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = dashedDateFormat(article.publishedAt),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
            )
        }
    }
}

@Composable
private fun DetailActionBar(
    isBookmarked: Boolean,
    onBlockSource: () -> Unit,
    onShare: () -> Unit,
    onToggleBookmark: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(8.dp),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            // Block Source Button (Bottom Start)
            IconButton(onClick = onBlockSource) {
                Icon(
                    imageVector = Icons.Default.Block,
                    contentDescription = "Block Source",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                IconButton(onClick = onShare) {
                    Icon(Icons.Default.Share, "Share", tint = MaterialTheme.colorScheme.onSurface)
                }
                IconButton(onClick = onToggleBookmark) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Bookmark",
                        tint = if (isBookmarked) BookmarkedTint else MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}
