package com.powerletter.data.usage

import android.content.Context
import android.content.SharedPreferences
import com.powerletter.data.subscription.ProStatusManager
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class DailyUsageTracker(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME, Context.MODE_PRIVATE
    )

    private val proStatusManager = ProStatusManager(context)

    companion object {
        private const val PREFS_NAME = "power_letter_usage"
        private const val KEY_USAGE_DATE = "usage_date"
        private const val KEY_USAGE_COUNT = "usage_count"
        private const val DAILY_FREE_LIMIT = 2
    }

    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    val isPro: Boolean
        get() = proStatusManager.isPro

    val remainingFreeLetters: Int
        get() {
            if (isPro) return Int.MAX_VALUE // Unlimited for Pro
            resetIfNewDay()
            return (DAILY_FREE_LIMIT - getCurrentUsageCount()).coerceAtLeast(0)
        }

    val hasFreeLettersAvailable: Boolean
        get() = isPro || remainingFreeLetters > 0

    val dailyLimit: Int
        get() = if (isPro) Int.MAX_VALUE else DAILY_FREE_LIMIT

    fun consumeFreeLetter(): Boolean {
        if (isPro) return true // Pro users always succeed

        resetIfNewDay()
        val currentCount = getCurrentUsageCount()

        if (currentCount >= DAILY_FREE_LIMIT) {
            return false
        }

        prefs.edit()
            .putInt(KEY_USAGE_COUNT, currentCount + 1)
            .apply()

        return true
    }

    private fun resetIfNewDay() {
        val today = LocalDate.now().format(dateFormatter)
        val storedDate = prefs.getString(KEY_USAGE_DATE, null)

        if (storedDate != today) {
            prefs.edit()
                .putString(KEY_USAGE_DATE, today)
                .putInt(KEY_USAGE_COUNT, 0)
                .apply()
        }
    }

    private fun getCurrentUsageCount(): Int {
        return prefs.getInt(KEY_USAGE_COUNT, 0)
    }
}
