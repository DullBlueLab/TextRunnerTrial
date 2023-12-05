package com.dullbluelab.textrunnertrial

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.dullbluelab.textrunnertrial.data.UserPreferencesRepository

private const val RUNNER_PREFERENCE_NAME = "text_runner_preferences"
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = RUNNER_PREFERENCE_NAME
)

class RunnerApplication : Application() {
    lateinit var userPreferencesRepository: UserPreferencesRepository

    override fun onCreate() {
        super.onCreate()
        userPreferencesRepository = UserPreferencesRepository(dataStore)
    }
}
