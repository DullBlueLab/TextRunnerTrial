package com.dullbluelab.textrunnertrial.action

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.ui.geometry.Offset
import com.dullbluelab.textrunnertrial.RunnerViewModel
import com.dullbluelab.textrunnertrial.logic.CodeUnit
import com.dullbluelab.textrunnertrial.logic.References
import com.dullbluelab.textrunnertrial.logic.Syntax
import com.dullbluelab.textrunnertrial.Errors
import com.dullbluelab.textrunnertrial.block.*
import com.dullbluelab.textrunnertrial.data.LibraryRepository
import com.dullbluelab.textrunnertrial.objects.*
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
private val voids = RunVoid()

class Runner(
    private var vm: RunnerViewModel,
    private val repositories: LibraryRepository
) {
    private var codes: CodeUnit? = null
    private val spaces: Spaces = Spaces(repositories)
    private var machine = Machine(vm)
    private var status = vm.status()

    private var mainObject:RunObject? = null

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
                val main = it as RunInstance

                if (main.searchFunDef(WORD_TAP_METHOD) == null) {
                    return
                }
                status.runningCount++

                val args: MutableList<RunObject> = mutableListOf()
                args.add(RunDouble(offset.x.toDouble()))
                args.add(RunDouble(offset.y.toDouble()))

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
                val main = it as RunInstance

                if (main.searchFunDef(WORD_CANVAS_CHANGED) == null) {
                    return
                }
                status.runningCount++

                val args: MutableList<RunObject> = mutableListOf()
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
        val args: MutableList<RunObject> = mutableListOf()

        if (spaces.findClassDef(WORD_MAIN_CLASS) != null) {
            mainObject = spaces.createVarObject(
                WORD_MAIN_VAR, Spaces.Names(WORD_MAIN_CLASS), isMember = false
            )
        }

        if (mainObject?.type == RunObject.Type.INSTANCE) {
            val main = mainObject as RunInstance
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

                val args: MutableList<RunObject> = mutableListOf()
                args.add(RunInt(interval))
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

    private fun executeLists(blocks: BlockLists):RunObject {
        var result:RunObject = RunVoid()

        for (block in blocks.lists()) {
            result = executeBlock(block)
            if (spaces.returns.isReturn() || status.errorCount > 0 || status.flagLoopBreak) break
        }
        return result
    }

    private fun executeBlock(block: CodeBlock) :RunObject {
        val result = when (block.type()) {
            CodeBlock.Type.WORD -> {
                if ((block as BlockWord).isReservedKey())
                    getReservedValue(block)
                else
                    spaces.getValue((block).name())
            }
            CodeBlock.Type.STRING -> {
                RunString(block as BlockStrings)
            }
            CodeBlock.Type.NUMBER -> {
                if (block.strings().contains(Syntax.Chars.DOT))
                    RunDouble(block as BlockNumber)
                else
                    RunInt(block as BlockNumber)
            }
            CodeBlock.Type.LIST -> {
                makeListValue(block as BlockListValue)
            }
            CodeBlock.Type.VAR_DEF -> {
                makeVarObject(block as BlockVarDef, isMembers = false)
            }
            CodeBlock.Type.CONST_DEF -> {
                makeConstObject(block as BlockConstDef, isMembers = false)
            }
            CodeBlock.Type.BRACKET -> {
                executeBracket(block)
            }
            CodeBlock.Type.OBJECT_REF -> {
                executeObjectRef(block)
            }
            CodeBlock.Type.FUN_CALL -> {
                executeFunCall(block as BlockFunCall)
            }
            CodeBlock.Type.IF -> {
                executeIf(block as BlockIf)
            }
            CodeBlock.Type.WHILE -> {
                executeWhile(block as BlockCondition)
            }
            CodeBlock.Type.FOR -> {
                executeFor(block as BlockCondition)
            }
            CodeBlock.Type.WHEN -> {
                executeWhen(block as BlockWhen)
            }
            CodeBlock.Type.RETURN -> {
                executeReturn(block as BlockReturns)
            }
            else -> {
                RunVoid()
            }
        }
        return result ?: RunVoid()
    }

    private fun getReservedValue(block: BlockWord):RunObject? {
        val result = when(block.reservedKey()) {
            Syntax.Reserved.Key.TRUE, Syntax.Reserved.Key.FALSE -> RunBoolean(block)
            Syntax.Reserved.Key.THIS -> spaces.thisObject()
            Syntax.Reserved.Key.SUPER -> spaces.thisObject()?.supers
            Syntax.Reserved.Key.BREAK -> {
                status.flagLoopBreak = true
                RunVoid()
            }
            else -> null
        }
        return result
    }

    private fun makeVarObject(block: BlockVarDef, isMembers: Boolean):RunObject {
        var objects:RunObject? = null
        val names = block.nameList()

        val method = Syntax.Reserved.word(Syntax.Reserved.Key.INIT)
        val className = block.className()
        if (method == null || className == null) throw Errors.Logic("Runner.makeVarObject")

        var arguments: MutableList<RunObject>? = null
        block.classArgument()?.let { arguments = makeArgumentList(it) }
        if (status.errorCount > 0) return voids

        for (name in names) {
            objects = spaces.createVarObject(name, className, isMembers)
            if (objects.type == RunObject.Type.ERRORS) break

            if (objects.type == RunObject.Type.INSTANCE)
                makeObjectMember(objects as RunInstance)

            if (arguments != null) {
                executeMethodCall(objects, method, arguments!!)

                if (objects.type == RunObject.Type.IMAGES) {
                    (objects as RunImage).load(repositories)
                }
            }
        }
        return objects ?: voids
    }

    private fun makeObjectMember(objects: RunInstance) {
        val classDef: BlockClassDef = objects.classDef()
        spaces.updateThisObject(objects)

        val refs: References.Lists? = classDef.reference()
        refs?.forEach { sets ->
            val block = sets.codes()

            when (block?.type()) {
                CodeBlock.Type.VAR_DEF -> {
                    makeVarObject(block as BlockVarDef, isMembers = true)
                }
                CodeBlock.Type.CONST_DEF -> {
                    makeConstObject(block as BlockConstDef, isMembers = true)
                }
                else -> {}
            }
        }
        val superDef = classDef.superClassDef()
        if (superDef != null) {
            val supers = RunInstance(superDef)
            makeObjectMember(supers)
            objects.setSuper(supers)
        }
        spaces.returnThisObject()
    }

    private fun makeConstObject(block: BlockConstDef, isMembers: Boolean):RunObject {
        val value = block.value()
        if (value?.type() == CodeBlock.Type.LIST) {
            val listValue = value as BlockListValue
            val valueList = makeArgumentList(listValue.values())
            if (status.errorCount == 0) listValue.setObjects(valueList)
            else return voids
        }
        return spaces.createConst(block, isMembers)
    }

    private fun executeBracket(block: CodeBlock) :RunObject {
        val bracket = block as BlockBracket
        val lists = bracket.lists()
        var result:RunObject? = null

        if (lists != null) {
            result = executeLists(lists)
        }
        return result ?: voids
    }

    private fun makeListValue(block: BlockListValue) :RunObject {
        val values = makeArgumentList(block.values())
        return RunList(values)
    }

    private fun executeObjectRef(block: CodeBlock) :RunObject {
        var result:RunObject? = null
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
                    if (objects.type == RunObject.Type.INSTANCE) {
                        val instance = objects as RunInstance
                        result = instance.searchObjects(argument.name()) ?: RunVoid()
                    }
                }
                CodeBlock.Type.FUN_CALL -> {
                    if (objects.type == RunObject.Type.ERRORS || objects.type == RunObject.Type.VOIDS)
                        throw Errors.Syntax(Errors.Key.UNKNOWN, subject.text())

                    val objectArg = makeArgumentList(argument.child(CodeBlock.ARGUMENT))
                    val method = (argument as BlockFunCall).name()
                    if (status.errorCount == 0)
                        result = executeMethodCall(objects, method, objectArg)
                }
                else -> {}
            }
        }
        return result ?: throw Errors.Syntax(Errors.Key.SYNTAX)
    }

    private fun executeMachineMethod(block: CodeBlock) :RunObject {
        var result:RunObject? = null

        if (block.type() == CodeBlock.Type.FUN_CALL) {
            val arguments = makeArgumentList(block.child(CodeBlock.ARGUMENT))
            val methods = block.name()

            if (status.errorCount == 0)
                result = machine.execute(methods, arguments)
        }
        return result ?: throw Errors.Logic("Runner.executeMachineMethod")
    }

    private fun executeMethodCall(
        objects:RunObject, method: String, arguments: MutableList<RunObject>)
            :RunObject {
        val result:RunObject?

        if (objects.type == RunObject.Type.INSTANCE) {
            val instance = objects as RunInstance
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

    private fun executeFunCall(block: BlockFunCall) :RunObject? {
        var result:RunObject? = null
        var funDef: BlockFunDef? = null
        val argument = block.child(CodeBlock.ARGUMENT)

        val objectArg = makeArgumentList(argument)
        if (status.errorCount > 0) return null

        val method = block.name()
        val instance = spaces.thisObject()

        if (instance != null) {
            funDef = instance.searchFunDef(method)
                ?: throw Errors.Syntax(Errors.Key.UNKNOWN, ": ${instance.classNames()} $method", block.lineNO())

            result = funCall(funDef, objectArg)
        }
        if (funDef == null) {
            val match = codes?.reference()?.search(References.Type.FUN, method)?.codes()

            if ((match != null) && (match.type() == CodeBlock.Type.FUN_DEF)) {
                spaces.groundFlag = true
                result = funCall(match as BlockFunDef, objectArg)
                spaces.groundFlag = false
            }
            else throw Errors.Syntax(Errors.Key.UNKNOWN, ": $method", block.lineNO())
        }
        return result
    }

    private fun funCall(
        def: BlockFunDef, objectArg: MutableList<RunObject>):RunObject {

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

    private fun executeIf(block: BlockIf):RunObject {
        var result:RunObject? = null
        var condition:RunObject? = null

        block.conditions()?.let { condition = executeBlock(it) }
        if (condition != null && condition!!.type == RunObject.Type.BOOLEANS) {

            if ((condition as RunBoolean).valueBoolean())
                block.statementTrue()?.let { result = executeBlock(it) }
            else
                block.statementElse()?.let { result = executeBlock(it) }
        }
        else throw Errors.Syntax(Errors.Key.ILLEGAL_CONDITION, " if", block.lineNO())

        return result ?: voids
    }

    private fun executeWhile(block: BlockCondition):RunObject {
        var result:RunObject? = null
        var loopCount = 0

        val condition = block.conditions()
        val statement = block.statement()
        if (condition == null || statement == null) throw Errors.Logic("Runner.executeWhile")

        var loops = executeBlock(condition)

        while ((loops.type == RunObject.Type.BOOLEANS)
            && ((loops as RunBoolean).valueBoolean()) ) {

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

    private fun executeFor(block: BlockCondition):RunObject {
        var result:RunObject?
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
            if (flag.type != RunObject.Type.BOOLEANS)
                throw Errors.Syntax(Errors.Key.ILLEGAL_CONDITION, " for", block.lineNO())

            if (!(flag as RunBoolean).valueBoolean()) {
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

    private fun executeWhen(block: BlockWhen):RunObject {
        val method = Syntax.Method.Word.EQUAL
        var arguments: MutableList<RunObject>
        var match = false
        var result:RunObject? = null
        var objects:RunObject

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
                if (result.type == RunObject.Type.BOOLEANS
                    && (result as RunBoolean).valueBoolean()) {
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

    private fun executeReturn(block: BlockReturns):RunObject {
        var result:RunObject? = null

        block.result()?.let { result = executeBlock(it) }
        if (result == null) result = voids
        spaces.returns.set(result)

        return result as RunObject
    }

    private fun makeArgumentList(block: CodeBlock?): MutableList<RunObject> {
        val lists = mutableListOf<RunObject>()

        if (block != null) {
            if (block.type() == CodeBlock.Type.BRACKET) {

                val arguments = (block as BlockBracket).lists()?.lists()
                arguments?.let { args ->
                    for (child in args) {
                        val objects = executeBlock(child)
                        if (objects.type == RunObject.Type.VOIDS)
                            throw Errors.Syntax(Errors.Key.ILLEGAL_ARGUMENT, child.text(), block.lineNO())

                        lists.add(objects)
                    }
                }
            }
            else {
                val objects = executeBlock(block)
                if (objects.type == RunObject.Type.VOIDS)
                    throw Errors.Syntax(Errors.Key.ILLEGAL_ARGUMENT, block.text(), block.lineNO())

                lists.add(objects)
            }
        }
        return lists
    }

    private fun setTimerTask(count: Long) {
        timer = Timer()
        timer!!.scheduleAtFixedRate( RunnerTimerTask(this), count, count)
        //timer?.schedule(RunnerTimerTask(this), count)
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
