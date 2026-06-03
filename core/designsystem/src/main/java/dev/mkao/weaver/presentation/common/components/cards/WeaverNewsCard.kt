package dev.mkao.weaver.presentation.common.components.cards

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import dev.mkao.weaver.designsystem.R
import dev.mkao.weaver.domain.model.Article
import dev.mkao.weaver.presentation.common.components.feedback.ChipRow
import dev.mkao.weaver.presentation.common.components.feedback.SourceChip
import dev.mkao.weaver.util.AppConstants
import dev.mkao.weaver.util.Dimens
import dev.mkao.weaver.util.shortDateFormat

/**
 * Home-feed card displaying a full-bleed image with a bottom gradient scrim,
 * category, source chips, and headline text.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WeaverNewsCard(
    article: Article,
    category: String,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .clip(RoundedCornerShape(Dimens.RadiusExtraLarge))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
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


        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.45f to Color.Transparent,
                            1f to Color.Black.copy(alpha = AppConstants.AlphaScrimVideo),
                        ),
                    ),
                ),
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(horizontal = Dimens.PaddingMediumLarge, vertical = Dimens.PaddingMedium),
        ) {
            ChipRow(
                category = category,
                source = article.source.name?.take(MAX_SOURCE_CHIP_CHARS)
                    ?: article.source.url?.take(MAX_SOURCE_CHIP_CHARS)
                    ?: "News",
            )
            Spacer(modifier = Modifier.height(Dimens.PaddingSmall))
            Text(
                text = article.title,
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                maxLines = AppConstants.MaxLinesTitle,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/** Card variant with the text and chips below the image. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WeaverTextUnderCard(
    article: Article,
    category: String,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Dimens.RadiusExtraLarge))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
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
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(Dimens.RadiusExtraLarge)),
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.PaddingMedium),
        ) {
            ChipRow(
                category = category,
                source = article.source.name?.take(MAX_SOURCE_CHIP_CHARS)
                    ?: article.source.url?.take(MAX_SOURCE_CHIP_CHARS)
                    ?: "News",
            )
            Spacer(modifier = Modifier.height(Dimens.PaddingSmall))
            Text(
                text = article.title,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = AppConstants.MaxLinesTitle,
                overflow = TextOverflow.Ellipsis,
            )
            article.description?.takeIf { it.isNotBlank() }?.let { excerpt ->
                Spacer(modifier = Modifier.height(Dimens.PaddingSmall))
                Text(
                    text = excerpt,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    maxLines = AppConstants.MaxLinesDescription,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

private const val MAX_SOURCE_CHIP_CHARS = 24

/**
 * Magazine style: edge-to-edge image with an editorial text block below —
 * big serif-like headline, multi-line excerpt and a meta row. Resembles a
 * feature story in a print magazine.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WeaverMagazineCard(
    article: Article,
    category: String,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Dimens.RadiusMedium))
            .background(scheme.surfaceVariant)
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
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(4f / 3f),
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = Dimens.PaddingMediumLarge,
                    vertical = Dimens.PaddingMedium,
                ),
        ) {
            ChipRow(
                category = category,
                source = article.source.name?.take(MAX_SOURCE_CHIP_CHARS)
                    ?: article.source.url?.take(MAX_SOURCE_CHIP_CHARS)
                    ?: "News",
            )
            Spacer(modifier = Modifier.height(Dimens.PaddingMediumSmall))
            Text(
                text = article.title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = scheme.onSurface,
                maxLines = AppConstants.MaxLinesDescription,
                overflow = TextOverflow.Ellipsis,
            )
            article.description?.takeIf { it.isNotBlank() }?.let { excerpt ->
                Spacer(modifier = Modifier.height(Dimens.PaddingSmall))
                Text(
                    text = excerpt,
                    style = MaterialTheme.typography.bodyMedium,
                    color = scheme.onSurfaceVariant,
                    maxLines = AppConstants.MaxLinesDescription,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(modifier = Modifier.height(Dimens.PaddingMediumSmall))
            Text(
                text = shortDateFormat(article.publishedAt),
                style = MaterialTheme.typography.labelMedium,
                color = scheme.onSurfaceVariant.copy(alpha = AppConstants.AlphaMedium),
            )
        }
    }
}

private const val COVER_FLOW_ROTATION = 8f
private const val COVER_FLOW_SCALE = 0.94f
private const val MASONRY_MIN_ASPECT = 0.72f
private const val MASONRY_ASPECT_STEP = 0.16f
private const val MASONRY_ASPECT_VARIANTS = 3
private const val ENTRANCE_ANIM_DURATION_MS = 480
private const val ENTRANCE_STAGGER_MS = 60

/**
 * Cover Flow style: a 3D carousel card with perspective tilt, parallax depth
 * and a glassmorphic text panel floating over a full-bleed hero image. The
 * card subtly rotates/scales based on its horizontal scroll offset, giving a
 * physical "flipping through covers" feel. Entrances animate with a
 * fade + rise.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WeaverCoverFlowCard(
    article: Article,
    category: String,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    scrollOffset: Float = 0f,
) {
    val scheme = MaterialTheme.colorScheme

    // 3D tilt + parallax derived from the card's scroll offset.
    val rotationY = (scrollOffset * COVER_FLOW_ROTATION).coerceIn(-COVER_FLOW_ROTATION, COVER_FLOW_ROTATION)
    val scale = 1f - (kotlin.math.abs(scrollOffset) * (1f - COVER_FLOW_SCALE)).coerceIn(0f, 1f - COVER_FLOW_SCALE)
    val imageParallax = scrollOffset * 40f

    // Animated entrance (fade + rise).
    val entrance = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        entrance.animateTo(1f, tween(durationMillis = ENTRANCE_ANIM_DURATION_MS))
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(4f / 5f)
            .graphicsLayer {
                this.rotationY = rotationY
                this.scaleX = scale
                this.scaleY = scale
                cameraDistance = 16f * density
                alpha = 0.35f + 0.65f * entrance.value
                translationY = (1f - entrance.value) * 60f
            }
            .clip(RoundedCornerShape(Dimens.RadiusExtraLarge + 8.dp))
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
    ) {
        // Parallax hero image
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(article.image)
                .crossfade(true)
                .build(),
            contentDescription = article.title,
            contentScale = ContentScale.Crop,
            placeholder = painterResource(R.drawable.ic_logo),
            error = painterResource(R.drawable.ic_logo),
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { translationX = imageParallax },
        )

        // Diagonal cinematic gradient scrim.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colorStops = arrayOf(
                            0.0f to Color.Transparent,
                            0.5f to Color.Black.copy(alpha = AppConstants.AlphaLow),
                            1f to Color.Black.copy(alpha = AppConstants.AlphaScrim),
                        ),
                    ),
                ),
        )

        // Glassmorphic floating panel.
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(Dimens.PaddingMediumLarge)
                .clip(RoundedCornerShape(Dimens.RadiusExtraLarge))
                .background(Color.Black.copy(alpha = AppConstants.AlphaLow))
                .padding(Dimens.PaddingMediumLarge),
        ) {
            ChipRow(
                category = category,
                source = article.source.name?.take(MAX_SOURCE_CHIP_CHARS)
                    ?: article.source.url?.take(MAX_SOURCE_CHIP_CHARS)
                    ?: "News",
            )
            Spacer(modifier = Modifier.height(Dimens.PaddingSmall))
            Text(
                text = article.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = AppConstants.MaxLinesTitle,
                overflow = TextOverflow.Ellipsis,
            )
            article.description?.takeIf { it.isNotBlank() }?.let { excerpt ->
                Spacer(modifier = Modifier.height(Dimens.PaddingSmall))
                Text(
                    text = excerpt,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = AppConstants.AlphaHigh),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        // Accent edge light on the leading edge for the "cover" feel.
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .width(Dimens.PaddingMicro + 2.dp)
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            scheme.primary.copy(alpha = AppConstants.AlphaHigh),
                            Color.Transparent,
                        ),
                    ),
                ),
        )
    }
}

/**
 * Masonry Grid style: a staggered hero-image card whose height varies
 * deterministically per-article (producing the masonry rhythm), with a bold
 * gradient overlay, a floating source badge, and an animated staggered
 * scale/fade entrance.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WeaverMasonryCard(
    article: Article,
    category: String,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    index: Int = 0,
) {
    val scheme = MaterialTheme.colorScheme

    // Deterministic per-article height variation -> staggered masonry rhythm.
    val variant = kotlin.math.abs(article.url.hashCode()) % MASONRY_ASPECT_VARIANTS
    val aspect = MASONRY_MIN_ASPECT + variant * MASONRY_ASPECT_STEP

    // Staggered entrance animation (scale + fade), offset by position.
    val entrance = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay((index % 6) * ENTRANCE_STAGGER_MS.toLong())
        entrance.animateTo(1f, tween(durationMillis = ENTRANCE_ANIM_DURATION_MS))
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(aspect)
            .graphicsLayer {
                alpha = entrance.value
                val s = 0.92f + 0.08f * entrance.value
                scaleX = s
                scaleY = s
            }
            .clip(RoundedCornerShape(Dimens.RadiusExtraLarge))
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
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

        // Bold vertical gradient overlay.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.0f to Color.Black.copy(alpha = AppConstants.AlphaLow),
                            0.45f to Color.Transparent,
                            1f to Color.Black.copy(alpha = AppConstants.AlphaScrimVideo),
                        ),
                    ),
                ),
        )

        // Floating source badge (top-start).
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(Dimens.PaddingMedium)
                .clip(RoundedCornerShape(Dimens.RadiusCircular))
                .background(scheme.primary.copy(alpha = AppConstants.AlphaHigh))
                .padding(horizontal = Dimens.PaddingMediumSmall, vertical = Dimens.PaddingTiny),
        ) {
            Text(
                text = (article.source.name ?: category).take(MAX_SOURCE_CHIP_CHARS),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        // Bottom headline block over the gradient.
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(Dimens.PaddingMediumLarge),
        ) {
            Text(
                text = category,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color.White.copy(alpha = AppConstants.AlphaHigh),
            )
            Spacer(modifier = Modifier.height(Dimens.PaddingTiny))
            Text(
                text = article.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = AppConstants.MaxLinesDescription,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
