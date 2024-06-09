package com.dullbluelab.textrunnertrial

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.dullbluelab.textrunnertrial.data.ImageFiles
import com.dullbluelab.textrunnertrial.data.LibraryDatabase
import com.dullbluelab.textrunnertrial.data.LibraryRepository
import com.dullbluelab.textrunnertrial.data.UserPreferencesRepository

private const val RUNNER_PREFERENCE_NAME = "text_runner_preferences"
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = RUNNER_PREFERENCE_NAME
)

class RunnerApplication : Application() {
    lateinit var container: AppContainer
    lateinit var userPreferencesRepository: UserPreferencesRepository
    lateinit var imageFiles: ImageFiles

    override fun onCreate() {
        super.onCreate()
        container = AppDataContainer(this)
        userPreferencesRepository = UserPreferencesRepository(dataStore)
        imageFiles = ImageFiles(this)
    }
}

interface AppContainer {
    val libraryRepository: LibraryRepository
}

class AppDataContainer(private val context: Context) : AppContainer {
    override val libraryRepository: LibraryRepository by lazy {
        val database = LibraryDatabase.getDatabase(context)
        LibraryRepository(database.directoryDao(), database, context)
    }
}
