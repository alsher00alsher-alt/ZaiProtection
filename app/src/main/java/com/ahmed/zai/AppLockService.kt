package com.ahmed.zai

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast

class AppLockService : AccessibilityService() {
    companion object {
        var isGalleryLocked = false
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null || !isGalleryLocked) return
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val packageName = event.packageName?.toString() ?: return
            if (packageName.contains("gallery") || packageName.contains("photos") || packageName.contains("miui.gallery")) {
                Toast.makeText(this, "⛔ المعرض مقفول بواسطة حماية مصطفى", Toast.LENGTH_SHORT).show()
                performGlobalAction(GLOBAL_ACTION_HOME)
            }
        }
    }
    override fun onInterrupt() {}
}
