package com.dullbluelab.textrunnertrial.objects

import com.dullbluelab.textrunnertrial.Errors
import com.dullbluelab.textrunnertrial.logic.Syntax

class RunVoid : RunObject(Type.VOIDS) {

    override fun toRunString(): RunString = RunString("[void]")
    override fun isRunValue(): Boolean = false
    override fun typeWord(): String = Syntax.Reserved.Word.VOID

    override fun execute(method: String, arguments: MutableList<RunObject>): RunObject {
        throw Errors.Syntax("${Errors.message(Errors.Key.METHOD)} $method, Void cannot be executed")
    }
}
