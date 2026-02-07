package com.optshield.app

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import java.util.regex.Pattern

/**
 * Listens to notifications to detect OTP/verification text (e.g. from SMS apps).
 * Policy-compliant: NO READ_SMS — we only read notification content the user already sees.
 */
class OtpNotificationListenerService : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (sbn == null) return
        val extras = sbn.notification?.extras ?: return
        val title = extras.getCharSequence(android.app.Notification.EXTRA_TITLE)?.toString() ?: ""
        val text = extras.getCharSequence(android.app.Notification.EXTRA_TEXT)?.toString() ?: ""
        val subText = extras.getCharSequence(android.app.Notification.EXTRA_SUB_TEXT)?.toString() ?: ""
        val bigText = extras.getCharSequence(android.app.Notification.EXTRA_BIG_TEXT)?.toString() ?: ""
        val combined = "$title $text $subText $bigText"
        if (OTP_PATTERN.matcher(combined).find() || combined.contains(".apk", ignoreCase = true)) {
            Log.d(TAG, "Notification: OTP/.apk detected")
            AlertOverlayService.show(this)
        }
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        isRunning = true
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        isRunning = false
    }

    companion object {
        private const val TAG = "OtpNotificationListener"
        private val OTP_PATTERN = Pattern.compile("\\b(OTP|code|verification)\\b", Pattern.CASE_INSENSITIVE)

        @Volatile
        var isRunning = false
            private set
    }
}
