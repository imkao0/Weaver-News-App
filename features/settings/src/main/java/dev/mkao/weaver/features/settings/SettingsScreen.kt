package dev.mkao.weaver.features.settings

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Feedback
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.PersonAddAlt
import androidx.compose.material.icons.outlined.ViewAgenda
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import dev.mkao.weaver.features.settings.BuildConfig
import dev.mkao.weaver.R
import dev.mkao.weaver.presentation.common.components.feedback.WeaverHeader
import dev.mkao.weaver.presentation.common.components.feedback.WeaverSwitch
import dev.mkao.weaver.presentation.common.theme.WeaverOutline
import dev.mkao.weaver.presentation.common.theme.WeaverPrimary
import timber.log.Timber

private const val FACEBOOK_PACKAGE = "com.facebook.katana"
private const val FACEBOOK_URL = "https://www.facebook.com/weaverapp"
private const val INSTAGRAM_PACKAGE = "com.instagram.android"
private const val INSTAGRAM_URL = "https://www.instagram.com/weaverapp"
private const val TIKTOK_PACKAGE = "com.zhiliaoapp.musically"
private const val TIKTOK_URL = "https://www.tiktok.com/@weaverapp"
private const val PRIVACY_POLICY_URL =
    "https://github.com/imkao0/Weaver-News-App/blob/master/PRIVACY.md"
private const val TAG = "SettingsScreen"

private data class SettingsState(
    val pushEnabled: Boolean,
    val darkModeEnabled: Boolean,
    val preferredLayout: String,
)

private data class SettingsActions(
    val onBlockedSourcesClick: () -> Unit,
    val onTogglePushNotifications: (Boolean) -> Unit,
    val onToggleDarkMode: (Boolean) -> Unit,
    val onShowLayoutDialog: () -> Unit,
    val onShowConsentDialog: () -> Unit,
    val onAboutClick: () -> Unit,
)

/**
 * Settings screen: mint back arrow, centered wordmark, and outline-icon rows
 * (two of them toggles) separated by full-bleed hairline dividers.
 */
@Composable
fun SettingsScreen(
    onAboutClick: () -> Unit,
    onBlockedSourcesClick: () -> Unit,
    onBackPressed: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val darkModeEnabled by viewModel.isDarkMode.collectAsState()
    val pushEnabled by viewModel.isPushNotificationsEnabled.collectAsState()
    val preferredLayout by viewModel.preferredLayout.collectAsState()
    var showLayoutDialog by remember { mutableStateOf(false) }
    var showConsentDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column(modifier = Modifier.statusBarsPadding()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = stringResource(R.string.arrow_forward),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
                WeaverHeader(logoSize = 34.dp)
                Spacer(modifier = Modifier.padding(vertical = 10.dp))
            }
        },
    ) { paddingValues ->
        SettingsContent(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            state = SettingsState(
                pushEnabled = pushEnabled,
                darkModeEnabled = darkModeEnabled,
                preferredLayout = preferredLayout,
            ),
            actions = SettingsActions(
                onBlockedSourcesClick = onBlockedSourcesClick,
                onTogglePushNotifications = viewModel::togglePushNotifications,
                onToggleDarkMode = viewModel::toggleDarkMode,
                onShowLayoutDialog = { showLayoutDialog = true },
                onShowConsentDialog = { showConsentDialog = true },
                onAboutClick = onAboutClick,
            ),
        )
    }

    if (showConsentDialog) {
        ConsentDialog(onDismiss = { showConsentDialog = false })
    }

    if (showLayoutDialog) {
        FeedStyleDialog(
            preferredLayout = preferredLayout,
            onLayoutSelected = viewModel::setPreferredLayout,
            onDismiss = { showLayoutDialog = false },
        )
    }
}

@Composable
private fun SettingsContent(
    state: SettingsState,
    actions: SettingsActions,
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier = modifier) {
        generalSection(state, actions)
        feedSection(state, actions)
        socialSection()
        legalSection(actions)
        versionFooter()
    }
}

