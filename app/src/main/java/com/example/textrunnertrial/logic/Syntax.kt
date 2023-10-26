package com.example.textrunnertrial.logic

object Syntax {

    object Chars {
        enum class Type {
            WORD_START, WORDS, NUMBER_START, NUMBERS, SIGN,
            BRACKET_START, BRACKET_END, STRING_BRACKET,
            ESCAPE, COMMA, NON
        }
        private val map = mapOf<Type, String>(
            Type.WORD_START to "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ$#_",
            Type.WORDS to "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ$#_0123456789",
            Type.NUMBER_START to "0123456789",
            Type.NUMBERS to "0123456789.",
            Type.SIGN to "+-*/!%&=~|@\\.,:<>?",
            Type.BRACKET_START to "({[",
            Type.BRACKET_END to ")}]",
            Type.STRING_BRACKET to "\"\'",
            Type.ESCAPE to "\\",
            Type.COMMA to ","
        )

        const val ARGUMENT_BRACKET = "()"
        const val STATEMENT_BRACKET = "{}"
        const val LIST_DATA_BRACKET = "()"
        const val LIST_INDEX_BRACKET = "[]"

        val LIST_DATA_BRACKET_START = LIST_DATA_BRACKET[0]
        val LIST_DATA_BRACKET_END = LIST_DATA_BRACKET[1]
        val LIST_INDEX_BRACKET_START = LIST_INDEX_BRACKET[0]
        val LIST_INDEX_BRACKET_END = LIST_INDEX_BRACKET[1]

        const val WORD_DIV_SIGN = ":"
        const val COMMA = ","
        const val DOT = "."
        val OBJECT_REF = Operator.setting(Operator.Type.DOT)?.sign ?: "."
        const val FOR_BRANCH = ":"

        const val COMMENT_START = "/*"
        const val COMMENT_END = "*/"
        const val LINE_COMMENT = "//"

        fun isMatch(char: Char, type:Type) : Boolean {
            val strings = map[type] ?: ""
            return strings.contains(char)
        }

        fun matchComment(text: String, index: Int): Boolean {
            val endIndex = index + COMMENT_START.length
            return if (endIndex <= text.length)
                (text.substring(index, endIndex) == COMMENT_START)
            else false
        }

        fun matchCommentEnd(text: String, index: Int): Boolean {
            val endIndex = index + COMMENT_END.length
            return if (endIndex <= text.length)
                (text.substring(index, endIndex) == COMMENT_END)
            else false
        }

        fun matchLineComment(text: String, index: Int): Boolean {
            val endIndex = index + LINE_COMMENT.length
            return if (endIndex <= text.length)
                (text.substring(index, endIndex) == LINE_COMMENT)
            else false
        }

        fun matchBracket(start: Char, end: Char): Boolean {
            val startSet = map[Type.BRACKET_START]
            val endSet = map[Type.BRACKET_END]

            return if (startSet != null && endSet != null)
                (startSet.indexOf(start) == endSet.indexOf(end))
            else false
        }

        fun getEscapeChar(char: Char) : Char {
            val result = when (char) {
                'r' -> '\r'
                'n' -> '\n'
                't' -> '\t'
                else -> char
            }
            return result
        }
    }

    object Reserved {
        enum class Key {
            NON, NULL,
            CLASS, FUN, INIT, SUPER, THIS, RETURN,
            VAR, CONST,
            IF, WHILE, FOR, ELSE, WHEN, TRUE, FALSE, BREAK,
            INT, DOUBLE, STRING, BOOLEAN, LIST, LIST_OF
        }

        object Word {
            const val NULL = "null"
            const val CLASS = "class"
            const val FUN = "fun"
            const val SUPER = "super"
            const val THIS = "this"
            const val RETURN = "return"
            const val INIT = "init"
            const val VAR = "var"
            const val CONST = "const"
            const val IF = "if"
            const val FOR = "for"
            const val WHILE = "while"
            const val ELSE = "else"
            const val WHEN = "when"
            const val TRUE = "true"
            const val FALSE = "false"
            const val BREAK = "break"
            const val INT = "Int"
            const val DOUBLE = "Double"
            const val STRING = "String"
            const val BOOLEAN = "Boolean"
            const val VOID = "Void"
            const val LIST = "List"
            const val LIST_OF = "listOf"
        }

        data class Sets(
            val key: Key,
            val word: String,
            val codeNO: Int,
            val isValues: Boolean,
        )

