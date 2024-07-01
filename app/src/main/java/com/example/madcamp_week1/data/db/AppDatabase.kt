package com.example.madcamp_week1.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [ImageDetail::class], version = 3)
abstract class AppDatabase : RoomDatabase() {
    abstract fun imageDetailDao(): ImageDetailDao
}
