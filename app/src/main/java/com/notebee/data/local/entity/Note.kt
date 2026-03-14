package com.notebee.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity representing a single note.
 * Primary key is auto-generated. Pinned notes are sorted to the top in the DAO.
 */
@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val content: String,
    val timestamp: Long,
    val isPinned: Boolean = false
)
