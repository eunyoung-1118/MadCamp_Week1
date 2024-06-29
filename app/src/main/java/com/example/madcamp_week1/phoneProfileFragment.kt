package com.example.madcamp_week1

import android.os.Bundle
import android.app.Activity
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment



class phoneProfileFragment : Fragment() {
    private lateinit var nameTextView: TextView
    private lateinit var numberTextView: TextView

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

        val name = arguments?.getString("name")
        val number = arguments?.getString("number")

        nameTextView.text = name
        numberTextView.text = number

        view.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                imm?.hideSoftInputFromWindow(view.windowToken, 0)
            }
            false
        }
    }

    private fun cancelProfile() {
        activity?.setResult(Activity.RESULT_CANCELED)
        activity?.supportFragmentManager?.popBackStack()
    }

    private fun hideKeyboard(view: View) {
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun showKeyboard(view: View) {
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
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
