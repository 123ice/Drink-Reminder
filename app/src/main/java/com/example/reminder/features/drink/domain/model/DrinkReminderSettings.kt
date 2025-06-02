package com.example.reminder.features.drink.domain.model

data class DrinkReminderSettings(
    val dailyGoal: Int = 1000, // 每日目标 ml
    val intervalMinutes: Int = 60, // 提醒间隔
    val repeatDays: Set<Int> = setOf(2,3,4,5,6), // 1=周日, 2=周一...
    val startHour: Int = 9,
    val startMinute: Int = 0,
    val endHour: Int = 18,
    val endMinute: Int = 0
) 