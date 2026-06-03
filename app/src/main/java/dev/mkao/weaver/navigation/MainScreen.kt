package dev.mkao.weaver.navigation

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.NavigableListDetailPaneScaffold
import androidx.compose.material3.adaptive.navigation.ThreePaneScaffoldNavigator
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteItemColors
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.window.core.layout.WindowSizeClass
import dev.mkao.weaver.R
import dev.mkao.weaver.presentation.common.theme.WeaverBackground
import dev.mkao.weaver.presentation.common.theme.WeaverInactive
import dev.mkao.weaver.presentation.common.theme.WeaverPrimary
import dev.mkao.weaver.features.details.DetailScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Top-level scaffold handling adaptive window layouts.
 */
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun MainScreen(
    windowSizeClass: WindowSizeClass,
    articleDeepLink: ArticleDeepLink? = null,
    onArticleDeepLinkConsumed: () -> Unit = {},
    viewModel: MainViewModel = hiltViewModel(),
) {
    val isOnboardingCompleted by viewModel.isOnboardingCompleted.collectAsStateWithLifecycle(initialValue = null)

    if (isOnboardingCompleted == null) {
        return
    }

    val startDestination = if (isOnboardingCompleted == true) Destinations.Bulletin else Destinations.Onboarding
    val backStack: NavBackStack<NavKey> = rememberNavBackStack(startDestination)

    // Open the article detail screen when the widget delivers a deep link.
    LaunchedEffect(articleDeepLink) {
        if (articleDeepLink != null) {
            backStack.add(
                Destinations.ArticleDetail(
                    articleUrl = articleDeepLink.articleUrl,
                    title = articleDeepLink.title,
                ),
            )
            onArticleDeepLinkConsumed()
        }
    }

    // Sync currentDestination with backstack for the NavSuite
    var currentDestination by rememberSaveable {
        mutableStateOf(if (isOnboardingCompleted == true) AppDestination.HOME else null)
    }

    LaunchedEffect(backStack.lastOrNull()) {
        currentDestination = backStack.lastOrNull().toAppDestination() ?: currentDestination
    }

    val navigator = rememberListDetailPaneScaffoldNavigator<Destinations.ArticleDetail>()

    // Show the navigation suite on top-level destinations only
    val showNavSuite = backStack.lastOrNull().isTopLevelDestination()

    // The `navigationSuiteItems` builder lambda is not @Composable in the
    // adaptive library, so the item colors must be resolved up-front here.
    val navItemColors = weaverNavItemColors()

    val coroutineScope = rememberCoroutineScope()

    if (showNavSuite) {
        MainScreenWithNavSuite(
            backStack = backStack,
            windowSizeClass = windowSizeClass,
            currentDestination = currentDestination,
            onDestinationSelected = { currentDestination = it },
            navItemColors = navItemColors,
            navigator = navigator,
            coroutineScope = coroutineScope,
        )
    } else {
        AppNavigation(
            backStack = backStack,
            windowSizeClass = windowSizeClass,
        )
    }

    // When a deep link arrives while the NavSuite scaffold is showing, surface
    // the article in the list-detail detail pane for a polished large-screen UX.
    LaunchedEffect(backStack.lastOrNull()) {
        val top = backStack.lastOrNull()
        if (top is Destinations.ArticleDetail && showNavSuite) {
            backStack.removeLastOrNull()
            coroutineScope.launch {
                navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, top)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
private fun MainScreenWithNavSuite(
    backStack: NavBackStack<NavKey>,
    windowSizeClass: WindowSizeClass,
    currentDestination: AppDestination?,
    onDestinationSelected: (AppDestination) -> Unit,
    navItemColors: NavigationSuiteItemColors,
    navigator: ThreePaneScaffoldNavigator<Destinations.ArticleDetail>,
    coroutineScope: CoroutineScope,
) {
    // The article detail is an inner screen. When it is shown in the detail
    // pane, the Nav3 back-stack top still points at the list destination
    // (e.g. Bulletin), so `showNavSuite` stays true. Detect the open detail
    // pane here and hide the bottom navigation while it is showing an article.
    val isArticleDetailShowing =
        navigator.currentDestination?.contentKey is Destinations.ArticleDetail

    val defaultLayoutType =
        NavigationSuiteScaffoldDefaults.calculateFromAdaptiveInfo(currentWindowAdaptiveInfo())
    val layoutType = if (isArticleDetailShowing) NavigationSuiteType.None else defaultLayoutType

    NavigationSuiteScaffold(
        layoutType = layoutType,
        containerColor = MaterialTheme.colorScheme.background,
        navigationSuiteColors = NavigationSuiteDefaults.colors(
            navigationBarContainerColor = MaterialTheme.colorScheme.background,
            navigationBarContentColor = MaterialTheme.colorScheme.onBackground,
        ),
        navigationSuiteItems = {
            AppDestination.entries.forEach { destination ->
                item(
                    selected = currentDestination == destination,
                    onClick = {
                        onDestinationSelected(destination)
                        navigateToTopLevel(backStack, destination)
                    },
                    icon = {
                        Icon(
                            painter = painterResource(destination.icon),
                            contentDescription = stringResource(destination.label),
                            modifier = Modifier.size(24.dp),
                        )
                    },
                    label = {
                        Text(
                            text = stringResource(destination.label),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                        )
                    },
                    colors = navItemColors,
                )
            }
        },
    ) {
        NavigableListDetailPaneScaffold(
            navigator = navigator,
            listPane = {
                AppNavigation(
                    backStack = backStack,
                    windowSizeClass = windowSizeClass,
                    onArticleClick = { article ->
                        if (isVideoArticle(article)) {
                            backStack.add(Destinations.VideoPlayer(extractVideoId(article)))
                        } else {
                            coroutineScope.launch {
                                navigator.navigateTo(
                                    ListDetailPaneScaffoldRole.Detail,
                                    Destinations.ArticleDetail(article.url, article.title),
                                )
                            }
                        }
                    },
                )
            },
            detailPane = {
                val destination = navigator.currentDestination
                if (destination != null) {
                    val contentKey = destination.contentKey
                    if (contentKey is Destinations.ArticleDetail) {
                        DetailScreen(
                            articleUrl = contentKey.articleUrl,
                            onBackPressed = {
                                coroutineScope.launch {
                                    navigator.navigateBack()
                                }
                            },
                        )
                    }
                }
            },
        )
    }
}

/** WEAVER bottom-bar colors: teal accent when selected, muted gray otherwise. */
@Composable
private fun weaverNavItemColors() = NavigationSuiteItemColors(
    navigationBarItemColors = NavigationBarItemDefaults.colors(
        selectedIconColor = WeaverPrimary,
        selectedTextColor = WeaverPrimary,
        unselectedIconColor = WeaverInactive,
        unselectedTextColor = WeaverInactive,
        indicatorColor = Color.Transparent,
    ),
    navigationRailItemColors = NavigationRailItemDefaults.colors(
        selectedIconColor = WeaverPrimary,
        selectedTextColor = WeaverPrimary,
        unselectedIconColor = WeaverInactive,
        unselectedTextColor = WeaverInactive,
        indicatorColor = WeaverBackground,
    ),
    navigationDrawerItemColors = NavigationDrawerItemDefaults.colors(
        selectedIconColor = WeaverPrimary,
        selectedTextColor = WeaverPrimary,
        unselectedIconColor = WeaverInactive,
        unselectedTextColor = WeaverInactive,
        selectedContainerColor = WeaverBackground,
    ),
)

private fun NavKey?.toAppDestination(): AppDestination? = when (this) {
    is Destinations.Bulletin -> AppDestination.HOME
    is Destinations.VideoFeed -> AppDestination.VIDEO_FEED
    is Destinations.Search -> AppDestination.SEARCH
    is Destinations.Categories -> AppDestination.CATEGORIES
    is Destinations.Bookmarks -> AppDestination.BOOKMARKS
    else -> null
}

private fun NavKey?.isTopLevelDestination(): Boolean = toAppDestination() != null

private fun navigateToTopLevel(
    backStack: NavBackStack<NavKey>,
    destination: AppDestination,
) {
    val key: NavKey = when (destination) {
        AppDestination.HOME -> Destinations.Bulletin
        AppDestination.VIDEO_FEED -> Destinations.VideoFeed
        AppDestination.SEARCH -> Destinations.Search
        AppDestination.CATEGORIES -> Destinations.Categories
        AppDestination.BOOKMARKS -> Destinations.Bookmarks
    }
    if (backStack.lastOrNull() != key) {
        // Re-selecting a tab pops back to its key instead of clearing the
        // stack, so tab ViewModels and view scopes survive.
        if (backStack.contains(key)) {
            while (backStack.lastOrNull() != key) {
                backStack.removeLastOrNull()
            }
        } else {
            backStack.add(key)
        }
    }
}

enum class AppDestination(
    @StringRes val label: Int,
    val icon: Int,
) {
    HOME(R.string.nav_home, R.drawable.ic_home),
    VIDEO_FEED(R.string.nav_video_feed, R.drawable.ic_play_outline),
    SEARCH(R.string.nav_search, R.drawable.ic_search),
    CATEGORIES(R.string.nav_categories, R.drawable.ic_sliders),
    BOOKMARKS(R.string.nav_bookmarks, R.drawable.bookmark),
}
