package com.example.foser

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager


class MainActivity : AppCompatActivity() {
    private lateinit var buttonStart: Button
    private lateinit var buttonStop: Button
    private lateinit var buttonRestart: Button
    private lateinit var textInfoService: TextView
    private lateinit var textInfoSettings: TextView
    private lateinit var message: String
    private var showTime: Boolean = false
    private var work: Boolean = false
    private var workDouble: Boolean = false
    private var listPreference: Long = 0L
    private var savedCounter: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        buttonStart = findViewById(R.id.buttonStart)
        buttonStop = findViewById(R.id.buttonStop)
        buttonRestart = findViewById(R.id.buttonRestart)
        textInfoService = findViewById(R.id.textInfoServiceState)
        textInfoSettings = findViewById(R.id.textInfoSettings)
        updateUI()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.itemSettings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            R.id.itemExit -> {
                finishAndRemoveTask()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun clickStart(view: View) {
        getPreferences()
        val intent = Intent(this, MyForegroundService::class.java)
        intent.putExtra(MyForegroundService.MESSAGE, message)
        intent.putExtra(MyForegroundService.WORK, work)
        intent.putExtra(MyForegroundService.TIME, showTime)
        intent.putExtra(MyForegroundService.WORK_DOUBLE, workDouble)
        intent.putExtra(MyForegroundService.LIST_PREFERENCES, listPreference)
        intent.putExtra("counter", savedCounter)

        ContextCompat.startForegroundService(this, intent)
        updateUI()
    }

    fun clickStop(view: View) {
        val intent = Intent(this, MyForegroundService::class.java)
        stopService(intent)
        updateUI()
    }

    fun clickRestart(view: View) {
        val intent = Intent(this, MyForegroundService::class.java)
        clickStop(view)
        savedCounter = intent.getIntExtra("counter", 0)
        clickStart(view)
    }

    private fun getPreferences(): String {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        message = sharedPreferences.getString("message", "ForSer")!!
        showTime = sharedPreferences.getBoolean("show_time", true)
        work = sharedPreferences.getBoolean("sync", true)
        workDouble = sharedPreferences.getBoolean("double", false)
        return """
            Message: $message
            show_time: $showTime
            work: $work
            double: $workDouble
            """.trimIndent()
    }

    private fun updateUI() {
        if (isMyForegroundServiceRunning()) {
            buttonStart.isEnabled = false
            buttonStop.isEnabled = true
            buttonRestart.isEnabled = true
            textInfoService.text = getString(R.string.info_service_not_running)
        } else {
            buttonStart.isEnabled = true
            buttonStop.isEnabled = false
            buttonRestart.isEnabled = false
            textInfoService.text = getString(R.string.info_service_not_running)
        }
        textInfoSettings.text = getPreferences()
    }


    @SuppressWarnings("deprecation")
    private fun isMyForegroundServiceRunning(): Boolean {
        val myServiceName = MyForegroundService::class.java.name
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (runningService in activityManager.getRunningServices(Int.MAX_VALUE)) {
            val runningServiceName = runningService.service.className
            if (runningServiceName == myServiceName) {
                return true
            }
        }
        return false
    }
}
