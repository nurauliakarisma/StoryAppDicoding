package com.example.storyappdicoding.ui.viewmodel

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.storyappdicoding.data.repository.StoriesRepository
import com.example.storyappdicoding.utils.Event
import java.io.File

class UploadStoryViewModel(private val storiesRepository: StoriesRepository) : ViewModel() {
    val imageFile = MutableLiveData<File>()
    val latCoor = MutableLiveData<Double>(null)
    val lonCoor = MutableLiveData<Double>(null)
    val descText = MutableLiveData<String>()
    val isLoading = MutableLiveData<Boolean>()
    val toastText = MutableLiveData<Event<String>>()

    val canUpload = MediatorLiveData<Boolean>().apply {
        var imageFlag = true
        var descFlag = true
        value = false
        addSource(imageFile) { x ->
            imageFlag = x == null
            value = !imageFlag && !descFlag
        }
        addSource(descText) { x ->
            descFlag = x.isNullOrEmpty()
            value = !imageFlag && !descFlag
        }
    }

    fun uploadStory(
        file: File,
        description: String,
    ) = storiesRepository.uploadStory(file,
        description,
        if (latCoor.value == null) null else latCoor.value.toString(),
        if (lonCoor.value == null) null else lonCoor.value.toString())
}