package com.quicknotes.ui.noteslist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quicknotes.data.local.entity.Note
import com.quicknotes.data.repository.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI state for the notes list screen.
 */
data class NotesListUiState(
    val notes: List<Note> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false
)

/**
 * ViewModel for the notes list screen.
 * Combines search query with repository Flow for real-time filtering.
 * Sorting (pinned first, then by timestamp) is done in the DAO.
 */
@HiltViewModel
class NotesListViewModel @Inject constructor(
    private val repository: NoteRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")

    /** Notes flow from DB (pinned first, then by latest timestamp). */
    private val notesFlow = combine(
        _searchQuery.asStateFlow(),
        repository.getAllNotes()
    ) { query, allNotes ->
        if (query.isBlank()) allNotes
        else allNotes.filter {
            it.title.contains(query, ignoreCase = true) ||
                it.content.contains(query, ignoreCase = true)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val state: StateFlow<NotesListUiState> = combine(
        _searchQuery.asStateFlow(),
        notesFlow
    ) { searchQuery, notes ->
        NotesListUiState(notes = notes, searchQuery = searchQuery)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = NotesListUiState()
    )

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch {
            repository.deleteNote(note)
        }
    }

    fun togglePin(note: Note) {
        viewModelScope.launch {
            repository.setPinned(note.id, !note.isPinned)
        }
    }
}
