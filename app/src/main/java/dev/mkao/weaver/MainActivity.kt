package dev.mkao.weaver

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import dagger.hilt.android.AndroidEntryPoint
import dev.mkao.weaver.data.preferences.UserPreferencesRepository
import dev.mkao.weaver.domain.analytics.AnalyticsEvent
import dev.mkao.weaver.domain.analytics.AnalyticsHelper
import dev.mkao.weaver.navigation.ArticleDeepLink
import dev.mkao.weaver.navigation.MainScreen
import dev.mkao.weaver.presentation.common.theme.WeaverTheme
import dev.mkao.weaver.features.widget.WidgetProvider
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var analyticsHelper: AnalyticsHelper

    @Inject
    lateinit var preferencesRepository: UserPreferencesRepository

    // Pending widget deep-link, consumed once by MainScreen.
    private val pendingArticleDeepLink = mutableStateOf<ArticleDeepLink?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        analyticsHelper.logEvent(AnalyticsEvent.AppOpen)
        analyticsHelper.logEvent(AnalyticsEvent.ScreenView("home"))

        pendingArticleDeepLink.value = intent.toArticleDeepLink()

        setContent {
            val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
            val isDarkMode by preferencesRepository.isDarkMode.collectAsStateWithLifecycle(initialValue = true)

            WeaverTheme(darkTheme = isDarkMode) {
                NotificationPermissionEffect()
                Surface(modifier = Modifier.fillMaxSize()) {
                    MainScreen(
                        windowSizeClass = windowSizeClass,
                        articleDeepLink = pendingArticleDeepLink.value,
                        onArticleDeepLinkConsumed = { pendingArticleDeepLink.value = null },
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        intent.toArticleDeepLink()?.let { pendingArticleDeepLink.value = it }
    }

    /** Maps a widget tap (ACTION_OPEN_ARTICLE + url extra) to an in-app deep link. */
    private fun Intent?.toArticleDeepLink(): ArticleDeepLink? {
        val url = this?.getStringExtra(WidgetProvider.EXTRA_ARTICLE_URL)
            ?: return null
        val title = getStringExtra(WidgetProvider.EXTRA_ARTICLE_TITLE).orEmpty()
        return ArticleDeepLink(articleUrl = url, title = title)
    }
}

/** Notification-permission request (Android 13+). */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun NotificationPermissionEffect() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val notificationPermission = rememberPermissionState(
            Manifest.permission.POST_NOTIFICATIONS,
        )
        androidx.compose.runtime.LaunchedEffect(Unit) {
            if (!notificationPermission.status.isGranted) {
                notificationPermission.launchPermissionRequest()
            }
        }
    }
}
