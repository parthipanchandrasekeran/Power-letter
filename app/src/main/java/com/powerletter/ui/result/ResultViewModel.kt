package com.powerletter.ui.result

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.powerletter.data.repository.LetterRepository
import com.powerletter.data.repository.LetterResult
import com.powerletter.domain.model.LetterRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ResultViewModel : ViewModel() {

    private val repository = LetterRepository()

    private val _state = MutableStateFlow<ResultState>(ResultState.Idle)
    val state: StateFlow<ResultState> = _state.asStateFlow()

    // Store last request for retry
    private var lastRequest: LetterRequest? = null

    fun generateLetter(request: LetterRequest) {
        lastRequest = request
        executeGeneration(request)
    }

    fun retry() {
        lastRequest?.let { request ->
            executeGeneration(request)
        }
    }

    private fun executeGeneration(request: LetterRequest) {
        viewModelScope.launch {
            _state.value = ResultState.Loading

            when (val result = repository.generateLetter(request)) {
                is LetterResult.Success -> {
                    _state.value = ResultState.Success(result.letter)
                }
                is LetterResult.Error -> {
                    _state.value = ResultState.Error(result.message)
                }
            }
        }
    }

    fun reset() {
        _state.value = ResultState.Idle
        lastRequest = null
    }
}
