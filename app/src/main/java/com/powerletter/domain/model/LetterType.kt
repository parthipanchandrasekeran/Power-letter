package com.powerletter.domain.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Flight
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.outlined.Subscriptions
import androidx.compose.ui.graphics.vector.ImageVector

enum class LetterType(
    val displayName: String,
    val description: String,
    val icon: ImageVector
) {
    GYM(
        displayName = "Gym Membership",
        description = "Request a refund for unused membership fees or unwanted charges.",
        icon = Icons.Outlined.FitnessCenter
    ),
    TELECOM(
        displayName = "Phone or Internet Bill",
        description = "Dispute unexpected charges or billing errors on your account.",
        icon = Icons.Outlined.PhoneAndroid
    ),
    SUBSCRIPTION(
        displayName = "Subscription Service",
        description = "Cancel a subscription and request a refund for recent charges.",
        icon = Icons.Outlined.Subscriptions
    ),
    AIRLINE(
        displayName = "Flight Issue",
        description = "Request compensation for a delayed, cancelled, or disrupted flight.",
        icon = Icons.Outlined.Flight
    )
}
