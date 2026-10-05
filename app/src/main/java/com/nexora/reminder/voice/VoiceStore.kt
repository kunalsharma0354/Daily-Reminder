package com.nexora.reminder.voice

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VoiceStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private fun dir(): File = File(context.filesDir, "voices").apply { mkdirs() }

    fun fileFor(reminderId: Long): File = File(dir(), "voice_$reminderId.wav")

    fun save(reminderId: Long, wav: ByteArray): String {
        val f = fileFor(reminderId)
        f.writeBytes(wav)
        return f.absolutePath
    }

    fun saveFromPreview(reminderId: Long, previewPath: String): String? {
        return try {
            val src = File(previewPath)
            if (!src.exists()) return null
            val dst = fileFor(reminderId)
            src.copyTo(dst, overwrite = true)
            dst.absolutePath
        } catch (_: Exception) { null }
    }

    fun pathFor(reminderId: Long): String? {
        val f = fileFor(reminderId)
        return if (f.exists() && f.length() > 0) f.absolutePath else null
    }

    fun delete(reminderId: Long) {
        try { fileFor(reminderId).delete() } catch (_: Exception) { }
    }
}
