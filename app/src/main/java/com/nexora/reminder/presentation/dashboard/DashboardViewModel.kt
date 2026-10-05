package com.nexora.reminder.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexora.reminder.domain.model.DashboardStats
import com.nexora.reminder.domain.model.HeatmapDay
import com.nexora.reminder.domain.model.Reminder
import com.nexora.reminder.domain.usecase.CompleteReminder
import com.nexora.reminder.domain.usecase.CreateReminder
import com.nexora.reminder.domain.usecase.DeleteReminder
import com.nexora.reminder.domain.usecase.GetDashboardFlow
import com.nexora.reminder.domain.usecase.GetHeatmapData
import com.nexora.reminder.domain.usecase.GetStatistics
import com.nexora.reminder.domain.usecase.SkipReminder
import com.nexora.reminder.auth.SessionManager
import com.nexora.reminder.domain.usecase.UpdateInterval
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    getDashboardFlow: GetDashboardFlow,
    getStatistics: GetStatistics,
    getHeatmapData: GetHeatmapData,
    private val createReminder: CreateReminder,
    private val deleteReminder: DeleteReminder,
    private val completeReminder: CompleteReminder,
    private val skipReminder: SkipReminder,
    private val updateInterval: UpdateInterval,
    private val sessionManager: SessionManager
) : ViewModel() {

    val reminders: StateFlow<List<Reminder>> = getDashboardFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val stats: StateFlow<DashboardStats> = getStatistics()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardStats())

    val heatmap: StateFlow<List<HeatmapDay>> = getHeatmapData()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun create(
        name: String,
        title: String,
        intervalMin: Long,
        voiceText: String = "",
        voicePreviewPath: String = "",
        mode: com.nexora.reminder.domain.model.ReminderMode = com.nexora.reminder.domain.model.ReminderMode.ONCE,
        dailyTimeMinutes: Int = 540,
        onDone: (Boolean, String?) -> Unit = { _, _ -> }
    ) {
        viewModelScope.launch {
            try {
                createReminder(name, title, intervalMin, voiceText, voicePreviewPath, mode, dailyTimeMinutes)
                onDone(true, null)
            } catch (e: Exception) {
                onDone(false, e.message)
            }
        }
    }

    fun delete(id: Long) {
        viewModelScope.launch { deleteReminder(id) }
    }

    fun complete(id: Long) {
        viewModelScope.launch { completeReminder(id) }
    }

    fun skip(id: Long) {
        viewModelScope.launch { skipReminder(id) }
    }

    fun changeInterval(id: Long, minutes: Long) {
        viewModelScope.launch { updateInterval(id, minutes) }
    }

    fun logout() {
        sessionManager.clear()
    }
}
