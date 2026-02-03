package com.powerletter.data.model

import com.google.gson.annotations.SerializedName

data class GeneratedLetter(
    @SerializedName("subject")
    val subject: String,

    @SerializedName("emailBody")
    val emailBody: String,

    @SerializedName("legalBasis")
    val legalBasis: List<LegalBasis>,

    @SerializedName("tone")
    val tone: Tone
) {
    fun isValid(): Boolean {
        return subject.length in 10..100 &&
               emailBody.length in 200..3000 &&
               legalBasis.size <= 5
    }
}

enum class LegalBasis(val displayName: String) {
    @SerializedName("Consumer Protection Act, 2002 (Ontario)")
    CONSUMER_PROTECTION_ACT("Consumer Protection Act, 2002 (Ontario)"),

    @SerializedName("Sale of Goods Act (Ontario)")
    SALE_OF_GOODS_ACT("Sale of Goods Act (Ontario)"),

    @SerializedName("Air Passenger Protection Regulations (Canada)")
    AIR_PASSENGER_PROTECTION("Air Passenger Protection Regulations (Canada)"),

    @SerializedName("Wireless Code (CRTC)")
    WIRELESS_CODE("Wireless Code (CRTC)"),

    @SerializedName("Internet Code (CRTC)")
    INTERNET_CODE("Internet Code (CRTC)"),

    @SerializedName("Competition Act (Canada)")
    COMPETITION_ACT("Competition Act (Canada)"),

    @SerializedName("Electronic Commerce Act, 2000 (Ontario)")
    ELECTRONIC_COMMERCE_ACT("Electronic Commerce Act, 2000 (Ontario)")
}

enum class Tone {
    @SerializedName("professional")
    PROFESSIONAL,

    @SerializedName("firm")
    FIRM
}
