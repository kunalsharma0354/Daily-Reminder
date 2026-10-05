package com.nexora.reminder.presentation.dashboard

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AllInclusive
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nexora.reminder.ui.theme.ReminderTokens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(vm: DashboardViewModel = hiltViewModel()) {
    val reminders by vm.reminders.collectAsState()
    val stats by vm.stats.collectAsState()
    val heatmap by vm.heatmap.collectAsState()
    var showSheet by remember { mutableStateOf(false) }

    var askedPermission by remember { mutableStateOf(false) }
    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> askedPermission = true }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= 33 && !askedPermission) {
            askedPermission = true
            try { permLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS) } catch (_: Exception) { }
        }
    }

    val fabScale by animateFloatAsState(
        if (showSheet) 0.9f else 1f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium),
        label = "fab"
    )

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showSheet = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = ReminderTokens.Pill,
                modifier = Modifier.scale(fabScale)
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add reminder")
            }
        }
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Column {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                "Daily Reminder",
                                style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.Bold)
                            )
                            Row {
                                Icon(Icons.Filled.AllInclusive, contentDescription = null, modifier = Modifier.padding(end = 6.dp))
                                Text(
                                    "Complete your daily tasks.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        androidx.compose.material3.IconButton(onClick = { vm.logout() }, modifier = Modifier.size(48.dp)) {
                            Icon(Icons.Filled.Logout, contentDescription = "Sign out")
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }

            item {
                AnimatedVisibility(true, enter = fadeIn() + expandVertically()) {
                    PermissionBanner()
                }
            }

            item {
                AnimatedVisibility(true, enter = fadeIn() + slideInVertically()) {
                    StatsSection(stats)
                }
            }
            item {
                AnimatedVisibility(true, enter = fadeIn() + slideInVertically()) {
                    HeatmapSection(heatmap)
                }
            }

            if (reminders.isEmpty()) {
                item {
                    AnimatedVisibility(true, enter = fadeIn() + slideInVertically()) {
                        EmptyState(onCreate = { showSheet = true })
                    }
                }
            } else {
                item {
                    Text(
                        "Today's tasks (${reminders.size})",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
                itemsIndexed(reminders, key = { _, r -> r.id }) { index, r ->
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn() + slideInVertically(
                            initialOffsetY = { 40 + index * 10 }
                        ),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        ReminderCard(
                            reminder = r,
                            onDone = { vm.complete(r.id) },
                            onSkip = { vm.skip(r.id) },
                            onDelete = { vm.delete(r.id) },
                            onIntervalChange = { vm.changeInterval(r.id, it) }
                        )
                    }
                }
            }
            item { Spacer(Modifier.height(80.dp)) }
        }
    }

    if (showSheet) {
        ReminderBottomSheet(
            onDismiss = { showSheet = false },
            onSave = { n, t, mins, vText, vPath, m, dt, cb -> vm.create(n, t, mins, vText, vPath, m, dt, cb) }
        )
    }
}

@Composable
private fun PermissionBanner() {
    val context = LocalContext.current
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shape = ReminderTokens.CardRadiusSmall,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(Modifier.padding(14.dp)) {
            Column(Modifier.weight(1f)) {
                Text("Enable notifications for reminders", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                Text(
                    "First reminder in 1 minute, then at your selected interval until completed.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            TextButton(onClick = {
                try {
                    val i = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                    }
                    context.startActivity(i)
                } catch (_: Exception) {
                    try {
                        context.startActivity(
                            Intent(
                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.parse("package:${context.packageName}")
                            )
                        )
                    } catch (_: Exception) { }
                }
            }) { Text("Settings", fontWeight = FontWeight.Bold) }
        }
    }
}
