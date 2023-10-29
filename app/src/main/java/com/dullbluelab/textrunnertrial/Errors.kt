package com.dullbluelab.textrunnertrial

import android.content.Context

object Errors {

    class Syntax(message: String, var lineNO: Int = 0) : Exception(message) {

        constructor(key: Key, addText: String = "", lineNO: Int = 0) : this("${message(key)} $addText", lineNO)

        fun threwMessage() = super.message ?: ""

        override val message: String
            get() = if (lineNO > 0) "${super.message} line:${lineNO.toString()}" else "${super.message}"
    }

    class Logic(message: String) : Exception("Safety stop $message") {

    }

    enum class Key {
        INTERNAL, SYNTAX, SAFETY,
        NOT_MATCH_ARGUMENT, NOT_MATCH_TYPE, NOT_MATCH_BRACKET,
        ILLEGAL_CONDITION, ILLEGAL_ARGUMENT, ILLEGAL_METHOD, ILLEGAL_LIST_INDEX,
        OUT_OF_RANGE, OUT_OF_SIZE, LIMIT_OVER, LIMIT_FUNC,
        UNKNOWN_CONST, UNKNOWN, UNKNOWN_OPERATOR,
        METHOD, BRACKET,
    }

    private val table: MutableMap<Key, String> = mutableMapOf()

    fun loadResource(context: Context) {
        table[Key.INTERNAL] = context.getString(R.string.error_internal)
        table[Key.NOT_MATCH_ARGUMENT] = context.getString(R.string.error_not_match_argument)
        table[Key.UNKNOWN_OPERATOR] = context.getString(R.string.error_unknown_operation)
        table[Key.ILLEGAL_CONDITION] = context.getString(R.string.error_illegal_condition)
        table[Key.ILLEGAL_ARGUMENT] = context.getString(R.string.error_illegal_argument)

        table[Key.ILLEGAL_METHOD] = context.getString(R.string.error_illegal_method)
        table[Key.ILLEGAL_LIST_INDEX] = context.getString(R.string.error_illegal_list_index)
        table[Key.OUT_OF_RANGE] = context.getString(R.string.error_out_of_range)
        table[Key.OUT_OF_SIZE] = context.getString(R.string.error_out_of_size)
        table[Key.UNKNOWN_CONST] = context.getString(R.string.error_unknown_const)
        table[Key.UNKNOWN] = context.getString(R.string.error_unknown)

        table[Key.SYNTAX] = context.getString(R.string.error_syntax)
        table[Key.SAFETY] = context.getString(R.string.error_safety)
        table[Key.NOT_MATCH_TYPE] = context.getString(R.string.error_not_match_type)
        table[Key.METHOD] = context.getString(R.string.error_method)
        table[Key.LIMIT_OVER] = context.getString(R.string.error_limit_over)
        table[Key.LIMIT_FUNC] = context.getString(R.string.limit_func)

        table[Key.BRACKET] = context.getString(R.string.error_bracket)
        table[Key.NOT_MATCH_BRACKET] = context.getString(R.string.error_not_match_bracket)
    }

    fun message(key: Key): String = table[key] ?: "error"
}