package com.example.foser

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Handler
import android.os.IBinder
import androidx.annotation.Nullable
import androidx.annotation.RequiresApi
import androidx.preference.PreferenceManager
import java.util.*


class MyForegroundService : Service() {

    companion object {
        val CHANNEL_ID = "MyForegroundServiceChannel"
        val CHANNEL_NAME = "FoSer service channel"

        val MESSAGE = "message"
        val TIME = "time"
        val WORK = "work"
        val WORK_DOUBLE = "work_double"
        var LIST_PREFERENCES = "list_preferences"

    }


    private var message: String? = null
    private var showTime: Boolean? = null
    private var doWork: Boolean? = null
    private var doubleSpeed: Boolean? = null
    private lateinit var ctx: Context
    private lateinit var notificationIntent: Intent
    private lateinit var pendingIntent: PendingIntent
    private var counter: Int? = null
    private var timer: Timer? = null
    private lateinit var timerTask: TimerTask
    val handler:Handler = Handler()
    private var period: Long = 2000
    private lateinit var listPreferences: SharedPreferences


    val runnable:Runnable = Runnable {
        val notification: Notification = Notification.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_my_icon)
            .setContentTitle(getString(R.string.ser_title))
            .setShowWhen(showTime!!)
            .setContentText("$message  $counter")
            .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.circle))
            .setContentIntent(pendingIntent)
            .build()

        getSystemService(NotificationManager::class.java).notify(1, notification)

    }

    override fun onCreate() {
        super.onCreate()
        listPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        period = (listPreferences.getString("list_preference", "0") + "000").toLong()
        ctx = this
        notificationIntent = Intent(ctx, MainActivity::class.java)
        pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)
        val savedCounter = notificationIntent.getIntExtra("counter", 0)
        counter = savedCounter


        timer = Timer()

        timerTask = object : TimerTask() {
            override fun run() {
                counter = counter!! +1
                handler.post(runnable)
            }
        }
    }

    override fun onDestroy() {
        handler.removeCallbacks(runnable)
        timer?.cancel()
        timer?.purge()
        timer = null
        period = 2000
        notificationIntent.putExtra("counter", counter)
        super.onDestroy()
    }

    private fun doWork() {
        if (doWork!!) {
            val p: Long = if (doubleSpeed!!) period/2L else period
            timer?.schedule(timerTask, 0L, p)
        }
    }

    @Nullable
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        ///return super.onStartCommand(intent, flags, startId)

        message = intent?.getStringExtra(MESSAGE)
        showTime = intent?.getBooleanExtra(TIME, false)
        doWork = intent?.getBooleanExtra(WORK, false)
        doubleSpeed = intent?.getBooleanExtra(WORK_DOUBLE, false)

        createNotificationChannel()

        val notification: Notification = Notification.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_my_icon)
            .setContentTitle(getString(R.string.ser_title))
            .setShowWhen(showTime!!)
            .setContentText("$message  $counter")
            .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.circle))
            .setContentIntent(pendingIntent)
            .build()



        startForeground(1, notification)


        doWork()

        return START_NOT_STICKY
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        val serviceChannel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT)
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(serviceChannel)
    }
}
