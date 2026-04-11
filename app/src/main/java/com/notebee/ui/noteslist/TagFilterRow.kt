package com.notebee.ui.noteslist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.notebee.data.local.entity.Tag

/**
 * Horizontal scrollable row of tag filter chips.
 * Shows "All" option to clear filter, then lists all available tags.
 */
@Composable
fun TagFilterRow(
    allTags: List<Tag>,
    selectedTag: String?,
    onTagSelected: (String?) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            FilterChip(
                selected = selectedTag == null,
                onClick = { onTagSelected(null) },
                label = { 
                    Text(
                        text = "All",
                        style = MaterialTheme.typography.labelMedium
                    ) 
                }
            )
        }
        
        items(allTags, key = { it.id }) { tag ->
            FilterChip(
                selected = selectedTag == tag.name,
                onClick = { onTagSelected(tag.name) },
                label = { 
                    Text(
                        text = tag.name,
                        style = MaterialTheme.typography.labelMedium
                    ) 
                }
            )
        }
    }
}
