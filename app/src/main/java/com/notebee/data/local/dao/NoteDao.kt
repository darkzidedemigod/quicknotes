package com.notebee.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Transaction
import com.notebee.data.local.entity.Note
import com.notebee.data.local.entity.NoteWithTags
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Note entity.
 */
@Dao
interface NoteDao {

    @Query("SELECT * FROM notes ORDER BY isPinned DESC, timestamp DESC")
    fun getAllNotes(): Flow<List<Note>>

    @Query("""
        SELECT * FROM notes 
        WHERE title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%'
        ORDER BY isPinned DESC, timestamp DESC
    """)
    fun searchNotes(query: String): Flow<List<Note>>

    @Transaction
    @Query("""
        SELECT * FROM notes 
        WHERE title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%'
        ORDER BY isPinned DESC, timestamp DESC
    """)
    fun searchNotesWithTags(query: String): Flow<List<NoteWithTags>>

    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getNoteById(id: Long): Note?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: Note): Long

    @Update
    suspend fun updateNote(note: Note)

    @Delete
    suspend fun deleteNote(note: Note)

    @Query("UPDATE notes SET isPinned = :pinned WHERE id = :id")
    suspend fun setPinned(id: Long, pinned: Boolean)

    @Transaction
    @Query("SELECT * FROM notes ORDER BY isPinned DESC, timestamp DESC")
    fun getNotesWithTags(): Flow<List<NoteWithTags>>

    @Transaction
    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getNoteWithTagsById(id: Long): NoteWithTags?
}
