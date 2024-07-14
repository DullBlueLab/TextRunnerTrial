package com.dullbluelab.textrunnertrial.data

import android.content.Context
import android.graphics.Bitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val THUMBNAIL_SIZE = 240
private const val ERROR_NOT_FILE_OPEN = "Not file open : LibraryImeges.appendImage"

class ImageLibrary(
    private val context: Context
) {
    val list: MutableList<Table> = mutableListOf()
    class Table(
        var directory: DirectoryTable,
        var thumbnailBitmap: Bitmap,
        var width: Int = 0,
        var height: Int = 0,
        var importFlag: Boolean = false,
        var bitmap: Bitmap? = null
    ) {
        var name: String = directory.name
    }

    private val imageFiles: ImageFiles = ImageFiles(context)

    private suspend fun load(directory: DirectoryTable): Table {
        val table: Table
        val bitmap = imageFiles.load(directory.thumbnail)
        table = Table(directory, bitmap, directory.width, directory.height)
        return table
    }

    suspend fun loadList(directoryList: List<DirectoryTable>) {
        list.clear()
        for (directory in directoryList) {
            val table = load(directory)
            list.add(table)
        }
    }

    suspend fun store(directory: DirectoryTable, bitmap: Bitmap) {
        val thumbnailBitmap = makeThumbnail(bitmap)
        imageFiles.store(directory.path, bitmap)
        imageFiles.store(directory.thumbnail, thumbnailBitmap)
        val table = Table(directory, thumbnailBitmap, bitmap.width, bitmap.height)
        list.add(table)
    }

    private suspend fun makeThumbnail(bitmap: Bitmap): Bitmap {
        val thumbnail: Bitmap
        val targetWidth: Int
        val targetHeight: Int

        if (bitmap.width <= THUMBNAIL_SIZE && bitmap.height <= THUMBNAIL_SIZE) {
            targetWidth = bitmap.width
            targetHeight = bitmap.height
        } else if (bitmap.width > bitmap.height) {
            targetHeight = bitmap.height / (bitmap.width / THUMBNAIL_SIZE)
            targetWidth = THUMBNAIL_SIZE
        } else {
            targetWidth = bitmap.width / (bitmap.height / THUMBNAIL_SIZE)
            targetHeight = THUMBNAIL_SIZE
        }

        withContext(Dispatchers.Default) {
            thumbnail = Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)
        }
        return thumbnail
    }

    suspend fun delete(directory: DirectoryTable) {
        for (table in list) {
            if (table.directory.name == directory.name) {
                list.remove(table)
                break
            }
        }
        imageFiles.delete(directory.thumbnail)
        imageFiles.delete(directory.path)
    }

    suspend fun loadImage(name: String): Bitmap? {
        var result: Bitmap? = null
        for (table in list) {
            if (table.name == name) {
                result = imageFiles.load(table.directory.path)
                break
            }
        }
        return result
    }

    fun rename(directory: DirectoryTable, newName: String) {
        for (table in list) {
            if (table.name == directory.name) {
                val newDirectory = table.directory.copy(
                    name = newName
                )
                table.directory = newDirectory
                table.name = newName
                break
            }
        }
    }
}
