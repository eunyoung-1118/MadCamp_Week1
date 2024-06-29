package com.example.madcamp_week1

import android.os.Bundle
import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide


class phoneProfileFragment : Fragment() {
    private lateinit var nameTextView: TextView
    private lateinit var numberTextView: TextView
    private lateinit var profileImageView: ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.phone_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar = view.findViewById<Toolbar>(R.id.profile_toolbar)
        (activity as? AppCompatActivity)?.setSupportActionBar(toolbar)
        (activity as? AppCompatActivity)?.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        (activity as? AppCompatActivity)?.supportActionBar?.setDisplayShowHomeEnabled(true)
        (activity as? AppCompatActivity)?.supportActionBar?.title = ""
        toolbar.setNavigationOnClickListener {
            cancelProfile()
        }

        nameTextView = view.findViewById(R.id.profile_name)
        numberTextView = view.findViewById(R.id.profile_num)
        profileImageView = view.findViewById(R.id.profile_image)

        val name = arguments?.getString("name")
        val number = arguments?.getString("number")
        val photoUri = arguments?.getString("photoUri")

        nameTextView.text = name
        numberTextView.text = number

        if (photoUri != null && photoUri.isNotEmpty()) {
            Glide.with(this)
                .load(photoUri)
                .placeholder(R.drawable.person)
                .into(profileImageView)
        } else {
            profileImageView.setImageResource(R.drawable.person)
        }
    }

    private fun cancelProfile() {
        activity?.setResult(Activity.RESULT_CANCELED)
        activity?.supportFragmentManager?.popBackStack()
    }

    companion object {
        fun newInstance(name: String, number: String, photoUri: String): phoneProfileFragment {
            val fragment = phoneProfileFragment()
            val args = Bundle()
            args.putString("name", name)
            args.putString("number", number)
            args.putString("photoUri", photoUri)
            fragment.arguments = args
            return fragment
        }
    }
}
