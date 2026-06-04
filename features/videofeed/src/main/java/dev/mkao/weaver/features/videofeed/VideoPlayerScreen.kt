package dev.mkao.weaver.features.videofeed

import android.content.Intent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import dev.mkao.weaver.R
import dev.mkao.weaver.domain.model.VideoItem
import dev.mkao.weaver.util.Dimens
import dev.mkao.weaver.util.calculateElapsedTime
import kotlinx.coroutines.flow.collectLatest

private val ActionIconSize = 30.dp
private val ActionButtonSize = 52.dp
private val HeartActive
    get() = Color(HEART_ACTIVE_ARGB)
private const val HEART_ACTIVE_ARGB = 0xFFFF2D55
private const val SCRIM_TOP = 0.45f
private const val SCRIM_BOTTOM_ALPHA = 0.72f
private const val TIMESTAMP_ALPHA = 0.7f
private const val SCRIM_CHIP_ALPHA = 0.35f
private const val TITLE_ALPHA = 0.92f
private const val IFRAME_CONTROLS_DISABLED = 0
private const val IFRAME_ANNOTATIONS_HIDDEN = 3
private const val IFRAME_CAPTIONS_DISABLED = 0

/**
 * Full-screen vertical video player.
 *
 * Swiping up/down moves to the next/previous video and auto-plays the current
 * page via ExoPlayer; overlays an action rail with a favorite toggle and a
 * share action.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun VideoPlayerScreen(
    initialVideoId: String,
    onClose: () -> Unit,
    viewModel: VideoPlayerViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        when {
            state.isLoading && state.videos.isEmpty() -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.White,
                )
            }
            state.videos.isEmpty() -> {
                Text(
                    text = stringResource(R.string.no_videos_available),
                    color = Color.White,
                    modifier = Modifier.align(Alignment.Center),
                )
            }
            else -> {
                val startIndex = state.videos
                    .indexOfFirst { it.videoId == initialVideoId }
                    .coerceAtLeast(0)
                val pagerState = rememberPagerState(
                    initialPage = startIndex,
                    pageCount = { state.videos.size },
                )

                // Snap back to the tapped video once the list (re)loads.
                LaunchedEffect(state.videos, initialVideoId) {
                    snapshotFlow { state.videos }.collectLatest { videos ->
                        val target = videos.indexOfFirst { it.videoId == initialVideoId }
                        if (target >= 0 && pagerState.currentPage != target &&
                            pagerState.currentPage >= videos.size
                        ) {
                            pagerState.scrollToPage(target)
                        }
                    }
                }

                VerticalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                ) { page ->
                    VideoPage(
                        video = state.videos[page],
                        isActivePage = pagerState.settledPage == page,
                        isFavorite = state.videos[page].videoId in state.favoriteVideoIds,
                        onToggleFavorite = {
                            viewModel.toggleFavorite(state.videos[page].videoId)
                        },
                    )
                }
            }
        }

        IconButton(
            onClick = onClose,
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(Dimens.PaddingMedium)
                .size(ActionButtonSize)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.35f)),
        ) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = stringResource(R.string.close_player),
                tint = Color.White,
            )
        }
    }
}

@Composable
private fun VideoPage(
    video: VideoItem,
    isActivePage: Boolean,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        if (video.isPlayable) {
            ExoVideoPlayer(
                video = video,
                isActivePage = isActivePage,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            YouTubeVideoPlayer(
                videoId = video.videoId,
                isActivePage = isActivePage,
                modifier = Modifier.fillMaxSize(),
            )
        }

        VideoScrim()
        VideoTimestamp(publishedAt = video.publishedAt, modifier = Modifier.align(Alignment.TopEnd))
        VideoInfoColumn(video = video, modifier = Modifier.align(Alignment.BottomStart))
        VideoActionRail(
            video = video,
            isFavorite = isFavorite,
            onToggleFavorite = onToggleFavorite,
            modifier = Modifier.align(Alignment.BottomEnd),
        )
    }
}

@Composable
private fun VideoScrim() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colorStops = arrayOf(
                        SCRIM_TOP to Color.Transparent,
                        1f to Color.Black.copy(alpha = SCRIM_BOTTOM_ALPHA),
                    ),
                ),
            ),
    )
}

@Composable
private fun VideoTimestamp(publishedAt: String, modifier: Modifier = Modifier) {
    Text(
        text = calculateElapsedTime(publishedAt),
        color = Color.White.copy(alpha = TIMESTAMP_ALPHA),
        style = MaterialTheme.typography.labelMedium,
        modifier = modifier
            .statusBarsPadding()
            .padding(Dimens.PaddingMedium)
            .background(Color.Black.copy(alpha = SCRIM_CHIP_ALPHA), CircleShape)
            .padding(horizontal = 12.dp, vertical = 6.dp),
    )
}

@Composable
private fun VideoInfoColumn(video: VideoItem, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .navigationBarsPadding()
            .padding(
                start = Dimens.PaddingLarge,
                end = 88.dp,
                bottom = Dimens.PaddingDoubleExtraLarge,
            ),
    ) {
        Text(
            text = video.channelName,
            color = Color.White,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.height(Dimens.PaddingMediumSmall))
        Text(
            text = video.title,
            color = Color.White.copy(alpha = TITLE_ALPHA),
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun VideoActionRail(
    video: VideoItem,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .navigationBarsPadding()
            .padding(end = Dimens.PaddingMedium, bottom = Dimens.PaddingDoubleExtraLarge),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Dimens.PaddingDoubleExtraLarge),
    ) {
        ActionButton(
            icon = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
            tint = if (isFavorite) HeartActive else Color.White,
            contentDescription = stringResource(
                if (isFavorite) R.string.remove_from_favorites else R.string.add_to_favorites,
            ),
            onClick = onToggleFavorite,
        )
        ShareAction(video = video)
    }
}

@Composable
private fun ActionButton(
    icon: ImageVector,
    tint: Color,
    contentDescription: String,
    onClick: () -> Unit,
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(ActionButtonSize)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.35f)),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(ActionIconSize),
        )
    }
}

/** Share arrow that opens the Android share sheet. */
@Composable
private fun ShareAction(video: VideoItem) {
    val context = LocalContext.current
    ActionButton(
        icon = Icons.Filled.Send,
        tint = Color.White,
        contentDescription = stringResource(R.string.share_video),
        onClick = {
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, "${video.title}\n${video.watchUrl}")
            }
            runCatching {
                context.startActivity(
                    Intent.createChooser(sendIntent, video.title),
                )
            }
        },
    )
}

