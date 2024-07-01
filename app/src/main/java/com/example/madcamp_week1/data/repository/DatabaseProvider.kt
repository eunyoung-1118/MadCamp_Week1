// DatabaseProvider.kt
package com.example.madcamp_week1.data.repository

import android.content.Context
import androidx.room.Room
import com.example.madcamp_week1.data.db.AppDatabase

object DatabaseProvider {
    private var db: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
        return db ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "database-name"
            )
                .fallbackToDestructiveMigration()
                .build()
            db = instance
            instance
        }
    }
}
