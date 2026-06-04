package dev.mkao.weaver.features.videofeed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.mkao.weaver.domain.model.VideoItem
import dev.mkao.weaver.domain.repository.VideoRepository
import dev.mkao.weaver.util.AppConstants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

data class VideoFeedState(
    val videos: List<VideoItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class VideoFeedViewModel @Inject constructor(
    private val videoRepository: VideoRepository,
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)

    val state: StateFlow<VideoFeedState> = combine(
        videoRepository.getVideos(),
        _isLoading,
        _error,
    ) { videos, isLoading, error ->
        VideoFeedState(
            videos = videos,
            isLoading = isLoading,
            error = if (videos.isEmpty() && error != null) error else null,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(AppConstants.STATE_FLOW_TIMEOUT_MS),
        initialValue = VideoFeedState(isLoading = true),
    )

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                videoRepository.refreshVideos()
            } catch (e: IOException) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
}
