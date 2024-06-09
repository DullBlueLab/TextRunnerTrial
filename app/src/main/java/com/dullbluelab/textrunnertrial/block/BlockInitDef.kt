package com.dullbluelab.textrunnertrial.block

import com.dullbluelab.textrunnertrial.Errors
import com.dullbluelab.textrunnertrial.logic.References
import com.dullbluelab.textrunnertrial.logic.Syntax

class BlockInitDef() : BlockFunDef(Syntax.Reserved.Word.INIT) {

    constructor(lists: BlockLists, start: Int) : this() {
        val codeNO = Syntax.Reserved.codeNO(Syntax.Reserved.Key.INIT)
        val argsChars = Syntax.Chars.ARGUMENT_BRACKET
        val stateChars = Syntax.Chars.STATEMENT_BRACKET
        var index = start

        var block = lists.block(index)
        if (block == null || block.type() != CodeBlock.Type.WORD || (block as BlockWord).codeNO() != codeNO)
            throw Errors.Logic("CodeBlock.InitDef")

        lists.removeAt(index)

        lineNO = block.lineNO()

        while (index < lists.size() && statement == null) {
            block = lists.block(index)!!
            // argument
            if (block.type() == CodeBlock.Type.BRACKET && block.strings() == argsChars) {
                val args = (block as BlockBracket).lists()
                args?.let { arguments = BlockArgumentList(it) }
                lists.removeAt(index)
            }
            // statement
            else if (block.type() == CodeBlock.Type.BRACKET && block.strings() == stateChars) {
                statement = (block as BlockBracket)
                lists.removeAt(index)
            }
            else {
                index++
            }
        }
        if (arguments == null || statement == null) throw Errors.Syntax(Errors.Key.SYNTAX, lineNO = lineNO)
    }

    override fun setToReference(ref: References.Lists) {
        ref.appendFun(strings, arguments, returns = null, this, childRef = null)
    }

    override fun dump(shift: String): String {
        var text = "$shift[init def: $strings]\n"
        text += arguments?.dump("$shift  ")
        text += "$shift  statement = \n" + statement?.dump("$shift  ")
        return text
    }
}