private fun LazyListScope.generalSection(state: SettingsState, actions: SettingsActions) {
    item {
        SettingsRow(
            icon = Icons.Outlined.Block,
            label = stringResource(R.string.blocked_sources),
            onClick = actions.onBlockedSourcesClick,
        )
    }
    item {
        SettingsToggleRow(
            icon = Icons.Outlined.NotificationsNone,
            label = stringResource(R.string.push_notifications),
            checked = state.pushEnabled,
            onCheckedChange = actions.onTogglePushNotifications,
        )
    }
}

private fun LazyListScope.feedSection(state: SettingsState, actions: SettingsActions) {
    item {
        val context = LocalContext.current
        SettingsRow(
            icon = Icons.Outlined.Feedback,
            label = stringResource(R.string.feedback),
            onClick = { launchFeedbackIntent(context) },
        )
    }
    item {
        SettingsRow(
            icon = Icons.Outlined.ViewAgenda,
            label = stringResource(R.string.feed_styles),
            subLabel = when (state.preferredLayout) {
                "compact" -> stringResource(R.string.layout_compact)
                "text-under" -> stringResource(R.string.layout_text_under)
                "magazine" -> stringResource(R.string.layout_magazine)
                "coverflow" -> stringResource(R.string.layout_coverflow)
                "masonry" -> stringResource(R.string.layout_masonry)
                else -> stringResource(R.string.layout_comfortable)
            },
            onClick = actions.onShowLayoutDialog,
        )
    }
    item {
        SettingsToggleRow(
            icon = Icons.Outlined.DarkMode,
            label = stringResource(R.string.dark_mode),
            checked = state.darkModeEnabled,
            onCheckedChange = actions.onToggleDarkMode,
        )
    }
}

private fun LazyListScope.socialSection() {
    item {
        val context = LocalContext.current
        SettingsRow(
            icon = Icons.Outlined.PersonAddAlt,
            label = stringResource(R.string.invite_friends_to_weaver),
            onClick = { launchShareIntent(context, null) },
        )
    }
    item {
        val context = LocalContext.current
        SettingsRow(
            painter = painterResource(R.drawable.ic_facebook_circle),
            label = stringResource(R.string.weaver_on_facebook),
            onClick = {
                launchSocial(context, FACEBOOK_PACKAGE, FACEBOOK_URL)
            },
        )
    }
    item {
        val context = LocalContext.current
        SettingsRow(
            painter = painterResource(R.drawable.ic_instagram),
            label = stringResource(R.string.weaver_on_instagram),
            onClick = {
                launchSocial(context, INSTAGRAM_PACKAGE, INSTAGRAM_URL)
            },
        )
    }
    item {
        val context = LocalContext.current
        SettingsRow(
            painter = painterResource(R.drawable.ic_tiktok),
            label = stringResource(R.string.weaver_on_tiktok),
            onClick = {
                launchSocial(context, TIKTOK_PACKAGE, TIKTOK_URL)
            },
        )
    }
}

private fun LazyListScope.legalSection(actions: SettingsActions) {
    item {
        val context = LocalContext.current
        SettingsRow(
            icon = Icons.Outlined.Lock,
            label = stringResource(R.string.privacy_and_conditions),
            onClick = { launchUrl(context, PRIVACY_POLICY_URL) },
        )
    }
    item {
        SettingsRow(
            icon = Icons.Outlined.Description,
            label = stringResource(R.string.manage_consent),
            onClick = actions.onShowConsentDialog,
        )
    }
    item {
        SettingsRow(
            icon = Icons.Outlined.Info,
            label = stringResource(R.string.about_weaver),
            onClick = actions.onAboutClick,
        )
    }
}

private fun LazyListScope.versionFooter() {
    item {
        Text(
            text = stringResource(R.string.version) + " " + BuildConfig.VERSION_NAME,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp,
        )
    }
}

@Composable
private fun ConsentDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.manage_consent), fontWeight = FontWeight.Bold) },
        text = {
            Text(
                text = stringResource(R.string.manage_consent_summary),
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.accept_all), color = MaterialTheme.colorScheme.primary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.reject_optional), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
    )
}

