package com.example.rss.util

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "reading_prefs")

object ReadingPreferences {
    private val DARK_MODE = booleanPreferencesKey("dark_mode")
    private val FONT_SCALE = intPreferencesKey("font_scale")

    fun darkModeFlow(context: Context): Flow<Boolean> =
        context.dataStore.data.map { it[DARK_MODE] ?: false }

    fun fontScaleFlow(context: Context): Flow<Int> =
        context.dataStore.data.map { it[FONT_SCALE] ?: 100 }

    suspend fun setDarkMode(context: Context, enabled: Boolean) {
        context.dataStore.edit { it[DARK_MODE] = enabled }
    }

    suspend fun setFontScale(context: Context, scale: Int) {
        context.dataStore.edit { it[FONT_SCALE] = scale.coerceIn(80, 160) }
    }
}
