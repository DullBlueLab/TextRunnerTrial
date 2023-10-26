package com.example.textrunnertrial.logic

class CodeUnit {

    private var blocks: CodeBlock.Lists? = null
    private val reference: References.Lists = References.Lists()
    private val wordList: WordList = WordList()

    fun blocks(): CodeBlock.Lists = this.blocks ?: CodeBlock.Lists()
    fun setBlocks(blocks: CodeBlock.Lists) { this.blocks = blocks }
    fun reference(): References.Lists = this.reference
    fun wordList(): WordList = this.wordList

    fun buildReference() {
        blocks?.makeReference(reference)
    }

    fun clear() {
        blocks = null
        reference.clear()
        wordList.clear()
    }

    fun dump(): String {
        return blocks?.dump("") + reference.dump()
    }
}