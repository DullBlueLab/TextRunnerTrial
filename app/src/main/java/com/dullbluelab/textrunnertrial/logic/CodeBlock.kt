package com.dullbluelab.textrunnertrial.logic

import com.dullbluelab.textrunnertrial.Errors
import com.dullbluelab.textrunnertrial.action.Objects
import com.dullbluelab.textrunnertrial.action.Spaces

class CodeBlock {

    enum class Type {
        NULL,
        WORD, NUMBER, SIGN, BRACKET, STRING, LIST,
        OBJECT_REF, VAR_DEF, CONST_DEF, FUN_DEF, INIT_DEF, CLASS_DEF,
        IF, FOR, WHILE, WHEN, FUN_CALL, RETURN
    }

    companion object {
        const val SUBJECT = 0
        const val ARGUMENT = 1

    }

    abstract class Common(
        private var type: Type = Type.NULL,
        protected var strings: String = "",
        protected var lineNO: Int = 0
    ) {
        fun type() = type
        fun lineNO() = lineNO
        fun strings() = strings

        open fun name() = ""
        open fun setName(name: String) {}
        open fun child(index: Int): Common? = null
        open fun text() = ""

        open fun findListAndRun(process: (Lists) -> Unit) {}

        fun matchCharType(char: Char, type: Syntax.Chars.Type)
            = (char != ' ' && Syntax.Chars.isMatch(char, type))

        open fun dump(shift: String): String = "$shift[$type:$strings] \n"
    }

    class Word(strings: String, lineNO: Int) : Common(Type.WORD, strings, lineNO) {
        private var number: Int = 0

        override fun name(): String = strings
        override fun text(): String = strings
        fun codeNO(): Int = number

        constructor(box: Parser.TextBox, wordList: WordList) : this("", box.lineNO) {
            var popChar = box.getPos() ?: ' '
            if (matchCharType(popChar, Syntax.Chars.Type.WORD_START)) {
                strings += popChar

                popChar = box.getNext() ?: ' '
                while (matchCharType(popChar, Syntax.Chars.Type.WORDS)) {
                    strings += popChar
                    popChar = box.getNext() ?: ' '
                }
                number = Syntax.Reserved.codeNO(strings())
                if (number == 0) number = wordList.entry(strings())
            }
        }

        fun reservedKey(): Syntax.Reserved.Key = Syntax.Reserved.key(strings)
        fun isReservedKey(): Boolean = (number < 0)

        override fun dump(shift: String): String = "$shift[word:$number:${strings()}] \n"
    }

    class Number(strings: String, lineNO: Int) : Common(Type.NUMBER, strings, lineNO) {

        override fun text(): String = strings

        constructor(box: Parser.TextBox) : this("", box.lineNO) {
            var popChar = box.getPos() ?: ' '
            while (matchCharType(popChar, Syntax.Chars.Type.NUMBERS)) {
                strings += popChar
                popChar = box.getNext() ?: ' '
            }
        }
    }

    class Sign(strings: String, lineNO: Int) : Common(Type.SIGN, strings, lineNO) {

        constructor(box: Parser.TextBox) : this("", box.lineNO) {
            var popChar = box.getPos() ?: ' '
            while (matchCharType(popChar, Syntax.Chars.Type.SIGN)) {
                strings += popChar
                popChar = box.getNext() ?: ' '
            }
        }
    }

    class Bracket(chars: String, lineNO: Int) : Common(Type.BRACKET, chars, lineNO) {
        private var lists: Lists? = null

        fun lists(): Lists? = lists
        fun chars() = strings

        constructor(chars: String, lists: Lists, lineNO: Int) : this(chars, lineNO) {
            this.lists = lists
        }

        constructor(chars: String, lists: Lists) : this(chars, lists, 0)

        override fun text(): String {
            var text = ""
            if (strings.length >= 2) {
                text = strings[0].toString()
                lists?.forEach {
                    if (text.length > 1) text += ", "
                    text += it.text()
                }
                text += strings[1].toString()
            }
            return text
        }

        override fun dump(shift: String): String =
            "$shift[open : $strings ] \n" +
            "${lists?.dump("$shift  ")}" +
            "$shift[close : $strings] \n"

        override fun findListAndRun(process: (Lists) -> Unit) {
            lists?.findListAndRun(process)
        }
    }

    class Strings(strings: String, lineNO: Int) : Common(Type.STRING, strings, lineNO) {
        private var stringChar: Char = ' '

