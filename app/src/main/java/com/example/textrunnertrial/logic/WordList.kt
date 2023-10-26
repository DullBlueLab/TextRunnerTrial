package com.example.textrunnertrial.logic

class WordList {
    private val words: MutableList<String> = mutableListOf()

    init {
        words.add("")
    }

    fun entry(word: String) : Int {
        if (!words.contains(word)) words.add(word)
        return words.indexOf(word)
    }

    fun getWord(number: Int): String = (words[number] ?: "")

    fun clear() {
        words.clear()
        words.add("")
    }
}


