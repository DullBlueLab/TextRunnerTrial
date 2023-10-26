package com.example.textrunnertrial.action

import androidx.core.text.isDigitsOnly
import com.example.textrunnertrial.Errors
import com.example.textrunnertrial.logic.CodeBlock
import com.example.textrunnertrial.logic.References
import com.example.textrunnertrial.logic.Syntax
import java.lang.Math.toDegrees
import java.lang.Math.toRadians
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

class Objects {

    enum class Type {
        INSTANCE, METHOD, BOOLEANS,
        INTS, DOUBLES, STRINGS,
        VOIDS, ERRORS,
        LISTS,
    }

    companion object {
        fun isValue(type: Type): Boolean =
            (type == Type.INTS || type == Type.DOUBLES || type == Type.STRINGS
                    || type == Type.BOOLEANS)

        private val RW = Syntax.Reserved.Word
        private val MW = Syntax.Method.Word

        fun typeValue(word: String): Type? =
            when (word) {
                RW.INT     -> Type.INTS
                RW.STRING  -> Type.STRINGS
                RW.DOUBLE  -> Type.DOUBLES
                RW.BOOLEAN -> Type.BOOLEANS
                RW.LIST    -> Type.LISTS
                else -> null
            }
    }

    abstract class Common(
        private val type: Type
    ){
        private val name: String = ""

        fun type() = type
        fun name() = name

        abstract fun toStrings(): Strings
        abstract fun isValues(): Boolean
        abstract fun typeWord(): String

        open fun execute(method: String, arguments: MutableList<Common>): Common? {
            var result: Common? = null
            if (arguments.size == 0) {
                result = when(method) {
                    MW.TO_STRING -> toStrings()
                    MW.TYPE -> Strings(typeWord())
                    else -> null
                }
            }
            return result
        }

        open fun matchType(typeName: Spaces.Names): Boolean {
            return (typeWord() != typeName.text())
        }

        open fun typeError() = (type == Type.ERRORS)
    }

    class Instances(
        private var classes: CodeBlock.ClassDef ) : Common(Type.INSTANCE) {
        private var members: Spaces.VarLists = Spaces.VarLists()
        private var supers: Instances? = null

        override fun execute(method: String, arguments: MutableList<Common>): Common {
            val result: Common? = super.execute(method, arguments)
            return result ?: throw Errors.Syntax("${Errors.message(Errors.Key.METHOD)} $method instance ${typeWord()}")
        }

        fun set(dst: Common): Common {
            if (dst.type() != Type.INSTANCE)
                throw Errors.Syntax("${Errors.message(Errors.Key.NOT_MATCH_TYPE)} method set instance ${typeWord()}")

            val instances = dst as Instances

            val dstClass = instances.classes
            if (classes.matchClass(dstClass)) {
                members = instances.members
                supers = instances.supers
            }
            return dst
        }

        override fun toStrings(): Strings = Strings("class : ${ classNames()?.text() }")
        override fun isValues(): Boolean = false
        override fun typeWord(): String = classes.text()

        override fun matchType(typeName: Spaces.Names): Boolean {
            var match = classNames()?.match(typeName) ?: false
            if (!match && supers != null) match = supers!!.matchType(typeName)
            return match
        }

        fun classNames(): Spaces.Names? = classes.classNameSpaces()
        fun classDef() = classes
        fun members() = members
        fun supers() = supers
        fun setSupers(supers: Instances) { this.supers = supers }

        fun references(): References.Lists? = classes.reference()

        fun searchObjects(word: String): Common? {
            var result = members.search(word)?.value()
            if (result == null && supers != null) result = supers!!.searchObjects(word)
            return result
        }

        fun searchFunDef(word: String): CodeBlock.FunDef? {
            var result: CodeBlock.FunDef? = null
            var seek: CodeBlock.ClassDef? = classes

            while (result == null && seek != null) {
                result = seek.findMethod(word)
                if (result == null) seek = seek.superClassDef()
            }
            return result
        }
    }

/*    class Methods(private val def: CodeBlock.FunDef) : Common(Type.METHOD) {

        override fun toStrings(): Strings = Strings("method : ${ def.name() }")
        override fun isValues(): Boolean = false
        override fun typeWord(): String = "Method"
        override fun matchType(typeName: Spaces.Names): Boolean = false
    }
*/
    abstract class Values(type: Type) : Common(type) {
        abstract fun valueInt(): Int
        abstract fun valueString(): String
        abstract fun valueDouble(): Double
        abstract fun valueBoolean(): Boolean

