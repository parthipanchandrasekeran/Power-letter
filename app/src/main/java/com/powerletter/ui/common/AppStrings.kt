package com.powerletter.ui.common

object AppStrings {
    // Home Screen
    const val HOME_TITLE = "Power Letter"
    const val HOME_SUBTITLE = "Get the refund you deserve — with a professionally written request."

    // Letter Types
    object LetterTypes {
        const val GYM_TITLE = "Gym Membership"
        const val GYM_DESCRIPTION = "Request a refund for unused membership fees or unwanted charges."

        const val TELECOM_TITLE = "Phone or Internet Bill"
        const val TELECOM_DESCRIPTION = "Dispute unexpected charges or billing errors on your account."

        const val SUBSCRIPTION_TITLE = "Subscription Service"
        const val SUBSCRIPTION_DESCRIPTION = "Cancel a subscription and request a refund for recent charges."

        const val AIRLINE_TITLE = "Flight Issue"
        const val AIRLINE_DESCRIPTION = "Request compensation for a delayed, cancelled, or disrupted flight."
    }

    // Form Screen
    const val FORM_TITLE = "Tell us what happened"
    const val FORM_SUBTITLE = "We'll use these details to write a clear, professional request on your behalf."

    // Form Fields
    object FormFields {
        const val COMPANY_LABEL = "Company name"
        const val COMPANY_HELPER = "Who should receive this letter?"

        const val ISSUE_LABEL = "What happened"
        const val ISSUE_HELPER = "Describe the problem in your own words."

        const val AMOUNT_LABEL = "Amount ($)"
        const val AMOUNT_HELPER = "How much are you requesting back?"

        const val DATE_LABEL = "When it happened"
        const val DATE_HELPER = "The date of the charge or incident."

        const val REFERENCE_LABEL = "Reference number (optional)"
        const val REFERENCE_HELPER = "Any account, order, or confirmation number you have."
    }

    // Form Validation
    object Validation {
        const val COMPANY_REQUIRED = "Please enter the company name"
        const val ISSUE_REQUIRED = "Please describe what happened"
        const val AMOUNT_REQUIRED = "Please enter the amount"
        const val DATE_REQUIRED = "Please enter the date"
    }

    // Generate Button
    const val GENERATE_BUTTON = "Create My Letter"
    const val GENERATE_BUTTON_LOADING = "Writing your letter..."
    const val GENERATE_REASSURANCE = "Your letter will be professional, clear, and ready to send."

    // Payment Screen
    const val PAYMENT_TITLE = "Ready to send your request"
    const val PAYMENT_SUBTITLE = "Your letter will reference your rights as an Ontario consumer."

    const val FREE_LETTER_TITLE = "Your first letter is free"
    const val FREE_LETTER_DESCRIPTION = "No payment needed — start with one on us."
    const val FREE_LETTER_BADGE = "FREE"

    const val USE_CREDIT_TITLE = "Use letter credit"
    const val USE_CREDIT_BUTTON = "Use Free Letter"

    const val UNLOCK_PRO_TITLE = "Unlock Pro"
    const val UNLOCK_PRO_DESCRIPTION = "Unlimited letters for 6 months"
    const val UNLOCK_PRO_BUTTON = "Get 6 Months for $1.99"
    const val UNLOCK_PRO_BUTTON_SHORT = "Unlock for $1.99"

    const val PAYMENT_FOOTER = "Letters are generated instantly. You can copy, share, or email directly."

    // Preview Flow
    const val PREVIEW_TITLE = "Here's a preview of your letter"
    const val PREVIEW_SUBTITLE = "The full version is ready — unlock it when you're ready."
    const val PREVIEW_UNLOCK_PROMPT = "Unlock to reveal full letter"
    const val PREVIEW_UNLOCK_BODY = "Your personalized letter is ready. Unlock now to copy, share, or send it directly."

