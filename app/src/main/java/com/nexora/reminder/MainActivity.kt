package com.nexora.reminder

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.nexora.reminder.auth.SessionManager
import com.nexora.reminder.presentation.auth.LoginScreen
import com.nexora.reminder.presentation.dashboard.DashboardScreen
import com.nexora.reminder.ui.theme.ReminderTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ReminderTheme {
                val session by sessionManager.session.collectAsState()
                if (session.loggedIn) {
                    DashboardScreen()
                } else {
                    LoginScreen()
                }
            }
        }
    }
}
