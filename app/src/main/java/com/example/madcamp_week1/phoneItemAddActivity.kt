package com.example.madcamp_week1

import android.app.Activity
import android.content.ContentProviderOperation
import android.content.Intent
import android.os.Bundle
import android.provider.ContactsContract
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import android.text.TextWatcher
import android.text.Editable

class phoneItemAddActivity : AppCompatActivity() {
    private val textLength = 13

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.phone_itemadd)

        val nameEditText = findViewById<EditText>(R.id.name)
        val numEditText = findViewById<EditText>(R.id.num)


        numEditText.addTextChangedListener(object : TextWatcher {
            private var current = ""

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.toString() != current) {
                    val cleanString = s.toString().replace("[^\\d]".toRegex(), "")
                    val formattedString = StringBuilder()

                    if (cleanString.length > 3) {
                        formattedString.append(cleanString.substring(0, 3)).append("-")
                        if (cleanString.length > 7) {
                            formattedString.append(cleanString.substring(3, 7)).append("-")
                            formattedString.append(cleanString.substring(7))
                        } else if (cleanString.length > 3) {
                            formattedString.append(cleanString.substring(3))
                        }
                    } else {
                        formattedString.append(cleanString)
                    }

                    current = formattedString.toString()
                    numEditText.removeTextChangedListener(this)
                    numEditText.setText(current)
                    numEditText.setSelection(current.length.coerceAtMost(textLength))
                    numEditText.addTextChangedListener(this)
                }
            }
            override fun afterTextChanged(s: Editable) {}
        })

        val btnAdd = findViewById<Button>(R.id.btnAdd)

        btnAdd.setOnClickListener {
            val name = nameEditText.text.toString()
            val number = numEditText.text.toString()

            if (name.isNotEmpty() && number.isNotEmpty() && number.length == textLength) {
                val resultIntent = Intent()
                resultIntent.putExtra("name", name)
                resultIntent.putExtra("number", number)
                setResult(Activity.RESULT_OK, resultIntent)
                finish()
            } else {
                Toast.makeText(this, "Please enter correct phone number", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
