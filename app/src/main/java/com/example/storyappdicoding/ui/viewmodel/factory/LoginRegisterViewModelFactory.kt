package com.example.storyappdicoding.ui.viewmodel.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.storyappdicoding.data.remote.ApiService
import com.example.storyappdicoding.data.repository.LoginRegisterRepository
import com.example.storyappdicoding.ui.viewmodel.LoginViewModel
import com.example.storyappdicoding.ui.viewmodel.RegisterViewModel

class LoginRegisterViewModelFactory(
    private val apiService: ApiService
) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")

        return when {
            modelClass.isAssignableFrom(LoginViewModel::class.java) -> {
                LoginViewModel(LoginRegisterRepository(apiService)) as T
            }
            modelClass.isAssignableFrom(RegisterViewModel::class.java) -> {
                RegisterViewModel(LoginRegisterRepository(apiService)) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: " + modelClass.name)
        }
    }
}