package com.notebee

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class required for Hilt dependency injection.
 * @HiltAndroidApp triggers Hilt's code generation and sets up the application-level dependency container.
 */
@HiltAndroidApp
class QuickNotesApplication : Application()
