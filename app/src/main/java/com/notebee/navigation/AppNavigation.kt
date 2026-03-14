package com.notebee.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.hilt.navigation.compose.hiltViewModel
import com.notebee.ui.addeditnote.AddEditNoteScreen
import com.notebee.ui.addeditnote.AddEditNoteViewModel
import com.notebee.ui.noteslist.NotesListScreen
import com.notebee.ui.noteslist.NotesListViewModel

/**
 * Route names for type-safe navigation.
 */


object Routes {
    const val NOTES_LIST = "notes_list"
    const val ADD_EDIT_NOTE = "add_edit_note"
    const val NOTE_ID = "noteId"

    fun addEditNote(noteId: Long? = null): String =
        if (noteId != null) "$ADD_EDIT_NOTE/$noteId" else ADD_EDIT_NOTE
}

/**
 * Main navigation graph: NotesListScreen and AddEditNoteScreen (with optional noteId).
 */
@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Routes.NOTES_LIST
    ) {
        composable(Routes.NOTES_LIST) {
            val viewModel: NotesListViewModel = hiltViewModel()
            val state by viewModel.state.collectAsState()
            NotesListScreen(
                state = state,
                onSearchQueryChange = viewModel::updateSearchQuery,
                onNoteClick = { id -> navController.navigate(Routes.addEditNote(id)) },
                onDeleteNote = viewModel::deleteNote,
                onTogglePin = viewModel::togglePin,
                onAddNote = { navController.navigate(Routes.ADD_EDIT_NOTE) }
            )
        }

        composable(Routes.ADD_EDIT_NOTE) {
            val viewModel: AddEditNoteViewModel = hiltViewModel()
            val state by viewModel.state.collectAsState()
            AddEditNoteScreen(
                state = state,
                onTitleChange = viewModel::updateTitle,
                onContentChange = viewModel::updateContent,
                onTogglePinned = viewModel::togglePinned,
                onSave = { viewModel.saveNote { navController.popBackStack() } },
                onBack = { navController.popBackStack() },
                onDelete = null
            )
        }

        composable(
            route = "${Routes.ADD_EDIT_NOTE}/{${Routes.NOTE_ID}}",
            arguments = listOf(
                navArgument(Routes.NOTE_ID) { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val viewModel: AddEditNoteViewModel = hiltViewModel(backStackEntry)
            val state by viewModel.state.collectAsState()
            AddEditNoteScreen(
                state = state,
                onTitleChange = viewModel::updateTitle,
                onContentChange = viewModel::updateContent,
                onTogglePinned = viewModel::togglePinned,
                onSave = { viewModel.saveNote { navController.popBackStack() } },
                onBack = { navController.popBackStack() },
                onDelete = {
                    viewModel.deleteNote { navController.popBackStack() }
                }
            )
        }
    }
}
