package com.optshield.app

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import java.util.regex.Pattern

/**
 * Scans focused window text (e.g. WhatsApp) for OTP keywords and .apk mentions.
 * Policy-compliant: does not read SMS or call logs.
 */
class OtpShieldAccessibilityService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null || event.source == null) return
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) return

        val text = gatherText(event.source)
        if (text.isEmpty()) return

        if (OTP_PATTERN.matcher(text).find() || text.contains(".apk", ignoreCase = true)) {
            Log.d(TAG, "Accessibility: OTP/.apk detected in app text")
            AlertOverlayService.show(this)
        }
    }

    override fun onInterrupt() {}

    override fun onServiceConnected() {
        super.onServiceConnected()
        isRunning = true
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
    }

    private fun gatherText(node: AccessibilityNodeInfo?): String {
        if (node == null) return ""
        val sb = StringBuilder()
        if (node.text?.isNotEmpty() == true) sb.append(node.text).append(' ')
        for (i in 0 until node.childCount) {
            sb.append(gatherText(node.getChild(i)))
        }
        if (node.contentDescription?.isNotEmpty() == true) sb.append(node.contentDescription).append(' ')
        return sb.toString()
    }

    companion object {
        private const val TAG = "OtpShieldA11y"
        private val OTP_PATTERN = Pattern.compile("\\b(OTP|code|verification)\\b", Pattern.CASE_INSENSITIVE)

        @Volatile
        var isRunning = false
            private set
    }
}
