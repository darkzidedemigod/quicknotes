package com.notebee.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.notebee.data.local.entity.Note
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Note entity.
 * Exposes Flow-based queries so the UI updates automatically when data changes.
 * Sorting: pinned first, then by timestamp descending (latest first).
 */
@Dao
interface NoteDao {

    /**
     * Returns all notes as a Flow. Pinned notes first, then by latest timestamp.
     * Any change in the table triggers a new emission.
     */
    @Query("SELECT * FROM notes ORDER BY isPinned DESC, timestamp DESC")
    fun getAllNotes(): Flow<List<Note>>

    /**
     * Returns notes matching the search query (title or content).
     * Same sort order: pinned first, then by timestamp.
     */
    @Query("""
        SELECT * FROM notes 
        WHERE title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%'
        ORDER BY isPinned DESC, timestamp DESC
    """)
    fun searchNotes(query: String): Flow<List<Note>>

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
}
