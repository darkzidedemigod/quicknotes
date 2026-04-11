package com.notebee.ui.addeditnote

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.notebee.data.local.entity.Tag
import com.notebee.ui.theme.PinYellow
import com.notebee.ui.theme.QuickNotesTheme

/**
 * Screen for creating a new note or editing an existing one.
 * Shows title and content fields, pin toggle, save and (when editing) delete.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditNoteScreen(
    state: AddEditNoteUiState,
    onTitleChange: (String) -> Unit,
    onContentChange: (String) -> Unit,
    onTogglePinned: () -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit,
    onDelete: (() -> Unit)?,
    onShowTagSelector: () -> Unit,
    onHideTagSelector: () -> Unit,
    onToggleTagSelection: (com.notebee.data.local.entity.Tag) -> Unit,
    onAddNewTag: (String) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (state.noteId == null) "New Note" else "Edit Note") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onTogglePinned) {
                        Icon(
                            Icons.Default.PushPin,
                            contentDescription = if (state.isPinned) "Unpin" else "Pin",
                            tint = if (state.isPinned) PinYellow else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = onSave) {
                        Icon(Icons.Default.Save, contentDescription = "Save")
                    }
                    if (onDelete != null) {
                        IconButton(onClick = onDelete) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = state.title,
                onValueChange = onTitleChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Title") },
                placeholder = { Text("Note title") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = state.content,
                onValueChange = onContentChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 200.dp),
                label = { Text("Content") },
                placeholder = { Text("Write your note...") },
                minLines = 8,
                shape = RoundedCornerShape(12.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Tags section
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Tags",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Button(
                        onClick = onShowTagSelector,
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
                        Text("Add Tag")
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Selected tags row
                if (state.selectedTags.isNotEmpty()) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.selectedTags) { tag ->
                            InputChip(
                                selected = true,
                                onClick = { onToggleTagSelection(tag) },
                                label = { Text(tag.name) },
                                trailingIcon = {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Remove tag",
                                        modifier = Modifier.padding(end = 4.dp)
                                    )
                                }
                            )
                        }
                    }
                } else {
                    Text(
                        text = "No tags added. Tap 'Add Tag' to organize your note.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Tag selector modal
            if (state.showTagSelector) {
                TagSelectorBottomSheet(
                    isVisible = state.showTagSelector,
                    allTags = state.allTags,
                    selectedTags = state.selectedTags,
                    onDismiss = onHideTagSelector,
                    onTagToggle = { onToggleTagSelection(it) },
                    onAddNewTag = { onAddNewTag(it) }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddNotePreview() {
    QuickNotesTheme {
        AddEditNoteScreen(
            state = AddEditNoteUiState(
                title = "",
                content = "",
                isPinned = false,
                noteId = null
            ),
            onTitleChange = {},
            onContentChange = {},
            onTogglePinned = {},
            onSave = {},
            onBack = {},
            onDelete = null,
            onShowTagSelector = {},
            onHideTagSelector = {},
            onToggleTagSelection = {},
            onAddNewTag = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun EditNotePreview() {
    QuickNotesTheme {
        AddEditNoteScreen(
            state = AddEditNoteUiState(
                title = "Meeting Notes",
                content = "Discuss the new project requirements and timelines.",
                isPinned = true,
                noteId = 1L,
                selectedTags = listOf(
                    Tag(1, "Work"),
                    Tag(2, "Urgent")
                )
            ),
            onTitleChange = {},
            onContentChange = {},
            onTogglePinned = {},
            onSave = {},
            onBack = {},
            onDelete = {},
            onShowTagSelector = {},
            onHideTagSelector = {},
            onToggleTagSelection = {},
            onAddNewTag = {}
        )
    }
}
