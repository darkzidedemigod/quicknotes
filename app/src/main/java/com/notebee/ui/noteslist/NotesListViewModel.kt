package com.notebee.ui.noteslist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notebee.data.local.entity.Note
import com.notebee.data.local.entity.NoteWithTags
import com.notebee.data.local.entity.Tag
import com.notebee.data.repository.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI state for the notes list screen.
 */
data class NotesListUiState(
    val notesWithTags: List<NoteWithTags> = emptyList(),
    val allTags: List<Tag> = emptyList(),
    val searchQuery: String = "",
    val selectedTag: String? = null,
    val isLoading: Boolean = false
)

/**
 * ViewModel for the notes list screen.
 * Combines search query and tag filter with repository Flow for real-time filtering.
 * Sorting (pinned first, then by timestamp) is done in the DAO.
 */
@HiltViewModel
class NotesListViewModel @Inject constructor(
    private val repository: NoteRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _selectedTag = MutableStateFlow<String?>(null)

    /** Notes flow from DB (pinned first, then by latest timestamp). */
    private val notesWithTagsFlow = combine(
        _searchQuery.asStateFlow(),
        _selectedTag.asStateFlow(),
        repository.getAllTags()
    ) { query, selectedTag, allTags ->
        when {
            selectedTag != null -> {
                // Filter by selected tag
                repository.getNotesByTag(selectedTag)
            }
            query.isNotBlank() -> {
                // Search by query (convert to NoteWithTags)
                repository.searchNotes(query).map { notes ->
                    notes.map { note ->
                        // Convert Note to NoteWithTags with empty tags for search results
                        NoteWithTags(note = note, tags = emptyList())
                    }
                }
            }
            else -> {
                // All notes
                repository.getNotesWithTags()
            }
        }
    }.flatMapLatest { it }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val state: StateFlow<NotesListUiState> = combine(
        _searchQuery.asStateFlow(),
        _selectedTag.asStateFlow(),
        notesWithTagsFlow,
        repository.getAllTags()
    ) { searchQuery, selectedTag, notesWithTags, allTags ->
        NotesListUiState(
            notesWithTags = notesWithTags,
            allTags = allTags,
            searchQuery = searchQuery,
            selectedTag = selectedTag
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = NotesListUiState()
    )

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        // Clear tag filter when searching
        if (query.isNotBlank()) {
            _selectedTag.value = null
        }
    }

    fun setTagFilter(tagName: String?) {
        _selectedTag.value = tagName
        // Clear search when filtering by tag
        if (tagName != null) {
            _searchQuery.value = ""
        }
    }

    fun clearTagFilter() {
        _selectedTag.value = null
    }

    fun deleteNote(noteWithTags: NoteWithTags) {
        viewModelScope.launch {
            repository.deleteNote(noteWithTags.note)
        }
    }

    fun togglePin(noteWithTags: NoteWithTags) {
        viewModelScope.launch {
            repository.setPinned(noteWithTags.note.id, !noteWithTags.note.isPinned)
        }
    }
}
