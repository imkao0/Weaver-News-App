package dev.mkao.weaver.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/**
 * Type-safe Navigation 3 routes. The article detail is passed by **key**
 * (`articleUrl`) and re-fetched from the Room cache, replacing the fragile
 * shared-ViewModel-state handoff.
 *
 * Every destination is a [NavKey] so it can be stored in Nav3's
 * [androidx.navigation3.runtime.NavBackStack] and saved/restored via
 * kotlinx-serialization.
 */
sealed interface Destinations : NavKey {

    @Serializable
    data object Onboarding : Destinations

    @Serializable
    data object Bulletin : Destinations

    @Serializable
    data object VideoFeed : Destinations

    @Serializable
    data object Search : Destinations

    @Serializable
    data object Categories : Destinations

    @Serializable
    data object Discover : Destinations

    @Serializable
    data object Bookmarks : Destinations

    @Serializable
    data object Settings : Destinations

    @Serializable
    data object About : Destinations

    @Serializable
    data object LanguageEditions : Destinations

    @Serializable
    data object BlockedSources : Destinations

    @Serializable
    data class ArticleDetail(val articleUrl: String, val title: String) : Destinations

    @Serializable
    data class VideoPlayer(val initialVideoId: String) : Destinations
}

/**
 * Deep-link request to open an article straight into the detail screen.
 * Delivered to the app by the home-screen widget (and future shortcuts /
 * notifications) via [dev.mkao.weaver.features.widget.WidgetProvider.ACTION_OPEN_ARTICLE].
 */
data class ArticleDeepLink(val articleUrl: String, val title: String)
