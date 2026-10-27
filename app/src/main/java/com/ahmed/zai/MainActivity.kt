package com.ahmed.zai

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.material.switchmaterial.SwitchMaterial

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val switchCamera = findViewById<SwitchMaterial>(R.id.switchCamera)
        val switchMic = findViewById<SwitchMaterial>(R.id.switchMic)
        val btnAppProtect = findViewById<Button>(R.id.btnAppProtect)
        val btnLogs = findViewById<Button>(R.id.btnLogs)
        val btnAdmin = findViewById<Button>(R.id.btnAdmin)
        val btnAccessibility = findViewById<Button>(R.id.btnAccessibility)
        val btnLockNow = findViewById<Button>(R.id.btnLockNow)
        val statusText = findViewById<TextView>(R.id.statusText)

        // طلب الصلاحيات
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
        }

        startService(Intent(this, SocketService::class.java))

        // فحص الأزرار الدائم
        val handler = android.os.Handler(android.os.Looper.getMainLooper())
        handler.postDelayed(object : Runnable {
            override fun run() {
                val dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
                val adminComponent = ComponentName(this@MainActivity, MyDeviceAdminReceiver::class.java)
                if (!dpm.isAdminActive(adminComponent)) {
                    switchCamera.isChecked = false
                    switchMic.isChecked = false
                }
                handler.postDelayed(this, 3000)
            }
        }, 3000)

        btnAdmin.setOnClickListener {
            val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, ComponentName(this, MyDeviceAdminReceiver::class.java))
            startActivity(intent)
        }

        btnAccessibility.setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }

        btnLockNow.setOnClickListener {
            val dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
            val adminComponent = ComponentName(this, MyDeviceAdminReceiver::class.java)
            if (dpm.isAdminActive(adminComponent)) dpm.lockNow()
            else Toast.makeText(this, "فعّل صلاحيات الحماية الأول!", Toast.LENGTH_SHORT).show()
        }

        // زرار حماية التطبيقات
        btnAppProtect.setOnClickListener {
            startActivity(Intent(this, AppListActivity::class.java))
        }

        // زرار قاعدة البيانات (السجلات)
        btnLogs.setOnClickListener {
            val prefs = getSharedPreferences("ZaiPrefs", Context.MODE_PRIVATE)
            val logs = prefs.getString("system_logs", "لا توجد سجلات حتى الآن.") ?: ""
            AlertDialog.Builder(this)
                .setTitle("⚠️ قاعدة البيانات (السجلات)")
                .setMessage(logs)
                .setPositiveButton("مسح السجلات") { _, _ -> prefs.edit().putString("system_logs", "").apply() }
                .setNegativeButton("إغلاق", null)
                .show()
        }

        // تفعيل الكاميرا
        switchCamera.setOnCheckedChangeListener { _, isChecked ->
            val dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
            val adminComponent = ComponentName(this, MyDeviceAdminReceiver::class.java)
            if (dpm.isAdminActive(adminComponent)) {
                dpm.setCameraDisabled(adminComponent, isChecked)
                statusText.text = if(isChecked) "📸 الكاميرا مقفولة" else "📸 الكاميرا مفتوحة"
                statusText.setTextColor(if(isChecked) 0xFF10b981.toInt() else 0xFFef4444.toInt())
                SocketService.sendDataToServer("STATE", "Camera: $isChecked")
            } else {
                switchCamera.isChecked = false
                Toast.makeText(this, "فعّل صلاحيات الحماية الأول!", Toast.LENGTH_SHORT).show()
            }
        }

        // تفعيل المايك
        switchMic.setOnCheckedChangeListener { _, isChecked ->
            val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
            if (isChecked) {
                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_MUTE, 0)
                statusText.text = "🎤 المايك مقفول"
                SocketService.sendDataToServer("STATE", "Mic: true")
            } else {
                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_UNMUTE, 0)
                statusText.text = "🎤 المايك مفتوح"
                SocketService.sendDataToServer("STATE", "Mic: false")
            }
        }
    }
}
