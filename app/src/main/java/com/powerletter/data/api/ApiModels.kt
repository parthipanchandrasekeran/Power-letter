package com.powerletter.data.api

import com.google.gson.annotations.SerializedName

data class GenerateLetterRequest(
    @SerializedName("letterType")
    val letterType: String,

    @SerializedName("companyName")
    val companyName: String,

    @SerializedName("issueDescription")
    val issueDescription: String,

    @SerializedName("amount")
    val amount: String,

    @SerializedName("transactionDate")
    val transactionDate: String,

    @SerializedName("accountOrOrderNumber")
    val accountOrOrderNumber: String
)

data class GenerateLetterResponse(
    @SerializedName("subject")
    val subject: String,

    @SerializedName("emailBody")
    val emailBody: String,

    @SerializedName("legalBasis")
    val legalBasis: List<String>,

    @SerializedName("tone")
    val tone: String
)

data class ApiError(
    @SerializedName("error")
    val error: String,

    @SerializedName("details")
    val details: List<String>? = null
)
