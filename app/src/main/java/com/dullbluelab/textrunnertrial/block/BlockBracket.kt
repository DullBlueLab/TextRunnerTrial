package com.dullbluelab.textrunnertrial.block


class BlockBracket(chars: String, lineNO: Int) : CodeBlock(Type.BRACKET, chars, lineNO) {
    private var lists: BlockLists? = null

    fun lists(): BlockLists? = lists
    fun chars() = strings

    constructor(chars: String, lists: BlockLists, lineNO: Int) : this(chars, lineNO) {
        this.lists = lists
    }

    constructor(chars: String, lists: BlockLists) : this(chars, lists, 0)

    override fun text(): String {
        var text = ""
        if (strings.length >= 2) {
            text = strings[0].toString()
            lists?.forEach {
                if (text.length > 1) text += ", "
                text += it.text()
            }
            text += strings[1].toString()
        }
        return text
    }

    override fun dump(shift: String): String =
        "$shift[open : $strings ] \n" +
                "${lists?.dump("$shift  ")}" +
                "$shift[close : $strings] \n"

    override fun findListAndRun(process: (BlockLists) -> Unit) {
        lists?.findListAndRun(process)
    }

    override fun findBracketAndRun(bracket: String, process: (BlockLists) -> Unit) {
        lists?.let {
            it.findBracketAndRun(bracket, process)
            if (strings == bracket) process(it)
        }
    }
}
