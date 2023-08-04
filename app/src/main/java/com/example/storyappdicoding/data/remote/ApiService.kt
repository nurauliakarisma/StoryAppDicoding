package com.example.storyappdicoding.data.remote

import com.example.storyappdicoding.data.models.Login
import com.example.storyappdicoding.data.models.Register
import com.example.storyappdicoding.data.models.Stories
import com.example.storyappdicoding.data.models.Upload
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

interface ApiService {
    @POST("login")
    @FormUrlEncoded
    suspend fun login(
        @Field("email") email: String,
        @Field("password") password: String
    ): Login

    @POST("register")
    @FormUrlEncoded
    suspend fun register(
        @Field("name") name: String,
        @Field("email") email: String,
        @Field("password") password: String
    ): Register

    @Multipart
    @POST("stories")
    suspend fun upload(
        @Header("Authorization") token: String,
        @Part file: MultipartBody.Part,
        @Part("description") description: RequestBody,
        @Part("lat") lat: RequestBody?,
        @Part("lon") long: RequestBody?
    ): Upload

    @GET("stories")
    suspend fun getStories(
        @Header("Authorization") token: String,
        @Query("page") page: Int,
        @Query("size") size: Int
    ): Stories

    @GET("stories")
    suspend fun getStoriesWithMap(
        @Header("Authorization") token: String,
        @Query("size") size: Int
    ): Stories
}