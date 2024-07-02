package com.example.madcamp_week1.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface PhoneBookDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(phoneBook: PhoneBook)

    @Update
    suspend fun update(phoneBook: PhoneBook)

    @Query("SELECT * FROM phone_book")
    suspend fun getAllContact(): List<PhoneBook>


    @Query("SELECT * FROM phone_book WHERE id = :id LIMIT 1")
    suspend fun getContact(id: Long): PhoneBook

    @Query("DELETE FROM phone_book")
    suspend fun deleteAll()

    @Query("SELECT * FROM phone_book WHERE name = :name AND num = :num LIMIT 1")
    suspend fun getContactByNameAndNumber(name: String, num: String): PhoneBook?

    @Query("SELECT * FROM phone_book WHERE name = :name LIMIT 1")
    suspend fun getContactByName(name: String): PhoneBook
}