package com.example.madcamp_week1

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.madcamp_week1.databinding.ActivityImageDetailBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ImageDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityImageDetailBinding
    private var imageUrl: String? = null
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImageDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences("ImageDetails", Context.MODE_PRIVATE)
        imageUrl = intent.getStringExtra("image_url")

        // Request permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestMediaPermissions()
        } else {
            loadImage()
        }

        // Save button click listener
        binding.saveButton.setOnClickListener {
            val date = binding.dateEditText.text.toString()
            val place = binding.placeEditText.text.toString()
            val memo = binding.memoEditText.text.toString()
            saveData(date, place, memo)
            finish()
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun requestMediaPermissions() {
        val permissions = listOf(
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.READ_MEDIA_VIDEO,
            Manifest.permission.READ_MEDIA_AUDIO,
            Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
        )

        val permissionsToRequest = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (permissionsToRequest.isNotEmpty()) {
            permissionLauncher.launch(permissionsToRequest.toTypedArray())
        } else {
            loadImage()
        }
    }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.values.all { it }) {
            loadImage()
        } else {
            Toast.makeText(this, "Permissions are required for the app to function properly", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun loadImage() {
        imageUrl?.let {
            Glide.with(this)
                .load(it)
                .into(binding.detailImageView)
            setDate() // Set the date to the current date
            loadData() // Load saved data after loading the image
        } ?: showErrorAndFinish()
    }

    private fun setDate() {
        val currentDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        binding.dateEditText.setText(currentDate)
    }

    private fun loadData() {
        imageUrl?.let { url ->
            binding.dateEditText.setText(sharedPreferences.getString("${url}_date", binding.dateEditText.text.toString()))
            binding.placeEditText.setText(sharedPreferences.getString("${url}_place", ""))
            binding.memoEditText.setText(sharedPreferences.getString("${url}_memo", ""))
        }
    }

    private fun saveData(date: String, place: String, memo: String) {
        imageUrl?.let { url ->
            with(sharedPreferences.edit()) {
                putString("${url}_date", date)
                putString("${url}_place", place)
                putString("${url}_memo", memo)
                apply()
            }
        }
    }

    private fun showErrorAndFinish() {
        Toast.makeText(this, "Invalid image URL", Toast.LENGTH_SHORT).show()
        finish()
    }
}
