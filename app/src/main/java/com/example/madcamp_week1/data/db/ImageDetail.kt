package com.example.madcamp_week1.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "image_details")
data class ImageDetail(
    @PrimaryKey val url: String,
    var date: String?,
    var place: String?,
    var memo: String?,
    val imageUri: String? // 새로운 필드 추가

)
