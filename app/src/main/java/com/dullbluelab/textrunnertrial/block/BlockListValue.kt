package com.dullbluelab.textrunnertrial.block

import com.dullbluelab.textrunnertrial.logic.Syntax
import com.dullbluelab.textrunnertrial.objects.RunObject

class BlockListValue() : CodeBlock(Type.LIST, Syntax.Chars.LIST_DATA_BRACKET, 0) {
    private var values: BlockBracket? = null
    private var objects: MutableList<RunObject>? = null

    fun values() = values
    fun objects() = objects
    fun setObjects(objects: MutableList<RunObject>) { this.objects = objects}

    constructor(values: BlockBracket) : this() {
        this.values = values
        this.lineNO = values.lineNO()
    }

    override fun findListAndRun(process: (BlockLists) -> Unit) {
        values?.findListAndRun(process)
    }

    override fun findBracketAndRun(bracket: String, process: (BlockLists) -> Unit) {
        values?.findBracketAndRun(bracket, process)
    }

    override fun text(): String = values?.text() ?: ""
    override fun dump(shift: String): String = values?.dump(shift) ?: ""
}
