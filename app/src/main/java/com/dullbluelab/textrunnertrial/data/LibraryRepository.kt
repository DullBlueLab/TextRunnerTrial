package com.dullbluelab.textrunnertrial.data

import android.content.Context
import android.graphics.Bitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class LibraryRepository(
    private val directoryDao: DirectoryDao,
    private val database: LibraryDatabase,
    context: Context
) {
    private suspend fun insertDirectoryTable(table: DirectoryTable) = directoryDao.insert(table)
    private suspend fun deleteDirectoryTable(table: DirectoryTable) = directoryDao.delete(table)
    private suspend fun updateDirectoryTable(table: DirectoryTable) = directoryDao.update(table)
//    suspend fun deleteDirectoryAll() = directoryDao.deleteAll()
//    fun getDirectoryItem(name: String): Flow<DirectoryTable> = directoryDao.getTable(name)
    private fun getDirectoryAll(): Flow<List<DirectoryTable>> = directoryDao.getAll()

    private val imageLibrary: ImageLibrary = ImageLibrary(context)

    suspend fun loadImageList(success: (List<ImageLibrary.Table>) -> Unit) {
        val stream = withContext(Dispatchers.Default) { getDirectoryAll() }
        stream.collect { directory ->
            imageLibrary.loadList(directory)
            success(imageLibrary.list)
        }
    }

    suspend fun appendImage(name: String, bitmap: Bitmap) {
        val table = makeDirectoryTable(name, bitmap)
        imageLibrary.store(table, bitmap)
        withContext(Dispatchers.Default) { insertDirectoryTable(table) }
    }

    suspend fun loadImage(name: String): Bitmap? {
        return imageLibrary.loadImage(name)
    }

    suspend fun deleteImage(table: DirectoryTable) {
        imageLibrary.delete(table)
        withContext(Dispatchers.Default) { deleteDirectoryTable(table) }
    }

    private fun makeDirectoryTable(name: String, bitmap: Bitmap): DirectoryTable {
        val path = "$name.png"
        val width = bitmap.width
        val height = bitmap.height
        val thumbnail = "$name-thum.png"
        return DirectoryTable(0, name, path, "PNG", width, height, thumbnail)
    }

    suspend fun renameImage(directory: DirectoryTable, newName: String) {
        val newDirectory = directory.copy(
            name = newName
        )
        withContext(Dispatchers.Default) { updateDirectoryTable(newDirectory) }
        imageLibrary.rename(directory, newName)
    }
}
