package com.dullbluelab.textrunnertrial.block

import com.dullbluelab.textrunnertrial.Errors
import com.dullbluelab.textrunnertrial.logic.Syntax

class BlockArgumentSet() {
    private var name: CodeBlock? = null
    private var className: CodeBlock? = null

    fun name() = name
    fun className() = className

    constructor(lists: BlockLists) : this() {
        val errors = "${Errors.message(Errors.Key.SYNTAX)} arguments"
        if (lists.size() == 3) {

            val block = lists.block(0) ?: throw Errors.Syntax(errors)
            if (block.type() == CodeBlock.Type.WORD) name = block

            val div = lists.block(1) ?: throw Errors.Syntax(errors)
            if (div.type() == CodeBlock.Type.SIGN && div.strings() == Syntax.Chars.WORD_DIV_SIGN) {
                className = lists.block(2)
            }
        }
        else {
            throw Errors.Syntax(errors)
        }
    }
}

class BlockArgumentList() {
    private val list = mutableListOf<BlockArgumentSet>()

    fun size() = list.size
    fun item(num: Int) = list[num]

    constructor(lists: BlockLists) : this() {
        val errors = "${Errors.message(Errors.Key.SYNTAX)} arguments"
        if (lists.isCommaDivide()) {
            lists.forEach { block ->
                if (block.type() == CodeBlock.Type.BRACKET) {
                    (block as BlockBracket).lists()?.let { list.add(BlockArgumentSet(it)) }
                }
                else throw Errors.Syntax(errors, block.lineNO())
            }
        }
        else if (lists.size() > 0) {
            list.add(BlockArgumentSet(lists))
        }
    }

    fun textOfType(): String {
        var text = "("
        list.forEach { set ->
            if (text.length > 1) text += ","
            text += set.className()?.text()
        }
        text += ")"
        return text
    }

    fun dump(shift: String): String {
        var text = ""
        list.forEach { set ->
            set.name()?.let { text += "$shift arg = ${ it.text() }" }
            set.className()?.let { text += " : class = ${ it.text() }\n" }
        }
        return text
    }
}
