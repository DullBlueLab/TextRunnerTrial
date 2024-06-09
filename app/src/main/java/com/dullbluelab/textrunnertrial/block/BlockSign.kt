package com.dullbluelab.textrunnertrial.block

import com.dullbluelab.textrunnertrial.logic.Parser
import com.dullbluelab.textrunnertrial.logic.Syntax

class BlockSign(strings: String, lineNO: Int) : CodeBlock(Type.SIGN, strings, lineNO) {

    constructor(box: Parser.TextBox) : this("", box.lineNO) {
        var popChar = box.getPos() ?: ' '
        while (matchCharType(popChar, Syntax.Chars.Type.SIGN)) {
            strings += popChar
            popChar = box.getNext() ?: ' '
        }
    }
}
