package dev.mkao.weaver.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.mkao.weaver.data.preferences.UserPreferencesRepository
import dev.mkao.weaver.util.AppConstants
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    preferencesRepository: UserPreferencesRepository,
) : ViewModel() {
    val isOnboardingCompleted = preferencesRepository.isOnboardingCompleted
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(AppConstants.STATE_FLOW_TIMEOUT_MS),
            initialValue = null,
        )
}
