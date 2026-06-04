package dev.mkao.weaver.features.onboarding

import app.cash.turbine.test
import dev.mkao.weaver.data.preferences.UserPreferencesRepository
import dev.mkao.weaver.domain.analytics.AnalyticsEvent
import dev.mkao.weaver.domain.analytics.AnalyticsHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val preferencesRepository: UserPreferencesRepository = mock()
    private val analyticsHelper: AnalyticsHelper = mock()
    private lateinit var viewModel: OnboardingViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = OnboardingViewModel(preferencesRepository, analyticsHelper)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is correct and logs start event`() = runTest {
        verify(analyticsHelper).logEvent(AnalyticsEvent.OnboardingStarted)
        viewModel.state.test {
            val state = awaitItem()
            assertEquals("US", state.selectedCountryCode)
            assertEquals("en", state.selectedLanguageCode)
            assertTrue(state.selectedTopics.isEmpty())
            assertEquals("default", state.selectedLayout)
        }
    }

    @Test
    fun `toggling topic updates state`() = runTest {
        viewModel.toggleTopic("sports")
        viewModel.state.test {
            val state = awaitItem()
            assertTrue(state.selectedTopics.contains("sports"))
        }
    }

    @Test
    fun `finishing onboarding saves preferences and logs event`() = runTest {
        viewModel.onCountrySelected("GB", "en")
        viewModel.toggleTopic("sports")
        viewModel.onLayoutSelected("compact")

        viewModel.finishOnboarding()
        testDispatcher.scheduler.advanceUntilIdle()

        verify(preferencesRepository).setEdition("en", "gb")
        verify(preferencesRepository).setPreferredTopics(setOf("sports"))
        verify(preferencesRepository).setPreferredLayout("compact")
        verify(preferencesRepository).setOnboardingCompleted(true)
        verify(analyticsHelper).logEvent(
            AnalyticsEvent.OnboardingFinished(
                country = "GB",
                topics = setOf("sports"),
                layout = "compact",
            ),
        )

        viewModel.state.test {
            assertTrue(awaitItem().isFinished)
        }
    }

    @Test
    fun `skipping onboarding logs skip event and finishes`() = runTest {
        viewModel.onSkip()
        testDispatcher.scheduler.advanceUntilIdle()

        verify(analyticsHelper).logEvent(AnalyticsEvent.OnboardingSkipped)
        verify(preferencesRepository).setOnboardingCompleted(true)

        viewModel.state.test {
            assertTrue(awaitItem().isFinished)
        }
    }
}
