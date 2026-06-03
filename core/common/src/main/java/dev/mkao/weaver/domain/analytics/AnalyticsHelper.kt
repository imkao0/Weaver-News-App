package dev.mkao.weaver.domain.analytics

/** Analytics logging decoupled from the underlying provider SDK. */
interface AnalyticsHelper {
    fun logEvent(event: AnalyticsEvent)
}
