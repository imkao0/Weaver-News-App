package dev.mkao.weaver.features.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.window.core.layout.WindowSizeClass
import androidx.window.core.layout.WindowWidthSizeClass
import dev.mkao.weaver.R
import dev.mkao.weaver.presentation.common.theme.WeaverChipBusiness
import dev.mkao.weaver.presentation.common.theme.WeaverChipEntertainment
import dev.mkao.weaver.presentation.common.theme.WeaverChipFilm
import dev.mkao.weaver.presentation.common.theme.WeaverChipGaming
import dev.mkao.weaver.presentation.common.theme.WeaverChipHealth
import dev.mkao.weaver.presentation.common.theme.WeaverChipNation
import dev.mkao.weaver.presentation.common.theme.WeaverChipScience
import dev.mkao.weaver.presentation.common.theme.WeaverChipSports
import dev.mkao.weaver.presentation.common.theme.WeaverChipTech
import dev.mkao.weaver.presentation.common.theme.WeaverChipWorld
import dev.mkao.weaver.features.shared.LanguageConstants
import dev.mkao.weaver.util.NewsCategories
import kotlinx.coroutines.launch

private const val PAGE_WELCOME = 0
private const val PAGE_COUNTRY = 1
private const val PAGE_TOPICS = 2
private const val PAGE_LAYOUT = 3
private const val PAGE_COUNT = 4
private const val MIN_TOPIC_SELECTIONS = 3
private const val WELCOME_MESSAGE =
    "Create your personalized newsfeed now or skip to set it up later. Get started and stay informed!"
private const val CONTINUE_BUTTON_WIDTH_FRACTION = 0.8f
private const val DISABLED_TEXT_ALPHA = 0.38f
private const val BODY_TEXT_ALPHA = 0.7f
private const val DIVIDER_ALPHA = 0.3f
private const val UNSELECTED_BORDER_ALPHA = 0.5f
private const val PREVIEW_SURFACE_ALPHA = 0.5f
private const val PREVIEW_BLOCK_ALPHA = 0.4f
private const val PREVIEW_ACCENT_ALPHA = 0.5f

@Composable
fun OnboardingScreen(
    onFinished: () -> Unit,
    windowSizeClass: WindowSizeClass,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val pagerState = rememberPagerState(pageCount = { PAGE_COUNT })
    val scope = rememberCoroutineScope()

    LaunchedEffect(state.isFinished) {
        if (state.isFinished) {
            onFinished()
        }
    }

    Scaffold(
        topBar = { OnboardingTopBar(onSkip = viewModel::onSkip) },
        bottomBar = {
            OnboardingBottomBar(
                pagerState = pagerState,
                selectedTopicsCount = state.selectedTopics.size,
                onContinue = {
                    if (pagerState.currentPage < PAGE_LAYOUT) {
                        scope.launch {
                            viewModel.onStepCompleted(pagerState.currentPage + 1)
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    } else {
                        viewModel.finishOnboarding()
                    }
                },
            )
        },
    ) { padding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            userScrollEnabled = false,
        ) { page ->
            when (page) {
                PAGE_WELCOME -> WelcomeSlide(windowSizeClass = windowSizeClass)
                PAGE_COUNTRY -> CountrySelectionSlide(
                    selectedCountry = state.selectedCountryCode,
                    onCountrySelected = viewModel::onCountrySelected,
                )
                PAGE_TOPICS -> TopicsSelectionSlide(
                    selectedTopics = state.selectedTopics,
                    onTopicToggle = viewModel::toggleTopic,
                )
                PAGE_LAYOUT -> LayoutSelectionSlide(
                    selectedLayout = state.selectedLayout,
                    onLayoutSelected = viewModel::onLayoutSelected,
                )
            }
        }
    }
}

