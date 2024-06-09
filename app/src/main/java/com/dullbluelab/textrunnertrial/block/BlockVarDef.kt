package com.dullbluelab.textrunnertrial.block

import com.dullbluelab.textrunnertrial.Errors
import com.dullbluelab.textrunnertrial.action.Spaces
import com.dullbluelab.textrunnertrial.logic.References
import com.dullbluelab.textrunnertrial.logic.Syntax

class BlockVarDef() : CodeBlock(Type.VAR_DEF, Syntax.Reserved.Word.VAR, 0) {
    private val nameList = mutableListOf<CodeBlock>()
    private var className: CodeBlock? = null
    private var classArgument: CodeBlock? = null

    fun classArgument() = classArgument

    constructor(lists: BlockLists, index: Int) : this() {
        val codeNO = Syntax.Reserved.codeNO(Syntax.Reserved.Key.VAR)
        var block: CodeBlock? = lists.block(index)
        if (block == null || block.type() != CodeBlock.Type.WORD || (block as BlockWord).codeNO() != codeNO)
            throw Errors.Logic("BlockVarDef")

        lists.removeAt(index)
        lineNO = block.lineNO()

        while (index < lists.size()) {
            block = lists.block(index)!!
            val type = block.type()

            if (type != Type.WORD && type != Type.SIGN) break
            if (type == Type.SIGN && block.strings() != Syntax.Chars.COMMA) break

            if (type == Type.WORD) nameList.add(block)
            lists.removeAt(index)
        }
        if (block?.type() == Type.SIGN
            && block.strings() == Syntax.Chars.WORD_DIV_SIGN
            && (index + 1) < lists.size()) {

            lists.removeAt(index)
            className = lists.block(index)
            lists.removeAt(index)

            if (className?.type() == Type.FUN_CALL) {
                classArgument = (className as BlockFunCall).child(ARGUMENT)
            }
            else {
                block = lists.block(index)
                if (block != null && block.type() == Type.BRACKET
                    && block.strings() == Syntax.Chars.ARGUMENT_BRACKET) {

                    classArgument = block
                    lists.removeAt(index)
                }
            }
        }
        if (nameList.size == 0 || className == null) throw Errors.Syntax(Errors.Key.SYNTAX, lineNO = lineNO)
    }

    fun setToReference(ref: References.Lists) {
        for (name in nameList) {
            ref.appendVar(name.strings(), className, this)
        }
    }

    fun nameList(): MutableList<String> {
        val list = mutableListOf<String>()

        nameList.forEach { name ->
            if (name.type() == CodeBlock.Type.WORD) {
                list.add((name as BlockWord).name())
            }
        }
        return list
    }

    fun className(): Spaces.Names? {
        val names = if (className?.type() == CodeBlock.Type.OBJECT_REF || className?.type() == CodeBlock.Type.WORD) {
            Spaces.Names(className)
        } else if (className?.type() == CodeBlock.Type.FUN_CALL) {
            Spaces.Names((className as BlockFunCall).name())
        } else null

        return names
    }

    override fun dump(shift: String): String {
        var text = "$shift[var def]\n"
        text += "$shift  name = "
        nameList.forEach { block ->
            text += "${ block.text() } "
        }
        text += "\n$shift  class = ${ className?.text() }\n"

        return text
    }
}
