package com.dullbluelab.textrunnertrial.objects

import com.dullbluelab.textrunnertrial.Errors
import com.dullbluelab.textrunnertrial.logic.Syntax

private val RW = Syntax.Reserved.Word
private val MW = Syntax.Method.Word

class RunList() : RunObject(Type.LISTS) {
    private var list: MutableList<RunObject> = mutableListOf()

    constructor(list: MutableList<RunObject>) : this() {
        this.list = list
    }

    override fun isRunValue(): Boolean = false
    override fun typeWord(): String = "List"

    override fun toRunString(): RunString {
        var text: String = Syntax.Chars.LIST_DATA_BRACKET_START.toString()
        list.forEach {
            if (text.length > 1) text += ", "
            text += it.toRunString().valueString()
        }
        text += Syntax.Chars.LIST_DATA_BRACKET_END.toString()
        return RunString(text)
    }

    override fun execute(method: String, arguments: MutableList<RunObject>) : RunObject {
        var result: RunObject? = null
        when {
            (arguments.size == 2) -> {
                val dstA = arguments[0]
                val dstB = arguments[1]
                result = when (method) {
                    MW.APPEND -> append(dstA, dstB)
                    else -> null
                }
            }
            (arguments.size == 1) -> {
                val dst = arguments[0]
                result = when (method) {
                    MW.SET -> set(dst)
                    MW.ADD_SET -> addSet(dst)
                    MW.SUB_SET -> subSet(dst)
                    MW.ADD -> add(dst)
                    MW.SUB -> sub(dst)
                    MW.ITEM -> item(dst)
                    MW.REMOVE_AT -> removeAt(dst)
                    MW.ADD_LIST -> addList(dst)
                    else -> null
                }
            }
            (arguments.size == 0) -> {
                result = when (method) {
                    MW.SIZE -> size()
                    MW.CLEAR -> clear()
                    else -> null
                }
            }
        }
        return result ?: throw Errors.Syntax("${Errors.message(Errors.Key.METHOD)} $method data type List")
    }

    private fun set(dst: RunObject): RunObject {
        when (dst.type) {
            Type.LISTS -> list = (dst as RunList).list
            else -> throw Errors.Syntax("${Errors.message(Errors.Key.NOT_MATCH_TYPE)} List object")
        }
        return dst
    }

    private fun item(dst: RunObject): RunObject {
        if (dst.type != Type.INTS)
            throw Errors.Syntax(Errors.message(Errors.Key.ILLEGAL_LIST_INDEX))

        val index = (dst as RunInt).valueInt()
        if (index < 0 || list.size <= index)
            throw Errors.Syntax("${Errors.message(Errors.Key.OUT_OF_RANGE)} list index")

        return list[index]
    }

    private fun size(): RunObject {
        return RunInt(list.size)
    }

    private fun clear(): RunObject {
        list.clear()
        return RunList(list)
    }

    private fun addSet(dst: RunObject): RunObject {
        if (dst.type == Type.LISTS) list.addAll((dst as RunList).list)
        else list.add(dst)
        return RunList(list)
    }

    private fun subSet(dst: RunObject): RunObject {
        if (dst.type == Type.LISTS) list.removeAll((dst as RunList).list)
        else list.remove(dst)
        return RunList(list)
    }

    private fun add(dst: RunObject): RunObject {
        val newList: MutableList<RunObject> = mutableListOf()
        newList.addAll(list)
        if (dst.type == Type.LISTS) newList.addAll((dst as RunList).list)
        else newList.add(dst)
        return RunList(newList)
    }

    private fun sub(dst: RunObject): RunObject {
        val newList: MutableList<RunObject> = mutableListOf()
        newList.addAll(list)
        if (dst.type == Type.LISTS) newList.removeAll((dst as RunList).list)
        else newList.remove(dst)
        return RunList(newList)
    }

    private fun append(index: RunObject, objects: RunObject): RunObject {
        if (!index.isRunValue()) throw Errors.Syntax(Errors.message(Errors.Key.ILLEGAL_ARGUMENT))
        val number = (index as RunValue).valueInt()

        if (list.size == number) {
            list.add(objects)
        }
        else if (0 <= number && number < list.size) {
            list.add(number, objects)
        }
        else {
            throw Errors.Syntax(Errors.message(Errors.Key.OUT_OF_RANGE))
        }
        return RunList(list)
    }

    private fun removeAt(index: RunObject): RunObject {
        if (!index.isRunValue()) throw Errors.Syntax(Errors.message(Errors.Key.ILLEGAL_ARGUMENT))
        val number = (index as RunValue).valueInt()

        if (0 <= number && number < list.size) {
            list.removeAt(number)
        }
        else {
            throw Errors.Syntax(Errors.message(Errors.Key.OUT_OF_RANGE))
        }
        return RunList(list)
    }

    private fun addList(dst: RunObject): RunObject {
        if (dst.type == Type.LISTS) list.add(dst)
        else {
            val dstList = RunList().add(dst)
            list.add(dstList)
        }
        return RunList(list)
    }
}
