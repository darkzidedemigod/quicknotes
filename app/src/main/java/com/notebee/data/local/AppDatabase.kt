package com.notebee.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.notebee.data.local.dao.NoteDao
import com.notebee.data.local.dao.TagDao
import com.notebee.data.local.entity.Note
import com.notebee.data.local.entity.Tag
import com.notebee.data.local.entity.NoteTagCrossRef

/**
 * Room database for the Quick Notes app.
 * Version is incremented when the schema changes; add migrations for production.
 */
@Database(
    entities = [Note::class, Tag::class, NoteTagCrossRef::class], 
    version = 4,
    exportSchema = false,
    autoMigrations = []
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
    abstract fun tagDao(): TagDao
}
