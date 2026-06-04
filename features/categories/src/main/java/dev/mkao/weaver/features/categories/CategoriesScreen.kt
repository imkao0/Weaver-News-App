package dev.mkao.weaver.features.categories

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import dev.mkao.weaver.R
import dev.mkao.weaver.util.AppConstants
import dev.mkao.weaver.util.Dimens
import dev.mkao.weaver.util.NewsCategories

/**
 * Categories tab — the "Manage topics" screen: a Topics catalog and a
 * Sources catalog where each entry can be followed/unfollowed. Both
 * selections persist via DataStore through [CategoriesViewModel].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(
    viewModel: CategoriesViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var topicQuery by rememberSaveable { mutableStateOf("") }

    val allTopics = NewsCategories.entries.toList()
    val isFiltering = topicQuery.isNotBlank()
    val visibleTopics = remember(topicQuery) {
        if (isFiltering) {
            allTopics.filter { it.displayName.contains(topicQuery, ignoreCase = true) }
        } else {
            allTopics
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.manage_topics),
                        color = MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = Dimens.PaddingLarge),
        ) {
            Spacer(modifier = Modifier.height(Dimens.PaddingMedium))

            TopicSearchField(
                value = topicQuery,
                onValueChange = { topicQuery = it },
            )

            Spacer(modifier = Modifier.height(Dimens.PaddingMedium))

            val selectedCategories = state.categories.map { it.lowercase() }.toSet()

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(Dimens.PaddingMicro),
                verticalArrangement = Arrangement.spacedBy(Dimens.PaddingMicro),
                contentPadding = PaddingValues(bottom = Dimens.PaddingLarge),
            ) {
                // ---------------- Topics section ----------------
                if (visibleTopics.isNotEmpty()) {
                    item(key = "topics_header", span = { GridItemSpan(maxLineSpan) }) {
                        SectionHeader(text = stringResource(R.string.topics))
                    }
                    items(visibleTopics, key = { "topic_${it.apiValue}" }) { category ->
                        CategoryBox(
                            category = category,
                            imageUrl = state.categoryImages[category.apiValue],
                            isSelected = selectedCategories.contains(category.storageValue),
                            onToggle = {
                                viewModel.onCategoryToggled(category.apiValue)
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = AppConstants.AlphaHigh),
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = Dimens.PaddingSmall),
    )
}

@Composable
private fun TopicSearchField(
    value: String,
    onValueChange: (String) -> Unit,
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .height(Dimens.SearchFieldHeight)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = AppConstants.AlphaMedium),
                shape = RoundedCornerShape(Dimens.MediumIconSize),
            )
            .clip(RoundedCornerShape(Dimens.MediumIconSize)),
        placeholder = {
            Text(
                text = stringResource(R.string.search),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        leadingIcon = {
            Icon(
                painter = painterResource(id = R.drawable.ic_search),
                contentDescription = stringResource(R.string.search),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(Dimens.IconSmall),
            )
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            disabledContainerColor = Color.Transparent,
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
            cursorColor = MaterialTheme.colorScheme.primary,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
        ),
    )
}

@Composable
private fun CategoryBox(
    category: NewsCategories,
    imageUrl: String?,
    isSelected: Boolean,
    onToggle: () -> Unit,
) {
    val selectionColor = MaterialTheme.colorScheme.primary
    val shape = RoundedCornerShape(Dimens.RadiusMedium)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(shape)
            .clickable(onClick = onToggle)
            .then(
                if (isSelected) {
                    Modifier.border(
                        width = Dimens.SelectedBorderWidth,
                        color = selectionColor,
                        shape = shape,
                    )
                } else {
                    Modifier
                },
            ),
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(imageUrl)
                .crossfade(true)
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            placeholder = painterResource(R.drawable.ic_logo),
            error = painterResource(R.drawable.ic_logo),
            modifier = Modifier.fillMaxSize(),
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = AppConstants.AlphaHigh),
                        ),
                    ),
                ),
        )

        if (isSelected) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(selectionColor.copy(alpha = AppConstants.AlphaSelectedScrim)),
            )
        }

        Text(
            text = category.displayName,
            color = Color.White,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(Dimens.PaddingSmall),
        )

        if (isSelected) {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = stringResource(R.string.selected),
                tint = selectionColor,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(Dimens.PaddingTiny)
                    .background(
                        color = MaterialTheme.colorScheme.background,
                        shape = CircleShape,
                    )
                    .size(Dimens.StandardIconSize),
            )
        }
    }
}
