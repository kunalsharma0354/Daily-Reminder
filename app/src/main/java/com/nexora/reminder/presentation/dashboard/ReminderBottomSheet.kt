package com.nexora.reminder.presentation.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nexora.reminder.presentation.voice.VoiceViewModel
import com.nexora.reminder.ui.theme.ReminderTokens
import com.nexora.reminder.util.ReminderConstants
import com.nexora.reminder.voice.NexoraTtsApi
import kotlinx.coroutines.launch
import kotlin.math.roundToLong

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun ReminderBottomSheet(
    onDismiss: () -> Unit,
    onSave: (
        name: String, title: String, intervalMin: Long,
        voiceText: String, voicePreviewPath: String,
        mode: com.nexora.reminder.domain.model.ReminderMode,
        dailyTimeMinutes: Int,
        done: (Boolean, String?) -> Unit
    ) -> Unit,
    voiceVm: VoiceViewModel = hiltViewModel()
) {
    var name by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var interval by remember { mutableLongStateOf(30L) }
    var voiceText by remember { mutableStateOf("") }
    var mode by remember { mutableStateOf(com.nexora.reminder.domain.model.ReminderMode.ONCE) }
    var dailyTime by remember { mutableStateOf(540) }
    var showTimePicker by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val voiceUi by voiceVm.ui.collectAsState()
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState, shape = ReminderTokens.SheetRadius) {
        Column(
            Modifier.padding(22.dp).fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            Text("New reminder", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold))
            Spacer(Modifier.height(4.dp))
            Text(
                when (mode) {
                    com.nexora.reminder.domain.model.ReminderMode.REPEAT ->
                        "First reminder in 1 minute, then repeats at your interval. No completion needed."
                    com.nexora.reminder.domain.model.ReminderMode.DAILY_TIME ->
                        "One notification every day at your chosen time."
                    else ->
                        "First reminder in 1 minute, then repeats until marked as done."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                listOf(
                    com.nexora.reminder.domain.model.ReminderMode.ONCE to "Once a day",
                    com.nexora.reminder.domain.model.ReminderMode.REPEAT to "Repeat",
                    com.nexora.reminder.domain.model.ReminderMode.DAILY_TIME to "Daily time"
                ).forEach { (m, label) ->
                    val selected = m == mode
                    AssistChip(
                        onClick = { mode = m },
                        label = { Text(label, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            labelColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
            Text(
                when (mode) {
                    com.nexora.reminder.domain.model.ReminderMode.REPEAT ->
                        "Repeat: rings every interval (e.g. Drink Water). No Work Done button."
                    com.nexora.reminder.domain.model.ReminderMode.DAILY_TIME ->
                        "Daily time: one notification each day at the selected time."
                    else ->
                        "Once a day: requires Work Done. Counts in stats and history."
                },
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = name,
                onValueChange = { name = it; error = null },
                label = { Text("Task name * e.g. Workout, Read 30 min") },
                singleLine = true,
                isError = error != null,
                modifier = Modifier.fillMaxWidth(),
                shape = ReminderTokens.CardRadiusSmall
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Custom notification title (optional)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = ReminderTokens.CardRadiusSmall
            )
            Spacer(Modifier.height(18.dp))
            if (mode == com.nexora.reminder.domain.model.ReminderMode.DAILY_TIME) {
                Text(
                    "Daily time",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                Text(
                    "One notification every day at this time.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { showTimePicker = true },
                    shape = ReminderTokens.Pill,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.Schedule, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text("  ${ReminderConstants.formatDailyTime(dailyTime)}")
                }
                Spacer(Modifier.height(18.dp))
            } else {
            Text(
                "Repeat every ${ReminderConstants.formatInterval(interval)}",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            )
            Text(
                "Repeat interval: 30 minutes to 6 hours",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ReminderConstants.PRESETS_MIN.forEach { preset ->
                    val selected = preset == interval
                    val sc by animateFloatAsState(
                        if (selected) 1.06f else 1f,
                        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium),
                        label = "preset"
                    )
                    AssistChip(
                        modifier = Modifier.scale(sc),
                        onClick = { interval = preset },
                        label = { Text(ReminderConstants.formatInterval(preset), fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            labelColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Slider(
                value = interval.toFloat(),
                onValueChange = {
                    val stepped = ((it / 15f).roundToLong() * 15L).coerceIn(
                        ReminderConstants.MIN_INTERVAL_MIN, ReminderConstants.MAX_INTERVAL_MIN
                    )
                    interval = stepped
                },
                valueRange = ReminderConstants.MIN_INTERVAL_MIN.toFloat()..ReminderConstants.MAX_INTERVAL_MIN.toFloat(),
                steps = ((ReminderConstants.MAX_INTERVAL_MIN - ReminderConstants.MIN_INTERVAL_MIN) / 15).toInt() - 1,
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = MaterialTheme.colorScheme.outlineVariant
                )
            )
            RowLabels()
            } // end interval block (hidden for DAILY_TIME)

            Spacer(Modifier.height(18.dp))
            Text(
                "Voice message (optional)",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            )
            Text(
                "Spoken with each notification. English (en-US) by default.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            AssistChip(
                onClick = {},
                label = { Text("English (en-US)") },
                leadingIcon = { Icon(Icons.Filled.GraphicEq, contentDescription = null, modifier = Modifier.size(16.dp)) }
            )
            Spacer(Modifier.height(8.dp))
            AssistChip(
                onClick = {},
                enabled = false,
                label = { Text("Hello! Nexora Voice Assistance Speaking:") },
                leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null, modifier = Modifier.size(16.dp)) }
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Locked intro — always spoken first, cannot be changed. Type your message below.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = voiceText,
                onValueChange = {
                    if (it.length <= 200) {
                        voiceText = it
                        error = null
                        if (voiceUi.ready) voiceVm.clear()
                    }
                },
                label = { Text("e.g. it's time to drink water") },
                supportingText = { Text("${voiceText.length}/200") },
                modifier = Modifier.fillMaxWidth(),
                shape = ReminderTokens.CardRadiusSmall,
                minLines = 2,
                maxLines = 3
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedButton(
                    onClick = {
                        scope.launch {
                            voiceVm.convertAndPlay(voiceText)
                        }
                    },
                    enabled = voiceText.isNotBlank() && !voiceUi.converting,
                    modifier = Modifier.weight(1f),
                    shape = ReminderTokens.Pill
                ) {
                    if (voiceUi.converting) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        Text("  Converting…")
                    } else {
                        Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                        Text(" Convert to voice")
                    }
                }
                if (voiceUi.ready) {
                    OutlinedButton(
                        onClick = { voiceVm.playPreview() },
                        shape = ReminderTokens.Pill
                    ) { Text("Replay") }
                }
            }
            AnimatedVisibility(voiceUi.ready) {
                Column {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Voice ready. It will play with each notification and is saved automatically.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            AnimatedVisibility(voiceUi.error != null) {
                Column {
                    Spacer(Modifier.height(6.dp))
                    Text(voiceUi.error ?: "", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                }
            }

            if (error != null) {
                Spacer(Modifier.height(8.dp))
                AnimatedVisibility(error != null) {
                    Text(error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                }
            }
            Spacer(Modifier.height(18.dp))
            Button(
                onClick = {
                    if (name.trim().isEmpty() || name.trim().length > 50) {
                        error = "Task name required (1-50 chars)"
                        return@Button
                    }
                    if (title.length > 40) {
                        error = "Custom title max 40 chars"
                        return@Button
                    }
                    if (voiceText.length > 200) {
                        error = "Voice text max 200 chars"
                        return@Button
                    }
                    onSave(name.trim(), title.trim(), interval, voiceText.trim(), voiceUi.previewPath, mode, dailyTime) { ok, msg ->
                        if (ok) onDismiss() else error = msg ?: "Could not save"
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = ReminderTokens.Pill,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    if (mode == com.nexora.reminder.domain.model.ReminderMode.DAILY_TIME)
                        "Start daily alarm • ${ReminderConstants.formatDailyTime(dailyTime)}"
                    else
                        "Start reminders • Every ${ReminderConstants.formatInterval(interval)}",
                    fontWeight = FontWeight.Bold
                )
            }
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text("Cancel") }
            Spacer(Modifier.height(18.dp))
        }
    }
    if (showTimePicker) {
        DailyTimePickerDialog(
            initialMinutes = dailyTime,
            onDismiss = { showTimePicker = false },
            onConfirm = { h, m -> dailyTime = h * 60 + m; showTimePicker = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DailyTimePickerDialog(
    initialMinutes: Int,
    onDismiss: () -> Unit,
    onConfirm: (hour: Int, minute: Int) -> Unit
) {
    val state = rememberTimePickerState(
        initialHour = (initialMinutes / 60).coerceIn(0, 23),
        initialMinute = (initialMinutes % 60).coerceIn(0, 59),
        is24Hour = false
    )
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onConfirm(state.hour, state.minute) }) { Text("Set time") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        title = { Text("Select daily time") },
        text = {
            androidx.compose.material3.TimePicker(state = state)
        }
    )
}

@Composable
private fun RowLabels() {
    androidx.compose.foundation.layout.Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text("30m", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text("3h", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text("6h", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
