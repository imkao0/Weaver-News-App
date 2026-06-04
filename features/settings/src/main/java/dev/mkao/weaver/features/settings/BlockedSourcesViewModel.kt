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
class BlockedSourcesViewModel @Inject constructor(
    private val preferencesRepository: UserPreferencesRepository,
) : ViewModel() {

    val blockedSources: StateFlow<Set<String>> = preferencesRepository.blockedSources
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(AppConstants.STATE_FLOW_TIMEOUT_MS), emptySet())

    fun unblockSource(sourceName: String) {
        viewModelScope.launch {
            preferencesRepository.unblockSource(sourceName)
        }
    }
}
