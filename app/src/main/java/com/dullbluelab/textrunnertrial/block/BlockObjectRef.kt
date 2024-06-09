package com.dullbluelab.textrunnertrial.block

import com.dullbluelab.textrunnertrial.Errors
import com.dullbluelab.textrunnertrial.logic.Syntax

class BlockObjectRef() : CodeBlock(Type.OBJECT_REF, "", 0) {
    private var subject: CodeBlock? = null
    private var argument: CodeBlock? = null

    constructor(lists: BlockLists, index: Int, def: Syntax.Operator.Setting) : this() {
        val block = lists.block(index) ?: throw Errors.Logic("CodeBlock.ObjectRef")
        lineNO = block.lineNO()

        subject =
            if (def.prev && index > 0)
                lists.block(index - 1)
            else if (def.next && (index + 1) < lists.size())
                lists.block(index + 1)
            else null

        argument =
            if (def.prev && def.next && (index + 1) < lists.size())
                lists.block(index + 1)
            else null

        if (def.method != "") {
            val method = BlockFunCall(def.method, argument)
            strings = Syntax.Chars.OBJECT_REF
            argument = method
        }
        else {
            strings = block.strings()
        }
        if ((def.prev && subject == null) || (def.next && argument == null)) {
            throw Errors.Syntax(Errors.Key.SYNTAX, lineNO = lineNO)
        }
    }

    override fun child(index: Int): CodeBlock? {
        return when(index) {
            CodeBlock.SUBJECT -> subject
            CodeBlock.ARGUMENT -> argument
            else -> null
        }
    }
    override fun findListAndRun(process: (BlockLists) -> Unit) {
        subject?.findListAndRun(process)
        argument?.findListAndRun(process)
    }

    override fun findBracketAndRun(bracket: String, process: (BlockLists) -> Unit) {
        subject?.findBracketAndRun(bracket, process)
        argument?.findBracketAndRun(bracket, process)
    }

    override fun text(): String {
        return "${subject?.text()}$strings${argument?.text()}"
    }

    override fun dump(shift: String): String {
        var text = "$shift[ref:$strings]\n"
        subject?.let { text += "$shift  subject = \n" + it.dump("$shift    ") }
        argument?.let { text += "$shift  argument = \n" + it.dump("$shift    ") }
        return text
    }
}
