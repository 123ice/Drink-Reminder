package com.example.reminder.features.drink.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DrinkRecordDao {
    
    @Insert
    suspend fun insertDrinkRecord(record: DrinkRecord)
    
    @Query("SELECT SUM(amount) FROM drink_records WHERE date = :date")
    suspend fun getTotalAmountByDate(date: String): Int?
    
    @Query("SELECT SUM(amount) FROM drink_records WHERE date = :date")
    fun getTotalAmountByDateFlow(date: String): Flow<Int?>
    
    @Query("SELECT * FROM drink_records WHERE date = :date ORDER BY timestamp DESC")
    suspend fun getRecordsByDate(date: String): List<DrinkRecord>
    
    @Query("SELECT * FROM drink_records WHERE date = :date ORDER BY timestamp DESC")
    fun getRecordsByDateFlow(date: String): Flow<List<DrinkRecord>>
    
    @Query("SELECT date, SUM(amount) as totalAmount FROM drink_records GROUP BY date ORDER BY date DESC LIMIT 30")
    suspend fun getLast30DaysStats(): List<DayStats>
    
    @Query("SELECT date, SUM(amount) as totalAmount FROM drink_records GROUP BY date ORDER BY date DESC LIMIT 7")
    suspend fun getLast7DaysStats(): List<DayStats>
    
    @Query("SELECT * FROM drink_records ORDER BY timestamp DESC")
    fun getAllRecordsFlow(): Flow<List<DrinkRecord>>
    
    @Delete
    suspend fun deleteRecord(record: DrinkRecord)
    
    @Query("DELETE FROM drink_records WHERE date = :date")
    suspend fun deleteRecordsByDate(date: String)
}

data class DayStats(
    val date: String,
    val totalAmount: Int
) 