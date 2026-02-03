package com.powerletter.data.repository

import com.google.gson.Gson
import com.powerletter.data.api.ApiClient
import com.powerletter.data.api.ApiError
import com.powerletter.data.api.GenerateLetterRequest
import com.powerletter.data.api.GenerateLetterResponse
import com.powerletter.data.model.GeneratedLetter
import com.powerletter.data.model.LegalBasis
import com.powerletter.data.model.Tone
import com.powerletter.domain.model.LetterRequest
import com.powerletter.ui.common.ErrorMessages
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLException

sealed class LetterResult {
    data class Success(val letter: GeneratedLetter) : LetterResult()
    data class Error(val message: String) : LetterResult()
}

class LetterRepository {

    private val api = ApiClient.api
    private val gson = Gson()

    suspend fun generateLetter(request: LetterRequest): LetterResult {
        return withContext(Dispatchers.IO) {
            // Retry up to 2 times for transient failures
            var lastError: LetterResult.Error? = null
            repeat(2) { attempt ->
                try {
                    val apiRequest = request.toApiRequest()
                    val response = api.generateLetter(apiRequest)

                    when {
                        response.isSuccessful -> {
                            return@withContext handleSuccessResponse(response.body())
                        }
                        response.code() in 500..599 -> {
                            // Server error - retry
                            lastError = handleErrorResponse(response.errorBody()?.string(), response.code()) as LetterResult.Error
                        }
                        else -> {
                            return@withContext handleErrorResponse(response.errorBody()?.string(), response.code())
                        }
                    }
                } catch (e: Exception) {
                    lastError = LetterResult.Error(mapExceptionToMessage(e))
                    // Retry on network errors
                    if (attempt == 0) {
                        kotlinx.coroutines.delay(1000) // Wait 1s before retry
                    }
                }
            }
            lastError ?: LetterResult.Error(ErrorMessages.GENERATION_ERROR_MESSAGE)
        }
    }

    private fun LetterRequest.toApiRequest() = GenerateLetterRequest(
        letterType = letterType.name,
        companyName = companyName,
        issueDescription = issueDescription,
        amount = amount,
        transactionDate = incidentDate,
        accountOrOrderNumber = referenceNumber
    )

    private fun handleSuccessResponse(body: GenerateLetterResponse?): LetterResult {
        if (body == null) {
            return LetterResult.Error(ErrorMessages.SERVER_ERROR_MESSAGE)
        }

        val letter = body.toGeneratedLetter()

        return if (letter.isValid()) {
            LetterResult.Success(letter)
        } else {
            return LetterResult.Error(ErrorMessages.GENERATION_ERROR_MESSAGE)
        }
    }

    private fun GenerateLetterResponse.toGeneratedLetter(): GeneratedLetter {
        val mappedLegalBasis = legalBasis.mapNotNull { basis ->
            LegalBasis.entries.find { it.displayName == basis }
        }

        val mappedTone = when (tone.lowercase()) {
            "professional" -> Tone.PROFESSIONAL
            "firm" -> Tone.FIRM
            else -> Tone.FIRM
        }

        return GeneratedLetter(
            subject = subject,
            emailBody = emailBody,
            legalBasis = mappedLegalBasis,
            tone = mappedTone
        )
    }

    private fun handleErrorResponse(errorBody: String?, statusCode: Int): LetterResult {
        val message = try {
            if (errorBody != null) {
                val apiError = gson.fromJson(errorBody, ApiError::class.java)
                apiError.error
            } else {
                getDefaultErrorMessage(statusCode)
            }
        } catch (e: Exception) {
            getDefaultErrorMessage(statusCode)
        }

        return LetterResult.Error(message)
    }

    private fun mapExceptionToMessage(e: Exception): String {
        return when (e) {
            is UnknownHostException -> ErrorMessages.NETWORK_ERROR_MESSAGE
            is SocketTimeoutException -> ErrorMessages.TIMEOUT_ERROR_MESSAGE
            is SSLException -> ErrorMessages.NETWORK_ERROR_MESSAGE
            else -> {
                val message = e.message?.lowercase() ?: ""
                when {
                    message.contains("unable to resolve host") -> ErrorMessages.NETWORK_ERROR_MESSAGE
                    message.contains("timeout") -> ErrorMessages.TIMEOUT_ERROR_MESSAGE
                    message.contains("connection") -> ErrorMessages.NETWORK_ERROR_MESSAGE
                    else -> ErrorMessages.GENERIC_ERROR_MESSAGE
                }
            }
        }
    }

    private fun getDefaultErrorMessage(statusCode: Int): String {
        return when (statusCode) {
            400 -> ErrorMessages.INVALID_INPUT_MESSAGE
            401, 403 -> ErrorMessages.SERVER_ERROR_MESSAGE
            404 -> ErrorMessages.SERVER_ERROR_MESSAGE
            429 -> ErrorMessages.SERVER_ERROR_MESSAGE
            in 500..599 -> ErrorMessages.SERVER_ERROR_MESSAGE
            else -> ErrorMessages.GENERIC_ERROR_MESSAGE
        }
    }
}
