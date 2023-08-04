package com.example.storyappdicoding.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.storyappdicoding.utils.AccountPreferences
import kotlinx.coroutines.launch

class AccountSettingsViewModel(private val accountPreferences: AccountPreferences) : ViewModel() {
    fun saveToken(
        token: String,
    ) {
        viewModelScope.launch {
            accountPreferences.saveToken(token)
        }
    }

    fun getToken() = accountPreferences.getToken().asLiveData()

    fun clearToken() {
        viewModelScope.launch {
            accountPreferences.clearToken()
        }
    }
}