        constructor(box: Parser.TextBox) : this( "", box.lineNO) {
            stringChar = box.getInc() ?: return
            var popChar: Char? = box.getPos()

            while (popChar != null && popChar != stringChar) {

                if (matchCharType(popChar, Syntax.Chars.Type.ESCAPE)) {
                    popChar = box.getNext() ?: break
                    strings += Syntax.Chars.getEscapeChar(popChar)
                }
                else {
                    strings += popChar
                }
                popChar = box.getNext()
            }
            if (!box.isEnd()) box.inc()
        }

        override fun text(): String = strings()
    }

    class ListValue() : Common(Type.LIST, Syntax.Chars.LIST_DATA_BRACKET, 0) {
        private var values: Bracket? = null
        private var objects: MutableList<Objects.Common>? = null

        fun values() = values
        fun objects() = objects
        fun setObjects(objects: MutableList<Objects.Common>) { this.objects = objects}

        constructor(values: Bracket) : this() {
            this.values = values
            this.lineNO = values.lineNO()
        }

        override fun findListAndRun(process: (Lists) -> Unit) {
            values?.findListAndRun(process)
        }

        override fun text(): String = values?.text() ?: ""
        override fun dump(shift: String): String = values?.dump(shift) ?: ""
    }

    class ObjectRef() : Common(Type.OBJECT_REF, "", 0) {
        private var subject: Common? = null
        private var argument: Common? = null

        constructor(lists: Lists, index: Int, def: Syntax.Operator.Setting) : this() {
            val block = lists.block(index) ?: throw Errors.Logic("CodeBlock.ObjectRef")
            lineNO = block.lineNO()

            subject =
                if (def.prev && index > 0)
                    lists.block(index - 1)
                else if (def.next && (index + 1) < lists.size())
                    lists.block(index + 1)
                else null

            argument =
                if (def.prev && def.next && (index + 1) < lists.size())
                    lists.block(index + 1)
                else null

            if (def.method != "") {
                val method = FunCall(def.method, argument)
                strings = Syntax.Chars.OBJECT_REF
                argument = method
            }
            else {
                strings = block.strings()
            }
            if ((def.prev && subject == null) || (def.next && argument == null)) {
                throw Errors.Syntax(Errors.Key.SYNTAX, lineNO = lineNO)
            }
        }

        override fun child(index: Int): Common? {
            return when(index) {
                SUBJECT -> subject
                ARGUMENT -> argument
                else -> null
            }
        }
        override fun findListAndRun(process: (Lists) -> Unit) {
            subject?.findListAndRun(process)
            argument?.findListAndRun(process)
        }

        override fun text(): String {
            return "${subject?.text()}$strings${argument?.text()}"
        }

        override fun dump(shift: String): String {
            var text = "$shift[ref:$strings]\n"
            subject?.let { text += "$shift  subject = \n" + it.dump("$shift    ") }
            argument?.let { text += "$shift  argument = \n" + it.dump("$shift    ") }
            return text
        }
    }

    class FunCall() : Common(Type.FUN_CALL, "", 0) {
        private var argument: Common? = null

        override fun name(): String = strings

        constructor(lists: Lists, index: Int) : this() {
            val block = lists.block(index) ?: throw Errors.Logic("CodeBlock.FunCall")

            lineNO = block.lineNO()
            strings = block.name()
            argument = lists.block(index + 1) ?: throw Errors.Syntax(Errors.Key.SYNTAX, lineNO = lineNO)
        }

        constructor(method: String, args: Common?) : this() {
            strings = method
            argument = args
        }

        override fun child(index: Int): Common? {
            return when(index) {
                ARGUMENT -> argument
                else -> null
            }
        }

        override fun findListAndRun(process: (Lists) -> Unit) {
            argument?.findListAndRun(process)
        }

        override fun text(): String {
            return "$strings${argument?.text()}"
        }

        override fun dump(shift: String): String {
            var text = "$shift[method:$strings]\n"
            argument?.let { text += "$shift  argument = \n" + it.dump("$shift    ") }
            return text
        }
    }

    class VarDef() : Common(Type.VAR_DEF, Syntax.Reserved.Word.VAR, 0) {
        private val nameList = mutableListOf<Common>()
        private var className: Common? = null
        private var classArgument: Common? = null

        fun classArgument() = classArgument