        private val lists = listOf<Sets>(
            Sets(Key.NULL,    Word.NULL,    (-10000),  false),
            Sets(Key.CLASS,   Word.CLASS,   (-10001),  false),
            Sets(Key.FUN,     Word.FUN,     (-10002),  false),
            Sets(Key.SUPER,   Word.SUPER,   (-10003),  false),
            Sets(Key.THIS,    Word.THIS,    (-10004),  false),
            Sets(Key.RETURN,  Word.RETURN,  (-10005),  false),
            Sets(Key.INIT,    Word.INIT,    (-10006),  false),
            Sets(Key.VAR,     Word.VAR,     (-10011),  false),
            Sets(Key.CONST,   Word.CONST,   (-10012),  false),
            Sets(Key.IF,      Word.IF,      (-10021),  false),
            Sets(Key.WHILE,   Word.WHILE,   (-10022),  false),
            Sets(Key.FOR,     Word.FOR,     (-10023),  false),
            Sets(Key.ELSE,    Word.ELSE,    (-10024),  false),
            Sets(Key.WHEN,    Word.WHEN,    (-10025),  false),
            Sets(Key.TRUE,    Word.TRUE,    (-10026),  true ),
            Sets(Key.FALSE,   Word.FALSE,   (-10027),  true ),
            Sets(Key.BREAK,   Word.BREAK,   (-10028),  true ),
            Sets(Key.INT,     Word.INT,     (-10031),  true ),
            Sets(Key.DOUBLE,  Word.DOUBLE,  (-10032),  true ),
            Sets(Key.STRING,  Word.STRING,  (-10033),  true ),
            Sets(Key.BOOLEAN, Word.BOOLEAN, (-10034),  true ),
            Sets(Key.LIST,    Word.LIST,    (-10035),  false),
            Sets(Key.LIST_OF, Word.LIST_OF, (-10036),  false),
        )

        fun key(word: String): Key {
            var key: Key = Key.NON
            for (sets in lists) {
                if (sets.word == word) {
                    key = sets.key
                    break
                }
            }
            return key
        }

        fun word(key: Key): String? {
            var word: String? = null
            for (sets in lists) {
                if (sets.key == key) {
                    word = sets.word
                    break
                }
            }
            return word
        }

        fun codeNO(word: String): Int {
            var code: Int = 0
            for (sets in lists) {
                if (sets.word == word) {
                    code = sets.codeNO
                    break
                }
            }
            return code
        }

        fun codeNO(key: Key): Int {
            var code = 0
            for (sets in lists) {
                if (sets.key == key) {
                    code = sets.codeNO
                    break
                }
            }
            return code
        }
    }

    object Operator {
        enum class Type {
            DOT,
            INC_FRONT, INC_REAR, DEC_FRONT, DEC_REAR,
            NOT,
            ADD, SUB, MULTI, DIV, MOD,
            PLUS, MINUS,
            MORE_SMALL, MORE_LARGE,
            SMALL, LARGE,
            EQUAL, NOT_EQUAL,
            AND, OR,
            NULL_IS,
            SET,  ADD_SET, SUB_SET,
        }

        data class Setting(
            val sign: String,
            val method: String,
            val prev: Boolean,
            val next: Boolean,
            val classNO: Int,
        )

        private val defines = mapOf<Type, Setting>(
            Type.DOT        to Setting(".",  "",             true,  true,  1),
            Type.INC_FRONT  to Setting("++", Method.Word.INC_FRONT,  true,  false, 3),
            Type.INC_REAR   to Setting("++", Method.Word.INC_REAR,   false, true,  3),
            Type.DEC_FRONT  to Setting("--", Method.Word.DEC_FRONT,  true,  false, 3),
            Type.DEC_REAR   to Setting("--", Method.Word.DEC_REAR,   false, true,  3),
            Type.NOT        to Setting("!",  Method.Word.NOT,        false, true,  5),
            Type.ADD        to Setting("+",  Method.Word.ADD,        true,  true,  9),
            Type.SUB        to Setting("-",  Method.Word.SUB,        true,  true,  9),
            Type.MULTI      to Setting("*",  Method.Word.MULTI,      true,  true,  7),
            Type.DIV        to Setting("/",  Method.Word.DIV,        true,  true,  7),
            Type.MOD        to Setting("%",  Method.Word.MOD,        true,  true,  7),
            //Type.PLUS       to Setting("+",  Method.Word.PLUS,       false, true,  11),
            Type.MINUS      to Setting("-",  Method.Word.MINUS,      false, true,  11),
            Type.MORE_SMALL to Setting("<",  Method.Word.MORE_SMALL, true,  true,  13),
            Type.MORE_LARGE to Setting(">",  Method.Word.MORE_LARGE, true,  true,  13),
            Type.SMALL      to Setting("<=", Method.Word.SMALL,      true,  true,  13),
            Type.LARGE      to Setting(">=", Method.Word.LARGE,      true,  true,  13),
            Type.EQUAL      to Setting("==", Method.Word.EQUAL,      true,  true,  13),
            Type.NOT_EQUAL  to Setting("!=", Method.Word.NOT_EQUAL,  true,  true,  13),
            Type.AND        to Setting("&&", Method.Word.AND,        true,  true,  15),
            Type.OR         to Setting("||", Method.Word.OR,         true,  true,  15),
            Type.NULL_IS    to Setting("?:", Method.Word.NULL_IS,    true,  true,  17),
            Type.SET        to Setting("=",  Method.Word.SET,        true,  true,  19),
            Type.ADD_SET    to Setting("+=", Method.Word.ADD_SET,    true,  true,  19),
            Type.SUB_SET    to Setting("-=", Method.Word.SUB_SET,    true,  true,  19),
        )

