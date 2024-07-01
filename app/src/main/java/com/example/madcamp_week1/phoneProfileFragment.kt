package com.example.madcamp_week1

import android.content.Intent
import android.os.Bundle
import android.app.Activity
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.madcamp_week1.data.db.PhoneBook
import com.example.madcamp_week1.data.repository.PhoneBookRepository
import com.example.madcamp_week1.data.repository.DatabaseProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class phoneProfileFragment : Fragment() {

    private lateinit var repository: PhoneBookRepository

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.phone_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        repository = PhoneBookRepository(DatabaseProvider.getDatabase(requireContext()).phoneBookDao())

        val toolbar = view.findViewById<Toolbar>(R.id.profile_toolbar)
        (activity as? AppCompatActivity)?.setSupportActionBar(toolbar)
        (activity as? AppCompatActivity)?.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        (activity as? AppCompatActivity)?.supportActionBar?.setDisplayShowHomeEnabled(true)
        (activity as? AppCompatActivity)?.supportActionBar?.title = ""
        toolbar.setNavigationOnClickListener {
            cancelProfile()
        }

        val callButton = view.findViewById<ImageView>(R.id.call_button)
        val messageButton = view.findViewById<ImageView>(R.id.message_button)

        val id = arguments?.getLong("id")
        var contact: PhoneBook? =null
        // Coroutine을 사용하여 suspend 함수 호출
        if (id != null) {
            lifecycleScope.launch {
                contact = repository.getContact(id)
                withContext(Dispatchers.Main) {
                    updateUI(view, contact)
                }
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

    private fun updateUI(view: View, contact: PhoneBook?) {
        val profileImageView = view.findViewById<ImageView>(R.id.profile_image)
        view.findViewById<TextView>(R.id.profile_name).text = contact?.name
        view.findViewById<TextView>(R.id.profile_num).text = contact?.num

        if (contact?.image != null && contact.image.isNotEmpty()) {
            profileImageView.setImageURI(Uri.parse(contact.image))
        } else {
            profileImageView.setImageResource(R.drawable.person)
        }
    }

    private fun cancelProfile() {
        activity?.setResult(Activity.RESULT_CANCELED)
        activity?.supportFragmentManager?.popBackStack()
    }

    companion object {
        fun newInstance(id: Long, name: String, number: String, photoUri: String): phoneProfileFragment {
            val fragment = phoneProfileFragment()
            val args = Bundle()
            args.putLong("id", id)
            args.putString("name", name)
            args.putString("number", number)
            args.putString("photoUri", photoUri)
            fragment.arguments = args
            return fragment
        }
    }
}
