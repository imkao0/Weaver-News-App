package dev.mkao.weaver.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dev.mkao.weaver.data.preferences.UserPreferencesRepository
import dev.mkao.weaver.domain.repository.BookmarksRepository
import dev.mkao.weaver.domain.repository.HeadlinesRepository
import dev.mkao.weaver.util.NotificationHelper
import dev.mkao.weaver.util.Result
import kotlinx.coroutines.flow.first
import timber.log.Timber
import java.io.IOException

/**
 * Periodic worker that checks for fresh headlines and posts a notification
 * when new articles arrive.
 */
@HiltWorker
class RefreshNewsWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted workerParameters: WorkerParameters,
    private val repository: HeadlinesRepository,
    private val bookmarksRepository: BookmarksRepository,
    private val preferencesRepository: UserPreferencesRepository,
) : CoroutineWorker(appContext, workerParameters) {

    override suspend fun doWork(): Result {
        Timber.tag(TAG).d("Starting $WORK_NAME.")
        return try {
            // Throttle redundant background syncs via the BACKGROUND_REFRESH
            // freshness ledger so the periodic worker only hits the network
            // once the 6-hour window has elapsed.
            if (!repository.shouldRunBackgroundRefresh()) {
                Timber.tag(TAG).d("Background refresh still fresh; skipping fetch.")
                return Result.success()
            }

            // Use the user's selected edition country (e.g. "sg"), not the
            // device locale, so background refreshes match the visible feed.
            val countryCode = preferencesRepository.countryCode.first()
            val languageCode = preferencesRepository.languageCode.first()
            val result = repository.getTopHeadlines(countryCode, "general", languageCode)

            if (result is dev.mkao.weaver.util.Result.Success) {
                repository.markBackgroundRefreshed()
                val articles = result.data
                val bookmarkedUrls = bookmarksRepository.getBookedArticlesStream()
                    .first()
                    .map { it.url }
                    .toSet()
                val newArticles = articles.filter { it.url !in bookmarkedUrls }

                if (newArticles.isNotEmpty()) {
                    NotificationHelper.createNotificationChannel(appContext)
                    NotificationHelper.showNewsNotification(appContext, newArticles.first())
                    Timber.tag(TAG).i("Posted notification for ${newArticles.size} new articles.")
                }
            }
            Result.success()
        } catch (e: IOException) {
            Timber.tag(TAG).e(e, "$WORK_NAME failed.")
            if (runAttemptCount < MAX_RETRIES) Result.retry() else Result.failure()
        }
    }

    companion object {
        const val WORK_NAME = "weaver_refresh_news"
        private const val TAG = "RefreshNewsWorker"
        private const val MAX_RETRIES = 3
    }
}
