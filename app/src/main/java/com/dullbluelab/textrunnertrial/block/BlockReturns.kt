package com.dullbluelab.textrunnertrial.block

import com.dullbluelab.textrunnertrial.Errors
import com.dullbluelab.textrunnertrial.logic.Syntax

class BlockReturns() : CodeBlock(Type.RETURN, Syntax.Reserved.Word.RETURN, 0) {
    private var result: CodeBlock? = null

    fun result() = result

    constructor(lists: BlockLists, index: Int) : this() {
        val codeNO = Syntax.Reserved.codeNO(Syntax.Reserved.Key.RETURN)

        var block = lists.block(index)
        if (block == null || block.type() != Type.WORD || (block as BlockWord).codeNO() != codeNO)
            throw Errors.Logic("CodeBlock.Returns")

        lists.removeAt(index)
        lineNO = block.lineNO()

        block = lists.block(index) ?: return
        if (block.type() == Type.BRACKET
            && (block as BlockBracket).strings() == Syntax.Chars.ARGUMENT_BRACKET) {
            result = block
            lists.removeAt(index)
        }
    }

    override fun dump(shift: String): String {
        var text = "$shift[return] =\n"
        result?.let { text += it.dump("$shift  ") }
        return text
    }
}
