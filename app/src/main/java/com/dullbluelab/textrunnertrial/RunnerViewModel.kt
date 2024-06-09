package com.dullbluelab.textrunnertrial

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dullbluelab.textrunnertrial.action.Drawing
import com.dullbluelab.textrunnertrial.action.Runner
import com.dullbluelab.textrunnertrial.data.LibraryRepository
import com.dullbluelab.textrunnertrial.data.SettingItem
import com.dullbluelab.textrunnertrial.data.UserPreferencesRepository
import com.dullbluelab.textrunnertrial.logic.Parser
import com.dullbluelab.textrunnertrial.ui.Console
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.OffsetDateTime

class RunnerViewModel(
    private val preferences: UserPreferencesRepository,
    repositories: LibraryRepository
) : ViewModel() {

    data class UiState(
        var sourceText: String = "",
        var codeText: String = "",
        var consoleText: String = "",

        var drawingQueue: Drawing.Lists = Drawing.Lists(),

        var timerLimit: String = "",
        var loopLimit: String = "",
        var functionLimit: String = "",

        var flagGuideDialog: Boolean = false,
        var flagClearDataDialog: Boolean = false
    )
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    val setting: StateFlow<SettingItem> = preferences.setting
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = SettingItem()
        )

    class Status {
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

    private val runner = Runner(this, repositories)

    //private var text_success = ""
    //private var text_timer_stop = ""

/*    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as RunnerApplication)
                RunnerViewModel(application.userPreferencesRepository)
            }
        }
    }
*/

/*    fun setup(activity: MainActivity) {
        //updateConsole(activity.getString(R.string.text_console))
        //text_success = activity.getString(R.string.label_success)
        //text_timer_stop = activity.getString(R.string.label_timer_stop)
    }
*/

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

            //if (status.errorCount == 0 && status.timerCount == 0L)
            //    updateConsole(text_success)
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
        //if (flag) updateConsole(text_timer_stop)
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

    fun updateConsole(text: String) {
        console.append(text)
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

    fun updateGuideDialog(flag: Boolean) {
        _uiState.update { state ->
            state.copy(
                flagGuideDialog = flag
            )
        }
    }

    fun requestClearDataDialog(flag: Boolean) {
        _uiState.update { state ->
            state.copy(
                flagClearDataDialog = flag
            )
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            preferences.clearDataStore()
        }
        clear()
    }

    fun updateSettingValue() {
        _uiState.update { state ->
            state.copy(
                timerLimit = setting.value.timerLimit.toString(),
                loopLimit = setting.value.loopLimit.toString(),
                functionLimit = setting.value.functionLimit.toString()
            )
        }
    }

    fun updateTimerLimit(text: String) {
        _uiState.update { state ->
            state.copy(
                timerLimit = text
            )
        }
    }

    fun updateLoopLimit(text: String) {
        _uiState.update { state ->
            state.copy(
                loopLimit = text
            )
        }
    }

    fun updateFuncLimit(text: String) {
        _uiState.update { state ->
            state.copy(
                functionLimit = text
            )
        }
    }

    fun saveSettingValue() {
        var text = _uiState.value.timerLimit
        val timerLimit = if (text.isDigitsOnly() && text.isNotEmpty()) text.toLong() else 0

        text = _uiState.value.loopLimit
        val loopLimit = if (text.isDigitsOnly() && text.isNotEmpty()) text.toInt() else 0

        text = _uiState.value.functionLimit
        val functionLimit = if (text.isDigitsOnly() && text.isNotEmpty()) text.toInt() else 0

        val newSetting = SettingItem(
            timerLimit = timerLimit,
            loopLimit = loopLimit,
            functionLimit = functionLimit
        )
        viewModelScope.launch {
            preferences.saveSetting(newSetting)
        }
    }

    fun saveGuideDialog(flag: Boolean) {
        viewModelScope.launch {
            preferences.saveGuideDialog(flag)
        }
    }

    fun settingSafetyCheck(key: String, text: String): Boolean =
        if (text.isDigitsOnly() && text.isNotEmpty()) preferences.safetyCheck(key, text.toInt())
        else false
}
