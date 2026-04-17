package com.example.meetingmemo.data.preference

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "user_preferences")

@Singleton
class UserPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val defaultEmailKey = stringPreferencesKey("default_email")

    val defaultEmail: Flow<String> = context.dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { preferences -> preferences[defaultEmailKey].orEmpty() }

    suspend fun setDefaultEmail(email: String) {
        context.dataStore.edit { preferences ->
            preferences[defaultEmailKey] = email
        }
    }
}
