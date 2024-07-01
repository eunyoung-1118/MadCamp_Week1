package com.example.madcamp_week1.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface ImageDetailDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(imageDetail: ImageDetail)

    @Update
    suspend fun update(imageDetail: ImageDetail)

    @Query("SELECT * FROM image_details")
    suspend fun getAllImages(): List<ImageDetail>

    @Query("SELECT * FROM image_details WHERE url = :url LIMIT 1")
    suspend fun getImageDetail(url: String): ImageDetail?

    @Delete
    suspend fun deleteImage(imageDetail: ImageDetail)

    @Query("SELECT imageUri FROM image_details WHERE url = :url LIMIT 1")
    suspend fun getImageUrl(url: String): String?
}
