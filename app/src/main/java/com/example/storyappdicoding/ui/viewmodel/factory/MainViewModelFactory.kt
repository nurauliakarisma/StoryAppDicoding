package com.example.storyappdicoding.ui.viewmodel.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.storyappdicoding.data.db.StoryDatabase
import com.example.storyappdicoding.data.remote.ApiService
import com.example.storyappdicoding.data.repository.StoriesRepository
import com.example.storyappdicoding.ui.viewmodel.MainViewModel
import com.example.storyappdicoding.ui.viewmodel.MapsViewModel
import com.example.storyappdicoding.ui.viewmodel.UploadStoryViewModel

class MainViewModelFactory(
    private val accountToken: String,
    private val storyDatabase: StoryDatabase,
    private val apiService: ApiService,
) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")

        return when {
            modelClass.isAssignableFrom(MainViewModel::class.java) -> {
                MainViewModel(StoriesRepository(accountToken, storyDatabase, apiService)) as T
            }
            modelClass.isAssignableFrom(MapsViewModel::class.java) -> {
                MapsViewModel(StoriesRepository(accountToken, storyDatabase, apiService)) as T
            }
            modelClass.isAssignableFrom(UploadStoryViewModel::class.java) -> {
                UploadStoryViewModel(StoriesRepository(accountToken, storyDatabase, apiService)) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: " + modelClass.name)
        }
    }
}