        const val MAKING_START = 3
        const val MAKING_END = 19

        fun getDefines(classNO: Int): MutableList<Setting> {
            val matches = mutableListOf<Setting>()
            defines.forEach { (_, set) ->
                if (set.classNO == classNO) matches.add(set)
            }
            return matches
        }

        fun setting(type: Type): Setting? = defines[type]

        fun setting(sign: String): Setting? {
            defines.forEach { (_, set) ->
                if (set.sign == sign) return set
            }
            return null
        }

        fun key(sign: String): Type? {
            for (sets in defines) {
                if (sets.value.sign == sign) return sets.key
            }
            return null
        }

        fun word(type: Type): String = setting(type)?.method ?: ""
    }

    object Method {

        object Word {
            const val INC_FRONT = "\$incFront"
            const val INC_REAR = "\$incRear"
            const val DEC_FRONT = "\$decFront"
            const val DEC_REAR = "\$decRear"
            const val NOT = "\$not"
            const val ADD = "\$add"
            const val SUB = "\$sub"
            const val MULTI = "\$multi"
            const val DIV = "\$div"
            const val MOD = "\$mod"
            //const val PLUS        = "\$plus"
            const val MINUS = "\$minus"
            const val MORE_SMALL = "\$moreSmall"
            const val MORE_LARGE = "\$moreLarge"
            const val SMALL = "\$small"
            const val LARGE = "\$large"
            const val EQUAL = "\$equal"
            const val NOT_EQUAL = "\$notEqual"
            const val AND = "\$and"
            const val OR = "\$or"
            const val NULL_IS = "\$nullIs"
            const val SET = "\$set"
            const val ADD_SET = "\$addSet"
            const val SUB_SET = "\$subSet"

            const val ITEM = "item"
            const val SIZE = "size"
            const val LENGTH = "length"
            const val CLEAR = "clear"
            const val TO_STRING = "toString"
            const val TO_INT = "toInt"
            const val TO_DOUBLE = "toDouble"
            const val TYPE = "type"
            const val APPEND = "append"
            const val REMOVE_AT = "removeAt"
            const val ADD_LIST = "addList"

            const val MAXES = "max"
            const val MINES = "min"

            const val CHAR_AT = "charAt"
            const val CONTAINS = "contains"
            const val INDEX_OF = "indexOf"
            const val SUBSTRING = "substring"
            const val LOWERCASE = "lowercase"
            const val UPPERCASE = "uppercase"

            const val PRINT = "print"
            const val CANVAS_WIDTH = "canvasWidth"
            const val CANVAS_HEIGHT = "canvasHeight"
            const val DRAW_CIRCLE = "drawCircle"
            const val DRAW_LINE = "drawLine"
            const val DRAW_RECT = "drawRect"
            const val DRAW_UP = "drawUp"
            const val NEW_DRAWING = "newDrawing"
            const val CHANGE_COLOR = "setColor"
            const val CHANGE_STROKE = "setStroke"
            const val CHANGE_ALPHA = "setAlpha"
            const val FILL_CANVAS = "fillCanvas"
            const val RANDOM = "random"
            const val PI = "pi"
            const val SET_TIMER = "setTimer"
            const val CANCEL_TIMER = "cancelTimer"
            const val TAP_ACTION = "tapAction"

            const val ABS = "abs"
            const val ACOS = "acos"
            const val ASIN = "asin"
            const val ATAN = "atan"
            const val COS = "cos"
            const val SIN = "sin"
            const val TAN = "tan"
            const val CEIL = "ceil"
            const val FLOOR = "floor"
            const val ROUND = "round"
            const val SQRT = "sqrt"
            const val TO_DEGREES = "toDegrees"
            const val TO_RADIANS = "toRadians"
            const val POW = "pow"
            const val EXP = "exp"
            const val LOG = "log"
        }
    }
}