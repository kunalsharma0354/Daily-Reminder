package com.nexora.reminder.auth

import android.content.Context
import android.provider.Settings
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

sealed interface LoginResult {
    data class Success(val token: String, val username: String, val expiresAt: Long) : LoginResult
    data class Error(val message: String) : LoginResult
}

@Singleton
class NexoraAuthApi @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        const val BASE_URL = "https://nexora-navy-omega.vercel.app"
        const val LOGIN_PATH = "/api/auth-login"
        const val SITE_ID = "remindernexora"
    }

    fun deviceId(): String {
        return runCatching {
            Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) ?: "android-device"
        }.getOrDefault("android-device").ifBlank { "android-device" }
    }

    suspend fun login(username: String, password: String): LoginResult = withContext(Dispatchers.IO) {
        val u = username.trim()
        val p = password
        if (u.isEmpty()) return@withContext LoginResult.Error("Username is required.")
        if (p.isEmpty()) return@withContext LoginResult.Error("Password is required.")
        try {
            val url = URL(BASE_URL + LOGIN_PATH)
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 15_000
                readTimeout = 15_000
                doOutput = true
                setRequestProperty("Content-Type", "application/json")
            }
            val body = JSONObject()
                .put("username", u)
                .put("password", p)
                .put("hwid", deviceId())
                .put("siteId", SITE_ID)
                .toString()
            conn.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
            val code = conn.responseCode
            val text = try {
                (if (code in 200..299) conn.inputStream else conn.errorStream)?.bufferedReader()?.readText() ?: ""
            } catch (_: Exception) { "" }
            if (text.isBlank()) return@withContext LoginResult.Error("Login failed (empty response).")
            val json = JSONObject(text)
            val ok = json.optBoolean("ok", false)
            if (!ok) {
                val msg = json.optString("message", "").ifBlank { "Wrong username or password." }
                return@withContext LoginResult.Error(msg)
            }
            val token = json.optString("token", "")
            if (token.isBlank()) return@withContext LoginResult.Error("Login failed (no token).")
            val user = json.optJSONObject("user")
            val name = user?.optString("username", u)?.ifBlank { u } ?: u
            val expiresAt = user?.optLong("expiry", 0L) ?: 0L
            LoginResult.Success(token, name, expiresAt)
        } catch (e: java.net.UnknownHostException) {
            LoginResult.Error("No internet connection.")
        } catch (e: java.net.SocketTimeoutException) {
            LoginResult.Error("Request timed out. Please retry.")
        } catch (e: Exception) {
            LoginResult.Error("Login failed. Please retry.")
        }
    }
}
