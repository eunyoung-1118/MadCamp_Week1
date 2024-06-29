package com.example.madcamp_week1

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Geocoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.madcamp_week1.databinding.ActivityImageDetailBinding
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.media.ExifInterface

class ImageDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityImageDetailBinding
    private var imageUrl: String? = null
    private lateinit var sharedPreferences: SharedPreferences
    private var valid = false
    private var latitude: Float? = null
    private var longitude: Float? = null
    private lateinit var geocoder: Geocoder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImageDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        geocoder = Geocoder(this)

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
        binding.detailImageView.setOnClickListener {
            val intent = Intent(this, ZoomActivity::class.java)
            intent.putExtra("image_url", imageUrl)
            startActivity(intent)
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun requestMediaPermissions() {
        val permissions = listOf(
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.READ_MEDIA_VIDEO,
            Manifest.permission.READ_MEDIA_AUDIO,
            Manifest.permission.ACCESS_MEDIA_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
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
            setExifData() // Set the date and location from the photo's EXIF data
            loadData() // Load saved data after loading the image
        } ?: showErrorAndFinish()
    }

    private fun setExifData() {
        try {
            imageUrl?.let { url ->
                val inputStream = contentResolver.openInputStream(Uri.parse(url))
                inputStream?.let {
                    val exif = ExifInterface(it)

                    // Log all EXIF attributes
                    logAllExifAttributes(exif)

                    // Set date
                    val exifDate = exif.getAttribute(ExifInterface.TAG_DATETIME)
                    val formatter = SimpleDateFormat("yyyy:MM:dd HH:mm:ss", Locale.getDefault())
                    val date: Date? = exifDate?.let { formatter.parse(it) }
                    val displayFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    val displayDate = date?.let { displayFormatter.format(it) } ?: ""
                    binding.dateEditText.setText(displayDate)
                    Log.d("ImageDetailActivity", "EXIF Date: $displayDate")

                    // Get and set address from EXIF GPS data
                    val attrLATITUDE = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE)
                    val attrLATITUDE_REF = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF)
                    val attrLONGITUDE = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE)
                    val attrLONGITUDE_REF = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF)

                    if (attrLATITUDE != null && attrLATITUDE_REF != null && attrLONGITUDE != null && attrLONGITUDE_REF != null) {
                        valid = true
                        latitude = if (attrLATITUDE_REF == "N") {
                            convertToDegree(attrLATITUDE)
                        } else {
                            0 - convertToDegree(attrLATITUDE)
                        }
                        longitude = if (attrLONGITUDE_REF == "E") {
                            convertToDegree(attrLONGITUDE)
                        } else {
                            0 - convertToDegree(attrLONGITUDE)
                        }
                    }

                    if (latitude != null && longitude != null) {
                        val list = geocoder.getFromLocation(latitude!!.toDouble(), longitude!!.toDouble(), 10)
                        if (list != null && list.isNotEmpty()) {
                            val address = list[0].getAddressLine(0)
                            binding.placeEditText.setText(address)
                            Log.d("ImageDetailActivity", "Address from GPS: $address")
                        } else {
                            binding.placeEditText.setText("Address not available")
                            Log.d("ImageDetailActivity", "No address data found from GPS")
                        }
                    } else {
                        binding.placeEditText.setText("Address not available")
                        Log.d("ImageDetailActivity", "No GPS data available")
                    }

                    it.close()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            binding.dateEditText.setText("") // Clear date field if there's an error
            binding.placeEditText.setText("Address not available") // Clear address field if there's an error
            Log.e("ImageDetailActivity", "Error reading EXIF data", e)
        }
    }

    private fun convertToDegree(stringDMS: String): Float {
        val dms = stringDMS.split(",", " ")
        val degrees = dms[0].split("/")[0].toDouble() / dms[0].split("/")[1].toDouble()
        val minutes = dms[1].split("/")[0].toDouble() / dms[1].split("/")[1].toDouble() / 60
        val seconds = dms[2].split("/")[0].toDouble() / dms[2].split("/")[1].toDouble() / 3600
        return (degrees + minutes + seconds).toFloat()
    }

    private fun logAllExifAttributes(exif: ExifInterface) {
        val tags = exif.javaClass.declaredFields
            .filter { it.name.startsWith("TAG_") }
            .map { it.get(null) as String }
            .toTypedArray()

        for (tag in tags) {
            val value = exif.getAttribute(tag)
            if (value != null) {
                Log.d("ImageDetailActivity", "EXIF $tag: $value")
            }
        }
    }

    private fun loadData() {
        imageUrl?.let { url ->
            binding.dateEditText.setText(sharedPreferences.getString("${url}_date", binding.dateEditText.text.toString()))
            binding.placeEditText.setText(sharedPreferences.getString("${url}_place", binding.placeEditText.text.toString()))
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
