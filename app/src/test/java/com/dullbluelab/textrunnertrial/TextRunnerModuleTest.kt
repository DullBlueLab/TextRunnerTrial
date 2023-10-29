package com.dullbluelab.textrunnertrial

import org.junit.Test
import org.junit.Assert.*

private const val SOURCE_HELLO = "\$m.print(\"Hello !\")"
private const val RESULT_HELLO = "Hello !\n"

private const val SOURCE_CONDITION =
    "var rd x y tx ty : Double\n" +
            "var flag : Boolean\n" +
            "rd = 200\n" +
            "x = 500\n" +
            "y = 700\n" +
            "tx = 470\n" +
            "ty = 780\n" +
            "if (((x - rd) <= tx && tx <= (x + rd))\n" +
            "  && ((y - rd) <= ty && ty <= (y + rd)))\n" +
            "  flag = true\n" +
            "else flag = false\n" +
            "\$m.print(flag)"
private const val RESULT_CONDITION = "true\n"

private const val SOURCE_WHILE =
    "var cnt : Int \n" +
            "cnt = 0 \n" +
            "while (cnt < 500) { \n" +
            "  cnt ++ \n" +
            "} \n" +
            "\$m.print(cnt) \n"
private const val RESULT_WHILE = "500\n"

private const val SOURCE_FOR =
    "var cnt : Int \n" +
            "var text : String \n" +
            "for (cnt = 10 : cnt > 0 : cnt -= 2) { \n" +
            " text += \"Hi! \" \n" +
            "} \n" +
            "\$m.print(text) \n"
private const val RESULT_FOR = "Hi! Hi! Hi! Hi! Hi! \n"

private const val SOURCE_MAIN_CLASS =
    "class Main { \n" +
            "  var text : String \n" +
            "  init() { \n" +
            "    text = \"Hello !\" \n" +
            "  } \n" +
            "  fun run() { \n" +
            "    \$m.print(text) \n" +
            "  } \n" +
            "} \n"
private const val RESULT_MAIN_CLASS = "Hello !\n"

private const val SOURCE_RETURN =
    "fun check(value: Int): String {" +
            "  if ((value % 2) == 1) return(\"odd\")\n" +
            "  return(\"even\") \n" +
            "}" +
            "\$m.print(check(11)) \n" +
            "\$m.print(check(8)) \n"
private const val RESULT_RETURN = "odd\neven\n"

private const val SOURCE_RETURN_2 =
    "fun check(): Int {" +
            "  var cnt: Int" +
            "  cnt = 0" +
            "  while (cnt < 100) {" +
            "    if (cnt >= 10) return(cnt)" +
            "    cnt ++" +
            "  }" +
            "  return(cnt)" +
            "}" +
            "\$m.print(check()) \n"
private const val RESULT_RETURN_2 = "10\n"

private const val SOURCE_RETURN_3 =
    "fun check(): Int {" +
            "  var cnt: Int " +
            "  for (cnt = 0 : cnt < 100 : cnt ++) { " +
            "    if (cnt >= 15) return(cnt) " +
            "  }" +
            "  return(cnt) " +
            "}" +
            "\$m.print(check()) \n"
private const val RESULT_RETURN_3 = "15\n"

private const val SOURCE_OPERATION =
    "class To { " +
            "  var text : String " +
            "  fun \$add(dst: String) {" +
            "    return (text + \" to \" + dst) " +
            "  }" +
            "}" +
            "var tos : To " +
            "tos.text = \"Face\" " +
            "\$m.print(tos + \"face\") \n"
private const val RESULT_OPERATION = "Face to face\n"

private const val SOURCE_BREAK =
    "fun check(): Int {" +
            "  var cnt: Int" +
            "  cnt = 0" +
            "  while (cnt < 100) {" +
            "    if (cnt >= 10) break" +
            "    cnt ++" +
            "  }" +
            "  return(cnt)" +
            "}" +
            "\$m.print(check()) \n"
private const val RESULT_BREAK = "10\n"

private const val SOURCE_BREAK_2 =
    "fun check(): Int {" +
            "  var cnt: Int " +
            "  for (cnt = 0 : cnt < 100 : cnt ++) { " +
            "    if (cnt >= 15) break " +
            "  }" +
            "  return(cnt) " +
            "}" +
            "\$m.print(check()) \n"
private const val RESULT_BREAK_2 = "15\n"

private const val SOURCE_CLASS =
    "class Boss { " +
            "  const text = \"Sum\" " +
            "}" +
            "class Parent : Boss {" +
            "  fun print() {" +
            "    \$m.print(text)" +
            "  }" +
            "}" +
            "class Child: Parent {" +
            "  var text : String" +
            "  init() {" +
            "    text = \"Mary\" " +
            "  }" +
            "  fun printChild() {" +
            "    var text : String " +
            "    text = \"Jane\"" +
            "    \$m.print(text)" +
            "  }" +
            "  fun printThis() {" +
            "    \$m.print(this.text)" +
            "  }" +
            "  fun printParent() {" +
            "    super.print()" +
            "  }" +
            "}" +
            "var obj : Child()" +
            "obj.printChild()" +
            "obj.printThis()" +
            "obj.printParent()" +
            ""