    // Result Screen
    const val RESULT_TITLE = "Your letter is ready"
    const val RESULT_SUBTITLE = "Review it below, then send it to the company."
    const val RESULT_COPY_SUCCESS = "Copied to clipboard"
    const val RESULT_TIP = "Tip: Send this letter by email for the fastest response. Keep a copy for your records."

    const val COPY_BUTTON = "Copy"
    const val SHARE_BUTTON = "Share"
    const val EMAIL_BUTTON = "Send via Email"
    const val NEW_LETTER_BUTTON = "Write Another Letter"

    // Settings / Pro Status
    object Settings {
        const val TITLE = "Settings"

        const val STATUS_PRO = "Pro Member"
        const val STATUS_FREE = "Free"
        const val STATUS_EXPIRES = "Access until"
        const val STATUS_EXPIRED = "Expired"

        const val PRO_ACTIVE_MESSAGE = "You have full access to Power Letter."
        const val PRO_EXPIRED_MESSAGE = "Your Pro access has ended. Renew to continue creating letters."
        const val PRO_EXPIRING_SOON = "Your access expires soon. Renew anytime to keep your Pro features."

        const val WHATS_INCLUDED_TITLE = "What's included with Pro"
        const val FEATURE_1 = "Unlimited letters for 2 months"
        const val FEATURE_2 = "Copy, share, and email instantly"
        const val FEATURE_3 = "All complaint and refund types"
        const val FEATURE_4 = "No daily limits"

        const val RESTORE_TITLE = "Already unlocked Pro?"
        const val RESTORE_HELPER = "Restore your access if you've reinstalled or switched devices."
        const val RESTORE_BUTTON = "Restore Purchase"
        const val RESTORE_SUCCESS = "Welcome back! Your Pro access has been restored."

        const val UPGRADE_PROMPT = "2 free letters per day. Upgrade for unlimited."
        const val UPGRADE_BUTTON = "Get Pro — \$2.99 for 2 months"
    }

    // Trust & Reassurance
    object Trust {
        const val WHY_IT_WORKS_TITLE = "Why Power Letter works"
        const val WHY_1_TITLE = "Written with care"
        const val WHY_1_BODY = "Your letter uses respectful, professional language that companies take seriously."
        const val WHY_2_TITLE = "References your rights"
        const val WHY_2_BODY = "We include relevant Ontario consumer protections — no exaggeration, no empty threats."
        const val WHY_3_TITLE = "You stay in control"
        const val WHY_3_BODY = "Review, edit, and send the letter yourself. It's your voice, polished."

        const val PRIVACY_SHORT = "Your information stays on your device. We don't store, sell, or share your personal details."
        const val PRIVACY_LONG = "Power Letter processes your request securely and does not store your personal information. Your details are used only to generate your letter — nothing is saved to our servers or shared with third parties."

        const val DISCLAIMER = "Power Letter helps you write professional refund and complaint requests. This app provides general guidance only and is not a substitute for legal advice. Results may vary depending on the company and situation."
        const val NOT_LEGAL_ADVICE = "Power Letter is a writing tool and does not provide legal advice."
    }

    // Post-Unlock
    const val UNLOCK_SUCCESS_TITLE = "You're all set"
    const val UNLOCK_SUCCESS_MESSAGE = "Full access unlocked for 6 months. Your letter is ready to send."
    const val UNLOCK_SUCCESS_BUTTON = "View My Letter"

    // Loading States
    const val LOADING = "Loading..."
    const val LOADING_LETTER = "Writing your letter..."
    const val LOADING_PROCESSING = "Processing..."
    const val LOADING_ALMOST_DONE = "Almost ready..."

    // Button States
    const val BUTTON_DISABLED = "Fill in details above"
    const val BUTTON_MAYBE_LATER = "Maybe later"
    const val BUTTON_GOT_IT = "Got It"
    const val BUTTON_CONTACT_SUPPORT = "Contact Support"
}
