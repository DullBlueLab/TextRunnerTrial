package com.dullbluelab.textrunnertrial.block

import com.dullbluelab.textrunnertrial.Errors
import com.dullbluelab.textrunnertrial.logic.References
import com.dullbluelab.textrunnertrial.logic.Syntax

class BlockConstDef() : CodeBlock(Type.CONST_DEF, Syntax.Reserved.Word.CONST, 0) {
    private var name: CodeBlock? = null
    private var value: CodeBlock? = null

    override fun name() = name?.text() ?: ""
    fun value() = value

    constructor(lists: BlockLists, index: Int) : this() {
        val codeNO = Syntax.Reserved.codeNO(Syntax.Reserved.Key.CONST)
        val operator = Syntax.Operator.word(Syntax.Operator.Type.SET)

        var block = lists.block(index)
        if (block == null || block.type() != Type.WORD || (block as BlockWord).codeNO() != codeNO)
            throw Errors.Logic("CodeBlock.ConstDef")

        lists.removeAt(index)
        lineNO = block.lineNO()

        block = lists.block(index) ?: return
        val method = block.child(ARGUMENT) ?: return
        if (block.type() == Type.OBJECT_REF && method.name() == operator) {
            name = block.child(SUBJECT)
            value = method.child(ARGUMENT)
            lists.removeAt(index)
        }
        if (name == null || value == null) throw Errors.Syntax(Errors.Key.SYNTAX, lineNO = lineNO)
    }

    fun setToReference(ref: References.Lists) {
        val word = name?.name() ?: ""
        ref.appendConst(word, this)
    }

    override fun dump(shift: String): String {
        return "$shift[const : ${name?.text()} : ${value?.text()} ]\n"
    }
}
