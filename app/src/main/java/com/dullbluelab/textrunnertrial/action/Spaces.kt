package com.dullbluelab.textrunnertrial.action

import com.dullbluelab.textrunnertrial.Errors
import com.dullbluelab.textrunnertrial.block.BlockArgumentList
import com.dullbluelab.textrunnertrial.block.BlockClassDef
import com.dullbluelab.textrunnertrial.block.BlockConstDef
import com.dullbluelab.textrunnertrial.block.BlockFunDef
import com.dullbluelab.textrunnertrial.block.BlockListValue
import com.dullbluelab.textrunnertrial.block.BlockNumber
import com.dullbluelab.textrunnertrial.block.BlockStrings
import com.dullbluelab.textrunnertrial.block.CodeBlock
import com.dullbluelab.textrunnertrial.data.LibraryRepository
import com.dullbluelab.textrunnertrial.logic.References
import com.dullbluelab.textrunnertrial.logic.Syntax
import com.dullbluelab.textrunnertrial.objects.*

class Spaces(repositories: LibraryRepository) {

    private val groundVarList: VarLists = VarLists()
    private var thisObject: RunInstance? = null
    private val thisObjectStack: MutableList<RunInstance?> = mutableListOf()
    private var thisVarList: VarLists? = null
    private val thisVarStack: MutableList<VarLists> = mutableListOf()

    private var referenceList: References.Lists? = null
    private val referenceStack: MutableList<References.Lists?> = mutableListOf()

    var groundFlag: Boolean = false

    class Returns {
        private var objects: RunObject? = null

        fun objects() = objects
        fun set(objects: RunObject?) { this.objects = objects }
        fun isReturn() = (objects != null)
        fun clear() { objects = null }
    }
    val returns = Returns()

    fun thisObject() = thisObject

    fun setup(refList: References.Lists) {
        clear()
        referenceList = refList
        thisVarList = groundVarList
    }

    private fun clear() {
        groundVarList.clear()
        thisObject = null
        thisObjectStack.clear()
        thisVarList = null
        thisVarStack.clear()

        referenceList = null
        referenceStack.clear()
    }

    fun getValue(word: String): RunObject? {
        var sets: RunObject? = null

        thisVarList?.let { sets = it.search(word)?.value() }

        if (sets == null && !groundFlag) {
            sets = thisObject?.searchObjects(word)
        }
        if (sets == null && thisVarList != groundVarList) {
            sets = groundVarList.search(word)?.value()
        }
        return sets
    }

    fun updateThisObject(instances: RunInstance) {
        thisObjectStack.add(thisObject)
        thisObject = instances

        referenceStack.add(referenceList)
        referenceList = instances.references()
    }

    fun returnThisObject() {
        var index = thisObjectStack.size - 1
        if (index >= 0) {
            thisObject = thisObjectStack[index]
            thisObjectStack.removeAt(index)
        }
        else {
            thisObject = null
        }
        index = referenceStack.size - 1
        if (index >= 0) {
            referenceList = referenceStack[index]
            referenceStack.removeAt(index)
        }
        else {
            referenceList = null
        }
    }

    fun updateFunVar(funDef: BlockFunDef, arguments: MutableList<RunObject>): String {
        var result = ""
        thisVarList?.let { thisVarStack.add(it) }
        thisVarList = VarLists()

        val argList: BlockArgumentList? = funDef.arguments()
        if (argList != null && argList.size() > 0) {
            result = thisVarList?.setArguments(argList, arguments)
                ?: throw Errors.Syntax(Errors.Key.INTERNAL, "updateFunVar")
        }
        return result
    }

    fun returnFunVar() {
        val index = thisVarStack.size - 1
        if (index >= 0) {
            thisVarList = thisVarStack[index]
            thisVarStack.removeAt(index)
        }
        else {
            thisVarList = null
        }
    }

    fun createVarObject(name: String, className: Names, isMember: Boolean): RunObject {
        return if (referenceList != null) {
            if (isMember)
                thisObject?.members()?.appendVarObjects(name, className, referenceList!!)
                    ?: throw Errors.Syntax(Errors.Key.UNKNOWN, "create var")
            else
                thisVarList?.appendVarObjects(name, className, referenceList!!)
                    ?: throw Errors.Syntax(Errors.Key.UNKNOWN, " create var")
        }
        else throw Errors.Logic("Spaces.createVarObject")
    }

    fun createConst(constDef: BlockConstDef, isMember: Boolean): RunObject {
        return if (isMember) {
            if (referenceList != null)
                thisObject?.members()?.appendConstValue(constDef)
                    ?: throw Errors.Logic("Spaces.createConst")
            else throw Errors.Logic("Spaces.createConst")
        }
        else
            thisVarList?.appendConstValue(constDef)
                ?: throw Errors.Logic("Spaces.createConst")
    }

    fun findClassDef(name: String): CodeBlock? {
        val set = referenceList?.search(References.Type.CLASS, name)
        return set?.codes()
    }

    data class VarSets(
        private val name: String,
        private val type: RunObject.Type?,
        private val classes: Names?,
        private var value: RunObject? = null
    ) {
        fun name() = name
        fun value() = value
    }

    class VarLists() {
        private val lists: MutableList<VarSets> = mutableListOf()

