package com.example.textrunnertrial.logic

import com.example.textrunnertrial.RunnerViewModel
import com.example.textrunnertrial.ui.Console

private val ED = Syntax.Errors

class Parser(
    private val vm: RunnerViewModel
) {
    private val status: RunnerViewModel.Status = vm.status()
    private var console: Console = vm.console()

    fun parse(codeString: String): CodeUnit {
        val codes = CodeUnit()

        try {
            val blocks = makeCodeBlockList(TextBox(codeString), codes)
            if (status.errorCount > 0) return codes
            codes.setBlocks(blocks)

            buildBlock(codes.blocks())
            if (status.errorCount > 0) return codes

            codes.buildReference()
        }
        catch (e: Exception) {
            vm.error(Syntax.Errors.Key.SAFETY, " : ${e.message}")
        }
        return codes
    }

    private fun makeCodeBlockList(box: TextBox, codes: CodeUnit): CodeBlock.Lists {
        val blocks = CodeBlock.Lists("")
        var isBracketEnd = false

        while (!box.isEnd() && !isBracketEnd && status.errorCount == 0) {
            val startChar = box.getPos() ?: break

            if (box.matchComment()) {
                box.trimComment()
            }
            else if (box.matchLineComment()) {
                box.trimLineComment()
            }
            else if (Syntax.Chars.isMatch(startChar, Syntax.Chars.Type.WORD_START)) {
                blocks.append(CodeBlock.Word(box, codes.wordList()))
            }
            else if (Syntax.Chars.isMatch(startChar, Syntax.Chars.Type.NUMBER_START)) {
                blocks.append(CodeBlock.Number(box))
            }
            else if (Syntax.Chars.isMatch(startChar, Syntax.Chars.Type.SIGN)) {
                blocks.append(CodeBlock.Sign(box))
            }
            else if (Syntax.Chars.isMatch(startChar, Syntax.Chars.Type.BRACKET_START)) {
                val block = extractBracketSet(box, codes) ?: break
                blocks.append(block)
            }
            else if (Syntax.Chars.isMatch(startChar, Syntax.Chars.Type.BRACKET_END)) {
                isBracketEnd = true
            }
            else if (Syntax.Chars.isMatch(startChar, Syntax.Chars.Type.STRING_BRACKET)) {
                blocks.append(CodeBlock.Strings(box))
            }
            else {
                box.inc()
            }
        }
        return blocks
    }

    private fun extractBracketSet(box: TextBox, codes: CodeUnit): CodeBlock.Common? {
        val startBracket = box.getInc()
        val lineNO = box.lineNO

        val block: CodeBlock.Lists = makeCodeBlockList(box, codes)
        val endBracket = box.getInc()

        if (startBracket == null || endBracket == null) {
            vm.error(Syntax.Errors.Key.BRACKET, " line:$lineNO")
            return null
        }

        if (!Syntax.Chars.matchBracket(startBracket, endBracket)) {
            vm.error(Syntax.Errors.Key.NOT_MATCH_BRACKET, " $startBracket to $endBracket line:$lineNO")
            return null
        }
        val bracketChars = startBracket.toString() + endBracket.toString()
        return CodeBlock.Bracket(bracketChars, block, lineNO)
    }

    private fun buildBlock(lists: CodeBlock.Lists) {
        lists.findListAndRun { list -> list.buildCommaLists() }
        lists.findListAndRun { list -> makeFunCall(list) }
        lists.findListAndRun { list -> makeOperator(list, ".") }

        buildOperators(lists)

        makeReturnBlock(lists)
        makeConditionBlock(lists)
        makeDefinitionBlock(lists)
    }

    private fun buildOperators(lists: CodeBlock.Lists) {
        for (classNO in Syntax.Operator.MAKING_START .. Syntax.Operator.MAKING_END) {
            val defines = Syntax.Operator.getDefines(classNO)
            if (defines.size > 0) {
                lists.findListAndRun { list ->
                    makeOperator(list, defines.toList())
                }
            }
            if (status.errorCount > 0) break
        }
    }

    private fun makeFunCall(lists: CodeBlock.Lists) {
        var index = 1
        while (index < lists.size() && status.errorCount == 0) {
            val block = lists.block(index)
            val prev = lists.block(index - 1)

            if (block != null && prev != null) {
                if (block.type() == CodeBlock.Type.BRACKET) {
                    val bracket = block as CodeBlock.Bracket
                    if (bracket.chars() == Syntax.Chars.ARGUMENT_BRACKET
                        && prev.type() == CodeBlock.Type.WORD
                        && (prev as CodeBlock.Word).reservedKey() == Syntax.Reserved.Key.NON
                    ) {
                        index --
                        val newBlock = CodeBlock.FunCall(lists, index)
                        if (newBlock.errorText() != "") {
                            vm.error(newBlock.errorText())
                            return
                        }
                        lists.removeAt(index)
                        lists.removeAt(index)
                        lists.insert(index, newBlock)
                    }
                    else if (bracket.chars() == Syntax.Chars.LIST_DATA_BRACKET
                        && prev.type() == CodeBlock.Type.WORD
                        && (prev as CodeBlock.Word).reservedKey() == Syntax.Reserved.Key.LIST_OF
                        ) {
                        val newBlock = CodeBlock.ListValue(bracket)
                        if (newBlock.errorText() != "") {
                            vm.error(newBlock.errorText())
                            return
                        }
                        index --
                        lists.removeAt(index)
                        lists.removeAt(index)
                        lists.insert(index, newBlock)
                    }
                    else if (bracket.chars() == Syntax.Chars.LIST_INDEX_BRACKET) {
                        val dotBlock = CodeBlock.Sign(Syntax.Chars.DOT, bracket.lineNO())
                        val funBlock = CodeBlock.FunCall(Syntax.Method.Word.ITEM, bracket)
                        if (funBlock.errorText() != "") {
                            vm.error(funBlock.errorText())
                            return
                        }
                        lists.removeAt(index)
                        lists.insert(index, dotBlock)
                        index ++
                        lists.insert(index, funBlock)
                    }
                }
            }
            index++
        }
    }

    private fun makeOperator(lists: CodeBlock.Lists, sign: String) {
        val def = Syntax.Operator.setting(sign) ?: return
        var index = if (def.prev) 1 else 0
        var endPos = if (def.next) lists.size() - 1 else lists.size()
        while (index < endPos && status.errorCount == 0) {
            val block = lists.block(index) ?: break
            if (block.type() == CodeBlock.Type.SIGN && block.strings() == sign) {
                val newBlock = CodeBlock.ObjectRef(lists, index, def)
                if (newBlock.errorText() != "") {
                    vm.error(newBlock.errorText())
                    return
                }
                if (newBlock.child(CodeBlock.SUBJECT) != null) {
                    if (def.prev) {
                        index--
                        lists.removeAt(index)
                        endPos--
                    }
                    if (def.next) {
                        lists.removeAt(index + 1)
                        endPos--
                    }
                    lists.removeAt(index)
                    lists.insert(index, newBlock)
                }
            }
            index++
        }
    }

    private fun makeOperator(lists: CodeBlock.Lists, defines: List<Syntax.Operator.Setting>) {
        var index = 0
        while (index < lists.size() && status.errorCount == 0) {
            val block = lists.block(index) ?: break
            var def: Syntax.Operator.Setting? = null
            var newBlock: CodeBlock.Common? = null
            var dp = 0
            while (dp < defines.size && newBlock == null) {
                if (block.type() == CodeBlock.Type.SIGN && block.strings() == defines[dp].sign) {
                    def =
                        if (defines[dp].prev && index < 1) null
                        else if (defines[dp].next && (index + 1) >= lists.size()) null
                        else defines[dp]
                }
                if (def != null) {
                    newBlock = CodeBlock.ObjectRef(lists, index, def)
                    if (newBlock.errorText() != "") {
                        vm.error(newBlock.errorText())
                        return
                    }
                    if (newBlock.child(CodeBlock.SUBJECT) != null) {
                        if (def.prev) {
                            index--
                            lists.removeAt(index)
                        }
                        if (def.next) {
                            lists.removeAt(index + 1)
                        }
                        lists.removeAt(index)
                        lists.insert(index, newBlock)
                    }
                }
                dp++
            }
            index++
        }
    }

    private fun makeReturnBlock(lists: CodeBlock.Lists) {
        lists.findListAndRun { list ->
            var index = 0
            while (index < list.size() && status.errorCount == 0) {
                val block = list.block(index) ?: break

                if (block.type() == CodeBlock.Type.WORD
                    && block.strings() == Syntax.Reserved.Word.RETURN) {

                    val newBlock = CodeBlock.Returns(list, index)
                    if (newBlock.errorText() != "") {
                        vm.error(newBlock.errorText())
                        break
                    }
                    list.insert(index, newBlock)
                }
                index ++
            }
        }
    }

    private fun getConditionType(block: CodeBlock.Common): CodeBlock.Type? {
        var type: CodeBlock.Type? = null
        if (block.type() == CodeBlock.Type.WORD) {
            type = when (block.strings()) {
                Syntax.Reserved.Word.IF -> CodeBlock.Type.IF
                Syntax.Reserved.Word.WHILE -> CodeBlock.Type.WHILE
                Syntax.Reserved.Word.FOR -> CodeBlock.Type.FOR
                Syntax.Reserved.Word.WHEN -> CodeBlock.Type.WHEN
                else -> null
            }
        }
        return type
    }

    private fun makeConditionBlock(lists: CodeBlock.Lists) {
        lists.findListAndRun { list ->
            var index = 0
            while (index < list.size() && status.errorCount == 0) {
                val block = list.block(index) ?: break
                val type = getConditionType(block)

                if (type != null) {
                    val condition =
                        if (type == CodeBlock.Type.IF) CodeBlock.BlockIf(list, index)
                        else CodeBlock.Condition(list, index, type)

                    if (condition.errorText() != "") {
                        error(condition.errorText())
                    }
                    list.insert(index, condition)
                }
                index ++
            }
        }
    }

    private fun makeDefinitionBlock(lists: CodeBlock.Lists) {
        // var
        lists.findListAndRun { list ->
            val codeNO = Syntax.Reserved.codeNO(Syntax.Reserved.Key.VAR)
            makeDefSub(list, codeNO) { innerList, index ->
                CodeBlock.VarDef(innerList, index)
            }
        }
        // const
        lists.findListAndRun { list ->
            val codeNO = Syntax.Reserved.codeNO(Syntax.Reserved.Key.CONST)
            makeDefSub(list, codeNO) { innerList, index ->
                CodeBlock.ConstDef(innerList, index)
            }
        }
        // fun
        lists.findListAndRun { list ->
            val codeNO = Syntax.Reserved.codeNO(Syntax.Reserved.Key.FUN)
            makeDefSub(list, codeNO) { innerList, index ->
                CodeBlock.FunDef(innerList, index)
            }
        }
        // init
        lists.findListAndRun { list ->
            val codeNO = Syntax.Reserved.codeNO(Syntax.Reserved.Key.INIT)
            makeDefSub(list, codeNO) { innerList, index ->
                CodeBlock.InitDef(innerList, index)
            }
        }
        // class
        lists.findListAndRun { list ->
            val codeNO = Syntax.Reserved.codeNO(Syntax.Reserved.Key.CLASS)
            makeDefSub(list, codeNO) { innerList, index ->
                CodeBlock.ClassDef(innerList, index)
            }
        }
    }

    private fun makeDefSub(
        lists: CodeBlock.Lists, codeNO: Int,
        makeDef: (CodeBlock.Lists, Int) -> CodeBlock.Common
    ) {
        var index = 0
        while (index < lists.size() && status.errorCount == 0) {
            val block = lists.block(index) ?: break

            if (block.type() == CodeBlock.Type.WORD
                && (block as CodeBlock.Word).codeNO() == codeNO) {

                val varDef = makeDef(lists, index)
                if (varDef.errorText() != "") {
                    vm.error(varDef.errorText())
                    break
                }
                lists.insert(index, varDef)
            }
            index ++
        }
    }

    class TextBox(private val text: String) {
        private var position: Int = 0
        private var prevPosition: Int = (-1)
        var lineNO: Int = 1

        companion object {
            const val RETURN = '\n'
        }

        fun getPos(): Char? {
            if (position >= text.length) return null
            val char = text[position]
            if (char == RETURN && position != prevPosition) lineNO ++
            prevPosition = position
            return char
        }

        fun getNext(): Char? {
            position++
            if (position >= text.length) return null
            val char = text[position]
            if (char == RETURN && position != prevPosition) lineNO ++
            prevPosition = position
            return char
        }

        fun getInc(): Char? {
            if (position >= text.length) return null
            val char = text[position]
            if (char == RETURN && position != prevPosition) lineNO ++
            prevPosition = position
            position++
            return char
        }

        fun isEnd(): Boolean {
            return (position >= text.length)
        }

        fun inc() {
            position++
        }

        fun matchComment(): Boolean = Syntax.Chars.matchComment(text, position)

        fun trimComment() {
            position += 2
            while (position < text.length) {
                if (Syntax.Chars.matchCommentEnd(text, position)) break
                if (text[position] == RETURN) lineNO ++
                position ++
            }
            position += 2
        }

        fun matchLineComment(): Boolean = Syntax.Chars.matchLineComment(text, position)

        fun trimLineComment() {
            position += 2
            while (position < text.length) {
                if (text[position] == RETURN) break
                position ++
            }
            lineNO ++
            position ++
        }
    }
}
