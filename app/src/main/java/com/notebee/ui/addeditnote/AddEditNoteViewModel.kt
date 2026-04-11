package com.notebee.ui.addeditnote

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notebee.data.local.entity.Note
import com.notebee.data.local.entity.NoteWithTags
import com.notebee.data.local.entity.Tag
import com.notebee.data.repository.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
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
    val isSaving: Boolean = false,
    val selectedTags: List<Tag> = emptyList(),
    val allTags: List<Tag> = emptyList(),
    val showTagSelector: Boolean = false
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
        // Load all tags
        viewModelScope.launch {
            repository.getAllTags().collect { allTags ->
                _state.update { it.copy(allTags = allTags) }
            }
        }

        // Load existing note if editing
        noteId?.let { id ->
            viewModelScope.launch {
                repository.getNoteWithTagsById(id)?.let { noteWithTags ->
                    _state.update {
                        it.copy(
                            title = noteWithTags.note.title,
                            content = noteWithTags.note.content,
                            isPinned = noteWithTags.note.isPinned,
                            selectedTags = noteWithTags.tags
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

    fun showTagSelector() {
        _state.update { it.copy(showTagSelector = true) }
    }

    fun hideTagSelector() {
        _state.update { it.copy(showTagSelector = false) }
    }

    fun toggleTagSelection(tag: Tag) {
        _state.update { currentState ->
            val isSelected = currentState.selectedTags.any { it.id == tag.id }
            val newSelectedTags = if (isSelected) {
                currentState.selectedTags.filter { it.id != tag.id }
            } else {
                currentState.selectedTags + tag
            }
            currentState.copy(selectedTags = newSelectedTags)
        }
    }

    fun addNewTag(tagName: String) {
        if (tagName.isBlank()) return
        
        viewModelScope.launch {
            val newTag = repository.getOrCreateTag(tagName.trim())
            _state.update { currentState ->
                if (!currentState.selectedTags.any { it.id == newTag.id }) {
                    currentState.copy(
                        selectedTags = currentState.selectedTags + newTag,
                        allTags = currentState.allTags + newTag
                    )
                } else {
                    currentState
                }
            }
        }
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
     * Uses current timestamp on save and updates tags.
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
                val insertedId = repository.insertNote(note)
                // Update noteId for tag assignment
                repository.updateNoteTags(insertedId, current.selectedTags.map { it.name })
                _state.update { it.copy(noteId = insertedId) }
                _state.update { it.copy(isSaving = false) }
                onSaved()
                return@launch
            }
            
            // Update tags for existing note
            repository.updateNoteTags(note.id, current.selectedTags.map { it.name })
            _state.update { it.copy(isSaving = false) }
            onSaved()
        }
    }
}
