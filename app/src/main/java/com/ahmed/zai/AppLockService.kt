package com.ahmed.zai

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast

class AppLockService : AccessibilityService() {
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null || event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        val packageName = event.packageName?.toString() ?: return
        val prefs = getSharedPreferences("ZaiPrefs", Context.MODE_PRIVATE)

        val isAppLocked = prefs.getBoolean("lock_$packageName", false)
        val isGalleryLocked = prefs.getBoolean("lock_gallery", false) && (packageName.contains("gallery") || packageName.contains("photos") || packageName.contains("miui.gallery"))
        val isFilesLocked = prefs.getBoolean("lock_files", false) && packageName.contains("documentsui")
        val isCallsLocked = prefs.getBoolean("lock_calls", false) && (packageName.contains("dialer") || packageName.contains("phone"))
        val isContactsLocked = prefs.getBoolean("lock_contacts", false) && packageName.contains("contacts")

        if (isAppLocked || isGalleryLocked || isFilesLocked || isCallsLocked || isContactsLocked) {
            // إضافة تأخير بسيط لضمان استلام الحدث كاملاً
            Handler(Looper.getMainLooper()).postDelayed({
                Toast.makeText(this, "⛔ تم القفل بواسطة حماية مصطفى", Toast.LENGTH_SHORT).show()
                performGlobalAction(GLOBAL_ACTION_HOME)
            }, 200)
        }
    }
    override fun onInterrupt() {}
}
