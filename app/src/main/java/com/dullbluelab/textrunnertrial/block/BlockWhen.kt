package com.dullbluelab.textrunnertrial.block

import com.dullbluelab.textrunnertrial.Errors
import com.dullbluelab.textrunnertrial.logic.Syntax

class BlockWhen() : CodeBlock(Type.WHEN, Syntax.Reserved.Word.WHEN, 0) {
    var subject: CodeBlock? = null
    val itemList: MutableList<ItemWhen> = mutableListOf()
    var elseStatement: CodeBlock? = null

    constructor(lists: BlockLists, index: Int) : this() {
        val codeNO = Syntax.Reserved.codeNO(Syntax.Reserved.Key.WHEN)

        var block = lists.block(index) ?: throw Errors.Logic("CodeBlock.BlockWhen")
        lineNO = block.lineNO()
        lists.removeAt(index)

        if ((block.type() != CodeBlock.Type.WORD) || ((block as BlockWord).codeNO() != codeNO))
            throw Errors.Logic("CodeBlock.BlockWhen line:$lineNO")

        block = lists.block(index) ?: throw Errors.Logic("CodeBlock.BlockWhen line:$lineNO")
        lists.removeAt(index)

        if (!(block.type() == CodeBlock.Type.BRACKET
                    && (block as BlockBracket).chars() == Syntax.Chars.STATEMENT_BRACKET)) {
            subject = block
            block = lists.block(index) ?: throw Errors.Logic("CodeBlock.BlockWhen")
            lists.removeAt(index)
        }
        if (block.type() == CodeBlock.Type.BRACKET
            && (block as BlockBracket).chars() == Syntax.Chars.STATEMENT_BRACKET) {

            val list = block.lists()?.lists()
                ?: throw Errors.Syntax(Errors.Key.SYNTAX, " when statement", lineNO)
            var args = mutableListOf<CodeBlock>()
            var cnt = 0

            while (cnt < list.size) {
                block = list[cnt]

                if (block.type() == CodeBlock.Type.SIGN && block.strings() == Syntax.Chars.WHEN_BRANCH) {
                    cnt ++
                    if (cnt >= list.size)
                        throw Errors.Syntax(Errors.Key.SYNTAX, " when statement", lineNO)
                    val statement = list[cnt]

                    val item = ItemWhen(args, statement)
                    itemList.add(item)
                    args = mutableListOf()
                }
                else if (block.type() == CodeBlock.Type.WORD && block.strings() == Syntax.Reserved.Word.ELSE) {
                    if ((cnt + 2) >= list.size)
                        throw Errors.Syntax(Errors.Key.SYNTAX, " when else statement", lineNO)

                    cnt ++
                    block = list[cnt]
                    if (!(block.type() == CodeBlock.Type.SIGN && block.strings() == Syntax.Chars.WHEN_BRANCH))
                        throw Errors.Syntax(Errors.Key.SYNTAX, " when else ->", lineNO)

                    cnt ++
                    elseStatement = list[cnt]
                }
                else {
                    if (!(block.type() == CodeBlock.Type.SIGN && block.strings() == Syntax.Chars.COMMA))
                        args.add(block)
                }
                cnt ++
            }
        }
        else {
            throw Errors.Syntax(Errors.Key.SYNTAX, " when statement", lineNO)
        }
    }

    override fun dump(shift: String): String {
        var text = "$shift[when]\n"
        text += "$shift  subject = \n"
        text += subject?.dump("$shift    ")

        itemList.forEach { item ->
            text += item.dump("$shift  ")
        }
        text += "${shift}- close when -\n"
        return text
    }
}

class ItemWhen(
    val arguments: MutableList<CodeBlock>,
    val statement: CodeBlock
) {
    fun dump(shift: String): String {
        var text = "${shift}arguments =\n"
        arguments.forEach { arg ->
            text += arg.dump("$shift  ")
        }
        text += "${shift}statement = \n"
        text += statement.dump("$shift  ")
        return text
    }
}
