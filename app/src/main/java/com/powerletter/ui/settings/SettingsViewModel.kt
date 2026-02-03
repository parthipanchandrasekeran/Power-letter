package com.powerletter.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.powerletter.data.subscription.ProStatusManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val proStatusManager = ProStatusManager(application)

    private val _uiState = MutableStateFlow(getState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private fun getState(): SettingsUiState {
        return SettingsUiState(
            isPro = proStatusManager.isPro,
            expiryDate = proStatusManager.expiryDate,
            daysRemaining = proStatusManager.daysRemaining
        )
    }

    fun refreshState() {
        _uiState.value = getState()
    }

    // For testing - simulate purchase
    fun activatePro() {
        proStatusManager.activatePro()
        refreshState()
    }
}

data class SettingsUiState(
    val isPro: Boolean = false,
    val expiryDate: String? = null,
    val daysRemaining: Int = 0
)
