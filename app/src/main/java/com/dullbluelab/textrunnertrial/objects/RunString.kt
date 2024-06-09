package com.dullbluelab.textrunnertrial.objects

import androidx.core.text.isDigitsOnly
import com.dullbluelab.textrunnertrial.Errors
import com.dullbluelab.textrunnertrial.block.BlockStrings
import com.dullbluelab.textrunnertrial.logic.Syntax

private val RW = Syntax.Reserved.Word
private val MW = Syntax.Method.Word

class RunString() : RunValue(Type.STRINGS) {
    private var value: String = ""

    constructor(value: String) : this() {
        this.value = value
    }

    constructor(block: BlockStrings) : this() {
        this.value = block.strings()
    }

    override fun typeWord(): String = RW.STRING

    override fun valueInt() =
        if (value.isNotEmpty() && value.isDigitsOnly()) value.toInt() else 0
    override fun valueString() = value
    override fun valueDouble(): Double =
        if (value.isNotEmpty() && value.isDigitsOnly()) value.toDouble() else 0.0
    override fun valueBoolean() = (value == "true")

    override fun execute(method: String, arguments: MutableList<RunObject>) : RunObject {
        var result: RunObject? = super.execute(method, arguments)

        if (result?.type == null) {
            if (arguments.size == 0) {
                result = when (method) {
                    MW.LENGTH -> length()
                    MW.LOWERCASE -> lowercase()
                    MW.UPPERCASE -> uppercase()
                    else -> null
                }
            }
            else if (arguments.size == 1 && arguments[0].isRunValue()) {
                val dst = arguments[0] as RunValue
                result = when (method) {
                    MW.ADD_SET -> addSet(dst)
                    MW.CHAR_AT -> charAt(dst)
                    MW.CONTAINS -> contains(dst)
                    else -> null
                }
            }
            else if (arguments.size == 2 && arguments[0].isRunValue() && arguments[1].isRunValue()) {
                val dst = arguments[0] as RunValue
                val param = arguments[1] as RunValue
                result = when (method) {
                    MW.INDEX_OF -> indexOf(dst, param)
                    MW.SUBSTRING -> substring(dst, param)
                    else -> null
                }
            }
            else result = null
        }
        return result ?: throw Errors.Syntax("${Errors.message(Errors.Key.METHOD)} $method data type String")
    }

    override fun set(dst: RunValue) : RunValue {
        value = dst.valueString()
        return RunString(value)
    }

    private fun addSet(dst: RunValue) : RunValue {
        value += dst.valueString()
        return RunString(value)
    }

    private fun charAt(dst: RunValue): RunObject {
        val index = dst.valueInt()
        if (index < 0 || value.length <= index)
            throw Errors.Syntax("${Errors.message(Errors.Key.OUT_OF_SIZE)} charAt")

        return RunString(value[index].toString())
    }

    private fun contains(dst: RunValue): RunObject {
        return RunBoolean(value.indexOf(dst.valueString()) >= 0)
    }

    private fun indexOf(dst: RunValue, from: RunValue): RunObject {
        return RunInt(value.indexOf(dst.valueString(), from.valueInt()))
    }

    private fun length(): RunObject = RunInt(value.length)

    private fun substring(begin: RunValue, end: RunValue): RunObject {
        val bv = begin.valueInt()
        val ev = end.valueInt()
        val len = value.length

        if ((bv < 0 || len <= bv) || (ev < 0 || len <= ev) || (bv > ev))
            throw Errors.Syntax("${Errors.message(Errors.Key.OUT_OF_SIZE)} substring")

        return RunString(value.substring(bv, ev))
    }

    private fun lowercase(): RunObject = RunString(value.lowercase())
    private fun uppercase(): RunObject = RunString(value.uppercase())

    override fun add(dst: RunValue) : RunValue = RunString(this.value + dst.valueString())
    override fun sub(dst: RunValue): RunValue = RunString((value.toDouble() + dst.valueDouble()).toString())
    override fun multi(dst: RunValue): RunValue = RunString((value.toDouble() * dst.valueDouble()).toString())
    override fun div(dst: RunValue): RunValue = RunString((value.toDouble() / dst.valueDouble()).toString())
    override fun mod(dst: RunValue): RunValue = RunString((value.toDouble() % dst.valueDouble()).toString())

    override fun moreSmall(dst: RunValue): RunBoolean =
        RunBoolean(value < dst.valueString())
    override fun moreLarge(dst: RunValue): RunBoolean =
        RunBoolean(value > dst.valueString())
    override fun small(dst: RunValue): RunBoolean =
        RunBoolean(value <= dst.valueString())
    override fun large(dst: RunValue): RunBoolean =
        RunBoolean(value >= dst.valueString())
    override fun equal(dst: RunValue): RunBoolean =
        RunBoolean(value == dst.valueString())
    override fun notEqual(dst: RunValue): RunBoolean =
        RunBoolean(value != dst.valueString())
}
