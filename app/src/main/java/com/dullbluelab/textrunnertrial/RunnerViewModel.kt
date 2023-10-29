package com.dullbluelab.textrunnertrial

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.ViewModel
import com.dullbluelab.textrunnertrial.action.Drawing
import com.dullbluelab.textrunnertrial.action.Runner
import com.dullbluelab.textrunnertrial.logic.Parser
import com.dullbluelab.textrunnertrial.ui.Console
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.OffsetDateTime

class RunnerViewModel : ViewModel() {

    data class UiState(
        var sourceText: String = "",
        var codeText: String = "",
        var consoleText: String = "",

        var drawingQueue: Drawing.Lists = Drawing.Lists(),

        var timerLimit: Long = 0L,
        var loopLimit: Int = 0,
        var functionLimit: Int = 0
    )
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private var setting: Setting = Setting()
    fun setting() = setting

    class Status() {
        var canvasSize: Size = Size(0f, 0f)

        var runnerActive: Boolean = false
        var runningCount: Int = 0

        var flagTapAction: Boolean = false

        var timerCount: Long = 0
        var prevTimerTime: OffsetDateTime? = null

        var errorCount: Int = 0
        var functionCount: Int = 0
        var flagLoopBreak = false

        fun reset() {
            runnerActive = false
            runningCount = 0
            flagTapAction = false
            timerCount = 0
            prevTimerTime = null
            errorCount = 0
            functionCount = 0
            flagLoopBreak = false
        }
    }
    private val status = Status()
    fun status() = status

    var screenPosition: String = ""

    private val console = Console()
    fun console() = console

    private val runner = Runner(this)

    fun setup(activity: MainActivity) {
        setting.setup(activity)
        setting.load()
    }

    fun testSetup() {
        setting.initialize()
    }

    fun updateSourceText(text: String) {
        _uiState.update { state ->
            state.copy(
                sourceText = text
            )
        }
    }

    fun run() {
        var dumpText = ""
        try {
            status.reset()
            console.clear()
            val parser = Parser(this)
            val codes = parser.parse(_uiState.value.sourceText)
            dumpText = codes.dump()

            if (status.errorCount == 0) runner.run(codes)
        }
        catch (e: Exception) {
            error("${Errors.message(Errors.Key.SAFETY)} ${e.message}")
        }

        _uiState.update { state ->
            state.copy(
                codeText = console.textError + dumpText,
                consoleText = console.textAll
            )
        }
    }

    fun stop() {
        runner.stop()
    }

    fun restart() {
        runner.restart()
    }

    fun onTap(offset: Offset) {
        runner.onTap(offset)
        updateConsole()
    }

    fun canvasChanged() {
        runner.canvasChanged()
        updateConsole()
    }

    fun cancelTimer() {
        runner.cancelTimer()
    }

    fun enableAction(): Boolean =
        (status.runnerActive && status.runningCount == 0 && status.errorCount == 0
                && screenPosition == RunnerScreen.Drawing.name)

    fun enablePause(): Boolean =
        (status.errorCount == 0 && !status.runnerActive
                && screenPosition == RunnerScreen.Drawing.name && status().timerCount > 0)

    fun error(message: String) {
        console.appendError(message)
        status.errorCount ++
        status.runnerActive = false
    }

    fun error(key: Errors.Key, text: String) {
        val message = Errors.message(key) + text
        error(message)
    }

    fun clear() {
        console.clear()
        _uiState.update { state ->
            state.drawingQueue.clear()
            state.copy(
                sourceText = "",
                codeText = "",
                consoleText = ""
            )
        }
    }

    fun updateConsole() {
        _uiState.update { state ->
            state.copy(
                consoleText = console.textAll
            )
        }
    }

    fun updateDrawing(queue: Drawing.Lists) {
        _uiState.update { state ->
            state.copy(
                drawingQueue = queue
            )
        }
    }

    fun setupSettingValue() {
        _uiState.update { state ->
            state.copy(
                timerLimit = setting.timerLimit,
                loopLimit = setting.loopLimit,
                functionLimit = setting.functionLimit
            )
        }
    }

    fun updateTimerLimit(text: String) {
        val value = if (text.isDigitsOnly() && text.isNotEmpty()) text.toLong() else 0L
        _uiState.update { state ->
            state.copy(
                timerLimit = value
            )
        }
    }

    fun updateLoopLimit(text: String) {
        val value = if (text.isDigitsOnly() && text.isNotEmpty()) text.toInt() else 0
        _uiState.update { state ->
            state.copy(
                loopLimit = value
            )
        }
    }

    fun updateFuncLimit(text: String) {
        val value = if (text.isDigitsOnly() && text.isNotEmpty()) text.toInt() else 0
        _uiState.update { state ->
            state.copy(
                functionLimit = value
            )
        }
    }
    fun saveSettingValue() {
        setting.timerLimit = _uiState.value.timerLimit
        setting.loopLimit = _uiState.value.loopLimit
        setting.functionLimit = _uiState.value.functionLimit
        setting.safetyCheck()
        setting.save()
    }
}
