package com.example.appio_test_2.data.repository.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.appio_test_2.utils.Constants.DATABASE_NAME

@Database(
    [PlaceEntity::class],
    version = 1,
    exportSchema = false
)
abstract class PlaceDatabase : RoomDatabase() {
    abstract fun getPlace(): PlaceDao

    companion object {
        @Volatile
        private var INSTANCE: PlaceDatabase? = null

        fun getDatabase(context: Context): PlaceDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PlaceDatabase::class.java,
                    DATABASE_NAME
                ).build()
                INSTANCE = instance
                return instance
            }
        }
    }
}