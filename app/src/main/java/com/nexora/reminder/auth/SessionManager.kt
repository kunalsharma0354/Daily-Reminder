package com.nexora.reminder.auth

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

data class Session(val token: String = "", val username: String = "", val expiresAt: Long = 0L) {
    val loggedIn: Boolean get() = token.isNotBlank()
}

@Singleton
class SessionManager @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs = context.getSharedPreferences("nexora_auth", Context.MODE_PRIVATE)

    private val _session = MutableStateFlow(load())
    val session: StateFlow<Session> = _session.asStateFlow()

    private fun load(): Session = Session(
        token = prefs.getString("token", "") ?: "",
        username = prefs.getString("username", "") ?: "",
        expiresAt = prefs.getLong("expiresAt", 0L)
    )

    fun save(token: String, username: String, expiresAt: Long) {
        prefs.edit()
            .putString("token", token)
            .putString("username", username)
            .putLong("expiresAt", expiresAt)
            .apply()
        _session.value = Session(token, username, expiresAt)
    }

    fun clear() {
        prefs.edit().clear().apply()
        _session.value = Session()
    }
}
