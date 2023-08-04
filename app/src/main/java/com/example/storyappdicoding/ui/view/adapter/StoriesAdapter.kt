package com.example.storyappdicoding.ui.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.storyappdicoding.R
import com.example.storyappdicoding.data.models.ListStoryItem
import com.example.storyappdicoding.databinding.ItemStoryListLayoutBinding
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class StoriesAdapter : PagingDataAdapter<ListStoryItem, StoriesAdapter.ViewHolder>(DIFF_CALLBACK) {

    var onStoryClick: ((View, ListStoryItem) -> Unit)? = null

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val binding = ItemStoryListLayoutBinding.bind(itemView)
        fun bind(story: ListStoryItem) {
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

                Glide.with(itemView)
                    .load(story.photoUrl)
                    .into(ivStory)

                root.setOnClickListener {
                    onStoryClick?.invoke(itemView, story)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_story_list_layout, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        getItem(position).let { stories ->
            stories?.let { story ->
                holder.bind(story)
            }
        }
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ListStoryItem>() {
            override fun areItemsTheSame(
                oldStory: ListStoryItem,
                newStory: ListStoryItem,
            ): Boolean {
                return oldStory == newStory
            }

            override fun areContentsTheSame(
                oldStory: ListStoryItem,
                newStory: ListStoryItem,
            ): Boolean {
                return oldStory.id == newStory.id
            }
        }
    }
}