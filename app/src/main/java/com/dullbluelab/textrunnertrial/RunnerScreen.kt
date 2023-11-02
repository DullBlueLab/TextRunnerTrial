package com.dullbluelab.textrunnertrial

import android.os.Bundle
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.dullbluelab.textrunnertrial.ui.GuideDialog
import com.dullbluelab.textrunnertrial.ui.ScreenDrawing
import com.dullbluelab.textrunnertrial.ui.ScreenHome
import com.dullbluelab.textrunnertrial.ui.ScreenExecute
import com.dullbluelab.textrunnertrial.ui.ScreenSetting

enum class RunnerScreen {
    Home, Drawing, Execute, Setting
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RunnerAppBar(
    canNavigateBack: Boolean,
    currentScreen: String,
    navigateUp: () -> Unit,
    onGuideButtonClicked: () -> Unit,
    onSettingButtonClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val title = when(currentScreen) {
        "Home" -> stringResource(id = R.string.app_name)
        "Drawing" -> stringResource(id = R.string.title_drawing)
        "Execute" -> stringResource(id = R.string.title_execute)
        else -> currentScreen
    }

    TopAppBar(
        title = { Text(title) },
        colors = TopAppBarDefaults.mediumTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer ),
        modifier = modifier,
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(onClick = navigateUp) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back_button)
                    )
                }
            }
        },
        actions = {
            if (currentScreen == "Home") {
                IconButton(
                    onClick = {
                        onGuideButtonClicked()
                    }
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.help_24px),
                        contentDescription = stringResource(id = R.string.icon_guide))
                }
                IconButton(
                    onClick = {
                        onSettingButtonClicked()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = stringResource(id = R.string.setting_button)
                    )
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextRunnerApp(
    viewModel: RunnerViewModel,
    launchLoadText: () -> Unit,
    onLinkGuideClicked: () -> Unit,

) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val uiState by viewModel.uiState.collectAsState()

    if (destinationListener == null) {
        val listener = makeDestinationListener(viewModel)
        navController.addOnDestinationChangedListener(listener)
    }

    Scaffold(
        topBar = {
            RunnerAppBar(
                canNavigateBack = navController.previousBackStackEntry != null,
                currentScreen = backStackEntry?.destination?.route ?: RunnerScreen.Home.name,
                navigateUp = {
                    navController.navigateUp()
                },
                onGuideButtonClicked = {
                    viewModel.updateGuideDialog(true)
                },
                onSettingButtonClicked = {
                    navController.navigate(RunnerScreen.Setting.name)
                }
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = RunnerScreen.Home.name,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(route = RunnerScreen.Home.name) {
                ScreenHome(
                    viewModel = viewModel,
                    onExecuteButtonClicked = {
                        viewModel.run()
                        if (viewModel.status().errorCount == 0) {
                            navController.navigate(RunnerScreen.Drawing.name)
                        }
                    },
                    onClearButtonClicked = {
                        viewModel.clear()
                    },
                    launchLoadText = {
                        launchLoadText()
                    }
                )
                if (uiState.flagGuideDialog) {
                    GuideDialog(
                        onBrowse = {
                            viewModel.updateGuideDialog(false)
                            onLinkGuideClicked()
                        },
                        onCancel = {
                            viewModel.updateGuideDialog(false)
                        }
                    )
                }
            }
            composable(route = RunnerScreen.Drawing.name) {
                ScreenDrawing(viewModel)
            }
            composable(route = RunnerScreen.Execute.name) {
                ScreenExecute(viewModel)
            }
            composable(route = RunnerScreen.Setting.name) {
                viewModel.setupSettingValue()
                ScreenSetting(
                    viewModel,
                    onDumpClicked = {
                        navController.navigate(RunnerScreen.Execute.name)
                    }
                )
            }
        }
    }
}

class DestinationListener(private val viewModel: RunnerViewModel)
    : NavController.OnDestinationChangedListener {

    override fun onDestinationChanged(
        controller: NavController,
        destination: NavDestination,
        arguments: Bundle?
    ) {
        if (viewModel.screenPosition == RunnerScreen.Drawing.name
            && destination.route != RunnerScreen.Drawing.name
            && viewModel.status().runnerActive) {
            viewModel.stop()
        }
        else if (viewModel.screenPosition == RunnerScreen.Setting.name
            && destination.route != RunnerScreen.Setting.name) {
            viewModel.saveSettingValue()
        }
        viewModel.screenPosition = destination.route ?: ""
    }
}
var destinationListener: DestinationListener? = null

private fun makeDestinationListener(viewModel: RunnerViewModel) : DestinationListener {
    destinationListener = DestinationListener(viewModel)
    return destinationListener!!
}
