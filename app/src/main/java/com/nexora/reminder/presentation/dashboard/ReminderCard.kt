package com.nexora.reminder.presentation.dashboard

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.nexora.reminder.domain.model.DayStatus
import com.nexora.reminder.domain.model.Reminder
import com.nexora.reminder.ui.theme.ReminderTokens
import com.nexora.reminder.util.ReminderConstants

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ReminderCard(
    reminder: Reminder,
    onDone: () -> Unit,
    onSkip: () -> Unit,
    onDelete: () -> Unit,
    onIntervalChange: (Long) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val completed = reminder.todayStatus == DayStatus.COMPLETED
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        if (pressed) 0.97f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium, dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "press"
    )
    val cardColor by animateColorAsState(
        if (completed) MaterialTheme.colorScheme.surfaceVariant
        else MaterialTheme.colorScheme.surface,
        label = "cardColor"
    )
    val every = ReminderConstants.formatInterval(reminder.intervalMinutes)
    val skipLabel = if (reminder.isDailyTime) "Snooze 30m" else "Skip $every"

    Card(
        modifier = modifier.fillMaxWidth().scale(pressScale),
        shape = ReminderTokens.CardRadius,
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (completed) 0.dp else 3.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val iconScale by animateFloatAsState(
                    if (completed) 1.1f else 1f,
                    animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow),
                    label = "icon"
                )
                Icon(
                    if (completed) Icons.Filled.CheckCircle else Icons.Filled.Notifications,
                    contentDescription = if (completed) "Completed" else "Active, every $every",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(30.dp).scale(iconScale)
                )
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        reminder.taskName,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                        textDecoration = if (completed) TextDecoration.LineThrough else null,
                        color = if (completed) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                    )
                    if (reminder.customTitle.isNotBlank()) {
                        Text(
                            reminder.customTitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Schedule, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.width(4.dp))
                        val sub = when {
                            reminder.isDailyTime -> "Daily at ${com.nexora.reminder.util.ReminderConstants.formatDailyTime(reminder.dailyTimeMinutes)}"
                            reminder.hasVoice -> "Every $every • Voice on"
                            else -> "Every $every"
                        }
                        Text(
                            sub,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                AssistChip(
                    onClick = {},
                    label = {
                        Text(
                            if (reminder.isRepeat) "Repeat"
                            else if (reminder.isDailyTime) "Daily"
                            else if (completed) "Done" else "Live",
                            fontWeight = FontWeight.Bold
                        )
                    },
                    leadingIcon = if (completed) {
                        { Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    } else null,
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = if (completed) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                        else MaterialTheme.colorScheme.primary,
                        labelColor = if (completed) MaterialTheme.colorScheme.onSurfaceVariant
                        else MaterialTheme.colorScheme.onPrimary
                    ),
                    border = if (completed) BorderStroke(1.dp, MaterialTheme.colorScheme.outline) else null
                )
                IconButton(onClick = onDelete, modifier = Modifier.size(48.dp)) {
                    Icon(Icons.Filled.Delete, contentDescription = "Delete ${reminder.taskName}")
                }
            }
            if (reminder.isRepeat) {
                Spacer(Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = onSkip,
                        modifier = Modifier.weight(1f),
                        shape = ReminderTokens.Pill
                    ) { Text(skipLabel) }
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    "Repeat mode — rings every $every, no completion needed",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(10.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ReminderConstants.PRESETS_MIN.forEach { preset ->
                        val selected = preset == reminder.intervalMinutes
                        val chipScale by animateFloatAsState(if (selected) 1.05f else 1f, label = "chip")
                        AssistChip(
                            modifier = Modifier.scale(chipScale),
                            onClick = { if (!selected) onIntervalChange(preset) },
                            label = { Text(ReminderConstants.formatInterval(preset)) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = if (selected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant,
                                labelColor = if (selected) MaterialTheme.colorScheme.onPrimary
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            } else if (!completed) {
                Spacer(Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = onDone,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = ReminderTokens.Pill
                    ) { Text("Work Done", fontWeight = FontWeight.Bold) }
                    OutlinedButton(
                        onClick = onSkip,
                        modifier = Modifier.weight(1f),
                        shape = ReminderTokens.Pill
                    ) { Text(skipLabel) }
                }
                if (!reminder.isDailyTime) {
                Spacer(Modifier.height(10.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ReminderConstants.PRESETS_MIN.forEach { preset ->
                        val selected = preset == reminder.intervalMinutes
                        val chipScale by animateFloatAsState(if (selected) 1.05f else 1f, label = "chip")
                        AssistChip(
                            modifier = Modifier.scale(chipScale),
                            onClick = { if (!selected) onIntervalChange(preset) },
                            label = { Text(ReminderConstants.formatInterval(preset)) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = if (selected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant,
                                labelColor = if (selected) MaterialTheme.colorScheme.onPrimary
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
                }
            } else {
                Spacer(Modifier.height(6.dp))
                Text(
                    "Completed for today",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
