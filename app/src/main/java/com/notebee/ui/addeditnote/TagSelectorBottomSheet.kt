package com.notebee.ui.addeditnote

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.notebee.data.local.entity.Tag
import com.notebee.ui.theme.QuickNotesTheme
import kotlinx.coroutines.launch

/**
 * Modal bottom sheet for selecting tags.
 * Shows all available tags with checkboxes and option to create new tags.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagSelectorBottomSheet(
    isVisible: Boolean,
    allTags: List<Tag>,
    selectedTags: List<Tag>,
    onDismiss: () -> Unit,
    onTagToggle: (Tag) -> Unit,
    onAddNewTag: (String) -> Unit
) {
    if (isVisible) {
        val sheetState = rememberModalBottomSheetState(
            skipPartiallyExpanded = true
        )
        val scope = rememberCoroutineScope()

        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState,
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
        ) {
            TagSelectorContent(
                allTags = allTags,
                selectedTags = selectedTags,
                onClose = {
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        onDismiss()
                    }
                },
                onTagToggle = onTagToggle,
                onAddNewTag = onAddNewTag
            )
        }
    }
}

/**
 * Content of the tag selector.
 * Extracted to allow for easier previewing and testing.
 */
@Composable
fun TagSelectorContent(
    allTags: List<Tag>,
    selectedTags: List<Tag>,
    onClose: () -> Unit,
    onTagToggle: (Tag) -> Unit,
    onAddNewTag: (String) -> Unit
) {
    var newTagName by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Select Tags",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Create new tag section
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "Create New Tag",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = newTagName,
                        onValueChange = { newTagName = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Tag name") },
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )
                    
                    Button(
                        onClick = {
                            if (newTagName.isNotBlank()) {
                                onAddNewTag(newTagName.trim())
                                newTagName = ""
                            }
                        },
                        enabled = newTagName.isNotBlank()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add tag")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Existing tags list
        if (allTags.isNotEmpty()) {
            Text(
                text = "Existing Tags",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            LazyColumn(
                modifier = Modifier.heightIn(max = 200.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(allTags) { tag ->
                    val isSelected = selectedTags.any { it.id == tag.id }
                    FilterChip(
                        selected = isSelected,
                        onClick = { onTagToggle(tag) },
                        label = { Text(tag.name) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No tags yet. Create your first tag above!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onClose) {
                Text("Done")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TagSelectorBottomSheetPreview() {
    val sampleTags = listOf(
        Tag(id = 1, name = "Work"),
        Tag(id = 2, name = "Personal"),
        Tag(id = 3, name = "Ideas"),
        Tag(id = 4, name = "Urgent")
    )
    val selectedTags = listOf(sampleTags[0], sampleTags[2])

    QuickNotesTheme {
        Surface {
            TagSelectorContent(
                allTags = sampleTags,
                selectedTags = selectedTags,
                onClose = {},
                onTagToggle = {},
                onAddNewTag = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TagSelectorBottomSheetEmptyPreview() {
    QuickNotesTheme {
        Surface {
            TagSelectorContent(
                allTags = emptyList(),
                selectedTags = emptyList(),
                onClose = {},
                onTagToggle = {},
                onAddNewTag = {}
            )
        }
    }
}
