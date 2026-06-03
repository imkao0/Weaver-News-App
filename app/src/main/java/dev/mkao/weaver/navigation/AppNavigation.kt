package dev.mkao.weaver.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.window.core.layout.WindowSizeClass
import dev.mkao.weaver.domain.model.Article
import dev.mkao.weaver.features.about.AboutMe
import dev.mkao.weaver.features.bookmarks.BookmarkScreen
import dev.mkao.weaver.features.categories.CategoriesScreen
import dev.mkao.weaver.features.details.DetailScreen
import dev.mkao.weaver.features.home.ArticleScreen
import dev.mkao.weaver.features.home.DashBoard
import dev.mkao.weaver.features.languages.LanguageEditionsSidebar
import dev.mkao.weaver.features.onboarding.OnboardingScreen
import dev.mkao.weaver.features.search.SearchScreen
import dev.mkao.weaver.features.settings.BlockedSourcesScreen
import dev.mkao.weaver.features.settings.SettingsScreen
import dev.mkao.weaver.features.videofeed.VideoFeedScreen
import dev.mkao.weaver.features.videofeed.VideoPlayerScreen

private const val TRANSITION_DURATION_MS = 300

/**
 * Navigation 3 host.
 */
@Composable
fun AppNavigation(
    backStack: NavBackStack<NavKey>,
    windowSizeClass: WindowSizeClass,
    onArticleClick: ((Article) -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val onBack: () -> Unit = { backStack.removeLastOrNull() }

    NavDisplay(
        backStack = backStack,
        onBack = onBack,
        modifier = modifier,
        transitionSpec = { forwardTransition() },
        popTransitionSpec = { backwardTransition() },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        entryProvider = entryProvider {
            homeEntries(backStack, windowSizeClass, onArticleClick, onBack)
            videoEntries(backStack, onBack)
            browserEntries(backStack, onArticleClick, onBack)
        },
    )
}

private fun EntryProviderScope<NavKey>.homeEntries(
    backStack: NavBackStack<NavKey>,
    windowSizeClass: WindowSizeClass,
    onArticleClick: ((Article) -> Unit)?,
    onBack: () -> Unit,
) {
    entry<Destinations.Onboarding> {
        OnboardingScreen(
            onFinished = {
                backStack.clear()
                backStack.add(Destinations.Bulletin)
            },
            windowSizeClass = windowSizeClass,
        )
    }

    entry<Destinations.Bulletin> {
        DashBoard(
            onArticleClick = { article -> backStack.openArticle(article, onArticleClick) },
            onSettingsClick = { backStack.add(Destinations.Settings) },
            onEditionClick = { backStack.add(Destinations.LanguageEditions) },
            windowSizeClass = windowSizeClass,
        )
    }

    entry<Destinations.Search> {
        SearchScreen(
            onArticleClick = { article -> backStack.openArticle(article, onArticleClick) },
        )
    }

    entry<Destinations.ArticleDetail> { key ->
        DetailScreen(
            articleUrl = key.articleUrl,
            onBackPressed = onBack,
        )
    }
}

private fun EntryProviderScope<NavKey>.videoEntries(
    backStack: NavBackStack<NavKey>,
    onBack: () -> Unit,
) {
    entry<Destinations.VideoFeed> {
        VideoFeedScreen(
            onVideoClick = { video ->
                backStack.add(Destinations.VideoPlayer(video.videoId))
            },
        )
    }

    entry<Destinations.VideoPlayer> { key ->
        VideoPlayerScreen(
            initialVideoId = key.initialVideoId,
            onClose = onBack,
        )
    }
}

private fun EntryProviderScope<NavKey>.browserEntries(
    backStack: NavBackStack<NavKey>,
    onArticleClick: ((Article) -> Unit)?,
    onBack: () -> Unit,
) {
    entry<Destinations.Categories> {
        CategoriesScreen()
    }

    entry<Destinations.Discover> {
        ArticleScreen(
            onArticleClick = { article -> backStack.openArticleDetail(article, onArticleClick) },
        )
    }

    entry<Destinations.Bookmarks> {
        BookmarkScreen(
            onArticleClick = { article -> backStack.openArticleDetail(article, onArticleClick) },
        )
    }

    entry<Destinations.Settings> {
        SettingsScreen(
            onAboutClick = { backStack.add(Destinations.About) },
            onBlockedSourcesClick = { backStack.add(Destinations.BlockedSources) },
            onBackPressed = onBack,
        )
    }

    entry<Destinations.BlockedSources> {
        BlockedSourcesScreen(onBackPressed = onBack)
    }

    entry<Destinations.About> {
        AboutMe()
    }

    entry<Destinations.LanguageEditions> {
        LanguageEditionsSidebar(onBack = onBack)
    }
}

/** Open an article, routing video articles to the in-app player. */
private fun NavBackStack<NavKey>.openArticle(article: Article, onArticleClick: ((Article) -> Unit)?) {
    if (isVideoArticle(article)) {
        add(Destinations.VideoPlayer(extractVideoId(article)))
    } else {
        openArticleDetail(article, onArticleClick)
    }
}

/** Open a non-video article via the host callback or the detail destination. */
private fun NavBackStack<NavKey>.openArticleDetail(article: Article, onArticleClick: ((Article) -> Unit)?) {
    if (onArticleClick != null) {
        onArticleClick(article)
    } else {
        add(Destinations.ArticleDetail(article.url, article.title))
    }
}

private fun forwardTransition() =
    slideInHorizontally(
        initialOffsetX = { it },
        animationSpec = tween(TRANSITION_DURATION_MS),
    ) + fadeIn(animationSpec = tween(TRANSITION_DURATION_MS)) togetherWith
        slideOutHorizontally(
            targetOffsetX = { -it },
            animationSpec = tween(TRANSITION_DURATION_MS),
        ) + fadeOut(animationSpec = tween(TRANSITION_DURATION_MS))

private fun backwardTransition() =
    slideInHorizontally(
        initialOffsetX = { -it },
        animationSpec = tween(TRANSITION_DURATION_MS),
    ) + fadeIn(animationSpec = tween(TRANSITION_DURATION_MS)) togetherWith
        slideOutHorizontally(
            targetOffsetX = { it },
            animationSpec = tween(TRANSITION_DURATION_MS),
        ) + fadeOut(animationSpec = tween(TRANSITION_DURATION_MS))
