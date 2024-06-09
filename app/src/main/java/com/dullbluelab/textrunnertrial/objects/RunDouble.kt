package com.dullbluelab.textrunnertrial.objects

import com.dullbluelab.textrunnertrial.Errors
import com.dullbluelab.textrunnertrial.block.BlockNumber
import com.dullbluelab.textrunnertrial.logic.Syntax
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.log
import kotlin.math.pow
import kotlin.math.round
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

private val RW = Syntax.Reserved.Word
private val MW = Syntax.Method.Word

class RunDouble() : RunValue(Type.DOUBLES) {
    private var value: Double = 0.0

    constructor(value: Double) : this() {
        this.value = value
    }

    constructor(block: BlockNumber) : this() {
        this.value = block.strings().toDouble()
    }

    override fun typeWord(): String = RW.DOUBLE

    override fun valueInt() = value.toInt()
    override fun valueString() = value.toString()
    override fun valueDouble(): Double = value
    override fun valueBoolean() = (value != 0.0)

    override fun execute(method: String, arguments: MutableList<RunObject>) : RunObject {
        var result: RunObject? = super.execute(method, arguments)
        if (result != null) return result

        if (arguments.size == 0) {
            result = when (method) {
                MW.INC_FRONT -> incFront()
                MW.INC_REAR  -> incRear()
                MW.DEC_FRONT -> decFront()
                MW.DEC_REAR  -> decRear()
                MW.MINUS     -> minus()

                MW.ABS -> mathAbs()
                MW.ACOS -> mathACos()
                MW.ASIN -> mathASin()
                MW.ATAN -> mathATan()
                MW.COS -> mathCos()
                MW.SIN -> mathSin()
                MW.TAN -> mathTan()
                MW.CEIL -> mathCeil()
                MW.FLOOR -> mathFloor()
                MW.ROUND -> mathRound()
                MW.SQRT -> mathSqrt()
                MW.TO_DEGREES -> mathToDegrees()
                MW.TO_RADIANS -> mathToRadians()
                MW.EXP -> mathExp()
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

                MW.POW -> mathPow(dst)
                MW.LOG -> mathLog(dst)
                else -> null
            }
        }
        else result = null

        return result ?: throw Errors.Syntax("${Errors.message(Errors.Key.METHOD)} $method type Double")
    }

    override fun set(dst: RunValue) : RunValue {
        value = dst.valueDouble()
        return RunDouble(value)
    }

    private fun addSet(dst: RunValue) : RunValue {
        value += dst.valueInt()
        return RunDouble(value)
    }

    private fun subSet(dst: RunValue) : RunValue {
        value -= dst.valueInt()
        return RunDouble(value)
    }

    private fun incFront(): RunObject = RunDouble(value++)
    private fun incRear(): RunObject = RunDouble(++value)
    private fun decFront(): RunObject = RunDouble(value--)
    private fun decRear(): RunObject = RunDouble(--value)

    private fun minus(): RunValue = RunDouble(-value)

    private fun maxes(dst: RunValue) = RunDouble(value.coerceAtLeast(dst.valueDouble()))
    private fun mines(dst: RunValue) = RunDouble(value.coerceAtMost(dst.valueDouble()))

    override fun add(dst: RunValue): RunValue = RunDouble(value + dst.valueDouble())
    override fun sub(dst: RunValue): RunValue = RunDouble(value - dst.valueDouble())
    override fun multi(dst: RunValue): RunValue = RunDouble(value * dst.valueDouble())
    override fun div(dst: RunValue): RunValue = RunDouble(value / dst.valueDouble())
    override fun mod(dst: RunValue): RunValue = RunDouble(value % dst.valueDouble())

    override fun moreSmall(dst: RunValue): RunBoolean =
        RunBoolean(value < dst.valueDouble())
    override fun moreLarge(dst: RunValue): RunBoolean =
        RunBoolean(value > dst.valueDouble())
    override fun small(dst: RunValue): RunBoolean =
        RunBoolean(value <= dst.valueDouble())
    override fun large(dst: RunValue): RunBoolean =
        RunBoolean(value >= dst.valueDouble())
    override fun equal(dst: RunValue): RunBoolean =
        RunBoolean(value == dst.valueDouble())
    override fun notEqual(dst: RunValue): RunBoolean =
        RunBoolean(value != dst.valueDouble())

    private fun mathAbs(): RunDouble = RunDouble(abs(value))
    private fun mathACos(): RunDouble = RunDouble(acos(value))
    private fun mathASin(): RunDouble = RunDouble(asin(value))
    private fun mathATan(): RunDouble = RunDouble(atan(value))
    private fun mathCos(): RunDouble = RunDouble(cos(value))
    private fun mathSin(): RunDouble = RunDouble(sin(value))
    private fun mathTan(): RunDouble = RunDouble(tan(value))
    private fun mathCeil(): RunDouble = RunDouble(ceil(value))
    private fun mathFloor(): RunDouble = RunDouble(floor(value))
    private fun mathRound(): RunDouble = RunDouble(round(value))
    private fun mathSqrt(): RunDouble = RunDouble(sqrt(value))
    private fun mathToDegrees(): RunDouble = RunDouble(Math.toDegrees(value))
    private fun mathToRadians(): RunDouble = RunDouble(Math.toRadians(value))
    private fun mathPow(dst: RunValue): RunDouble = RunDouble(value.pow(dst.valueDouble()))
    private fun mathExp(): RunDouble = RunDouble(kotlin.math.exp(value))
    private fun mathLog(dst: RunValue): RunDouble = RunDouble(log(value, dst.valueDouble()))
}
