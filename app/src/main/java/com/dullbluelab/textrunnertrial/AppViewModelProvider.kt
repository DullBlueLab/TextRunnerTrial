package com.dullbluelab.textrunnertrial

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.dullbluelab.textrunnertrial.ui.library.LibraryViewModel

object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            RunnerViewModel(
                runnerApplication().userPreferencesRepository,
                runnerApplication().container.libraryRepository
            )
        }
        initializer {
            val apply = runnerApplication()
            LibraryViewModel(
                apply.userPreferencesRepository,
                apply.container.libraryRepository,
                apply.imageFiles
            )
        }
    }
}

fun CreationExtras.runnerApplication(): RunnerApplication =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as RunnerApplication)