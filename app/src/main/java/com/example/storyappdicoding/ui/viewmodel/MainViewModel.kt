package com.example.storyappdicoding.ui.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.example.storyappdicoding.data.repository.StoriesRepository

class MainViewModel(storiesRepository: StoriesRepository) :
    ViewModel() {

    val isAllFabsVisible = MutableLiveData(false)

    val storyItems =
        storiesRepository.getStories().cachedIn(viewModelScope)
}