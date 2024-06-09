package com.dullbluelab.textrunnertrial.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "directory_table")
data class DirectoryTable(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val name: String,
    val path: String,
    val type: String,
    val thumbnail: String
)

@Dao
interface DirectoryDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(item: DirectoryTable)

    @Update
    suspend fun update(item: DirectoryTable)

    @Delete
    suspend fun delete(item: DirectoryTable)

    @Query("SELECT * FROM directory_table WHERE name = :name")
    fun getTable(name: String): Flow<DirectoryTable>

    @Query("DELETE FROM directory_table")
    suspend fun deleteAll()

    @Query("SELECT * FROM directory_table")
    fun getAll(): Flow<List<DirectoryTable>>
}