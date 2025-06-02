package com.example.reminder.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.reminder.features.drink.data.database.DrinkRecord
import com.example.reminder.features.drink.data.database.DrinkRecordDao

@Database(
    entities = [DrinkRecord::class],
    version = 1,
    exportSchema = false
)
abstract class DrinkDatabase : RoomDatabase() {
    
    abstract fun drinkRecordDao(): DrinkRecordDao
    
    companion object {
        @Volatile
        private var INSTANCE: DrinkDatabase? = null
        
        fun getDatabase(context: Context): DrinkDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DrinkDatabase::class.java,
                    "drink_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
} 