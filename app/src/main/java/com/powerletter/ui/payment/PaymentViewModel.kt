package com.powerletter.ui.payment

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.powerletter.data.billing.BillingManager
import com.powerletter.data.billing.BillingState
import com.powerletter.data.subscription.ProStatusManager
import com.powerletter.data.usage.DailyUsageTracker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PaymentViewModel(application: Application) : AndroidViewModel(application) {

    private val usageTracker = DailyUsageTracker(application)
    private val proStatusManager = ProStatusManager(application)
    val billingManager = BillingManager(application)

    private val _uiState = MutableStateFlow<PaymentUiState>(PaymentUiState.Loading)
    val uiState: StateFlow<PaymentUiState> = _uiState.asStateFlow()

    init {
        billingManager.initialize()
        observeBillingState()
        updateState()
    }

    private fun observeBillingState() {
        viewModelScope.launch {
            billingManager.billingState.collect { state ->
                when (state) {
                    is BillingState.PurchaseComplete -> {
                        // Purchase successful - update UI
                        updateState()
                    }
                    is BillingState.Error -> {
                        _uiState.value = PaymentUiState.Error(state.message)
                    }
                    is BillingState.Cancelled -> {
                        updateState() // Return to ready state
                    }
                    else -> { /* Handle other states if needed */ }
                }
            }
        }
    }

    private fun updateState() {
        val isPro = proStatusManager.isPro
        val remaining = usageTracker.remainingFreeLetters

        _uiState.value = PaymentUiState.Ready(
            isPro = isPro,
            remainingFreeLetters = if (isPro) Int.MAX_VALUE else remaining,
            hasFreeLetters = isPro || remaining > 0,
            price = billingManager.getFormattedPrice()
        )
    }

    fun hasFreeLettersAvailable(): Boolean {
        return usageTracker.hasFreeLettersAvailable
    }

    fun consumeFreeLetter(): Boolean {
        val success = usageTracker.consumeFreeLetter()
        if (success) {
            updateState()
        }
        return success
    }

    fun launchPurchase(activity: Activity, onResult: (Boolean) -> Unit) {
        billingManager.launchPurchaseFlow(activity) { success ->
            if (success) {
                updateState()
            }
            onResult(success)
        }
    }

    fun resetState() {
        billingManager.resetBillingState()
        updateState()
    }

    override fun onCleared() {
        super.onCleared()
        billingManager.disconnect()
    }
}

sealed class PaymentUiState {
    data object Loading : PaymentUiState()
    data class Ready(
        val isPro: Boolean,
        val remainingFreeLetters: Int,
        val hasFreeLetters: Boolean,
        val price: String = "$2.99"
    ) : PaymentUiState()
    data class Error(val message: String) : PaymentUiState()
}
