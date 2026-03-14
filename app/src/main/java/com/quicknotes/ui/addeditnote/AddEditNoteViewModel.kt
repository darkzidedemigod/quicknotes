package com.quicknotes.ui.addeditnote

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quicknotes.data.local.entity.Note
import com.quicknotes.data.repository.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI state for add/edit note screen.
 */
data class AddEditNoteUiState(
    val title: String = "",
    val content: String = "",
    val isPinned: Boolean = false,
    val noteId: Long? = null,
    val isSaving: Boolean = false
)

/**
 * ViewModel for adding a new note or editing an existing one.
 * noteId from SavedStateHandle: when null, we're adding; otherwise editing.
 */
@HiltViewModel
class AddEditNoteViewModel @Inject constructor(
    private val repository: NoteRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val noteId: Long? = savedStateHandle.get<Long>("noteId")

    private val _state = MutableStateFlow(AddEditNoteUiState(noteId = noteId))
    val state: StateFlow<AddEditNoteUiState> = _state.asStateFlow()

    init {
        noteId?.let { id ->
            viewModelScope.launch {
                repository.getNoteById(id)?.let { note ->
                    _state.update {
                        it.copy(
                            title = note.title,
                            content = note.content,
                            isPinned = note.isPinned
                        )
                    }
                }
            }
        }
    }

    fun updateTitle(title: String) {
        _state.update { it.copy(title = title) }
    }

    fun updateContent(content: String) {
        _state.update { it.copy(content = content) }
    }

    fun togglePinned() {
        _state.update { it.copy(isPinned = !it.isPinned) }
    }

    /**
     * Deletes the current note (only when editing). Call onDeleted after delete.
     */
    fun deleteNote(onDeleted: () -> Unit) {
        val id = _state.value.noteId ?: return
        viewModelScope.launch {
            repository.getNoteById(id)?.let { repository.deleteNote(it) }
            onDeleted()
        }
    }

    /**
     * Saves the note: insert if new, update if editing.
     * Uses current timestamp on save.
     */
    fun saveNote(onSaved: () -> Unit) {
        val current = _state.value
        if (current.title.isBlank() && current.content.isBlank()) {
            onSaved()
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            val timestamp = System.currentTimeMillis()
            val note = Note(
                id = current.noteId ?: 0,
                title = current.title.ifBlank { "Untitled" },
                content = current.content,
                timestamp = timestamp,
                isPinned = current.isPinned
            )
            if (current.noteId != null) {
                repository.updateNote(note)
            } else {
                repository.insertNote(note)
            }
            _state.update { it.copy(isSaving = false) }
            onSaved()
        }
    }
}
