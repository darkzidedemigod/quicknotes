package com.notebee.data.repository

import com.notebee.data.local.dao.NoteDao
import com.notebee.data.local.dao.TagDao
import com.notebee.data.local.entity.Note
import com.notebee.data.local.entity.Tag
import com.notebee.data.local.entity.NoteTagCrossRef
import com.notebee.data.local.entity.NoteWithTags
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository layer that abstracts data sources (currently only Room).
 * ViewModels depend on this interface, not on the DAO directly, for testability and single source of truth.
 */
@Singleton
class NoteRepository @Inject constructor(
    private val noteDao: NoteDao,
    private val tagDao: TagDao
) {

    /**
     * Stream of all notes, sorted: pinned first, then by latest timestamp.
     */
    fun getAllNotes(): Flow<List<Note>> = noteDao.getAllNotes()

    /**
     * Stream of all notes with their associated tags.
     */
    fun getNotesWithTags(): Flow<List<NoteWithTags>> = noteDao.getNotesWithTags()

    /**
     * Stream of notes filtered by search query (title or content).
     */
    fun searchNotes(query: String): Flow<List<Note>> = noteDao.searchNotes(query)

    /**
     * Stream of notes filtered by a specific tag name.
     */
    fun getNotesByTag(tagName: String): Flow<List<NoteWithTags>> = tagDao.getNotesByTag(tagName)

    suspend fun getNoteById(id: Long): Note? = noteDao.getNoteById(id)

    suspend fun getNoteWithTagsById(id: Long): NoteWithTags? = noteDao.getNoteWithTagsById(id)

    suspend fun insertNote(note: Note): Long = noteDao.insertNote(note)

    suspend fun updateNote(note: Note) = noteDao.updateNote(note)

    suspend fun deleteNote(note: Note) {
        // Delete tag associations first, then the note
        tagDao.deleteNoteTagReferences(note.id)
        noteDao.deleteNote(note)
    }

    suspend fun setPinned(id: Long, pinned: Boolean) = noteDao.setPinned(id, pinned)

    // Tag-related operations

    /**
     * Stream of all tags, sorted alphabetically.
     */
    fun getAllTags(): Flow<List<Tag>> = tagDao.getAllTags()

    /**
     * Stream of tags associated with a specific note.
     */
    fun getTagsForNote(noteId: Long): Flow<List<Tag>> = tagDao.getTagsForNote(noteId)

    /**
     * Creates a new tag if it doesn't exist, or returns existing tag.
     */
    suspend fun getOrCreateTag(name: String): Tag {
        return tagDao.getTagByName(name) ?: Tag(name = name.trim()).also {
            tagDao.insertTag(it)
        }
    }

    /**
     * Assigns a tag to a note.
     */
    suspend fun assignTagToNote(noteId: Long, tagId: Long) {
        tagDao.insertNoteTagCrossRef(NoteTagCrossRef(noteId, tagId))
    }

    /**
     * Removes a tag from a note.
     */
    suspend fun removeTagFromNote(noteId: Long, tagId: Long) {
        tagDao.deleteNoteTagCrossRef(NoteTagCrossRef(noteId, tagId))
    }

    /**
     * Updates tags for a note by replacing all existing tags with the provided list.
     */
    suspend fun updateNoteTags(noteId: Long, tagNames: List<String>) {
        // Remove existing tag associations
        tagDao.deleteNoteTagReferences(noteId)
        
        // Add new tag associations
        tagNames.forEach { tagName ->
            if (tagName.isNotBlank()) {
                val tag = getOrCreateTag(tagName.trim())
                assignTagToNote(noteId, tag.id)
            }
        }
    }

    /**
     * Deletes a tag and removes all its associations with notes.
     */
    suspend fun deleteTag(tag: Tag) {
        tagDao.deleteTag(tag)
    }

    /**
     * Cleans up unused tags (tags not associated with any notes).
     */
    suspend fun deleteUnusedTags() {
        tagDao.deleteUnusedTags()
    }

    /**
     * Returns the count of notes associated with a specific tag.
     */
    suspend fun getNoteCountForTag(tagId: Long): Int = tagDao.getNoteCountForTag(tagId)
}
