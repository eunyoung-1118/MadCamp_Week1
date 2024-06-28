package com.example.madcamp_week1

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ImageFragment : Fragment() {

    private val imageUrls = mutableListOf(

        "https://example.com/image1.jpg",
        "https://example.com/image2.jpg"
        // ...
    )

    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_image, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = GridLayoutManager(context, 2)
        recyclerView.adapter = ImageAdapter(imageUrls)

        val addButton: Button = view.findViewById(R.id.addButton)
        addButton.setOnClickListener {
            (activity as? MainActivity)?.openGallery()
        }
    }

    fun setImageUri(uri: Uri) {
        imageUrls.add(uri.toString())
        recyclerView.adapter?.notifyDataSetChanged()
    }
}
