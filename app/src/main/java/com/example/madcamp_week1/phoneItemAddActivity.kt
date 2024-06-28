package com.example.madcamp_week1

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.EditText

class phoneItemAddActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.phone_itemadd)

        val nameEditText = findViewById<EditText>(R.id.name)
        val numEditText = findViewById<EditText>(R.id.num)
        val btnAdd = findViewById<Button>(R.id.btnAdd)

        btnAdd.setOnClickListener {
            val name = nameEditText.text.toString()
            val number = numEditText.text.toString()

            val resultIntent = Intent()
            resultIntent.putExtra("name", name)
            resultIntent.putExtra("number", number)
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }
    }
}
