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
import com.example.reminder.features.drink.domain.repository.DrinkSettingsRepository
import java.util.*

class ReminderService : Service() {
    private val handler = Handler(Looper.getMainLooper())
    private var drinkReminderRunnable: Runnable? = null
    private var nextReminderTime: Long = 0L

    companion object {
        const val ACTION_START_DRINK_REMINDER = "com.example.reminder.ACTION_START_DRINK_REMINDER"
        const val ACTION_RECORD_DRINK = "com.example.reminder.ACTION_RECORD_DRINK"
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("ReminderService", "喝水提醒服务创建")
    }

    override fun onDestroy() {
        super.onDestroy()
        drinkReminderRunnable?.let { handler.removeCallbacks(it) }
        Log.d("ReminderService", "喝水提醒服务销毁")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()
        startForegroundNotification()
        
        when (intent?.action) {
            ACTION_START_DRINK_REMINDER -> {
                startDrinkReminder()
            }
            ACTION_RECORD_DRINK -> {
                // TODO: 记录喝水，这里可以后续扩展
                Log.d("ReminderService", "记录喝水")
                scheduleDrinkReminder()
            }
        }
        
        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "drink_reminder_channel"
            val channel = NotificationChannel(
                channelId, 
                "喝水提醒", 
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    private fun startForegroundNotification() {
        val channelId = "drink_reminder_channel"
        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("喝水提醒服务正在运行")
            .setContentText("定时喝水提醒服务已启动")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setOngoing(true)
            .build()
            
        if (Build.VERSION.SDK_INT >= 34) {
            startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            startForeground(1, notification)
        }
    }

    private fun startDrinkReminder() {
        Log.d("ReminderService", "开始喝水提醒计时")
        scheduleDrinkReminder()
    }

    private fun scheduleDrinkReminder() {
        // 取消之前的提醒
        drinkReminderRunnable?.let { handler.removeCallbacks(it) }
        
        // 加载喝水提醒设置
        val settings = DrinkSettingsRepository.loadSettings(this)
        
        // 检查当前是否在提醒时间范围内
        val currentTime = Calendar.getInstance()
        val currentHour = currentTime.get(Calendar.HOUR_OF_DAY)
        val currentMinute = currentTime.get(Calendar.MINUTE)
        val currentDayOfWeek = currentTime.get(Calendar.DAY_OF_WEEK)
        
        // 检查是否在设置的工作日内
        if (!settings.repeatDays.contains(currentDayOfWeek)) {
            Log.d("ReminderService", "今天不在提醒日期内")
            return
        }
        
        // 检查是否在时间范围内
        val currentMinutes = currentHour * 60 + currentMinute
        val startMinutes = settings.startHour * 60 + settings.startMinute
        val endMinutes = settings.endHour * 60 + settings.endMinute
        
        if (currentMinutes < startMinutes || currentMinutes > endMinutes) {
            Log.d("ReminderService", "当前时间不在提醒范围内")
            // 计算下一次提醒时间（明天的开始时间或今天的开始时间）
            scheduleNextDay(settings)
            return
        }
        
        // 计算下次提醒时间
        val intervalMillis = settings.intervalMinutes * 60 * 1000L
        nextReminderTime = System.currentTimeMillis() + intervalMillis
        
        drinkReminderRunnable = Runnable {
            sendDrinkNotification()
            scheduleDrinkReminder() // 递归安排下次提醒
        }
        
        handler.postDelayed(drinkReminderRunnable!!, intervalMillis)
        Log.d("ReminderService", "下次喝水提醒将在 ${settings.intervalMinutes} 分钟后")
    }

    private fun scheduleNextDay(settings: com.example.reminder.features.drink.domain.model.DrinkReminderSettings) {
        val tomorrow = Calendar.getInstance()
        tomorrow.add(Calendar.DAY_OF_YEAR, 1)
        tomorrow.set(Calendar.HOUR_OF_DAY, settings.startHour)
        tomorrow.set(Calendar.MINUTE, settings.startMinute)
        tomorrow.set(Calendar.SECOND, 0)
        
        val delayMillis = tomorrow.timeInMillis - System.currentTimeMillis()
        
        drinkReminderRunnable = Runnable {
            scheduleDrinkReminder()
        }
        
        handler.postDelayed(drinkReminderRunnable!!, delayMillis)
        Log.d("ReminderService", "安排明天的喝水提醒")
    }

    private fun sendDrinkNotification() {
        val channelId = "drink_reminder_channel"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, 
                "喝水提醒", 
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        // 点击通知跳转到喝水界面
        val intent = Intent(this, DrinkActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 快捷记录喝水按钮
        val recordIntent = Intent(this, ReminderService::class.java)
        recordIntent.action = ACTION_RECORD_DRINK
        val recordPendingIntent = PendingIntent.getService(
            this, 1, recordIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("该喝水了！")
            .setContentText("保持水分充足，保护身体健康")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .addAction(R.mipmap.ic_launcher, "记录喝水", recordPendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        notificationManager.notify(2, notification)
        Log.d("ReminderService", "发送喝水提醒通知")
    }

    override fun onBind(intent: Intent?): IBinder? = null
}