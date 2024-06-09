package com.dullbluelab.textrunnertrial

import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.dullbluelab.textrunnertrial.ui.theme.TextRunnerTrialTheme
import java.io.BufferedReader
import java.io.InputStreamReader

private const val TAG = "MainActivity"

class MainActivity : ComponentActivity() {

    private var getTextContent: ActivityResultLauncher<String>? = null
    private var getImageContent: ActivityResultLauncher<String>? = null
    private var loadedImageListener: ((BitmapDrawable, Uri) -> Unit)? = null
    var runnerViewModel: RunnerViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        val guidanceUrl = getString(R.string.guidance_url)

        super.onCreate(savedInstanceState)

        registerLoadText()
        registerLoadImage()
        Errors.loadResource(this)
        destinationListener = null

        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            TextRunnerTrialTheme {
                TextRunnerApp(
                    activity = this,
                    launchLoadText = { launchLoadText() },
                    onLinkGuideClicked = { openWebPage(guidanceUrl) }
                )
            }
        }
    }

    private fun registerLoadText() {
        getTextContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
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
                    runnerViewModel?.updateConsole(getString(R.string.label_loaded))
                }
            }
            catch (e: Exception) {
                val message = this.getString(R.string.error_unload_source_file)
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun registerLoadImage() {
        getImageContent = registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            try {
                uri?.let {
                    contentResolver.openInputStream(it).use { inputStream ->
                        loadedImageListener?.let { listener ->
                            listener(BitmapDrawable(resources, inputStream), it)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, e.message, e)
            }
        }
    }

    fun loadImageFile(success: (BitmapDrawable, Uri) -> Unit) {
        try {
            loadedImageListener = success
            getImageContent?.launch("image/png")

        } catch (e: Exception) {
            Log.e(TAG, e.message, e)
        }
    }

    private fun launchLoadText() {
        getTextContent?.launch("text/plain")
    }

    private fun openWebPage(url: String) {
        try {
            val webpage: Uri = Uri.parse(url)
            val intent = Intent(Intent.ACTION_VIEW, webpage)
            startActivity(intent)
        }
        catch (e: Exception) {
            val message = getString(R.string.error_browse_guidance)
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
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
        getTextContent?.unregister()
        super.onDestroy()
    }
}