        constructor(lists: Lists, index: Int) : this() {
            val codeNO = Syntax.Reserved.codeNO(Syntax.Reserved.Key.VAR)
            var block: Common? = lists.block(index)
            if (block == null || block.type() != Type.WORD || (block as Word).codeNO() != codeNO)
                throw Errors.Logic("CodeBlock.VarDef")

            lists.removeAt(index)
            lineNO = block.lineNO()

            while (index < lists.size()) {
                block = lists.block(index)!!
                val type = block.type()

                if (type != Type.WORD && type != Type.SIGN) break
                if (type == Type.SIGN && block.strings() != Syntax.Chars.COMMA) break

                if (type == Type.WORD) nameList.add(block)
                lists.removeAt(index)
            }
            if (block?.type() == Type.SIGN
                && block.strings() == Syntax.Chars.WORD_DIV_SIGN
                && (index + 1) < lists.size()) {

                lists.removeAt(index)
                className = lists.block(index)
                lists.removeAt(index)

                if (className?.type() == Type.FUN_CALL) {
                    classArgument = (className as FunCall).child(ARGUMENT)
                }
                else {
                    block = lists.block(index)
                    if (block != null && block.type() == Type.BRACKET
                        && block.strings() == Syntax.Chars.ARGUMENT_BRACKET) {

                        classArgument = block
                        lists.removeAt(index)
                    }
                }
            }
            if (nameList.size == 0 || className == null) throw Errors.Syntax(Errors.Key.SYNTAX, lineNO = lineNO)
        }

        fun setToReference(ref: References.Lists) {
            for (name in nameList) {
                ref.appendVar(name.strings(), className, this)
            }
        }

        fun nameList(): MutableList<String> {
            val list = mutableListOf<String>()

            nameList.forEach { name ->
                if (name.type() == Type.WORD) {
                    list.add((name as Word).name())
                }
            }
            return list
        }

        fun className(): Spaces.Names? {
            val names = if (className?.type() == Type.OBJECT_REF || className?.type() == Type.WORD) {
                Spaces.Names(className)
            } else if (className?.type() == Type.FUN_CALL) {
                Spaces.Names((className as FunCall).name())
            } else null

            return names
        }

        override fun dump(shift: String): String {
            var text = "$shift[var def]\n"
            text += "$shift  name = "
            nameList.forEach { block ->
                text += "${ block.text() } "
            }
            text += "\n$shift  class = ${ className?.text() }\n"

            return text
        }
    }

    class ConstDef() : Common(Type.CONST_DEF, Syntax.Reserved.Word.CONST, 0) {
        private var name: Common? = null
        private var value: Common? = null

        override fun name() = name?.text() ?: ""
        fun value() = value

        constructor(lists: Lists, index: Int) : this() {
            val codeNO = Syntax.Reserved.codeNO(Syntax.Reserved.Key.CONST)
            val operator = Syntax.Operator.word(Syntax.Operator.Type.SET)

            var block = lists.block(index)
            if (block == null || block.type() != Type.WORD || (block as Word).codeNO() != codeNO)
                throw Errors.Logic("CodeBlock.ConstDef")

            lists.removeAt(index)
            lineNO = block.lineNO()

            block = lists.block(index) ?: return
            val method = block.child(ARGUMENT) ?: return
            if (block.type() == Type.OBJECT_REF && method.name() == operator) {
                name = block.child(SUBJECT)
                value = method.child(ARGUMENT)
                lists.removeAt(index)
            }
            if (name == null || value == null) throw Errors.Syntax(Errors.Key.SYNTAX, lineNO = lineNO)
        }

        fun setToReference(ref: References.Lists) {
            val word = name?.name() ?: ""
            ref.appendConst(word, this)
        }

        override fun dump(shift: String): String {
            return "$shift[const : ${name?.text()} : ${value?.text()} ]\n"
        }
    }

    open class FunDef(name: String) : Common(Type.FUN_DEF, name, 0) {
        protected var arguments: ArgumentList? = null
        protected var returnType: Common? = null
        protected var statement: Bracket? = null
        private var references: References.Lists? = null

        override fun name(): String = strings
        final override fun setName(name: String) { strings = name }
        fun arguments() = arguments
        fun statement() = statement

