package com.dullbluelab.textrunnertrial.block

import com.dullbluelab.textrunnertrial.Errors
import com.dullbluelab.textrunnertrial.logic.Syntax

class BlockIf() : CodeBlock(Type.IF, Syntax.Reserved.Word.IF, 0) {
    private var conditions: CodeBlock? = null
    private var statementTrue: CodeBlock? = null
    private var statementElse: CodeBlock? = null

    fun conditions() = conditions
    fun statementTrue() = statementTrue
    fun statementElse() = statementElse

    constructor(lists: BlockLists, index: Int) : this() {
        val codeNO = Syntax.Reserved.codeNO(Syntax.Reserved.Key.IF)
        val codeElse = Syntax.Reserved.codeNO(Syntax.Reserved.Key.ELSE)
        var flagElse = false

        var block = lists.block(index)
        lineNO = block?.lineNO() ?: 0

        if ((block == null) || (block.type() != CodeBlock.Type.WORD) || ((block as BlockWord).codeNO() != codeNO))
            throw Errors.Logic("CodeBlock.BlockIf line:$lineNO")

        lists.removeAt(index)

        while (index < lists.size()) {
            block = lists.block(index)!!
            if (conditions == null) {
                conditions = block
                lists.removeAt(index)
            }
            else if (statementTrue == null) {
                statementTrue = block
                lists.removeAt(index)
            }
            else if (!flagElse
                && block.type() == CodeBlock.Type.WORD && (block as BlockWord).codeNO() == codeElse) {
                flagElse = true
                lists.removeAt(index)
            }
            else if (flagElse && statementElse == null) {
                if (block.type() == CodeBlock.Type.WORD && (block as BlockWord).codeNO() == codeNO) {
                    statementElse = BlockIf(lists, index)
                }
                else {
                    statementElse = block
                    lists.removeAt(index)
                }
                break
            }
            else {
                break
            }
        }
        if (conditions == null || statementTrue == null) throw Errors.Syntax(Errors.Key.SYNTAX, lineNO = lineNO)
    }

    override fun dump(shift: String): String {
        var text = "$shift[ if ]\n"

        text += "$shift  condition = \n"
        conditions?.let { text += it.dump("$shift    ") }

        text += "$shift  if true = \n"
        statementTrue?.let { text += it.dump("$shift    ") }

        statementElse?.let {
            text += "$shift  else = \n"
            text += it.dump("$shift    ")
        }

        text += "$shift[ end if ]\n"
        return text
    }
}
