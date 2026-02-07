package com.optshield.app

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.optshield.app.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        requestPostNotificationsIfNeeded()

        binding.btnAccessibility.setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            })
            Toast.makeText(this, "Enable OTP Shield under Services", Toast.LENGTH_LONG).show()
        }

        binding.btnNotifications.setOnClickListener {
            val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
            startActivity(intent)
            Toast.makeText(this, "Enable OTP Shield", Toast.LENGTH_LONG).show()
        }

        binding.btnOverlay.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
                startActivityForResult(
                    Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")),
                    REQUEST_OVERLAY
                )
            } else {
                Toast.makeText(this, "Overlay already granted", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnStartService.setOnClickListener {
            startForegroundServiceAndRefreshStatus()
        }

        refreshStatus()
    }

    override fun onResume() {
        super.onResume()
        refreshStatus()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_OVERLAY) {
            refreshStatus()
        }
    }

    private fun requestPostNotificationsIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), REQUEST_NOTIFICATION)
        }
    }

    private fun startForegroundServiceAndRefreshStatus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(Intent(this, MonitorForegroundService::class.java))
        } else {
            startService(Intent(this, MonitorForegroundService::class.java))
        }
        refreshStatus()
        Toast.makeText(this, "Monitoring started", Toast.LENGTH_SHORT).show()
    }

    private fun refreshStatus() {
        val parts = mutableListOf<String>()
        if (OtpShieldAccessibilityService.isRunning) parts.add("Accessibility: ON")
        else parts.add("Accessibility: OFF")
        if (OtpNotificationListenerService.isRunning) parts.add("Notifications: ON")
        else parts.add("Notifications: OFF")
        parts.add(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.canDrawOverlays(this)) "Overlay: ON" else "Overlay: OFF")
        parts.add(if (MonitorForegroundService.isRunning) "Service: ON" else "Service: OFF")
        binding.statusText.text = parts.joinToString(" · ")
        binding.statusText.visibility = View.VISIBLE
    }

    companion object {
        private const val REQUEST_OVERLAY = 1001
        private const val REQUEST_NOTIFICATION = 1002
    }
}
