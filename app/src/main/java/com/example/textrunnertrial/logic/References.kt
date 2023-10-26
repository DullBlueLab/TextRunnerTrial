package com.example.textrunnertrial.logic

import com.example.textrunnertrial.action.Objects
import com.example.textrunnertrial.action.Spaces

class References {

    enum class Type {
        NULL,
        VAR, FUN, CLASS, CONST
    }

    class Sets() {
        private var type: Type = Type.NULL
        private var word: String = ""
        private var returns: CodeBlock.Common? = null
        private var arguments: CodeBlock.ArgumentList? = null
        private var codeRef: CodeBlock.Common? = null
        private var childRef: Lists? = null

        constructor(type: Type, word: String, ref: CodeBlock.Common?) : this() {
            this.type = type
            this.word = word
            this.codeRef = ref
        }

        fun type() = type
        fun word() = word
        fun returns() = returns
        fun arguments() = arguments
        fun codes() = codeRef
        fun returnNames(): Spaces.Names = Spaces.Names(returns)

        fun references(): Lists? =
            if (type == Type.CLASS) (codeRef as CodeBlock.ClassDef).reference()
            else null

        fun setBlock(args: CodeBlock.ArgumentList?, returns: CodeBlock.Common?) {
            this.arguments = args
            this.returns = returns
        }

        fun setChildRef(child: Lists?) {
            this.childRef = child
        }

        fun dump() : String{
            var text = "  [ $type : $word ]\n"
            text +=    "    return = ${returns?.text()}\n"
            text +=    "    argument = ${arguments?.textOfType()}\n"
            return text
        }

        fun dumpChild() : String {
            return childRef?.dump() ?: ""
        }
    }

    class Lists() {
        private val lists: MutableList<Sets> = mutableListOf()
        private var hierarchyWords: Spaces.Names? = null
        private var parent: Lists? = null

        constructor(words: Spaces.Names, parent: Lists?) : this() {
            hierarchyWords = words
            this.parent = parent
        }

        fun appendVar(word: String, returns: CodeBlock.Common?, codes: CodeBlock.Common?) {
            val newSet = Sets(Type.VAR, word, codes)
            newSet.setBlock(args = null, returns)
            lists.add(newSet)
        }

        fun appendConst(word: String, codes: CodeBlock.Common?) {
            val newSet = Sets(Type.CONST, word, codes)
            lists.add(newSet)
        }

        fun appendFun(
            word: String,
            args: CodeBlock.ArgumentList?, returns: CodeBlock.Common?,
            codes: CodeBlock.Common?, childRef: Lists?
        ) {
            val newSet = Sets(Type.FUN, word, codes)
            newSet.setBlock(args, returns)
            childRef?.let { newSet.setChildRef(it) }
            lists.add(newSet)
        }

        fun appendClass(word: String, codes: CodeBlock.Common?, childRef: Lists?) {
            val newSet = Sets(Type.CLASS, word, codes)
            childRef?.let { newSet.setChildRef(it) }
            lists.add(newSet)
        }

        fun lists() = lists
        fun size(): Int = lists.size
        fun hierarchyWords(): Spaces.Names? = hierarchyWords
        fun spaceName(): Spaces.Names = hierarchyWords ?: Spaces.Names()

        private fun hierarchyText(): String {
            var text = ""
            hierarchyWords?.forEach {
                if (text.isNotEmpty()) text += "."
                text += it
            }
            return text
        }

        fun search(word: String): Sets? {
            var match: Sets? = null
            for (seek in lists) {
                if (seek.word() == word) {
                    match = seek
                    break
                }
            }
            return match
        }

        fun search(type: Type, word: String): Sets? {
            var match: Sets? = null
            for (seek in lists) {
                if (seek.type() == type && seek.word() == word) {
                    match = seek
                    break
                }
            }
            return match
        }

        fun searchIntoTrees(type: Type, names: Spaces.Names): Sets? {
            val word = names.topWord() ?: return null
            var match = search(type, word)
            if (match == null) {
                parent?.let { match = it.searchIntoTrees(type, names) }
            }
            else if (names.size() > 1 && match!!.type() == Type.CLASS) {
                match = match!!.references()?.searchIntoTrees(type, names.childWords())
            }
            return match
        }

        fun forEach(process: (Sets) -> Unit) {
            lists.forEach { sets ->
                process(sets)
            }
        }

        fun dump() : String {
            var text = "[${hierarchyText()}]\n"
            lists.forEach {
                text += it.dump()
            }
            text += "[end]\n"

            lists.forEach {
                if (it.type() == Type.FUN || it.type() == Type.CLASS) {
                    text += it.dumpChild()
                }
            }
            return text
        }

        fun clear() {
            lists.clear()
            hierarchyWords = null
            parent = null
        }
    }
}