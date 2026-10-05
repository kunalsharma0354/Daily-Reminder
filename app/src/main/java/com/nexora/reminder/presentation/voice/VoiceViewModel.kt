package com.nexora.reminder.presentation.voice

import androidx.lifecycle.ViewModel
import com.nexora.reminder.voice.NexoraTtsApi
import com.nexora.reminder.voice.TtsResult
import com.nexora.reminder.voice.VoicePlayer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class VoiceUiState(
    val converting: Boolean = false,
    val previewPath: String = "",
    val error: String? = null,
    val ready: Boolean = false
)

@HiltViewModel
class VoiceViewModel @Inject constructor(
    private val api: NexoraTtsApi,
    private val player: VoicePlayer
) : ViewModel() {
    private val _ui = MutableStateFlow(VoiceUiState())
    val ui: StateFlow<VoiceUiState> = _ui.asStateFlow()

    suspend fun convertAndPlay(text: String): Boolean {
        _ui.value = VoiceUiState(converting = true)
        return when (val r = api.synthesize(text, NexoraTtsApi.DEFAULT_LANGUAGE, NexoraTtsApi.DEFAULT_VOICE)) {
            is TtsResult.Success -> {
                player.playBytes(r.wavBytes)
                _ui.value = VoiceUiState(previewPath = r.previewPath, ready = true)
                true
            }
            is TtsResult.Error -> {
                _ui.value = VoiceUiState(error = r.message)
                false
            }
        }
    }

    fun playPreview() {
        val p = _ui.value.previewPath
        if (p.isNotBlank()) player.playFile(p)
    }

    fun clear() {
        _ui.value = VoiceUiState()
    }

    override fun onCleared() {
        player.stop()
        super.onCleared()
    }
}
