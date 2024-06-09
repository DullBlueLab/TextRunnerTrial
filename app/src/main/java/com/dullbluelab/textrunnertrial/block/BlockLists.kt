package com.dullbluelab.textrunnertrial.block

import com.dullbluelab.textrunnertrial.logic.References
import com.dullbluelab.textrunnertrial.logic.Syntax

class BlockLists() {
    private var divide: String = ""
    private var list = mutableListOf<CodeBlock>()

    constructor(divide: String) : this() {
        this.divide = divide
    }

    fun lists() = list
    fun size() = list.size

    fun append(block: CodeBlock) { list.add(block) }
    fun removeAt(index: Int) { list.removeAt(index) }
    fun insert(index: Int, block: CodeBlock) { list.add(index, block) }

    fun block(index: Int): CodeBlock? =
        if (0 <= index && index < list.size) list[index] else null

    fun isCommaDivide(): Boolean = (divide == Syntax.Chars.COMMA)

    fun dump(shift: String): String {
        var text = "$shift[LIST IN:$divide] \n"
        list.forEach { set ->
            text += set.dump("$shift  ")
        }
        return text
    }

    fun forEach(process: (CodeBlock) -> Unit) {
        list.forEach { block -> process(block) }
    }

    fun findListAndRun(process: (BlockLists) -> Unit) {
        list.forEach { block ->
            if (block.type() == CodeBlock.Type.BRACKET || block.type() == CodeBlock.Type.LIST
                || block.type() == CodeBlock.Type.OBJECT_REF || block.type() == CodeBlock.Type.FUN_CALL) {
                block.findListAndRun(process)
            }
        }
        process(this)
    }

    fun findBracketAndRun(bracket: String, process: (BlockLists) -> Unit) {
        list.forEach { block ->
            if (block.type() == CodeBlock.Type.BRACKET || block.type() == CodeBlock.Type.LIST
                || block.type() == CodeBlock.Type.OBJECT_REF || block.type() == CodeBlock.Type.FUN_CALL) {
                block.findBracketAndRun(bracket, process)
            }
        }
    }

    fun makeReference(refs: References.Lists) {
        list.forEach { block ->
            when (block.type()) {
                CodeBlock.Type.CLASS_DEF -> {
                    val classes = block as BlockClassDef
                    classes.makeReference(refs.spaceName(), refs)
                    classes.setToReference(refs)
                }
                CodeBlock.Type.FUN_DEF -> {
                    val funDef = block as BlockFunDef
                    funDef.makeReference(refs.spaceName(), refs)
                    funDef.setToReference(refs)
                }
                CodeBlock.Type.INIT_DEF -> {
                    (block as BlockInitDef).setToReference(refs)
                }
                CodeBlock.Type.VAR_DEF -> {
                    (block as BlockVarDef).setToReference(refs)
                }
                CodeBlock.Type.CONST_DEF -> {
                    (block as BlockConstDef).setToReference(refs)
                }
                else -> {}
            }
        }
    }

    fun buildCommaLists() {
        var pos: Int
        val commaPos = getCommaPos()
        if (commaPos.size < 1) return

        val newList = mutableListOf<CodeBlock>()
        var index = 0
        var comma = 0

        while (index < list.size) {
            pos = if (comma < commaPos.size) commaPos[comma] else list.size
            comma++

            if ((pos - index) > 1) {
                val child = BlockLists("")
                while (index < pos) {
                    child.append(list[index])
                    index++
                }
                val branch = BlockBracket("", child)
                newList.add(branch)
            } else {
                newList.add(list[index])
            }
            index = pos + 1
        }
        list = newList
        divide = ","
    }

    private fun getCommaPos(): MutableList<Int> {
        val result = mutableListOf<Int>()
        var index = 0
        while (index < list.size) {
            val block = list[index]
            if (block.type() == CodeBlock.Type.SIGN && block.strings() == Syntax.Chars.COMMA) {
                result.add(index)
            }
            index++
        }
        return result
    }
}