        override fun toStrings(): Strings = Strings(valueString())
        override fun isValues(): Boolean = true

        override fun matchType(typeName: Spaces.Names): Boolean {
            return (typeWord() == typeName.text())
        }

        abstract fun set(dst: Values): Values
        abstract fun add(dst: Values): Values
        abstract fun sub(dst: Values): Values
        abstract fun multi(dst: Values): Values
        abstract fun div(dst: Values): Values
        abstract fun mod(dst: Values): Values
        abstract fun moreSmall(dst: Values): Booleans
        abstract fun moreLarge(dst: Values): Booleans
        abstract fun small(dst: Values): Booleans
        abstract fun large(dst: Values): Booleans
        abstract fun equal(dst: Values): Booleans
        abstract fun notEqual(dst: Values): Booleans

        protected open fun checkLevel(dst: Values): Values = this

        override fun execute(method: String, arguments: MutableList<Common>) : Common? {
            var result: Common? = super.execute(method, arguments)
            if (result != null) return result

            if (arguments.size == 0) {
                result = when (method) {
                    MW.TO_DOUBLE -> Doubles(valueDouble())
                    MW.TO_INT -> Ints(valueInt())
                    else -> null
                }
            }
            else if (arguments.size == 1 && arguments[0].isValues()) {
                val dst = arguments[0] as Values
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

    class Ints() : Values(Type.INTS) {
        private var value: Int = 0

        constructor(value: Int) : this() {
            this.value = value
        }

        constructor(block: CodeBlock.Number) : this() {
            this.value = block.strings().toInt()
        }

        override fun typeWord(): String = RW.INT

        override fun valueInt() = value
        override fun valueString() = value.toString()
        override fun valueDouble(): Double = value.toDouble()
        override fun valueBoolean() = (value != 0)

        override fun checkLevel(dst: Values): Values =
            if (dst.type() == Type.DOUBLES) Doubles(value.toDouble()) else this

        override fun execute(method: String, arguments: MutableList<Common>) : Common {
            var result: Common? = super.execute(method, arguments)
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
            else if (arguments.size == 1 && arguments[0].isValues()) {
                val dst = arguments[0] as Values
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

        override fun set(dst: Values) : Values {
            value = dst.valueInt()
            return Ints(value)
        }

        private fun addSet(dst: Values) : Values {
            value += dst.valueInt()
            return Ints(value)
        }

        private fun subSet(dst: Values) : Values {
            value -= dst.valueInt()
            return Ints(value)
        }

        private fun incFront(): Common = Ints(value++)
        private fun incRear(): Common = Ints(++value)
        private fun decFront(): Common = Ints(value--)
        private fun decRear(): Common = Ints(--value)

        private fun minus(): Values = Ints(-value)

        private fun maxes(dst: Values) = Ints(value.coerceAtLeast(dst.valueInt()))
        private fun mines(dst: Values) = Ints(value.coerceAtMost(dst.valueInt()))

        override fun add(dst: Values): Values = Ints(value + dst.valueInt())
        override fun sub(dst: Values): Values = Ints(value - dst.valueInt())
        override fun multi(dst: Values): Values = Ints(value * dst.valueInt())
        override fun div(dst: Values): Values = Ints(value / dst.valueInt())
        override fun mod(dst: Values): Values = Ints(value % dst.valueInt())
        override fun moreSmall(dst: Values): Booleans = Booleans(value < dst.valueInt())
        override fun moreLarge(dst: Values): Booleans = Booleans( value > dst.valueInt())
        override fun small(dst: Values): Booleans = Booleans(value <= dst.valueInt())
        override fun large(dst: Values): Booleans = Booleans(value >= dst.valueInt())
        override fun equal(dst: Values): Booleans = Booleans(value == dst.valueInt())
        override fun notEqual(dst: Values):Booleans = Booleans(value != dst.valueInt())

        private fun mathAbs(): Values = Ints(abs(value))
    }

    class Doubles() : Values(Type.DOUBLES) {
        private var value: Double = 0.0

        constructor(value: Double) : this() {
            this.value = value
        }

        constructor(block: CodeBlock.Number) : this() {
            this.value = block.strings().toDouble()
        }

        override fun typeWord(): String = RW.DOUBLE

        override fun valueInt() = value.toInt()
        override fun valueString() = value.toString()
        override fun valueDouble(): Double = value
        override fun valueBoolean() = (value != 0.0)

        override fun execute(method: String, arguments: MutableList<Common>) : Common {
            var result: Common? = super.execute(method, arguments)
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
            else if (arguments.size == 1 && arguments[0].isValues()) {
                val dst = arguments[0] as Values
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

        override fun set(dst: Values) : Values {
            value = dst.valueDouble()
            return Doubles(value)
        }

        private fun addSet(dst: Values) : Values {
            value += dst.valueInt()
            return Doubles(value)
        }

        private fun subSet(dst: Values) : Values {
            value -= dst.valueInt()
            return Doubles(value)
        }

        private fun incFront(): Common = Doubles(value++)
        private fun incRear(): Common = Doubles(++value)
        private fun decFront(): Common = Doubles(value--)
        private fun decRear(): Common = Doubles(--value)

        private fun minus(): Values = Doubles(-value)

        private fun maxes(dst: Values) = Doubles(value.coerceAtLeast(dst.valueDouble()))
        private fun mines(dst: Values) = Doubles(value.coerceAtMost(dst.valueDouble()))

        override fun add(dst: Values): Values = Doubles(value + dst.valueDouble())
        override fun sub(dst: Values): Values = Doubles(value - dst.valueDouble())
        override fun multi(dst: Values): Values = Doubles(value * dst.valueDouble())
        override fun div(dst: Values): Values = Doubles(value / dst.valueDouble())
        override fun mod(dst: Values): Values = Doubles(value % dst.valueDouble())

        override fun moreSmall(dst: Values): Booleans = Booleans(value < dst.valueDouble())
        override fun moreLarge(dst: Values): Booleans = Booleans( value > dst.valueDouble())
        override fun small(dst: Values): Booleans = Booleans(value <= dst.valueDouble())
        override fun large(dst: Values): Booleans = Booleans(value >= dst.valueDouble())
        override fun equal(dst: Values): Booleans = Booleans(value == dst.valueDouble())
        override fun notEqual(dst: Values):Booleans = Booleans(value != dst.valueDouble())

        private fun mathAbs(): Doubles = Doubles(abs(value))
        private fun mathACos(): Doubles = Doubles(acos(value))
        private fun mathASin(): Doubles = Doubles(asin(value))
        private fun mathATan(): Doubles = Doubles(atan(value))
        private fun mathCos(): Doubles = Doubles(cos(value))
        private fun mathSin(): Doubles = Doubles(sin(value))
        private fun mathTan(): Doubles = Doubles(tan(value))
        private fun mathCeil(): Doubles = Doubles(ceil(value))
        private fun mathFloor(): Doubles = Doubles(floor(value))
        private fun mathRound(): Doubles = Doubles(round(value))
        private fun mathSqrt(): Doubles = Doubles(sqrt(value))
        private fun mathToDegrees(): Doubles = Doubles(toDegrees(value))
        private fun mathToRadians(): Doubles = Doubles(toRadians(value))
        private fun mathPow(dst: Values): Doubles = Doubles(value.pow(dst.valueDouble()))
        private fun mathExp(): Doubles = Doubles(kotlin.math.exp(value))
        private fun mathLog(dst: Values): Doubles = Doubles(log(value, dst.valueDouble()))
    }

    class Strings() : Values(Type.STRINGS) {
        private var value: String = ""

        constructor(value: String) : this() {
            this.value = value
        }

        constructor(block: CodeBlock.Strings) : this() {
            this.value = block.strings()
        }

        override fun typeWord(): String = RW.STRING

        override fun valueInt() =
            if (value.isNotEmpty() && value.isDigitsOnly()) value.toInt() else 0
        override fun valueString() = value
        override fun valueDouble(): Double =
            if (value.isNotEmpty() && value.isDigitsOnly()) value.toDouble() else 0.0
        override fun valueBoolean() = (value == "true")

        override fun execute(method: String, arguments: MutableList<Common>) : Common {
            var result: Common? = super.execute(method, arguments)

            if (result?.type() == null) {
                if (arguments.size == 0) {
                    result = when (method) {
                        MW.LENGTH -> length()
                        MW.LOWERCASE -> lowercase()
                        MW.UPPERCASE -> uppercase()
                        else -> null
                    }
                }
                else if (arguments.size == 1 && arguments[0].isValues()) {
                    val dst = arguments[0] as Values
                    result = when (method) {
                        MW.ADD_SET -> addSet(dst)
                        MW.CHAR_AT -> charAt(dst)
                        MW.CONTAINS -> contains(dst)
                        else -> null
                    }
                }
                else if (arguments.size == 2 && arguments[0].isValues() && arguments[1].isValues()) {
                    val dst = arguments[0] as Values
                    val param = arguments[1] as Values
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

        override fun set(dst: Values) : Values {
            value = dst.valueString()
            return Strings(value)
        }

        private fun addSet(dst: Values) : Values {
            value += dst.valueString()
            return Strings(value)
        }

        private fun charAt(dst: Values): Common {
            val index = dst.valueInt()
            if (index < 0 || value.length <= index)
                throw Errors.Syntax("${Errors.message(Errors.Key.OUT_OF_SIZE)} charAt")

            return Strings(value[index].toString())
        }

        private fun contains(dst: Values): Common {
            return Booleans(value.indexOf(dst.valueString()) >= 0)
        }

        private fun indexOf(dst: Values, from: Values): Common {
            return Ints(value.indexOf(dst.valueString(), from.valueInt()))
        }

        private fun length(): Common = Ints(value.length)

        private fun substring(begin: Values, end: Values): Common {
            val bv = begin.valueInt()
            val ev = end.valueInt()
            val len = value.length

            if ((bv < 0 || len <= bv) || (ev < 0 || len <= ev) || (bv > ev))
                throw Errors.Syntax("${Errors.message(Errors.Key.OUT_OF_SIZE)} substring")

            return Strings(value.substring(bv, ev))
        }

        private fun lowercase(): Common = Strings(value.lowercase())
        private fun uppercase(): Common = Strings(value.uppercase())

        override fun add(dst: Values) : Values = Strings(this.value + dst.valueString())
        override fun sub(dst: Values): Values = Strings((value.toDouble() + dst.valueDouble()).toString())
        override fun multi(dst: Values): Values = Strings((value.toDouble() * dst.valueDouble()).toString())
        override fun div(dst: Values): Values = Strings((value.toDouble() / dst.valueDouble()).toString())
        override fun mod(dst: Values): Values = Strings((value.toDouble() % dst.valueDouble()).toString())

        override fun moreSmall(dst: Values): Booleans = Booleans(value < dst.valueString())
        override fun moreLarge(dst: Values): Booleans = Booleans( value > dst.valueString())
        override fun small(dst: Values): Booleans = Booleans(value <= dst.valueString())
        override fun large(dst: Values): Booleans = Booleans(value >= dst.valueString())
        override fun equal(dst: Values): Booleans = Booleans(value == dst.valueString())
        override fun notEqual(dst: Values):Booleans = Booleans(value != dst.valueString())
    }

    class Booleans() : Values(Type.BOOLEANS) {
        private var flag: Boolean = false

        constructor(flag: Boolean) : this() {
            this.flag = flag
        }

        constructor(block: CodeBlock.Word) : this() {
            this.flag = (block.strings() == RW.TRUE)
        }

        override fun typeWord(): String = RW.BOOLEAN

        override fun valueInt() = if (flag) 1 else 0
        override fun valueString() = flag.toString()
        override fun valueDouble(): Double = if (flag) 1.0 else 0.0
        override fun valueBoolean() = flag

        override fun execute(method: String, arguments: MutableList<Common>) : Common {
            var result: Common? = super.execute(method, arguments)

            if (result?.type() == null) {
                if (arguments.size == 0) {
                    result = when (method) {
                        Syntax.Method.Word.MINUS,
                        Syntax.Method.Word.NOT -> Booleans(!flag)
                        else -> null
                    }
                }
                else if (arguments.size == 1 && arguments[0].isValues()) {
                    val dst = arguments[0] as Values
                    result = when (method) {
                        Syntax.Method.Word.AND -> Booleans(flag && dst.valueBoolean())
                        Syntax.Method.Word.OR  -> Booleans(flag || dst.valueBoolean())
                        else -> null
                    }
                }
                else result = null
            }
            return result ?: throw Errors.Syntax("${Errors.message(Errors.Key.METHOD)} $method data type Boolean")
        }

        override fun set(dst: Values) : Values {
            flag = dst.valueBoolean()
            return Booleans(flag)
        }

        override fun add(dst: Values) : Values = Booleans(flag || dst.valueBoolean())
        override fun sub(dst: Values): Values = Booleans((flag && !dst.valueBoolean()) || (!flag && dst.valueBoolean()))
        override fun multi(dst: Values): Values = Booleans(flag && dst.valueBoolean())
        override fun div(dst: Values): Values = Booleans((flag && !dst.valueBoolean()) || (!flag && dst.valueBoolean()))
        override fun mod(dst: Values): Values = Booleans((flag && !dst.valueBoolean()) || (!flag && dst.valueBoolean()))

        override fun moreSmall(dst: Values): Booleans = Booleans(valueInt() < dst.valueInt())
        override fun moreLarge(dst: Values): Booleans = Booleans(valueInt() > dst.valueInt())
        override fun small(dst: Values): Booleans = Booleans(valueInt() <= dst.valueInt())
        override fun large(dst: Values): Booleans = Booleans(valueInt() >= dst.valueInt())
        override fun equal(dst: Values): Booleans = Booleans(flag == dst.valueBoolean())
        override fun notEqual(dst: Values):Booleans = Booleans(flag != dst.valueBoolean())
    }

    class Voids() : Common(Type.VOIDS) {
        override fun toStrings(): Strings = Strings("[void]")
        override fun isValues(): Boolean = false
        override fun typeWord(): String = Syntax.Reserved.Word.VOID

        override fun execute(method: String, arguments: MutableList<Common>): Common {
            throw Errors.Syntax("${Errors.message(Errors.Key.METHOD)} $method, Void cannot be executed")
        }
    }

    class Lists() : Common(Type.LISTS) {
        private var list: MutableList<Common> = mutableListOf()

        constructor(list: MutableList<Common>) : this() {
            this.list = list
        }

        override fun isValues(): Boolean = false
        override fun typeWord(): String = "List"

        override fun toStrings(): Strings {
            var text: String = Syntax.Chars.LIST_DATA_BRACKET_START.toString()
            list.forEach {
                if (text.length > 1) text += ", "
                text += it.toStrings().valueString()
            }
            text += Syntax.Chars.LIST_DATA_BRACKET_END.toString()
            return Strings(text)
        }

        override fun execute(method: String, arguments: MutableList<Common>) : Common {
            var result: Common? = null
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

        private fun set(dst: Common): Common {
            when (dst.type()) {
                Type.LISTS -> list = (dst as Lists).list
                else -> throw Errors.Syntax("${Errors.message(Errors.Key.NOT_MATCH_TYPE)} List object")
            }
            return dst
        }

        private fun item(dst: Common): Common {
            if (dst.type() != Type.INTS)
                throw Errors.Syntax(Errors.message(Errors.Key.ILLEGAL_LIST_INDEX))

            val index = (dst as Ints).valueInt()
            if (index < 0 || list.size <= index)
                throw Errors.Syntax("${Errors.message(Errors.Key.OUT_OF_RANGE)} list index")

            return list[index]
        }

        private fun size(): Common {
            return Ints(list.size)
        }

        private fun clear(): Common {
            list.clear()
            return Lists(list)
        }

        private fun addSet(dst: Common): Common {
            if (dst.type() == Type.LISTS) list.addAll((dst as Lists).list)
            else list.add(dst)
            return Lists(list)
        }

        private fun subSet(dst: Common): Common {
            if (dst.type() == Type.LISTS) list.removeAll((dst as Lists).list)
            else list.remove(dst)
            return Lists(list)
        }

        private fun add(dst: Common): Common {
            val newList: MutableList<Common> = mutableListOf()
            newList.addAll(list)
            if (dst.type() == Type.LISTS) newList.addAll((dst as Lists).list)
            else newList.add(dst)
            return Lists(newList)
        }

        private fun sub(dst: Common): Common {
            val newList: MutableList<Common> = mutableListOf()
            newList.addAll(list)
            if (dst.type() == Type.LISTS) newList.removeAll((dst as Lists).list)
            else newList.remove(dst)
            return Lists(newList)
        }

        private fun append(index: Common, objects: Common): Common {
            if (!index.isValues()) throw Errors.Syntax(Errors.message(Errors.Key.ILLEGAL_ARGUMENT))
            val number = (index as Values).valueInt()

            if (list.size == number) {
                list.add(objects)
            }
            else if (0 <= number && number < list.size) {
                list.add(number, objects)
            }
            else {
                throw Errors.Syntax(Errors.message(Errors.Key.OUT_OF_RANGE))
            }
            return Lists(list)
        }

        private fun removeAt(index: Common): Common {
            if (!index.isValues()) throw Errors.Syntax(Errors.message(Errors.Key.ILLEGAL_ARGUMENT))
            val number = (index as Values).valueInt()

            if (0 <= number && number < list.size) {
                list.removeAt(number)
            }
            else {
                throw Errors.Syntax(Errors.message(Errors.Key.OUT_OF_RANGE))
            }
            return Lists(list)
        }

        private fun addList(dst: Common): Common {
            if (dst.type() == Type.LISTS) list.add(dst)
            else {
                val dstList = Lists().add(dst)
                list.add(dstList)
            }
            return Lists(list)
        }
    }
}
