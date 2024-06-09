package com.dullbluelab.textrunnertrial.objects

import com.dullbluelab.textrunnertrial.action.Spaces
import com.dullbluelab.textrunnertrial.logic.Syntax

abstract class RunObject(val type: Type) {
    enum class Type {
        INSTANCE, // METHOD,
        INTS, DOUBLES, STRINGS, BOOLEANS,
        VOIDS, ERRORS,
        LISTS, IMAGES,
    }

    companion object {
        fun isValue(type: Type): Boolean =
            (type == Type.INTS || type == Type.DOUBLES || type == Type.STRINGS
                    || type == Type.BOOLEANS)

        private val RW = Syntax.Reserved.Word
        private val MW = Syntax.Method.Word

        fun typeValue(word: String): Type? =
            when (word) {
                RW.INT     -> Type.INTS
                RW.STRING  -> Type.STRINGS
                RW.DOUBLE  -> Type.DOUBLES
                RW.BOOLEAN -> Type.BOOLEANS
                RW.LIST    -> Type.LISTS
                RW.IMAGE   -> Type.IMAGES
                else -> null
            }
    }

    abstract fun toRunString(): RunString
    abstract fun isRunValue(): Boolean
    abstract fun typeWord(): String

    open fun execute(method: String, arguments: MutableList<RunObject>): RunObject? {
        var result: RunObject? = null
        if (arguments.size == 0) {
            result = when(method) {
                MW.TO_STRING -> toRunString()
                MW.TYPE -> RunString(typeWord())
                else -> null
            }
        }
        return result
    }

    open fun matchType(typeName: Spaces.Names): Boolean {
        return (typeWord() != typeName.text())
    }

    open fun typeError() = (type == Type.ERRORS)
}