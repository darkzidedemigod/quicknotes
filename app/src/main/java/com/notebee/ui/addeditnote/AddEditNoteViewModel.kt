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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
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
    val isAiLoading: Boolean = false,
    val aiError: String? = null,
    val reminderTime: Long? = null,
    val suggestedReminder: String? = null,
    val showDatePicker: Boolean = false,
    val showTimePicker: Boolean = false,
    val selectedDate: Long? = null,
    val password: String? = null,
    val showPasswordDialog: Boolean = false
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
                            selectedTags = noteWithTags.tags,
                            reminderTime = noteWithTags.note.reminderTime,
                            password = noteWithTags.note.password
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

    fun setReminder(time: Long?) {
        _state.update { it.copy(reminderTime = time) }
    }

    fun setPassword(password: String?) {
        _state.update { it.copy(password = password, showPasswordDialog = false) }
    }

    fun setShowPasswordDialog(show: Boolean) {
        _state.update { it.copy(showPasswordDialog = show) }
    }

    fun setShowDatePicker(show: Boolean) {
        _state.update { it.copy(showDatePicker = show) }
    }

    fun setShowTimePicker(show: Boolean) {
        _state.update { it.copy(showTimePicker = show) }
    }

    fun onDateSelected(dateMillis: Long?) {
        _state.update { it.copy(selectedDate = dateMillis, showDatePicker = false, showTimePicker = true) }
    }

    fun onTimeSelected(hour: Int, minute: Int) {
        val selectedDate = _state.value.selectedDate ?: System.currentTimeMillis()
        val calendar = Calendar.getInstance().apply {
            timeInMillis = selectedDate
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        setReminder(calendar.timeInMillis)
        _state.update { it.copy(showTimePicker = false, selectedDate = null) }
    }

    fun suggestSmartReminder() {
        val currentContent = _state.value.content
        if (currentContent.isBlank()) return

        viewModelScope.launch {
            _state.update { it.copy(isAiLoading = true, aiError = null) }
            try {
                val suggestion = aiRepository.suggestReminder(currentContent)
                if (suggestion != null) {
                    _state.update { it.copy(suggestedReminder = suggestion) }
                } else {
                    _state.update { it.copy(aiError = "Failed to suggest reminder") }
                }
            } catch (e: Exception) {
                _state.update { it.copy(aiError = "Error: ${e.message}") }
            } finally {
                _state.update { it.copy(isAiLoading = false) }
            }
        }
    }

    fun acceptSuggestedReminder() {
        val suggestion = _state.value.suggestedReminder ?: return
        // Basic parsing for demonstration. Real app would use a natural language library.
        val calendar = Calendar.getInstance()
        when {
            suggestion.contains("tomorrow", ignoreCase = true) -> calendar.add(Calendar.DAY_OF_YEAR, 1)
            suggestion.contains("hour", ignoreCase = true) -> calendar.add(Calendar.HOUR_OF_DAY, 1)
            suggestion.contains("week", ignoreCase = true) -> calendar.add(Calendar.WEEK_OF_YEAR, 1)
            else -> calendar.add(Calendar.DAY_OF_YEAR, 1) // Default to tomorrow
        }
        setReminder(calendar.timeInMillis)
        _state.update { it.copy(suggestedReminder = null) }
    }

    fun clearSuggestedReminder() {
        _state.update { it.copy(suggestedReminder = null) }
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
            _state.update { it.copy(isAiLoading = true, aiError = null) }
            try {
                val suggestedTagNames = aiRepository.suggestTags(currentTitle, currentContent)
                if (suggestedTagNames.isNotEmpty()) {
                    val newTags = suggestedTagNames.map { repository.getOrCreateTag(it) }
                    _state.update { currentState ->
                        val updatedSelected = (currentState.selectedTags + newTags).distinctBy { it.id }
                        currentState.copy(selectedTags = updatedSelected)
                    }
                } else {
                    _state.update { it.copy(aiError = "No tags suggested") }
                }
            } catch (e: Exception) {
                _state.update { it.copy(aiError = "Error: ${e.message}") }
            } finally {
                _state.update { it.copy(isAiLoading = false) }
            }
        }
    }

    fun generateTitle() {
        val currentContent = _state.value.content
        if (currentContent.isBlank()) return

        viewModelScope.launch {
            _state.update { it.copy(isAiLoading = true, aiError = null) }
            try {
                val generatedTitle = aiRepository.generateTitle(currentContent)
                if (!generatedTitle.isNullOrBlank()) {
                    _state.update { it.copy(title = generatedTitle) }
                } else {
                    _state.update { it.copy(aiError = "Failed to generate title") }
                }
            } catch (e: Exception) {
                _state.update { it.copy(aiError = "Error: ${e.message}") }
            } finally {
                _state.update { it.copy(isAiLoading = false) }
            }
        }
    }

    fun clearAiError() {
        _state.update { it.copy(aiError = null) }
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
                    isPinned = current.isPinned,
                    reminderTime = current.reminderTime,
                    password = current.password
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
