package com.dullbluelab.textrunnertrial.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream

class ImageFiles(private var context: Context) {

    suspend fun store(name: String, bitmap: Bitmap) {
        withContext(Dispatchers.IO) {
            val file = File(context.filesDir, name)
            val stream = file.outputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.close()
        }
    }

    suspend fun load(path: String): Bitmap {
        val drawable: BitmapDrawable
        withContext(Dispatchers.IO) {
            val file = File(context.filesDir, path)
            val stream = FileInputStream(file)
            drawable = BitmapDrawable(context.resources, stream)
            stream.close()
        }
        return drawable.bitmap
    }

    suspend fun delete(name: String) {
        withContext(Dispatchers.IO) {
            val file = File(context.filesDir, name)
            file.delete()
        }
    }
}