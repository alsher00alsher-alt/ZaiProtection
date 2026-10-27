package com.ahmed.zai

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast

class AppLockService : AccessibilityService() {
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null || event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        val packageName = event.packageName?.toString() ?: return
        
        // التحقق من قاعدة البيانات
        val prefs = getSharedPreferences("ZaiPrefs", Context.MODE_PRIVATE)
        val isLocked = prefs.getBoolean("lock_$packageName", false)
        
        if (isLocked) {
            Toast.makeText(this, "⛔ تم قفل التطبيق بواسطة حماية مصطفى", Toast.LENGTH_SHORT).show()
            performGlobalAction(GLOBAL_ACTION_HOME)
        }
    }
    override fun onInterrupt() {}
}
