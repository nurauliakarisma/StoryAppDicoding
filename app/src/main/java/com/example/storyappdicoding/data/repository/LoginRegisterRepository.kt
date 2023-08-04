package com.example.storyappdicoding.data.repository

import com.example.storyappdicoding.utils.Result
import androidx.lifecycle.liveData
import com.example.storyappdicoding.data.remote.ApiService

class LoginRegisterRepository(
    private val apiService: ApiService,
) {
    fun register(
        name: String,
        email: String,
        password: String,
    ) = liveData {
        emit(Result.Loading)
        try {
            val registerResponse = apiService.register(name, email, password)
            emit(Result.Success(registerResponse))
        } catch (e: Exception) {
            e.printStackTrace()
            emit(Result.Error(e.message.toString()))
        }
    }

    fun login(email: String, password: String) = liveData {
        emit(Result.Loading)
        try {
            val loginResponse = apiService.login(email, password)
            emit(Result.Success(loginResponse))
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            emit(Result.Error(e.message.toString()))
        }
    }
}