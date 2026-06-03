package dev.mkao.weaver.domain.analytics

/**
 * Crash reporting decoupled from the Firebase SDK so ViewModels and
 * repositories stay unit-testable with fakes.
 */
interface CrashlyticsHelper {

    /** Records a breadcrumb that is attached to the next crash report. */
    fun log(message: String)

    /** Reports a non-fatal exception to the crash dashboard. */
    fun recordException(e: Throwable)
}
