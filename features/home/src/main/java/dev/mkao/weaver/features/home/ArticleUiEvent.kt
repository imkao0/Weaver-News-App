package dev.mkao.weaver.features.home

/**
 * One-shot UI events for the Home/Dashboard screen.
 */
sealed class ArticleUiEvent {
    data class ShowSnackbar(val message: String) : ArticleUiEvent()
}
