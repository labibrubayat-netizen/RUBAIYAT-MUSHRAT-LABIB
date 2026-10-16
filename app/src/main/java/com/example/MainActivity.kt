package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.navigation.AcademicAppScaffold
import com.example.ui.theme.MyApplicationTheme
import com.example.util.NotificationHelper
import com.example.viewmodel.AcademicViewModel

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    // Initialize notification channel for class reminders
    NotificationHelper.createNotificationChannel(this)

    setContent {
      MyApplicationTheme {
        val viewModel: AcademicViewModel = viewModel()
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()

        Surface(modifier = Modifier.fillMaxSize()) {
          AcademicAppScaffold(
            viewModel = viewModel,
            uiState = uiState
          )
        }
      }
    }
  }
}
