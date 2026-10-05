package com.nexora.reminder.voice

import android.content.Context
import android.media.MediaPlayer
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VoicePlayer @Inject constructor(
    @ApplicationContext private val context: Context
) {
    @Volatile private var player: MediaPlayer? = null

    @Synchronized
    fun playFile(path: String, onDone: (() -> Unit)? = null) {
        stop()
        try {
            val f = File(path)
            if (!f.exists()) return
            val mp = MediaPlayer().apply {
                setDataSource(f.absolutePath)
                setOnCompletionListener {
                    try { stop(); release() } catch (_: Exception) { }
                    if (player === it) player = null
                    onDone?.invoke()
                }
                setOnErrorListener { p, _, _ ->
                    try { p.release() } catch (_: Exception) { }
                    if (player === p) player = null
                    onDone?.invoke()
                    true
                }
                prepare()
                start()
            }
            player = mp
        } catch (_: Exception) { onDone?.invoke() }
    }

    @Synchronized
    fun playBytes(wav: ByteArray, onDone: (() -> Unit)? = null) {
        try {
            val tmp = File(context.cacheDir, "voice_preview_${System.currentTimeMillis()}.wav")
            tmp.writeBytes(wav)
            playFile(tmp.absolutePath, onDone)
        } catch (_: Exception) { onDone?.invoke() }
    }

    @Synchronized
    fun stop() {
        try { player?.stop() } catch (_: Exception) { }
        try { player?.release() } catch (_: Exception) { }
        player = null
    }
}
