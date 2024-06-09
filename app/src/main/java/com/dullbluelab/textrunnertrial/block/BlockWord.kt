package com.dullbluelab.textrunnertrial.block

import com.dullbluelab.textrunnertrial.logic.Parser
import com.dullbluelab.textrunnertrial.logic.Syntax
import com.dullbluelab.textrunnertrial.logic.WordList

class BlockWord(strings: String, lineNO: Int) : CodeBlock(Type.WORD, strings, lineNO) {
    private var number: Int = 0

    override fun name(): String = strings
    override fun text(): String = strings
    fun codeNO(): Int = number

    constructor(box: Parser.TextBox, wordList: WordList) : this("", box.lineNO) {
        var popChar = box.getPos() ?: ' '
        if (matchCharType(popChar, Syntax.Chars.Type.WORD_START)) {
            strings += popChar

            popChar = box.getNext() ?: ' '
            while (matchCharType(popChar, Syntax.Chars.Type.WORDS)) {
                strings += popChar
                popChar = box.getNext() ?: ' '
            }
            number = Syntax.Reserved.codeNO(strings())
            if (number == 0) number = wordList.entry(strings())
        }
    }

    fun reservedKey(): Syntax.Reserved.Key = Syntax.Reserved.key(strings)
    fun isReservedKey(): Boolean = (number < 0)

    override fun dump(shift: String): String = "$shift[word:$number:${strings()}] \n"
}
