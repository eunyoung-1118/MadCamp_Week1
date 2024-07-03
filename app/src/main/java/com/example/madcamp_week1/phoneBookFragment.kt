package com.example.madcamp_week1

import android.Manifest
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
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.lifecycleScope
import com.example.madcamp_week1.data.db.PhoneBook
import com.example.madcamp_week1.data.repository.DatabaseProvider
import com.example.madcamp_week1.data.repository.PhoneBookRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.appcompat.widget.SearchView

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

        // ListView와 어댑터 설정
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
                updateContacts()
            } else {
                Toast.makeText(context, "Contacts permission denied", Toast.LENGTH_SHORT).show()
            }
        }

        // 업데이트 버튼 클릭 리스너 설정
        view.findViewById<Button>(R.id.update_button).setOnClickListener {
            requestPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
        }

        // SearchView 설정
        val searchView: SearchView = view.findViewById(R.id.search_view)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { filterContacts(it) }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let { filterContacts(it) }
                return true
            }
        })

        // 최초 권한 요청 및 데이터 로드
        requestPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
    }

    private fun updateContacts() {
        lifecycleScope.launch(Dispatchers.IO) {
            // 기존 연락처 모두 삭제
            repository.deleteAll()

            // 새 연락처 불러오기
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

                    // 새로운 연락처 추가
                    repository.insert(phoneBook)
                }
            }

            syncedList = repository.getAllContact().toMutableList()
            withContext(Dispatchers.Main) {
                updateUI(syncedList)
            }
        }
    }

    private fun filterContacts(query: String) {
        val filteredList = syncedList.filter {
            it.name?.contains(query, ignoreCase = true) == true || it.num?.contains(query) == true
        }
        lifecycleScope.launch {
            updateUI(filteredList)
        }
    }

    private suspend fun updateUI(contactList: List<PhoneBook>) {
        withContext(Dispatchers.Main) {
            val sortedList = contactList.sortedBy { it.name }
            adapter.updateData(sortedList)
        }
    }
}
