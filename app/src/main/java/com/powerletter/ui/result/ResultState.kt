package com.powerletter.ui.result

import com.powerletter.data.model.GeneratedLetter

sealed interface ResultState {
    data object Idle : ResultState
    data object Loading : ResultState
    data class Success(val letter: GeneratedLetter) : ResultState
    data class Error(val message: String) : ResultState
}
