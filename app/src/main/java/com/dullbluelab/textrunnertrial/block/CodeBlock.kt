package com.dullbluelab.textrunnertrial.block

import com.dullbluelab.textrunnertrial.logic.Syntax

abstract class CodeBlock(
    private var type: Type = Type.NULL,
    protected var strings: String = "",
    protected var lineNO: Int = 0
) {
    enum class Type {
        NULL,
        WORD, NUMBER, SIGN, BRACKET, STRING, LIST,
        OBJECT_REF, VAR_DEF, CONST_DEF, FUN_DEF, INIT_DEF, CLASS_DEF,
        IF, FOR, WHILE, WHEN, FUN_CALL, RETURN
    }

    companion object {
        const val SUBJECT = 0
        const val ARGUMENT = 1

    }

    fun type() = type
    fun lineNO() = lineNO
    fun strings() = strings

    open fun name() = ""
    open fun setName(name: String) {}
    open fun child(index: Int): CodeBlock? = null
    open fun text() = ""

    open fun findListAndRun(process: (BlockLists) -> Unit) {}

    open fun findBracketAndRun(bracket: String, process: (BlockLists) -> Unit) {}

    fun matchCharType(char: Char, type: Syntax.Chars.Type) =
        (char != ' ' && Syntax.Chars.isMatch(char, type))

    open fun dump(shift: String): String = "$shift[$type:$strings] \n"
}