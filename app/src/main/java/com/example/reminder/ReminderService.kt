package com.example.reminder

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import android.content.pm.ServiceInfo

class ReminderService : Service() {
    private val handler = Handler(Looper.getMainLooper())
    private var workRunnable: Runnable? = null
    private var workLogRunnable: Runnable? = null
    private var workRemainingMillis: Long = 0L
    private var isResting = false

    companion object {
        const val ACTION_START_TIMER = "com.example.reminder.ACTION_START_TIMER"
        const val ACTION_RESTART_WORK = "com.example.reminder.ACTION_RESTART_WORK"
    }

    override fun onCreate() {
        super.onCreate()
        // 不再需要注册广播
    }

    override fun onDestroy() {
        super.onDestroy()
        workRunnable?.let { handler.removeCallbacks(it) }
        stopWorkLog()
        // 不再需要注销广播
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val channelId = "reminder_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "休息提醒", NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("休息提醒服务正在运行")
            .setContentText("定时提醒服务已启动")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setOngoing(true)
            .build()
        if (Build.VERSION.SDK_INT >= 34) {
            startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            startForeground(1, notification)
        }
        if (intent?.action == ACTION_START_TIMER || intent?.action == ACTION_RESTART_WORK) {
            startWorkTimer()
        }
        return START_STICKY
    }

    private fun startWorkTimer() {
        Log.d("ReminderService", "开始新的工作计时")
        workRunnable?.let { handler.removeCallbacks(it) }
        stopWorkLog()
        val workMinutes = PreferenceHelper.getWorkMinutes(this)
        val delayMillis = workMinutes * 60 * 1000L
        workRemainingMillis = delayMillis
        isResting = false
        workRunnable = Runnable {
            sendRestNotification()
        }
        handler.postDelayed(workRunnable!!, delayMillis)
        startWorkLog()
    }

    private fun startWorkLog() {
        workLogRunnable = object : Runnable {
            override fun run() {
                if (!isResting && workRemainingMillis > 0) {
                    Log.d("ReminderService", "工作计时剩余: ${workRemainingMillis / 1000} 秒")
                    workRemainingMillis -= 5000
                    handler.postDelayed(this, 5000)
                }
            }
        }
        handler.post(workLogRunnable!!)
    }

    private fun stopWorkLog() {
        workLogRunnable?.let { handler.removeCallbacks(it) }
    }

    private fun sendRestNotification() {
        val channelId = "reminder_channel"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "休息提醒", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(this, RestActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("休息时间到啦！")
            .setContentText("点击开始休息倒计时")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        notificationManager.notify(2, notification)

        // 进入休息计时
        isResting = true
        stopWorkLog()
        // 不再自动 startWorkTimer，等待休息界面通知
    }

    override fun onBind(intent: Intent?): IBinder? = null
}