@Composable
private fun OnboardingTopBar(onSkip: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(16.dp),
        contentAlignment = Alignment.TopEnd,
    ) {
        Text(
            text = "Skip",
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clickable { onSkip() },
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun OnboardingBottomBar(
    pagerState: PagerState,
    selectedTopicsCount: Int,
    onContinue: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        PagerIndicator(pagerState.currentPage, pagerState.pageCount)
        Spacer(modifier = Modifier.height(24.dp))
        val isContinueEnabled = when (pagerState.currentPage) {
            PAGE_TOPICS -> selectedTopicsCount >= MIN_TOPIC_SELECTIONS
            else -> true
        }

        Button(
            onClick = onContinue,
            enabled = isContinueEnabled,
            modifier = Modifier
                .fillMaxWidth(CONTINUE_BUTTON_WIDTH_FRACTION)
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
            ),
            contentPadding = PaddingValues(),
        ) {
            Text(
                text = if (pagerState.currentPage == PAGE_LAYOUT) "Show me my feed!" else "Continue",
                color = if (isContinueEnabled) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(
                        alpha = DISABLED_TEXT_ALPHA,
                    )
                },
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun pagerIndicatorColor(isCurrentPage: Boolean) =
    if (isCurrentPage) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant

@Composable
fun PagerIndicator(currentPage: Int, pageCount: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        repeat(pageCount) { index ->
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(pagerIndicatorColor(index == currentPage)),
            )
        }
    }
}

@Composable
fun WelcomeSlide(windowSizeClass: WindowSizeClass) {
    val isExpanded = windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.EXPANDED

    if (isExpanded) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(48.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_logo),
                contentDescription = null,
                modifier = Modifier.size(240.dp),
                contentScale = ContentScale.Fit,
            )
            Spacer(modifier = Modifier.width(48.dp))
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.Start,
            ) {
                Text(
                    text = "Welcome to WEAVER!",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = WELCOME_MESSAGE,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Start,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = BODY_TEXT_ALPHA),
                )
            }
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_logo),
                contentDescription = null,
                modifier = Modifier.size(180.dp),
                contentScale = ContentScale.Fit,
            )
            Spacer(modifier = Modifier.height(48.dp))
            Text(
                text = "Welcome to WEAVER!",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = WELCOME_MESSAGE,
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = BODY_TEXT_ALPHA),
            )
        }
    }
}

@Composable
fun CountrySelectionSlide(
    selectedCountry: String,
    onCountrySelected: (String, String) -> Unit,
) {
    var searchQuery by remember { mutableStateOf("") }
    val countries = remember(searchQuery) {
        LanguageConstants.languages.flatMap { edition ->
            edition.abbreviations.map { abbr ->
                abbr to (LanguageConstants.countryMap[abbr] ?: edition.name) to edition.code
            }
        }.filter { it.first.second.contains(searchQuery, ignoreCase = true) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "CHOOSE YOUR COUNTRY",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search", color = MaterialTheme.colorScheme.onSurfaceVariant) },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            },
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            ),
        )
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            items(countries) { (info, langCode) ->
                val (abbr, name) = info
                CountryItem(
                    name = name,
                    isSelected = abbr == selectedCountry,
                    onClick = { onCountrySelected(abbr, langCode) },
                )
            }
        }
    }
}

@Composable
fun CountryItem(name: String, isSelected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = name, color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp)
        if (isSelected) {
            Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        }
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = DIVIDER_ALPHA))
}

@Composable
fun TopicsSelectionSlide(
    selectedTopics: Set<String>,
    onTopicToggle: (String) -> Unit,
) {
    val topics = NewsCategories.entries

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "What are you interested in?",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = "Choose at least 3 topics to get your personalized feed.",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = BODY_TEXT_ALPHA),
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(24.dp))
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(topics) { category ->
                TopicItem(
                    name = category.displayName,
                    isSelected = selectedTopics.contains(category.apiValue),
                    onClick = { onTopicToggle(category.apiValue) },
                    color = getCategoryColor(category),
                )
            }
        }
    }
}

private val CategoryColors = mapOf(
    NewsCategories.General to WeaverChipWorld,
    NewsCategories.World to WeaverChipWorld,
    NewsCategories.Business to WeaverChipBusiness,
    NewsCategories.Technology to WeaverChipTech,
    NewsCategories.Science to WeaverChipScience,
    NewsCategories.Health to WeaverChipHealth,
    NewsCategories.Sports to WeaverChipSports,
    NewsCategories.Entertainment to WeaverChipEntertainment,
    NewsCategories.Politics to WeaverChipNation,
    NewsCategories.Environment to WeaverChipNation,
    NewsCategories.Travel to WeaverChipEntertainment,
    NewsCategories.Food to WeaverChipHealth,
    NewsCategories.Style to WeaverChipFilm,
    NewsCategories.Gaming to WeaverChipGaming,
    NewsCategories.Cryptocurrency to WeaverChipWorld,
    NewsCategories.AI to WeaverChipTech,
    NewsCategories.Automotive to WeaverChipGaming,
    NewsCategories.RealEstate to WeaverChipBusiness,
    NewsCategories.Startups to WeaverChipBusiness,
    NewsCategories.Education to WeaverChipScience,
    NewsCategories.Finance to WeaverChipBusiness,
    NewsCategories.War to WeaverChipNation,
    NewsCategories.MiddleEast to WeaverChipWorld,
    NewsCategories.Africa to WeaverChipWorld,
    NewsCategories.Oceania to WeaverChipWorld,
    NewsCategories.Asia to WeaverChipWorld,
)

