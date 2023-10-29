package com.dullbluelab.textrunnertrial.logic

class WordList {
    private val words: MutableList<String> = mutableListOf()

    init {
        words.add("")
    }

    fun entry(word: String) : Int {
        if (!words.contains(word)) words.add(word)
        return words.indexOf(word)
    }

    fun clear() {
        words.clear()
        words.add("")
    }
}


