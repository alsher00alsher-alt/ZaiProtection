package com.ahmed.zai

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.switchmaterial.SwitchMaterial

class AppListActivity : AppCompatActivity() {
    private lateinit var prefs: android.content.SharedPreferences
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_list)
        prefs = getSharedPreferences("ZaiPrefs", Context.MODE_PRIVATE)

        val apps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
            .filter { it.packageName != packageName && (it.flags and ApplicationInfo.FLAG_SYSTEM) == 0 }
            .sortedBy { it.loadLabel(packageManager).toString() }

        val listView = findViewById<ListView>(R.id.appsListView)
        listView.adapter = AppAdapter(this, apps, prefs)
    }
}

class AppAdapter(private val context: Context, private val apps: List<ApplicationInfo>, private val prefs: android.content.SharedPreferences) : BaseAdapter() {
    override fun getCount(): Int = apps.size
    override fun getItem(position: Int): Any = apps[position]
    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: android.view.LayoutInflater.from(context).inflate(R.layout.item_app, parent, false)
        val appInfo = apps[position]
        val pm = context.packageManager

        view.findViewById<ImageView>(R.id.appIcon).setImageDrawable(appInfo.loadIcon(pm))
        view.findViewById<TextView>(R.id.appName).text = appInfo.loadLabel(pm)
        
        val switch = view.findViewById<SwitchMaterial>(R.id.appSwitch)
        switch.setOnCheckedChangeListener(null) // remove listener before setting state
        switch.isChecked = prefs.getBoolean("lock_${appInfo.packageName}", false)
        
        switch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // إظهار رسالة تأكيد
                android.app.AlertDialog.Builder(context)
                    .setTitle("حماية مصطفى")
                    .setMessage("هل تود حماية تطبيق (${appInfo.loadLabel(pm)})؟ لن تتمكن من فتحه إلا بعد إلغاء الحماية.")
                    .setPositiveButton("نعم") { _, _ ->
                        prefs.edit().putBoolean("lock_${appInfo.packageName}", true).apply()
                    }
                    .setNegativeButton("لا") { _, _ -> switch.isChecked = false }
                    .show()
            } else {
                prefs.edit().putBoolean("lock_${appInfo.packageName}", false).apply()
            }
        }
        return view
    }
}
