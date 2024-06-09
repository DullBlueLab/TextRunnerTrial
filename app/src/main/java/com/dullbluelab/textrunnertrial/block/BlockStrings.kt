package com.dullbluelab.textrunnertrial.block

import com.dullbluelab.textrunnertrial.logic.Parser
import com.dullbluelab.textrunnertrial.logic.Syntax

class BlockStrings(strings: String, lineNO: Int) : CodeBlock(Type.STRING, strings, lineNO) {
    private var stringChar: Char = ' '

    constructor(box: Parser.TextBox) : this( "", box.lineNO) {
        stringChar = box.getInc() ?: return
        var popChar: Char? = box.getPos()

        while (popChar != null && popChar != stringChar) {

            if (matchCharType(popChar, Syntax.Chars.Type.ESCAPE)) {
                popChar = box.getNext() ?: break
                strings += Syntax.Chars.getEscapeChar(popChar)
            }
            else {
                strings += popChar
            }
            popChar = box.getNext()
        }
        if (!box.isEnd()) box.inc()
    }

    override fun text(): String = strings()
}
