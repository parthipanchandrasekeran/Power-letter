package com.powerletter.data.model

import com.powerletter.domain.model.LetterType

object LetterTypeToLegalBasis {

    fun getApplicableLegalBasis(letterType: LetterType): List<LegalBasis> {
        return when (letterType) {
            LetterType.GYM -> listOf(
                LegalBasis.CONSUMER_PROTECTION_ACT
            )
            LetterType.TELECOM -> listOf(
                LegalBasis.CONSUMER_PROTECTION_ACT,
                LegalBasis.WIRELESS_CODE,
                LegalBasis.INTERNET_CODE
            )
            LetterType.SUBSCRIPTION -> listOf(
                LegalBasis.CONSUMER_PROTECTION_ACT,
                LegalBasis.ELECTRONIC_COMMERCE_ACT
            )
            LetterType.AIRLINE -> listOf(
                LegalBasis.AIR_PASSENGER_PROTECTION,
                LegalBasis.CONSUMER_PROTECTION_ACT
            )
        }
    }
}
