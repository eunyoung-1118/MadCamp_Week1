package com.example.madcamp_week1

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.madcamp_week1.data.db.ImageDetail
import com.example.madcamp_week1.data.repository.DatabaseProvider
import com.example.madcamp_week1.data.repository.ImageDetailRepository
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ImageFragment : Fragment() {

    private val imageUrls = mutableListOf<String>()
    private lateinit var imageAdapter: ImageAdapter
    private lateinit var repository: ImageDetailRepository

    private val getImage = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            val selectedImage: Uri? = data?.data
            selectedImage?.let {
                lifecycleScope.launch(Dispatchers.IO) {
                    saveImageToDB(it.toString())
                    withContext(Dispatchers.Main) {
                        imageUrls.add(it.toString())
                        imageAdapter.notifyItemInserted(imageUrls.size - 1)
                    }
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_image, container, false)

        // 데이터베이스 인스턴스 초기화
        val db = DatabaseProvider.getDatabase(requireContext())
        repository = ImageDetailRepository(db.imageDetailDao())

        imageAdapter = ImageAdapter(imageUrls, repository, viewLifecycleOwner.lifecycleScope) { position ->
            // Handle item click
        }

        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = imageAdapter

        // Add ItemDecoration to RecyclerView
        val spacingInPixels = resources.getDimensionPixelSize(R.dimen.recycler_item_spacing)
        recyclerView.addItemDecoration(RecyclerDecoration(spacingInPixels))

        val addButton: FloatingActionButton = view.findViewById(R.id.addButton)
        addButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            getImage.launch(intent)
        }

        // Load images from the database
        loadImagesFromDB()

        return view
    }

    private suspend fun saveImageToDB(imageUri: String) {
        val imageDetail = ImageDetail(
            url = imageUri,
            date = null,
            place = null,
            memo = null,
            imageUri = imageUri
        )
        repository.insert(imageDetail)
    }

    private fun loadImagesFromDB() {
        lifecycleScope.launch(Dispatchers.IO) {
            val images = repository.getAllImages()
            withContext(Dispatchers.Main) {
                imageUrls.clear()
                imageUrls.addAll(images.map { it.url })
                imageAdapter.notifyDataSetChanged()
            }
        }
    }
}
