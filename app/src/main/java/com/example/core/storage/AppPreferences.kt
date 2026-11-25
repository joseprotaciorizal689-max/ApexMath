package com.example.core.storage

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.core.math.AngleUnit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.apexDataStore: DataStore<Preferences> by preferencesDataStore(name = "apexmath_prefs")

data class UserSettings(
    val angleUnit: AngleUnit = AngleUnit.DEG,
    val fractionDisplay: Boolean = false,
    val decimalPrecision: Int = 10,
    val hapticEnabled: Boolean = true,
    val showGridLabels: Boolean = true
)

class AppPreferences(private val context: Context) {

    private object Keys {
        val ANGLE_UNIT = stringPreferencesKey("angle_unit")
        val FRACTION_DISPLAY = booleanPreferencesKey("fraction_display")
        val PRECISION = intPreferencesKey("decimal_precision")
        val HAPTIC = booleanPreferencesKey("haptic_enabled")
        val GRID_LABELS = booleanPreferencesKey("show_grid_labels")
    }

    val settingsFlow: Flow<UserSettings> = context.apexDataStore.data.map { prefs ->
        val angle = runCatching {
            AngleUnit.valueOf(prefs[Keys.ANGLE_UNIT] ?: AngleUnit.DEG.name)
        }.getOrDefault(AngleUnit.DEG)
        UserSettings(
            angleUnit = angle,
            fractionDisplay = prefs[Keys.FRACTION_DISPLAY] ?: false,
            decimalPrecision = (prefs[Keys.PRECISION] ?: 10).coerceIn(4, 12),
            hapticEnabled = prefs[Keys.HAPTIC] ?: true,
            showGridLabels = prefs[Keys.GRID_LABELS] ?: true
        )
    }

    suspend fun setAngleUnit(unit: AngleUnit) {
        context.apexDataStore.edit { it[Keys.ANGLE_UNIT] = unit.name }
    }

    suspend fun setFractionDisplay(enabled: Boolean) {
        context.apexDataStore.edit { it[Keys.FRACTION_DISPLAY] = enabled }
    }

    suspend fun setPrecision(precision: Int) {
        context.apexDataStore.edit { it[Keys.PRECISION] = precision.coerceIn(4, 12) }
    }

    suspend fun setHapticEnabled(enabled: Boolean) {
        context.apexDataStore.edit { it[Keys.HAPTIC] = enabled }
    }

    suspend fun setShowGridLabels(enabled: Boolean) {
        context.apexDataStore.edit { it[Keys.GRID_LABELS] = enabled }
    }
}
