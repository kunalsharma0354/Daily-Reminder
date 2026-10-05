package com.nexora.reminder.presentation.dashboard

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nexora.reminder.domain.model.HeatState
import com.nexora.reminder.domain.model.HeatmapDay
import com.nexora.reminder.ui.theme.ReminderTokens
import java.time.LocalDate

@Composable
fun HeatmapSection(days: List<HeatmapDay>, modifier: Modifier = Modifier) {
    val dark = isSystemInDarkTheme()
    Card(
        modifier.fillMaxWidth(),
        shape = ReminderTokens.CardRadius,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(Modifier.padding(18.dp)) {
            Text("Last 16 weeks", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            Spacer(Modifier.height(4.dp))
            Text(
                "Completed • Missed • Inactive — today highlighted",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))
            if (days.isEmpty()) {
                Text("No history yet — complete your first task!", style = MaterialTheme.typography.bodyMedium)
            } else {
                val weeks = days.chunked(7)
                val state = rememberLazyListState(initialFirstVisibleItemIndex = (weeks.size - 8).coerceAtLeast(0))
                LaunchedEffect(days.size) {
                    if (weeks.isNotEmpty()) state.scrollToItem(weeks.size - 1)
                }
                LazyRow(horizontalArrangement = Arrangement.spacedBy(5.dp), state = state) {
                    items(weeks) { week ->
                        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                            week.forEach { day -> HeatCell(day, dark) }
                            repeat((7 - week.size).coerceAtLeast(0)) { EmptyCell() }
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                LegendRow(dark)
            }
        }
    }
}

@Composable
private fun HeatCell(day: HeatmapDay, dark: Boolean) {
    val infinite = rememberInfiniteTransition(label = "pulse")
    val pulse by infinite.animateFloat(
        initialValue = 1f, targetValue = if (day.isToday) 1.18f else 1f,
        animationSpec = infiniteRepeatable(tween(1600), RepeatMode.Reverse),
        label = "todayPulse"
    )
    val base: Color = when (day.state) {
        HeatState.COMPLETED, HeatState.TODAY_COMPLETED ->
            if (dark) Color.White else Color.Black
        HeatState.MISSED ->
            if (dark) Color(0xFF525252) else Color(0xFFA3A3A3)
        HeatState.INACTIVE ->
            if (dark) Color(0xFF1F1F1F) else Color(0xFFF0F0F0)
        HeatState.TODAY_PENDING ->
            if (dark) Color(0xFF404040) else Color(0xFFD4D4D4)
    }
    val dateStr = runCatching { LocalDate.ofEpochDay(day.epochDay).toString() }.getOrDefault("${day.epochDay}")
    Box(
        Modifier
            .size(13.dp)
            .scale(if (day.isToday) pulse else 1f)
            .clip(RoundedCornerShape(4.dp))
            .background(base)
            .then(
                if (day.isToday) Modifier.border(
                    1.5.dp,
                    if (dark) Color.White else Color.Black,
                    RoundedCornerShape(4.dp)
                )
                else Modifier
            )
            .semantics { contentDescription = "$dateStr ${day.state}" }
    )
}

@Composable
private fun EmptyCell() {
    Box(Modifier.size(13.dp))
}

@Composable
private fun LegendRow(dark: Boolean) {
    Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        LegendDot(if (dark) Color.White else Color.Black, "Done")
        LegendDot(if (dark) Color(0xFF525252) else Color(0xFFA3A3A3), "Missed")
        LegendDot(if (dark) Color(0xFF1F1F1F) else Color(0xFFF0F0F0), "Idle")
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row {
        Box(
            Modifier.size(11.dp).clip(RoundedCornerShape(3.dp)).background(color)
                .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(3.dp))
        )
        Text(" $label", style = MaterialTheme.typography.labelSmall)
    }
}
