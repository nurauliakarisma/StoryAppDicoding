package com.example.storyappdicoding.ui.view.map

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Geocoder
import android.os.Bundle
import android.os.StrictMode
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.storyappdicoding.R
import com.example.storyappdicoding.data.db.StoryDatabase
import com.example.storyappdicoding.data.models.ListStoryItem
import com.example.storyappdicoding.data.remote.ApiConfig
import com.example.storyappdicoding.databinding.ActivityStoryMapsBinding
import com.example.storyappdicoding.databinding.ItemStoryTooltipBinding
import com.example.storyappdicoding.ui.view.detail.DetailActivity
import com.example.storyappdicoding.ui.view.detail.DetailActivity.Companion.KEY_DETAIL
import com.example.storyappdicoding.ui.viewmodel.MapsViewModel
import com.example.storyappdicoding.ui.viewmodel.factory.MainViewModelFactory
import com.example.storyappdicoding.utils.AccountPreferences
import com.example.storyappdicoding.utils.Result
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import java.io.IOException
import java.net.URL


class StoryMapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.InfoWindowAdapter {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityStoryMapsBinding

    private val tokenId by lazy { intent.getStringExtra(AccountPreferences.TOKEN_ID) }

    private val mapsViewModel by viewModels<MapsViewModel> {
        MainViewModelFactory(
            tokenId!!,
            StoryDatabase.getDatabaseInstance(this),
            ApiConfig.getApiService()
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityStoryMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.apply {
            uiSettings.isZoomControlsEnabled = true
            uiSettings.isIndoorLevelPickerEnabled = true
            uiSettings.isCompassEnabled = true
            uiSettings.isMapToolbarEnabled = true

            moveCamera(CameraUpdateFactory.newLatLng(indonesia))
            setInfoWindowAdapter(this@StoryMapsActivity)
        }


        markStory()
        setListeners()
    }

    override fun getInfoContents(marker: Marker): View? {
        return null
    }

    override fun getInfoWindow(marker: Marker): View {
        val tooltipBinding =
            ItemStoryTooltipBinding.inflate(LayoutInflater.from(this))
        val story: ListStoryItem = marker.tag as ListStoryItem

        tooltipBinding.apply {
            ivStory.setImageBitmap(bitmapFromUrl(story.photoUrl))
            tvStoryName.text = story.name
            tvStoryDescription.text = story.description
            tvStoryLocation.text = getAddress(story.lat, story.lon)
        }

        return tooltipBinding.root
    }

    private fun markStory() {
        mapsViewModel.getMapsStory()
            .observe(this) { result ->
                when (result) {
                    is Result.Success -> {
                        result.data.listStory?.forEach { story ->
                            mMap.addMarker(
                                MarkerOptions().position(
                                    LatLng(
                                        story!!.lat,
                                        story.lon
                                    )
                                )
                            )?.tag = story
                        }
                    }
                    is Result.Loading -> {}
                    is Result.Error -> {}
                }
            }
    }

    private fun setListeners() {
        mMap.setOnInfoWindowClickListener { marker ->
            val story: ListStoryItem = marker.tag as ListStoryItem

            val iDetail = Intent(this@StoryMapsActivity, DetailActivity::class.java)
            iDetail.putExtra(KEY_DETAIL, story)
            startActivity(iDetail)
        }
    }

    private fun getAddress(lat: Double, lng: Double): String? {
        val geocoder = Geocoder(this)
        val list = geocoder.getFromLocation(lat, lng, 1)
        return list?.get(0)!!.getAddressLine(0)
    }

    private fun bitmapFromUrl(url: String): Bitmap {
        return try {
            StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder().permitAll().build())

            val urlString = URL(url)
            return BitmapFactory.decodeStream(urlString.openConnection().getInputStream())
        } catch (e: IOException) {
            BitmapFactory.decodeResource(resources, R.drawable.placeholder)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.map_options, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.normal_type -> {
                mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
                true
            }
            R.id.satellite_type -> {
                mMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
                true
            }
            R.id.terrain_type -> {
                mMap.mapType = GoogleMap.MAP_TYPE_TERRAIN
                true
            }
            R.id.hybrid_type -> {
                mMap.mapType = GoogleMap.MAP_TYPE_HYBRID
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    companion object {
        val indonesia = LatLng(-6.2, 106.81)
    }
}