        constructor(lists: Lists, start: Int) : this("") {
            val codeNO = Syntax.Reserved.codeNO(Syntax.Reserved.Key.FUN)
            var index = start

            var block = lists.block(index)
            if (block == null || block.type() != Type.WORD || (block as Word).codeNO() != codeNO)
                throw Errors.Logic("CodeBlock.FunDef")

            lists.removeAt(index)
            lineNO = block.lineNO()

            while (index < lists.size() && statement == null) {
                block = lists.block(index)!!
                // argument
                if (block.type() == Type.FUN_CALL) {
                    setName(block.name())
                    val child = block.child(ARGUMENT)
                    if (child != null && child.type() == Type.BRACKET) {
                        val args = (child as Bracket).lists()
                        args?.let { arguments = ArgumentList(it) }
                    }
                    lists.removeAt(index)
                }
                // return type
                else if (block.type() == Type.SIGN
                    && block.strings() == Syntax.Chars.WORD_DIV_SIGN) {
                    lists.removeAt(index)
                    if (index < lists.size()) {
                        returnType = lists.block(index)
                        lists.removeAt(index)
                    }
                }
                // statement
                else if (block.type() == Type.BRACKET
                    && block.strings() == Syntax.Chars.STATEMENT_BRACKET) {
                    statement = (block as Bracket)
                    lists.removeAt(index)
                }
                else {
                    index++
                }
            }
            if (arguments == null || statement == null) throw Errors.Syntax(Errors.Key.SYNTAX, lineNO = lineNO)
        }

        open fun setToReference(ref: References.Lists) {
            ref.appendFun(name(), arguments, returnType, this, references)
        }

        fun makeReference(hierarchy: Spaces.Names, parent: References.Lists?) {
            val spaceName = Spaces.Names(hierarchy, name())
            references = References.Lists(spaceName, parent)
            statement?.lists()?.makeReference(references!!)
        }

        override fun dump(shift: String): String {
            var text = "$shift[fun def: $strings]\n"
            text += arguments?.dump("$shift  ")
            text += "$shift  return = ${ returnType?.text() }\n"
            text += "$shift  statement = \n" + statement?.dump("$shift  ")
            return text
        }
    }

    class ArgumentSet() {
        private var name: Common? = null
        private var className: Common? = null

        fun name() = name
        fun className() = className

        constructor(lists: Lists) : this() {
            val errors = "${Errors.message(Errors.Key.SYNTAX)} arguments"
            if (lists.size() == 3) {

                val block = lists.block(0) ?: throw Errors.Syntax(errors)
                if (block.type() == Type.WORD) name = block

                val div = lists.block(1) ?: throw Errors.Syntax(errors)
                if (div.type() == Type.SIGN && div.strings() == Syntax.Chars.WORD_DIV_SIGN) {
                    className = lists.block(2)
                }
            }
            else {
                throw Errors.Syntax(errors)
            }
        }
    }

    class ArgumentList() {
        private val list = mutableListOf<ArgumentSet>()

        fun size() = list.size
        fun item(num: Int) = list[num]

        constructor(lists: Lists) : this() {
            val errors = "${Errors.message(Errors.Key.SYNTAX)} arguments"
            if (lists.isCommaDivide()) {
                lists.forEach { block ->
                    if (block.type() == Type.BRACKET) {
                        (block as Bracket).lists()?.let { list.add(ArgumentSet(it)) }
                    }
                    else throw Errors.Syntax(errors, block.lineNO())
                }
            }
            else if (lists.size() > 0) {
                list.add(ArgumentSet(lists))
            }
        }

        fun textOfType(): String {
            var text = "("
            list.forEach { set ->
                if (text.length > 1) text += ","
                text += set.className()?.text()
            }
            text += ")"
            return text
        }

        fun dump(shift: String): String {
            var text = ""
            list.forEach { set ->
                set.name()?.let { text += "$shift arg = ${ it.text() }" }
                set.className()?.let { text += " : class = ${ it.text() }\n" }
            }
            return text
        }
    }

    class InitDef() : FunDef(Syntax.Reserved.Word.INIT) {

