package dev.mkao.weaver.domain.model

/**
 * Domain model for a single recent-search history row.
 */
data class RecentSearch(
    val id: Int,
    val query: String,
    val searchTime: Long,
)
