package dev.mkao.weaver.util.analytics

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import dev.mkao.weaver.domain.analytics.AnalyticsEvent
import dev.mkao.weaver.domain.analytics.AnalyticsHelper
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseAnalyticsHelper @Inject constructor(
    private val firebaseAnalytics: FirebaseAnalytics,
) : AnalyticsHelper {

    override fun logEvent(event: AnalyticsEvent) {
        val bundle = Bundle().apply {
            event.extras.forEach { parameter ->
                putString(parameter.key, parameter.value)
            }
        }
        firebaseAnalytics.logEvent(event.type, bundle)
    }
}
