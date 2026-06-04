package dev.mkao.weaver.features.languages

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import dev.mkao.weaver.R
import dev.mkao.weaver.presentation.common.theme.WeaverOkGradientEnd
import dev.mkao.weaver.presentation.common.theme.WeaverOkGradientStart
import dev.mkao.weaver.presentation.common.theme.WeaverOkText
import dev.mkao.weaver.presentation.common.theme.WeaverPrimary
import dev.mkao.weaver.presentation.common.theme.WeaverSecondary
import dev.mkao.weaver.features.home.HomeViewModel
import dev.mkao.weaver.features.home.ArticleEvent
import dev.mkao.weaver.features.shared.LanguageConstants.countryMap
import dev.mkao.weaver.features.shared.LanguageConstants.languages

/**
 * "Select edition" country picker: theme-aware back chevron + centered title,
 * an outlined search field, a circular-flag country list with a themed
 * checkmark on the selected row, and a floating gradient "OK" pill.
 */
@Composable
fun LanguageEditionsSidebar(
    onBack: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    var selectedLanguage by remember { mutableStateOf(state.selectedLanguage) }
    var selectedCountryCode by remember {
        mutableStateOf(state.selectedCountry?.abbreviations?.firstOrNull() ?: "US")
    }
    var query by rememberSaveable { mutableStateOf("") }

    val countryLanguageList = remember {
        languages.flatMap { edition ->
            edition.abbreviations.mapNotNull { countryCode ->
                countryMap[countryCode]?.let { countryName ->
                    EditionRow(edition.code, edition.name, countryCode, countryName)
                }
            }
        }
    }

    val filtered = remember(query) {
        if (query.isBlank()) {
            countryLanguageList
        } else {
            countryLanguageList.filter {
                it.countryName.contains(query, ignoreCase = true) ||
                    it.languageName.contains(query, ignoreCase = true)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding(),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            EditionHeader(onBack = onBack)

            // Outlined search field
            EditionSearchField(query = query, onQueryChange = { query = it })

            EditionList(
                filtered = filtered,
                selectedLanguage = selectedLanguage,
                selectedCountryCode = selectedCountryCode,
                onSelect = { languageCode, countryCode ->
                    selectedLanguage = languageCode
                    selectedCountryCode = countryCode
                },
                modifier = Modifier.weight(1f),
            )
        }

        // Floating gradient "OK" pill button
        ConfirmEditionButton(
            onConfirm = {
                val edition = languages.find { it.code == selectedLanguage }
                if (edition != null) {
                    viewModel.onEvent(
                        ArticleEvent.CountryLanguageChanged(
                            edition.copy(abbreviations = listOf(selectedCountryCode)),
                            selectedLanguage,
                        ),
                    )
                }
                onBack()
            },
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@Composable
private fun EditionHeader(onBack: () -> Unit) {
    // Header: mint back arrow + centered mint title
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier.align(Alignment.CenterStart),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = stringResource(R.string.arrow_forward),
                tint = MaterialTheme.colorScheme.primary,
            )
        }
        Text(
            text = stringResource(R.string.select_edition),
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Center),
        )
    }
}

private data class EditionRow(
    val languageCode: String,
    val languageName: String,
    val countryCode: String,
    val countryName: String,
)

@Composable
private fun EditionListItem(
    row: EditionRow,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AsyncImage(
            model = "https://flagsapi.com/${row.countryCode}/flat/64.png",
            contentDescription = "${row.countryName} flag",
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop,
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = row.countryName,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f),
        )

        if (isSelected) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(26.dp),
            )
        }
    }
}

@Composable
private fun EditionSearchField(query: String, onQueryChange: (String) -> Unit) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .border(
                width = 1.5.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(10.dp),
            ),
        placeholder = {
            Text(
                text = stringResource(R.string.search),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        singleLine = true,
        shape = RoundedCornerShape(10.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            focusedTextColor = MaterialTheme.colorScheme.onBackground,
            unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
            cursorColor = MaterialTheme.colorScheme.primary,
        ),
    )
}

@Composable
private fun EditionList(
    filtered: List<EditionRow>,
    selectedLanguage: String,
    selectedCountryCode: String,
    onSelect: (String, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier = modifier.fillMaxWidth()) {
        itemsIndexed(
            filtered,
            key = { _, row -> "${row.languageCode}-${row.countryCode}" },
        ) { index, row ->
            EditionListItem(
                row = row,
                isSelected = row.languageCode == selectedLanguage &&
                    row.countryCode.equals(selectedCountryCode, ignoreCase = true),
                onClick = { onSelect(row.languageCode, row.countryCode) },
            )
            if (index < filtered.size - 1) {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outline,
                    thickness = 0.5.dp,
                )
            }
        }
    }
}

@Composable
private fun ConfirmEditionButton(onConfirm: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onConfirm,
        modifier = modifier
            .padding(horizontal = 40.dp, vertical = 24.dp)
            .fillMaxWidth()
            .height(56.dp)
            .background(
                Brush.horizontalGradient(
                    listOf(WeaverPrimary, WeaverSecondary),
                ),
                shape = RoundedCornerShape(28.dp),
            ),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(28.dp),
    ) {
        Text(
            text = stringResource(R.string.confirm),
            color = WeaverOkText,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
        )
    }
}
