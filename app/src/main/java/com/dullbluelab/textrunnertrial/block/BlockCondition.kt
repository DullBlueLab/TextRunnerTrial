package com.dullbluelab.textrunnertrial.block

import com.dullbluelab.textrunnertrial.Errors
import com.dullbluelab.textrunnertrial.logic.Syntax

class BlockCondition(type: Type) : CodeBlock(type, "", 0) {
    private var conditions: CodeBlock? = null
    private var initial: CodeBlock? = null
    private var countUp: CodeBlock? = null
    private var statement: CodeBlock? = null

    fun conditions() = conditions
    fun statement() = statement
    fun initial() = initial
    fun countUp() = countUp

    constructor(lists: BlockLists, index: Int, type: Type) : this(type) {
        val loopType = getLoopType(type)
        val codeNO = Syntax.Reserved.codeNO(loopType)

        var block = lists.block(index)
        if ((block == null) || (block.type() != CodeBlock.Type.WORD) || ((block as BlockWord).codeNO() != codeNO))
            throw Errors.Logic("CodeBlock.Condition")

        lists.removeAt(index)

        lineNO = block.lineNO()

        while (index < lists.size()) {
            block = lists.block(index)
            if (block == null) throw Errors.Logic("class Condition line:$lineNO")

            if (conditions == null) {
                if (type == CodeBlock.Type.FOR) setConditionFor(block)
                else conditions = block
                lists.removeAt(index)
                if (conditions == null) break
            }
            else if (statement == null) {
                statement = block
                lists.removeAt(index)
                break
            }
            else {
                break
            }
        }
        if (conditions == null || statement == null)
            throw Errors.Syntax(Errors.Key.SYNTAX, lineNO = lineNO)
    }

    private fun getLoopType(type: CodeBlock.Type): Syntax.Reserved.Key {
        return when (type) {
            CodeBlock.Type.FOR   -> Syntax.Reserved.Key.FOR
            CodeBlock.Type.WHILE -> Syntax.Reserved.Key.WHILE
            else       -> throw Errors.Logic("class Condition line:$lineNO")
        }
    }

    private fun setConditionFor(block: CodeBlock) {
        val lists: BlockLists =
            (if (block.type() == CodeBlock.Type.BRACKET) (block as BlockBracket).lists() else null)
                ?: throw Errors.Syntax(Errors.Key.ILLEGAL_CONDITION, lineNO = lineNO)

        val branch = Syntax.Chars.FOR_BRANCH
        var cnt = 0

        while (cnt < lists.size() && cnt < 5) {
            val child = lists.block(cnt) ?: throw Errors.Logic("class Condition line:$lineNO")

            when (cnt) {
                0 -> initial = child
                2 -> conditions = child
                4 -> countUp = child
                1, 3 -> if (!(child.type() == CodeBlock.Type.SIGN && child.strings() == branch))
                    throw Errors.Syntax(Errors.Key.ILLEGAL_CONDITION, lineNO = lineNO)
            }
            cnt ++
        }
    }

    override fun dump(shift: String): String {
        val loopType = getLoopType(type())
        val name = Syntax.Reserved.word(loopType)
        var text = "$shift[ $name ]\n"

        text += "$shift  condition =\n"
        conditions?.let { text += it.dump("$shift  ") }

        text += "$shift  statement =\n"
        statement?.let { text += it.dump("$shift  ") }

        text += "$shift[ $name end ]\n"
        return text
    }
}
