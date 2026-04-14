package com.notebee.ui.noteslist

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.notebee.data.local.entity.Note
import com.notebee.data.local.entity.NoteWithTags
import com.notebee.ui.theme.PinYellow
import com.notebee.ui.theme.QuickNotesTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.notebee.R

/**
 * Main notes list screen: search bar, list of note cards, FAB to add note.
 * Pinned notes show a pin icon and are visually distinct.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesListScreen(
    state: NotesListUiState,
    onSearchQueryChange: (String) -> Unit,
    onNoteClick: (Long) -> Unit,
    onDeleteNote: (NoteWithTags) -> Unit,
    onTogglePin: (NoteWithTags) -> Unit,
    onAddNote: () -> Unit,
    onTagFilterSelected: (String?) -> Unit
) {
    Scaffold(topBar = {
        TopAppBar(
            title = { Text("Note Bee") },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = colorResource(R.color.primary),
                titleContentColor = colorResource(R.color.black)
            ))
    },floatingActionButton = {
        FloatingActionButton(
            onClick = onAddNote,
            containerColor = colorResource(R.color.primary),
            contentColor = colorResource(R.color.black),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add note")
        }
    },) { paddingValues ->Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {

        Column(modifier = Modifier.fillMaxSize()) {
            // Search bar at top
            TextField(
                value = state.searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Search notes...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null)
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                )
            )

            // Tag chips filter
            TagFilterRow(
                allTags = state.allTags,
                selectedTag = state.selectedTag,
                onTagSelected = onTagFilterSelected
            )

            if (state.notesWithTags.isEmpty()) {
                // Empty state
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    EmptyState(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(bottom = 150.dp)
                    )
                }
            } else {
                val notesWithReminders = remember(state.notesWithTags) {
                    state.notesWithTags.filter { it.note.reminderTime != null }
                }
                val notesWithoutReminders = remember(state.notesWithTags) {
                    state.notesWithTags.filter { it.note.reminderTime == null }
                }

                LazyColumn(
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 16.dp,
                        bottom = 88.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (notesWithReminders.isNotEmpty()) {
                        item(key = "header_reminders") {
                            SectionHeader(title = "Reminders")
                        }
                        itemsIndexed(
                            notesWithReminders,
                            key = { _, noteWithTags -> "reminder_${noteWithTags.note.id}" }
                        ) { _, noteWithTags ->
                            NoteCardAnimated(
                                noteWithTags = noteWithTags,
                                onNoteClick = onNoteClick,
                                onTogglePin = onTogglePin,
                                onDeleteNote = onDeleteNote
                            )
                        }
                        item { Spacer(modifier = Modifier.height(8.dp)) }
                    }

                    if (notesWithoutReminders.isNotEmpty()) {
                        if (notesWithReminders.isNotEmpty()) {
                            item(key = "header_others") {
                                SectionHeader(title = "Others")
                            }
                        }
                        itemsIndexed(
                            notesWithoutReminders,
                            key = { _, noteWithTags -> "other_${noteWithTags.note.id}" }
                        ) { _, noteWithTags ->
                            NoteCardAnimated(
                                noteWithTags = noteWithTags,
                                onNoteClick = onNoteClick,
                                onTogglePin = onTogglePin,
                                onDeleteNote = onDeleteNote
                            )
                        }
                    }
                }
            }
        }
    }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        ),
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 4.dp, horizontal = 4.dp)
    )
}

@Composable
fun NoteCardAnimated(
    noteWithTags: NoteWithTags,
    onNoteClick: (Long) -> Unit,
    onTogglePin: (NoteWithTags) -> Unit,
    onDeleteNote: (NoteWithTags) -> Unit
) {
    AnimatedVisibility(
        visible = true,
        enter = fadeIn() + slideInVertically(initialOffsetY = { it / 4 }),
        exit = fadeOut() + slideOutVertically(targetOffsetY = { -it / 4 })
    ) {
        NoteCard(
            noteWithTags = noteWithTags,
            onClick = { onNoteClick(noteWithTags.note.id) },
            onTogglePin = { onTogglePin(noteWithTags) },
            onDelete = { onDeleteNote(noteWithTags) }
        )
    }
}

@Composable
fun NoteCard(
    noteWithTags: NoteWithTags,
    onClick: () -> Unit,
    onTogglePin: () -> Unit,
    onDelete: () -> Unit
) {
    val note = noteWithTags.note
    val tags = noteWithTags.tags
    var showUnlockDialog by remember { mutableStateOf(false) }
    var passwordInput by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isError by remember { mutableStateOf(false) }

    if (showUnlockDialog) {
        AlertDialog(
            onDismissRequest = { 
                showUnlockDialog = false
                passwordInput = ""
                isError = false
            },
            title = { Text("Unlock Note") },
            text = {
                Column {
                    Text("Enter the password to view this note.")
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = { 
                            passwordInput = it
                            isError = false
                        },
                        label = { Text("Password") },
                        isError = isError,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(imageVector = image, contentDescription = "Toggle password visibility")
                            }
                        },
                        singleLine = true
                    )
                    if (isError) {
                        Text(
                            text = "Incorrect password",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (passwordInput == note.password) {
                        showUnlockDialog = false
                        onClick()
                    } else {
                        isError = true
                    }
                }) {
                    Text("Unlock")
                }
            },
            dismissButton = {
                TextButton(onClick = { showUnlockDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Card(
        onClick = {
            if (note.password != null) {
                showUnlockDialog = true
            } else {
                onClick()
            }
        },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (note.isPinned) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 4.dp)
                ) {
                    if (note.isPinned) {
                        Icon(
                            imageVector = Icons.Default.PushPin,
                            contentDescription = "Pinned",
                            tint = PinYellow,
                            modifier = Modifier.padding(end = 6.dp)
                        )
                    }
                    if (note.password != null) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Protected",
                            modifier = Modifier.size(18.dp).padding(end = 6.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Text(
                        text = note.title.ifBlank { "Untitled" },
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                Text(
                    text = if (note.password != null) "********" else note.content,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                // Tags row
                if (tags.isNotEmpty() && note.password == null) {
                    LazyRow(
                        modifier = Modifier.padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(tags.take(3)) { tag ->
                            InputChip(
                                selected = false,
                                onClick = { },
                                label = { Text(tag.name, style = MaterialTheme.typography.labelSmall) },
                                modifier = Modifier.height(24.dp)
                            )
                        }
                        if (tags.size > 3) {
                            item {
                                Text(
                                    text = "+${tags.size - 3}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                )
                            }
                        }
                    }
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = formatTimestamp(note.timestamp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    
                    if (note.reminderTime != null) {
                        Spacer(Modifier.size(8.dp))
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = "Reminder set",
                            modifier = Modifier.size(18.dp).padding(top = 8.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            
            Row(
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onTogglePin) {
                    Icon(
                        imageVector = Icons.Default.PushPin,
                        contentDescription = if (note.isPinned) "Unpin" else "Pin",
                        tint = if (note.isPinned) PinYellow else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "No notes yet\nTap + to create one",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

fun formatTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    return when {
        diff < 60_000 -> "Just now"
        diff < 3600_000 -> "${diff / 60_000}m ago"
        diff < 86400_000 -> "${diff / 3600_000}h ago"
        else -> SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(timestamp))
    }
}

@Preview(showBackground = true)
@Composable
fun NotesListScreenPreview() {
    QuickNotesTheme {
        val sampleNotes = listOf(
            Note(
                id = 1,
                title = "Pinned Note",
                content = "This is a pinned note content.",
                timestamp = System.currentTimeMillis(),
                isPinned = true,
                reminderTime = System.currentTimeMillis() + 100000
            ),
            Note(
                id = 2,
                title = "Regular Note",
                content = "This is a regular note content.",
                timestamp = System.currentTimeMillis() - 3600000,
                isPinned = false
            ),
            Note(
                id = 3,
                title = "",
                content = "This is a note without a title.",
                timestamp = System.currentTimeMillis() - 86400000,
                isPinned = false
            )
        )
        NotesListScreen(
            state = NotesListUiState(
                notesWithTags = sampleNotes.map { NoteWithTags(it, emptyList()) },
                allTags = emptyList(),
                searchQuery = "",
                selectedTag = null
            ),
            onSearchQueryChange = {},
            onNoteClick = {},
            onDeleteNote = {},
            onTogglePin = {},
            onAddNote = {},
            onTagFilterSelected = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun NotesListScreenEmptyPreview() {
    QuickNotesTheme {
        NotesListScreen(
            state = NotesListUiState(
                notesWithTags = emptyList(),
                allTags = emptyList(),
                searchQuery = "",
                selectedTag = null
            ),
            onSearchQueryChange = {},
            onNoteClick = {},
            onDeleteNote = {},
            onTogglePin = {},
            onAddNote = {},
            onTagFilterSelected = {}
        )
    }
}
