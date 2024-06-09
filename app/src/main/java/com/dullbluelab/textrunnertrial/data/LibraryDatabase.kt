package com.dullbluelab.textrunnertrial.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [DirectoryTable::class], version = 1, exportSchema = false)
abstract class LibraryDatabase : RoomDatabase() {
    abstract fun directoryDao(): DirectoryDao

    companion object {
        @Volatile
        private var Instance: LibraryDatabase? = null

        fun getDatabase(context: Context): LibraryDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(
                    context, LibraryDatabase::class.java, "library_database")
                    .build()
            }
        }
    }
}