package com.dullbluelab.textrunnertrial.block

import com.dullbluelab.textrunnertrial.Errors

class BlockFunCall() : CodeBlock(Type.FUN_CALL, "", 0) {
    private var argument: CodeBlock? = null

    override fun name(): String = strings

    constructor(lists: BlockLists, index: Int) : this() {
        val block = lists.block(index) ?: throw Errors.Logic("CodeBlock.FunCall")

        lineNO = block.lineNO()
        strings = block.name()
        argument = lists.block(index + 1) ?: throw Errors.Syntax(Errors.Key.SYNTAX, lineNO = lineNO)
    }

    constructor(method: String, args: CodeBlock?) : this() {
        strings = method
        argument = args
    }

    override fun child(index: Int): CodeBlock? {
        return when(index) {
            CodeBlock.ARGUMENT -> argument
            else -> null
        }
    }

    override fun findListAndRun(process: (BlockLists) -> Unit) {
        argument?.findListAndRun(process)
    }

    override fun findBracketAndRun(bracket: String, process: (BlockLists) -> Unit) {
        argument?.findBracketAndRun(bracket, process)
    }

    override fun text(): String {
        return "$strings${argument?.text()}"
    }

    override fun dump(shift: String): String {
        var text = "$shift[method:$strings]\n"
        argument?.let { text += "$shift  argument = \n" + it.dump("$shift    ") }
        return text
    }
}
