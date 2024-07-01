package com.example.madcamp_week1.data.repository

import android.util.Log
import com.example.madcamp_week1.data.db.PhoneBook
import com.example.madcamp_week1.data.db.PhoneBookDao

class PhoneBookRepository(private val phoneBookDao: PhoneBookDao) {

    suspend fun insert(phoneBook: PhoneBook) {
        phoneBookDao.insert(phoneBook)
    }

    suspend fun update(phoneBook: PhoneBook) {
        phoneBookDao.update(phoneBook)
    }

    suspend fun getAllContact(): List<PhoneBook> {
        return phoneBookDao.getAllContact()
    }

    suspend fun getContact(id: Long): PhoneBook {
        return phoneBookDao.getContact(id)
    }
    suspend fun deleteAll() {
        phoneBookDao.deleteAll()
    }

    suspend fun getContactByNameAndNumber(name: String, num: String): PhoneBook? {
        return phoneBookDao.getContactByNameAndNumber(name, num)
    }

}