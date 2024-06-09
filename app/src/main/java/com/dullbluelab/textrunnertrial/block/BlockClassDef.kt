package com.dullbluelab.textrunnertrial.block

import com.dullbluelab.textrunnertrial.Errors
import com.dullbluelab.textrunnertrial.action.Spaces
import com.dullbluelab.textrunnertrial.logic.References
import com.dullbluelab.textrunnertrial.logic.Syntax

class BlockClassDef() : CodeBlock(Type.CLASS_DEF, "", 0) {
    private var className: CodeBlock? = null
    private var superName: CodeBlock? = null
    private var statement: BlockBracket? = null
    private var references: References.Lists? = null
    private var classNameSpace: Spaces.Names? = null

    fun reference() = references

    constructor(lists: BlockLists, index: Int) : this() {
        val codeNO = Syntax.Reserved.codeNO(Syntax.Reserved.Key.CLASS)
        var flagSuper = false

        var block = lists.block(index)
        if (block == null || block.type() != CodeBlock.Type.WORD || (block as BlockWord).codeNO() != codeNO)
            throw Errors.Logic("CodeBlock.ClassDef")

        lists.removeAt(index)

        lineNO = block.lineNO()

        while (index < lists.size()) {
            block = lists.block(index)!!

            if (className == null && block.type() == Type.WORD) {
                className = block
                lists.removeAt(index)
            }
            else if (!flagSuper && block.type() == Type.SIGN
                && block.strings() == Syntax.Chars.WORD_DIV_SIGN) {
                flagSuper = true
                lists.removeAt(index)
            }
            else if (flagSuper && superName == null
                && (block.type() == Type.WORD || block.type() == Type.BRACKET)) {
                superName = block
                lists.removeAt(index)
            }
            else if (block.type() == Type.BRACKET
                && block.strings() == Syntax.Chars.STATEMENT_BRACKET) {
                statement = (block as BlockBracket)
                lists.removeAt(index)
                break
            }
            else {
                break
            }
        }
        if (className == null || statement == null) throw Errors.Syntax(Errors.Key.SYNTAX, lineNO = lineNO)
    }

    fun setToReference(ref: References.Lists) {
        ref.appendClass(name(), this, references)
    }

    fun makeReference(hierarchy: Spaces.Names, parent: References.Lists?) {
        classNameSpace = Spaces.Names(hierarchy, className)
        references = References.Lists(classNameSpace!!, parent)
        statement?.lists()?.makeReference(references!!)
    }

    override fun dump(shift: String): String {
        var text = "$shift[class ${className?.text()} : ${superName?.text()} ]\n"
        statement?.let { text += it.dump("$shift  ") }
        text += "$shift[class end] \n"
        return text
    }

    override fun name() = Spaces.Names(className).text()
    fun classNameSpaces(): Spaces.Names? = classNameSpace

    fun superClassDef(): BlockClassDef? {
        var def: BlockClassDef? = null
        superName?.let {
            val names = Spaces.Names(it)
            val result = references?.searchIntoTrees(References.Type.CLASS, names)?.codes()
            if (result != null && result.type() == Type.CLASS_DEF)
                def = result as BlockClassDef
        }
        return def
    }

    fun findMethod(word: String): BlockFunDef? {
        val sets = references?.search(References.Type.FUN, word)
        return if (sets != null && sets.type() == References.Type.FUN)
            (sets.codes() as BlockFunDef)
        else null
    }

    fun matchClass(dst: BlockClassDef) =
        (if (classNameSpace == null || dst.classNameSpace == null) false
        else classNameSpace!!.match(dst.classNameSpace!!))
}
