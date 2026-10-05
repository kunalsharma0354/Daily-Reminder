package com.nexora.reminder.presentation.dashboard

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nexora.reminder.domain.model.DashboardStats
import com.nexora.reminder.ui.theme.ReminderTokens

@Composable
fun StatsSection(stats: DashboardStats, modifier: Modifier = Modifier) {
    val completed by animateIntAsState(stats.completedDays, tween(900, easing = FastOutSlowInEasing), label = "c")
    val missed by animateIntAsState(stats.missedDays, tween(900, easing = FastOutSlowInEasing), label = "m")
    val streak by animateIntAsState(stats.currentStreak, tween(900, easing = FastOutSlowInEasing), label = "s")
    val pop by animateFloatAsState(
        if (streak > 0) 1.04f else 1f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow),
        label = "pop"
    )

    Card(
        modifier.fillMaxWidth().scale(pop),
        shape = ReminderTokens.CardRadius,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(Modifier.padding(18.dp)) {
            Text("Consistency", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                StatItem(completed.toString(), "Completed")
                StatItem(missed.toString(), "Missed")
                StatItem(streak.toString(), "Streak", highlight = true)
            }
        }
    }
}

@Composable
private fun StatItem(value: String, label: String, highlight: Boolean = false) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            value,
            style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            if (highlight) "$label ●" else label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = if (highlight) FontWeight.Bold else FontWeight.Normal),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
