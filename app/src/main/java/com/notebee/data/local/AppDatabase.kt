package com.notebee.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.notebee.data.local.dao.NoteDao
import com.notebee.data.local.entity.Note

/**
 * Room database for the Quick Notes app.
 * Version is incremented when the schema changes; add migrations for production.
 */
@Database(entities = [Note::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
}
