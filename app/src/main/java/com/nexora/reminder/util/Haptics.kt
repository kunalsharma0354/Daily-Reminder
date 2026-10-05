package com.nexora.reminder.util

import android.content.Context
import android.os.Build
import android.view.HapticFeedbackConstants
import android.view.View

object Haptics {
    fun light(view: View) {
        if (Build.VERSION.SDK_INT >= 27) view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        else view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
    }

    fun confirm(view: View) {
        view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
    }

    fun reject(view: View) {
        view.performHapticFeedback(HapticFeedbackConstants.REJECT)
    }
}
