package com.example.reminder.features.drink.domain.repository

import android.content.Context
import com.example.reminder.features.drink.domain.model.DrinkReminderSettings

object DrinkSettingsRepository {
    // 这里用 SharedPreferences 简单实现
    fun loadSettings(context: Context): DrinkReminderSettings {
        val sp = context.getSharedPreferences("drink_settings", Context.MODE_PRIVATE)
        return DrinkReminderSettings(
            dailyGoal = sp.getInt("dailyGoal", 1000),
            intervalMinutes = sp.getInt("intervalMinutes", 60),
            repeatDays = sp.getStringSet("repeatDays", setOf("2","3","4","5","6"))!!.map { it.toInt() }.toSet(),
            startHour = sp.getInt("startHour", 9),
            startMinute = sp.getInt("startMinute", 0),
            endHour = sp.getInt("endHour", 18),
            endMinute = sp.getInt("endMinute", 0),
            usePopupReminder = sp.getBoolean("usePopupReminder", true)
        )
    }

    fun saveSettings(context: Context, settings: DrinkReminderSettings) {
        val sp = context.getSharedPreferences("drink_settings", Context.MODE_PRIVATE)
        sp.edit()
            .putInt("dailyGoal", settings.dailyGoal)
            .putInt("intervalMinutes", settings.intervalMinutes)
            .putStringSet("repeatDays", settings.repeatDays.map { it.toString() }.toSet())
            .putInt("startHour", settings.startHour)
            .putInt("startMinute", settings.startMinute)
            .putInt("endHour", settings.endHour)
            .putInt("endMinute", settings.endMinute)
            .putBoolean("usePopupReminder", settings.usePopupReminder)
            .apply()
    }
} 