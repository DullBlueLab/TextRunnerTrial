package com.example.textrunnertrial

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.WindowCompat
import com.example.textrunnertrial.logic.Syntax
import com.example.textrunnertrial.ui.theme.TextRunnerTrialTheme
import java.io.BufferedReader
import java.io.InputStreamReader

private var runnerViewModel: RunnerViewModel? = null

class MainActivity : ComponentActivity() {

    private var getContent: ActivityResultLauncher<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (runnerViewModel == null) {
            runnerViewModel = RunnerViewModel()
            runnerViewModel?.setup(this)
        }

        registerLoadText(this)
        Syntax.Errors.loadResource(this)
        destinationListener = null

        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            TextRunnerTrialTheme {
                TextRunnerApp(runnerViewModel!!) { launchLoadText() }
            }
        }
    }

    private fun registerLoadText(activity: MainActivity) {
        getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            try {
                if (uri != null) {
                    var strings = ""
                    contentResolver.openInputStream(uri)?.use { inputStream ->
                        BufferedReader(InputStreamReader(inputStream)).use { reader ->
                            var line: String? = reader.readLine()
                            while (line != null) {
                                strings += line + "\n"
                                line = reader.readLine()
                            }
                        }
                    }
                    runnerViewModel?.updateSourceText(strings)
                }
            }
            catch (e: Exception) {
                val message = activity.getString(R.string.error_unload_source_file)
                Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun launchLoadText() {
        getContent?.launch("text/plain")
    }

    override fun onStart() {
        super.onStart()
        if (runnerViewModel != null && runnerViewModel!!.enablePause()) {
            runnerViewModel?.restart()
        }
    }

    override fun onStop() {
        super.onStop()
        if (runnerViewModel != null && runnerViewModel!!.status().timerCount > 0) {
            runnerViewModel?.stop()
        }
    }

    override fun onDestroy() {
        getContent?.unregister()
        super.onDestroy()
    }
}

