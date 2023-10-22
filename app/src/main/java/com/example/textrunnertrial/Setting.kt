package com.example.textrunnertrial

import android.content.Context

private const val INITIAL_TIMER_LIMIT = 50L
private const val INITIAL_LOOP_LIMIT = 1000000

private const val SAFETY_TIMER_LIMIT = 20L
private const val SAFETY_LOOP_LIMIT = 100

private const val KEY_TIMER_LIMIT = "timer_limit"
private const val KEY_LOOP_LIMIT = "loop_limit"

class Setting() {
    private var activity: MainActivity? = null
    var timerLimit:Long = 0L
    var loopLimit:Int  = 0

    fun setup(activity: MainActivity) {
        this.activity = activity
    }

    fun initialize() {
        timerLimit = INITIAL_TIMER_LIMIT
        loopLimit = INITIAL_LOOP_LIMIT
    }

    fun load(): Boolean {
        var result = false
        val pref = activity?.getPreferences(Context.MODE_PRIVATE)

        if (pref == null) {
            initialize()
            result = save()
        }
        else {
            timerLimit = pref.getLong(KEY_TIMER_LIMIT, INITIAL_TIMER_LIMIT)
            loopLimit = pref.getInt(KEY_LOOP_LIMIT, INITIAL_LOOP_LIMIT)
            result = true
        }
        return result
    }

    fun safetyCheck() {
        if (timerLimit < SAFETY_TIMER_LIMIT) timerLimit = SAFETY_TIMER_LIMIT
        if (loopLimit < SAFETY_LOOP_LIMIT) loopLimit = SAFETY_LOOP_LIMIT
    }

    fun save(): Boolean {
        val pref = activity?.getPreferences(Context.MODE_PRIVATE) ?: return false
        with (pref.edit()) {
            putLong(KEY_TIMER_LIMIT, timerLimit)
            putInt(KEY_LOOP_LIMIT, loopLimit)
            apply()
        }
        return true
    }
}