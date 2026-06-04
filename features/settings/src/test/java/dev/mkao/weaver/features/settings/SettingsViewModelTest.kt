package dev.mkao.weaver.features.settings

import app.cash.turbine.test
import dev.mkao.weaver.data.preferences.UserPreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var preferencesRepository: UserPreferencesRepository
    private lateinit var viewModel: SettingsViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        preferencesRepository = mock()

        whenever(preferencesRepository.isDarkMode).thenReturn(flowOf(true))
        whenever(preferencesRepository.isPushNotificationsEnabled).thenReturn(flowOf(true))
        whenever(preferencesRepository.preferredLayout).thenReturn(flowOf("comfortable"))

        viewModel = SettingsViewModel(preferencesRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `when dark mode is toggled, it is saved to preferences`() = runTest {
        viewModel.toggleDarkMode(false)
        verify(preferencesRepository).setDarkMode(false)
    }

    @Test
    fun `when layout is changed, it is saved to preferences`() = runTest {
        viewModel.setPreferredLayout("compact")
        verify(preferencesRepository).setPreferredLayout("compact")
    }

    @Test
    fun `when state is observed, it reflects preference values`() = runTest {
        viewModel.preferredLayout.test {
            assertEquals("comfortable", awaitItem())
        }
    }
}
