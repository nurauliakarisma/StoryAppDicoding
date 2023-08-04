package com.example.storyappdicoding.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.storyappdicoding.data.repository.StoriesRepository

class MapsViewModel(private val storiesRepository: StoriesRepository) :
    ViewModel() {

    fun getMapsStory() = storiesRepository.getMapsStory()
}