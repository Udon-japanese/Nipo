package com.example.nipo.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.dataStore by preferencesDataStore(name = "nipo_prefs")

private val KEY_LOCATION_PERMISSION_ASKED = booleanPreferencesKey("location_permission_asked")

class AppPreferences(private val context: Context) {

    suspend fun hasAskedLocationPermission(): Boolean =
        context.dataStore.data.first()[KEY_LOCATION_PERMISSION_ASKED] ?: false

    suspend fun setAskedLocationPermission() {
        context.dataStore.edit { it[KEY_LOCATION_PERMISSION_ASKED] = true }
    }
}
