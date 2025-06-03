package com.example.reminder.features.drink.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "drink_records")
data class DrinkRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Int, // 喝水量（ml）
    val timestamp: Long = System.currentTimeMillis(), // 记录时间戳
    val date: String = getCurrentDateString() // 日期字符串 (yyyy-MM-dd)
)

private fun getCurrentDateString(): String {
    val calendar = Calendar.getInstance()
    return "${calendar.get(Calendar.YEAR)}-${String.format("%02d", calendar.get(Calendar.MONTH) + 1)}-${String.format("%02d", calendar.get(Calendar.DAY_OF_MONTH))}"
} 