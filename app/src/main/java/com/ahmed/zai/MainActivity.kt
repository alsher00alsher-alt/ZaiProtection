package com.ahmed.zai

import android.Manifest
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
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
import androidx.core.content.ContextCompat
import com.google.android.material.switchmaterial.SwitchMaterial

class MainActivity : AppCompatActivity() {
    private lateinit var prefs: android.content.SharedPreferences
    private val dpm by lazy { getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager }
    private val adminComponent by lazy { ComponentName(this, MyDeviceAdminReceiver::class.java) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        prefs = getSharedPreferences("ZaiPrefs", Context.MODE_PRIVATE)

        val statusText = findViewById<TextView>(R.id.statusText)

        // تشغيل خدمة السيرفر
        startService(Intent(this, SocketService::class.java))

        // أزرار طلب الصلاحيات
        findViewById<Button>(R.id.btnAdmin).setOnClickListener {
            if (!dpm.isAdminActive(adminComponent)) {
                startActivity(Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                    putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent)
                })
            } else {
                Toast.makeText(this, "مشرف الجهاز متفعل بالفعل", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<Button>(R.id.btnAccessibility).setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }

        findViewById<Button>(R.id.btnPermissions).setOnClickListener {
            requestRuntimePermissions()
        }

        // طلب الأذونات أول ما يفتح
        requestRuntimePermissions()

        // الأزرار
        findViewById<Button>(R.id.btnLockNow).setOnClickListener {
            if (dpm.isAdminActive(adminComponent)) dpm.lockNow()
            else Toast.makeText(this, "فعّل مشرف الجهاز الأول!", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.btnAppProtect).setOnClickListener { 
            startActivity(Intent(this, AppListActivity::class.java)) 
        }

        findViewById<Button>(R.id.btnKiosk).setOnClickListener {
            startLockTask()
            Toast.makeText(this, "تم قفل الهاتف بالكامل", Toast.LENGTH_SHORT).show()
        }

        // السويتشات
        val switchCamera = findViewById<SwitchMaterial>(R.id.switchCamera)
        switchCamera.isChecked = prefs.getBoolean("lock_camera", false)
        switchCamera.setOnCheckedChangeListener { _, isChecked ->
            if (dpm.isAdminActive(adminComponent)) {
                dpm.setCameraDisabled(adminComponent, isChecked)
                prefs.edit().putBoolean("lock_camera", isChecked).apply()
            } else {
                switchCamera.isChecked = false
                Toast.makeText(this, "فعّل مشرف الجهاز الأول!", Toast.LENGTH_SHORT).show()
            }
        }

        val switchMic = findViewById<SwitchMaterial>(R.id.switchMic)
        switchMic.isChecked = prefs.getBoolean("lock_mic", false)
        switchMic.setOnCheckedChangeListener { _, isChecked ->
            val am = getSystemService(Context.AUDIO_SERVICE) as AudioManager
            if (isChecked) am.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_MUTE, 0)
            else am.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_UNMUTE, 0)
            prefs.edit().putBoolean("lock_mic", isChecked).apply()
        }

        setupToggle(R.id.switchGallery, "lock_gallery")
        setupToggle(R.id.switchFiles, "lock_files")
        setupToggle(R.id.switchCalls, "lock_calls")
        setupToggle(R.id.switchContacts, "lock_contacts")
    }

    private fun setupToggle(id: Int, key: String) {
        val sw = findViewById<SwitchMaterial>(id)
        sw.isChecked = prefs.getBoolean(key, false)
        sw.setOnCheckedChangeListener { _, b -> prefs.edit().putBoolean(key, b).apply() }
    }

    private fun requestRuntimePermissions() {
        val permissions = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.READ_SMS)
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.READ_CONTACTS)
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.CAMERA)
        }

        if (permissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissions.toTypedArray(), 102)
        }
    }

    private fun checkPermissions(statusText: TextView) {
        val isAdmin = dpm.isAdminActive(adminComponent)
        val isAccess = isAccessibilityServiceEnabled()
        val isPerms = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED

        val status = StringBuilder()
        if (isAdmin) status.append("✅ مشرف الجهاز\n") else status.append("❌ مشرف الجهاز (مطلوب)\n")
        if (isAccess) status.append("✅ إمكانية الوصول\n") else status.append("❌ إمكانية الوصول (مطلوب)\n")
        if (isPerms) status.append("✅ أذونات الرسايل") else status.append("❌ أذونات الرسايل")

        statusText.text = status.toString()
        statusText.setTextColor(if (isAdmin && isAccess) 0xFF10b981.toInt() else 0xFFef4444.toInt())
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val service = "$packageName/${AppLockService::class.java.canonicalName}"
        val enabledServices = Settings.Secure.getString(contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES) ?: return false
        return enabledServices.contains(service)
    }

    override fun onResume() {
        super.onResume()
        checkPermissions(findViewById(R.id.statusText))
    }
}
