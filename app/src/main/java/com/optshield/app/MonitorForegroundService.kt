package com.optshield.app

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import androidx.core.app.NotificationCompat

/**
 * Foreground service for persistence. Listens to call state (TelephonyManager) only;
 * does not use READ_CALL_LOG.
 */
class MonitorForegroundService : Service() {

    private var telephonyManager: TelephonyManager? = null
    private var phoneStateListener: PhoneStateListener? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        isRunning = true
        registerPhoneStateListener()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, buildNotification())
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        unregisterPhoneStateListener()
        isRunning = false
        super.onDestroy()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(com.optshield.app.R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(com.optshield.app.R.string.notification_channel_desc)
            }
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                .createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        val pending = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(com.optshield.app.R.string.app_name))
            .setContentText(getString(com.optshield.app.R.string.notification_channel_desc))
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setContentIntent(pending)
            .setOngoing(true)
            .build()
    }

    private fun registerPhoneStateListener() {
        telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        phoneStateListener = object : PhoneStateListener() {
            override fun onCallStateChanged(state: Int, phoneNumber: String?) {
                when (state) {
                    TelephonyManager.CALL_STATE_RINGING,
                    TelephonyManager.CALL_STATE_OFFHOOK -> {
                        AlertOverlayService.show(this@MonitorForegroundService)
                    }
                }
            }
        }
        @Suppress("DEPRECATION")
        telephonyManager?.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE)
    }

    private fun unregisterPhoneStateListener() {
        phoneStateListener?.let {
            @Suppress("DEPRECATION")
            telephonyManager?.listen(it, PhoneStateListener.LISTEN_NONE)
        }
        phoneStateListener = null
    }

    companion object {
        private const val CHANNEL_ID = "optshield_channel"
        private const val NOTIFICATION_ID = 9001

        @Volatile
        var isRunning = false
            private set
    }
}
