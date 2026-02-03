package com.powerletter.ui.form

import com.powerletter.domain.model.LetterType

data class FormState(
    val letterType: LetterType? = null,
    val companyName: String = "",
    val issueDescription: String = "",
    val amount: String = "",
    val incidentDate: String = "",
    val referenceNumber: String = "",
    val errors: FormErrors = FormErrors()
)

data class FormErrors(
    val companyName: String? = null,
    val issueDescription: String? = null,
    val amount: String? = null,
    val incidentDate: String? = null,
    val referenceNumber: String? = null
)
