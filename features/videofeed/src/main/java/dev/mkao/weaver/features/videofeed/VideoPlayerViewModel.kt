package dev.mkao.weaver.features.videofeed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.mkao.weaver.data.preferences.UserPreferencesRepository
import dev.mkao.weaver.domain.model.VideoItem
import dev.mkao.weaver.domain.repository.VideoRepository
import dev.mkao.weaver.util.AppConstants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject

data class VideoPlayerState(
    val videos: List<VideoItem> = emptyList(),
    val isLoading: Boolean = true,
    val favoriteVideoIds: Set<String> = emptySet(),
)

/**
 * Backs the full-screen [VideoPlayerScreen]. Shares the same
 * [VideoRepository] feed as the grid so swiping vertically pages through the
 * same list, and persists favorite toggles via DataStore.
 */
@HiltViewModel
class VideoPlayerViewModel @Inject constructor(
    private val videoRepository: VideoRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)

    val state: StateFlow<VideoPlayerState> = combine(
        videoRepository.getVideos(),
        userPreferencesRepository.favoriteVideoIds,
        _isLoading,
    ) { videos, favorites, isLoading ->
        VideoPlayerState(
            videos = videos,
            favoriteVideoIds = favorites,
            isLoading = isLoading,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(AppConstants.STATE_FLOW_TIMEOUT_MS),
        initialValue = VideoPlayerState(isLoading = true),
    )

    init {
        ensureData()
    }

    private fun ensureData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                videoRepository.refreshVideos()
            } catch (e: IOException) {
                // Network failure: keep showing cached videos instead.
                Timber.tag(TAG).w(e, "Video refresh failed; using cached videos.")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun toggleFavorite(videoId: String) {
        viewModelScope.launch {
            userPreferencesRepository.toggleFavoriteVideo(videoId)
        }
    }

    private companion object {
        const val TAG = "VideoPlayerViewModel"
    }
}
