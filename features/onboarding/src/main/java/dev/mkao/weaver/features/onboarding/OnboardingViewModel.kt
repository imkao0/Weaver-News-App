package dev.mkao.weaver.features.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.mkao.weaver.data.preferences.UserPreferencesRepository
import dev.mkao.weaver.domain.analytics.AnalyticsEvent
import dev.mkao.weaver.domain.analytics.AnalyticsHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val preferencesRepository: UserPreferencesRepository,
    private val analyticsHelper: AnalyticsHelper,
) : ViewModel() {

    private val _state = MutableStateFlow(OnboardingState())
    val state = _state.asStateFlow()

    init {
        analyticsHelper.logEvent(AnalyticsEvent.OnboardingStarted)
    }

    fun onStepCompleted(step: Int) {
        analyticsHelper.logEvent(AnalyticsEvent.OnboardingStepCompleted(step))
    }

    fun onSkip() {
        analyticsHelper.logEvent(AnalyticsEvent.OnboardingSkipped)
        finishOnboarding()
    }

    fun onCountrySelected(countryCode: String, languageCode: String) {
        _state.update { it.copy(selectedCountryCode = countryCode, selectedLanguageCode = languageCode) }
    }

    fun toggleTopic(topic: String) {
        _state.update {
            val topics = it.selectedTopics.toMutableSet()
            if (topics.contains(topic)) {
                topics.remove(topic)
            } else {
                topics.add(topic)
            }
            it.copy(selectedTopics = topics)
        }
    }

    fun onLayoutSelected(layout: String) {
        _state.update { it.copy(selectedLayout = layout) }
    }

    fun finishOnboarding() {
        viewModelScope.launch {
            preferencesRepository.setEdition(
                _state.value.selectedLanguageCode,
                _state.value.selectedCountryCode.lowercase(),
            )
            preferencesRepository.setPreferredTopics(_state.value.selectedTopics)
            preferencesRepository.setPreferredLayout(_state.value.selectedLayout)
            preferencesRepository.setOnboardingCompleted(true)
            analyticsHelper.logEvent(
                AnalyticsEvent.OnboardingFinished(
                    country = _state.value.selectedCountryCode,
                    topics = _state.value.selectedTopics,
                    layout = _state.value.selectedLayout,
                ),
            )
            _state.update { it.copy(isFinished = true) }
        }
    }
}

data class OnboardingState(
    val selectedCountryCode: String = "US",
    val selectedLanguageCode: String = "en",
    val selectedTopics: Set<String> = emptySet(),
    val selectedLayout: String = "default",
    val isFinished: Boolean = false,
)