        constructor(lists: Lists, start: Int) : this() {
            val codeNO = Syntax.Reserved.codeNO(Syntax.Reserved.Key.INIT)
            val argsChars = Syntax.Chars.ARGUMENT_BRACKET
            val stateChars = Syntax.Chars.STATEMENT_BRACKET
            var index = start

            var block = lists.block(index)
            if (block == null || block.type() != Type.WORD || (block as Word).codeNO() != codeNO)
                throw Errors.Logic("CodeBlock.InitDef")

            lists.removeAt(index)

            lineNO = block.lineNO()

            while (index < lists.size() && statement == null) {
                block = lists.block(index)!!
                // argument
                if (block.type() == Type.BRACKET && block.strings() == argsChars) {
                    val args = (block as Bracket).lists()
                    args?.let { arguments = ArgumentList(it) }
                    lists.removeAt(index)
                }
                // statement
                else if (block.type() == Type.BRACKET && block.strings() == stateChars) {
                    statement = (block as Bracket)
                    lists.removeAt(index)
                }
                else {
                    index++
                }
            }
            if (arguments == null || statement == null) throw Errors.Syntax(Errors.Key.SYNTAX, lineNO = lineNO)
        }

        override fun setToReference(ref: References.Lists) {
            ref.appendFun(strings, arguments, returns = null, this, childRef = null)
        }

        override fun dump(shift: String): String {
            var text = "$shift[init def: $strings]\n"
            text += arguments?.dump("$shift  ")
            text += "$shift  statement = \n" + statement?.dump("$shift  ")
            return text
        }
    }

    class BlockIf() : Common(Type.IF, Syntax.Reserved.Word.IF, 0) {
        private var conditions: Common? = null
        private var statementTrue: Common? = null
        private var statementElse: Common? = null

        fun conditions() = conditions
        fun statementTrue() = statementTrue
        fun statementElse() = statementElse

        constructor(lists: Lists, index: Int) : this() {
            val codeNO = Syntax.Reserved.codeNO(Syntax.Reserved.Key.IF)
            val codeElse = Syntax.Reserved.codeNO(Syntax.Reserved.Key.ELSE)
            var flagElse = false

            var block = lists.block(index)
            if ((block == null) || (block.type() != Type.WORD) || ((block as Word).codeNO() != codeNO))
                throw Errors.Logic("CodeBlock.BlockIf line:$lineNO")

            lists.removeAt(index)

            lineNO = block.lineNO()

            while (index < lists.size()) {
                block = lists.block(index)!!
                if (conditions == null) {
                    conditions = block
                    lists.removeAt(index)
                }
                else if (statementTrue == null) {
                    statementTrue = block
                    lists.removeAt(index)
                }
                else if (!flagElse
                    && block.type() == Type.WORD && (block as Word).codeNO() == codeElse) {
                    flagElse = true
                    lists.removeAt(index)
                }
                else if (flagElse && statementElse == null) {
                    if (block.type() == Type.WORD && (block as Word).codeNO() == codeNO) {
                        statementElse = BlockIf(lists, index)
                    }
                    else {
                        statementElse = block
                        lists.removeAt(index)
                    }
                    break
                }
                else {
                    break
                }
            }
            if (conditions == null || statementTrue == null) throw Errors.Syntax(Errors.Key.SYNTAX, lineNO = lineNO)
        }

        override fun dump(shift: String): String {
            var text = "$shift[ if ]\n"

            text += "$shift  condition = \n"
            conditions?.let { text += it.dump("$shift    ") }

            text += "$shift  if true = \n"
            statementTrue?.let { text += it.dump("$shift    ") }

            statementElse?.let {
                text += "$shift  else = \n"
                text += it.dump("$shift    ")
            }

            text += "$shift[ end if ]\n"
            return text
        }
    }

    class Condition(type: Type) : Common(type, "", 0) {
        private var conditions: Common? = null
        private var initial: Common? = null
        private var countUp: Common? = null
        private var statement: Common? = null

        fun conditions() = conditions
        fun statement() = statement
        fun initial() = initial
        fun countUp() = countUp

        constructor(lists: Lists, index: Int, type: Type) : this(type) {
            val loopType = getLoopType(type)
            val codeNO = Syntax.Reserved.codeNO(loopType)

            var block = lists.block(index)
            if ((block == null) || (block.type() != Type.WORD) || ((block as Word).codeNO() != codeNO))
                throw Errors.Logic("CodeBlock.Condition")

            lists.removeAt(index)

            lineNO = block.lineNO()

            while (index < lists.size()) {
                block = lists.block(index)
                if (block == null) throw Errors.Logic("class Condition line:$lineNO")

                if (conditions == null) {
                    if (type == Type.FOR) setConditionFor(block)
                    else conditions = block
                    lists.removeAt(index)
                    if (conditions == null) break
                }
                else if (statement == null) {
                    statement = block
                    lists.removeAt(index)
                    break
                }
                else {
                    break
                }
            }
            if (conditions == null || statement == null) throw Errors.Syntax(Errors.Key.SYNTAX, lineNO = lineNO)
        }

