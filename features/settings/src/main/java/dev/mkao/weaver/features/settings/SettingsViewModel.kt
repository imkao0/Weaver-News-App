package dev.mkao.weaver.features.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.mkao.weaver.data.preferences.UserPreferencesRepository
import dev.mkao.weaver.util.AppConstants
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesRepository: UserPreferencesRepository,
) : ViewModel() {

    val isDarkMode: StateFlow<Boolean> = preferencesRepository.isDarkMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(AppConstants.STATE_FLOW_TIMEOUT_MS), true)

    val isPushNotificationsEnabled: StateFlow<Boolean> = preferencesRepository.isPushNotificationsEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(AppConstants.STATE_FLOW_TIMEOUT_MS), true)

    val preferredLayout: StateFlow<String> = preferencesRepository.preferredLayout
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(AppConstants.STATE_FLOW_TIMEOUT_MS), "default")

    fun toggleDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setDarkMode(enabled)
        }
    }

    fun togglePushNotifications(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setPushNotificationsEnabled(enabled)
        }
    }

    fun setPreferredLayout(layout: String) {
        viewModelScope.launch {
            preferencesRepository.setPreferredLayout(layout)
        }
    }
}
