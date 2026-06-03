package dev.mkao.weaver

import android.app.Application
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.android.material.color.DynamicColors
import dagger.hilt.android.HiltAndroidApp
import dev.mkao.weaver.util.NotificationHelper
import dev.mkao.weaver.work.RefreshNewsWorker
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class MyApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        DynamicColors.applyToActivitiesIfAvailable(this)
        NotificationHelper.createNotificationChannel(this)
        enqueuePeriodicNewsRefresh()
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(if (BuildConfig.DEBUG) Log.DEBUG else Log.INFO)
            .build()

    private fun enqueuePeriodicNewsRefresh() {
        val refreshRequest = PeriodicWorkRequestBuilder<RefreshNewsWorker>(
            REFRESH_INTERVAL_HOURS,
            TimeUnit.HOURS,
        )
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build(),
            )
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            RefreshNewsWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            refreshRequest,
        )
        Timber.tag(TAG).d("Enqueued ${RefreshNewsWorker.WORK_NAME}")
    }

    companion object {
        private const val TAG = "MyApplication"
        private const val REFRESH_INTERVAL_HOURS = 6L
    }
}
