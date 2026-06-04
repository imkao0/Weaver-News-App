package dev.mkao.weaver.features.videofeed

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import dev.mkao.weaver.R
import dev.mkao.weaver.domain.model.VideoItem
import dev.mkao.weaver.presentation.common.components.feedback.ArticleCardShimmerEffect
import dev.mkao.weaver.presentation.common.components.feedback.WeaverHeader
import dev.mkao.weaver.presentation.common.theme.WeaverPrimary
import dev.mkao.weaver.presentation.common.theme.WeaverTextSecondary
import dev.mkao.weaver.util.AppConstants
import dev.mkao.weaver.util.Dimens

private const val SHIMMER_PLACEHOLDER_COUNT = 4
private const val VIDEO_GRID_COLUMNS = 2
private const val SCRIM_START_FRACTION = 0.60f
private val RETRY_BUTTON_TEXT_COLOR
    get() = Color(RETRY_BUTTON_TEXT_ARGB)
private const val RETRY_BUTTON_TEXT_ARGB = 0xFF0B3C4E

/**
 * Video feed tab: centered wordmark header and a 2-column grid of rounded
 * video cards with a bottom scrim, bold channel handle and a truncated
 * caption.
 *
 * Tapping a card opens the in-app player.
 */
@Composable
fun VideoFeedScreen(
    onVideoClick: (VideoItem) -> Unit = {},
    viewModel: VideoFeedViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            WeaverHeader(
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(top = Dimens.PaddingLarge + Dimens.PaddingMicro, bottom = Dimens.PaddingMedium),
            )
        },
    ) { paddingValues ->
        when {
            state.isLoading && state.videos.isEmpty() -> {
                VideoFeedLoading(modifier = Modifier.padding(paddingValues))
            }
            state.videos.isEmpty() -> {
                VideoFeedEmpty(
                    onRetry = viewModel::refresh,
                    modifier = Modifier.padding(paddingValues),
                )
            }
            else -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(VIDEO_GRID_COLUMNS),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(
                        start = Dimens.PaddingMedium,
                        end = Dimens.PaddingMedium,
                        top = Dimens.PaddingMedium,
                        bottom = Dimens.PaddingLarge,
                    ),
                    verticalArrangement = Arrangement.spacedBy(Dimens.PaddingMedium),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.PaddingMedium),
                ) {
                    items(state.videos, key = { it.videoId }) { video ->
                        VideoCard(
                            video = video,
                            onClick = { onVideoClick(video) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun VideoCard(
    video: VideoItem,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(Dimens.VideoCardAspectRatio)
            .clip(RoundedCornerShape(Dimens.RadiusLarge))
            .clickable(onClick = onClick),
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(video.thumbnailUrl)
                .crossfade(true)
                .build(),
            contentDescription = video.title,
            contentScale = ContentScale.Crop,
            placeholder = painterResource(R.drawable.placeholder_image),
            error = painterResource(R.drawable.placeholder_image),
            modifier = Modifier.fillMaxSize(),
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            SCRIM_START_FRACTION to Color.Transparent,
                            1f to Color.Black.copy(alpha = AppConstants.AlphaScrimVideo),
                        ),
                    ),
                ),
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = Dimens.PaddingMedium, end = Dimens.PaddingMedium, bottom = Dimens.PaddingMediumLarge),
        ) {
            Text(
                text = video.channelName,
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.padding(top = Dimens.PaddingMicro))
            Text(
                text = video.title,
                color = Color.White.copy(alpha = 0.95f),
                style = MaterialTheme.typography.labelMedium,
                maxLines = AppConstants.MaxLinesTitle,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun VideoFeedLoading(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(Dimens.PaddingExtraLarge),
        verticalArrangement = Arrangement.spacedBy(Dimens.PaddingLarge),
    ) {
        repeat(SHIMMER_PLACEHOLDER_COUNT) {
            ArticleCardShimmerEffect(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            )
        }
    }
}

@Composable
private fun VideoFeedEmpty(onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(R.string.no_videos_available),
                color = WeaverTextSecondary,
            )
            Button(
                onClick = onRetry,
                modifier = Modifier.padding(top = Dimens.PaddingLarge),
                colors = ButtonDefaults.buttonColors(
                    containerColor = WeaverPrimary,
                ),
            ) {
                Text(
                    text = stringResource(R.string.retry),
                    color = RETRY_BUTTON_TEXT_COLOR,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}
