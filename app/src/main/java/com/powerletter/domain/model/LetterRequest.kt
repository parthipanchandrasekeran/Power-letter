package com.powerletter.domain.model

data class LetterRequest(
    val letterType: LetterType,
    val companyName: String,
    val issueDescription: String,
    val amount: String,
    val incidentDate: String,
    val referenceNumber: String
)
