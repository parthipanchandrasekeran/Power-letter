package com.powerletter.data.subscription

import android.content.Context
import android.content.SharedPreferences
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ProStatusManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME, Context.MODE_PRIVATE
    )

    companion object {
        private const val PREFS_NAME = "power_letter_pro"
        private const val KEY_IS_PRO = "is_pro"
        private const val KEY_EXPIRY_DATE = "expiry_date"
        private const val PRO_DURATION_DAYS = 60L // 2 months
    }

    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    val isPro: Boolean
        get() {
            if (!prefs.getBoolean(KEY_IS_PRO, false)) {
                return false
            }
            // Check if subscription has expired
            val expiryString = prefs.getString(KEY_EXPIRY_DATE, null) ?: return false
            val expiryDate = LocalDate.parse(expiryString, dateFormatter)

            if (LocalDate.now().isAfter(expiryDate)) {
                // Subscription expired, clear pro status
                clearProStatus()
                return false
            }
            return true
        }

    val expiryDate: String?
        get() {
            if (!isPro) return null
            val expiryString = prefs.getString(KEY_EXPIRY_DATE, null) ?: return null
            val date = LocalDate.parse(expiryString, dateFormatter)
            return date.format(DateTimeFormatter.ofPattern("MMMM d, yyyy"))
        }

    val daysRemaining: Int
        get() {
            if (!isPro) return 0
            val expiryString = prefs.getString(KEY_EXPIRY_DATE, null) ?: return 0
            val expiryDate = LocalDate.parse(expiryString, dateFormatter)
            val today = LocalDate.now()
            return (expiryDate.toEpochDay() - today.toEpochDay()).toInt().coerceAtLeast(0)
        }

    fun activatePro() {
        val expiryDate = LocalDate.now().plusDays(PRO_DURATION_DAYS)
        prefs.edit()
            .putBoolean(KEY_IS_PRO, true)
            .putString(KEY_EXPIRY_DATE, expiryDate.format(dateFormatter))
            .apply()
    }

    fun restorePro(expiryDateString: String): Boolean {
        return try {
            val expiryDate = LocalDate.parse(expiryDateString, dateFormatter)
            if (LocalDate.now().isBefore(expiryDate) || LocalDate.now().isEqual(expiryDate)) {
                prefs.edit()
                    .putBoolean(KEY_IS_PRO, true)
                    .putString(KEY_EXPIRY_DATE, expiryDateString)
                    .apply()
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    private fun clearProStatus() {
        prefs.edit()
            .putBoolean(KEY_IS_PRO, false)
            .remove(KEY_EXPIRY_DATE)
            .apply()
    }
}
