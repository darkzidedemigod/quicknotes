package com.notebee.ui.noteslist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notebee.data.local.entity.NoteWithTags
import com.notebee.data.local.entity.Tag
import com.notebee.data.repository.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
 */
@HiltViewModel
class NotesListViewModel @Inject constructor(
    private val repository: NoteRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedTag = MutableStateFlow<String?>(null)
    val selectedTag = _selectedTag.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val notesWithTagsFlow = combine(
        _searchQuery,
        _selectedTag
    ) { query, tag -> query to tag }
        .flatMapLatest { (query, tag) ->
            when {
                tag != null -> repository.getNotesByTag(tag)
                query.isNotBlank() -> repository.searchNotesWithTags(query)
                else -> repository.getNotesWithTags()
            }
        }

    val state: StateFlow<NotesListUiState> = combine(
        _searchQuery,
        _selectedTag,
        notesWithTagsFlow,
        repository.getAllTags()
    ) { query, tag, notes, tags ->
        NotesListUiState(
            notesWithTags = notes,
            allTags = tags,
            searchQuery = query,
            selectedTag = tag
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = NotesListUiState(isLoading = true)
    )

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        if (query.isNotBlank()) {
            _selectedTag.value = null
        }
    }

    fun setTagFilter(tagName: String?) {
        _selectedTag.value = tagName
        if (tagName != null) {
            _searchQuery.value = ""
        }
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
