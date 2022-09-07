package com.vroomvroomrider.android.repository.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AppPreferences @Inject constructor (
    private val dataStore: DataStore<Preferences>
) {

        val token: Flow<String?>
        get() = dataStore.data.map { preferences ->
            preferences[TOKEN]
        }

        suspend fun saveToken(accessToken: String) {
            dataStore.edit { preferences ->
                preferences[TOKEN] = accessToken
            }
        }

        suspend fun clear() {
            dataStore.edit { preferences ->
                preferences.clear()
            }
        }

        companion object {
            private val TOKEN = stringPreferencesKey("key_token")
            private val LOCATION = stringPreferencesKey("key_location")
        }

}