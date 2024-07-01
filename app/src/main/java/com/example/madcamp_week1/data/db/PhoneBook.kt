package com.example.madcamp_week1.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "phone_book")
data class PhoneBook(
    @PrimaryKey(autoGenerate = true)
    var id: Long? = null,
    var name: String?,
    var num: String?,
    val image: String?
)