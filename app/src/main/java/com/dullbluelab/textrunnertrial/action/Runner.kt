package com.dullbluelab.textrunnertrial.action

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.ui.geometry.Offset
import com.dullbluelab.textrunnertrial.RunnerViewModel
import com.dullbluelab.textrunnertrial.logic.CodeBlock
import com.dullbluelab.textrunnertrial.logic.CodeUnit
import com.dullbluelab.textrunnertrial.logic.References
import com.dullbluelab.textrunnertrial.logic.Syntax
import com.dullbluelab.textrunnertrial.Errors
import java.time.OffsetDateTime
import java.util.Timer
import java.time.temporal.ChronoUnit
import java.util.TimerTask

private const val WORD_MAIN_CLASS = "Main"
private const val WORD_MAIN_VAR = "main"
private const val WORD_RUN_METHOD = "run"
private const val WORD_TIMER_METHOD = "timer"
private const val WORD_TAP_METHOD = "onTap"
private const val WORD_CANVAS_CHANGED = "canvasChanged"

private val RW = Syntax.Reserved.Word
private val voids = Objects.Voids()

class Runner(
    private var vm: RunnerViewModel
) {
    private var codes: CodeUnit? = null
    private val spaces: Spaces = Spaces()
    private var machine = Machine(vm)
    private var status = vm.status()

    private var mainObject: Objects.Common? = null

    private var timer: Timer? = null

    fun run(codes: CodeUnit) {
        try {
            mainObject = null
            timer = null
            this.codes = codes
            spaces.setup(codes.reference())

            status.runningCount = 1
            executeLists(codes.blocks())

            if (status.errorCount == 0) {
                executeMain()
                status.runnerActive = true
            }
            status.runningCount = 0

            if (mainObject != null && status.timerCount > 0 && status.errorCount == 0) {
                setTimerTask(status.timerCount)
            }
        }
        catch (e: Errors.Syntax) {
            vm.error(e.message)
        }
        catch (e: Exception) {
            vm.error(Errors.Key.SAFETY, " ${e.message} Runner.run")
        }
        if (status.errorCount > 0) stop()
    }

    fun stop() {
        cancelTimer()
        status.runnerActive = false
    }

    fun restart() {
        try {
            mainObject?.let {
                status.runnerActive = true
                if (status.timerCount > 0) {
                    setTimerTask(status.timerCount)
                }
            }
        }
        catch (e: Errors.Syntax) {
            vm.error(e.message)
        }
        catch (e: Exception) {
            vm.error(Errors.Key.SAFETY, " ${e.message} Runner.run")
        }
        if (status.errorCount > 0) stop()
    }

    fun onTap(offset: Offset) {
        try {
            mainObject?.let {
                val main = it as Objects.Instances

                if (main.searchFunDef(WORD_TAP_METHOD) == null) {
                    return
                }
                status.runningCount++

                val args: MutableList<Objects.Common> = mutableListOf()
                args.add(Objects.Doubles(offset.x.toDouble()))
                args.add(Objects.Doubles(offset.y.toDouble()))

                executeMethodCall(main, WORD_TAP_METHOD, args)
                status.runningCount--
            }
        }
        catch (e: Errors.Syntax) {
            vm.error(e.message)
        }
        catch(e: Exception) {
            vm.error(Errors.Key.SAFETY, " ${e.message} Runner.onTap")
        }
        if (status.errorCount > 0) stop()
        return
    }

    fun canvasChanged() {
        try {
            mainObject?.let {
                val main = it as Objects.Instances

                if (main.searchFunDef(WORD_CANVAS_CHANGED) == null) {
                    return
                }
                status.runningCount++

                val args: MutableList<Objects.Common> = mutableListOf()
                executeMethodCall(main, WORD_CANVAS_CHANGED, args)
                status.runningCount--
            }
        }
        catch (e: Errors.Syntax) {
            vm.error(e.message)
        }
         catch (e: Exception) {
             vm.error(Errors.Key.SAFETY, " ${e.message} Runner.canvasChanged")
        }
        if (status.errorCount > 0) stop()
    }

    private fun executeMain() {
        val args: MutableList<Objects.Common> = mutableListOf()

        if (spaces.findClassDef(WORD_MAIN_CLASS) != null) {
            mainObject = spaces.createVarObject(
                WORD_MAIN_VAR, Spaces.Names(WORD_MAIN_CLASS), isMember = false
            )
        }

        if (mainObject?.type() == Objects.Type.INSTANCE) {
            val main = mainObject as Objects.Instances
            makeObjectMember(main)

            // find and call init method
            val initFun = main.searchFunDef(RW.INIT)
            if (initFun != null) {
                executeMethodCall(main, RW.INIT, args)
            }
            // find and call run method
            val runFun = main.searchFunDef(WORD_RUN_METHOD)
            if (runFun != null) {
                executeMethodCall(main, WORD_RUN_METHOD, args)
            }
        }
        // nothing class Main
        else {
            mainObject = null
        }
        return
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun executeTimerMethod() {
        try {
            if (mainObject != null && status.runningCount == 0) {
                status.runningCount++
                val now = OffsetDateTime.now()
                val interval = status.prevTimerTime?.until(now, ChronoUnit.MILLIS)?.toInt() ?: 0

                val args: MutableList<Objects.Common> = mutableListOf()
                args.add(Objects.Ints(interval))
                executeMethodCall(mainObject!!, WORD_TIMER_METHOD, args)

                status.prevTimerTime = now
                status.runningCount--
            }
        }
        catch (e: Errors.Syntax) {
            vm.error(e.message)
        }
        catch (e: Exception) {
            vm.error(Errors.Key.SAFETY, " ${e.message} Runner.executeTimerMethod")
        }
        vm.updateConsole()
        if (status.errorCount > 0) stop()
        return
    }

    private fun executeLists(blocks: CodeBlock.Lists): Objects.Common {
        var result: Objects.Common = Objects.Voids()

        for (block in blocks.lists()) {
            result = executeBlock(block)
            if (spaces.returns.isReturn() || status.errorCount > 0 || status.flagLoopBreak) break
        }
        return result
    }

    private fun executeBlock(block: CodeBlock.Common) : Objects.Common {
        val result = when (block.type()) {
            CodeBlock.Type.WORD -> {
                if ((block as CodeBlock.Word).isReservedKey())
                    getReservedValue(block)
                else
                    spaces.getValue((block).name())
            }
            CodeBlock.Type.STRING -> {
                Objects.Strings(block as CodeBlock.Strings)
            }
            CodeBlock.Type.NUMBER -> {
                if (block.strings().contains(Syntax.Chars.DOT))
                    Objects.Doubles(block as CodeBlock.Number)
                else
                    Objects.Ints(block as CodeBlock.Number)
            }
            CodeBlock.Type.LIST -> {
                makeListValue(block as CodeBlock.ListValue)
            }
            CodeBlock.Type.VAR_DEF -> {
                makeVarObject(block as CodeBlock.VarDef, isMembers = false)
            }
            CodeBlock.Type.CONST_DEF -> {
                makeConstObject(block as CodeBlock.ConstDef, isMembers = false)
            }
            CodeBlock.Type.BRACKET -> {
                executeBracket(block)
            }
            CodeBlock.Type.OBJECT_REF -> {
                executeObjectRef(block)
            }
            CodeBlock.Type.FUN_CALL -> {
                executeFunCall(block as CodeBlock.FunCall)
            }
            CodeBlock.Type.IF -> {
                executeIf(block as CodeBlock.BlockIf)
            }
            CodeBlock.Type.WHILE -> {
                executeWhile(block as CodeBlock.Condition)
            }
            CodeBlock.Type.FOR -> {
                executeFor(block as CodeBlock.Condition)
            }
            CodeBlock.Type.WHEN -> {
                executeWhen(block as CodeBlock.BlockWhen)
            }
            CodeBlock.Type.RETURN -> {
                executeReturn(block as CodeBlock.Returns)
            }
            else -> {
                Objects.Voids()
            }
        }
        return result ?: Objects.Voids()
    }

    private fun getReservedValue(block: CodeBlock.Word): Objects.Common? {
        val result = when(block.reservedKey()) {
            Syntax.Reserved.Key.TRUE, Syntax.Reserved.Key.FALSE -> Objects.Booleans(block)
            Syntax.Reserved.Key.THIS -> spaces.thisObject()
            Syntax.Reserved.Key.SUPER -> spaces.thisObject()?.supers()
            Syntax.Reserved.Key.BREAK -> {
                status.flagLoopBreak = true
                Objects.Voids()
            }
            else -> null
        }
        return result
    }

    private fun makeVarObject(block: CodeBlock.VarDef, isMembers: Boolean): Objects.Common {
        var objects: Objects.Common? = null
        val names = block.nameList()

        val method = Syntax.Reserved.word(Syntax.Reserved.Key.INIT)
        val className = block.className()
        if (method == null || className == null) throw Errors.Logic("Runner.makeVarObject")

        var arguments: MutableList<Objects.Common>? = null
        block.classArgument()?.let { arguments = makeArgumentList(it) }
        if (status.errorCount > 0) return voids

        for (name in names) {
            objects = spaces.createVarObject(name, className, isMembers)
            if (objects.type() == Objects.Type.ERRORS) break

            if (objects.type() == Objects.Type.INSTANCE)
                makeObjectMember(objects as Objects.Instances)

            if (arguments != null) {
                executeMethodCall(objects, method, arguments!!)
            }
        }
        return objects ?: voids
    }

    private fun makeObjectMember(objects: Objects.Instances) {
        val classDef: CodeBlock.ClassDef = objects.classDef()
        spaces.updateThisObject(objects)

        val refs: References.Lists? = classDef.reference()
        refs?.forEach { sets ->
            val block = sets.codes()

            when (block?.type()) {
                CodeBlock.Type.VAR_DEF -> {
                    makeVarObject(block as CodeBlock.VarDef, isMembers = true)
                }
                CodeBlock.Type.CONST_DEF -> {
                    makeConstObject(block as CodeBlock.ConstDef, isMembers = true)
                }
                else -> {}
            }
        }
        val superDef = classDef.superClassDef()
        if (superDef != null) {
            val supers = Objects.Instances(superDef)
            makeObjectMember(supers)
            objects.setSupers(supers)
        }
        spaces.returnThisObject()
    }

    private fun makeConstObject(block: CodeBlock.ConstDef, isMembers: Boolean): Objects.Common {
        val value = block.value()
        if (value?.type() == CodeBlock.Type.LIST) {
            val listValue = value as CodeBlock.ListValue
            val valueList = makeArgumentList(listValue.values())
            if (status.errorCount == 0) listValue.setObjects(valueList)
            else return voids
        }
        return spaces.createConst(block, isMembers)
    }

    private fun executeBracket(block: CodeBlock.Common) : Objects.Common {
        val bracket = block as CodeBlock.Bracket
        val lists = bracket.lists()
        var result: Objects.Common? = null

        if (lists != null) {
            result = executeLists(lists)
        }
        return result ?: voids
    }

    private fun makeListValue(block: CodeBlock.ListValue) : Objects.Common {
        val values = makeArgumentList(block.values())
        return Objects.Lists(values)
    }

    private fun executeObjectRef(block: CodeBlock.Common) : Objects.Common {
        var result: Objects.Common? = null
        val subject = block.child(CodeBlock.SUBJECT)
        val argument = block.child(CodeBlock.ARGUMENT)

        if (subject == null || argument == null) throw Errors.Logic("executeObjectRef")

        if (subject.type() == CodeBlock.Type.WORD && subject.name() == Machine.NAME) {
            result = executeMachineMethod(argument)
        }
        else {
            val objects  = executeBlock(subject)

            when (argument.type()) {
                CodeBlock.Type.WORD -> {
                    if (objects.type() == Objects.Type.INSTANCE) {
                        val instance = objects as Objects.Instances
                        result = instance.searchObjects(argument.name()) ?: Objects.Voids()
                    }
                }
                CodeBlock.Type.FUN_CALL -> {
                    if (objects.type() == Objects.Type.ERRORS || objects.type() == Objects.Type.VOIDS)
                        throw Errors.Syntax(Errors.Key.UNKNOWN, subject.text())

                    val objectArg = makeArgumentList(argument.child(CodeBlock.ARGUMENT))
                    val method = (argument as CodeBlock.FunCall).name()
                    if (status.errorCount == 0)
                        result = executeMethodCall(objects, method, objectArg)
                }
                else -> {}
            }
        }
        return result ?: throw Errors.Syntax(Errors.Key.SYNTAX)
    }

    private fun executeMachineMethod(block: CodeBlock.Common) : Objects.Common {
        var result: Objects.Common? = null

        if (block.type() == CodeBlock.Type.FUN_CALL) {
            val arguments = makeArgumentList(block.child(CodeBlock.ARGUMENT))
            val methods = block.name()

            if (status.errorCount == 0)
                result = machine.execute(methods, arguments)
        }
        return result ?: throw Errors.Logic("Runner.executeMachineMethod")
    }

    private fun executeMethodCall(
        objects: Objects.Common, method: String, arguments: MutableList<Objects.Common>)
            : Objects.Common {
        val result: Objects.Common?

        if (objects.type() == Objects.Type.INSTANCE) {
            val instance = objects as Objects.Instances
            val funDef = objects.searchFunDef(method)

            if (funDef != null) {
                spaces.updateThisObject(instance)
                result = funCall(funDef, arguments)
                spaces.returnThisObject()
            }
            else {
                result = instance.execute(method, arguments)
            }
        }
        else {
            result = objects.execute(method, arguments)
        }
        return result ?: throw Errors.Logic("Runner.executeMethodCall")
    }

    private fun executeFunCall(block: CodeBlock.FunCall) : Objects.Common? {
        var result: Objects.Common? = null
        var funDef: CodeBlock.FunDef? = null
        val argument = block.child(CodeBlock.ARGUMENT)

        val objectArg = makeArgumentList(argument)
        if (status.errorCount > 0) return null

        val method = block.name()
        val instance = spaces.thisObject()

        if (instance != null) {
            funDef = instance.searchFunDef(method)
                ?: throw Errors.Syntax(Errors.Key.UNKNOWN, ": ${instance.name()} $method", block.lineNO())

            result = funCall(funDef, objectArg)
        }
        if (funDef == null) {
            val match = codes?.reference()?.search(References.Type.FUN, method)?.codes()

            if ((match != null) && (match.type() == CodeBlock.Type.FUN_DEF)) {
                spaces.groundFlag = true
                result = funCall(match as CodeBlock.FunDef, objectArg)
                spaces.groundFlag = false
            }
            else throw Errors.Syntax(Errors.Key.UNKNOWN, ": $method", block.lineNO())
        }
        return result
    }

    private fun funCall(
        def: CodeBlock.FunDef, objectArg: MutableList<Objects.Common>): Objects.Common {

        status.functionCount ++
        if (status.functionCount > vm.setting.value.functionLimit) {
            throw Errors.Syntax(Errors.Key.LIMIT_FUNC, lineNO = def.lineNO())
        }

        val lists = def.statement()?.lists() ?: throw Errors.Logic("Runner.funCall")

        spaces.updateFunVar(def, objectArg)
        var result = executeLists(lists)

        spaces.returns.objects()?.let {
            result = it
            spaces.returns.clear()
        }
        spaces.returnFunVar()
        status.functionCount --

        return result
    }

    private fun executeIf(block: CodeBlock.BlockIf): Objects.Common {
        var result: Objects.Common? = null
        var condition: Objects.Common? = null

        block.conditions()?.let { condition = executeBlock(it) }
        if (condition != null && condition!!.type() == Objects.Type.BOOLEANS) {

            if ((condition as Objects.Booleans).valueBoolean())
                block.statementTrue()?.let { result = executeBlock(it) }
            else
                block.statementElse()?.let { result = executeBlock(it) }
        }
        else throw Errors.Syntax(Errors.Key.ILLEGAL_CONDITION, " if", block.lineNO())

        return result ?: voids
    }

    private fun executeWhile(block: CodeBlock.Condition): Objects.Common {
        var result: Objects.Common? = null
        var loopCount = 0

        val condition = block.conditions()
        val statement = block.statement()
        if (condition == null || statement == null) throw Errors.Logic("Runner.executeWhile")

        var loops = executeBlock(condition)

        while ((loops.type() == Objects.Type.BOOLEANS)
            && ((loops as Objects.Booleans).valueBoolean()) ) {

            loopCount ++
            if (loopCount > vm.setting.value.loopLimit) throw Errors.Syntax(Errors.Key.LIMIT_OVER, " while", block.lineNO())

            result = executeBlock(statement)
            if (result.typeError()) return result
            if (spaces.returns.isReturn() || status.errorCount > 0 || status.flagLoopBreak) break

            loops = executeBlock(condition)
        }
        status.flagLoopBreak = false
        return result ?: voids
    }

    private fun executeFor(block: CodeBlock.Condition): Objects.Common {
        var result: Objects.Common?
        val statement = block.statement() ?: throw Errors.Syntax(Errors.Key.UNKNOWN, " for", block.lineNO())
        var loopFlag = true
        var loopCount = 0

        val initial = block.initial()
        val condition = block.conditions()
        val countUp = block.countUp()
        if (initial == null || condition == null || countUp == null)
            throw Errors.Syntax(Errors.Key.ILLEGAL_CONDITION, " for", block.lineNO())

        result = executeBlock(initial)
        if (result.typeError()) return result

        while (loopFlag) {
            loopCount ++
            if (loopCount > vm.setting.value.loopLimit)
                throw Errors.Syntax(Errors.Key.LIMIT_OVER, " for", block.lineNO())

            val flag = executeBlock(condition)
            if (flag.typeError()) return flag
            if (flag.type() != Objects.Type.BOOLEANS)
                throw Errors.Syntax(Errors.Key.ILLEGAL_CONDITION, " for", block.lineNO())

            if (!(flag as Objects.Booleans).valueBoolean()) {
                loopFlag = false
            }
            else {
                result = executeBlock(statement)
                if (result.typeError()) return result
                if (spaces.returns.isReturn() || status.errorCount > 0 || status.flagLoopBreak) break

                val obj = executeBlock(countUp)
                if (obj.typeError()) return obj
            }
        }
        status.flagLoopBreak = false
        return result ?: voids
    }

    private fun executeWhen(block: CodeBlock.BlockWhen): Objects.Common {
        val method = Syntax.Method.Word.EQUAL
        var arguments: MutableList<Objects.Common>
        var match = false
        var result: Objects.Common? = null
        var objects: Objects.Common

        val subject = if (block.subject != null) executeBlock(block.subject!!) else null

        for (item in block.itemList) {

            for (argument in item.arguments) {
                if (subject == null) {
                    result = executeBlock(argument)
                }
                else {
                    objects = executeBlock(argument)
                    arguments = mutableListOf(objects)
                    result = executeMethodCall(subject, method, arguments)
                }
                if (result.type() == Objects.Type.BOOLEANS
                    && (result as Objects.Booleans).valueBoolean()) {
                    match = true
                    break
                }
            }
            if (match) {
                result = executeBlock(item.statement)
                break
            }
        }
        if (!match && block.elseStatement != null) {
            result = executeBlock(block.elseStatement!!)
            match = true
        }
        return if (match && result != null) result
        else throw Errors.Syntax(Errors.Key.NOT_MATCH_TYPE, "when", block.lineNO())
    }

    private fun executeReturn(block: CodeBlock.Returns): Objects.Common {
        var result: Objects.Common? = null

        block.result()?.let { result = executeBlock(it) }
        if (result == null) result = voids
        spaces.returns.set(result)

        return result as Objects.Common
    }

    private fun makeArgumentList(block: CodeBlock.Common?): MutableList<Objects.Common> {
        val lists = mutableListOf<Objects.Common>()

        if (block != null) {
            if (block.type() == CodeBlock.Type.BRACKET) {

                val arguments = (block as CodeBlock.Bracket).lists()?.lists()
                arguments?.let { args ->
                    for (child in args) {
                        val objects = executeBlock(child)
                        if (objects.type() == Objects.Type.VOIDS)
                            throw Errors.Syntax(Errors.Key.ILLEGAL_ARGUMENT, child.text(), block.lineNO())

                        lists.add(objects)
                    }
                }
            }
            else {
                val objects = executeBlock(block)
                if (objects.type() == Objects.Type.VOIDS)
                    throw Errors.Syntax(Errors.Key.ILLEGAL_ARGUMENT, block.text(), block.lineNO())

                lists.add(objects)
            }
        }
        return lists
    }

    private fun setTimerTask(count: Long) {
        timer = Timer()
        timer!!.scheduleAtFixedRate( RunnerTimerTask(this), count, count)
    }

    private class RunnerTimerTask(val parent: Runner): TimerTask() {
        @RequiresApi(Build.VERSION_CODES.O)
        override fun run() {
            if (parent.status.runningCount > 0) return
            parent.executeTimerMethod()
        }
    }

    fun cancelTimer() {
        timer?.cancel()
        timer = null
    }
}
