package com.dullbluelab.textrunnertrial.block

import com.dullbluelab.textrunnertrial.logic.Parser
import com.dullbluelab.textrunnertrial.logic.Syntax

class BlockNumber(strings: String, lineNO: Int) : CodeBlock(Type.NUMBER, strings, lineNO) {

    override fun text(): String = strings

    constructor(box: Parser.TextBox) : this("", box.lineNO) {
        var popChar = box.getPos() ?: ' '
        while (matchCharType(popChar, Syntax.Chars.Type.NUMBERS)) {
            strings += popChar
            popChar = box.getNext() ?: ' '
        }
    }
}
