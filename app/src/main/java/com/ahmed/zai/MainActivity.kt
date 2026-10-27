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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.material.switchmaterial.SwitchMaterial

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val switchCamera = findViewById<SwitchMaterial>(R.id.switchCamera)
        val switchMic = findViewById<SwitchMaterial>(R.id.switchMic)
        val switchGallery = findViewById<SwitchMaterial>(R.id.switchGallery)
        val switchNotif = findViewById<SwitchMaterial>(R.id.switchNotif)
        val btnAdmin = findViewById<Button>(R.id.btnAdmin)
        val btnAccessibility = findViewById<Button>(R.id.btnAccessibility)
        val btnLockNow = findViewById<Button>(R.id.btnLockNow)
        val statusText = findViewById<TextView>(R.id.statusText)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
        }

        try {
            startService(Intent(this, SocketService::class.java))
        } catch (e: Exception) {
            statusText.text = "خطأ في الخدمة: ${e.message}"
        }

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

        // حماية المايك باستخدام كتم الصوت
        switchMic.setOnCheckedChangeListener { _, isChecked ->
            val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
            if (isChecked) {
                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_MUTE, 0)
                audioManager.adjustStreamVolume(AudioManager.STREAM_VOICE_CALL, AudioManager.ADJUST_MUTE, 0)
                statusText.text = "🎤 المايك مقفول (تم كتم الصوت)"
            } else {
                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_UNMUTE, 0)
                audioManager.adjustStreamVolume(AudioManager.STREAM_VOICE_CALL, AudioManager.ADJUST_UNMUTE, 0)
                statusText.text = "🎤 المايك مفتوح"
            }
        }

        switchGallery.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            }
            AppLockService.isGalleryLocked = isChecked
        }

        switchNotif.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))
            }
        }
    }
}
