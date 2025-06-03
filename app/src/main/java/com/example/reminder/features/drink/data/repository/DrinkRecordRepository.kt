package com.example.reminder.features.drink.data.repository

import android.content.Context
import com.example.reminder.core.database.DrinkDatabase
import com.example.reminder.features.drink.data.database.DrinkRecord
import com.example.reminder.features.drink.data.database.DayStats
import kotlinx.coroutines.flow.Flow
import java.util.*

class DrinkRecordRepository(context: Context) {
    
    private val drinkRecordDao = DrinkDatabase.getDatabase(context).drinkRecordDao()
    
    suspend fun addDrinkRecord(amount: Int) {
        val record = DrinkRecord(amount = amount)
        drinkRecordDao.insertDrinkRecord(record)
    }
    
    suspend fun getTodayTotalAmount(): Int {
        val today = getCurrentDateString()
        return drinkRecordDao.getTotalAmountByDate(today) ?: 0
    }
    
    fun getTodayTotalAmountFlow(): Flow<Int?> {
        val today = getCurrentDateString()
        return drinkRecordDao.getTotalAmountByDateFlow(today)
    }
    
    suspend fun getTodayRecords(): List<DrinkRecord> {
        val today = getCurrentDateString()
        return drinkRecordDao.getRecordsByDate(today)
    }
    
    fun getTodayRecordsFlow(): Flow<List<DrinkRecord>> {
        val today = getCurrentDateString()
        return drinkRecordDao.getRecordsByDateFlow(today)
    }
    
    suspend fun getLast7DaysStats(): List<DayStats> {
        return drinkRecordDao.getLast7DaysStats()
    }
    
    suspend fun getLast30DaysStats(): List<DayStats> {
        return drinkRecordDao.getLast30DaysStats()
    }
    
    fun getAllRecordsFlow(): Flow<List<DrinkRecord>> {
        return drinkRecordDao.getAllRecordsFlow()
    }
    
    suspend fun deleteRecord(record: DrinkRecord) {
        drinkRecordDao.deleteRecord(record)
    }
    
    private fun getCurrentDateString(): String {
        val calendar = Calendar.getInstance()
        return "${calendar.get(Calendar.YEAR)}-${String.format("%02d", calendar.get(Calendar.MONTH) + 1)}-${String.format("%02d", calendar.get(Calendar.DAY_OF_MONTH))}"
    }
} 