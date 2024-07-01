package com.example.madcamp_week1

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.madcamp_week1.data.db.ImageDetail
import com.example.madcamp_week1.data.repository.ImageDetailRepository
import com.example.madcamp_week1.databinding.ActivityImageDetailBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.madcamp_week1.data.repository.DatabaseProvider
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager

class ImageDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityImageDetailBinding
    private var imageUrl: String? = null
    private lateinit var repository: ImageDetailRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImageDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 데이터베이스 인스턴스 초기화
        val db = DatabaseProvider.getDatabase(applicationContext)
        repository = ImageDetailRepository(db.imageDetailDao())

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

        binding.root.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                hideKeyboard()
                v.performClick()
            }
            false
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
            loadData()
        } ?: showErrorAndFinish()
    }


    private fun loadData() {
        imageUrl?.let { url ->
            lifecycleScope.launch {
                val imageDetail = withContext(Dispatchers.IO) {
                    repository.getImageDetail(url)
                }
                imageDetail?.let {
                    binding.dateEditText.setText(it.date)
                    binding.placeEditText.setText(it.place)
                    binding.memoEditText.setText(it.memo)
                }
            }
        }
    }

    private fun saveData(date: String, place: String, memo: String) {
        imageUrl?.let { url ->
            lifecycleScope.launch(Dispatchers.IO) {
                val imageDetail = withContext(Dispatchers.IO) {
                    repository.getImageDetail(url)
                }

                if (imageDetail != null) {
                    // 기존 데이터가 있으면 업데이트
                    imageDetail.date = date
                    imageDetail.place = place
                    imageDetail.memo = memo
                    repository.update(imageDetail)
                } else {
                    // 새로운 데이터로 추가
                    val newImageDetail = ImageDetail(url, date, place, memo, url)
                    repository.insert(newImageDetail)
                }

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ImageDetailActivity, "Data saved", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }

    private fun showErrorAndFinish() {
        Toast.makeText(this, "Invalid image URL", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun hideKeyboard() {
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val currentFocusView = currentFocus
        currentFocusView?.let {
            inputMethodManager.hideSoftInputFromWindow(it.windowToken, 0)
        }
    }
}
