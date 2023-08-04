package com.example.storyappdicoding.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.paging.*
import com.example.storyappdicoding.data.db.StoryDatabase
import com.example.storyappdicoding.data.models.ListStoryItem
import com.example.storyappdicoding.data.remote.ApiService
import com.example.storyappdicoding.data.remote.RemoteMediator
import com.example.storyappdicoding.utils.Result
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class StoriesRepository(
    private val accToken: String,
    private val storyDatabase: StoryDatabase,
    private val apiService: ApiService,
) {
    fun getStories(): LiveData<PagingData<ListStoryItem>> {
        val token = "Bearer $accToken"

        @OptIn(ExperimentalPagingApi::class)
        return Pager(
            config = PagingConfig(
                5
            ),
            remoteMediator = RemoteMediator(token, storyDatabase, apiService),
            pagingSourceFactory = { storyDatabase.storyDao().getAllStory() }
        ).liveData
    }

    fun getMapsStory() = liveData {
        emit(Result.Loading)
        try {
            val token = "Bearer $accToken"

            val mapsResponse = apiService.getStoriesWithMap(token, 50)
            emit(Result.Success(mapsResponse))
        } catch (e: Exception) {
            e.printStackTrace()
            emit(Result.Error(e.message.toString()))
        }
    }

    fun uploadStory(
        file: File,
        description: String,
        latitude: String?,
        longitude: String?
    ) = liveData {
        emit(Result.Loading)
        try {
            val token = "Bearer $accToken"
            val imageFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
            val imageMultipart = MultipartBody.Part.createFormData(
                "photo",
                file.name,
                imageFile
            )

            val desc = description.toRequestBody("text/plain".toMediaType())
            val lat = latitude?.toRequestBody("text/plain".toMediaType())
            val lon = longitude?.toRequestBody("text/plain".toMediaType())

            val response = apiService.upload(
                token,
                imageMultipart,
                desc,
                lat,
                lon
            )
            emit(Result.Success(response))
        } catch (e: Exception) {
            emit(Result.Error(e.message.toString()))
        }
    }
}