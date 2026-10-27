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
        val switchFiles = findViewById<SwitchMaterial>(R.id.switchFiles)
        val switchScreenRec = findViewById<SwitchMaterial>(R.id.switchScreenRec)
        val switchNotif = findViewById<SwitchMaterial>(R.id.switchNotif)
        val switchCalls = findViewById<SwitchMaterial>(R.id.switchCalls)
        val switchContacts = findViewById<SwitchMaterial>(R.id.switchContacts)
        
        val btnAppProtect = findViewById<Button>(R.id.btnAppProtect)
        val btnKiosk = findViewById<Button>(R.id.btnKiosk)
        val btnAdmin = findViewById<Button>(R.id.btnAdmin)
        val btnAccessibility = findViewById<Button>(R.id.btnAccessibility)
        val btnLockNow = findViewById<Button>(R.id.btnLockNow)
        val statusText = findViewById<TextView>(R.id.statusText)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
        }

        startService(Intent(this, SocketService::class.java))
        val prefs = getSharedPreferences("ZaiPrefs", Context.MODE_PRIVATE)

        // فحص حالة الأدمن كل 3 ثواني
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
            startActivity(Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, ComponentName(this, MyDeviceAdminReceiver::class.java))
            })
        }

        btnAccessibility.setOnClickListener { startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }

        btnLockNow.setOnClickListener {
            val dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
            if (dpm.isAdminActive(ComponentName(this, MyDeviceAdminReceiver::class.java))) dpm.lockNow()
            else Toast.makeText(this, "فعّل صلاحيات الحماية الأول!", Toast.LENGTH_SHORT).show()
        }

        btnAppProtect.setOnClickListener { startActivity(Intent(this, AppListActivity::class.java)) }

        // قفل الهاتف بالكامل
        btnKiosk.setOnClickListener {
            startLockTask()
            Toast.makeText(this, "تم قفل الهاتف بالكامل", Toast.LENGTH_LONG).show()
        }

        // السويتشات
        switchCamera.setOnCheckedChangeListener { _, isChecked ->
            val dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
            if (dpm.isAdminActive(ComponentName(this, MyDeviceAdminReceiver::class.java))) {
                dpm.setCameraDisabled(ComponentName(this, MyDeviceAdminReceiver::class.java), isChecked)
                prefs.edit().putBoolean("lock_camera", isChecked).apply()
            } else { switchCamera.isChecked = false; Toast.makeText(this, "فعّل صلاحيات الحماية!", Toast.LENGTH_SHORT).show() }
        }

        switchMic.setOnCheckedChangeListener { _, isChecked ->
            val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
            if (isChecked) audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_MUTE, 0)
            else audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_UNMUTE, 0)
            prefs.edit().putBoolean("lock_mic", isChecked).apply()
        }

        // حفظ باقي الحالات في الذاكرة عشان خدمة إمكانية الوصول تقراها
        switchGallery.setOnCheckedChangeListener { _, b -> prefs.edit().putBoolean("lock_gallery", b).apply() }
        switchFiles.setOnCheckedChangeListener { _, b -> prefs.edit().putBoolean("lock_files", b).apply() }
        switchScreenRec.setOnCheckedChangeListener { _, b -> prefs.edit().putBoolean("lock_screenrec", b).apply() }
        switchNotif.setOnCheckedChangeListener { _, b -> prefs.edit().putBoolean("lock_notif", b).apply() }
        switchCalls.setOnCheckedChangeListener { _, b -> prefs.edit().putBoolean("lock_calls", b).apply() }
        switchContacts.setOnCheckedChangeListener { _, b -> prefs.edit().putBoolean("lock_contacts", b).apply() }
    }
}
