package com.notebee.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.notebee.data.local.entity.Tag
import com.notebee.data.local.entity.NoteTagCrossRef
import com.notebee.data.local.entity.NoteWithTags
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Tag entity and tag-related operations.
 * Handles tag management and many-to-many relationships with notes.
 */
@Dao
interface TagDao {

    /**
     * Returns all tags as a Flow, sorted alphabetically by name.
     */
    @Query("SELECT * FROM tags ORDER BY name ASC")
    fun getAllTags(): Flow<List<Tag>>

    /**
     * Returns a tag by its name.
     */
    @Query("SELECT * FROM tags WHERE name = :name LIMIT 1")
    suspend fun getTagByName(name: String): Tag?

    /**
     * Returns a tag by its ID.
     */
    @Query("SELECT * FROM tags WHERE id = :id LIMIT 1")
    suspend fun getTagById(id: Long): Tag?

    /**
     * Inserts a new tag. If a tag with the same name exists, it replaces it.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTag(tag: Tag): Long

    /**
     * Updates an existing tag.
     */
    @Update
    suspend fun updateTag(tag: Tag)

    /**
     * Deletes a tag and all its associated cross-references.
     */
    @Delete
    suspend fun deleteTag(tag: Tag)

    /**
     * Inserts a cross-reference between a note and a tag.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertNoteTagCrossRef(ref: NoteTagCrossRef)

    /**
     * Deletes a cross-reference between a note and a tag.
     */
    @Delete
    suspend fun deleteNoteTagCrossRef(ref: NoteTagCrossRef)

    /**
     * Returns all notes with their associated tags.
     */
    @Query("""
        SELECT * FROM notes 
        ORDER BY isPinned DESC, timestamp DESC
    """)
    fun getNotesWithTags(): Flow<List<NoteWithTags>>

    /**
     * Returns notes with their associated tags, filtered by a specific tag name.
     */
    @Query("""
        SELECT DISTINCT notes.* FROM notes
        INNER JOIN note_tag_cross_ref ON notes.id = note_tag_cross_ref.noteId
        INNER JOIN tags ON note_tag_cross_ref.tagId = tags.id
        WHERE tags.name = :tagName
        ORDER BY notes.isPinned DESC, notes.timestamp DESC
    """)
    fun getNotesByTag(tagName: String): Flow<List<NoteWithTags>>

    /**
     * Returns tags associated with a specific note.
     */
    @Query("""
        SELECT tags.* FROM tags
        INNER JOIN note_tag_cross_ref ON tags.id = note_tag_cross_ref.tagId
        WHERE note_tag_cross_ref.noteId = :noteId
        ORDER BY tags.name ASC
    """)
    fun getTagsForNote(noteId: Long): Flow<List<Tag>>

    /**
     * Deletes all cross-references for a specific note (called when a note is deleted).
     */
    @Query("DELETE FROM note_tag_cross_ref WHERE noteId = :noteId")
    suspend fun deleteNoteTagReferences(noteId: Long)

    /**
     * Deletes all cross-references for a specific tag (called when a tag is deleted).
     */
    @Query("DELETE FROM note_tag_cross_ref WHERE tagId = :tagId")
    suspend fun deleteTagNoteReferences(tagId: Long)

    /**
     * Deletes unused tags (tags that are not associated with any notes).
     */
    @Query("""
        DELETE FROM tags 
        WHERE id NOT IN (
            SELECT DISTINCT tagId FROM note_tag_cross_ref
        )
    """)
    suspend fun deleteUnusedTags()

    /**
     * Returns the count of notes associated with a specific tag.
     */
    @Query("SELECT COUNT(*) FROM note_tag_cross_ref WHERE tagId = :tagId")
    suspend fun getNoteCountForTag(tagId: Long): Int
}
