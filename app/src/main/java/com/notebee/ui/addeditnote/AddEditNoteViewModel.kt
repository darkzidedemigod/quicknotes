package com.notebee.ui.addeditnote

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notebee.data.local.entity.Note
import com.notebee.data.local.entity.Tag
import com.notebee.data.repository.AiRepository
import com.notebee.data.repository.NoteRepository
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
    val isSaving: Boolean = false,
    val selectedTags: List<Tag> = emptyList(),
    val allTags: List<Tag> = emptyList(),
    val showTagSelector: Boolean = false,
    val isAiLoading: Boolean = false
)

/**
 * ViewModel for adding a new note or editing an existing one.
 */
@HiltViewModel
class AddEditNoteViewModel @Inject constructor(
    private val repository: NoteRepository,
    private val aiRepository: AiRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val noteId: Long? = savedStateHandle.get<Long>("noteId")

    private val _state = MutableStateFlow(AddEditNoteUiState(noteId = noteId))
    val state: StateFlow<AddEditNoteUiState> = _state.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
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
        val trimmed = tagName.trim()
        if (trimmed.isBlank()) return
        
        viewModelScope.launch {
            val tag = repository.getOrCreateTag(trimmed)
            _state.update { currentState ->
                if (currentState.selectedTags.none { it.id == tag.id }) {
                    currentState.copy(selectedTags = currentState.selectedTags + tag)
                } else currentState
            }
        }
    }

    fun suggestTags() {
        val currentTitle = _state.value.title
        val currentContent = _state.value.content
        if (currentContent.isBlank()) return

        viewModelScope.launch {
            _state.update { it.copy(isAiLoading = true) }
            try {
                val suggestedTagNames = aiRepository.suggestTags(currentTitle, currentContent)
                val newTags = suggestedTagNames.map { repository.getOrCreateTag(it) }
                
                _state.update { currentState ->
                    val updatedSelected = (currentState.selectedTags + newTags).distinctBy { it.id }
                    currentState.copy(selectedTags = updatedSelected)
                }
            } finally {
                _state.update { it.copy(isAiLoading = false) }
            }
        }
    }

    fun generateTitle() {
        val currentContent = _state.value.content
        if (currentContent.isBlank()) return

        viewModelScope.launch {
            _state.update { it.copy(isAiLoading = true) }
            try {
                val generatedTitle = aiRepository.generateTitle(currentContent)
                if (!generatedTitle.isNullOrBlank()) {
                    _state.update { it.copy(title = generatedTitle) }
                }
            } finally {
                _state.update { it.copy(isAiLoading = false) }
            }
        }
    }

    fun deleteNote(onDeleted: () -> Unit) {
        val id = noteId ?: return
        viewModelScope.launch {
            repository.getNoteById(id)?.let { repository.deleteNote(it) }
            onDeleted()
        }
    }

    fun saveNote(onSaved: () -> Unit) {
        val current = _state.value
        if (current.title.isBlank() && current.content.isBlank()) {
            onSaved()
            return
        }
        
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            try {
                val note = Note(
                    id = current.noteId ?: 0L,
                    title = current.title.ifBlank { "Untitled" },
                    content = current.content,
                    timestamp = System.currentTimeMillis(),
                    isPinned = current.isPinned
                )
                
                val savedId = repository.saveNoteWithTags(note, current.selectedTags.map { it.name })
                _state.update { it.copy(noteId = savedId) }
                onSaved()
            } finally {
                _state.update { it.copy(isSaving = false) }
            }
        }
    }
}
