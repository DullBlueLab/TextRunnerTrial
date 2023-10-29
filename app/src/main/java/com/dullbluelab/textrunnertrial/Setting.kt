package com.dullbluelab.textrunnertrial

import android.content.Context

private const val INITIAL_TIMER_LIMIT = 50L
private const val INITIAL_LOOP_LIMIT = 1000000
private const val INITIAL_FUNC_LIMIT = 1000

private const val SAFETY_TIMER_LIMIT = 20L
private const val SAFETY_LOOP_LIMIT = 100
private const val SAFETY_FUNC_LIMIT = 100

private const val KEY_TIMER_LIMIT = "timer_limit"
private const val KEY_LOOP_LIMIT = "loop_limit"
private const val KEY_FUNC_LIMIT = "function_limit"

class Setting() {
    private var activity: MainActivity? = null
    var timerLimit:Long = 0L
    var loopLimit:Int  = 0
    var functionLimit: Int = 0

    fun setup(activity: MainActivity) {
        this.activity = activity
    }

    fun initialize() {
        timerLimit = INITIAL_TIMER_LIMIT
        loopLimit = INITIAL_LOOP_LIMIT
        functionLimit = INITIAL_FUNC_LIMIT
    }

    fun load(): Boolean {
        val result: Boolean
        val pref = activity?.getPreferences(Context.MODE_PRIVATE)

        if (pref == null) {
            initialize()
            result = save()
        }
        else {
            timerLimit = pref.getLong(KEY_TIMER_LIMIT, INITIAL_TIMER_LIMIT)
            loopLimit = pref.getInt(KEY_LOOP_LIMIT, INITIAL_LOOP_LIMIT)
            functionLimit = pref.getInt(KEY_FUNC_LIMIT, INITIAL_FUNC_LIMIT)
            result = true
        }
        return result
    }

    fun safetyCheck() {
        if (timerLimit < SAFETY_TIMER_LIMIT) timerLimit = SAFETY_TIMER_LIMIT
        if (loopLimit < SAFETY_LOOP_LIMIT) loopLimit = SAFETY_LOOP_LIMIT
        if (functionLimit < SAFETY_FUNC_LIMIT) functionLimit = SAFETY_FUNC_LIMIT
    }

    fun save(): Boolean {
        val pref = activity?.getPreferences(Context.MODE_PRIVATE) ?: return false
        with (pref.edit()) {
            putLong(KEY_TIMER_LIMIT, timerLimit)
            putInt(KEY_LOOP_LIMIT, loopLimit)
            putInt(KEY_FUNC_LIMIT, functionLimit)
            apply()
        }
        return true
    }
}