package com.notebee

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.notebee.navigation.AppNavigation
import com.notebee.ui.theme.NoteBeePrimary
import com.notebee.ui.theme.QuickNotesTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Single-activity entry point. Hilt injects dependencies; Compose sets the navigation and theme.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Install splash screen before super.onCreate()
        installSplashScreen()

        super.onCreate(savedInstanceState)

        // Disable default edge-to-edge as we'll handle status bar in QuickNotesTheme
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                NoteBeePrimary.toArgb(),
                NoteBeePrimary.toArgb()
            )
        )

        setContent {
            QuickNotesTheme {
                var showUpdateDialog by remember { mutableStateOf(false) }

                LaunchedEffect(Unit) {
                    if (checkIfUpdateNeeded()) {
                        showUpdateDialog = true
                    }
                }

                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavigation()

                    if (showUpdateDialog) {
                        UpdateNeededDialog(onDismiss = { showUpdateDialog = false })
                    }
                }
            }
        }
    }

    /**
     * Checks if the app needs an update. 
     * In a real app, this would compare BuildConfig.VERSION_CODE with a value from a remote server (e.g., Firebase Remote Config).
     */
    private fun checkIfUpdateNeeded(): Boolean {
        val currentVersion = BuildConfig.VERSION_CODE
        val requiredVersion = 5 // Example required version
        return currentVersion < requiredVersion
    }
}

@Composable
fun UpdateNeededDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = { /* Prevent dismissal if update is mandatory */ },
        title = { Text(text = "Update Required") },
        text = { Text(text = "A new version of NoteBee is available. Please update to continue using the app.") },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("OK")
            }
        }
    )
}
