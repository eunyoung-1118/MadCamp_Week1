package com.example.madcamp_week1

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment


class phoneProfileFragment : Fragment() {

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
            parentFragmentManager.popBackStack()
        }

        val name = arguments?.getString("name")
        val number = arguments?.getString("number")

        val nameTextView = view.findViewById<TextView>(R.id.profile_name)
        val numberTextView = view.findViewById<TextView>(R.id.profile_num)

        nameTextView.text = name
        numberTextView.text = number
    }

    companion object {
        fun newInstance(name: String, number: String): phoneProfileFragment {
            val fragment = phoneProfileFragment()
            val args = Bundle()
            args.putString("name", name)
            args.putString("number", number)
            fragment.arguments = args
            return fragment
        }
    }
}
