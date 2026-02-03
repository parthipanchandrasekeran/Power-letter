package com.powerletter.ui.form

import androidx.lifecycle.ViewModel
import com.powerletter.domain.model.LetterRequest
import com.powerletter.domain.model.LetterType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class FormViewModel : ViewModel() {

    private val _state = MutableStateFlow(FormState())
    val state: StateFlow<FormState> = _state.asStateFlow()

    fun setLetterType(letterType: LetterType) {
        _state.update { it.copy(letterType = letterType) }
    }

    fun updateCompanyName(value: String) {
        _state.update {
            it.copy(
                companyName = value,
                errors = it.errors.copy(companyName = null)
            )
        }
    }

    fun updateIssueDescription(value: String) {
        _state.update {
            it.copy(
                issueDescription = value,
                errors = it.errors.copy(issueDescription = null)
            )
        }
    }

    fun updateAmount(value: String) {
        _state.update {
            it.copy(
                amount = value,
                errors = it.errors.copy(amount = null)
            )
        }
    }

    fun updateIncidentDate(value: String) {
        _state.update {
            it.copy(
                incidentDate = value,
                errors = it.errors.copy(incidentDate = null)
            )
        }
    }

    fun updateReferenceNumber(value: String) {
        _state.update {
            it.copy(
                referenceNumber = value,
                errors = it.errors.copy(referenceNumber = null)
            )
        }
    }

    fun validate(): Boolean {
        val currentState = _state.value
        val errors = FormErrors(
            companyName = if (currentState.companyName.isBlank()) "Company name is required" else null,
            issueDescription = if (currentState.issueDescription.isBlank()) "Issue description is required" else null,
            amount = if (currentState.amount.isBlank()) "Amount is required" else null,
            incidentDate = if (currentState.incidentDate.isBlank()) "Date is required" else null,
            referenceNumber = null // Optional field
        )

        _state.update { it.copy(errors = errors) }

        return errors.companyName == null &&
               errors.issueDescription == null &&
               errors.amount == null &&
               errors.incidentDate == null
    }

    fun toLetterRequest(): LetterRequest? {
        val currentState = _state.value
        val letterType = currentState.letterType ?: return null

        return LetterRequest(
            letterType = letterType,
            companyName = currentState.companyName,
            issueDescription = currentState.issueDescription,
            amount = currentState.amount,
            incidentDate = currentState.incidentDate,
            referenceNumber = currentState.referenceNumber
        )
    }

    fun reset() {
        _state.value = FormState()
    }
}
