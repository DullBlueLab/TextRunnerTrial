package com.dullbluelab.textrunnertrial.ui.library

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dullbluelab.textrunnertrial.RunnerViewModel
import com.dullbluelab.textrunnertrial.data.DirectoryTable
import com.dullbluelab.textrunnertrial.data.ImageFiles
import com.dullbluelab.textrunnertrial.data.ImageLibrary
import com.dullbluelab.textrunnertrial.data.LibraryRepository
import com.dullbluelab.textrunnertrial.data.UserPreferencesRepository
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val TAG = "LibraryViewModel"
private const val THUMBNAIL_LENGTH = 480
private const val THUMBNAIL_LABEL = "_thum"
private const val IMAGE_FILE_IDENTIFIER = ".png"

class LibraryViewModel(
    private val preferences: UserPreferencesRepository,
    private val repositories: LibraryRepository,
    private val imageFiles: ImageFiles
) : ViewModel() {

    data class ItemUiState(
        val mode: String = "edit", // or "load"
        val bitmap: Bitmap? = null,
        val directory: DirectoryTable? = null,
        val rename: String = "",
        val loadedUri: Uri? = null,
        val progressFlag: Boolean = false,
        val deleteDialogFlag: Boolean = false
    )
    private val _itemUi = MutableStateFlow(ItemUiState())
    val itemUi: StateFlow<ItemUiState> = _itemUi.asStateFlow()

    data class LibraryUiState(
        val list: List<ImageLibrary.Table> = listOf()
    )
    private val _libraryUi = MutableStateFlow(LibraryUiState())
    val libraryUi: StateFlow<LibraryUiState> = _libraryUi.asStateFlow()

    init {
        linkLibraryList()
    }

    private fun linkLibraryList() {
        viewModelScope.launch {
            try {
                repositories.loadImageList { list ->
                    _libraryUi.update { state ->
                        state.copy(
                            list = list
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, e.message, e)
            }
        }
    }

    //
    // ItemScreen

    fun updateRename(name: String) {
        _itemUi.update { state ->
            state.copy(
                rename = name
            )
        }
    }
    fun renameItem() {
        val newName = itemUi.value.rename
        itemUi.value.directory?.let { directory ->
            viewModelScope.launch {
                try {
                    repositories.renameImage(directory, newName)
                } catch (e: Exception) {
                    Log.e(TAG, e.message, e)
                }
            }
        }
    }

    fun deleteItem() {
        itemUi.value.directory?.let { directory ->
            viewModelScope.launch {
                try {
                    repositories.deleteImage(directory)
                } catch (e: Exception) {
                    Log.e(TAG, e.message, e)
                }
            }
        }
    }

    fun saveItem(done: (String) -> Unit) {
        itemProgress(true)
        viewModelScope.launch {
            try {
                itemUi.value.bitmap?.let { bitmap ->
                    repositories.appendImage(itemUi.value.rename, bitmap)
                }
                itemProgress(false)
                done("success")

            } catch (e:Exception) {
                Log.e(TAG, e.message, e)
                itemProgress(false)
                val message = e.message ?: "error"
                done(message)
            }
        }
    }

    private fun itemProgress(flag: Boolean) {
        _itemUi.update { state ->
            state.copy(
                progressFlag = flag
            )
        }
    }

    fun showDeleteDialog(flag: Boolean) {
        _itemUi.update { state ->
            state.copy(
                deleteDialogFlag = flag
            )
        }
    }

    //
    // LibraryScreen

    fun selectItem(directory: DirectoryTable) {
        viewModelScope.launch {
            val bitmap = repositories.loadImage(directory.name)
            _itemUi.update { state ->
                state.copy(
                    mode = "edit",
                    rename = directory.name,
                    bitmap = bitmap,
                    directory = directory
                )
            }
        }
    }

    fun loadImage(drawable: BitmapDrawable, uri: Uri) {
        _itemUi.update { state ->
            state.copy(
                mode = "load",
                bitmap = drawable.bitmap,
                directory = null,
                loadedUri = uri
            )
        }
    }
}