package com.example.madcamp_week1

import android.content.Intent
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.madcamp_week1.data.repository.DatabaseProvider
import com.example.madcamp_week1.data.repository.ImageDetailRepository
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class mapFragment : Fragment(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var repository: ImageDetailRepository
    private val markerMap = mutableMapOf<Int, Marker>()
    private val markerImageMap = mutableMapOf<Marker, String>()
    private var markerCounter = 1
    private var currentMarkerIndex = 1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_map, container, false)

        // 데이터베이스 인스턴스 초기화
        val db = DatabaseProvider.getDatabase(requireContext())
        repository = ImageDetailRepository(db.imageDetailDao())

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // 버튼 클릭 리스너 설정
        val nextMarkerButton: Button = view.findViewById(R.id.next_marker_button)
        nextMarkerButton.setOnClickListener {
            moveToNextMarker()
        }

        return view
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        Log.d("freeFragment", "Google Map is ready")

        // 마커 클릭 리스너 설정
        mMap.setOnMarkerClickListener { marker ->
            val imageUrl = markerImageMap[marker]
            imageUrl?.let {
                val intent = Intent(requireContext(), ImageDetailActivity::class.java)
                intent.putExtra("image_url", it)
                startActivity(intent)
            }
            true
        }

        // 이미지 정보를 데이터베이스에서 불러와 마커 추가
        addMarkers()
    }

    private fun addMarkers() {
        lifecycleScope.launch(Dispatchers.IO) {
            val images = repository.getAllImages()
            val geocoder = Geocoder(requireContext(), Locale.getDefault())

            Log.d("freeFragment", "Number of images: ${images.size}")

            for (image in images) {
                val place = image.place
                Log.d("freeFragment", "Processing image with URL: ${image.url} and place: $place")
                if (!place.isNullOrEmpty()) {
                    try {
                        val addresses = geocoder.getFromLocationName(place, 1)
                        Log.d("freeFragment", "Geocoding place: $place")
                        if (addresses != null && addresses.isNotEmpty()) {
                            val address = addresses[0]
                            val latLng = LatLng(address.latitude, address.longitude)
                            Log.d("freeFragment", "Found location: $latLng for place: $place")
                            withContext(Dispatchers.Main) {
                                val marker = mMap.addMarker(MarkerOptions().position(latLng).title("Marker #$markerCounter: $place"))
                                marker?.let {
                                    markerMap[markerCounter] = it
                                    markerImageMap[it] = image.url
                                    markerCounter++
                                }
                            }
                        } else {
                            Log.d("freeFragment", "No address found for place: $place")
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Log.e("freeFragment", "Error geocoding place: $place", e)
                    }
                } else {
                    Log.d("freeFragment", "Place is null or empty for image: ${image.url}")
                }
            }
        }
    }

    private fun moveToNextMarker() {
        if (markerMap.isNotEmpty()) {
            if (currentMarkerIndex > markerMap.size) {
                currentMarkerIndex = 1 // 처음으로 돌아가기
            }
            val marker = markerMap[currentMarkerIndex]
            marker?.let {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(it.position, 15f))
                currentMarkerIndex++
            } ?: run {
                Toast.makeText(requireContext(), "Marker not found", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(requireContext(), "No markers available", Toast.LENGTH_SHORT).show()
        }
    }
}
