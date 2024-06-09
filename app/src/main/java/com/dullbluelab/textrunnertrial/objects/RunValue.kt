package com.dullbluelab.textrunnertrial.objects

import com.dullbluelab.textrunnertrial.action.Spaces
import com.dullbluelab.textrunnertrial.logic.Syntax

private val RW = Syntax.Reserved.Word
private val MW = Syntax.Method.Word

abstract class RunValue(type: Type) : RunObject(type) {
    abstract fun valueInt(): Int
    abstract fun valueString(): String
    abstract fun valueDouble(): Double
    abstract fun valueBoolean(): Boolean

    override fun toRunString(): RunString = RunString(valueString())
    override fun isRunValue(): Boolean = true

    override fun matchType(typeName: Spaces.Names): Boolean {
        return (typeWord() == typeName.text())
    }

    abstract fun set(dst: RunValue): RunValue
    abstract fun add(dst: RunValue): RunValue
    abstract fun sub(dst: RunValue): RunValue
    abstract fun multi(dst: RunValue): RunValue
    abstract fun div(dst: RunValue): RunValue
    abstract fun mod(dst: RunValue): RunValue
    abstract fun moreSmall(dst: RunValue): RunBoolean
    abstract fun moreLarge(dst: RunValue): RunBoolean
    abstract fun small(dst: RunValue): RunBoolean
    abstract fun large(dst: RunValue): RunBoolean
    abstract fun equal(dst: RunValue): RunBoolean
    abstract fun notEqual(dst: RunValue): RunBoolean

    protected open fun checkLevel(dst: RunValue): RunValue = this

    override fun execute(method: String, arguments: MutableList<RunObject>): RunObject? {
        var result: RunObject? = super.execute(method, arguments)
        if (result != null) return result

        if (arguments.size == 0) {
            result = when (method) {
                MW.TO_DOUBLE -> RunDouble(valueDouble())
                MW.TO_INT -> RunInt(valueInt())
                else -> null
            }
        }
        else if (arguments.size == 1 && arguments[0].isRunValue()) {
            val dst = arguments[0] as RunValue
            result = when (method) {
                MW.SET -> set(dst)
                MW.ADD -> checkLevel(dst).add(dst)
                MW.SUB -> checkLevel(dst).sub(dst)
                MW.MULTI -> checkLevel(dst).multi(dst)
                MW.DIV -> checkLevel(dst).div(dst)
                MW.MOD -> checkLevel(dst).mod(dst)
                MW.EQUAL -> checkLevel(dst).equal(dst)
                MW.NOT_EQUAL -> checkLevel(dst).notEqual(dst)
                MW.MORE_SMALL -> checkLevel(dst).moreSmall(dst)
                MW.MORE_LARGE -> checkLevel(dst).moreLarge(dst)
                MW.SMALL -> checkLevel(dst).small(dst)
                MW.LARGE -> checkLevel(dst).large(dst)
                RW.INIT -> set(dst)
                else -> null
            }
        }
        return result
    }
}
