package com.example.madcamp_week1

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.ActivityResultLauncher
import android.content.ContentResolver
import android.provider.ContactsContract
import android.util.Log
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.lifecycleScope
import com.example.madcamp_week1.data.db.PhoneBook
import com.example.madcamp_week1.data.repository.DatabaseProvider
import com.example.madcamp_week1.data.repository.PhoneBookRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class phoneBookFragment : Fragment() {

    private lateinit var repository: PhoneBookRepository
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private var syncedList: MutableList<PhoneBook> = mutableListOf()
    private lateinit var adapter: phoneBookAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Repository 초기화
        val db = DatabaseProvider.getDatabase(requireContext())
        repository = PhoneBookRepository(db.phoneBookDao())

        // UI 준비
        return inflater.inflate(R.layout.fragment_phone, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Listview와 어댑터 설정
        adapter = phoneBookAdapter(emptyList())
        val listView = view.findViewById<ListView>(R.id.listView)
        listView.adapter = adapter

        // ListView 아이템 클릭 리스너 설정
        listView.setOnItemClickListener { _, _, position, _ ->
            val contact = adapter.getItem(position)

            val bundle = Bundle().apply {
                putLong("id", contact.id ?: 0L)
            }

            val fragment = phoneProfileFragment().apply {
                arguments = bundle
            }

            val transaction: FragmentTransaction = parentFragmentManager.beginTransaction()
            transaction.replace(R.id.fragment_container, fragment)
            transaction.addToBackStack(null)
            transaction.commit()
        }

        // 퍼미션 요청 런처 초기화
        requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                loadContactsIfNeeded()
                lifecycleScope.launch(Dispatchers.IO) {
                    syncedList = repository.getAllContact().toMutableList()
                    Log.d("phoneBookFragment", "syncedList: ${syncedList.size}")

                    withContext(Dispatchers.Main) {
                        updateUI(syncedList)
                    }
                }
            } else {
                Toast.makeText(context, "Contacts permission denied", Toast.LENGTH_SHORT).show()
            }
        }

        // 업데이트 버튼 클릭 리스너 설정
        view.findViewById<Button>(R.id.update_button).setOnClickListener {
            requestPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
        }
        requestPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)

    }

    private fun loadContactsIfNeeded() {
        lifecycleScope.launch(Dispatchers.IO) {
            val currentContacts = repository.getAllContact()
            if (currentContacts.isEmpty()) {
                loadContacts()
            }
        }
    }

    private fun loadContacts() {
        lifecycleScope.launch(Dispatchers.IO) {
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

                    val phoneBook = PhoneBook(name = name, num = number, image = photoUri)

                    // 중복 확인 후 삽입
                    val existingContact = repository.getContactByNameAndNumber(name, number)
                    if (existingContact == null) {
                        repository.insert(phoneBook)
                    }
                }
            }
        }
    }

    private suspend fun updateUI(ContactList: List<PhoneBook>) {
        withContext(Dispatchers.Main) {
            val sortedList = ContactList.sortedBy { it.name }
            adapter.updateData(sortedList)
        }
    }
}
