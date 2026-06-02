package dev.mkao.weaver.util

/**
 * News topics selectable on the "Manage topics" screen and during onboarding.
 *
 * `apiValue` is the value sent to the news provider's `category` query
 * parameter. The active backend (GNews v4) natively supports the first seven
 * values; the remaining topics are resolved by [dev.mkao.weaver.data.repository.RepositoryImpl]
 * through the provider's `search` endpoint so every topic still powers feed
 * filtering. `storageValue` keeps persisted selections stable even if the API
 * mapping changes.
 */
enum class NewsCategories(val apiValue: String, val displayName: String) {
    General("general", "Top News"),
    World("world", "World"),
    Business("business", "Business"),
    Technology("technology", "Technology"),
    Science("science", "Science"),
    Health("health", "Health"),
    Sports("sports", "Sports"),
    Entertainment("entertainment", "Entertainment"),
    Politics("politics", "Politics"),
    Environment("environment", "Environment"),
    Travel("travel", "Travel"),
    Food("food", "Food"),
    Style("style", "Style & Fashion"),
    Gaming("gaming", "Gaming"),
    Cryptocurrency("cryptocurrency", "Cryptocurrency"),
    AI("ai", "AI & Machine Learning"),
    Automotive("automotive", "Automotive"),
    RealEstate("real-estate", "Real Estate"),
    Startups("startups", "Startups"),
    Education("education", "Education"),
    Finance("finance", "Finance"),
    War("war", "War & Conflict"),
    MiddleEast("middle-east", "Middle East"),
    Africa("africa", "Africa"),
    Oceania("oceania", "Oceania"),
    Asia("asia", "Asia"),
    ;

    /** Stable lowercase key used for persistence and state comparisons. */
    val storageValue: String get() = apiValue.lowercase()

    companion object {
        /** Categories natively supported by the GNews `top-headlines` endpoint. */
        private val NATIVE_API_CATEGORIES = setOf(
            "general",
            "world",
            "business",
            "technology",
            "science",
            "health",
            "sports",
            "entertainment",
        )

        /** True when [apiValue] can be sent as a `category` param directly. */
        fun isNativeApiCategory(apiValue: String): Boolean =
            apiValue.lowercase() in NATIVE_API_CATEGORIES

        fun fromApiValue(apiValue: String): NewsCategories? =
            entries.firstOrNull { it.apiValue.equals(apiValue, ignoreCase = true) }
    }
}
