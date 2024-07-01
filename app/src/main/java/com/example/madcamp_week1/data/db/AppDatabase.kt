package com.example.madcamp_week1.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [ImageDetail::class, PhoneBook::class], version = 4)
abstract class AppDatabase : RoomDatabase() {
    abstract fun imageDetailDao(): ImageDetailDao
    abstract fun phoneBookDao(): PhoneBookDao
}
