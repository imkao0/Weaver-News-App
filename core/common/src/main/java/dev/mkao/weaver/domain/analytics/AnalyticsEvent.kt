package dev.mkao.weaver.domain.analytics

sealed class AnalyticsEvent(
    val type: String,
    val extras: List<AnalyticsParameter> = emptyList(),
) {
    data class ScreenView(val screenName: String) : AnalyticsEvent(
        type = "screen_view",
        extras = listOf(AnalyticsParameter("screen_name", screenName)),
    )

    data class ArticleClick(val articleUrl: String) : AnalyticsEvent(
        type = "article_click",
        extras = listOf(AnalyticsParameter("article_url", articleUrl)),
    )

    data class CategorySelected(val category: String) : AnalyticsEvent(
        type = "category_selected",
        extras = listOf(AnalyticsParameter("category", category)),
    )

    data class SearchPerformed(val query: String) : AnalyticsEvent(
        type = "search_performed",
        extras = listOf(AnalyticsParameter("search_query", query)),
    )

    data class BookmarkToggled(val articleUrl: String, val isBookmarked: Boolean) : AnalyticsEvent(
        type = "bookmark_toggled",
        extras = listOf(
            AnalyticsParameter("article_url", articleUrl),
            AnalyticsParameter("is_bookmarked", isBookmarked.toString()),
        ),
    )

    data object BookmarksCleared : AnalyticsEvent(type = "bookmarks_cleared")

    data object AppOpen : AnalyticsEvent(type = "app_open")

    data object NewsRefreshed : AnalyticsEvent(type = "news_refreshed")

    data object OnboardingStarted : AnalyticsEvent(type = "onboarding_started")

    data class OnboardingStepCompleted(val step: Int) : AnalyticsEvent(
        type = "onboarding_step_completed",
        extras = listOf(AnalyticsParameter("step", step.toString())),
    )

    data object OnboardingSkipped : AnalyticsEvent(type = "onboarding_skipped")

    data class OnboardingFinished(
        val country: String,
        val topics: Set<String>,
        val layout: String,
    ) : AnalyticsEvent(
        type = "onboarding_finished",
        extras = listOf(
            AnalyticsParameter("country", country),
            AnalyticsParameter("topics", topics.joinToString(",")),
            AnalyticsParameter("layout", layout),
        ),
    )
}

data class AnalyticsParameter(val key: String, val value: String)
