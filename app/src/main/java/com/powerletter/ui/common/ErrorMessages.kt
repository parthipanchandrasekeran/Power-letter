package com.powerletter.ui.common

object ErrorMessages {
    // Network errors
    const val NETWORK_ERROR_TITLE = "Connection issue"
    const val NETWORK_ERROR_MESSAGE = "We couldn't reach our servers. Please check your internet connection and try again."
    const val NETWORK_ERROR_ACTION = "Try Again"

    // Generation errors (never mention AI)
    const val GENERATION_ERROR_TITLE = "Something went wrong"
    const val GENERATION_ERROR_MESSAGE = "We couldn't create your letter this time. This can happen when the connection is slow. Your information is safe."
    const val GENERATION_ERROR_ACTION = "Try Again"

    // Timeout errors
    const val TIMEOUT_ERROR_TITLE = "Taking longer than usual"
    const val TIMEOUT_ERROR_MESSAGE = "The request is taking a while. Please try again — it usually works on the second attempt."
    const val TIMEOUT_ERROR_ACTION = "Try Again"

    // Invalid input
    const val INVALID_INPUT_TITLE = "Missing information"
    const val INVALID_INPUT_MESSAGE = "Please fill in the required fields so we can write your letter."
    const val INVALID_INPUT_ACTION = "Review Details"

    // Purchase errors
    const val PURCHASE_INCOMPLETE_TITLE = "Purchase not completed"
    const val PURCHASE_INCOMPLETE_MESSAGE = "Your purchase wasn't completed. You haven't been charged. Try again when you're ready."
    const val PURCHASE_INCOMPLETE_ACTION = "Try Again"

    const val PURCHASE_CANCELLED_TITLE = "No problem"
    const val PURCHASE_CANCELLED_MESSAGE = "You can unlock Pro whenever you're ready. Your letter preview is still here."
    const val PURCHASE_CANCELLED_ACTION = "Maybe Later"

    // Restore errors
    const val RESTORE_FAILED_TITLE = "Couldn't restore purchase"
    const val RESTORE_FAILED_MESSAGE = "We couldn't find a previous purchase on this account. If you believe this is a mistake, please contact support."
    const val RESTORE_FAILED_ACTION = "Contact Support"

    const val RESTORE_NOTHING_TITLE = "No previous purchase found"
    const val RESTORE_NOTHING_MESSAGE = "We couldn't find a Pro purchase linked to this account. If you purchased on a different account, try signing in with that one."
    const val RESTORE_NOTHING_ACTION = "Got It"

    // Server errors
    const val SERVER_ERROR_TITLE = "Service temporarily unavailable"
    const val SERVER_ERROR_MESSAGE = "Our servers are busy right now. Please wait a moment and try again."
    const val SERVER_ERROR_ACTION = "Try Again"

    // Generic fallback
    const val GENERIC_ERROR_TITLE = "Something went wrong"
    const val GENERIC_ERROR_MESSAGE = "We ran into an unexpected issue. Please try again."
    const val GENERIC_ERROR_ACTION = "Try Again"
}
