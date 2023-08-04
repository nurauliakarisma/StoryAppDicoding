package com.example.storyappdicoding.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.storyappdicoding.utils.AccountPreferences.Companion.preferenceName
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = preferenceName)

class AccountPreferences private constructor(private val dataStore: DataStore<Preferences>) {

    suspend fun saveToken(
        token: String,
    ) {
        dataStore.edit { prefs ->
            prefs[accountToken] = token
        }
    }

    suspend fun clearToken() {
        dataStore.edit { prefs ->
            prefs.clear()
        }
    }

    fun getToken() = dataStore.data.map { it[accountToken] ?: preferenceDefaultValue }

    companion object {
        @Volatile
        private var INSTANCE: AccountPreferences? = null

        fun getPrefInstance(dataStore: DataStore<Preferences>): AccountPreferences {
            return INSTANCE ?: synchronized(this) {
                val instance = AccountPreferences(dataStore)
                INSTANCE = instance
                instance
            }
        }

        const val preferenceName: String = "preferences"
        const val preferenceDefaultValue: String = "default_value"

        const val TOKEN_ID = "token_id"
        val accountToken = stringPreferencesKey(TOKEN_ID)
    }
}