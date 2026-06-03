package dev.mkao.weaver.util

/**
 * Catalog of followable news publishers.
 *
 * [name] must match the publisher name reported by the news API
 * (`article.source.name`) so followed sources can be applied to feed
 * filtering. [apiValue] is a stable lowercase key used for persistence.
 */
data class NewsSource(
    val apiValue: String,
    val name: String,
    val accentColor: Long,
)

object NewsSources {

    val available: List<NewsSource> = listOf(
        NewsSource("bbc-news", "BBC News", 0xFFBB1919),
        NewsSource("cnn", "CNN", 0xFFCC0000),
        NewsSource("reuters", "Reuters", 0xFFFF8000),
        NewsSource("associated-press", "Associated Press", 0xFFDA0000),
        NewsSource("al-jazeera-english", "Al Jazeera English", 0xFFF0A500),
        NewsSource("the-guardian", "The Guardian", 0xFF052962),
        NewsSource("the-new-york-times", "The New York Times", 0xFF567B79),
        NewsSource("the-washington-post", "The Washington Post", 0xFF64707D),
        NewsSource("bloomberg", "Bloomberg", 0xFF2800D8),
        NewsSource("financial-times", "Financial Times", 0xFFFFF1E0),
        NewsSource("the-wall-street-journal", "The Wall Street Journal", 0xFF0274B6),
        NewsSource("cnbc", "CNBC", 0xFF005B96),
        NewsSource("espn", "ESPN", 0xFFE02F2F),
        NewsSource("techcrunch", "TechCrunch", 0xFF0A9E01),
        NewsSource("the-verge", "The Verge", 0xFFE5127D),
        NewsSource("wired", "Wired", 0xFF6E6E6E),
        NewsSource("ars-technica", "Ars Technica", 0xFFFF4E00),
        NewsSource("national-geographic", "National Geographic", 0xFFFFCC00),
        NewsSource("fox-news", "Fox News", 0xFF003366),
        NewsSource("nbc-news", "NBC News", 0xFF0089A8),
    )

    fun normalizedName(source: NewsSource): String = source.name.lowercase()

    fun byName(name: String): NewsSource? =
        available.firstOrNull { it.name.equals(name, ignoreCase = true) }
}
