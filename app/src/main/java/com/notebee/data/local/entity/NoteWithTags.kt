package com.notebee.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

/**
 * Data class that represents a Note with its associated Tags.
 * Used for Room queries that need to fetch notes along with their tags.
 */
data class NoteWithTags(
    @Embedded
    val note: Note,
    
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = androidx.room.Junction(
            value = NoteTagCrossRef::class,
            parentColumn = "noteId",
            entityColumn = "tagId"
        )
    )
    val tags: List<Tag>
)
