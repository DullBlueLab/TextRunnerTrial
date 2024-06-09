package com.dullbluelab.textrunnertrial.logic

import com.dullbluelab.textrunnertrial.Errors
import com.dullbluelab.textrunnertrial.RunnerViewModel
import com.dullbluelab.textrunnertrial.block.*

class Parser(
    private val vm: RunnerViewModel
) {
    private val status: RunnerViewModel.Status = vm.status()

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
        catch (e: Errors.Syntax) {
            vm.error(e.message)
        }
        catch (e: Exception) {
            vm.error(e.message ?: Errors.message(Errors.Key.SAFETY))
        }
        return codes
    }

    private fun makeCodeBlockList(box: TextBox, codes: CodeUnit): BlockLists {
        val blocks = BlockLists("")
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
                blocks.append(BlockWord(box, codes.wordList()))
            }
            else if (Syntax.Chars.isMatch(startChar, Syntax.Chars.Type.NUMBER_START)) {
                blocks.append(BlockNumber(box))
            }
            else if (Syntax.Chars.isMatch(startChar, Syntax.Chars.Type.SIGN)) {
                blocks.append(BlockSign(box))
            }
            else if (Syntax.Chars.isMatch(startChar, Syntax.Chars.Type.BRACKET_START)) {
                val block = extractBracketSet(box, codes)
                blocks.append(block)
            }
            else if (Syntax.Chars.isMatch(startChar, Syntax.Chars.Type.BRACKET_END)) {
                isBracketEnd = true
            }
            else if (Syntax.Chars.isMatch(startChar, Syntax.Chars.Type.STRING_BRACKET)) {
                blocks.append(BlockStrings(box))
            }
            else {
                box.inc()
            }
        }
        return blocks
    }

    private fun extractBracketSet(box: TextBox, codes: CodeUnit): CodeBlock {
        val startBracket = box.getInc()
        val lineNO = box.lineNO

        val block: BlockLists = makeCodeBlockList(box, codes)
        val endBracket = box.getInc()

        if (startBracket == null || endBracket == null) throw Errors.Syntax(Errors.Key.BRACKET, lineNO = lineNO)

        if (!Syntax.Chars.matchBracket(startBracket, endBracket))
            throw Errors.Syntax("${Errors.message(Errors.Key.NOT_MATCH_BRACKET)} $startBracket to $endBracket", lineNO)

        val bracketChars = startBracket.toString() + endBracket.toString()
        return BlockBracket(bracketChars, block, lineNO)
    }

    private fun buildBlock(lists: BlockLists) {
        lists.findBracketAndRun(Syntax.Chars.COMMA_BRACKET) { list -> list.buildCommaLists() }
        lists.findListAndRun { list -> makeFunCall(list) }
        lists.findListAndRun { list -> makeOperator(list, ".") }

        buildOperators(lists)

        makeReturnBlock(lists)
        makeConditionBlock(lists)
        makeDefinitionBlock(lists)
    }

    private fun buildOperators(lists: BlockLists) {
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

    private fun makeFunCall(lists: BlockLists) {
        var index = 1
        while (index < lists.size() && status.errorCount == 0) {
            val block = lists.block(index)
            val prev = lists.block(index - 1)

            if (block != null && prev != null) {
                if (block.type() == CodeBlock.Type.BRACKET) {
                    val bracket = block as BlockBracket
                    if (bracket.chars() == Syntax.Chars.ARGUMENT_BRACKET
                        && prev.type() == CodeBlock.Type.WORD
                        && (prev as BlockWord).reservedKey() == Syntax.Reserved.Key.NON
                    ) {
                        index --
                        val newBlock = BlockFunCall(lists, index)
                        lists.removeAt(index)
                        lists.removeAt(index)
                        lists.insert(index, newBlock)
                    }
                    else if (bracket.chars() == Syntax.Chars.LIST_DATA_BRACKET
                        && prev.type() == CodeBlock.Type.WORD
                        && (prev as BlockWord).reservedKey() == Syntax.Reserved.Key.LIST_OF
                        ) {
                        val newBlock = BlockListValue(bracket)

                        index --
                        lists.removeAt(index)
                        lists.removeAt(index)
                        lists.insert(index, newBlock)
                    }
                    else if (bracket.chars() == Syntax.Chars.LIST_INDEX_BRACKET) {
                        val dotBlock = BlockSign(Syntax.Chars.DOT, bracket.lineNO())
                        val funBlock = BlockFunCall(Syntax.Method.Word.ITEM, bracket)
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

    private fun makeOperator(lists: BlockLists, sign: String) {
        val def = Syntax.Operator.setting(sign) ?: return
        var index = if (def.prev) 1 else 0
        var endPos = if (def.next) lists.size() - 1 else lists.size()
        while (index < endPos && status.errorCount == 0) {
            val block = lists.block(index) ?: break
            if (block.type() == CodeBlock.Type.SIGN && block.strings() == sign) {
                val newBlock = BlockObjectRef(lists, index, def)
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

    private fun makeOperator(lists: BlockLists, defines: List<Syntax.Operator.Setting>) {
        var index = 0
        while (index < lists.size() && status.errorCount == 0) {
            val block = lists.block(index) ?: break
            var def: Syntax.Operator.Setting? = null
            var newBlock: CodeBlock? = null
            var dp = 0
            while (dp < defines.size && newBlock == null) {
                if (block.type() == CodeBlock.Type.SIGN && block.strings() == defines[dp].sign) {
                    def =
                        if (defines[dp].prev && index < 1) null
                        else if (defines[dp].next && (index + 1) >= lists.size()) null
                        else defines[dp]
                }
                if (def != null) {
                    newBlock = BlockObjectRef(lists, index, def)
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

    private fun makeReturnBlock(lists: BlockLists) {
        lists.findListAndRun { list ->
            var index = 0
            while (index < list.size() && status.errorCount == 0) {
                val block = list.block(index) ?: break

                if (block.type() == CodeBlock.Type.WORD
                    && block.strings() == Syntax.Reserved.Word.RETURN) {

                    val newBlock = BlockReturns(list, index)
                    list.insert(index, newBlock)
                }
                index ++
            }
        }
    }

    private fun getConditionType(block: CodeBlock): CodeBlock.Type? {
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

    private fun makeConditionBlock(lists: BlockLists) {
        lists.findListAndRun { list ->
            var index = 0
            while (index < list.size() && status.errorCount == 0) {
                val block = list.block(index) ?: break
                val type = getConditionType(block)

                if (type != null) {
                    val condition = when (type) {
                        CodeBlock.Type.IF -> BlockIf(list, index)
                        CodeBlock.Type.WHEN -> BlockWhen(list, index)
                        else -> BlockCondition(list, index, type)
                    }

                    list.insert(index, condition)
                }
                index ++
            }
        }
    }

    private fun makeDefinitionBlock(lists: BlockLists) {
        // var
        lists.findListAndRun { list ->
            val codeNO = Syntax.Reserved.codeNO(Syntax.Reserved.Key.VAR)
            makeDefSub(list, codeNO) { innerList, index ->
                BlockVarDef(innerList, index)
            }
        }
        // const
        lists.findListAndRun { list ->
            val codeNO = Syntax.Reserved.codeNO(Syntax.Reserved.Key.CONST)
            makeDefSub(list, codeNO) { innerList, index ->
                BlockConstDef(innerList, index)
            }
        }
        // fun
        lists.findListAndRun { list ->
            val codeNO = Syntax.Reserved.codeNO(Syntax.Reserved.Key.FUN)
            makeDefSub(list, codeNO) { innerList, index ->
                BlockFunDef(innerList, index)
            }
        }
        // init
        lists.findListAndRun { list ->
            val codeNO = Syntax.Reserved.codeNO(Syntax.Reserved.Key.INIT)
            makeDefSub(list, codeNO) { innerList, index ->
                BlockInitDef(innerList, index)
            }
        }
        // class
        lists.findListAndRun { list ->
            val codeNO = Syntax.Reserved.codeNO(Syntax.Reserved.Key.CLASS)
            makeDefSub(list, codeNO) { innerList, index ->
                BlockClassDef(innerList, index)
            }
        }
    }

    private fun makeDefSub(
        lists: BlockLists, codeNO: Int,
        makeDef: (BlockLists, Int) -> CodeBlock
    ) {
        var index = 0
        while (index < lists.size() && status.errorCount == 0) {
            val block = lists.block(index) ?: break

            if (block.type() == CodeBlock.Type.WORD
                && (block as BlockWord).codeNO() == codeNO) {

                try {
                    val varDef = makeDef(lists, index)
                    lists.insert(index, varDef)
                }
                catch (e: Errors.Syntax) {
                    val number = if (e.lineNO == 0) block.lineNO() else e.lineNO
                    throw Errors.Syntax(e.threwMessage(), number)
                }
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
