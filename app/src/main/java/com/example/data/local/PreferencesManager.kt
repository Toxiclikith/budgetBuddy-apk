package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "budget_buddy_preferences")

class PreferencesManager(private val context: Context) {

    companion object {
        val CURRENCY_SYMBOL = stringPreferencesKey("currency_symbol")
        val DAILY_LIMIT = doublePreferencesKey("daily_limit")
        val PIN_LOCK_ENABLED = booleanPreferencesKey("pin_lock_enabled")
        val SECURITY_PIN = stringPreferencesKey("security_pin")
        val SAVINGS_STREAK = intPreferencesKey("savings_streak")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val LAST_STREAK_UPDATE = longPreferencesKey("last_streak_update")
    }

    val lastStreakUpdate: Flow<Long> = context.dataStore.data.map { preferences ->
        preferences[LAST_STREAK_UPDATE] ?: 0L
    }

    val currencySymbol: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[CURRENCY_SYMBOL] ?: try {
            val symbol = java.util.Currency.getInstance(java.util.Locale.getDefault()).symbol
            if (symbol != null && symbol.length <= 3) symbol else "$"
        } catch (e: Exception) {
            "$"
        }
    }

    val dailySpendingLimit: Flow<Double> = context.dataStore.data.map { preferences ->
        preferences[DAILY_LIMIT] ?: 50.0
    }

    val pinLockEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PIN_LOCK_ENABLED] ?: false
    }

    val securityPin: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[SECURITY_PIN] ?: "1234"
    }

    val savingsStreak: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[SAVINGS_STREAK] ?: 0
    }

    val onboardingCompleted: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[ONBOARDING_COMPLETED] ?: false
    }

    suspend fun setCurrencySymbol(symbol: String) {
        context.dataStore.edit { preferences ->
            preferences[CURRENCY_SYMBOL] = symbol
        }
    }

    suspend fun setDailySpendingLimit(limit: Double) {
        context.dataStore.edit { preferences ->
            preferences[DAILY_LIMIT] = limit
        }
    }

    suspend fun setPinLockEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PIN_LOCK_ENABLED] = enabled
        }
    }

    suspend fun setSecurityPin(pin: String) {
        context.dataStore.edit { preferences ->
            preferences[SECURITY_PIN] = pin
        }
    }

    suspend fun incrementSavingsStreak(timestamp: Long) {
        context.dataStore.edit { preferences ->
            val current = preferences[SAVINGS_STREAK] ?: 0
            preferences[SAVINGS_STREAK] = current + 1
            preferences[LAST_STREAK_UPDATE] = timestamp
        }
    }

    suspend fun setSavingsStreak(count: Int, timestamp: Long) {
        context.dataStore.edit { preferences ->
            preferences[SAVINGS_STREAK] = count
            preferences[LAST_STREAK_UPDATE] = timestamp
        }
    }

    suspend fun resetSavingsStreak() {
        context.dataStore.edit { preferences ->
            preferences[SAVINGS_STREAK] = 0
        }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[ONBOARDING_COMPLETED] = completed
        }
    }
}
