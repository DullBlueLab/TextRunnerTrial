package com.dullbluelab.textrunnertrial.data

import android.text.method.TextKeyListener.clear
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

class UserPreferencesRepository(
    private val dataStore: DataStore<Preferences>
) {
    private companion object {
        val KEY_TIMER_LIMIT = longPreferencesKey("timer_limit")
        val KEY_LOOP_LIMIT = intPreferencesKey("loop_limit")
        val KEY_FUNC_LIMIT = intPreferencesKey("function_limit")
        val KEY_GUIDE_DIALOG = booleanPreferencesKey("flag_guide_dialog")
        const val TAG = "UserPreference"

        private const val INITIAL_TIMER_LIMIT = 50L
        private const val INITIAL_LOOP_LIMIT = 1000000
        private const val INITIAL_FUNC_LIMIT = 1000
        private const val INITIAL_GUIDE_DIALOG = true

        private const val SAFETY_TIMER_LIMIT = 20L
        private const val SAFETY_LOOP_LIMIT = 100
        private const val SAFETY_FUNC_LIMIT = 100
    }

    val setting: Flow<SettingItem> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading preferences.", it)
                emit(emptyPreferences())
            }
        }
        .map { prefer ->
            SettingItem(
                timerLimit = prefer[KEY_TIMER_LIMIT] ?: INITIAL_TIMER_LIMIT,
                loopLimit = prefer[KEY_LOOP_LIMIT] ?: INITIAL_LOOP_LIMIT,
                functionLimit = prefer[KEY_FUNC_LIMIT] ?: INITIAL_FUNC_LIMIT,
                flagGuideDialog = prefer[KEY_GUIDE_DIALOG] ?: INITIAL_GUIDE_DIALOG
            )
        }

    suspend fun saveSetting(item: SettingItem) {
        val checked = safetyCheck(item)
        dataStore.edit { prefer ->
            prefer[KEY_TIMER_LIMIT] = checked.timerLimit
            prefer[KEY_LOOP_LIMIT] = checked.loopLimit
            prefer[KEY_FUNC_LIMIT] = checked.functionLimit
        }
    }

    suspend fun saveGuideDialog(flag: Boolean) {
        dataStore.edit { prefer ->
            prefer[KEY_GUIDE_DIALOG] = flag
        }
    }

    private fun safetyCheck(item: SettingItem) : SettingItem {
        val newItem = item.copy(
            timerLimit = if (item.timerLimit >= SAFETY_TIMER_LIMIT) item.timerLimit else SAFETY_TIMER_LIMIT,
            loopLimit = if (item.loopLimit >= SAFETY_LOOP_LIMIT) item.loopLimit else SAFETY_LOOP_LIMIT,
            functionLimit = if (item.functionLimit >= SAFETY_FUNC_LIMIT) item.functionLimit else SAFETY_FUNC_LIMIT
        )
        return newItem
    }

    fun safetyCheck(key: String, value: Int): Boolean {
        return when(key) {
            "timer" -> (value >= SAFETY_TIMER_LIMIT)
            "loop" -> (value >= SAFETY_LOOP_LIMIT)
            "func" -> (value >= SAFETY_FUNC_LIMIT)
            else -> false
        }
    }
    suspend fun clearDataStore() {
        dataStore.edit { it.clear() }
    }
}

data class SettingItem(
    val timerLimit:Long = 0L,
    val loopLimit:Int  = 0,
    val functionLimit: Int = 0,
    val flagGuideDialog: Boolean = true
)