        private fun getLoopType(type: Type): Syntax.Reserved.Key {
            return when (type) {
                Type.FOR   -> Syntax.Reserved.Key.FOR
                Type.WHILE -> Syntax.Reserved.Key.WHILE
                else       -> throw Errors.Logic("class Condition line:$lineNO")
            }
        }

        private fun setConditionFor(block: Common) {
            val lists: Lists =
                (if (block.type() == Type.BRACKET) (block as Bracket).lists() else null)
                    ?: throw Errors.Syntax(Errors.Key.ILLEGAL_CONDITION, lineNO = lineNO)

            val branch = Syntax.Chars.FOR_BRANCH
            var cnt = 0

            while (cnt < lists.size() && cnt < 5) {
                val child = lists.block(cnt) ?: throw Errors.Logic("class Condition line:$lineNO")

                when (cnt) {
                    0 -> initial = child
                    2 -> conditions = child
                    4 -> countUp = child
                    1, 3 -> if (!(child.type() == Type.SIGN && child.strings() == branch))
                        throw Errors.Syntax(Errors.Key.ILLEGAL_CONDITION, lineNO = lineNO)
                }
                cnt ++
            }
        }

        override fun dump(shift: String): String {
            val loopType = getLoopType(type())
            val name = Syntax.Reserved.word(loopType)
            var text = "$shift[ $name ]\n"

            text += "$shift  condition =\n"
            conditions?.let { text += it.dump("$shift  ") }

            text += "$shift  statement =\n"
            statement?.let { text += it.dump("$shift  ") }

            text += "$shift[ $name end ]\n"
            return text
        }
    }

    class ClassDef() : Common(Type.CLASS_DEF, "", 0) {
        private var className: Common? = null
        private var superName: Common? = null
        private var statement: Bracket? = null
        private var references: References.Lists? = null
        private var classNameSpace: Spaces.Names? = null

        fun reference() = references

        constructor(lists: Lists, index: Int) : this() {
            val codeNO = Syntax.Reserved.codeNO(Syntax.Reserved.Key.CLASS)
            var flagSuper = false

            var block = lists.block(index)
            if (block == null || block.type() != Type.WORD || (block as Word).codeNO() != codeNO)
                throw Errors.Logic("CodeBlock.ClassDef")

            lists.removeAt(index)

            lineNO = block.lineNO()

            while (index < lists.size()) {
                block = lists.block(index)!!

                if (className == null && block.type() == Type.WORD) {
                    className = block
                    lists.removeAt(index)
                }
                else if (!flagSuper && block.type() == Type.SIGN
                    && block.strings() == Syntax.Chars.WORD_DIV_SIGN) {
                    flagSuper = true
                    lists.removeAt(index)
                }
                else if (flagSuper && superName == null
                    && (block.type() == Type.WORD || block.type() == Type.BRACKET)) {
                    superName = block
                    lists.removeAt(index)
                }
                else if (block.type() == Type.BRACKET
                    && block.strings() == Syntax.Chars.STATEMENT_BRACKET) {
                    statement = (block as Bracket)
                    lists.removeAt(index)
                    break
                }
                else {
                    break
                }
            }
            if (className == null || statement == null) throw Errors.Syntax(Errors.Key.SYNTAX, lineNO = lineNO)
        }

        fun setToReference(ref: References.Lists) {
            ref.appendClass(name(), this, references)
        }

        fun makeReference(hierarchy: Spaces.Names, parent: References.Lists?) {
            classNameSpace = Spaces.Names(hierarchy, className)
            references = References.Lists(classNameSpace!!, parent)
            statement?.lists()?.makeReference(references!!)
        }

        override fun dump(shift: String): String {
            var text = "$shift[class ${className?.text()} : ${superName?.text()} ]\n"
            statement?.let { text += it.dump("$shift  ") }
            text += "$shift[class end] \n"
            return text
        }

        override fun name() = Spaces.Names(className).text()
        fun classNameSpaces(): Spaces.Names? = classNameSpace

        fun superClassDef(): ClassDef? {
            var def: ClassDef? = null
            superName?.let {
                val names = Spaces.Names(it)
                val result = references?.searchIntoTrees(References.Type.CLASS, names)?.codes()
                if (result != null && result.type() == Type.CLASS_DEF)
                    def = result as ClassDef
            }
            return def
        }

