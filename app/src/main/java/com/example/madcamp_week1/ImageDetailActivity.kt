package com.example.madcamp_week1

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
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
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.FragmentTransaction
import com.example.madcamp_week1.data.repository.PhoneBookRepository

class ImageDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityImageDetailBinding
    private var imageUrl: String? = null
    private lateinit var repository: ImageDetailRepository
    private lateinit var PBrepository: PhoneBookRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImageDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 데이터베이스 인스턴스 초기화
        val db = DatabaseProvider.getDatabase(applicationContext)
        repository = ImageDetailRepository(db.imageDetailDao())
        PBrepository = PhoneBookRepository(db.phoneBookDao())

        imageUrl = intent.getStringExtra("image_url")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestMediaPermissions()
        } else {
            loadImage()
        }

        val toolbar = findViewById<Toolbar>(R.id.image_detail_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title = ""
        toolbar.setNavigationOnClickListener {
            cancelImageDetail()
        }

        binding.saveButton.setOnClickListener {
            val date = binding.dateEditText.text.toString()
            val place = binding.placeEditText.text.toString()
            val people = binding.peopleEditText.text.toString()
            val memo = binding.memoEditText.text.toString()
            saveData(date, place, people, memo)
        }

        binding.detailImageView.setOnClickListener {
            val intent = Intent(this, ZoomActivity::class.java)
            intent.putExtra("image_url", imageUrl)
            startActivity(intent)
        }

        binding.addButton.setOnClickListener {
            showContactsDialog()
        }

        binding.addButton.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                showContactsDialog()
            }
        }

        // 클릭 가능한 텍스트 설정
        binding.peopleEditText.movementMethod = LinkMovementMethod.getInstance()

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
        imageUrl?.let { url ->
            lifecycleScope.launch {
                val imageUri = repository.getImageUrl(url)
                imageUri?.let {
                    withContext(Dispatchers.Main) {
                        Glide.with(this@ImageDetailActivity)
                            .load(it)
                            .into(binding.detailImageView)
                    }
                    loadData()
                } ?: showErrorAndFinish()
            }
        } ?: showErrorAndFinish()
    }

    private fun loadData() {
        imageUrl?.let { url ->
            lifecycleScope.launch {
                val imageDetail = repository.getImageDetail(url)
                imageDetail?.let {
                    binding.dateEditText.setText(it.date)
                    binding.placeEditText.setText(it.place)
                    binding.memoEditText.setText(it.memo)
                    setPeopleSpannableText(it.people)
                }
            }
        }
    }

    private fun saveData(date: String, place: String, people: String, memo: String) {
        imageUrl?.let { url ->
            lifecycleScope.launch(Dispatchers.IO) {
                val imageDetail = repository.getImageDetail(url)

                if (imageDetail != null) {
                    // 기존 데이터가 있으면 업데이트
                    imageDetail.date = date
                    imageDetail.place = place
                    imageDetail.memo = memo
                    imageDetail.people = people
                    repository.update(imageDetail)
                } else {
                    // 새로운 데이터로 추가
                    val newImageDetail = ImageDetail(url, date, place, memo, people, url)
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

    private fun showContactsDialog() {
        lifecycleScope.launch(Dispatchers.IO) {
            val contacts = PBrepository.getAllContact()
            val contactNames = contacts.map { it.name }.toTypedArray()
            val selectedContacts = mutableListOf<String>()
            val selectedItems = BooleanArray(contactNames.size)

            withContext(Dispatchers.Main) {
                AlertDialog.Builder(this@ImageDetailActivity)
                    .setTitle("Select people")
                    .setMultiChoiceItems(contactNames, selectedItems) { _, which, isChecked ->
                        if (isChecked) {
                            contactNames[which]?.let { selectedContacts.add(it) }
                        } else {
                            selectedContacts.remove(contactNames[which])
                        }
                    }
                    .setPositiveButton("OK") { _, _ ->
                        binding.peopleEditText.setText(selectedContacts.joinToString(", "))
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        }
    }
    private fun setPeopleSpannableText(people: String?) {
        if (people.isNullOrEmpty()) return

        val spannableString = SpannableString(people)
        val names = people.split(", ")

        var start = 0
        for (name in names) {
            val end = start + name.length
            val clickableSpan = object : ClickableSpan() {
                override fun onClick(widget: View) {
                    lifecycleScope.launch {
                        val contact = PBrepository.getContactByName(name)
                        if (contact != null) {
                            val intent = contact.id?.let {
                                contact.name?.let { it1 ->
                                    contact.num?.let { it2 ->
                                        phoneProfileActivity.newIntent(
                                            this@ImageDetailActivity,
                                            it, it1, it2, contact.image

                                        )
                                    }
                                }
                            }
                            Log.d("ImageDetailActivity","${contact.name}")
                            startActivity(intent)
                        } else {

                            Toast.makeText(this@ImageDetailActivity, "Contact not found", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            spannableString.setSpan(clickableSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            start = end + 2 // ", " 를 고려하여 2를 더해줌
        }

        binding.peopleEditText.setText(spannableString, TextView.BufferType.SPANNABLE)
    }

    private fun cancelImageDetail() {
        setResult(Activity.RESULT_CANCELED)
        finish()
    }
}



