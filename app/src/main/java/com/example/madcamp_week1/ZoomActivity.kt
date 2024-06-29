package com.example.madcamp_week1

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide

class ZoomActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_zoom)

        val imageView = findViewById<ImageView>(R.id.zoomImageView)
        val imageUrl = intent.getStringExtra("image_url")

        imageUrl?.let {
            Glide.with(this)
                .load(it)
                .into(imageView)
        }

        imageView.setOnClickListener {
            finish()
        }
    }
}