        fun findMethod(word: String): FunDef? {
            val sets = references?.search(References.Type.FUN, word)
            return if (sets != null && sets.type() == References.Type.FUN)
                (sets.codes() as FunDef)
            else null
        }

        fun matchClass(dst: ClassDef) =
            (if (classNameSpace == null || dst.classNameSpace == null) false
             else classNameSpace!!.match(dst.classNameSpace!!))
    }

    class Returns() : Common(Type.RETURN, Syntax.Reserved.Word.RETURN, 0) {
        private var result: Common? = null

        fun result() = result

        constructor(lists: Lists, index: Int) : this() {
            val codeNO = Syntax.Reserved.codeNO(Syntax.Reserved.Key.RETURN)

            var block = lists.block(index)
            if (block == null || block.type() != Type.WORD || (block as Word).codeNO() != codeNO)
                throw Errors.Logic("CodeBlock.Returns")

            lists.removeAt(index)
            lineNO = block.lineNO()

            block = lists.block(index) ?: return
            if (block.type() == Type.BRACKET
                && (block as Bracket).strings() == Syntax.Chars.ARGUMENT_BRACKET) {
                result = block
                lists.removeAt(index)
            }
        }

        override fun dump(shift: String): String {
            var text = "$shift[return] =\n"
            result?.let { text += it.dump("$shift  ") }
            return text
        }
    }

    class Lists() {
        private var divide: String = ""
        private var list = mutableListOf<Common>()

        constructor(divide: String) : this() {
            this.divide = divide
        }

        fun lists() = list
        fun size() = list.size

        fun append(block: Common) { list.add(block) }
        fun removeAt(index: Int) { list.removeAt(index) }
        fun insert(index: Int, block: Common) { list.add(index, block) }

        fun block(index: Int): Common? =
            if (0 <= index && index < list.size) list[index] else null

        fun isCommaDivide(): Boolean = (divide == Syntax.Chars.COMMA)

        fun dump(shift: String): String {
            var text = "$shift[LIST IN:$divide] \n"
            list.forEach { set ->
                text += set.dump("$shift  ")
            }
            return text
        }

        fun forEach(process: (Common) -> Unit) {
            list.forEach { block -> process(block) }
        }

        fun findListAndRun(process: (Lists) -> Unit) {
            list.forEach { block ->
                if (block.type() == Type.BRACKET || block.type() == Type.LIST
                    || block.type() == Type.OBJECT_REF || block.type() == Type.FUN_CALL) {
                    block.findListAndRun(process)
                }
            }
            process(this)
        }

        fun makeReference(refs: References.Lists) {
            list.forEach { block ->
                when (block.type()) {
                    Type.CLASS_DEF -> {
                        val classes = block as ClassDef
                        classes.makeReference(refs.spaceName(), refs)
                        classes.setToReference(refs)
                    }
                    Type.FUN_DEF -> {
                        val funDef = block as FunDef
                        funDef.makeReference(refs.spaceName(), refs)
                        funDef.setToReference(refs)
                    }
                    Type.INIT_DEF -> {
                        (block as InitDef).setToReference(refs)
                    }
                    Type.VAR_DEF -> {
                        (block as VarDef).setToReference(refs)
                    }
                    Type.CONST_DEF -> {
                        (block as ConstDef).setToReference(refs)
                    }
                    else -> {}
                }
            }
        }

        fun buildCommaLists() {
            var pos: Int
            val commaPos = getCommaPos()
            if (commaPos.size < 1) return

            val newList = mutableListOf<Common>()
            var index = 0
            var comma = 0

            while (index < list.size) {
                pos = if (comma < commaPos.size) commaPos[comma] else list.size
                comma++

                if ((pos - index) > 1) {
                    val child = Lists("")
                    while (index < pos) {
                        child.append(list[index])
                        index++
                    }
                    val branch = Bracket("", child)
                    newList.add(branch)
                } else {
                    newList.add(list[index])
                }
                index = pos + 1
            }
            list = newList
            divide = ","
        }

        private fun getCommaPos(): MutableList<Int> {
            val result = mutableListOf<Int>()
            var index = 0
            while (index < list.size) {
                val block = list[index]
                if (block.type() == Type.SIGN && block.strings() == Syntax.Chars.COMMA) {
                    result.add(index)
                }
                index++
            }
            return result
        }
    }
}