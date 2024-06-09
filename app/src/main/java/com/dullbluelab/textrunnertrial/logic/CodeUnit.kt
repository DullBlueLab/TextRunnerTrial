package com.dullbluelab.textrunnertrial.logic

import com.dullbluelab.textrunnertrial.block.BlockLists

class CodeUnit {

    private var blocks: BlockLists? = null
    private val reference: References.Lists = References.Lists()
    private val wordList: WordList = WordList()

    fun blocks(): BlockLists = this.blocks ?: BlockLists()
    fun setBlocks(blocks: BlockLists) { this.blocks = blocks }
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