package com.example.storyappdicoding.ui.view.detail

import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.example.storyappdicoding.data.models.ListStoryItem
import com.example.storyappdicoding.databinding.ActivityDetailBinding
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class DetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailBinding

    private lateinit var story: ListStoryItem

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        story = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(KEY_DETAIL, ListStoryItem::class.java)!!
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(KEY_DETAIL)!!
        }

        val uploadTime = try {
            val dateFormatter =
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            dateFormatter.timeZone = TimeZone.getTimeZone("UTC")
            dateFormatter.parse(story.createdAt) as Date
        } catch (e: ParseException) {
            Date()
        }
        val spare: Long = Date().time - uploadTime.time
        val secs = spare / 1000
        val mins = secs / 60
        val hours = mins / 60
        val days = hours / 24

        binding.apply {
            tvStoryName.text = story.name
            tvStoryDescription.text = story.description
            tvStoryTime.text = when (mins.toInt()) {
                0 -> " $secs seconds ago"
                in 1..59 -> " $mins mins ago"
                in 60..1440 -> " $hours hours ago"
                else -> " $days days ago"
            }

            Glide.with(root)
                .load(story.photoUrl)
                .into(ivStory)

            if (story.lat != 0.0 && story.lon != 0.0) {
                tvStoryLocation.text = getAddress(story.lat, story.lon)
                layoutLocation.isVisible = true
            } else {
                layoutLocation.isVisible = false
            }
        }
    }

    private fun getAddress(lat: Double, lng: Double): String? {
        val geocoder = Geocoder(this)
        val list = geocoder.getFromLocation(lat, lng, 1)
        return list?.get(0)!!.getAddressLine(0)
    }

    companion object {
        const val KEY_DETAIL = "key_detail"
    }
}