fun getCategoryColor(category: NewsCategories): Color = CategoryColors.getValue(category)

@Composable
fun TopicItem(name: String, isSelected: Boolean, onClick: () -> Unit, color: Color) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) color else Color.Transparent,
        border = if (isSelected) {
            null
        } else {
            BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = UNSELECTED_BORDER_ALPHA))
        },
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = name,
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
            )
            if (isSelected) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
            }
        }
    }
}

@Composable
fun LayoutSelectionSlide(
    selectedLayout: String,
    onLayoutSelected: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Choose your preferred layout",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(modifier = Modifier.height(32.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            LayoutOption("Default", "default", selectedLayout == "default") { onLayoutSelected("default") }
            LayoutOption("Compact", "compact", selectedLayout == "compact") { onLayoutSelected("compact") }
            LayoutOption("Text-under", "text-under", selectedLayout == "text-under") { onLayoutSelected("text-under") }
        }
    }
}

@Composable
fun LayoutOption(name: String, value: String, isSelected: Boolean, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(width = 80.dp, height = 140.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = PREVIEW_SURFACE_ALPHA))
                .border(
                    width = if (isSelected) 2.dp else 0.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                    shape = RoundedCornerShape(8.dp),
                )
                .clickable { onClick() },
        ) {
            when (value) {
                "default" -> DefaultLayoutPreview()
                "compact" -> CompactLayoutPreview()
                "text-under" -> TextUnderLayoutPreview()
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = name, color = MaterialTheme.colorScheme.onSurface, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(8.dp))
        RadioButton(
            selected = isSelected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = MaterialTheme.colorScheme.primary,
                unselectedColor = MaterialTheme.colorScheme.outline,
            ),
        )
    }
}

@Composable
fun DefaultLayoutPreview() {
    Column(modifier = Modifier.padding(4.dp)) {
        Box(
            modifier = Modifier.fillMaxWidth().height(
                20.dp,
            ).background(MaterialTheme.colorScheme.onSurfaceVariant.copy(PREVIEW_BLOCK_ALPHA)),
        )
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .background(MaterialTheme.colorScheme.primary.copy(PREVIEW_ACCENT_ALPHA)),
        )
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier.fillMaxWidth().height(
                20.dp,
            ).background(MaterialTheme.colorScheme.onSurfaceVariant.copy(PREVIEW_BLOCK_ALPHA)),
        )
    }
}

@Composable
fun CompactLayoutPreview() {
    Row(modifier = Modifier.padding(4.dp)) {
        Box(modifier = Modifier.size(30.dp).background(MaterialTheme.colorScheme.primary.copy(PREVIEW_ACCENT_ALPHA)))
        Spacer(modifier = Modifier.width(4.dp))
        Column {
            Box(
                modifier = Modifier.fillMaxWidth().height(
                    10.dp,
                ).background(MaterialTheme.colorScheme.onSurfaceVariant.copy(PREVIEW_BLOCK_ALPHA)),
            )
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier.fillMaxWidth().height(
                    10.dp,
                ).background(MaterialTheme.colorScheme.onSurfaceVariant.copy(PREVIEW_BLOCK_ALPHA)),
            )
        }
    }
}

@Composable
fun TextUnderLayoutPreview() {
    Column(modifier = Modifier.padding(4.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .background(MaterialTheme.colorScheme.primary.copy(PREVIEW_ACCENT_ALPHA)),
        )
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier.fillMaxWidth().height(
                10.dp,
            ).background(MaterialTheme.colorScheme.onSurfaceVariant.copy(PREVIEW_BLOCK_ALPHA)),
        )
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier.fillMaxWidth().height(
                10.dp,
            ).background(MaterialTheme.colorScheme.onSurfaceVariant.copy(PREVIEW_BLOCK_ALPHA)),
        )
    }
}
