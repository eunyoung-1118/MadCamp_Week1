package com.example.madcamp_week1

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.madcamp_week1.data.db.PhoneBook
import com.example.madcamp_week1.data.repository.PhoneBookRepository
import com.example.madcamp_week1.data.repository.DatabaseProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class phoneProfileActivity : AppCompatActivity() {

    private lateinit var repository: PhoneBookRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.phone_profile)

        repository = PhoneBookRepository(DatabaseProvider.getDatabase(applicationContext).phoneBookDao())

        val toolbar = findViewById<Toolbar>(R.id.profile_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title = ""
        toolbar.setNavigationOnClickListener {
            cancelProfile()
        }

        val callButton = findViewById<ImageView>(R.id.call_button)
        val messageButton = findViewById<ImageView>(R.id.message_button)

        val id = intent.getLongExtra("id", 0L)
        var contact: PhoneBook? = null
        // Coroutine을 사용하여 suspend 함수 호출
        lifecycleScope.launch {
            contact = repository.getContact(id)
            withContext(Dispatchers.Main) {
                updateUI(contact)
            }
        }

        callButton.setOnClickListener {
            val dialIntent = Intent(Intent.ACTION_DIAL)
            dialIntent.data = Uri.parse("tel:${contact?.num}")
            startActivity(dialIntent)
        }

        messageButton.setOnClickListener {
            val smsIntent = Intent(Intent.ACTION_VIEW)
            smsIntent.data = Uri.parse("sms:${contact?.num}")
            startActivity(smsIntent)
        }
    }

    private fun updateUI(contact: PhoneBook?) {
        val profileImageView = findViewById<ImageView>(R.id.profile_image)
        findViewById<TextView>(R.id.profile_name).text = contact?.name
        findViewById<TextView>(R.id.profile_num).text = contact?.num

        if (contact?.image != null && contact.image.isNotEmpty()) {
            profileImageView.setImageURI(Uri.parse(contact.image))
        } else {
            profileImageView.setImageResource(R.drawable.person)
        }
    }

    private fun cancelProfile() {
        setResult(Activity.RESULT_CANCELED)
        finish()
    }

    companion object {
        fun newIntent(context: Context, id: Long, name: String, number: String, photoUri: String?): Intent {
            return Intent(context, phoneProfileActivity::class.java).apply {
                putExtra("id", id)
                putExtra("name", name)
                putExtra("number", number)
                putExtra("photoUri", photoUri)
            }
        }
    }
}
