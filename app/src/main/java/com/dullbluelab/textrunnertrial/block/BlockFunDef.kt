package com.dullbluelab.textrunnertrial.block

import com.dullbluelab.textrunnertrial.Errors
import com.dullbluelab.textrunnertrial.action.Spaces
import com.dullbluelab.textrunnertrial.logic.References
import com.dullbluelab.textrunnertrial.logic.Syntax

open class BlockFunDef(name: String) : CodeBlock(Type.FUN_DEF, name, 0) {
    protected var arguments: BlockArgumentList? = null
    protected var returnType: CodeBlock? = null
    protected var statement: BlockBracket? = null
    private var references: References.Lists? = null

    override fun name(): String = strings
    final override fun setName(name: String) { strings = name }
    fun arguments() = arguments
    fun statement() = statement

    constructor(lists: BlockLists, start: Int) : this("") {
        val codeNO = Syntax.Reserved.codeNO(Syntax.Reserved.Key.FUN)
        var index = start

        var block = lists.block(index)
        if (block == null || block.type() != CodeBlock.Type.WORD || (block as BlockWord).codeNO() != codeNO)
            throw Errors.Logic("CodeBlock.FunDef")

        lists.removeAt(index)
        lineNO = block.lineNO()

        while (index < lists.size() && statement == null) {
            block = lists.block(index)!!
            // argument
            if (block.type() == Type.FUN_CALL) {
                setName(block.name())
                val child = block.child(ARGUMENT)
                if (child != null && child.type() == Type.BRACKET) {
                    val args = (child as BlockBracket).lists()
                    args?.let { arguments = BlockArgumentList(it) }
                }
                lists.removeAt(index)
            }
            // return type
            else if (block.type() == Type.SIGN
                && block.strings() == Syntax.Chars.WORD_DIV_SIGN) {
                lists.removeAt(index)
                if (index < lists.size()) {
                    returnType = lists.block(index)
                    lists.removeAt(index)
                }
            }
            // statement
            else if (block.type() == Type.BRACKET
                && block.strings() == Syntax.Chars.STATEMENT_BRACKET) {
                statement = (block as BlockBracket)
                lists.removeAt(index)
            }
            else {
                index++
            }
        }
        if (arguments == null || statement == null) throw Errors.Syntax(Errors.Key.SYNTAX, lineNO = lineNO)
    }

    open fun setToReference(ref: References.Lists) {
        ref.appendFun(name(), arguments, returnType, this, references)
    }

    fun makeReference(hierarchy: Spaces.Names, parent: References.Lists?) {
        val spaceName = Spaces.Names(hierarchy, name())
        references = References.Lists(spaceName, parent)
        statement?.lists()?.makeReference(references!!)
    }

    override fun dump(shift: String): String {
        var text = "$shift[fun def: $strings]\n"
        text += arguments?.dump("$shift  ")
        text += "$shift  return = ${ returnType?.text() }\n"
        text += "$shift  statement = \n" + statement?.dump("$shift  ")
        return text
    }
}
