package com.nexora.reminder.util

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class PermissionUtils @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun hasNotificationPermission(): Boolean {
        if (Build.VERSION.SDK_INT < 33) return true
        return ContextCompat.checkSelfPermission(
            context, Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun canScheduleExactAlarms(): Boolean {
        if (Build.VERSION.SDK_INT < 31) return true
        val am = context.getSystemService<AlarmManager>()
        return am?.canScheduleExactAlarms() == true
    }
}
