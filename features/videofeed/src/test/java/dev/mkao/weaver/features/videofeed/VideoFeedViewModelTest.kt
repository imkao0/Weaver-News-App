package dev.mkao.weaver.features.videofeed

import app.cash.turbine.test
import dev.mkao.weaver.data.repository.fakes.FakeVideoRepository
import dev.mkao.weaver.domain.model.VideoItem
import dev.mkao.weaver.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class VideoFeedViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var videoRepository: FakeVideoRepository
    private lateinit var viewModel: VideoFeedViewModel

    @Before
    fun setup() {
        videoRepository = FakeVideoRepository()
    }

    @Test
    fun `feed populates with videos from repository flow`() = runTest {
        val mockVideos = listOf(
            VideoItem(
                videoId = "videoId",
                title = "title",
                channelName = "channel",
                thumbnailUrl = "thumb",
                watchUrl = "watch",
                publishedAt = "2026-09-15T12:00:00Z",
                isPlayable = true,
            ),
        )
        videoRepository.setVideos(mockVideos)

        viewModel = VideoFeedViewModel(videoRepository)
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            // Initial state might be loading or the one with videos depending on how fast combine works
            if (state.videos.isEmpty()) {
                val next = awaitItem()
                assertEquals(mockVideos, next.videos)
            } else {
                assertEquals(mockVideos, state.videos)
            }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `feed surfaces error when refresh fails and cache is empty`() = runTest {
        videoRepository.shouldFailRefresh = true
        videoRepository.setVideos(emptyList())

        viewModel = VideoFeedViewModel(videoRepository)
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        viewModel.state.test {
            var state = awaitItem()
            // Wait for refresh to fail and error to be set
            while (state.error == null && state.isLoading) {
                state = awaitItem()
            }
            assertEquals("Refresh failed", state.error)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