/**
 * ExoPlayer-backed page for directly playable streams (Internet Archive
 * MP4s). The player is created once per page, prepared immediately and only
 * plays while its page is the settled (active) one, so autoplay-on-open and
 * pause-on-swipe-away both work.
 */
@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
@Composable
private fun ExoVideoPlayer(
    video: VideoItem,
    isActivePage: Boolean,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    val player = remember(video.videoId) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(video.watchUrl))
            repeatMode = Player.REPEAT_MODE_ONE
            prepare()
        }
    }

    LaunchedEffect(isActivePage) {
        if (isActivePage) {
            player.play()
        } else {
            player.pause()
        }
    }

    DisposableEffect(video.videoId) {
        onDispose { player.release() }
    }

    AndroidView(
        factory = { ctx ->
            PlayerView(ctx).apply {
                useController = false
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                this.player = player
            }
        },
        update = { view ->
            view.player = player
        },
        modifier = modifier,
    )
}

/**
 * YouTube-backed page for YouTube videos. Uses the android-youtube-player
 * library to wrap the IFrame API.
 */
@Composable
private fun YouTubeVideoPlayer(
    videoId: String,
    isActivePage: Boolean,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var player: YouTubePlayer? by remember { mutableStateOf(null) }

    val playerView = remember {
        YouTubePlayerView(context).apply {
            enableAutomaticInitialization = false
            lifecycleOwner.lifecycle.addObserver(this)
            initialize(
                object : AbstractYouTubePlayerListener() {
                    override fun onReady(youTubePlayer: YouTubePlayer) {
                        player = youTubePlayer
                        if (isActivePage) {
                            youTubePlayer.loadVideo(videoId, 0f)
                        } else {
                            youTubePlayer.cueVideo(videoId, 0f)
                        }
                    }
                },
                IFramePlayerOptions.Builder(context)
                    .controls(IFRAME_CONTROLS_DISABLED)
                    .rel(IFRAME_CONTROLS_DISABLED)
                    .ivLoadPolicy(IFRAME_ANNOTATIONS_HIDDEN)
                    .ccLoadPolicy(IFRAME_CAPTIONS_DISABLED)
                    .build(),
            )
        }
    }

    LaunchedEffect(isActivePage, player) {
        player?.let {
            if (isActivePage) {
                it.play()
            } else {
                it.pause()
            }
        }
    }

    DisposableEffect(videoId) {
        onDispose {
            playerView.release()
            lifecycleOwner.lifecycle.removeObserver(playerView)
        }
    }

    AndroidView(
        factory = { playerView },
        modifier = modifier,
    )
}
