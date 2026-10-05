package com.nexora.reminder.voice

import android.content.Context
import com.nexora.reminder.auth.SessionManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

sealed interface TtsResult {
    data class Success(val wavBytes: ByteArray, val previewPath: String) : TtsResult
    data class Error(val message: String) : TtsResult
}

@Singleton
class NexoraTtsApi @Inject constructor(
    @ApplicationContext private val context: Context,
    private val session: SessionManager
) {
    companion object {
        const val BASE_URL = "https://nexora-navy-omega.vercel.app"
        const val TTS_PATH = "/api/tts"
        const val DEFAULT_LANGUAGE = "en-US"
        const val DEFAULT_VOICE = "Magpie-Multilingual.EN-US.Aria"
        const val MAX_CHARS = 2000
        // Fixed, non-editable intro — always spoken first. Users cannot change this.
        const val INTRO_PREFIX = "Hello! Nexora Voice Assistance Speaking: "

        fun withIntro(userText: String): String {
            val clean = userText.trim()
            // avoid double-prefix if pasted
            return if (clean.startsWith(INTRO_PREFIX, ignoreCase = true)) clean
            else INTRO_PREFIX + clean
        }
    }

    suspend fun synthesize(
        text: String,
        language: String = DEFAULT_LANGUAGE,
        voice: String = DEFAULT_VOICE
    ): TtsResult = withContext(Dispatchers.IO) {
        val user = text.trim()
        if (user.isEmpty()) return@withContext TtsResult.Error("Voice text is required.")
        if (user.length > 200) return@withContext TtsResult.Error("Voice text max 200 chars.")
        // Locked intro — always prepended server-side, never user-editable
        val t = withIntro(user)
        if (t.length > MAX_CHARS) return@withContext TtsResult.Error("Voice text too long (max $MAX_CHARS).")
        val token = session.session.value.token
        if (token.isBlank()) return@withContext TtsResult.Error("Please sign in again.")
        try {
            val url = URL(BASE_URL + TTS_PATH)
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 30_000
                readTimeout = 60_000
                doOutput = true
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Authorization", "Bearer $token")
            }
            val body = JSONObject()
                .put("text", t)
                .put("language", language.ifBlank { DEFAULT_LANGUAGE })
                .put("voice", voice.ifBlank { DEFAULT_VOICE })
                .put("sampleRate", 44100)
                .toString()
            conn.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
            val code = conn.responseCode
            if (code !in 200..299) {
                val err = try { conn.errorStream?.bufferedReader()?.readText() ?: "" } catch (_: Exception) { "" }
                val msg = runCatching { JSONObject(err).optString("message", "") }.getOrDefault("")
                return@withContext TtsResult.Error(
                    when (code) {
                        401 -> "Session expired. Please sign in again."
                        403 -> "Voice is disabled for this account."
                        429 -> "Too many requests. Wait and retry."
                        else -> msg.ifBlank { "Voice conversion failed ($code)." }
                    }
                )
            }
            val bytes = conn.inputStream.use { it.readBytes() }
            if (bytes.isEmpty()) return@withContext TtsResult.Error("Voice conversion failed (empty audio).")
            val preview = File(context.cacheDir, "voice_preview_${System.currentTimeMillis()}.wav")
            preview.writeBytes(bytes)
            TtsResult.Success(bytes, preview.absolutePath)
        } catch (e: java.net.UnknownHostException) {
            TtsResult.Error("No internet connection.")
        } catch (e: java.net.SocketTimeoutException) {
            TtsResult.Error("Request timed out. Please retry.")
        } catch (e: Exception) {
            TtsResult.Error("Voice conversion failed. Please retry.")
        }
    }
}
