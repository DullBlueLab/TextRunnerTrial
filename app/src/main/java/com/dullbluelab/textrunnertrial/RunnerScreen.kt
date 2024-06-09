package com.dullbluelab.textrunnertrial

import android.os.Bundle
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.dullbluelab.textrunnertrial.ui.ScreenDrawing
import com.dullbluelab.textrunnertrial.ui.ScreenHome
import com.dullbluelab.textrunnertrial.ui.ScreenExecute
import com.dullbluelab.textrunnertrial.ui.ScreenSetting
import com.dullbluelab.textrunnertrial.ui.library.ItemScreen
import com.dullbluelab.textrunnertrial.ui.library.LibraryScreen
import com.dullbluelab.textrunnertrial.ui.library.LibraryViewModel

enum class RunnerScreen {
    Home, Drawing, Execute, Setting, Library, Item
}

@Composable
fun TextRunnerApp(
    activity: MainActivity,
    viewModel: RunnerViewModel = viewModel(factory = AppViewModelProvider.Factory),
    libraryViewModel: LibraryViewModel = viewModel(factory = AppViewModelProvider.Factory),
    launchLoadText: () -> Unit,
    onLinkGuideClicked: () -> Unit,
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    //val uiState by viewModel.uiState.collectAsState()
    val setting by viewModel.setting.collectAsState()
    activity.runnerViewModel = viewModel

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
                    if (setting.flagGuideDialog) viewModel.updateGuideDialog(true)
                    else onLinkGuideClicked()
                },
                onLibraryButtonClicked = {
                    navController.navigate(RunnerScreen.Library.name)
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
                    onLinkGuideClicked = onLinkGuideClicked,
                    launchLoadText = {
                        launchLoadText()
                    }
                )
            }
            composable(route = RunnerScreen.Drawing.name) {
                ScreenDrawing(viewModel)
            }
            composable(route = RunnerScreen.Execute.name) {
                ScreenExecute(viewModel)
            }
            composable(route = RunnerScreen.Library.name) {
                LibraryScreen(
                    viewModel = libraryViewModel,
                    onItemClick = { navController.navigate(RunnerScreen.Item.name) },
                    onLoadClick = {
                        activity.loadImageFile(success = { bitmapDrawable, uri ->
                            libraryViewModel.loadImage(bitmapDrawable, uri)
                            navController.navigate(RunnerScreen.Item.name)
                        })
                    }
                )
            }
            composable(route = RunnerScreen.Item.name) {
                ItemScreen(
                    viewModel = libraryViewModel,
                    done = { navController.navigateUp() }
                )
            }
            composable(route = RunnerScreen.Setting.name) {
                ScreenSetting(
                    viewModel = viewModel,
                    onDumpClicked = { navController.navigate(RunnerScreen.Execute.name) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RunnerAppBar(
    canNavigateBack: Boolean,
    currentScreen: String,
    navigateUp: () -> Unit,
    onGuideButtonClicked: () -> Unit,
    onSettingButtonClicked: () -> Unit,
    onLibraryButtonClicked: () -> Unit,
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
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back_button)
                    )
                }
            }
        },
        actions = {
            if (currentScreen == "Home") {
                IconButton(onClick = { onGuideButtonClicked() }) {
                    Icon(
                        painter = painterResource(id = R.drawable.help_24px),
                        contentDescription = stringResource(id = R.string.icon_guide)
                    )
                }
                IconButton(onClick = { onLibraryButtonClicked() }) {
                    Icon(
                        imageVector = Icons.Filled.DateRange,
                        contentDescription = stringResource(id = R.string.name_library)
                    )
                }
                IconButton(onClick = { onSettingButtonClicked() }) {
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = stringResource(id = R.string.setting_button)
                    )
                }
            }
        }
    )
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
        else if (viewModel.screenPosition != RunnerScreen.Setting.name
            && destination.route == RunnerScreen.Setting.name) {
            viewModel.updateSettingValue()
        }
        viewModel.screenPosition = destination.route ?: ""
    }
}
var destinationListener: DestinationListener? = null

private fun makeDestinationListener(viewModel: RunnerViewModel) : DestinationListener {
    destinationListener = DestinationListener(viewModel)
    return destinationListener!!
}
