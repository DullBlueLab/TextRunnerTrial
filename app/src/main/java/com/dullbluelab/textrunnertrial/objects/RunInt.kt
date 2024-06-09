package com.dullbluelab.textrunnertrial.objects

import com.dullbluelab.textrunnertrial.Errors
import com.dullbluelab.textrunnertrial.block.BlockNumber
import com.dullbluelab.textrunnertrial.logic.Syntax
import kotlin.math.abs

private val RW = Syntax.Reserved.Word
private val MW = Syntax.Method.Word

class RunInt() : RunValue(Type.INTS) {

    private var value: Int = 0

    constructor(value: Int) : this() {
        this.value = value
    }

    constructor(block: BlockNumber) : this() {
        this.value = block.strings().toInt()
    }

    override fun typeWord(): String = RW.INT

    override fun valueInt() = value
    override fun valueString() = value.toString()
    override fun valueDouble(): Double = value.toDouble()
    override fun valueBoolean() = (value != 0)

    override fun checkLevel(dst: RunValue): RunValue =
        if (dst.type == Type.DOUBLES) RunDouble(value.toDouble()) else this

    override fun execute(method: String, arguments: MutableList<RunObject>) : RunObject? {
        var result: RunObject? = super.execute(method, arguments)
        if (result != null) return result

        if (arguments.size == 0) {
            result = when (method) {
                MW.INC_FRONT -> incFront()
                MW.INC_REAR -> incRear()
                MW.DEC_FRONT -> decFront()
                MW.DEC_REAR -> decRear()
                MW.MINUS -> minus()
                MW.ABS -> mathAbs()
                else -> null
            }
        }
        else if (arguments.size == 1 && arguments[0].isRunValue()) {
            val dst = arguments[0] as RunValue
            result = when (method) {
                MW.ADD_SET -> addSet(dst)
                MW.SUB_SET -> subSet(dst)
                MW.MAXES -> maxes(dst)
                MW.MINES -> mines(dst)
                else -> null
            }
        }
        else result = null

        return result ?: throw Errors.Syntax("${Errors.message(Errors.Key.METHOD)} $method type Int")
    }

    override fun set(dst: RunValue) : RunValue {
        value = dst.valueInt()
        return RunInt(value)
    }

    private fun addSet(dst: RunValue) : RunValue {
        value += dst.valueInt()
        return RunInt(value)
    }

    private fun subSet(dst: RunValue) : RunValue {
        value -= dst.valueInt()
        return RunInt(value)
    }

    private fun incFront(): RunObject = RunInt(value++)
    private fun incRear(): RunObject = RunInt(++value)
    private fun decFront(): RunObject = RunInt(value--)
    private fun decRear(): RunObject = RunInt(--value)

    private fun minus(): RunValue = RunInt(-value)

    private fun maxes(dst: RunValue) = RunInt(value.coerceAtLeast(dst.valueInt()))
    private fun mines(dst: RunValue) = RunInt(value.coerceAtMost(dst.valueInt()))

    override fun add(dst: RunValue): RunValue = RunInt(value + dst.valueInt())
    override fun sub(dst: RunValue): RunValue = RunInt(value - dst.valueInt())
    override fun multi(dst: RunValue): RunValue = RunInt(value * dst.valueInt())
    override fun div(dst: RunValue): RunValue = RunInt(value / dst.valueInt())
    override fun mod(dst: RunValue): RunValue = RunInt(value % dst.valueInt())
    override fun moreSmall(dst: RunValue): RunBoolean =
        RunBoolean(value < dst.valueInt())
    override fun moreLarge(dst: RunValue): RunBoolean =
        RunBoolean(value > dst.valueInt())
    override fun small(dst: RunValue): RunBoolean =
        RunBoolean(value <= dst.valueInt())
    override fun large(dst: RunValue): RunBoolean =
        RunBoolean(value >= dst.valueInt())
    override fun equal(dst: RunValue): RunBoolean =
        RunBoolean(value == dst.valueInt())
    override fun notEqual(dst: RunValue): RunBoolean =
        RunBoolean(value != dst.valueInt())

    private fun mathAbs(): RunValue = RunInt(abs(value))
}
