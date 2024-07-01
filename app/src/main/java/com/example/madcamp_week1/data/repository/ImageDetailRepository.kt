package com.example.madcamp_week1.data.repository

import com.example.madcamp_week1.data.db.ImageDetail
import com.example.madcamp_week1.data.db.ImageDetailDao

class ImageDetailRepository(private val imageDetailDao: ImageDetailDao) {

    suspend fun insert(imageDetail: ImageDetail) {
        imageDetailDao.insert(imageDetail)
    }

    suspend fun update(imageDetail: ImageDetail) {
        imageDetailDao.update(imageDetail)
    }

    suspend fun getImageDetail(url: String): ImageDetail? {
        return imageDetailDao.getImageDetail(url)
    }

    suspend fun getAllImages(): List<ImageDetail> {
        return imageDetailDao.getAllImages()
    }
}