private const val RESULT_CLASS = "Jane\nMary\nSum\n"

private const val SOURCE_LIST =
    "var box1 box2 : List " +
            "box1 = listOf(\"aaa\", \"bbb\", \"ccc\") " +
            "box1 += listOf(\"ddd\", \"eee\", \"fff\") " +
            "box2.addList(listOf(\"AAA\", \"BBB\", \"CCC\")) " +
            "box2.addList(listOf(\"DDD\", \"EEE\", \"FFF\")) " +
            "\$m.print(box1[4]) " +
            "box2[1].removeAt(1) " +
            "\$m.print(box2[1][1]) "
private const val RESULT_LIST = "eee\nFFF\n"

class TextRunnerModuleTest {

    @Test
    fun testHello() {
        val viewModel = RunnerViewModel()
        viewModel.testSetup()

        viewModel.uiState.value.sourceText = SOURCE_HELLO
        viewModel.run()
        val result = viewModel.uiState.value.consoleText
        assertEquals(result, RESULT_HELLO)
    }

    @Test
    fun testCondition() {
        val viewModel = RunnerViewModel()
        viewModel.testSetup()

        viewModel.uiState.value.sourceText = SOURCE_CONDITION
        viewModel.run()
        val result = viewModel.uiState.value.consoleText
        assertEquals(result, RESULT_CONDITION)
    }

    @Test
    fun testWhile() {
        val viewModel = RunnerViewModel()
        viewModel.testSetup()

        viewModel.uiState.value.sourceText = SOURCE_WHILE
        viewModel.run()
        val result = viewModel.uiState.value.consoleText
        assertEquals(result, RESULT_WHILE)
    }

    @Test
    fun testFor() {
        val viewModel = RunnerViewModel()
        viewModel.testSetup()

        viewModel.uiState.value.sourceText = SOURCE_FOR
        viewModel.run()
        val result = viewModel.uiState.value.consoleText
        assertEquals(result, RESULT_FOR)
    }

    @Test
    fun testMainClass() {
        val viewModel = RunnerViewModel()
        viewModel.testSetup()

        viewModel.uiState.value.sourceText = SOURCE_MAIN_CLASS
        viewModel.run()
        val result = viewModel.uiState.value.consoleText
        assertEquals(result, RESULT_MAIN_CLASS)
    }

    @Test
    fun testReturn() {
        val viewModel = RunnerViewModel()
        viewModel.testSetup()

        viewModel.uiState.value.sourceText = SOURCE_RETURN
        viewModel.run()
        val result = viewModel.uiState.value.consoleText
        assertEquals(result, RESULT_RETURN)
    }

    @Test
    fun testReturn2() {
        val viewModel = RunnerViewModel()
        viewModel.testSetup()

        viewModel.uiState.value.sourceText = SOURCE_RETURN_2
        viewModel.run()
        val result = viewModel.uiState.value.consoleText
        assertEquals(result, RESULT_RETURN_2)
    }

    @Test
    fun testReturn3() {
        val viewModel = RunnerViewModel()
        viewModel.testSetup()

        viewModel.uiState.value.sourceText = SOURCE_RETURN_3
        viewModel.run()
        val result = viewModel.uiState.value.consoleText
        assertEquals(result, RESULT_RETURN_3)
    }

    @Test
    fun testOperation() {
        val viewModel = RunnerViewModel()
        viewModel.testSetup()

        viewModel.uiState.value.sourceText = SOURCE_OPERATION
        viewModel.run()
        val result = viewModel.uiState.value.consoleText
        assertEquals(result, RESULT_OPERATION)
    }

    @Test
    fun testLoopBreak() {
        val viewModel = RunnerViewModel()
        viewModel.testSetup()

        viewModel.uiState.value.sourceText = SOURCE_BREAK
        viewModel.run()
        val result = viewModel.uiState.value.consoleText
        assertEquals(result, RESULT_BREAK)
    }

    @Test
    fun testLoopBreak2() {
        val viewModel = RunnerViewModel()
        viewModel.testSetup()

        viewModel.uiState.value.sourceText = SOURCE_BREAK_2
        viewModel.run()
        val result = viewModel.uiState.value.consoleText
        assertEquals(result, RESULT_BREAK_2)
    }

    @Test
    fun testClass() {
        val viewModel = RunnerViewModel()
        viewModel.testSetup()

        viewModel.uiState.value.sourceText = SOURCE_CLASS
        viewModel.run()
        val result = viewModel.uiState.value.consoleText
        assertEquals(result, RESULT_CLASS)
    }

    @Test
    fun testList() {
        val viewModel = RunnerViewModel()
        viewModel.testSetup()

        viewModel.uiState.value.sourceText = SOURCE_LIST
        viewModel.run()
        val result = viewModel.uiState.value.consoleText
        assertEquals(result, RESULT_LIST)
    }
}
