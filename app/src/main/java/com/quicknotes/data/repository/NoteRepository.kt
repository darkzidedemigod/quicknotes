package com.quicknotes.data.repository

import com.quicknotes.data.local.dao.NoteDao
import com.quicknotes.data.local.entity.Note
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository layer that abstracts data sources (currently only Room).
 * ViewModels depend on this interface, not on the DAO directly, for testability and single source of truth.
 */
@Singleton
class NoteRepository @Inject constructor(
    private val noteDao: NoteDao
) {

    /**
     * Stream of all notes, sorted: pinned first, then by latest timestamp.
     */
    fun getAllNotes(): Flow<List<Note>> = noteDao.getAllNotes()

    /**
     * Stream of notes filtered by search query (title or content).
     */
    fun searchNotes(query: String): Flow<List<Note>> = noteDao.searchNotes(query)

    suspend fun getNoteById(id: Long): Note? = noteDao.getNoteById(id)

    suspend fun insertNote(note: Note): Long = noteDao.insertNote(note)

    suspend fun updateNote(note: Note) = noteDao.updateNote(note)

    suspend fun deleteNote(note: Note) = noteDao.deleteNote(note)

    suspend fun setPinned(id: Long, pinned: Boolean) = noteDao.setPinned(id, pinned)
}
