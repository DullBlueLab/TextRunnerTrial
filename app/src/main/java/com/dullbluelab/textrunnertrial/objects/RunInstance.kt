package com.dullbluelab.textrunnertrial.objects

import com.dullbluelab.textrunnertrial.Errors
import com.dullbluelab.textrunnertrial.action.Spaces
import com.dullbluelab.textrunnertrial.block.BlockClassDef
import com.dullbluelab.textrunnertrial.block.BlockFunDef
import com.dullbluelab.textrunnertrial.logic.References

class RunInstance(var classes: BlockClassDef) : RunObject(Type.INSTANCE) {
    var members: Spaces.VarLists = Spaces.VarLists()
    var supers: RunInstance? = null

    override fun execute(method: String, arguments: MutableList<RunObject>): RunObject? {
        val result: RunObject? = super.execute(method, arguments)
        return result ?: throw Errors.Syntax("${Errors.message(Errors.Key.METHOD)} $method instance ${typeWord()}")
    }

    fun set(dst: RunObject): RunObject {
        if (dst.type != Type.INSTANCE)
            throw Errors.Syntax("${Errors.message(Errors.Key.NOT_MATCH_TYPE)} method set instance ${typeWord()}")

        val instances = dst as RunInstance

        val dstClass = instances.classes
        if (classes.matchClass(dstClass)) {
            members = instances.members
            supers = instances.supers
        }
        return dst
    }

    override fun toRunString(): RunString = RunString("class : ${classNames()?.text()}")
    override fun isRunValue(): Boolean = false
    override fun typeWord(): String = classes.text()

    override fun matchType(typeName: Spaces.Names): Boolean {
        var match = classNames()?.match(typeName) ?: false
        if (!match && supers != null) match = supers!!.matchType(typeName)
        return match
    }

    fun classDef(): BlockClassDef = classes
    fun classNames(): Spaces.Names? = classes.classNameSpaces()
    fun references(): References.Lists? = classes.reference()
    fun setSuper(superClass: RunInstance) { supers = superClass }
    fun members() = members


    fun searchObjects(word: String): RunObject? {
        var result = members.search(word)?.value()
        if (result == null && supers != null) result = supers!!.searchObjects(word)
        return result
    }

    fun searchFunDef(word: String): BlockFunDef? {
        var result: BlockFunDef? = null
        var seek: BlockClassDef? = classes

        while (result == null && seek != null) {
            result = seek.findMethod(word)
            if (result == null) seek = seek.superClassDef()
        }
        return result
    }
}