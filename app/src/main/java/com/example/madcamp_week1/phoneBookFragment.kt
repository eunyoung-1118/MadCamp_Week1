package com.example.madcamp_week1

import android.Manifest
import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.ContactsContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment

class phoneBookFragment : Fragment() {

    private val nameList = ArrayList<String>()
    private val numList = ArrayList<String>()
    private lateinit var addContactLauncher: androidx.activity.result.ActivityResultLauncher<Intent>
    private lateinit var requestPermissionLauncher: androidx.activity.result.ActivityResultLauncher<String>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_phone, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar = view.findViewById<Toolbar>(R.id.phonebook_toolbar)
        (activity as? AppCompatActivity)?.setSupportActionBar(toolbar)
        (activity as? AppCompatActivity)?.supportActionBar?.title = ""

        requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                loadContacts()
            } else {
                Toast.makeText(context, "Contacts permission denied", Toast.LENGTH_SHORT).show()
            }
        }

        val addButton = view.findViewById<TextView>(R.id.add_button)
        addButton.setOnClickListener {
            val intent = Intent(activity, phoneItemAddActivity::class.java)
            addContactLauncher.launch(intent)
        }

        val list = view.findViewById<ListView>(R.id.listView)
        val adapter = CustomAdapter(requireContext())
        list.adapter = adapter

        list.setOnItemClickListener { parent, view, position, id ->
            val name = nameList[position]
            val number = numList[position]
            val profileFragment = phoneProfileFragment.newInstance(name, number)

            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, profileFragment)
                .addToBackStack(null)
                .commit()
        }

        list.setOnItemLongClickListener { parent, view, position, id ->
            nameList.removeAt(position)
            numList.removeAt(position)
            adapter.notifyDataSetChanged()
            true
        }

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            loadContacts()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
        }

        addContactLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                val name = data?.getStringExtra("name")
                val number = data?.getStringExtra("number")
                if (name != null && number != null) {
                    nameList.add(name)
                    numList.add(number)
                    (view?.findViewById<ListView>(R.id.listView)?.adapter as CustomAdapter).notifyDataSetChanged()
                }
            }
        }
    }

    private fun loadContacts() {
        val contentResolver: ContentResolver = requireContext().contentResolver
        val cursor = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null, null, null, null
        )

        cursor?.use {
            val nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

            while (cursor.moveToNext()) {
                val name = cursor.getString(nameIndex)
                val number = cursor.getString(numberIndex)
                nameList.add(name)
                numList.add(number)
            }
            (view?.findViewById<ListView>(R.id.listView)?.adapter as CustomAdapter).notifyDataSetChanged()
        }
    }

    private inner class CustomAdapter(private val context: Context) : BaseAdapter() {
        override fun getCount(): Int {
            return nameList.size
        }

        override fun getItem(i: Int): Any? {
            return null
        }

        override fun getItemId(i: Int): Long {
            return 0
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val itemView: View = convertView ?: View.inflate(context, R.layout.phone_item, null)
            val nameItem = itemView.findViewById<TextView>(R.id.nameItem)
            val numItem = itemView.findViewById<TextView>(R.id.numItem)

            nameItem.text = nameList[position]
            numItem.text = numList[position]
            return itemView
        }
    }
}
