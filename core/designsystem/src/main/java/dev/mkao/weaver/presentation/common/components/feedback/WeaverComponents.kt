package dev.mkao.weaver.presentation.common.components.feedback

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.mkao.weaver.presentation.common.theme.WeaverSourceChip
import dev.mkao.weaver.presentation.common.theme.WeaverToggleThumbOff
import dev.mkao.weaver.presentation.common.theme.WeaverToggleThumbOn
import dev.mkao.weaver.presentation.common.theme.WeaverToggleTrackOff
import dev.mkao.weaver.presentation.common.theme.WeaverToggleTrackOn

/**
 * Shared UI atoms reused by the home feed cards, search rows, the
 * select-edition screen and the settings screen.
 */

fun categoryChipColor(category: String): Color = when (category.lowercase()) {
    "world", "general", "top news" -> Color(0xFFF2C12E)
    "sports", "football" -> Color(0xFF129BD8)
    "tech", "technology" -> Color(0xFFB7C4CC)
    "gaming" -> Color(0xFF5B707A)
    "film", "entertainment" -> Color(0xFF8A5FA8)
    "business" -> Color(0xFF2ECC71)
    "science" -> Color(0xFF4FC3F7)
    "health" -> Color(0xFFF0508C)
    "nation" -> Color(0xFF26A69A)
    else -> Color(0xFF5B707A)
}

/**
 * Brand-inspired colors for known publishers — a varied palette so each news
 * source chip is visually distinct in the feed and on the detail screen.
 */
fun sourceChipColor(source: String): Color = when (source.lowercase()) {
    "bbc news", "bbc" -> Color(0xFFBB1919)
    "cnn" -> Color(0xFFCC0000)
    "reuters" -> Color(0xFFF38000)
    "associated press", "ap" -> Color(0xFFDA0000)
    "al jazeera english", "al jazeera" -> Color(0xFFF0A500)
    "the guardian", "guardian" -> Color(0xFF052962)
    "nyt", "the new york times" -> Color(0xFF567B79)
    "the washington post", "washington post" -> Color(0xFF64707D)
    "bloomberg" -> Color(0xFF2800D8)
    "financial times" -> Color(0xFFd1495b)
    "the wall street journal", "wsj" -> Color(0xFF0274B6)
    "cnbc" -> Color(0xFF005B96)
    "espn" -> Color(0xFFE02F2F)
    "techcrunch" -> Color(0xFF00A35C)
    "the verge", "verge" -> Color(0xFFE5127F)
    "wired" -> Color(0xFF6E6E6E)
    "ars technica" -> Color(0xFFFF4E00)
    "national geographic" -> Color(0xFFE3B716)
    "fox news" -> Color(0xFF003366)
    "nbc news" -> Color(0xFF0089A8)
    "abc news" -> Color(0xFF2D3E8F)
    "cbs news" -> Color(0xFF1F3C88)
    "usa today" -> Color(0xFF009BDE)
    "politico" -> Color(0xFF9C1A1C)
    "the hill" -> Color(0xFF005596)
    "time" -> Color(0xFFD72323)
    "newsweek" -> Color(0xFFED1C24)
    "business insider", "insider" -> Color(0xFF1B3A6B)
    "engadget" -> Color(0xFF563D7C)
    "mashable" -> Color(0xFF00AEF0)
    "ign" -> Color(0xFFBF1313)
    "bleacher report" -> Color(0xFF1CA3EC)
    "hacker news" -> Color(0xFFFF6600)
    "latest news", "google news" -> Color(0xFFF2C12E)
    else -> fallbackSourceChipColor(source)
}

/**
 * Deterministic vibrant fallback so sources outside the curated list still get
 * a consistent, varied chip color (same name -> same color across screens).
 */
private val FallbackSourceChipColors = listOf(
    Color(0xFF8E44AD), // purple
    Color(0xFF16A085), // teal
    Color(0xFFD35400), // burnt orange
    Color(0xFF2E86C1), // sky blue
    Color(0xFFC2185B), // magenta
    Color(0xFF7CB342), // lime green
    Color(0xFF5D4037), // brown
    Color(0xFF3949AB), // indigo
    Color(0xFF00838F), // cyan
    Color(0xFFB71C1C), // deep red
)

private fun fallbackSourceChipColor(source: String): Color {
    val key = source.trim().lowercase()
    if (key.isEmpty()) return FallbackSourceChipColors[0]
    val index = key.fold(0) { acc, char -> (acc * 31 + char.code) and Int.MAX_VALUE } %
        FallbackSourceChipColors.size
    return FallbackSourceChipColors[index]
}

@Composable
fun CategoryChip(
    category: String,
    modifier: Modifier = Modifier,
) {
    val background = categoryChipColor(category)
    val textColor = when (category.lowercase()) {
        "sports", "football", "gaming", "film", "entertainment", "nation" -> Color.White
        else -> Color(0xFF10232C)
    }
    Box(
        modifier = modifier
            .background(background, RoundedCornerShape(4.dp))
            .padding(horizontal = 7.dp, vertical = 2.5.dp),
    ) {
        Text(
            text = category,
            color = textColor,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            maxLines = 1,
        )
    }
}

@Composable
fun SourceChip(
    source: String,
    modifier: Modifier = Modifier,
) {
    val background = sourceChipColor(source)
    val textColor = Color.White

    Box(
        modifier = modifier
            .background(background, RoundedCornerShape(4.dp))
            .padding(horizontal = 7.dp, vertical = 2.5.dp),
    ) {
        Text(
            text = source,
            color = textColor,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            maxLines = 1,
        )
    }
}

@Composable
fun ChipRow(
    category: String,
    source: String,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        CategoryChip(category)
        Spacer(modifier = Modifier.width(6.dp))
        SourceChip(source)
    }
}

/** Pill switch — ON: teal track/aqua thumb; OFF: gray track/dark teal thumb. */
@Composable
fun WeaverSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier.scale(0.85f),
        colors = SwitchDefaults.colors(
            checkedThumbColor = WeaverToggleThumbOn,
            checkedTrackColor = WeaverToggleTrackOn,
            checkedBorderColor = Color.Transparent,
            uncheckedThumbColor = WeaverToggleThumbOff,
            uncheckedTrackColor = WeaverToggleTrackOff,
            uncheckedBorderColor = Color.Transparent,
        ),
    )
}
