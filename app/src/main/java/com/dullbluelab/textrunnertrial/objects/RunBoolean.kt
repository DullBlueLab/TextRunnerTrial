package com.dullbluelab.textrunnertrial.objects

import com.dullbluelab.textrunnertrial.Errors
import com.dullbluelab.textrunnertrial.block.BlockWord
import com.dullbluelab.textrunnertrial.logic.Syntax

private val RW = Syntax.Reserved.Word
private val MW = Syntax.Method.Word

class RunBoolean() : RunValue(Type.BOOLEANS) {

    private var flag: Boolean = false

    constructor(flag: Boolean) : this() {
        this.flag = flag
    }

    constructor(block: BlockWord) : this() {
        this.flag = (block.strings() == RW.TRUE)
    }

    override fun typeWord(): String = RW.BOOLEAN

    override fun toRunString(): RunString = RunString(flag.toString())

    override fun valueInt() = if (flag) 1 else 0
    override fun valueString() = flag.toString()
    override fun valueDouble(): Double = if (flag) 1.0 else 0.0
    override fun valueBoolean() = flag

    override fun execute(method: String, arguments: MutableList<RunObject>) : RunObject {
        var result: RunObject? = super.execute(method, arguments)

        if (result?.type == null) {
            if (arguments.size == 0) {
                result = when (method) {
                    Syntax.Method.Word.MINUS,
                    Syntax.Method.Word.NOT -> RunBoolean(!flag)
                    else -> null
                }
            }
            else if (arguments.size == 1 && arguments[0].isRunValue()) {
                val dst = arguments[0] as RunValue
                result = when (method) {
                    Syntax.Method.Word.AND -> RunBoolean(flag && dst.valueBoolean())
                    Syntax.Method.Word.OR  -> RunBoolean(flag || dst.valueBoolean())
                    else -> null
                }
            }
            else result = null
        }
        return result ?: throw Errors.Syntax("${Errors.message(Errors.Key.METHOD)} $method data type Boolean")
    }

    override fun set(dst: RunValue) : RunValue {
        flag = dst.valueBoolean()
        return RunBoolean(flag)
    }

    override fun add(dst: RunValue) : RunValue = RunBoolean(flag || dst.valueBoolean())
    override fun sub(dst: RunValue): RunValue = RunBoolean((flag && !dst.valueBoolean()) || (!flag && dst.valueBoolean()))
    override fun multi(dst: RunValue): RunValue = RunBoolean(flag && dst.valueBoolean())
    override fun div(dst: RunValue): RunValue = RunBoolean((flag && !dst.valueBoolean()) || (!flag && dst.valueBoolean()))
    override fun mod(dst: RunValue): RunValue = RunBoolean((flag && !dst.valueBoolean()) || (!flag && dst.valueBoolean()))

    override fun moreSmall(dst: RunValue): RunBoolean = RunBoolean(valueInt() < dst.valueInt())
    override fun moreLarge(dst: RunValue): RunBoolean = RunBoolean(valueInt() > dst.valueInt())
    override fun small(dst: RunValue): RunBoolean = RunBoolean(valueInt() <= dst.valueInt())
    override fun large(dst: RunValue): RunBoolean = RunBoolean(valueInt() >= dst.valueInt())
    override fun equal(dst: RunValue): RunBoolean = RunBoolean(flag == dst.valueBoolean())
    override fun notEqual(dst: RunValue):RunBoolean = RunBoolean(flag != dst.valueBoolean())
}
