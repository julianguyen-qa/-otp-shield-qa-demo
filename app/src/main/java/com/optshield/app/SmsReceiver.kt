package com.optshield.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * Placeholder for SMS-related triggers. OTP from SMS is detected via
 * [OtpNotificationListenerService] (notification content only — policy-compliant, no READ_SMS).
 * This receiver can be used for SMS Retriever API (app-specific OTP) if needed later.
 */
class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            // Optional: handle SMS Retriever one-time verification if you add it
            else -> Log.d(TAG, "SmsReceiver: OTP from SMS handled by OtpNotificationListenerService")
        }
    }

    companion object {
        private const val TAG = "SmsReceiver"
    }
}
