package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.TimetableViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: TimetableViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainScaffold(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainScaffold(viewModel: TimetableViewModel) {
    val selectedRole by viewModel.selectedRole.collectAsStateWithLifecycle()
    var currentSubDestination by remember { mutableStateOf("dashboard") }

    Box(modifier = Modifier.fillMaxSize()) {
        when (selectedRole) {
            null -> {
                // Force reset state when logging out
                currentSubDestination = "dashboard"
                LoginScreen(viewModel = viewModel)
            }
            "Principal", "Vice Principal", "Timetable Admin" -> {
                when (currentSubDestination) {
                    "dashboard" -> {
                        PrincipalDashboard(
                            viewModel = viewModel,
                            onNavigateToGrid = { currentSubDestination = "grid" },
                            onNavigateToData = { currentSubDestination = "data" },
                            onNavigateToAi = { currentSubDestination = "ai" },
                            onLogout = { viewModel.selectRole(null) }
                        )
                    }
                    "grid" -> {
                        MasterGridScreen(
                            viewModel = viewModel,
                            onBack = { currentSubDestination = "dashboard" }
                        )
                    }
                    "data" -> {
                        DataManagementScreen(
                            viewModel = viewModel,
                            onBack = { currentSubDestination = "dashboard" }
                        )
                    }
                    "ai" -> {
                        AiAssistantScreen(
                            viewModel = viewModel,
                            onBack = { currentSubDestination = "dashboard" }
                        )
                    }
                }
            }
            "Teacher" -> {
                TeacherDashboard(
                    viewModel = viewModel,
                    onLogout = { viewModel.selectRole(null) }
                )
            }
            "Student / Parent", "Guest Demo" -> {
                StudentDashboard(
                    viewModel = viewModel,
                    onLogout = { viewModel.selectRole(null) }
                )
            }
            else -> {
                // Fallback
                LoginScreen(viewModel = viewModel)
            }
        }
    }
}
