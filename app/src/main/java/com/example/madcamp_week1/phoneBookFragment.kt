package com.example.madcamp_week1

import android.Manifest
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.content.Context
import android.database.ContentObserver
import android.widget.*
import android.widget.BaseAdapter
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import android.app.Activity
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import android.content.ContentResolver
import android.provider.ContactsContract
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import androidx.core.app.ActivityCompat
import com.bumptech.glide.Glide

class phoneBookFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_phone, container, false)
    }
    private val nameList = ArrayList<String>()
    private val numList = ArrayList<String>()
    private val photoUriList = ArrayList<String>()
    private var syncedNameList = ArrayList<String>()
    private var syncedNumList = ArrayList<String>()
    private var syncedPhotoUriList = ArrayList<String>()

    private lateinit var addContactLauncher: ActivityResultLauncher<Intent>
    private var selectedPosition: Int = -1
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var contentObserver: ContentObserver

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar = view.findViewById<Toolbar>(R.id.phonebook_toolbar)
        (activity as? AppCompatActivity)?.setSupportActionBar(toolbar)
        (activity as? AppCompatActivity)?.supportActionBar?.title = ""

        requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                loadContacts()
                syncedNameList = ArrayList(nameList)
                syncedNumList = ArrayList(numList)
                syncedPhotoUriList = ArrayList(photoUriList)
                registerContentObserver()
            } else {
                Toast.makeText(context, "Contacts permission denied", Toast.LENGTH_SHORT).show()
            }
        }

        val addButton = view.findViewById<TextView>(R.id.add_button)
        val updateButton = view.findViewById<TextView>(R.id.update_button)

        addButton.setOnClickListener {
            val intent = Intent(activity, phoneItemAddActivity::class.java)
            addContactLauncher.launch(intent)
        }

        val list = view.findViewById<ListView>(R.id.listView)

        val adapter = CustomAdapter(requireContext())
        list.adapter = adapter

        updateButton.setOnClickListener {
            retainSyncedItems()
            adapter.notifyDataSetChanged()
        }

        list.setOnItemClickListener { parent, view, position, id ->
            val name = nameList[position]
            val number = numList[position]
            val photoUri = photoUriList[position]
            selectedPosition = position

            val profileFragment = phoneProfileFragment.newInstance(name, number, photoUri)

            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, profileFragment)
                .addToBackStack(null)
                .commit()
        }

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            loadContacts()
            syncedNameList = ArrayList(nameList)
            syncedNumList = ArrayList(numList)
            syncedPhotoUriList = ArrayList(photoUriList)
            registerContentObserver()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
        }

        addContactLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val name = result.data?.getStringExtra("name")
                val number = result.data?.getStringExtra("number")
                if (name != null && number != null) {
                    nameList.add(name)
                    numList.add(number)
                    photoUriList.add("")
                    sortLists()
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
            val photoUriIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI)

            while (cursor.moveToNext()) {
                val name = cursor.getString(nameIndex)
                val number = cursor.getString(numberIndex)
                val photoUri = cursor.getString(photoUriIndex) ?: ""
                if (!nameList.contains(name) && !numList.contains(number)) {
                    nameList.add(name)
                    numList.add(number)
                    photoUriList.add(photoUri)
                }
            }
            sortLists()
            (view?.findViewById<ListView>(R.id.listView)?.adapter as CustomAdapter).notifyDataSetChanged()
        }
    }

    private fun sortLists() {
        val sortedList = nameList.zip(numList).zip(photoUriList) { (name, number), photoUri -> Triple(name, number, photoUri) }
            .sortedBy { it.first }
        nameList.clear()
        numList.clear()
        photoUriList.clear()
        for ((name, number, photoUri) in sortedList) {
            nameList.add(name)
            numList.add(number)
            photoUriList.add(photoUri)
        }
    }

    private fun registerContentObserver() {
        contentObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                super.onChange(selfChange)
                loadContacts()
                (view?.findViewById<ListView>(R.id.listView)?.adapter as CustomAdapter).notifyDataSetChanged()
            }
        }
        requireContext().contentResolver.registerContentObserver(
            ContactsContract.Contacts.CONTENT_URI,
            true,
            contentObserver
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireContext().contentResolver.unregisterContentObserver(contentObserver)
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
            val photoItem = itemView.findViewById<ImageView>(R.id.contact_image)

            nameItem.text = nameList[position]
            numItem.text = numList[position]

            val photoUri = photoUriList[position]
            if (photoUri.isNotEmpty()) {
                Glide.with(context)
                    .load(photoUri)
                    .placeholder(R.drawable.person)
                    .into(photoItem)
            } else {
                photoItem.setImageResource(R.drawable.person)
            }

            return itemView
        }
    }

    private fun retainSyncedItems() {
        val newList = nameList.zip(numList).zip(photoUriList) { (name, num), photoUri ->
            Triple(name, num, photoUri)
        }.filter { (name, num, _) ->
            syncedNameList.contains(name) && syncedNumList.contains(num)
        }
        nameList.clear()
        numList.clear()
        photoUriList.clear()
        for ((name, number, photoUri) in newList) {
            nameList.add(name)
            numList.add(number)
            photoUriList.add(photoUri)
        }
    }
}