        fun appendVarObjects(name: String, className: Names, refList:References.Lists)
            : RunObject? {

            var objects: RunObject? = null
            val type = getVarType(className)

            if (type == RunObject.Type.INSTANCE) {
                val classDef = refList.searchIntoTrees(References.Type.CLASS, className)?.codes()
                if (classDef != null && classDef.type() == CodeBlock.Type.CLASS_DEF)
                    objects = RunInstance((classDef as BlockClassDef))
            }
            else if (type == RunObject.Type.LISTS) {
                objects = RunList()
            }
            else if (type == RunObject.Type.IMAGES) {
                objects = RunImage()
            }
            else if (RunObject.isValue(type)) {
                objects = createValueObject(type)
            }
            lists.add(VarSets(name, type, className, objects))
            return objects
        }

        private fun getVarType(returns: Names): RunObject.Type {
            return if (returns.size() == 1)
                RunObject.typeValue(returns.text()) ?: RunObject.Type.INSTANCE
            else RunObject.Type.INSTANCE // here
        }

        private fun createValueObject(type: RunObject.Type): RunValue? {
            val result: RunValue? =
                when (type) {
                    RunObject.Type.INTS     -> RunInt()
                    RunObject.Type.DOUBLES  -> RunDouble()
                    RunObject.Type.STRINGS  -> RunString()
                    RunObject.Type.BOOLEANS -> RunBoolean()
                    else -> null
                }
            return result
        }

        fun appendConstValue(codes: BlockConstDef): RunObject {
            var result: RunObject? = null
            val block = codes.value() ?: throw Errors.Logic("appendConstValue")
            val name = codes.name()

            when (block.type()) {
                CodeBlock.Type.STRING -> {
                    result = RunString(block as BlockStrings)
                }
                CodeBlock.Type.NUMBER -> {
                    val strings = (block as BlockNumber).strings()
                    result = if (strings.contains(Syntax.Chars.DOT))
                        RunDouble(strings.toDouble())
                    else
                        RunInt(strings.toInt())
                }
                CodeBlock.Type.LIST -> {
                    val objects = (block as BlockListValue).objects()
                    objects?.let { result = RunList(it) }
                }
                else -> throw Errors.Syntax(Errors.Key.UNKNOWN_CONST)
            }
            result?.let { lists.add(VarSets(name, it.type, classes = null, it)) }
            return result ?: throw Errors.Syntax(Errors.Key.UNKNOWN_CONST)
        }

        fun setArguments(
            argCodes: BlockArgumentList, argValues: MutableList<RunObject> ): String{

            if (argCodes.size() != argValues.size) throw Errors.Syntax(Errors.Key.ILLEGAL_ARGUMENT)

            var index = 0
            while (index < argCodes.size()) {

                val codeDef = argCodes.item(index)
                val codeWord = codeDef.name()?.text() ?: throw Errors.Syntax(Errors.Key.ILLEGAL_ARGUMENT)
                val codeClassName = Names(codeDef.className())

                val value = argValues[index]
                val valueType = value.type
                val valueClassName =
                    if (valueType == RunObject.Type.INSTANCE)
                            (value as RunInstance).classNames()
                    else null

                if (!value.matchType(codeClassName)) throw Errors.Syntax(Errors.Key.NOT_MATCH_ARGUMENT)

                lists.add(VarSets(codeWord, valueType, valueClassName, value))
                index ++
            }
            return ""
        }

        fun search(word: String): VarSets? {
            var result: VarSets? = null
            for (sets in lists) {
                if (sets.name() == word) {
                    result = sets
                    break
                }
            }
            return result
        }

        fun clear() {
            lists.clear()
        }
    }

    class Names() {
        private var words = mutableListOf<String>()

        constructor(space: Names, name: String) : this() {
            words.addAll(space.words)
            words.add(name)
        }

        constructor(blocks: CodeBlock?) : this() {
            make(blocks)
        }

        constructor(space: Names, block: CodeBlock?) : this() {
            words.addAll(space.words)
            make(block)
        }

        constructor(words: MutableList<String>) : this() {
            this.words = words
        }

        constructor(name: String) : this() {
            words.add(name)
        }

        private fun make(blocks: CodeBlock?) {
            if (blocks?.type() == CodeBlock.Type.WORD) {
                words.add(blocks.name())
            }
            else if (blocks?.type() == CodeBlock.Type.OBJECT_REF
                && blocks.strings() == Syntax.Chars.OBJECT_REF) {
                make(blocks.child(CodeBlock.SUBJECT))
                make(blocks.child(CodeBlock.ARGUMENT))
            }
        }

        fun match(dst: Names) : Boolean {
            val dstWord = dst.words
            var result = dstWord.size == words.size
            var index = 0
            while (index < words.size && result) {
                if (dstWord[index] != words[index]) result = false else index ++
            }
            return result
        }

        fun forEach(process: (String) -> Unit) {
            words.forEach { process(it) }
        }

        fun topWord(): String? = if (words.size > 0) words[0] else null

        fun childWords(): Names {
            val result = mutableListOf<String>()
            for (index in 1 until words.size) {
                result.add(words[index])
            }
            return Names(result)
        }

        fun size() = words.size

        fun text(): String {
            var text = ""
            words.forEach {
                if (text.isNotEmpty()) text += Syntax.Chars.OBJECT_REF
                text += it
            }
            return text
        }
    }
}