@Composable
private fun FeedStyleDialog(
    preferredLayout: String,
    onLayoutSelected: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.feed_style)) },
        text = {
            Column {
                LayoutOptionRow(
                    label = stringResource(R.string.layout_comfortable),
                    isSelected = preferredLayout == "comfortable" || preferredLayout == "default",
                    onClick = {
                        onLayoutSelected("comfortable")
                        onDismiss()
                    },
                )
                LayoutOptionRow(
                    label = stringResource(R.string.layout_compact),
                    isSelected = preferredLayout == "compact",
                    onClick = {
                        onLayoutSelected("compact")
                        onDismiss()
                    },
                )
                LayoutOptionRow(
                    label = stringResource(R.string.layout_text_under),
                    isSelected = preferredLayout == "text-under",
                    onClick = {
                        onLayoutSelected("text-under")
                        onDismiss()
                    },
                )
                LayoutOptionRow(
                    label = stringResource(R.string.layout_magazine),
                    isSelected = preferredLayout == "magazine",
                    onClick = {
                        onLayoutSelected("magazine")
                        onDismiss()
                    },
                )
                LayoutOptionRow(
                    label = stringResource(R.string.layout_coverflow),
                    isSelected = preferredLayout == "coverflow",
                    onClick = {
                        onLayoutSelected("coverflow")
                        onDismiss()
                    },
                )
                LayoutOptionRow(
                    label = stringResource(R.string.layout_masonry),
                    isSelected = preferredLayout == "masonry",
                    onClick = {
                        onLayoutSelected("masonry")
                        onDismiss()
                    },
                )
            }
        },
        confirmButton = {},
    )
}

@Composable
private fun LayoutOptionRow(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = WeaverPrimary,
            ),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = label, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun SettingsRow(
    label: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    painter: Painter? = null,
    subLabel: String? = null,
    onClick: () -> Unit = {},
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 20.dp, vertical = 17.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            when {
                icon != null -> Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(24.dp),
                )
                painter != null -> Icon(
                    painter = painter,
                    contentDescription = label,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(24.dp),
                )
            }
            Spacer(modifier = Modifier.width(28.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 16.sp,
                )
                if (subLabel != null) {
                    Text(
                        text = subLabel,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp,
                    )
                }
            }
        }
        HorizontalDivider(color = WeaverOutline, thickness = 0.5.dp)
    }
}

@Composable
private fun SettingsToggleRow(
    icon: ImageVector,
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(24.dp),
            )
            Spacer(modifier = Modifier.width(28.dp))
            Text(
                text = label,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 16.sp,
                modifier = Modifier.weight(1f),
            )
            WeaverSwitch(checked = checked, onCheckedChange = onCheckedChange)
        }
        HorizontalDivider(color = WeaverOutline, thickness = 0.5.dp)
    }
}

private fun launchUrl(context: Context, url: String) {
    runCatching {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }
}

private fun launchSocial(context: Context, packageName: String, url: String) {
    val intent = context.packageManager.getLaunchIntentForPackage(packageName)
    if (intent != null) {
        runCatching { context.startActivity(intent) }.onSuccess { return }
    }
    launchUrl(context, url)
}

private fun launchFeedbackIntent(context: Context) {
    runCatching {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:cnoceania@gmail.com")
            putExtra(Intent.EXTRA_SUBJECT, "Weaver News feedback")
        }
        context.startActivity(intent)
    }
}

fun launchShareIntent(context: Context, packageName: String?) {
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, context.getString(R.string.hey_check_out_this_awesome_app))
        packageName?.let { setPackage(it) }
    }
    try {
        context.startActivity(Intent.createChooser(shareIntent, context.getString(R.string.share_via)))
    } catch (e: ActivityNotFoundException) {
        Timber.tag(TAG).w(e, "No share chooser available; launching app directly.")
        packageName?.let { pkg ->
            context.packageManager.getLaunchIntentForPackage(pkg)?.let(context::startActivity)
        }
    }
}
