package com.dullbluelab.textrunnertrial.ui


class Console() {
    var text: String = ""
    var textAll: String = ""
    var textError: String = ""

    fun append(text: String) {
        this.text += text + "\n"
        textAll += text + "\n"
    }

    fun appendError(text: String) {
        textAll += text + "\n"
        textError += text + "\n"
    }

    fun clearError() {
        textError = ""
    }

    fun clear() {
        text = ""
        textAll = ""
        textError = ""
    }
}