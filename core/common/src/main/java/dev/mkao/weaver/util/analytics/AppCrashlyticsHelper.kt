package dev.mkao.weaver.util.analytics

import com.google.firebase.crashlytics.FirebaseCrashlytics
import dev.mkao.weaver.domain.analytics.CrashlyticsHelper
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firebase-backed [CrashlyticsHelper]. Uses the [FirebaseCrashlytics]
 * singleton initialized by the google-services plugin.
 */
@Singleton
class AppCrashlyticsHelper @Inject constructor() : CrashlyticsHelper {

    override fun log(message: String) {
        FirebaseCrashlytics.getInstance().log(message)
    }

    override fun recordException(e: Throwable) {
        FirebaseCrashlytics.getInstance().recordException(e)
    }
}
